package com.astraland.startup.listener;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

public class CompassLockListener implements Listener {

    private final AstraLandStartup plugin;

    public CompassLockListener(AstraLandStartup plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getWorld().getName().equals("world")) return;

        int compassSlot = plugin.getConfigManager().getCompassSlot();

        ItemStack current = event.getCurrentItem();
        ItemStack cursor  = event.getCursor();

        boolean currentIsCompass = current != null && current.getType() == Material.COMPASS;
        boolean cursorIsCompass  = cursor  != null && cursor.getType()  == Material.COMPASS;

        if (currentIsCompass || cursorIsCompass) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() instanceof PlayerInventory
                && event.getSlot() == compassSlot) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.getWorld().getName().equals("world")) return;

        int compassSlot = plugin.getConfigManager().getCompassSlot();

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == compassSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (!event.getPlayer().getWorld().getName().equals("world")) return;
        if (event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            event.setCancelled(true);
        }
    }
}
