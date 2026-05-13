package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.PlayerStatsManager;
import com.astraland.oneblock.managers.ShopPriceManager;
import com.astraland.oneblock.models.*;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class OneBlockListener implements Listener {

    private final OneBlock plugin;
    private final Random random = new Random();

    public static final String SELL_WAND_NAME = ChatColor.translateAlternateColorCodes('&', "&6✦ Baguette de Vente");

    public OneBlockListener(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlock().getWorld().getName().equals(plugin.getPluginWorld())) return;

        var om = plugin.getOneBlockManager();
        OneBlockIsland island = om.getIsland(player.getUniqueId());

        if (island == null) {
            event.setCancelled(true);
            player.sendMessage(c("&cTu n'as pas d'île OneBlock."));
            return;
        }

        if (!event.getBlock().getLocation().equals(island.getBlockLocation())) {
            if (!island.isMember(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(c("&cCette zone ne t'appartient pas."));
            } else {
                // Non-oneblock block: update island worth
                int val = getBlockValue(event.getBlock().getType());
                if (val > 0) island.removeWorth(val);
            }
            return;
        }

        // --- Multiplier = prestige × skills × boost ---
        UUID ownerUid = island.getOwner();
        double boostMult = plugin.getBoostManager().getMultiplier(ownerUid);
        double totalMult = island.getPrestigeMultiplier()
            * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId())
            * boostMult;

        int baseReward = plugin.getConfig().getInt("economy.block-reward", 2);
        int reward = (int) Math.max(1, baseReward * totalMult);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);

        // Stats & missions
        plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.BLOCKS_BROKEN);
        plugin.getPlayerStatsManager().add(player.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED, reward);
        plugin.getDailyMissionManager().addProgress(player.getUniqueId(), DailyMission.MissionType.BREAK_BLOCKS, 1);
        plugin.getDailyMissionManager().addProgress(player.getUniqueId(), DailyMission.MissionType.EARN_COINS, reward);
        plugin.getWeeklyMissionManager().addProgress(player.getUniqueId(), WeeklyMission.MissionType.BREAK_BLOCKS, 1);
        plugin.getWeeklyMissionManager().addProgress(player.getUniqueId(), WeeklyMission.MissionType.EARN_COINS, reward);
        plugin.getSkillManager().addXP(player.getUniqueId(), Skill.MINING, 5);

        Phase phaseBefore = island.getCurrentPhase();
        om.regenerateBlock(island);
        Phase phaseAfter = island.getCurrentPhase();

        if (!phaseBefore.equals(phaseAfter)) handlePhaseUnlock(island, phaseAfter, player);

        trySpawnMob(island, phaseAfter);
        tryDropBonusLoot(island, phaseAfter, player, totalMult);
        tryLuckyEvent(island, phaseAfter, player, totalMult);
        trySpawnerDrop(island, phaseAfter, player);

        checkAchievements(player, island);
        event.setDropItems(true);
        om.saveAll();
    }

    private void handlePhaseUnlock(OneBlockIsland island, Phase phase, Player trigger) {
        String title = c(phase.getColor() + "&lNouvelle Phase !");
        String sub   = c("&7Tu entres dans la phase " + phase.getColor() + "&l" + phase.getDisplayName());
        for (UUID uid : island.getAllMemberUUIDs()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendTitle(title, sub, 10, 60, 20);
                p.sendMessage(c(phase.getColor() + "&l✦ Nouvelle phase : " + phase.getDisplayName() + " !"));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }
        Bukkit.broadcastMessage(c("&6[AstraLand] &e" + trigger.getName()
            + " &7a atteint la phase " + phase.getColor() + "&l" + phase.getDisplayName()
            + " &7sur son île OneBlock !"));
        givePhaseReward(island, phase, trigger);

        // Achievement: phase unlocked
        plugin.getAchievementManager().checkAndUnlock(trigger, island,
            OBAchievement.AchType.PHASE_UNLOCKED, 1,
            plugin, false);
    }

    private void givePhaseReward(OneBlockIsland island, Phase phase, Player trigger) {
        int bonus = switch (phase) {
            case FORET -> 300; case DESERT -> 600; case NEIGE -> 1000;
            case ENFER -> 2000; case END -> 5000; default -> 0;
        };
        if (bonus > 0) {
            plugin.getEconomyManager().addBalance(island.getOwner(), bonus);
            Player owner = Bukkit.getPlayer(island.getOwner());
            if (owner != null) owner.sendMessage(c("&6+&e" + bonus + " &6pièces &7phase " + phase.getDisplayName()));
        }
    }

    private void trySpawnMob(OneBlockIsland island, Phase phase) {
        int baseChance = plugin.getConfig().getInt("oneblock.mob-spawn-chance", 12);
        int mobLevel = island.getUpgradeLevel(UpgradeType.MOB_DROPS);
        if (random.nextInt(100) >= baseChance + mobLevel * 5) return;
        if (island.getBlockLocation() == null || island.getBlockLocation().getWorld() == null) return;
        try { island.getBlockLocation().getWorld().spawnEntity(
            island.getBlockLocation().clone().add(0, 1, 0), phase.getRandomMob()); }
        catch (Exception ignored) {}
    }

    private void tryDropBonusLoot(OneBlockIsland island, Phase phase, Player player, double totalMult) {
        int baseChance = plugin.getConfig().getInt("oneblock.chest-drop-chance", 5);
        int luckLevel  = island.getUpgradeLevel(UpgradeType.CHEST_LUCK);
        double lootBonus = plugin.getSkillManager().getTotalLootChanceBonus(player.getUniqueId());
        int chance = (int)(baseChance + luckLevel * 4 + lootBonus * 100);
        if (random.nextInt(100) >= chance) return;

        Phase.LootEntry loot = phase.getRandomLoot();
        int amount = loot.minAmount + random.nextInt(Math.max(1, loot.maxAmount - loot.minAmount + 1));
        ItemStack item = new ItemStack(loot.material, amount);

        island.addToCollection(loot.material.name(), amount);
        plugin.getSkillManager().addXP(player.getUniqueId(), Skill.FARMING, 20);

        checkCollectionMilestones(island, player, loot.material);

        Location dropLoc = island.getBlockLocation().clone().add(0.5, 1.5, 0.5);
        if (dropLoc.getWorld() != null) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            player.sendMessage(c("&6✦ &eItem bonus : &f" + amount + "x " + formatName(loot.material.name()) + " &6✦"));
        }
    }

    private void checkCollectionMilestones(OneBlockIsland island, Player player, org.bukkit.Material mat) {
        OBCollection col = OBCollection.fromMaterial(mat);
        if (col == null) return;
        long collected = island.getCollection(mat.name());
        int lastClaimed = island.getClaimedMilestone(col.name());
        int nextMilestone = lastClaimed + 1;
        if (nextMilestone >= col.getMilestoneCount()) return;
        if (collected >= col.getMilestoneAmount(nextMilestone)) {
            island.setClaimedMilestone(col.name(), nextMilestone);
            int reward = col.getMilestoneReward(nextMilestone);
            plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);
            plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.COLLECTION_MILESTONES_REACHED);
            player.sendMessage(c("&b&l✦ Collection &e" + col.getDisplayName()
                + " &b&l- Palier " + (nextMilestone + 1) + " ! &e+" + reward + " &bpièces !"));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
        }
    }

    private void tryLuckyEvent(OneBlockIsland island, Phase phase, Player player, double totalMult) {
        if (random.nextInt(1000) >= 2) return;

        plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.LUCKY_EVENTS);
        player.sendTitle(c("&6&l✦ LUCKY EVENT ✦"), c("&eTu as eu une chance incroyable !"), 10, 80, 20);
        Bukkit.broadcastMessage(c("&6[Lucky] &e" + player.getName()
            + " &7a déclenché un &6&lLucky Event &7! &e✦"));

        int luckyMoney = (int)((3000 + random.nextInt(7001)) * totalMult);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), luckyMoney);
        plugin.getPlayerStatsManager().add(player.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED, luckyMoney);
        player.sendMessage(c("&6&l★ LUCKY EVENT ! &e+" + luckyMoney + " &6pièces !"));

        ItemStack luckyItem = getLuckyItem();
        if (player.getInventory().firstEmpty() != -1) player.getInventory().addItem(luckyItem);
        else player.getLocation().getWorld().dropItemNaturally(player.getLocation(), luckyItem);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.5f);
        spawnFirework(player.getLocation());
        checkAchievements(player, island);
    }

    private void trySpawnerDrop(OneBlockIsland island, Phase phase, Player player) {
        if (random.nextInt(2000) >= 1) return;
        plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.SPAWNERS_FOUND);
        EntityType mobType = phase.getRandomMob();
        ItemStack spawner = new ItemStack(org.bukkit.Material.SPAWNER);
        ItemMeta meta = spawner.getItemMeta();
        meta.setDisplayName(c("&5&lSpawner de &d" + formatName(mobType.name())));
        meta.setLore(Arrays.asList(c("&7Obtenu depuis le OneBlock !"),
            c("&8Phase : " + phase.getColor() + phase.getDisplayName())));
        spawner.setItemMeta(meta);
        if (player.getInventory().firstEmpty() != -1) player.getInventory().addItem(spawner);
        else player.getLocation().getWorld().dropItemNaturally(player.getLocation(), spawner);
        player.sendMessage(c("&5&l★ RARE ! &dSpawner de " + formatName(mobType.name()) + " obtenu !"));
        Bukkit.broadcastMessage(c("&5[RARE] &e" + player.getName() + " &7a obtenu un &5Spawner de "
            + formatName(mobType.name()) + " !"));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2f, 1f);
        checkAchievements(player, island);
    }

    private void checkAchievements(Player player, OneBlockIsland island) {
        var am = plugin.getAchievementManager();
        var ps = plugin.getPlayerStatsManager();
        long blocksBroken = ps.get(player.getUniqueId(), PlayerStatsManager.Stat.BLOCKS_BROKEN);
        long mobsKilled   = ps.get(player.getUniqueId(), PlayerStatsManager.Stat.MOBS_KILLED);
        long coinsEarned  = ps.get(player.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED);
        long luckyEvents  = ps.get(player.getUniqueId(), PlayerStatsManager.Stat.LUCKY_EVENTS);
        long spawners     = ps.get(player.getUniqueId(), PlayerStatsManager.Stat.SPAWNERS_FOUND);

        for (OBAchievement ach : OBAchievement.values()) {
            if (am.hasAchievement(player.getUniqueId(), ach)) continue;
            boolean reached = switch (ach.getType()) {
                case BLOCKS_BROKEN         -> blocksBroken >= ach.getThreshold();
                case MOBS_KILLED           -> mobsKilled >= ach.getThreshold();
                case COINS_EARNED          -> coinsEarned >= ach.getThreshold();
                case LUCKY_EVENTS          -> luckyEvents >= ach.getThreshold();
                case SPAWNERS_FOUND        -> spawners >= ach.getThreshold();
                case PRESTIGE              -> island != null && island.getPrestige() >= ach.getThreshold();
                case PHASE_UNLOCKED        -> false; // handled in handlePhaseUnlock
                case MAX_SKILL             -> false; // handled in skill check
                case UPGRADES_BOUGHT       -> ps.get(player.getUniqueId(), PlayerStatsManager.Stat.UPGRADES_BOUGHT) >= ach.getThreshold();
                case COLLECTION_MILESTONES -> ps.get(player.getUniqueId(), PlayerStatsManager.Stat.COLLECTION_MILESTONES_REACHED) >= ach.getThreshold();
            };
            if (reached) awardAchievement(player, ach);
        }
    }

    public void awardAchievement(Player player, OBAchievement ach) {
        if (!plugin.getAchievementManager().unlock(player.getUniqueId(), ach)) return;
        plugin.getPlayerStatsManager().increment(player.getUniqueId(), PlayerStatsManager.Stat.ACHIEVEMENTS);
        if (ach.getReward() > 0) {
            plugin.getEconomyManager().addBalance(player.getUniqueId(), ach.getReward());
            plugin.getPlayerStatsManager().add(player.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED, ach.getReward());
        }
        player.sendTitle(c("&6&l✦ Succès Débloqué ✦"), c("&e" + ach.getDisplayName()), 10, 70, 20);
        player.sendMessage(c("&6&l★ SUCCÈS : &e" + ach.getDisplayName()
            + (ach.getReward() > 0 ? " &6+&e" + ach.getReward() + " &6pièces !" : " !")));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
        Bukkit.broadcastMessage(c("&6[Succès] &e" + player.getName() + " &7a débloqué : &6" + ach.getDisplayName() + " !"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != org.bukkit.Material.STICK) return;
        if (item.getItemMeta() == null) return;
        String name = item.getItemMeta().getDisplayName();
        if (!SELL_WAND_NAME.equals(name)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
            && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR) return;
        event.setCancelled(true);

        // Sell all sellable items from hotbar
        int totalEarned = 0;
        int itemsSold = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack it = player.getInventory().getItem(i);
            if (it == null || it.getType().isAir()) continue;
            if (it == item) continue; // don't sell the wand
            int price = ShopPriceManager.getSellPrice(it.getType());
            if (price <= 0) continue;
            totalEarned += price * it.getAmount();
            itemsSold += it.getAmount();
            player.getInventory().setItem(i, null);
        }
        if (itemsSold > 0) {
            plugin.getEconomyManager().addBalance(player.getUniqueId(), totalEarned);
            plugin.getPlayerStatsManager().add(player.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED, totalEarned);
            player.sendMessage(c("&6✦ Baguette de vente : &e+" + totalEarned + " &6pièces (&e" + itemsSold + " &6items)"));
        } else {
            player.sendMessage(c("&cAucun item vendable dans la barre rapide."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlock().getWorld().getName().equals(plugin.getPluginWorld())) return;
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null || !island.isMember(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(c("&cTu ne peux pas placer de blocs ici."));
            return;
        }
        int val = getBlockValue(event.getBlock().getType());
        if (val > 0) island.addWorth(val);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.getWorld().getName().equals(plugin.getPluginWorld())) return;
        if (entity.getKiller() == null) return;
        Player killer = entity.getKiller();
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(killer.getUniqueId());
        if (island == null) return;

        island.addChallengeProgress(IslandChallenge.ChallengeType.MOBS_KILLED, 1);
        plugin.getDailyMissionManager().addProgress(killer.getUniqueId(), DailyMission.MissionType.KILL_MOBS, 1);
        plugin.getWeeklyMissionManager().addProgress(killer.getUniqueId(), WeeklyMission.MissionType.KILL_MOBS, 1);
        plugin.getPlayerStatsManager().increment(killer.getUniqueId(), PlayerStatsManager.Stat.MOBS_KILLED);
        plugin.getSkillManager().addXP(killer.getUniqueId(), Skill.COMBAT, 10);

        UUID ownerUid = island.getOwner();
        double boostMult = plugin.getBoostManager().getMultiplier(ownerUid);
        double totalMult = island.getPrestigeMultiplier()
            * plugin.getSkillManager().getTotalMoneyMultiplier(killer.getUniqueId())
            * boostMult;
        int mobReward = (int) Math.max(1, plugin.getConfig().getInt("economy.mob-reward", 5) * totalMult);
        plugin.getEconomyManager().addBalance(killer.getUniqueId(), mobReward);
        plugin.getPlayerStatsManager().add(killer.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED, mobReward);
        plugin.getDailyMissionManager().addProgress(killer.getUniqueId(), DailyMission.MissionType.EARN_COINS, mobReward);
        plugin.getWeeklyMissionManager().addProgress(killer.getUniqueId(), WeeklyMission.MissionType.EARN_COINS, mobReward);
        checkAchievements(killer, island);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        UUID pending = plugin.getOneBlockManager().getPendingInviteFrom(player.getUniqueId());
        if (pending != null) {
            Player inviter = Bukkit.getPlayer(pending);
            String name = inviter != null ? inviter.getName() : "quelqu'un";
            player.sendMessage(c("&6✉ Invitation de &f" + name + "&e ! (/ob accept ou /ob decline)"));
        }
    }

    private ItemStack getLuckyItem() {
        int roll = random.nextInt(5);
        return switch (roll) {
            case 0 -> {
                ItemStack it = new ItemStack(org.bukkit.Material.DIAMOND_SWORD);
                ItemMeta m = it.getItemMeta();
                m.setDisplayName(c("&b&l⚡ Épée Lucky"));
                m.addEnchant(Enchantment.SHARPNESS, 5, true);
                m.addEnchant(Enchantment.UNBREAKING, 3, true);
                m.addEnchant(Enchantment.LOOTING, 3, true);
                it.setItemMeta(m); yield it;
            }
            case 1 -> {
                ItemStack it = new ItemStack(org.bukkit.Material.DIAMOND_PICKAXE);
                ItemMeta m = it.getItemMeta();
                m.setDisplayName(c("&b&l⛏ Pioche Lucky"));
                m.addEnchant(Enchantment.EFFICIENCY, 5, true);
                m.addEnchant(Enchantment.FORTUNE, 3, true);
                m.addEnchant(Enchantment.UNBREAKING, 3, true);
                it.setItemMeta(m); yield it;
            }
            case 2 -> {
                ItemStack it = new ItemStack(org.bukkit.Material.ELYTRA);
                ItemMeta m = it.getItemMeta();
                m.setDisplayName(c("&b&l✦ Élytre Lucky"));
                m.addEnchant(Enchantment.UNBREAKING, 3, true);
                it.setItemMeta(m); yield it;
            }
            case 3 -> {
                ItemStack it = new ItemStack(org.bukkit.Material.DIAMOND, 64);
                ItemMeta m = it.getItemMeta();
                m.setDisplayName(c("&b&l◈ Diamants Lucky ×64"));
                it.setItemMeta(m); yield it;
            }
            default -> {
                ItemStack it = new ItemStack(org.bukkit.Material.NETHERITE_INGOT, 4);
                ItemMeta m = it.getItemMeta();
                m.setDisplayName(c("&5&l✦ Netherite Lucky ×4"));
                it.setItemMeta(m); yield it;
            }
        };
    }

    private void spawnFirework(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(Color.fromRGB(255, 165, 0), Color.YELLOW, Color.ORANGE)
            .withFade(Color.WHITE).withFlicker().withTrail().build());
        fwm.setPower(1);
        fw.setFireworkMeta(fwm);
    }

    private int getBlockValue(org.bukkit.Material mat) {
        return switch (mat) {
            case DIAMOND_BLOCK        -> 500;
            case EMERALD_BLOCK        -> 400;
            case GOLD_BLOCK           -> 150;
            case IRON_BLOCK           -> 80;
            case NETHERITE_BLOCK      -> 15000;
            case ANCIENT_DEBRIS       -> 2000;
            case DIAMOND              -> 50;
            case EMERALD              -> 40;
            case GOLD_INGOT           -> 15;
            case IRON_INGOT           -> 8;
            case OBSIDIAN             -> 15;
            case END_STONE_BRICKS, END_STONE -> 3;
            case PURPUR_BLOCK         -> 6;
            case NETHERRACK, CRIMSON_NYLIUM, WARPED_NYLIUM -> 1;
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG -> 3;
            case STONE, COBBLESTONE, DIRT, GRASS_BLOCK -> 1;
            case SAND, GRAVEL         -> 1;
            default -> {
                int price = ShopPriceManager.getSellPrice(mat);
                yield price > 0 ? price / 2 : 0;
            }
        };
    }

    private String formatName(String name) {
        return Arrays.stream(name.split("_"))
            .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
            .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
    }
}
