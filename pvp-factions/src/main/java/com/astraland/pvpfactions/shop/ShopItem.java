package com.astraland.pvpfactions.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record ShopItem(String name, Material icon, int price, String[] lore, ItemStack[] reward) {}
