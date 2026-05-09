package com.astraland.startup.gui;

import com.astraland.startup.AstraLandStartup;
import com.astraland.startup.manager.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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

        Component title = LegacyComponentSerializer.legacyAmpersand()
            .deserialize(config.getGuiTitle());

        Inventory inv = Bukkit.createInventory(new WorldSelectorHolder(), size, title);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
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

            meta.displayName(
                LegacyComponentSerializer.legacyAmpersand().deserialize(worldConfig.getName())
            );

            List<Component> lore = worldConfig.getLore().stream()
                .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                .collect(Collectors.toList());
            meta.lore(lore);

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
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&cLe monde &e" + worldName + " &cn'est pas chargé. Vérifie que Multiverse-Core l'a bien créé."));
            return;
        }

        player.teleport(world.getSpawnLocation());
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand()
            .deserialize("&aTéléportation vers &e" + worldName + " &a!"));
    }
}
