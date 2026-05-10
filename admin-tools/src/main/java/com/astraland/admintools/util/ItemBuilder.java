package com.astraland.admintools.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    public static ItemStack make(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(c(name));
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) loreList.add(c(line));
            meta.setLore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack skull(OfflinePlayer player, String... extraLore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;
        meta.setOwningPlayer(player);
        meta.setDisplayName("§b" + player.getName());
        if (extraLore.length > 0) {
            List<String> lore = new ArrayList<>();
            for (String line : extraLore) lore.add(c(line));
            meta.setLore(lore);
        }
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack glass(Material glassMaterial) {
        ItemStack item = new ItemStack(glassMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
