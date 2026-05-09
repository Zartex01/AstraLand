package com.astraland.startup.listener;

import com.astraland.startup.AstraLandStartup;
import com.astraland.startup.manager.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

        meta.displayName(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize(config.getCompassName())
        );

        List<Component> lore = config.getCompassLore().stream()
            .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
            .collect(Collectors.toList());
        meta.lore(lore);

        compass.setItemMeta(meta);
        player.getInventory().setItem(slot, compass);
    }
}
