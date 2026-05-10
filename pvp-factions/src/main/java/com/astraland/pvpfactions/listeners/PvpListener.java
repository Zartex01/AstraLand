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
        if (!plugin.isInPluginWorld(victim)) return;
        StatsManager sm = plugin.getStatsManager();
        FactionManager fm = plugin.getFactionManager();
        BountyManager bm = plugin.getBountyManager();

        int streakLost = sm.getCurrentStreak(victim.getUniqueId());
        sm.addDeath(victim.getUniqueId());

        Faction victimFaction = fm.getPlayerFaction(victim.getUniqueId());
        if (victimFaction != null) {
            double powerLoss = plugin.getConfig().getDouble("faction.power-loss-on-death", 2.0);
            victimFaction.removePower(powerLoss);
            fm.updatePower(victimFaction);
        }

        Player killer = victim.getKiller();
        if (killer != null) {
            sm.addKill(killer.getUniqueId());
            int killerStreak = sm.getCurrentStreak(killer.getUniqueId());
            int killerKills  = sm.getKills(killer.getUniqueId());

            int reward = plugin.getConfig().getInt("economy.kill-reward", 50);
            plugin.getEconomyManager().addBalance(killer.getUniqueId(), reward);
            killer.sendMessage(c("&a+" + reward + " pièces &7pour le kill !"));

            // Annonces de séries
            String streakMsg = switch (killerStreak) {
                case 5  -> "&e" + killer.getName() + " &6est en feu ! 5 kills consécutifs !";
                case 10 -> "&c" + killer.getName() + " &4est DOMINANT ! 10 kills !";
                case 20 -> "&4&l" + killer.getName() + " &c&lest INARRÊTABLE ! 20 kills !";
                case 30 -> "&5&l" + killer.getName() + " &d&lest un DIEU ! 30 kills !";
                default -> null;
            };
            if (streakMsg != null) Bukkit.broadcastMessage(c("&8[&6Streak&8] " + streakMsg));

            // Réclamer la prime
            int bounty = bm.claimBounty(killer.getUniqueId(), victim.getUniqueId());
            if (bounty > 0) {
                killer.sendMessage(pre() + c("&6Prime réclamée : &c" + bounty + " &6pièces sur &e" + victim.getName() + "&6 !"));
                Bukkit.broadcastMessage(c("&6[Prime] &e" + killer.getName() + " &aa réclamé &c" + bounty + " &6pièces sur &e" + victim.getName() + "&6 !"));
            }

            // Fin de série adverse
            if (streakLost >= 3)
                Bukkit.broadcastMessage(c("&c" + killer.getName() + " &7a mis fin à la série de &e" + streakLost + " kills &7de &c" + victim.getName() + "&7 !"));

            killer.sendMessage(c("&a+1 kill &7| Total: &e" + killerKills + " &7| Série: &6" + killerStreak));
        }

        // Notification membres de faction
        if (victimFaction != null) {
            victimFaction.getMembers().keySet().forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && !p.equals(victim))
                    p.sendMessage(pre() + c("&c" + victim.getName() + " &aest mort. Puissance : &e" + String.format("%.1f", victimFaction.getPower()) + "/" + victimFaction.getMaxClaims()));
            });
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!plugin.isInPluginWorld(victim)) return;
        Player attacker = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player p) attacker = p;
        else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) attacker = p;
        if (attacker == null || attacker.equals(victim)) return;

        FactionManager fm = plugin.getFactionManager();
        Faction aFaction = fm.getPlayerFaction(attacker.getUniqueId());
        Faction vFaction = fm.getPlayerFaction(victim.getUniqueId());

        if (aFaction != null && vFaction != null
                && aFaction.getName().equalsIgnoreCase(vFaction.getName())
                && plugin.getConfig().getBoolean("pvp.disable-friendly-fire", true)) {
            event.setCancelled(true);
            attacker.sendMessage(c("&cTir amical désactivé."));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        if (player.hasPermission("astraland.factions.admin")) return;
        FactionManager fm = plugin.getFactionManager();
        Faction owner = fm.getFactionByClaim(event.getBlock().getChunk());
        if (owner == null) return;
        Faction playerFaction = fm.getPlayerFaction(player.getUniqueId());
        if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(owner.getName())) {
            event.setCancelled(true);
            player.sendMessage(c("&cTerritoire de &e[" + owner.getTag() + "] " + owner.getName() + "&c."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        if (player.hasPermission("astraland.factions.admin")) return;
        FactionManager fm = plugin.getFactionManager();
        Faction owner = fm.getFactionByClaim(event.getBlock().getChunk());
        if (owner == null) return;
        Faction playerFaction = fm.getPlayerFaction(player.getUniqueId());
        if (playerFaction == null || !playerFaction.getName().equalsIgnoreCase(owner.getName())) {
            event.setCancelled(true);
            player.sendMessage(c("&cTerritoire de &e[" + owner.getTag() + "] " + owner.getName() + "&c."));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        FactionManager fm = plugin.getFactionManager();
        if (!fm.isAutoclaiming(player.getUniqueId())) return;

        Chunk from = event.getFrom().getChunk();
        Chunk to   = event.getTo().getChunk();
        if (from.getX() == to.getX() && from.getZ() == to.getZ()) return;

        Faction f = fm.getPlayerFaction(player.getUniqueId());
        if (f == null || !f.isOfficer(player.getUniqueId())) { fm.toggleAutoclaim(player.getUniqueId()); return; }
        if (f.getClaims().size() >= f.getMaxClaims()) {
            player.sendMessage(c("&c[Auto-claim] Puissance insuffisante. Auto-claim désactivé."));
            fm.toggleAutoclaim(player.getUniqueId()); return;
        }
        if (fm.getFactionByClaim(to) != null) return;

        fm.addClaim(f, to);
        player.sendMessage(c("&a[Auto-claim] Chunk claim ! &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
    }
}
