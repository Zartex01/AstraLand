package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

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

        int reward = plugin.getConfig().getInt("economy.block-reward", 2);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);

        Phase phaseBefore = island.getCurrentPhase();
        om.regenerateBlock(island);
        Phase phaseAfter = island.getCurrentPhase();

        if (!phaseBefore.equals(phaseAfter)) {
            handlePhaseUnlock(island, phaseAfter, player);
        }

        trySpawnMob(island, phaseAfter);
        tryDropBonusLoot(island, phaseAfter, player);

        event.setDropItems(true);
        om.saveAll();
    }

    private void handlePhaseUnlock(OneBlockIsland island, Phase phase, Player trigger) {
        for (UUID uid : island.getMembers()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendTitle(
                    c(phase.getColor() + "&lNouvelle Phase !"),
                    c("&7Tu entres dans la phase " + phase.getColor() + "&l" + phase.getDisplayName()),
                    10, 60, 20
                );
                p.sendMessage(c(phase.getColor() + "&l✦ Nouvelle phase débloquée : " + phase.getDisplayName() + " !"));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }
        Player owner = Bukkit.getPlayer(island.getOwner());
        if (owner != null && !island.isMember(island.getOwner())) {
            owner.sendTitle(
                c(phase.getColor() + "&lNouvelle Phase !"),
                c("&7Tu entres dans la phase " + phase.getColor() + "&l" + phase.getDisplayName()),
                10, 60, 20
            );
        }
        island.addChallengeProgress(IslandChallenge.ChallengeType.PHASE_REACHED, 0);
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
                owner.sendMessage(c("&6+&e" + bonus + " &6pièces &7offertes pour avoir atteint la phase &e" + phase.getDisplayName() + "&7 !"));
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
            Location spawnLoc = island.getBlockLocation().clone().add(0, 1, 0);
            island.getBlockLocation().getWorld().spawnEntity(spawnLoc, type);
        } catch (Exception ignored) {}
    }

    private void tryDropBonusLoot(OneBlockIsland island, Phase phase, Player player) {
        int baseChance = plugin.getConfig().getInt("oneblock.chest-drop-chance", 5);
        int luckLevel = island.getUpgradeLevel(UpgradeType.CHEST_LUCK);
        int chance = baseChance + (luckLevel * 4);

        if (random.nextInt(100) >= chance) return;

        Phase.LootEntry loot = phase.getRandomLoot();
        int amount = loot.minAmount + random.nextInt(Math.max(1, loot.maxAmount - loot.minAmount + 1));
        ItemStack item = new ItemStack(loot.material, amount);

        Location dropLoc = island.getBlockLocation().clone().add(0.5, 1.5, 0.5);
        if (dropLoc.getWorld() != null) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            player.sendMessage(c("&6✦ &eItem bonus : &f" + amount + "x " + formatName(loot.material.name()) + " &6✦"));
        }
    }

    private String formatName(String name) {
        return name.replace("_", " ").toLowerCase();
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
        int moneyDrop = plugin.getConfig().getInt("economy.mob-reward", 5);
        plugin.getEconomyManager().addBalance(killer.getUniqueId(), moneyDrop);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        UUID pending = plugin.getOneBlockManager().getPendingInviteFrom(player.getUniqueId());
        if (pending != null) {
            Player inviter = Bukkit.getPlayer(pending);
            String name = inviter != null ? inviter.getName() : "quelqu'un";
            player.sendMessage(c("&6✉ &eTu as une invitation de &f" + name + "&e sur son île OneBlock !"));
            player.sendMessage(c("&7Tape &a/ob accept &7pour accepter ou &c/ob decline &7pour refuser."));
        }
    }
}
