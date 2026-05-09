package com.astraland.startup.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class WorldSelectorHolder implements InventoryHolder {

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("WorldSelectorHolder ne gère pas l'inventaire directement.");
    }
}
