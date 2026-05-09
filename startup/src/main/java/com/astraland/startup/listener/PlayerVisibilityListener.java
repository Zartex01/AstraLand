package com.astraland.startup.listener;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerVisibilityListener implements Listener {

    private static final String MAIN_WORLD = "world";

    private final AstraLandStartup plugin;

    public PlayerVisibilityListener(AstraLandStartup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joined = event.getPlayer();
        if (!joined.getWorld().getName().equals(MAIN_WORLD)) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!joined.isOnline()) return;
            hideInMainWorld(joined);
        }, 5L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player   = event.getPlayer();
        String nowWorld = player.getWorld().getName();
        String oldWorld = event.getFrom().getName();

        if (nowWorld.equals(MAIN_WORLD)) {
            hideInMainWorld(player);
        } else if (oldWorld.equals(MAIN_WORLD)) {
            revealToMainWorld(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player leaving = event.getPlayer();
        if (!leaving.getWorld().getName().equals(MAIN_WORLD)) return;
        revealToMainWorld(leaving);
    }

    private void hideInMainWorld(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (!other.getWorld().getName().equals(MAIN_WORLD)) continue;

            player.hidePlayer(plugin, other);
            other.hidePlayer(plugin, player);
        }
    }

    private void revealToMainWorld(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (!other.getWorld().getName().equals(MAIN_WORLD)) continue;

            other.showPlayer(plugin, player);
            player.showPlayer(plugin, other);
        }
    }
}
