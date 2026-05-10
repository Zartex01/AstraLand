package com.astraland.bedwars.listeners;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.shop.ShopGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final Bedwars plugin;
    public ShopListener(Bedwars plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI shopGUI)) return;
        shopGUI.handleClick(event, plugin.getEconomyManager());
    }
}
