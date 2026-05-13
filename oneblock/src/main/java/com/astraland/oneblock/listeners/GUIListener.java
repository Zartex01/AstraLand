package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.gui.*;
import com.astraland.oneblock.managers.IslandVaultManager;
import com.astraland.oneblock.managers.PlayerStatsManager;
import com.astraland.oneblock.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
    private final CollectionsGUI collectionsGUI;
    private final AchievementsGUI achievementsGUI;
    private final BoostGUI boostGUI;
    private final WeeklyMissionsGUI weeklyGUI;
    private final PlayerStatsGUI playerStatsGUI;
    private final IslandVaultGUI vaultGUI;

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
        this.collectionsGUI = new CollectionsGUI(plugin);
        this.achievementsGUI = new AchievementsGUI(plugin);
        this.boostGUI = new BoostGUI(plugin);
        this.weeklyGUI = new WeeklyMissionsGUI(plugin);
        this.playerStatsGUI = new PlayerStatsGUI(plugin);
        this.vaultGUI = new IslandVaultGUI(plugin);
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
            event.setCancelled(true); handleMissionsGUI(player, event.getSlot());
        } else if (title.equals(strip(IslandStatsGUI.TITLE))) {
            event.setCancelled(true); if (event.getSlot() == 27) menuGUI.open(player);
        } else if (title.equals(strip(CollectionsGUI.TITLE))) {
            event.setCancelled(true); handleCollectionsGUI(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(strip(AchievementsGUI.TITLE))) {
            event.setCancelled(true);
            int backSlot = (int)(Math.min(6, Math.max(4, Math.ceil((OBAchievement.values().length + 9) / 9.0) + 1)) - 1) * 9;
            if (event.getSlot() == backSlot) menuGUI.open(player);
        } else if (title.equals(strip(BoostGUI.TITLE))) {
            event.setCancelled(true); handleBoostGUI(player, event.getSlot());
        } else if (title.equals(strip(WeeklyMissionsGUI.TITLE))) {
            event.setCancelled(true); handleWeeklyGUI(player, event.getSlot(), event.getCurrentItem());
        } else if (title.equals(strip(PlayerStatsGUI.TITLE))) {
            event.setCancelled(true); if (event.getSlot() == 36) menuGUI.open(player);
        }
        // Vault is handled by InventoryCloseEvent
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        String title = strip(event.getView().getTitle());
        if (title.contains("Coffre d'Île")) {
            OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
            if (island != null) {
                plugin.getIslandVaultManager().saveVault(island.getOwner());
            }
        }
    }

    private void handleMenuGUI(Player player, int slot) {
        switch (slot) {
            case 10 -> membersGUI.open(player);
            case 12 -> upgradesGUI.open(player);
            case 14 -> challengesGUI.open(player);
            case 16 -> warpGUI.open(player, 0);
            case 19 -> skillsGUI.open(player);
            case 21 -> missionsGUI.open(player);
            case 23 -> weeklyGUI.open(player);
            case 25 -> collectionsGUI.open(player);
            case 28 -> bankGUI.open(player);
            case 30 -> prestigeGUI.open(player);
            case 32 -> boostGUI.open(player);
            case 34 -> achievementsGUI.open(player);
            case 37 -> vaultGUI.open(player);
            case 39 -> statsGUI.open(player);
            case 41 -> playerStatsGUI.open(player);
            case 43 -> {
                OneBlockIsland isl = plugin.getOneBlockManager().getIsland(player.getUniqueId());
                if (isl != null && !isl.isOwner(player.getUniqueId())) {
                    player.sendMessage(c("&cSeul le propriétaire peut modifier les paramètres.")); return;
                }
                settingsGUI.open(player);
            }
            case 49 -> {
                player.closeInventory();
                OneBlockIsland isl = plugin.getOneBlockManager().getIsland(player.getUniqueId());
                if (isl != null) { player.teleport(isl.getHome()); player.sendMessage(c("&aTéléporté !")); }
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
            player.sendMessage(left ? c("&cTu as quitté l'île.") : c("&cErreur lors du départ.")); return;
        }
        if (slot < 45 && item != null && item.getType().name().contains("PLAYER_HEAD")
            && island.isOwner(player.getUniqueId()) && item.getItemMeta() != null) {
            String memberName = strip(item.getItemMeta().getDisplayName());
            for (UUID mu : island.getMembers()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(mu);
                if (memberName.equals(op.getName())) {
                    plugin.getOneBlockManager().kickMember(island.getOwner(), mu);
                    player.sendMessage(c("&e" + memberName + " &7expulsé."));
                    Player online = Bukkit.getPlayer(mu);
                    if (online != null) online.sendMessage(c("&cTu as été expulsé de l'île de &e" + player.getName()));
                    membersGUI.open(player); return;
                }
            }
        }
    }

    private void handleSettingsGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null || !island.isOwner(player.getUniqueId())) { player.sendMessage(c("&cSeul le propriétaire peut modifier.")); return; }
        switch (slot) {
            case 10 -> { island.setPvpEnabled(!island.isPvpEnabled()); plugin.getOneBlockManager().saveAll(); settingsGUI.open(player); }
            case 12 -> { island.setVisitorsAllowed(!island.isVisitorsAllowed()); plugin.getOneBlockManager().saveAll(); settingsGUI.open(player); }
            case 14 -> { island.setWarpEnabled(!island.isWarpEnabled()); plugin.getOneBlockManager().saveAll(); settingsGUI.open(player); }
            case 22 -> menuGUI.open(player);
        }
    }

    private void handleUpgradesGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (!island.isOwner(player.getUniqueId()) && !island.isCoOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire/co-proprio peut acheter.")); return;
        }
        int[] slots = {10, 12, 14, 16};
        UpgradeType[] types = UpgradeType.values();
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i] && i < types.length) {
                UpgradeType type = types[i];
                int level = island.getUpgradeLevel(type);
                if (level >= type.getMaxLevel()) { player.sendMessage(c("&cDéjà au maximum !")); return; }
                int cost = type.getCost(level);
                if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost)) {
                    player.sendMessage(c("&cPas assez de pièces ! (" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + "/" + cost + ")")); return;
                }
                island.setUpgradeLevel(type, level + 1);
                island.addChallengeProgress(IslandChallenge.ChallengeType.UPGRADES_BOUGHT, 1);
                plugin.getDailyMissionManager().addProgress(player.getUniqueId(), DailyMission.MissionType.BUY_UPGRADES, 1);
                plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.UPGRADES_BOUGHT);
                plugin.getOneBlockManager().saveAll();
                player.sendMessage(c("&a✔ " + type.getDisplayName() + " → Niv." + (level + 1)));
                upgradesGUI.open(player); return;
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
                    ? "Île de " + plugin.getOneBlockManager().getOwnerName(isl) : isl.getWarpName();
                if (warpName.equals(name)) {
                    player.closeInventory(); player.teleport(isl.getHome());
                    player.sendMessage(c("&aTéléporté vers &e" + name + "&a !"));
                    if (!isl.getMotd().isEmpty()) player.sendMessage(c("&8[MOTD] &7" + isl.getMotd())); return;
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
                player.sendMessage(c("&cPas assez de pièces.")); return;
            }
            island.depositToBank(amount);
            plugin.getOneBlockManager().saveAll();
            player.sendMessage(c("&a+&e" + amount + " &adéposées dans la banque."));
            bankGUI.open(player);
        } else if (slot == 15) {
            if (!island.isOwner(player.getUniqueId()) && !island.isCoOwner(player.getUniqueId())) {
                player.sendMessage(c("&cSeuls le proprio/co-proprio peuvent retirer.")); return;
            }
            int amount = click.isShiftClick() ? (int) Math.min(island.getBankBalance(), Integer.MAX_VALUE)
                : click.isRightClick() ? 1000 : 100;
            if (!island.withdrawFromBank(amount)) { player.sendMessage(c("&cPas assez dans la banque.")); return; }
            plugin.getEconomyManager().addBalance(player.getUniqueId(), amount);
            plugin.getOneBlockManager().saveAll();
            player.sendMessage(c("&e" + amount + " &7retirées de la banque."));
            bankGUI.open(player);
        }
    }

    private void handlePrestigeGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (slot == 22) { menuGUI.open(player); return; }
        if (slot == 13) {
            if (!island.isOwner(player.getUniqueId())) { player.sendMessage(c("&cSeul le proprio peut prestigier.")); return; }
            boolean done = plugin.getOneBlockManager().prestigeIsland(island.getOwner());
            if (done) {
                player.closeInventory();
                plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.PRESTIGE_DONE);
                player.sendTitle(c("&d&l✦ PRESTIGE " + island.getPrestige() + " ✦"),
                    c("&7Multiplicateur : &e×" + String.format("%.1f", island.getPrestigeMultiplier())), 10, 80, 20);
                Bukkit.broadcastMessage(c("&d[PRESTIGE] &e" + player.getName() + " &7→ &dPrestige " + island.getPrestige() + " !"));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.8f);
            } else player.sendMessage(c("&cConditions non remplies."));
        }
    }

    private void handleMissionsGUI(Player player, int slot) {
        if (slot == 27) { menuGUI.open(player); return; }
        List<DailyMission> missions = plugin.getDailyMissionManager().getDailyMissions(player.getUniqueId());
        int[] slots = {10, 12, 14, 16, 13};
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i] && i < missions.size()) {
                DailyMission m = missions.get(i);
                if (m.isClaimable()) {
                    plugin.getDailyMissionManager().claimMission(player.getUniqueId(), m.getId());
                    plugin.getEconomyManager().addBalance(player.getUniqueId(), m.getReward());
                    plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.DAILY_COMPLETED);
                    player.sendMessage(c("&a✔ Mission : &e" + m.getDisplayName() + " &a+&e" + m.getReward() + " &apièces !"));
                    missionsGUI.open(player);
                }
                return;
            }
        }
    }

    private void handleCollectionsGUI(Player player, int slot, ItemStack item) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        OBCollection[] cols = OBCollection.values();
        int rows = Math.min(6, Math.max(4, (int) Math.ceil((cols.length + 9) / 9.0) + 1));
        if (slot == (rows - 1) * 9) { menuGUI.open(player); return; }
        if (slot < cols.length) {
            OBCollection col = cols[slot];
            long collected = island.getCollection(col.getMaterial().name());
            int lastClaimed = island.getClaimedMilestone(col.name());
            int next = lastClaimed + 1;
            if (next < col.getMilestoneCount() && collected >= col.getMilestoneAmount(next)) {
                island.setClaimedMilestone(col.name(), next);
                int reward = col.getMilestoneReward(next);
                plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);
                plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.COLLECTION_MILESTONES_REACHED);
                plugin.getOneBlockManager().saveAll();
                player.sendMessage(c("&b✦ Collection &e" + col.getDisplayName()
                    + " &b- Palier " + (next + 1) + " ! &e+" + reward + " &bpièces !"));
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
                collectionsGUI.open(player);
            } else if (next < col.getMilestoneCount()) {
                player.sendMessage(c("&7" + col.getDisplayName() + " : &e" + collected
                    + "&7/&e" + col.getMilestoneAmount(next)));
            }
        }
    }

    private void handleBoostGUI(Player player, int slot) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (slot == 22) { menuGUI.open(player); return; }
        int[] slots = {10, 12, 14, 16};
        Boost.BoostType[] types = Boost.BoostType.values();
        for (int i = 0; i < slots.length && i < types.length; i++) {
            if (slot == slots[i]) {
                Boost.BoostType type = types[i];
                if (!island.isOwner(player.getUniqueId())) { player.sendMessage(c("&cSeul le proprio peut activer un boost.")); return; }
                if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), type.getCost())) {
                    player.sendMessage(c("&cPas assez de pièces (" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + "/" + type.getCost() + ")")); return;
                }
                plugin.getBoostManager().activateBoost(island.getOwner(), type);
                player.closeInventory();
                player.sendTitle(c("&c&l⚡ BOOST ACTIVÉ !"), c("&e" + type.getDisplayName() + " &7pendant &e" + type.formatDuration()), 10, 70, 20);
                player.sendMessage(c("&c⚡ &e" + type.getDisplayName() + " &7activé ! &e" + type.formatDuration()));
                for (UUID uid : island.getAllMemberUUIDs()) {
                    Player m = Bukkit.getPlayer(uid);
                    if (m != null && m != player) m.sendMessage(c("&c⚡ &e" + player.getName() + " &7a activé un boost : &e" + type.getDisplayName() + " &7pour l'île !"));
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2f, 1f);
                return;
            }
        }
    }

    private void handleWeeklyGUI(Player player, int slot, ItemStack item) {
        if (slot == 22) { menuGUI.open(player); return; }
        List<WeeklyMission> missions = plugin.getWeeklyMissionManager().getWeeklyMissions(player.getUniqueId());
        int[] slots = {10, 13, 16};
        for (int i = 0; i < slots.length && i < missions.size(); i++) {
            if (slot == slots[i]) {
                WeeklyMission m = missions.get(i);
                if (m.isClaimable()) {
                    plugin.getWeeklyMissionManager().claimMission(player.getUniqueId(), m.getId());
                    plugin.getEconomyManager().addBalance(player.getUniqueId(), m.getReward());
                    plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.WEEKLY_COMPLETED);
                    player.sendMessage(c("&d✔ Mission hebdo : &e" + m.getDisplayName() + " &d+&e" + m.getReward() + " &dpièces !"));
                    weeklyGUI.open(player);
                }
                return;
            }
        }
    }
}
