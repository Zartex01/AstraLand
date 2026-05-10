package com.astraland.uhc.listeners;

import com.astraland.uhc.ah.AHGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class AHListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AHGui ahGui)) return;
        ahGui.handleClick(event);
    }
}
