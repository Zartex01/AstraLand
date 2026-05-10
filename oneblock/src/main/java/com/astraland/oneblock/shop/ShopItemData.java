package com.astraland.oneblock.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record ShopItemData(
    String name,
    Material icon,
    int buyPrice,
    int sellPrice,
    String[] lore,
    ItemStack reward
) {
    public boolean isSellable() { return sellPrice > 0; }
}
