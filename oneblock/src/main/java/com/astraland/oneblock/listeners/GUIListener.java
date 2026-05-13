package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.gui.*;
import com.astraland.oneblock.models.DailyMission;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class GUIListener implements Listener {

    private final OneBlock plugin;
    private final IslandMenuGUI menuGUI;
    private final IslandMembersGUI membersGUI;
    private final IslandSettingsGUI settingsGUI;
    private final IslandUpgradesGUI upgradesGUI;
    private final IslandWarpGUI warpGUI;
    private final ChallengesGUI challengesGUI;
    private final SkillsGUI skillsGUI;
    private final IslandBankGUI bankGUI;
    private final PrestigeGUI prestigeGUI;
    private final DailyMissionsGUI missionsGUI;
    private final IslandStatsGUI statsGUI;

    public GUIListener(OneBlock plugin) {
        this.plugin = plugin;
        this.menuGUI = new IslandMenuGUI(plugin);
        this.membersGUI = new IslandMembersGUI(plugin);
        this.settingsGUI = new IslandSettingsGUI(plugin);
        this.upgradesGUI = new IslandUpgradesGUI(plugin);
        this.warpGUI = new IslandWarpGUI(plugin);
        this.challengesGUI = new ChallengesGUI(plugin);
        this.skillsGUI = new SkillsGUI(plugin);
        this.bankGUI = new IslandBankGUI(plugin);
        this.prestigeGUI = new PrestigeGUI(plugin);
        this.missionsGUI = new DailyMissionsGUI(plugin);
        this.statsGUI = new IslandStatsGUI(plugin);
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String strip(String s) { return ChatColor.stripColor(s); }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        String title = strip(event.getView().getTitle());

        if (title.equals(strip(IslandMenuGUI.TITLE))) {
            event.setCancelled(true); handleMenuGUI(player, event.getSlot());
        } else if (title.equals(strip(IslandMembersGUI.TITLE))) {
            event.setCancelled(true); handleMembersGUI(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(strip(IslandSettingsGUI.TITLE))) {
            event.setCancelled(true); handleSettingsGUI(player, event.getSlot());
        } else if (title.equals(strip(IslandUpgradesGUI.TITLE))) {
            event.setCancelled(true); handleUpgradesGUI(player, event.getSlot());
        } else if (title.equals(strip(IslandWarpGUI.TITLE))) {
            event.setCancelled(true); handleWarpGUI(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(strip(ChallengesGUI.TITLE))) {
            event.setCancelled(true); handleChallengesGUI(player, event.getSlot());
        } else if (title.equals(strip(SkillsGUI.TITLE))) {
            event.setCancelled(true); if (event.getSlot() == 18) menuGUI.open(player);
        } else if (title.equals(strip(IslandBankGUI.TITLE))) {
            event.setCancelled(true); handleBankGUI(player, event.getSlot(), event.getClick());
        } else if (title.equals(strip(PrestigeGUI.TITLE))) {
            event.setCancelled(true); handlePrestigeGUI(player, event.getSlot());
        } else if (title.equals(strip(DailyMissionsGUI.TITLE))) {
            event.setCancelled(true); handleMissionsGUI(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(strip(IslandStatsGUI.TITLE))) {
            event.setCancelled(true); if (event.getSlot() == 27) menuGUI.open(player);
        }
    }

    private void handleMenuGUI(Player player, int slot) {
        switch (slot) {
            case 19 -> membersGUI.open(player);
            case 21 -> upgradesGUI.open(player);
            case 23 -> challengesGUI.open(player);
            case 25 -> warpGUI.open(player, 0);
            case 28 -> skillsGUI.open(player);
            case 30 -> missionsGUI.open(player);
            case 32 -> bankGUI.open(player);
            case 34 -> prestigeGUI.open(player);
            case 37 -> {
                OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
                if (island != null && !island.isOwner(player.getUniqueId())) {
                    player.sendMessage(c("&cSeul le propriétaire peut modifier les paramètres.")); return;
                }
                settingsGUI.open(player);
            }
            case 43 -> statsGUI.open(player);
            case 49 -> {
                player.closeInventory();
                OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
                if (island != null) { player.teleport(island.getHome()); player.sendMessage(c("&aTéléporté vers ton île !")); }
            }
        }
    }

    private void handleMembersGUI(Player player, int slot, ItemStack item) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (slot == 45) { menuGUI.open(player); return; }
        if (slot == 49 && !island.isOwner(player.getUniqueId())) {
            player.closeInventory();
            boolean left = plugin.getOneBlockManager().leaveIsland(player.getUniqueId());
            player.sendMessage(left ? c("&cTu as quitté l'île.") : c("&cErreur lors du départ."));
            return;
        }
        if (slot < 45 && item != null && item.getType().name().contains("PLAYER_HEAD")
            && island.isOwner(player.getUniqueId()) && item.getItemMeta() != null) {
            String memberName = strip(item.getItemMeta().getDisplayName());
            for (UUID mu : island.getMembers()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(mu);
                if (memberName.equals(op.getName())) {
                    plugin.getOneBlockManager().kickMember(island.getOwner(), mu);
                    player.sendMessage(c("&e" + memberName + " &7expulsé de l'île."));
                    Player online = Bukkit.getPlayer(mu);
                    if (online != null) online.sendMessage(c("&cTu as été expulsé de l'île de &e" + player.getName()));
                    membersGUI.open(player);
                    return;
                }
            }
        }
    }

    private void handleSettingsGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (!island.isOwner(player.getUniqueId())) { player.sendMessage(c("&cSeul le propriétaire peut modifier les paramètres.")); return; }
        switch (slot) {
            case 10 -> { island.setPvpEnabled(!island.isPvpEnabled()); plugin.getOneBlockManager().saveAll(); player.sendMessage(c("&7PvP : " + (island.isPvpEnabled() ? "&aActivé" : "&cDésactivé"))); settingsGUI.open(player); }
            case 12 -> { island.setVisitorsAllowed(!island.isVisitorsAllowed()); plugin.getOneBlockManager().saveAll(); player.sendMessage(c("&7Visiteurs : " + (island.isVisitorsAllowed() ? "&aAutorisés" : "&cBloqués"))); settingsGUI.open(player); }
            case 14 -> { island.setWarpEnabled(!island.isWarpEnabled()); plugin.getOneBlockManager().saveAll(); player.sendMessage(c("&7Warp : " + (island.isWarpEnabled() ? "&aActivé" : "&cDésactivé"))); settingsGUI.open(player); }
            case 22 -> menuGUI.open(player);
        }
    }

    private void handleUpgradesGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (!island.isOwner(player.getUniqueId()) && !island.isCoOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire/co-proprio peut acheter des améliorations.")); return;
        }
        int[] slots = {10, 12, 14, 16};
        UpgradeType[] types = UpgradeType.values();
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i] && i < types.length) {
                UpgradeType type = types[i];
                int level = island.getUpgradeLevel(type);
                if (level >= type.getMaxLevel()) { player.sendMessage(c("&cAmélioration déjà au maximum !")); return; }
                int cost = type.getCost(level);
                if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost)) {
                    player.sendMessage(c("&cPas assez de pièces ! (" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + "/" + cost + ")")); return;
                }
                island.setUpgradeLevel(type, level + 1);
                island.addChallengeProgress(IslandChallenge.ChallengeType.UPGRADES_BOUGHT, 1);
                if (island.getUpgradeLevel(type) >= type.getMaxLevel())
                    island.addChallengeProgress(IslandChallenge.ChallengeType.UPGRADES_MAX, 1);
                plugin.getDailyMissionManager().addProgress(player.getUniqueId(), DailyMission.MissionType.BUY_UPGRADES, 1);
                plugin.getOneBlockManager().saveAll();
                player.sendMessage(c("&a✔ " + type.getDisplayName() + " amélioré au niveau &e" + (level + 1) + "&a !"));
                upgradesGUI.open(player);
                return;
            }
        }
        if (slot == 26) menuGUI.open(player);
    }

    private void handleWarpGUI(Player player, int slot, ItemStack item) {
        if (slot == 45) { menuGUI.open(player); return; }
        if (slot < 45 && item != null && item.getType().name().contains("PLAYER_HEAD") && item.getItemMeta() != null) {
            String warpName = strip(item.getItemMeta().getDisplayName());
            for (OneBlockIsland isl : plugin.getOneBlockManager().getPublicWarps()) {
                if (!isl.isVisitorsAllowed()) continue;
                String name = isl.getWarpName().isEmpty()
                    ? "Île de " + plugin.getOneBlockManager().getOwnerName(isl)
                    : isl.getWarpName();
                if (warpName.equals(name)) {
                    player.closeInventory();
                    player.teleport(isl.getHome());
                    player.sendMessage(c("&aTéléporté vers &e" + name + "&a !"));
                    if (!isl.getMotd().isEmpty())
                        player.sendMessage(c("&8[MOTD] &7" + isl.getMotd()));
                    return;
                }
            }
        }
    }

    private void handleChallengesGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        IslandChallenge[] challenges = IslandChallenge.values();
        if (slot < challenges.length) {
            IslandChallenge ch = challenges[slot];
            if (island.isChallengeClaimable(ch)) {
                island.completeChallenge(ch);
                plugin.getEconomyManager().addBalance(player.getUniqueId(), ch.getReward());
                plugin.getOneBlockManager().saveAll();
                player.sendMessage(c("&a✔ Défi : &e" + ch.getDisplayName() + " &a+&e" + ch.getReward() + " &apièces !"));
                challengesGUI.open(player);
            } else if (!island.isChallengeCompleted(ch)) {
                player.sendMessage(c("&7Progression : &e" + island.getChallengeProgress(ch) + "&7/&e" + ch.getTarget()));
            }
            return;
        }
        int rows = Math.min(6, Math.max(3, (int) Math.ceil((challenges.length + 9) / 9.0) + 1));
        if (slot == (rows - 1) * 9) menuGUI.open(player);
    }

    private void handleBankGUI(Player player, int slot, ClickType click) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (slot == 22) { menuGUI.open(player); return; }

        if (slot == 11) {
            int amount = click.isShiftClick() ? plugin.getEconomyManager().getBalance(player.getUniqueId())
                : click.isRightClick() ? 1000 : 100;
            if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), amount)) {
                player.sendMessage(c("&cPas assez de pièces personnelles.")); return;
            }
            island.depositToBank(amount);
            plugin.getOneBlockManager().saveAll();
            player.sendMessage(c("&a+&e" + amount + " &apièces déposées dans la banque de l'île."));
            bankGUI.open(player);
        } else if (slot == 15) {
            if (!island.isOwner(player.getUniqueId()) && !island.isCoOwner(player.getUniqueId())) {
                player.sendMessage(c("&cSeuls le propriétaire et les co-propriétaires peuvent retirer.")); return;
            }
            int amount = click.isShiftClick() ? (int) Math.min(island.getBankBalance(), Integer.MAX_VALUE)
                : click.isRightClick() ? 1000 : 100;
            if (!island.withdrawFromBank(amount)) {
                player.sendMessage(c("&cPas assez de pièces dans la banque.")); return;
            }
            plugin.getEconomyManager().addBalance(player.getUniqueId(), amount);
            plugin.getOneBlockManager().saveAll();
            player.sendMessage(c("&e" + amount + " &7pièces retirées de la banque."));
            bankGUI.open(player);
        }
    }

    private void handlePrestigeGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (slot == 22) { menuGUI.open(player); return; }
        if (slot == 13) {
            if (!island.isOwner(player.getUniqueId())) {
                player.sendMessage(c("&cSeul le propriétaire peut effectuer le prestige.")); return;
            }
            boolean done = plugin.getOneBlockManager().prestigeIsland(island.getOwner());
            if (done) {
                player.closeInventory();
                int p = island.getPrestige();
                player.sendTitle(c("&d&l✦ PRESTIGE " + p + " ✦"),
                    c("&7Multiplicateur : &e×" + String.format("%.1f", island.getPrestigeMultiplier())), 10, 80, 20);
                player.sendMessage(c("&d&l★ Prestige " + p + " accompli ! &7Multiplicateur : &e×" + String.format("%.1f", island.getPrestigeMultiplier())));
                Bukkit.broadcastMessage(c("&d[PRESTIGE] &e" + player.getName() + " &7a atteint le &dPrestige " + p + " &7sur OneBlock !"));
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.8f);
            } else {
                player.sendMessage(c("&cConditions non remplies pour le prestige."));
            }
        }
    }

    private void handleMissionsGUI(Player player, int slot, ItemStack item) {
        if (slot == 27) { menuGUI.open(player); return; }
        List<DailyMission> missions = plugin.getDailyMissionManager().getDailyMissions(player.getUniqueId());
        int[] slots = {10, 12, 14, 16, 13};
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i] && i < missions.size()) {
                DailyMission m = missions.get(i);
                if (m.isClaimable()) {
                    plugin.getDailyMissionManager().claimMission(player.getUniqueId(), m.getId());
                    plugin.getEconomyManager().addBalance(player.getUniqueId(), m.getReward());
                    plugin.getOneBlockManager().saveAll();
                    player.sendMessage(c("&a✔ Mission : &e" + m.getDisplayName() + " &a+&e" + m.getReward() + " &apièces !"));
                    missionsGUI.open(player);
                } else if (m.isClaimed()) {
                    player.sendMessage(c("&7Mission déjà récupérée."));
                } else {
                    player.sendMessage(c("&7Progression : &e" + m.getProgress() + "&7/&e" + m.getTarget()));
                }
                return;
            }
        }
    }
}
