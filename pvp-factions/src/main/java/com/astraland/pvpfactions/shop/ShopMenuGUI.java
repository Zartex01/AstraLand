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

    private static final int[] CATEGORY_SLOTS = {20, 22, 24, 29, 31, 33};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();

    public ShopMenuGUI(Player player, EconomyManager eco, ShopConfigManager config) {
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &6&lSHOP &8&l✦ &7PvP/Factions"));
        build(player, eco, config);
    }

    private void build(Player player, EconomyManager eco, ShopConfigManager config) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.ORANGE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);

        inv.setItem(4, makeHeader());
        inv.setItem(49, makeBalance(eco.getBalance(player.getUniqueId())));

        List<ShopCategoryData> cats = config.getCategories();
        for (int i = 0; i < cats.size() && i < CATEGORY_SLOTS.length; i++) {
            ShopCategoryData cat = cats.get(i);
            inv.setItem(CATEGORY_SLOTS[i], makeCategoryButton(cat));
            categorySlots.put(CATEGORY_SLOTS[i], cat);
        }
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&6&l✦ Shop PvP/Factions ✦"));
        m.setLore(List.of(
            c("&7Bienvenue dans le shop !"),
            c("&7Clique sur une catégorie pour explorer."),
            c(""),
            c("&a▶ Clic gauche &f: Acheter un item"),
            c("&6▶ Clic droit &f: Vendre un item"),
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
        m.setDisplayName(c("&e&lTon Solde : &6" + balance + " $"));
        m.setLore(List.of(
            c("&7/balance &8— &7voir ton solde"),
            c("&7/pay &8— &7payer un joueur")
        ));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack makeCategoryButton(ShopCategoryData cat) {
        ItemStack item = new ItemStack(cat.icon());
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c(cat.displayName()));
        m.setLore(List.of(
            c(cat.description()),
            c(""),
            c("&f" + cat.items().size() + " &7articles disponibles"),
            c(""),
            c("&7▶ &fClic pour ouvrir")
        ));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        item.setItemMeta(m);
        return item;
    }

    public ShopCategoryData getCategoryAt(int slot) {
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
