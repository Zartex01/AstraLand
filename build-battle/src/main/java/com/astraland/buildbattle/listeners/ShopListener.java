package com.astraland.buildbattle.listeners;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.shop.ShopGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final BuildBattle plugin;
    public ShopListener(BuildBattle plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopGUI shopGUI)) return;
        shopGUI.handleClick(event, plugin.getEconomyManager());
    }
}
