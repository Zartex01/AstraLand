package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenuGUI implements InventoryHolder {

    private final Inventory inv;
    private final Map<Integer, ShopCategory> categorySlots = new HashMap<>();

    public ShopMenuGUI(Player player, EconomyManager eco) {
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &6&lSHOP &8&l✦ &7PvP/Factions"));
        build(player, eco);
    }

    private void build(Player player, EconomyManager eco) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.ORANGE_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);
        inv.setItem(4, makeHeader());

        int balance = eco.getBalance(player.getUniqueId());
        inv.setItem(49, makeBalance(balance));

        int[] slots = {20, 22, 24, 29, 31, 33};
        ShopCategory[] cats = ShopCategory.values();
        for (int i = 0; i < cats.length && i < slots.length; i++) {
            inv.setItem(slots[i], makeCategoryButton(cats[i]));
            categorySlots.put(slots[i], cats[i]);
        }

        inv.setItem(11, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(13, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(15, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(38, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(40, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(42, glass(Material.GRAY_STAINED_GLASS_PANE, " "));
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&6&l✦ Shop PvP/Factions ✦"));
        m.setLore(List.of(
            c("&7Bienvenue dans le shop !"),
            c("&7Clique sur une catégorie pour acheter."),
            c(""),
            c("&eGagne de l'argent en tuant des joueurs.")
        ));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(m);
        return item;
    }

    private ItemStack makeBalance(int balance) {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&e&lTon Solde"));
        m.setLore(List.of(
            c("&6" + balance + " &e$"),
            c(""),
            c("&7/balance &8— &7voir ton solde"),
            c("&7/pay &8— &7payer un joueur")
        ));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack makeCategoryButton(ShopCategory cat) {
        ItemStack item = new ItemStack(cat.getIcon());
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c(cat.getDisplayName()));
        m.setLore(List.of(
            c(cat.getDescription()),
            c(""),
            c("&7▶ &fClic pour ouvrir")
        ));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        item.setItemMeta(m);
        return item;
    }

    public ShopCategory getCategoryAt(int slot) {
        return categorySlots.get(slot);
    }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }

    private ItemStack glass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        item.setItemMeta(m);
        return item;
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
