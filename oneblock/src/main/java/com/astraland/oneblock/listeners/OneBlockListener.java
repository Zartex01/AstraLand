package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.DailyMission;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import com.astraland.oneblock.models.Skill;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class OneBlockListener implements Listener {

    private final OneBlock plugin;
    private final Random random = new Random();

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
            }
            return;
        }

        double totalMult = island.getPrestigeMultiplier()
            * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId());
        int baseReward = plugin.getConfig().getInt("economy.block-reward", 2);
        int reward = (int) Math.max(1, baseReward * totalMult);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);

        plugin.getDailyMissionManager().addProgress(player.getUniqueId(), DailyMission.MissionType.BREAK_BLOCKS, 1);
        plugin.getDailyMissionManager().addProgress(player.getUniqueId(), DailyMission.MissionType.EARN_COINS, reward);
        plugin.getSkillManager().addXP(player.getUniqueId(), Skill.MINING, 5);

        Phase phaseBefore = island.getCurrentPhase();
        om.regenerateBlock(island);
        Phase phaseAfter = island.getCurrentPhase();

        if (!phaseBefore.equals(phaseAfter)) handlePhaseUnlock(island, phaseAfter, player);

        trySpawnMob(island, phaseAfter);
        tryDropBonusLoot(island, phaseAfter, player);
        tryLuckyEvent(island, phaseAfter, player);
        trySpawnerDrop(island, phaseAfter, player);

        event.setDropItems(true);
        om.saveAll();
    }

    private void handlePhaseUnlock(OneBlockIsland island, Phase phase, Player trigger) {
        String title = c(phase.getColor() + "&lNouvelle Phase !");
        String sub = c("&7Tu entres dans la phase " + phase.getColor() + "&l" + phase.getDisplayName());
        for (UUID uid : island.getAllMemberUUIDs()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendTitle(title, sub, 10, 60, 20);
                p.sendMessage(c(phase.getColor() + "&l✦ Nouvelle phase débloquée : " + phase.getDisplayName() + " !"));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }
        Bukkit.broadcastMessage(c("&6[AstraLand] &e" + trigger.getName()
            + " &7a atteint la phase " + phase.getColor() + "&l" + phase.getDisplayName()
            + " &7sur son île OneBlock !"));
        givePhaseReward(island, phase, trigger);
    }

    private void givePhaseReward(OneBlockIsland island, Phase phase, Player trigger) {
        int bonus = switch (phase) {
            case FORET -> 300;
            case DESERT -> 600;
            case NEIGE -> 1000;
            case ENFER -> 2000;
            case END -> 5000;
            default -> 0;
        };
        if (bonus > 0) {
            plugin.getEconomyManager().addBalance(island.getOwner(), bonus);
            Player owner = Bukkit.getPlayer(island.getOwner());
            if (owner != null)
                owner.sendMessage(c("&6+&e" + bonus + " &6pièces &7offertes pour la phase &e" + phase.getDisplayName() + "&7 !"));
        }
    }

    private void trySpawnMob(OneBlockIsland island, Phase phase) {
        int baseChance = plugin.getConfig().getInt("oneblock.mob-spawn-chance", 12);
        int mobLevel = island.getUpgradeLevel(UpgradeType.MOB_DROPS);
        int chance = baseChance + (mobLevel * 5);
        if (random.nextInt(100) >= chance) return;
        if (island.getBlockLocation() == null || island.getBlockLocation().getWorld() == null) return;
        EntityType type = phase.getRandomMob();
        try {
            island.getBlockLocation().getWorld().spawnEntity(
                island.getBlockLocation().clone().add(0, 1, 0), type);
        } catch (Exception ignored) {}
    }

    private void tryDropBonusLoot(OneBlockIsland island, Phase phase, Player player) {
        int baseChance = plugin.getConfig().getInt("oneblock.chest-drop-chance", 5);
        int luckLevel = island.getUpgradeLevel(UpgradeType.CHEST_LUCK);
        double lootBonus = plugin.getSkillManager().getTotalLootChanceBonus(player.getUniqueId());
        int chance = (int) (baseChance + luckLevel * 4 + lootBonus * 100);
        if (random.nextInt(100) >= chance) return;

        Phase.LootEntry loot = phase.getRandomLoot();
        int amount = loot.minAmount + random.nextInt(Math.max(1, loot.maxAmount - loot.minAmount + 1));
        ItemStack item = new ItemStack(loot.material, amount);

        island.addToCollection(loot.material.name(), amount);
        plugin.getSkillManager().addXP(player.getUniqueId(), Skill.FARMING, 20);

        Location dropLoc = island.getBlockLocation().clone().add(0.5, 1.5, 0.5);
        if (dropLoc.getWorld() != null) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            player.sendMessage(c("&6✦ &eItem bonus : &f" + amount + "x "
                + formatName(loot.material.name()) + " &6✦"));
        }
    }

    private void tryLuckyEvent(OneBlockIsland island, Phase phase, Player player) {
        if (random.nextInt(1000) >= 2) return;

        player.sendTitle(c("&6&l✦ LUCKY EVENT ✦"), c("&eTu as eu une chance incroyable !"), 10, 80, 20);
        player.sendMessage(c("&6&l★ &e&lLUCKY EVENT ! &6&l★"));
        player.sendMessage(c("&6Tu as décroché un événement chanceux sur le OneBlock !"));

        Bukkit.broadcastMessage(c("&6[Lucky] &e" + player.getName()
            + " &7a déclenché un &6&lLucky Event &7sur son île OneBlock ! &e✦"));

        int luckyMoney = 3000 + random.nextInt(7001);
        double mult = island.getPrestigeMultiplier()
            * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId());
        int totalMoney = (int)(luckyMoney * mult);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), totalMoney);
        player.sendMessage(c("&6+&e" + totalMoney + " &6pièces Lucky !"));

        ItemStack luckyItem = getLuckyItem(phase);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(luckyItem);
            player.sendMessage(c("&6Tu as reçu : &e" + luckyItem.getItemMeta().getDisplayName()));
        } else {
            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), luckyItem);
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 0.5f);
        spawnFirework(player.getLocation());
    }

    private void trySpawnerDrop(OneBlockIsland island, Phase phase, Player player) {
        if (random.nextInt(2000) >= 1) return;
        EntityType mobType = phase.getRandomMob();
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        ItemMeta meta = spawner.getItemMeta();
        meta.setDisplayName(c("&5&lSpawner de &d" + formatName(mobType.name())));
        meta.setLore(Arrays.asList(
            c("&7Obtenu depuis le OneBlock !"),
            c("&8Phase : " + phase.getColor() + phase.getDisplayName())
        ));
        spawner.setItemMeta(meta);
        if (player.getInventory().firstEmpty() != -1) player.getInventory().addItem(spawner);
        else player.getLocation().getWorld().dropItemNaturally(player.getLocation(), spawner);
        player.sendMessage(c("&5&l★ RARE ! &dTu as obtenu un &5Spawner &d!"));
        Bukkit.broadcastMessage(c("&5[RARE] &e" + player.getName()
            + " &7a obtenu un &5Spawner de " + formatName(mobType.name()) + " &7depuis son OneBlock !"));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2f, 1f);
    }

    private ItemStack getLuckyItem(Phase phase) {
        int roll = new Random().nextInt(4);
        ItemStack item;
        switch (roll) {
            case 0 -> {
                item = new ItemStack(Material.DIAMOND_SWORD);
                ItemMeta m = item.getItemMeta();
                m.setDisplayName(c("&b&l⚡ Épée Lucky"));
                m.addEnchant(Enchantment.SHARPNESS, 5, true);
                m.addEnchant(Enchantment.UNBREAKING, 3, true);
                m.addEnchant(Enchantment.LOOTING, 3, true);
                item.setItemMeta(m);
            }
            case 1 -> {
                item = new ItemStack(Material.DIAMOND_PICKAXE);
                ItemMeta m = item.getItemMeta();
                m.setDisplayName(c("&b&l⛏ Pioche Lucky"));
                m.addEnchant(Enchantment.EFFICIENCY, 5, true);
                m.addEnchant(Enchantment.FORTUNE, 3, true);
                m.addEnchant(Enchantment.UNBREAKING, 3, true);
                item.setItemMeta(m);
            }
            case 2 -> {
                item = new ItemStack(Material.ELYTRA);
                ItemMeta m = item.getItemMeta();
                m.setDisplayName(c("&b&l✦ Élytre Lucky"));
                m.addEnchant(Enchantment.UNBREAKING, 3, true);
                item.setItemMeta(m);
            }
            default -> {
                item = new ItemStack(Material.DIAMOND, 32);
                ItemMeta m = item.getItemMeta();
                m.setDisplayName(c("&b&l◈ Diamants Lucky ×32"));
                item.setItemMeta(m);
            }
        }
        return item;
    }

    private void spawnFirework(Location loc) {
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(Color.GOLD, Color.YELLOW, Color.ORANGE)
            .withFade(Color.WHITE)
            .withFlicker().withTrail().build());
        fwm.setPower(1);
        fw.setFireworkMeta(fwm);
    }

    private String formatName(String name) {
        return Arrays.stream(name.split("_"))
            .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
            .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!event.getBlock().getWorld().getName().equals(plugin.getPluginWorld())) return;
        var om = plugin.getOneBlockManager();
        OneBlockIsland island = om.getIsland(player.getUniqueId());
        if (island == null || !island.isMember(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(c("&cTu ne peux pas placer de blocs ici."));
        }
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
        plugin.getSkillManager().addXP(killer.getUniqueId(), Skill.COMBAT, 10);

        double totalMult = island.getPrestigeMultiplier()
            * plugin.getSkillManager().getTotalMoneyMultiplier(killer.getUniqueId());
        int mobBase = plugin.getConfig().getInt("economy.mob-reward", 5);
        int mobReward = (int) Math.max(1, mobBase * totalMult);
        plugin.getEconomyManager().addBalance(killer.getUniqueId(), mobReward);
        plugin.getDailyMissionManager().addProgress(killer.getUniqueId(), DailyMission.MissionType.EARN_COINS, mobReward);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        UUID pending = plugin.getOneBlockManager().getPendingInviteFrom(player.getUniqueId());
        if (pending != null) {
            Player inviter = Bukkit.getPlayer(pending);
            String name = inviter != null ? inviter.getName() : "quelqu'un";
            player.sendMessage(c("&6✉ &eTu as une invitation de &f" + name + "&e ! (/ob accept ou /ob decline)"));
        }
    }
}
