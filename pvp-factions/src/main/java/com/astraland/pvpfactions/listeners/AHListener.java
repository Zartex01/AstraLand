package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.ah.AHGui;
import com.astraland.pvpfactions.ah.AHSellGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class AHListener implements Listener {

    private final PvpFactions plugin;

    public AHListener(PvpFactions plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof AHGui ahGui) {
            ahGui.handleClick(event);
            return;
        }
        if (event.getInventory().getHolder() instanceof AHSellGUI sellGUI) {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getRawSlot() >= event.getInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
            sellGUI.handleClick(event, plugin.getAuctionManager(), plugin.getEconomyManager(), player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof AHSellGUI)) return;
        if (AHSellGUI.AWAITING_PRICE.containsKey(player.getUniqueId())) return;
        AHSellGUI.SellSession session = AHSellGUI.SESSIONS.remove(player.getUniqueId());
        if (session != null && !session.confirmed) {
            player.getInventory().addItem(session.item.clone());
            player.sendMessage("\u00a7c[AH] \u00a77Mise en vente annulée. Item rendu.");
        }
    }
}
