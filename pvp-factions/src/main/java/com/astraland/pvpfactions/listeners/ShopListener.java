package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.shop.ShopGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final PvpFactions plugin;
    public ShopListener(PvpFactions plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI shopGUI)) return;
        shopGUI.handleClick(event, plugin.getEconomyManager());
    }
}
