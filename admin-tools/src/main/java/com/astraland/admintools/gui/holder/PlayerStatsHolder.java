package com.astraland.admintools.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerStatsHolder implements InventoryHolder {

    private final UUID targetUUID;

    public PlayerStatsHolder(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public UUID getTargetUUID() { return targetUUID; }

    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException();
    }
}
