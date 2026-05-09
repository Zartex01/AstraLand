package com.astraland.skywars.listeners;

import com.astraland.skywars.Skywars;
import com.astraland.skywars.managers.SkywarsManager;
import com.astraland.skywars.models.SkywarsArena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SkywarsListener implements Listener {

    private final Skywars plugin;

    public SkywarsListener(Skywars plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!plugin.isInPluginWorld(victim)) return;
        SkywarsManager sm = plugin.getSkywarsManager();
        SkywarsArena arena = sm.getPlayerArena(victim.getUniqueId());
        if (arena == null || arena.getState() != SkywarsArena.State.INGAME) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        if (victim.getKiller() != null) {
            victim.getKiller().sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a+1 kill ! &e" + victim.getName() + " &aéliminé."));
        }

        sm.eliminatePlayer(arena, victim);
        victim.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTu es éliminé !"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isInPluginWorld(event.getPlayer())) return;
        SkywarsManager sm = plugin.getSkywarsManager();
        SkywarsArena arena = sm.getPlayerArena(event.getPlayer().getUniqueId());
        if (arena != null) sm.leaveArena(arena, event.getPlayer());
    }
}
