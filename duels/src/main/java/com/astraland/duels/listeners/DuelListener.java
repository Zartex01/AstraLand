package com.astraland.duels.listeners;

import com.astraland.duels.Duels;
import com.astraland.duels.managers.DuelManager;
import com.astraland.duels.models.DuelMatch;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {

    private final Duels plugin;

    public DuelListener(Duels plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player loser = event.getEntity();
        if (!plugin.isInPluginWorld(loser)) return;
        DuelManager dm = plugin.getDuelManager();
        if (!dm.isInDuel(loser.getUniqueId())) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        dm.onDeath(loser);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!plugin.isInPluginWorld(victim)) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player) attacker = (Player) event.getDamager();
        else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj) {
            if (proj.getShooter() instanceof Player) attacker = (Player) proj.getShooter();
        }
        if (attacker == null) return;

        DuelManager dm = plugin.getDuelManager();
        boolean victimInDuel = dm.isInDuel(victim.getUniqueId());
        boolean attackerInDuel = dm.isInDuel(attacker.getUniqueId());

        if (!victimInDuel && !attackerInDuel) return;

        if (!victimInDuel || !attackerInDuel) {
            event.setCancelled(true);
            return;
        }

        DuelMatch match = dm.getMatch(attacker.getUniqueId());
        if (match == null || !match.hasPlayer(victim.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTu ne peux pas attaquer ce joueur."));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isInPluginWorld(event.getPlayer())) return;
        DuelManager dm = plugin.getDuelManager();
        if (dm.isInDuel(event.getPlayer().getUniqueId())) {
            dm.onDeath(event.getPlayer());
        }
    }
}
