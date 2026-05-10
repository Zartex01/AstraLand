package com.astraland.skywars.shop;

import com.astraland.skywars.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI implements InventoryHolder {

    private final Inventory inv;
    private final Map<Integer, ShopEntry> entries = new HashMap<>();
    private record ShopEntry(int price, ItemStack[] reward) {}

    public ShopGUI() {
        inv = Bukkit.createInventory(this, 54,
            ChatColor.translateAlternateColorCodes('&', "&b&l✈ &3Shop Skywars &b&l✈"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.CYAN_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.ELYTRA, "&3&lSkywars Shop", "&7Domine le ciel !"));

        /* ── Armures ── */
        inv.setItem(10, section("&3&lARMURES"));
        set(11, d(Material.LEATHER_CHESTPLATE, "&6Kit Cuir", 40, "&7Protection légère"), 40,
            new ItemStack(Material.LEATHER_HELMET), new ItemStack(Material.LEATHER_CHESTPLATE),
            new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_BOOTS));
        set(12, d(Material.IRON_CHESTPLATE, "&fKit Fer", 130, "&7Protection II"), 130,
            ench(new ItemStack(Material.IRON_HELMET), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.IRON_CHESTPLATE), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.IRON_LEGGINGS), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION, 2));
        set(13, d(Material.DIAMOND_CHESTPLATE, "&bKit Diamant", 320, "&7Protection III"), 320,
            ench(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION, 3));

        /* ── Armes ── */
        inv.setItem(19, section("&3&lARMES"));
        set(20, d(Material.IRON_SWORD, "&fÉpée de Fer Sharp.II", 70, "&7Tranchant II"), 70,
            ench(new ItemStack(Material.IRON_SWORD), Enchantment.SHARPNESS, 2));
        set(21, d(Material.DIAMOND_SWORD, "&bÉpée Diamant Sharp.III", 220, "&7Tranchant III"), 220,
            ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 3));
        set(22, d(Material.BOW, "&aArc Puissance III", 130, "&7Puissance III"), 130,
            ench(new ItemStack(Material.BOW), Enchantment.POWER, 3));
        set(23, d(Material.ARROW, "&eFlèches ×32", 30), 30, new ItemStack(Material.ARROW, 32));

        /* ── Utilitaires ── */
        inv.setItem(28, section("&3&lUTILITAIRES"));
        set(29, d(Material.ENDER_PEARL, "&5Perle d'Ender ×2", 80), 80, new ItemStack(Material.ENDER_PEARL, 2));
        set(30, d(Material.GOLDEN_APPLE, "&6Pomme Dorée", 150), 150, new ItemStack(Material.GOLDEN_APPLE));
        set(31, speedDisplay(60), 60, speedPot());
        set(32, d(Material.OAK_PLANKS, "&6Planches ×16", 25, "&7Construire des ponts"), 25, new ItemStack(Material.OAK_PLANKS, 16));
        set(33, d(Material.ELYTRA, "&dÉlytre", 800, "&7Vole dans le ciel !"), 800, new ItemStack(Material.ELYTRA));

        /* ── Consommables ── */
        inv.setItem(37, section("&3&lCONSOMMATION"));
        set(38, d(Material.COOKED_BEEF, "&eSteak ×8", 30), 30, new ItemStack(Material.COOKED_BEEF, 8));
        set(39, d(Material.ENCHANTED_GOLDEN_APPLE, "&dPomme Enchantée", 700), 700, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        set(40, d(Material.TOTEM_OF_UNDYING, "&dTotem", 900), 900, new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private ItemStack speedPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 1), true); pot.setItemMeta(m); return pot;
    }
    private ItemStack speedDisplay(int price) { return d(Material.POTION, "&aPotion de Vitesse II", price, "&7Vitesse II 2min"); }

    private void set(int slot, ItemStack display, int price, ItemStack... reward) {
        inv.setItem(slot, display); entries.put(slot, new ShopEntry(price, reward));
    }
    public void handleClick(InventoryClickEvent e, EconomyManager eco) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ShopEntry entry = entries.get(e.getSlot());
        if (entry == null) return;
        if (!eco.removeBalance(player.getUniqueId(), entry.price())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + entry.price() + " $&c.")); return;
        }
        for (ItemStack item : entry.reward()) if (item != null) player.getInventory().addItem(item.clone());
        player.sendMessage(c("&a✔ Achat pour &6" + entry.price() + " $&a."));
    }
    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }
    private ItemStack d(Material mat, String name, int price, String... desc) {
        ItemStack item = new ItemStack(mat); ItemMeta meta = item.getItemMeta(); meta.setDisplayName(c(name));
        List<String> lore = new ArrayList<>(Arrays.stream(desc).map(this::c).toList());
        lore.add(""); lore.add(c("&6Prix : &e" + price + " $")); lore.add(c("&7▶ &fClic pour acheter"));
        meta.setLore(lore); meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); item.setItemMeta(meta); return item;
    }
    private ItemStack section(String name) { ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); i.setItemMeta(m); return i; }
    private ItemStack header(Material mat, String name, String desc) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); m.setLore(List.of(c(desc))); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); i.setItemMeta(m); return i; }
    private ItemStack border(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private ItemStack ench(ItemStack item, Enchantment e, int lvl) { item.addUnsafeEnchantment(e, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
