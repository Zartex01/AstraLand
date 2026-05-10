package com.astraland.spleef.listeners;

import com.astraland.spleef.Spleef;
import com.astraland.spleef.shop.ShopCategoryData;
import com.astraland.spleef.shop.ShopCategoryGUI;
import com.astraland.spleef.shop.ShopMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final Spleef plugin;
    public ShopListener(Spleef plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof ShopMenuGUI menu) {
            event.setCancelled(true);
            if (event.getRawSlot() >= event.getInventory().getSize()) return;
            ShopCategoryData cat = menu.getCategoryAt(event.getRawSlot());
            if (cat != null) {
                Runnable back = () -> new ShopMenuGUI(player, menu.getEconomyManager()).open(player);
                new ShopCategoryGUI(cat, 0, player, menu.getEconomyManager(), back).open(player);
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof ShopCategoryGUI catGUI) {
            if (event.getRawSlot() >= event.getInventory().getSize()) { event.setCancelled(true); return; }
            catGUI.handleClick(event, plugin.getEconomyManager(), player);
        }
    }
}
