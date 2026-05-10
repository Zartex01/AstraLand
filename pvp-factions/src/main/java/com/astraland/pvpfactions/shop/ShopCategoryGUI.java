package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopCategoryGUI implements InventoryHolder {

    private static final int[] ITEM_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    private static final int BACK_SLOT  = 49;
    private static final int PREV_SLOT  = 45;
    private static final int NEXT_SLOT  = 53;
    private static final int INFO_SLOT  = 4;

    private final Inventory inv;
    private final ShopCategory category;
    private final Map<Integer, ShopItem> itemSlots = new HashMap<>();
    private final int page;
    private final int totalPages;

    public ShopCategoryGUI(ShopCategory category, int page, Player player, EconomyManager eco) {
        this.category = category;
        List<ShopItem> items = category.getItems();
        int perPage = ITEM_SLOTS.length;
        this.totalPages = Math.max(1, (int) Math.ceil((double) items.size() / perPage));
        this.page = Math.max(0, Math.min(page, totalPages - 1));

        inv = Bukkit.createInventory(this, 54, c("&8&l« &r" + c(category.getDisplayName()) + " &8&l»"));
        build(items, player, eco);
    }

    private void build(List<ShopItem> items, Player player, EconomyManager eco) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);
        for (int r = 1; r <= 4; r++) {
            inv.setItem(r * 9, border);
            inv.setItem(r * 9 + 8, border);
        }

        inv.setItem(INFO_SLOT, makeInfo(eco.getBalance(player.getUniqueId())));
        inv.setItem(BACK_SLOT, makeBack());
        if (page > 0) inv.setItem(PREV_SLOT, makeNav(false));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, makeNav(true));

        int start = page * ITEM_SLOTS.length;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= items.size()) break;
            ShopItem shopItem = items.get(idx);
            inv.setItem(ITEM_SLOTS[i], makeDisplay(shopItem));
            itemSlots.put(ITEM_SLOTS[i], shopItem);
        }
    }

    public void handleClick(InventoryClickEvent e, EconomyManager eco, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == BACK_SLOT) {
            new ShopMenuGUI(player, eco).open(player);
            return;
        }
        if (slot == PREV_SLOT && page > 0) {
            new ShopCategoryGUI(category, page - 1, player, eco).open(player);
            return;
        }
        if (slot == NEXT_SLOT && page < totalPages - 1) {
            new ShopCategoryGUI(category, page + 1, player, eco).open(player);
            return;
        }

        ShopItem shopItem = itemSlots.get(slot);
        if (shopItem == null) return;

        if (!eco.removeBalance(player.getUniqueId(), shopItem.price())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! &7Il te faut &e" + shopItem.price() + " $ &7(ton solde : &e" + eco.getBalance(player.getUniqueId()) + " $&7)."));
            return;
        }

        for (ItemStack reward : shopItem.reward()) {
            if (reward != null) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward.clone());
                leftover.values().forEach(leftItem -> player.getWorld().dropItemNaturally(player.getLocation(), leftItem));
            }
        }
        player.sendMessage(c("&a✔ &f" + c(shopItem.name()) + " &aacheté pour &e" + shopItem.price() + " $ &a| Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);

        refreshBalanceDisplay(player, eco);
    }

    private void refreshBalanceDisplay(Player player, EconomyManager eco) {
        inv.setItem(INFO_SLOT, makeInfo(eco.getBalance(player.getUniqueId())));
    }

    private ItemStack makeDisplay(ShopItem shopItem) {
        ItemStack display = new ItemStack(shopItem.icon());
        ItemMeta m = display.getItemMeta();
        m.setDisplayName(c(shopItem.name()));
        List<String> lore = new ArrayList<>();
        Arrays.stream(shopItem.lore()).forEach(l -> lore.add(c(l)));
        lore.add("");
        lore.add(c("&6Prix : &e" + shopItem.price() + " $"));
        lore.add(c("&7▶ &fClic pour acheter"));
        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        display.setItemMeta(m);
        return display;
    }

    private ItemStack makeInfo(int balance) {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&e&lTon Solde : &6" + balance + " $"));
        m.setLore(List.of(
            c(category.getDescription()),
            c(""),
            c("&7Page &f" + (page + 1) + " &7/ &f" + totalPages)
        ));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack makeBack() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&c&l← Retour au menu"));
        m.setLore(List.of(c("&7Retourner au menu principal du shop")));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack makeNav(boolean next) {
        ItemStack item = new ItemStack(next ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(next ? c("&a&lPage suivante →") : c("&c&l← Page précédente"));
        m.setLore(List.of(c("&7Page " + (next ? page + 2 : page) + " / " + totalPages)));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack glass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        item.setItemMeta(m);
        return item;
    }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
