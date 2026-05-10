package com.astraland.buildbattle.listeners;

import com.astraland.buildbattle.ah.AHGui;
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
