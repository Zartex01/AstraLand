package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.shop.KitGUI;
import com.astraland.pvpfactions.shop.ShopCategoryData;
import com.astraland.pvpfactions.shop.ShopCategoryGUI;
import com.astraland.pvpfactions.shop.ShopMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {

    private final PvpFactions plugin;

    public ShopListener(PvpFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof ShopMenuGUI menu) {
            event.setCancelled(true);
            if (event.getRawSlot() >= event.getInventory().getSize()) return;
            ShopCategoryData cat = menu.getCategoryAt(event.getRawSlot());
            if (cat != null) {
                Runnable back = () -> new ShopMenuGUI(player, plugin.getEconomyManager(), plugin.getShopConfigManager()).open(player);
                new ShopCategoryGUI(cat, 0, player, plugin.getEconomyManager(), back).open(player);
            }
            return;
        }

        if (event.getInventory().getHolder() instanceof ShopCategoryGUI catGUI) {
            if (event.getRawSlot() >= event.getInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
            catGUI.handleClick(event, plugin.getEconomyManager(), player);
            return;
        }

        if (event.getInventory().getHolder() instanceof KitGUI kitGUI) {
            if (event.getRawSlot() >= event.getInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
            kitGUI.handleClick(event, player);
        }
    }
}
