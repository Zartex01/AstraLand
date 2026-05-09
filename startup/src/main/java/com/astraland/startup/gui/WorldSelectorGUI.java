package com.astraland.startup.gui;

import com.astraland.startup.AstraLandStartup;
import com.astraland.startup.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldSelectorGUI implements Listener {

    private final AstraLandStartup plugin;
    private final Map<Integer, String> slotToWorldName = new HashMap<>();

    public WorldSelectorGUI(AstraLandStartup plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        ConfigManager config = plugin.getConfigManager();
        int rows = Math.max(1, Math.min(6, config.getGuiRows()));
        int size = rows * 9;

        String title = color(config.getGuiTitle());
        Inventory inv = Bukkit.createInventory(new WorldSelectorHolder(), size, title);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < size; i++) {
            inv.setItem(i, glass);
        }

        slotToWorldName.clear();

        for (Map.Entry<String, ConfigManager.WorldConfig> entry : config.getWorlds().entrySet()) {
            ConfigManager.WorldConfig worldConfig = entry.getValue();
            int slot = worldConfig.getSlot();

            if (slot < 0 || slot >= size) {
                plugin.getLogger().warning("Slot " + slot + " invalide pour le monde '" + entry.getKey() + "' (taille GUI : " + size + ")");
                continue;
            }

            ItemStack item = new ItemStack(worldConfig.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(color(worldConfig.getName()));
            List<String> lore = worldConfig.getLore().stream()
                .map(this::color)
                .collect(Collectors.toList());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slotToWorldName.put(slot, worldConfig.getWorldName());
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof WorldSelectorHolder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (!slotToWorldName.containsKey(slot)) return;

        String worldName = slotToWorldName.get(slot);
        World world = Bukkit.getWorld(worldName);

        player.closeInventory();

        if (world == null) {
            player.sendMessage(color("&cLe monde &e" + worldName + " &cn'est pas chargé."));
            return;
        }

        if (player.getWorld().getName().equals("world")) {
            plugin.getLocationManager().save(player.getUniqueId(), player.getLocation());
        }

        player.teleport(world.getSpawnLocation());
        player.sendMessage(color("&aTéléportation vers &e" + worldName + " &a!"));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
