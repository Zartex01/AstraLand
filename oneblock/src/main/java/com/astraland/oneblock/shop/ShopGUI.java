package com.astraland.oneblock.shop;

import com.astraland.oneblock.managers.EconomyManager;
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
            ChatColor.translateAlternateColorCodes('&', "&5&l◈ &dShop OneBlock &5&l◈"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.PURPLE_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.COMMAND_BLOCK, "&d&lOneBlock Shop", "&7Ressources & améliorations | &aClic gauche &7: Acheter | &6Clic droit &7: Vendre"));

        /* ── Outils ── */
        inv.setItem(10, section("&d&lOUTILS"));
        set(11, d(Material.IRON_PICKAXE, "&fPioche de Fer", 60, 0, "&7Eff. I, Sans Brisure I"), 60, 0,
            ench(ench(new ItemStack(Material.IRON_PICKAXE), Enchantment.EFFICIENCY, 1), Enchantment.UNBREAKING, 1));
        set(12, d(Material.DIAMOND_PICKAXE, "&bPioche Diamant", 200, 0, "&7Eff. III, Sans Brisure II"), 200, 0,
            ench(ench(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.EFFICIENCY, 3), Enchantment.UNBREAKING, 2));
        set(13, d(Material.NETHERITE_PICKAXE, "&5Pioche Nétherite", 500, 0, "&7Eff. V, Fortune III"), 500, 0,
            ench(ench(new ItemStack(Material.NETHERITE_PICKAXE), Enchantment.EFFICIENCY, 5), Enchantment.FORTUNE, 3));
        set(14, d(Material.DIAMOND_AXE, "&bHache Diamant", 180, 0, "&7Tranchant II"), 180, 0,
            ench(new ItemStack(Material.DIAMOND_AXE), Enchantment.SHARPNESS, 2));

        /* ── Ressources ── */
        inv.setItem(19, section("&d&lRESSOURCES"));
        set(20, d(Material.IRON_INGOT, "&7Fer ×16", 50, 25), 50, 25, new ItemStack(Material.IRON_INGOT, 16));
        set(21, d(Material.GOLD_INGOT, "&6Or ×8", 60, 30), 60, 30, new ItemStack(Material.GOLD_INGOT, 8));
        set(22, d(Material.DIAMOND, "&bDiamant ×3", 100, 50), 100, 50, new ItemStack(Material.DIAMOND, 3));
        set(23, d(Material.EMERALD, "&aÉmeraude ×3", 90, 45), 90, 45, new ItemStack(Material.EMERALD, 3));
        set(24, d(Material.NETHERITE_INGOT, "&5Nétherite", 400, 200, "&7Lingot de Nétherite"), 400, 200, new ItemStack(Material.NETHERITE_INGOT));

        /* ── Spéciaux ── */
        inv.setItem(28, section("&d&lSPÉCIAUX"));
        set(29, d(Material.ENDER_PEARL, "&5Perle d'Ender ×4", 80, 40), 80, 40, new ItemStack(Material.ENDER_PEARL, 4));
        set(30, d(Material.GOLDEN_APPLE, "&6Pomme Dorée", 150, 0), 150, 0, new ItemStack(Material.GOLDEN_APPLE));
        set(31, d(Material.ENCHANTED_GOLDEN_APPLE, "&dPomme Enchantée", 800, 0), 800, 0, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        set(32, d(Material.TNT, "&cTNT ×2", 75, 0), 75, 0, new ItemStack(Material.TNT, 2));
        set(33, d(Material.SHULKER_BOX, "&dBoîte Shulker", 300, 0, "&7Stockage transportable"), 300, 0, new ItemStack(Material.SHULKER_BOX));

        /* ── Armures ── */
        inv.setItem(37, section("&d&lARMURES"));
        set(38, d(Material.IRON_CHESTPLATE, "&fKit Fer", 150, 0), 150, 0,
            new ItemStack(Material.IRON_HELMET), new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS));
        set(39, d(Material.DIAMOND_CHESTPLATE, "&bKit Diamant", 400, 0), 400, 0,
            new ItemStack(Material.DIAMOND_HELMET), new ItemStack(Material.DIAMOND_CHESTPLATE),
            new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_BOOTS));
        set(40, d(Material.ELYTRA, "&dÉlytre", 600, 0, "&7Pour explorer!"), 600, 0, new ItemStack(Material.ELYTRA));
        set(41, d(Material.TOTEM_OF_UNDYING, "&dTotem de Résurrection", 500, 0), 500, 0, new ItemStack(Material.TOTEM_OF_UNDYING));
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
