package com.astraland.admintools.listener;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.gui.PlayerInventoryGUI;
import com.astraland.admintools.gui.PlayerListGUI;
import com.astraland.admintools.gui.PlayerActionsGUI;
import com.astraland.admintools.gui.PlayerStatsGUI;
import com.astraland.admintools.gui.holder.PlayerActionsHolder;
import com.astraland.admintools.gui.holder.PlayerInventoryHolder;
import com.astraland.admintools.gui.holder.PlayerListHolder;
import com.astraland.admintools.gui.holder.PlayerStatsHolder;
import com.astraland.admintools.session.AdminSession;
import com.astraland.admintools.session.FilterMode;
import com.astraland.admintools.session.PendingInput;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class GUIListener implements Listener {

    private final AdminTools plugin;

    public GUIListener(AdminTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof PlayerListHolder) {
            handlePlayerList(event, admin);
        } else if (holder instanceof PlayerActionsHolder) {
            handlePlayerActions(event, admin);
        } else if (holder instanceof PlayerInventoryHolder h) {
            handlePlayerInventory(event, admin, h);
        } else if (holder instanceof PlayerStatsHolder h) {
            handlePlayerStats(event, admin, h);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof PlayerListHolder || holder instanceof PlayerActionsHolder || holder instanceof PlayerStatsHolder) {
            event.setCancelled(true);
        } else if (holder instanceof PlayerInventoryHolder) {
            for (int slot : event.getRawSlots()) {
                if (PlayerInventoryGUI.isControlSlot(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player admin)) return;
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof PlayerInventoryHolder h) {
            plugin.getPlayerInventoryGUI().saveToPlayer(admin, event.getInventory(), h.getTargetUUID());
        }
    }

    private void handlePlayerList(InventoryClickEvent event, Player admin) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        AdminSession session = plugin.getSession(admin);

        if (slot < 0 || slot >= 54) return;

        if (slot < PlayerListGUI.getPlayerSlots()) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR
                    || event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;
            java.util.List<Player> filtered = getFilteredPlayers(session);
            int index = session.getPage() * PlayerListGUI.getPlayerSlots() + slot;
            if (index >= 0 && index < filtered.size()) {
                Player target = filtered.get(index);
                plugin.getPlayerActionsGUI().open(admin, target.getUniqueId());
            }
            return;
        }

        for (FilterMode mode : FilterMode.values()) {
            if (slot == plugin.getPlayerListGUI().getFilterSlotForMode(mode)) {
                session.setFilterMode(mode);
                session.setPage(0);
                plugin.getPlayerListGUI().open(admin);
                return;
            }
        }

        if (slot == PlayerListGUI.getSlotSetFilter()) {
            admin.closeInventory();
            session.setPendingInput(PendingInput.FILTER_TEXT);
            admin.sendMessage(ItemBuilder.c("&7Tapez le texte de filtre dans le chat. (&cTapez &eannuler &cpour annuler&c)"));
            return;
        }

        if (slot == PlayerListGUI.getSlotClearFilter()) {
            session.setFilterText("");
            session.setPage(0);
            admin.sendMessage(ItemBuilder.c("&aFiltre effacé."));
            plugin.getPlayerListGUI().open(admin);
            return;
        }

        if (slot == PlayerListGUI.getSlotPrev()) {
            if (session.getPage() > 0) {
                session.setPage(session.getPage() - 1);
                plugin.getPlayerListGUI().open(admin);
            }
            return;
        }

        if (slot == PlayerListGUI.getSlotNext()) {
            java.util.List<Player> filtered = getFilteredPlayers(session);
            int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PlayerListGUI.getPlayerSlots()));
            if (session.getPage() < totalPages - 1) {
                session.setPage(session.getPage() + 1);
                plugin.getPlayerListGUI().open(admin);
            }
        }
    }

    private void handlePlayerActions(InventoryClickEvent event, Player admin) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        AdminSession session = plugin.getSession(admin);
        UUID targetUUID = session.getTargetPlayerUUID();
        if (targetUUID == null) return;

        if (slot == PlayerActionsGUI.getSlotInventory()) {
            plugin.getPlayerInventoryGUI().open(admin, targetUUID);

        } else if (slot == PlayerActionsGUI.getSlotStats()) {
            plugin.getPlayerStatsGUI().open(admin, targetUUID);

        } else if (slot == PlayerActionsGUI.getSlotDiscordScreen()) {
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            sendDiscordScreen(admin, target);

        } else if (slot == PlayerActionsGUI.getSlotDiscordChat()) {
            sendDiscordChat(admin);

        } else if (slot == PlayerActionsGUI.getSlotClearInv()) {
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            target.getInventory().clear();
            target.updateInventory();
            admin.sendMessage(ItemBuilder.c("&a✔ Inventaire de &b" + target.getName() + " &avidé."));
            target.sendMessage(ItemBuilder.c("&c&lVotre inventaire a été vidé par un administrateur."));
            plugin.getPlayerActionsGUI().open(admin, targetUUID);

        } else if (slot == PlayerActionsGUI.getSlotCustomScreen()) {
            admin.closeInventory();
            session.setPendingInput(PendingInput.SEND_SCREEN_MSG);
            admin.sendMessage(ItemBuilder.c("&7Tapez le message à afficher sur l'écran de &b"
                    + Bukkit.getOfflinePlayer(targetUUID).getName()
                    + "&7. (&cTapez &eannuler &cpour annuler&c)"));

        } else if (slot == PlayerActionsGUI.getSlotCustomChat()) {
            admin.closeInventory();
            session.setPendingInput(PendingInput.SEND_CHAT_MSG);
            admin.sendMessage(ItemBuilder.c("&7Tapez le message à diffuser dans le chat public. (&cTapez &eannuler &cpour annuler&c)"));

        } else if (slot == PlayerActionsGUI.getSlotBack()) {
            plugin.getPlayerListGUI().open(admin);
        }
    }

    private void handlePlayerInventory(InventoryClickEvent event, Player admin, PlayerInventoryHolder holder) {
        int slot = event.getRawSlot();

        if (slot == PlayerInventoryGUI.getSlotBack()) {
            event.setCancelled(true);
            Inventory inv = event.getInventory();
            UUID targetUUID = holder.getTargetUUID();
            admin.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getPlayerActionsGUI().open(admin, targetUUID), 1L);
            return;
        }

        if (PlayerInventoryGUI.isControlSlot(slot)) {
            event.setCancelled(true);
        }
    }

    private void handlePlayerStats(InventoryClickEvent event, Player admin, PlayerStatsHolder holder) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        UUID targetUUID = holder.getTargetUUID();
        AdminSession session = plugin.getSession(admin);
        session.setTargetPlayerUUID(targetUUID);

        Player target = Bukkit.getPlayer(targetUUID);

        if (slot == PlayerStatsGUI.getSlotHealth()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            admin.closeInventory();
            session.setPendingInput(PendingInput.HEALTH);
            admin.sendMessage(ItemBuilder.c("&7Entrez la nouvelle valeur de &cvie &7pour &b" + target.getName()
                    + " &7(actuel : &c" + String.format("%.1f", target.getHealth()) + "&7). (&cannuler&7 pour quitter)"));

        } else if (slot == PlayerStatsGUI.getSlotMaxHealth()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            admin.closeInventory();
            session.setPendingInput(PendingInput.MAX_HEALTH);
            admin.sendMessage(ItemBuilder.c("&7Entrez la nouvelle &6vie maximum &7pour &b" + target.getName()
                    + " &7(actuel : &6" + String.format("%.1f", target.getMaxHealth()) + "&7). (&cannuler&7 pour quitter)"));

        } else if (slot == PlayerStatsGUI.getSlotFood()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            admin.closeInventory();
            session.setPendingInput(PendingInput.FOOD);
            admin.sendMessage(ItemBuilder.c("&7Entrez le nouveau niveau de &efaim &7pour &b" + target.getName()
                    + " &7(actuel : &e" + target.getFoodLevel() + "&7, entre 0 et 20). (&cannuler&7 pour quitter)"));

        } else if (slot == PlayerStatsGUI.getSlotXpLevel()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            admin.closeInventory();
            session.setPendingInput(PendingInput.XP_LEVEL);
            admin.sendMessage(ItemBuilder.c("&7Entrez le nouveau &aniveau XP &7pour &b" + target.getName()
                    + " &7(actuel : &a" + target.getLevel() + "&7). (&cannuler&7 pour quitter)"));

        } else if (slot == PlayerStatsGUI.getSlotGameMode()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            GameMode next = nextGameMode(target.getGameMode());
            target.setGameMode(next);
            admin.sendMessage(ItemBuilder.c("&a✔ Mode de jeu de &b" + target.getName() + " &achangé : &b" + gameModeName(next)));
            plugin.getPlayerStatsGUI().open(admin, targetUUID);

        } else if (slot == PlayerStatsGUI.getSlotFly()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            boolean newFly = !target.getAllowFlight();
            target.setAllowFlight(newFly);
            if (!newFly) target.setFlying(false);
            admin.sendMessage(ItemBuilder.c("&a✔ Vol de &b" + target.getName() + (newFly ? " &aactivé." : " &cdésactivé.")));
            plugin.getPlayerStatsGUI().open(admin, targetUUID);

        } else if (slot == PlayerStatsGUI.getSlotGodMode()) {
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            boolean newInvul = !target.isInvulnerable();
            target.setInvulnerable(newInvul);
            admin.sendMessage(ItemBuilder.c("&a✔ Invulnérabilité de &b" + target.getName() + (newInvul ? " &aactivée." : " &cdésactivée.")));
            plugin.getPlayerStatsGUI().open(admin, targetUUID);

        } else if (slot == PlayerStatsGUI.getSlotBack()) {
            plugin.getPlayerActionsGUI().open(admin, targetUUID);
        }
    }

    private void sendDiscordScreen(Player admin, Player target) {
        String lien = plugin.getConfig().getString("discord.lien", "discord.gg/astraland");
        String titre = plugin.getConfig().getString("discord.message-ecran-titre", "&6&lViens sur Discord !");
        String sousTitre = plugin.getConfig().getString("discord.message-ecran-sous-titre", "&7{lien}");

        titre = ItemBuilder.c(titre);
        sousTitre = ItemBuilder.c(sousTitre.replace("{lien}", lien));

        target.sendTitle(titre, sousTitre, 10, 80, 20);
        target.sendActionBar(ItemBuilder.c("&6» &fdiscord.gg/" + lien.replace("discord.gg/", "") + " &6«"));
        target.playSound(target.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        if (plugin.getConfig().getBoolean("discord.particules", true)) {
            spawnParticles(target);
        }

        admin.sendMessage(ItemBuilder.c("&a✔ Message &6Discord &aaffiché sur l'écran de &b" + target.getName() + "&a."));
        plugin.getPlayerActionsGUI().open(admin, target.getUniqueId());
    }

    private void sendDiscordChat(Player admin) {
        String lien = plugin.getConfig().getString("discord.lien", "discord.gg/astraland");
        String msg  = plugin.getConfig().getString("discord.message-chat", "&8[&6AstraLand&8] &fViens sur Discord ! &7{lien}");
        msg = ItemBuilder.c(msg.replace("{lien}", lien));

        Bukkit.broadcastMessage(msg);
        admin.sendMessage(ItemBuilder.c("&a✔ Message &6Discord &adiffusé dans le chat public."));
    }

    private void spawnParticles(Player target) {
        Location loc = target.getLocation().add(0, 1, 0);
        int count = 40;
        double radius = 1.5;
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location pLoc = loc.clone().add(x, 0, z);
            target.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, pLoc, 3, 0, 0, 0, 0.05);
        }
        target.getWorld().spawnParticle(Particle.FIREWORK, loc, 20, 0.3, 0.5, 0.3, 0.1);
    }

    private GameMode nextGameMode(GameMode current) {
        return switch (current) {
            case SURVIVAL  -> GameMode.CREATIVE;
            case CREATIVE  -> GameMode.ADVENTURE;
            case ADVENTURE -> GameMode.SPECTATOR;
            case SPECTATOR -> GameMode.SURVIVAL;
        };
    }

    private String gameModeName(GameMode gm) {
        return switch (gm) {
            case SURVIVAL  -> "Survie";
            case CREATIVE  -> "Créatif";
            case ADVENTURE -> "Aventure";
            case SPECTATOR -> "Spectateur";
        };
    }

    private java.util.List<Player> getFilteredPlayers(AdminSession session) {
        java.util.List<Player> result = new java.util.ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (session.getFilterMode().matches(p.getName(), session.getFilterText())) {
                result.add(p);
            }
        }
        result.sort(java.util.Comparator.comparing(Player::getName));
        return result;
    }
}
