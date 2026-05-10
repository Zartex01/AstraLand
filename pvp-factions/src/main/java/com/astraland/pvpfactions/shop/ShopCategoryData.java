package com.astraland.pvpfactions.shop;

import org.bukkit.Material;

import java.util.List;

public record ShopCategoryData(
    String id,
    String displayName,
    Material icon,
    String description,
    List<ShopItemData> items
) {}
