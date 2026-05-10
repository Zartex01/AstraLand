package com.astraland.skyblock.shop;

import com.astraland.skyblock.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
            ChatColor.translateAlternateColorCodes('&', "&a&l🌿 &2Shop Skyblock &a&l🌿"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.LIME_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.GRASS_BLOCK, "&2&lSkyblock Shop", "&7Achète ou vends tes ressources | &aClic gauche &7: Acheter | &6Clic droit &7: Vendre"));

        /* ── Graines & Cultures ── */
        inv.setItem(10, section("&2&lGRAINES & CULTURES"));
        set(11, d(Material.WHEAT_SEEDS, "&aGraines de Blé ×16", 5, 2, "&7Cultive du blé sur ton île"), 5, 2, new ItemStack(Material.WHEAT_SEEDS, 16));
        set(12, d(Material.CARROT, "&6Carottes ×8", 8, 3, "&7Plante directement"), 8, 3, new ItemStack(Material.CARROT, 8));
        set(13, d(Material.POTATO, "&ePommes de Terre ×8", 8, 3), 8, 3, new ItemStack(Material.POTATO, 8));
        set(14, d(Material.MELON_SEEDS, "&aSem. de Melon ×8", 15, 7), 15, 7, new ItemStack(Material.MELON_SEEDS, 8));
        set(15, d(Material.PUMPKIN_SEEDS, "&eSem. de Citrouille ×8", 15, 7), 15, 7, new ItemStack(Material.PUMPKIN_SEEDS, 8));
        set(16, d(Material.SUGAR_CANE, "&aCanne à Sucre ×16", 10, 5), 10, 5, new ItemStack(Material.SUGAR_CANE, 16));

        /* ── Outils ── */
        inv.setItem(19, section("&2&lOUTILS"));
        set(20, d(Material.IRON_HOE, "&fHoue de Fer", 30, 0, "&7Efficacité I"), 30, 0, ench(new ItemStack(Material.IRON_HOE), Enchantment.EFFICIENCY, 1));
        set(21, d(Material.IRON_PICKAXE, "&fPioche de Fer", 60, 0, "&7Efficacité I"), 60, 0, ench(new ItemStack(Material.IRON_PICKAXE), Enchantment.EFFICIENCY, 1));
        set(22, d(Material.IRON_AXE, "&fHache de Fer", 50, 0), 50, 0, new ItemStack(Material.IRON_AXE));
        set(23, d(Material.FISHING_ROD, "&bCanne à Pêche", 40, 0, "&7Chance de Mer II"), 40, 0, ench(new ItemStack(Material.FISHING_ROD), Enchantment.LUCK_OF_THE_SEA, 2));
        set(24, d(Material.DIAMOND_PICKAXE, "&bPioche Diamant", 200, 0, "&7Efficacité III"), 200, 0, ench(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.EFFICIENCY, 3));

        /* ── Blocs ── */
        inv.setItem(28, section("&2&lBLOCS"));
        set(29, d(Material.COBBLESTONE, "&7Cobblestone ×64", 40, 20), 40, 20, new ItemStack(Material.COBBLESTONE, 64));
        set(30, d(Material.DIRT, "&6Terre ×64", 30, 15), 30, 15, new ItemStack(Material.DIRT, 64));
        set(31, d(Material.SAND, "&eSable ×64", 35, 17), 35, 17, new ItemStack(Material.SAND, 64));
        set(32, d(Material.OAK_LOG, "&6Bûche ×32", 50, 25), 50, 25, new ItemStack(Material.OAK_LOG, 32));
        set(33, d(Material.STONE, "&7Pierre ×64", 45, 22), 45, 22, new ItemStack(Material.STONE, 64));

        /* ── Ressources ── */
        inv.setItem(37, section("&2&lRESSOURCES"));
        set(38, d(Material.BONE_MEAL, "&fFarine d'Os ×16", 25, 12, "&7Accélère la croissance"), 25, 12, new ItemStack(Material.BONE_MEAL, 16));
        set(39, d(Material.IRON_INGOT, "&7Lingot de Fer ×8", 50, 25), 50, 25, new ItemStack(Material.IRON_INGOT, 8));
        set(40, d(Material.GOLD_INGOT, "&6Lingot d'Or ×4", 60, 30), 60, 30, new ItemStack(Material.GOLD_INGOT, 4));
        set(41, d(Material.DIAMOND, "&bDiamant", 100, 50), 100, 50, new ItemStack(Material.DIAMOND, 1));
        set(42, d(Material.ENDER_PEARL, "&5Perle d'Ender ×4", 80, 40), 80, 40, new ItemStack(Material.ENDER_PEARL, 4));
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
    private ItemStack section(String name) {
        ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); i.setItemMeta(m); return i;
    }
    private ItemStack header(Material mat, String name, String desc) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        m.setDisplayName(c(name)); m.setLore(List.of(c(desc)));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); i.setItemMeta(m); return i;
    }
    private ItemStack border(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private ItemStack ench(ItemStack item, Enchantment e, int lvl) { item.addUnsafeEnchantment(e, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
