package com.astraland.duels.listeners;

import com.astraland.duels.Duels;
import com.astraland.duels.shop.ShopGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final Duels plugin;
    public ShopListener(Duels plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI shopGUI)) return;
        shopGUI.handleClick(event, plugin.getEconomyManager());
    }
}
