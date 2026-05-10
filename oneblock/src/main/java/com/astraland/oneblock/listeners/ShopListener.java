package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.shop.ShopGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final OneBlock plugin;
    public ShopListener(OneBlock plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI shopGUI)) return;
        shopGUI.handleClick(event, plugin.getEconomyManager());
    }
}
