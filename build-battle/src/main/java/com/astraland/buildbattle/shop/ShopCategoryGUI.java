package com.astraland.buildbattle.shop;

import com.astraland.buildbattle.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopCategoryGUI implements InventoryHolder {

    private static final int[] ITEM_SLOTS = {
        10,11,12,13,14,15,16,
        19,20,21,22,23,24,25,
        28,29,30,31,32,33,34,
        37,38,39,40,41,42,43
    };
    private static final int BACK_SLOT = 49;
    private static final int PREV_SLOT = 45;
    private static final int NEXT_SLOT = 53;
    private static final int INFO_SLOT = 4;

    private final Inventory inv;
    private final ShopCategoryData category;
    private final Map<Integer, ShopItemData> itemSlots = new HashMap<>();
    private final int page;
    private final int totalPages;
    private final Runnable backAction;

    public ShopCategoryGUI(ShopCategoryData category, int page, Player player, EconomyManager eco, Runnable backAction) {
        this.category = category;
        this.backAction = backAction;
        List<ShopItemData> items = category.items();
        this.totalPages = Math.max(1, (int) Math.ceil((double) items.size() / ITEM_SLOTS.length));
        this.page = Math.max(0, Math.min(page, totalPages - 1));
        inv = Bukkit.createInventory(this, 54, c("&8&l« &r" + c(category.displayName()) + " &8&l»"));
        build(items, player, eco);
    }

    private void build(List<ShopItemData> items, Player player, EconomyManager eco) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.YELLOW_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);

        inv.setItem(INFO_SLOT, makeInfo(eco.getBalance(player.getUniqueId())));
        inv.setItem(BACK_SLOT, makeBack());
        if (page > 0) inv.setItem(PREV_SLOT, makeNav(false));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, makeNav(true));

        int start = page * ITEM_SLOTS.length;
        for (int i = 0; i < ITEM_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= items.size()) break;
            ShopItemData data = items.get(idx);
            inv.setItem(ITEM_SLOTS[i], makeDisplay(data));
            itemSlots.put(ITEM_SLOTS[i], data);
        }
    }

    public void handleClick(InventoryClickEvent e, EconomyManager eco, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == BACK_SLOT) { backAction.run(); return; }
        if (slot == PREV_SLOT && page > 0) { new ShopCategoryGUI(category, page - 1, player, eco, backAction).open(player); return; }
        if (slot == NEXT_SLOT && page < totalPages - 1) { new ShopCategoryGUI(category, page + 1, player, eco, backAction).open(player); return; }
        ShopItemData data = itemSlots.get(slot);
        if (data == null) return;
        ClickType click = e.getClick();
        if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) handleBuy(player, eco, data);
        else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) handleSell(player, eco, data);
    }

    private void handleBuy(Player player, EconomyManager eco, ShopItemData data) {
        if (data.buyPrice() <= 0) { player.sendMessage(c("&c✗ Cet item n'est pas en vente.")); return; }
        if (!eco.removeBalance(player.getUniqueId(), data.buyPrice())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! &7Il te faut &e" + data.buyPrice() + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $")); return;
        }
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(data.reward().clone());
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage(c("&a✔ Acheté : &f" + c(data.name()) + " &apour &e" + data.buyPrice() + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
        refreshInfo(player, eco);
    }

    private void handleSell(Player player, EconomyManager eco, ShopItemData data) {
        if (!data.isSellable()) { player.sendMessage(c("&c✗ Cet item n'est pas vendable.")); return; }
        Material mat = data.reward().getType();
        int needed = data.reward().getAmount();
        int inInv = countInInventory(player, mat);
        if (inInv == 0) { player.sendMessage(c("&c✗ Tu n'as pas de &f" + c(data.name()) + " &cà vendre.")); return; }
        int toSell = Math.min(inInv, needed);
        int gained = (int) Math.floor((double) data.sellPrice() / needed * toSell);
        removeFromInventory(player, mat, toSell);
        eco.addBalance(player.getUniqueId(), gained);
        player.sendMessage(c("&e💰 Vendu : &f" + toSell + "x " + c(data.name()) + " &epour &6" + gained + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 0.8f);
        refreshInfo(player, eco);
    }

    private int countInInventory(Player player, Material mat) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents())
            if (item != null && item.getType() == mat) count += item.getAmount();
        return count;
    }

    private void removeFromInventory(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != mat) continue;
            if (item.getAmount() <= remaining) { remaining -= item.getAmount(); player.getInventory().setItem(i, null); }
            else { item.setAmount(item.getAmount() - remaining); remaining = 0; }
        }
    }

    private void refreshInfo(Player player, EconomyManager eco) { inv.setItem(INFO_SLOT, makeInfo(eco.getBalance(player.getUniqueId()))); }

    private ItemStack makeDisplay(ShopItemData data) {
        ItemStack display = new ItemStack(data.icon()); ItemMeta m = display.getItemMeta();
        m.setDisplayName(c(data.name()));
        List<String> lore = new ArrayList<>();
        for (String l : data.lore()) lore.add(c(l));
        lore.add(""); lore.add(c("&a🛒 Achat : &e" + (data.buyPrice() > 0 ? data.buyPrice() + " $" : "Non disponible")));
        lore.add(c("&6💰 Vente : &e" + (data.isSellable() ? data.sellPrice() + " $" : "Non vendable")));
        lore.add(""); lore.add(c("&a▶ Clic gauche &fpour acheter"));
        if (data.isSellable()) lore.add(c("&6▶ Clic droit &fpour vendre"));
        m.setLore(lore); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        display.setItemMeta(m); return display;
    }

    private ItemStack makeInfo(int balance) {
        ItemStack item = new ItemStack(Material.SUNFLOWER); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&e&lSolde : &6" + balance + " $"));
        m.setLore(List.of(c(category.description()), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre"), c(""), c("&7Page &f" + (page + 1) + " &7/ &f" + totalPages)));
        item.setItemMeta(m); return item;
    }

    private ItemStack makeBack() {
        ItemStack item = new ItemStack(Material.ARROW); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&c&l← Retour au menu")); m.setLore(List.of(c("&7Retourner au menu principal du shop")));
        item.setItemMeta(m); return item;
    }

    private ItemStack makeNav(boolean next) {
        ItemStack item = new ItemStack(next ? Material.LIME_DYE : Material.RED_DYE); ItemMeta m = item.getItemMeta();
        m.setDisplayName(next ? c("&a&lPage suivante →") : c("&c&l← Page précédente"));
        m.setLore(List.of(c("&7Page " + (next ? page + 2 : page) + " / " + totalPages)));
        item.setItemMeta(m); return item;
    }

    private ItemStack glass(Material mat, String name) {
        ItemStack item = new ItemStack(mat); ItemMeta m = item.getItemMeta(); m.setDisplayName(name); item.setItemMeta(m); return item;
    }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
