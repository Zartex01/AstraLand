package com.astraland.uhc.shop;

import com.astraland.uhc.managers.EconomyManager;
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
            ChatColor.translateAlternateColorCodes('&', "&6&l☠ &eShop UHC &6&l☠"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.ORANGE_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.GOLDEN_APPLE, "&e&lUHC Shop", "&7Survie hardcore — aucune regen naturelle"));

        /* ── Soins ── */
        inv.setItem(10, section("&6&lSOINS"));
        set(11, d(Material.GOLDEN_APPLE, "&6Pomme Dorée", 200, "&7Régénération II 5s + Absorption"), 200, new ItemStack(Material.GOLDEN_APPLE));
        set(12, d(Material.ENCHANTED_GOLDEN_APPLE, "&dPomme Enchantée (Notch)", 800, "&7Régén IV + Absorption IV + Résistance I"), 800, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        set(13, regenDisplay(150), 150, regenPot());
        set(14, healDisplay(120), 120, healPot());
        set(15, d(Material.POTION, "&bPotion de Soin II", 100, "&7Soin instantané II"), 100, healPot2());

        /* ── Armures ── */
        inv.setItem(19, section("&6&lARMURES"));
        set(20, d(Material.IRON_CHESTPLATE, "&fKit Fer Prot.II", 200, "&7Protection II"), 200,
            ench(new ItemStack(Material.IRON_HELMET), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.IRON_CHESTPLATE), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.IRON_LEGGINGS), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION, 2));
        set(21, d(Material.DIAMOND_CHESTPLATE, "&bKit Diamant Prot.III", 500, "&7Protection III"), 500,
            ench(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION, 3));

        /* ── Armes ── */
        inv.setItem(28, section("&6&lARMES"));
        set(29, d(Material.DIAMOND_SWORD, "&bÉpée Diamant Sharp.III", 300, "&7Tranchant III"), 300,
            ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 3));
        set(30, d(Material.BOW, "&aArc Puissance III", 200, "&7Puissance III + Infini"), 200,
            ench(ench(new ItemStack(Material.BOW), Enchantment.POWER, 3), Enchantment.INFINITY, 1));
        set(31, d(Material.ARROW, "&eFlèche", 20), 20, new ItemStack(Material.ARROW, 1));

        /* ── Utilitaires ── */
        inv.setItem(37, section("&6&lUTILITAIRES"));
        set(38, d(Material.ENDER_PEARL, "&5Perle d'Ender ×4", 100), 100, new ItemStack(Material.ENDER_PEARL, 4));
        set(39, d(Material.COOKED_BEEF, "&eSteak ×16", 50), 50, new ItemStack(Material.COOKED_BEEF, 16));
        set(40, d(Material.EXPERIENCE_BOTTLE, "&aFlacon d'XP ×8", 80, "&7Répare l'équipement"), 80, new ItemStack(Material.EXPERIENCE_BOTTLE, 8));
        set(41, d(Material.TOTEM_OF_UNDYING, "&dTotem de Résurrection", 1000, "&7UHC — précieux !"), 1000, new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private ItemStack regenPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 1), true); pot.setItemMeta(m); return pot;
    }
    private ItemStack regenDisplay(int price) { return d(Material.POTION, "&dPotion de Régén II", price, "&7Régén II 1min"); }
    private ItemStack healPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0), true); pot.setItemMeta(m); return pot;
    }
    private ItemStack healDisplay(int price) { return d(Material.POTION, "&bPotion de Soin I", price, "&7Soin instantané I"); }
    private ItemStack healPot2() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1), true); pot.setItemMeta(m); return pot;
    }

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
