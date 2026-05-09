package com.astraland.startup.listener;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

public class WorldChangeListener implements Listener {

    private static final String MAIN_WORLD = "world";

    private final AstraLandStartup plugin;

    public WorldChangeListener(AstraLandStartup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player      = event.getPlayer();
        String nowWorld    = player.getWorld().getName();
        String beforeWorld = event.getFrom().getName();

        if (nowWorld.equals(MAIN_WORLD)) {
            plugin.getPlayerJoinListener().giveCompass(player);
            player.setScoreboard(org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard());
        } else if (beforeWorld.equals(MAIN_WORLD)) {
            removeCompass(player);
        }
    }

    private void removeCompass(Player player) {
        int slot = plugin.getConfigManager().getCompassSlot();
        ItemStack item = player.getInventory().getItem(slot);
        if (item != null && item.getType() == Material.COMPASS) {
            player.getInventory().setItem(slot, null);
        }
    }
}
