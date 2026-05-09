package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.FactionManager;
import com.astraland.pvpfactions.managers.StatsManager;
import com.astraland.pvpfactions.models.Faction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PvpListener implements Listener {

    private final PvpFactions plugin;

    public PvpListener(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        StatsManager sm = plugin.getStatsManager();
        FactionManager fm = plugin.getFactionManager();

        sm.addDeath(victim.getUniqueId());

        if (victim.getKiller() != null) {
            Player killer = victim.getKiller();
            sm.addKill(killer.getUniqueId());

            Faction killerFaction = fm.getPlayerFaction(killer.getUniqueId());
            Faction victimFaction = fm.getPlayerFaction(victim.getUniqueId());

            if (victimFaction != null) {
                double powerLoss = plugin.getConfig().getDouble("faction.power-loss-on-death", 2.0);
                victimFaction.removePower(powerLoss);
                fm.saveAll();
            }

            int kills = sm.getKills(killer.getUniqueId());
            killer.sendMessage(c("&a+1 kill &7(total: &e" + kills + "&7)"));
        }

        Faction victimFaction = fm.getPlayerFaction(victim.getUniqueId());
        if (victimFaction != null) {
            victimFaction.getMembers().keySet().forEach(uuid -> {
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null && !p.equals(victim))
                    p.sendMessage(c("&c" + victim.getName() + " &aest mort ! Puissance faction : &e" + String.format("%.1f", victimFaction.getPower())));
            });
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Entity damager = event.getDamager();
        Player attacker = null;

        if (damager instanceof Player) attacker = (Player) damager;
        else if (damager instanceof org.bukkit.entity.Projectile) {
            org.bukkit.entity.Projectile proj = (org.bukkit.entity.Projectile) damager;
            if (proj.getShooter() instanceof Player) attacker = (Player) proj.getShooter();
        }

        if (attacker == null) return;
        if (attacker.equals(victim)) return;

        FactionManager fm = plugin.getFactionManager();
        Faction attackerFaction = fm.getPlayerFaction(attacker.getUniqueId());
        Faction victimFaction = fm.getPlayerFaction(victim.getUniqueId());

        if (attackerFaction != null && victimFaction != null
                && attackerFaction.getName().equalsIgnoreCase(victimFaction.getName())
                && plugin.getConfig().getBoolean("pvp.disable-friendly-fire", true)) {
            event.setCancelled(true);
            attacker.sendMessage(c("&cTir amical désactivé dans ta faction."));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.factions.admin")) return;

        FactionManager fm = plugin.getFactionManager();
        org.bukkit.Chunk chunk = event.getBlock().getChunk();
        Faction owner = fm.getFactionByClaim(chunk);
        if (owner == null) return;

        Faction playerFaction = fm.getPlayerFaction(player.getUniqueId());
        if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(owner.getName())) {
            event.setCancelled(true);
            player.sendMessage(c("&cCe territoire appartient à la faction &e" + owner.getName() + "&c."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.factions.admin")) return;

        FactionManager fm = plugin.getFactionManager();
        org.bukkit.Chunk chunk = event.getBlock().getChunk();
        Faction owner = fm.getFactionByClaim(chunk);
        if (owner == null) return;

        Faction playerFaction = fm.getPlayerFaction(player.getUniqueId());
        if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(owner.getName())) {
            event.setCancelled(true);
            player.sendMessage(c("&cCe territoire appartient à la faction &e" + owner.getName() + "&c."));
        }
    }
}
