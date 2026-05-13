package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.gui.*;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GUIListener implements Listener {

    private final OneBlock plugin;
    private final IslandMenuGUI menuGUI;
    private final IslandMembersGUI membersGUI;
    private final IslandSettingsGUI settingsGUI;
    private final IslandUpgradesGUI upgradesGUI;
    private final IslandWarpGUI warpGUI;
    private final ChallengesGUI challengesGUI;

    public GUIListener(OneBlock plugin) {
        this.plugin = plugin;
        this.menuGUI = new IslandMenuGUI(plugin);
        this.membersGUI = new IslandMembersGUI(plugin);
        this.settingsGUI = new IslandSettingsGUI(plugin);
        this.upgradesGUI = new IslandUpgradesGUI(plugin);
        this.warpGUI = new IslandWarpGUI(plugin);
        this.challengesGUI = new ChallengesGUI(plugin);
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        String title = ChatColor.stripColor(event.getView().getTitle());

        if (title.equals(ChatColor.stripColor(IslandMenuGUI.TITLE))) {
            event.setCancelled(true);
            handleMenuGUI(player, event.getSlot());
        } else if (title.equals(ChatColor.stripColor(IslandMembersGUI.TITLE))) {
            event.setCancelled(true);
            handleMembersGUI(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(ChatColor.stripColor(IslandSettingsGUI.TITLE))) {
            event.setCancelled(true);
            handleSettingsGUI(player, event.getSlot());
        } else if (title.equals(ChatColor.stripColor(IslandUpgradesGUI.TITLE))) {
            event.setCancelled(true);
            handleUpgradesGUI(player, event.getSlot());
        } else if (title.equals(ChatColor.stripColor(IslandWarpGUI.TITLE))) {
            event.setCancelled(true);
            handleWarpGUI(player, event.getSlot(), event.getCurrentItem(), 0);
        } else if (title.equals(ChatColor.stripColor(ChallengesGUI.TITLE))) {
            event.setCancelled(true);
            handleChallengesGUI(player, event.getSlot(), event.getCurrentItem());
        }
    }

    private void handleMenuGUI(Player player, int slot) {
        switch (slot) {
            case 19 -> membersGUI.open(player);
            case 21 -> upgradesGUI.open(player);
            case 23 -> challengesGUI.open(player);
            case 25 -> warpGUI.open(player, 0);
            case 31 -> {
                OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
                if (island != null && !island.isOwner(player.getUniqueId())) {
                    player.sendMessage(c("&cSeul le propriétaire peut modifier les paramètres."));
                    return;
                }
                settingsGUI.open(player);
            }
            case 49 -> {
                player.closeInventory();
                OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
                if (island != null) {
                    player.teleport(island.getHome());
                    player.sendMessage(c("&aTéléporté vers ton île !"));
                }
            }
        }
    }

    private void handleMembersGUI(Player player, int slot, ItemStack item) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        if (slot == 45) { menuGUI.open(player); return; }

        if (slot == 49) {
            if (!island.isOwner(player.getUniqueId())) {
                player.closeInventory();
                boolean left = plugin.getOneBlockManager().leaveIsland(player.getUniqueId());
                if (left) {
                    player.sendMessage(c("&cTu as quitté l'île."));
                } else {
                    player.sendMessage(c("&cErreur lors du départ de l'île."));
                }
            }
            return;
        }

        if (slot < 45 && item != null && item.getType().name().contains("PLAYER_HEAD")) {
            if (!island.isOwner(player.getUniqueId())) return;
            if (item.getItemMeta() == null) return;
            String memberName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (memberName == null || memberName.isEmpty()) return;

            for (UUID memberUuid : island.getMembers()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(memberUuid);
                if (memberName.equals(op.getName())) {
                    boolean kicked = plugin.getOneBlockManager().kickMember(island.getOwner(), memberUuid);
                    if (kicked) {
                        player.sendMessage(c("&e" + memberName + " &7a été expulsé de l'île."));
                        Player online = Bukkit.getPlayer(memberUuid);
                        if (online != null) online.sendMessage(c("&cTu as été expulsé de l'île de &e" + player.getName() + "&c."));
                        membersGUI.open(player);
                    }
                    return;
                }
            }
        }
    }

    private void handleSettingsGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (!island.isOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire peut modifier les paramètres."));
            return;
        }

        switch (slot) {
            case 10 -> {
                island.setPvpEnabled(!island.isPvpEnabled());
                player.sendMessage(c("&7PvP : " + (island.isPvpEnabled() ? "&aActivé" : "&cDésactivé")));
                plugin.getOneBlockManager().saveAll();
                settingsGUI.open(player);
            }
            case 12 -> {
                island.setVisitorsAllowed(!island.isVisitorsAllowed());
                player.sendMessage(c("&7Visiteurs : " + (island.isVisitorsAllowed() ? "&aAutorisés" : "&cBloqués")));
                plugin.getOneBlockManager().saveAll();
                settingsGUI.open(player);
            }
            case 14 -> {
                island.setWarpEnabled(!island.isWarpEnabled());
                player.sendMessage(c("&7Warp public : " + (island.isWarpEnabled() ? "&aActivé" : "&cDésactivé")));
                plugin.getOneBlockManager().saveAll();
                settingsGUI.open(player);
            }
            case 22 -> menuGUI.open(player);
        }
    }

    private void handleUpgradesGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (!island.isOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire peut acheter des améliorations."));
            return;
        }

        int[] slots = {10, 12, 14, 16};
        UpgradeType[] types = UpgradeType.values();

        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i] && i < types.length) {
                UpgradeType type = types[i];
                int level = island.getUpgradeLevel(type);
                if (level >= type.getMaxLevel()) {
                    player.sendMessage(c("&cCette amélioration est déjà au niveau maximum !"));
                    return;
                }
                int cost = type.getCost(level);
                boolean success = plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost);
                if (!success) {
                    player.sendMessage(c("&cTu n'as pas assez de pièces ! &7(" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + "/" + cost + ")"));
                    return;
                }
                island.setUpgradeLevel(type, level + 1);
                island.addChallengeProgress(IslandChallenge.ChallengeType.UPGRADES_BOUGHT, 1);
                if (island.getUpgradeLevel(type) >= type.getMaxLevel()) {
                    island.addChallengeProgress(IslandChallenge.ChallengeType.UPGRADES_MAX, 1);
                }
                plugin.getOneBlockManager().saveAll();
                player.sendMessage(c("&a&l✔ &a" + type.getDisplayName() + " &7amélioré au niveau &e" + (level + 1) + "&7 !"));
                upgradesGUI.open(player);
                return;
            }
        }

        if (slot == 26) menuGUI.open(player);
    }

    private void handleWarpGUI(Player player, int slot, ItemStack item, int page) {
        if (slot == 45) { menuGUI.open(player); return; }
        if (slot == 48 && page > 0) { warpGUI.open(player, page - 1); return; }
        if (slot == 50) { warpGUI.open(player, page + 1); return; }

        if (slot < 45 && item != null && item.getType().name().contains("PLAYER_HEAD")) {
            if (item.getItemMeta() == null) return;
            String warpName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (warpName == null) return;

            for (OneBlockIsland isl : plugin.getOneBlockManager().getPublicWarps()) {
                if (!isl.isVisitorsAllowed()) continue;
                String name = isl.getWarpName().isEmpty()
                    ? "Île de " + plugin.getOneBlockManager().getOwnerName(isl)
                    : isl.getWarpName();
                if (warpName.equals(name)) {
                    if (!plugin.isInPluginWorld(player)) {
                        player.sendMessage(c("&cTu dois être dans le monde OneBlock pour utiliser les warps."));
                        return;
                    }
                    player.closeInventory();
                    player.teleport(isl.getHome());
                    player.sendMessage(c("&aTéléporté vers le warp &e" + warpName + "&a !"));
                    return;
                }
            }
        }
    }

    private void handleChallengesGUI(Player player, int slot, ItemStack item) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        IslandChallenge[] challenges = IslandChallenge.values();
        if (slot < challenges.length) {
            IslandChallenge ch = challenges[slot];
            if (island.isChallengeClaimable(ch)) {
                island.completeChallenge(ch);
                plugin.getEconomyManager().addBalance(player.getUniqueId(), ch.getReward());
                plugin.getOneBlockManager().saveAll();
                player.sendMessage(c("&a&l✔ Défi complété : &e" + ch.getDisplayName() + " &a+&e" + ch.getReward() + " &apièces !"));
                challengesGUI.open(player);
            } else if (island.isChallengeCompleted(ch)) {
                player.sendMessage(c("&7Ce défi est déjà complété."));
            } else {
                player.sendMessage(c("&7Défi non encore complété. Progression : &e"
                    + island.getChallengeProgress(ch) + "&7/&e" + ch.getTarget()));
            }
            return;
        }

        int rows = Math.max(3, (int) Math.ceil((challenges.length + 9) / 9.0) + 1);
        rows = Math.min(rows, 6);
        int bottom = (rows - 1) * 9;
        if (slot == bottom) menuGUI.open(player);
    }
}
