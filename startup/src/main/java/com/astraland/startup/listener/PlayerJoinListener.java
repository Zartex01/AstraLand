package com.astraland.startup.listener;

import com.astraland.startup.AstraLandStartup;
import com.astraland.startup.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerJoinListener implements Listener {

    private final AstraLandStartup plugin;

    public PlayerJoinListener(AstraLandStartup plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        giveCompass(event.getPlayer());
    }

    public void giveCompass(Player player) {
        ConfigManager config = plugin.getConfigManager();
        int slot = config.getCompassSlot();

        ItemStack existing = player.getInventory().getItem(slot);
        if (existing != null && existing.getType() == Material.COMPASS) {
            return;
        }

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName(color(config.getCompassName()));

        List<String> lore = config.getCompassLore().stream()
            .map(this::color)
            .collect(Collectors.toList());
        meta.setLore(lore);

        compass.setItemMeta(meta);
        player.getInventory().setItem(slot, compass);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
