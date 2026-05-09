package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.BountyManager;
import com.astraland.pvpfactions.managers.FactionManager;
import com.astraland.pvpfactions.managers.StatsManager;
import com.astraland.pvpfactions.models.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PvpListener implements Listener {

    private final PvpFactions plugin;

    public PvpListener(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&c&lFactions&8] &r")); }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        StatsManager sm = plugin.getStatsManager();
        FactionManager fm = plugin.getFactionManager();
        BountyManager bm = plugin.getBountyManager();

        int streak = sm.getCurrentStreak(victim.getUniqueId());
        sm.addDeath(victim.getUniqueId());

        Faction victimFaction = fm.getPlayerFaction(victim.getUniqueId());
        if (victimFaction != null) {
            double powerLoss = plugin.getConfig().getDouble("faction.power-loss-on-death", 2.0);
            victimFaction.removePower(powerLoss);
            fm.saveAll();
        }

        Player killer = victim.getKiller();
        if (killer != null) {
            sm.addKill(killer.getUniqueId());
            int killerStreak = sm.getCurrentStreak(killer.getUniqueId());
            int killerKills = sm.getKills(killer.getUniqueId());

            // Streak announcements
            String streakMsg = null;
            if (killerStreak == 5)  streakMsg = "&e" + killer.getName() + " &6est en feu ! &75 kills d'affilée !";
            else if (killerStreak == 10) streakMsg = "&c" + killer.getName() + " &4est DOMINANT ! &c10 kills d'affilée !";
            else if (killerStreak == 20) streakMsg = "&4&l" + killer.getName() + " &c&lest INARRÊTABLE ! &420 kills d'affilée !";
            else if (killerStreak == 30) streakMsg = "&5&l" + killer.getName() + " &d&lest un DIEU ! &530 kills d'affilée !";
            if (streakMsg != null) Bukkit.broadcastMessage(c("&8[&6Streak&8] " + streakMsg));

            // Bounty claim
            int bounty = bm.claimBounty(killer.getUniqueId(), victim.getUniqueId());
            if (bounty > 0) {
                killer.sendMessage(pre() + c("&6Tu as réclamé la prime de &c" + bounty + " &6pièces sur &e" + victim.getName() + "&6 !"));
                Bukkit.broadcastMessage(c("&6[Prime] &e" + killer.getName() + " &aa réclamé la prime de &c" + bounty + " &6pièces sur &e" + victim.getName() + "&6 !"));
            }

            // Victim streak lost
            if (streak >= 3) {
                Bukkit.broadcastMessage(c("&c" + killer.getName() + " &7a mis fin à la série de &e" + streak + " kills &7de &c" + victim.getName() + "&7 !"));
            }

            killer.sendMessage(c("&a+1 kill &7| Total: &e" + killerKills + " &7| Série: &6" + killerStreak));
        }

        // Notify faction members
        if (victimFaction != null) {
            victimFaction.getMembers().keySet().forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && !p.equals(victim)) {
                    p.sendMessage(pre() + c("&c" + victim.getName() + " &aest mort ! Puissance : &e" + String.format("%.1f", victimFaction.getPower()) + "/" + victimFaction.getMaxClaims()));
                }
            });
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player) attacker = (Player) damager;
        else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player) attacker = (Player) proj.getShooter();
        if (attacker == null || attacker.equals(victim)) return;

        FactionManager fm = plugin.getFactionManager();
        Faction aFaction = fm.getPlayerFaction(attacker.getUniqueId());
        Faction vFaction = fm.getPlayerFaction(victim.getUniqueId());

        // Tir amical désactivé
        if (aFaction != null && vFaction != null
                && aFaction.getName().equalsIgnoreCase(vFaction.getName())
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
        Chunk chunk = event.getBlock().getChunk();
        Faction owner = fm.getFactionByClaim(chunk);
        if (owner == null) return;
        Faction playerFaction = fm.getPlayerFaction(player.getUniqueId());
        if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(owner.getName())) {
            event.setCancelled(true);
            player.sendMessage(c("&cCe territoire appartient à la faction &e[" + owner.getTag() + "] " + owner.getName() + "&c."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.factions.admin")) return;
        FactionManager fm = plugin.getFactionManager();
        Chunk chunk = event.getBlock().getChunk();
        Faction owner = fm.getFactionByClaim(chunk);
        if (owner == null) return;
        Faction playerFaction = fm.getPlayerFaction(player.getUniqueId());
        if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(owner.getName())) {
            event.setCancelled(true);
            player.sendMessage(c("&cCe territoire appartient à la faction &e[" + owner.getTag() + "] " + owner.getName() + "&c."));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        FactionManager fm = plugin.getFactionManager();
        if (!fm.isAutoclaiming(player.getUniqueId())) return;

        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) return;

        Faction f = fm.getPlayerFaction(player.getUniqueId());
        if (f == null) { fm.toggleAutoclaim(player.getUniqueId()); return; }
        if (!f.isOfficer(player.getUniqueId())) { fm.toggleAutoclaim(player.getUniqueId()); return; }

        if (f.getClaims().size() >= f.getMaxClaims()) {
            player.sendMessage(c("&cAuto-claim : puissance insuffisante."));
            fm.toggleAutoclaim(player.getUniqueId());
            return;
        }
        if (fm.getFactionByClaim(to) != null) return;

        f.addClaim(to);
        fm.saveAll();
        player.sendMessage(c("&a[Auto-claim] Chunk claim ! &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
    }
}
