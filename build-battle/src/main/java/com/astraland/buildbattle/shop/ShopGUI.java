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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI implements InventoryHolder {

    private final Inventory inv;
    private final Map<Integer, ShopEntry> entries = new HashMap<>();
    private record ShopEntry(int price, int sellPrice, ItemStack[] reward) {}

    public ShopGUI() {
        inv = Bukkit.createInventory(this, 54,
            ChatColor.translateAlternateColorCodes('&', "&e&l🔨 &6Shop Build Battle &e&l🔨"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.YELLOW_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.CRAFTING_TABLE, "&6&lBuild Battle Shop", "&7Des blocs pour ta construction ! | &aClic gauche &7: Acheter | &6Clic droit &7: Vendre"));

        /* ── Laine ── */
        inv.setItem(10, section("&6&lLAINE (×64)"));
        set(11, d(Material.WHITE_WOOL, "&fLaine Blanche ×64", 20, 10), 20, 10, new ItemStack(Material.WHITE_WOOL, 64));
        set(12, d(Material.RED_WOOL, "&cLaine Rouge ×64", 20, 10), 20, 10, new ItemStack(Material.RED_WOOL, 64));
        set(13, d(Material.BLUE_WOOL, "&9Laine Bleue ×64", 20, 10), 20, 10, new ItemStack(Material.BLUE_WOOL, 64));
        set(14, d(Material.GREEN_WOOL, "&aLaine Verte ×64", 20, 10), 20, 10, new ItemStack(Material.GREEN_WOOL, 64));
        set(15, d(Material.YELLOW_WOOL, "&eLaine Jaune ×64", 20, 10), 20, 10, new ItemStack(Material.YELLOW_WOOL, 64));
        set(16, d(Material.PURPLE_WOOL, "&5Laine Violette ×64", 20, 10), 20, 10, new ItemStack(Material.PURPLE_WOOL, 64));

        /* ── Terracotta & Béton ── */
        inv.setItem(19, section("&6&lTERRACOTTA & BÉTON"));
        set(20, d(Material.WHITE_TERRACOTTA, "&fTerracotta Blanche ×64", 25, 12), 25, 12, new ItemStack(Material.WHITE_TERRACOTTA, 64));
        set(21, d(Material.WHITE_CONCRETE, "&fBéton Blanc ×64", 30, 15), 30, 15, new ItemStack(Material.WHITE_CONCRETE, 64));
        set(22, d(Material.ORANGE_CONCRETE, "&6Béton Orange ×64", 30, 15), 30, 15, new ItemStack(Material.ORANGE_CONCRETE, 64));
        set(23, d(Material.CYAN_CONCRETE, "&bBéton Cyan ×64", 30, 15), 30, 15, new ItemStack(Material.CYAN_CONCRETE, 64));
        set(24, d(Material.MAGENTA_CONCRETE, "&dBéton Magenta ×64", 30, 15), 30, 15, new ItemStack(Material.MAGENTA_CONCRETE, 64));

        /* ── Décorations ── */
        inv.setItem(28, section("&6&lDÉCORATIONS"));
        set(29, d(Material.OAK_TRAPDOOR, "&6Trappe Chêne ×16", 25, 12), 25, 12, new ItemStack(Material.OAK_TRAPDOOR, 16));
        set(30, d(Material.OAK_STAIRS, "&6Escaliers Chêne ×32", 30, 15), 30, 15, new ItemStack(Material.OAK_STAIRS, 32));
        set(31, d(Material.GLASS_PANE, "&7Vitre ×32", 25, 12), 25, 12, new ItemStack(Material.GLASS_PANE, 32));
        set(32, d(Material.FLOWER_POT, "&6Pot de Fleur ×8", 20, 10), 20, 10, new ItemStack(Material.FLOWER_POT, 8));
        set(33, d(Material.LANTERN, "&eLanterne ×16", 30, 15), 30, 15, new ItemStack(Material.LANTERN, 16));

        /* ── Végétation & Lumière ── */
        inv.setItem(37, section("&6&lVÉGÉTATION & LUMIÈRE"));
        set(38, d(Material.OAK_LEAVES, "&aFeuilles ×32", 15, 7), 15, 7, new ItemStack(Material.OAK_LEAVES, 32));
        set(39, d(Material.GRASS_BLOCK, "&aHerbe ×32", 15, 7), 15, 7, new ItemStack(Material.GRASS_BLOCK, 32));
        set(40, d(Material.GLOWSTONE, "&eGlowstone ×16", 30, 15), 30, 15, new ItemStack(Material.GLOWSTONE, 16));
        set(41, d(Material.SEA_LANTERN, "&bLanterne Marine ×16", 35, 17), 35, 17, new ItemStack(Material.SEA_LANTERN, 16));
        set(42, d(Material.SHROOMLIGHT, "&6Champignon Lumineux ×16", 40, 20), 40, 20, new ItemStack(Material.SHROOMLIGHT, 16));
    }

    private void set(int slot, ItemStack display, int price, int sellPrice, ItemStack... reward) {
        inv.setItem(slot, display); entries.put(slot, new ShopEntry(price, sellPrice, reward));
    }

    public void handleClick(InventoryClickEvent e, EconomyManager eco) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ShopEntry entry = entries.get(e.getSlot());
        if (entry == null) return;

        if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
            handleSell(player, eco, entry);
        } else {
            handleBuy(player, eco, entry);
        }
    }

    private void handleBuy(Player player, EconomyManager eco, ShopEntry entry) {
        if (!eco.removeBalance(player.getUniqueId(), entry.price())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + entry.price() + " $&c.")); return;
        }
        for (ItemStack item : entry.reward()) if (item != null) player.getInventory().addItem(item.clone());
        player.sendMessage(c("&a✔ Achat pour &6" + entry.price() + " $&a. &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
    }

    private void handleSell(Player player, EconomyManager eco, ShopEntry entry) {
        if (entry.sellPrice() <= 0 || entry.reward().length != 1) {
            player.sendMessage(c("&c✗ Cet item n'est pas vendable.")); return;
        }
        ItemStack ref = entry.reward()[0];
        Material mat = ref.getType();
        int needed = ref.getAmount();
        int inInv = countInInventory(player, mat);
        if (inInv == 0) {
            player.sendMessage(c("&c✗ Tu n'as pas de &f" + mat.name().toLowerCase().replace('_', ' ') + " &cà vendre.")); return;
        }
        int toSell = Math.min(inInv, needed);
        int gained = (int) Math.floor((double) entry.sellPrice() / needed * toSell);
        removeFromInventory(player, mat, toSell);
        eco.addBalance(player.getUniqueId(), gained);
        player.sendMessage(c("&e💰 Vendu : &f" + toSell + "x &7pour &6" + gained + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
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

    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }

    private ItemStack d(Material mat, String name, int price, int sellPrice, String... desc) {
        ItemStack item = new ItemStack(mat); ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(c(name));
        List<String> lore = new ArrayList<>(Arrays.stream(desc).map(this::c).toList());
        lore.add("");
        lore.add(c("&a🛒 Achat : &e" + price + " $"));
        if (sellPrice > 0) lore.add(c("&6💰 Vente : &e" + sellPrice + " $"));
        lore.add("");
        lore.add(c("&a▶ Clic gauche &fpour acheter"));
        if (sellPrice > 0) lore.add(c("&6▶ Clic droit &fpour vendre"));
        meta.setLore(lore); meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta); return item;
    }
    private ItemStack section(String name) { ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); i.setItemMeta(m); return i; }
    private ItemStack header(Material mat, String name, String desc) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); m.setLore(List.of(c(desc))); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); i.setItemMeta(m); return i; }
    private ItemStack border(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
