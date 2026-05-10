package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.managers.EconomyManager;
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
            ChatColor.translateAlternateColorCodes('&', "&4&l⚔ &cShop PvP/Factions &4&l⚔"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.RED_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }

        ItemStack sep = border(Material.BLACK_STAINED_GLASS_PANE);
        for (int s : new int[]{17, 26, 35}) inv.setItem(s, sep);

        inv.setItem(4, header(Material.DIAMOND_SWORD, "&c&lPvP / Factions", "&7Achète des équipements"));

        /* ── Armures ── */
        inv.setItem(10, section("&c&lARMURES"));
        set(11, display(Material.IRON_CHESTPLATE, "&fKit Fer", 150,
            "&7Casque + Plastron + Jambières + Bottes",
            "&7+ Épée de Fer"),
            150, kitFer());

        set(12, display(Material.GOLDEN_CHESTPLATE, "&6Kit Or", 120,
            "&7Armure complète en Or + Épée",
            "&7Boost de chance inclus"),
            120, kitOr());

        set(13, display(Material.DIAMOND_CHESTPLATE, "&bKit Diamant", 400,
            "&7Armure complète Diamant Prot II",
            "&7+ Épée Diamant Tranchant II"),
            400, kitDiam());

        set(14, display(Material.NETHERITE_CHESTPLATE, "&5Kit Nétherite", 900,
            "&7Armure complète Nétherite Prot III",
            "&7+ Épée Tranchant III"),
            900, kitNetherite());

        /* ── Armes ── */
        inv.setItem(19, section("&c&lARMES"));
        ItemStack iSword = ench(new ItemStack(Material.IRON_SWORD), Enchantment.SHARPNESS, 1);
        set(20, display(Material.IRON_SWORD, "&fÉpée de Fer", 50, "&7Tranchant I"), 50, iSword);

        ItemStack dSword = ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 2);
        set(21, display(Material.DIAMOND_SWORD, "&bÉpée Diamant", 200, "&7Tranchant II"), 200, dSword);

        ItemStack bow = ench(new ItemStack(Material.BOW), Enchantment.POWER, 2);
        set(22, display(Material.BOW, "&aArc", 80, "&7Puissance II"), 80, bow);

        set(23, display(Material.ARROW, "&eFlèches ×64", 30, "&764 flèches"), 30, new ItemStack(Material.ARROW, 64));

        ItemStack xbow = ench(new ItemStack(Material.CROSSBOW), Enchantment.QUICK_CHARGE, 3);
        set(24, display(Material.CROSSBOW, "&cArbalète", 150, "&7Chargement Rapide III"), 150, xbow);

        /* ── Consommables ── */
        inv.setItem(28, section("&c&lCONSOMMATION"));
        set(29, display(Material.GOLDEN_APPLE, "&6Pomme Dorée", 150, "&7Soins instantanés"), 150, new ItemStack(Material.GOLDEN_APPLE, 1));
        set(30, display(Material.ENCHANTED_GOLDEN_APPLE, "&dPomme Enchantée", 800, "&7Régénération + Résistance"), 800, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
        set(31, display(Material.COOKED_BEEF, "&eSteak ×16", 30, "&716 steaks cuits"), 30, new ItemStack(Material.COOKED_BEEF, 16));
        set(32, display(Material.ENDER_PEARL, "&bEnder Pearl ×4", 100, "&74 perles d'ender"), 100, new ItemStack(Material.ENDER_PEARL, 4));
        set(33, display(Material.TNT, "&cTNT", 100, "&71 TNT"), 100, new ItemStack(Material.TNT, 1));

        /* ── Potions ── */
        inv.setItem(37, section("&c&lPOTIONS"));
        set(38, potionDisplay("&aPotion de Vitesse", 60, "Vitesse II 3min"), 60, speedPot());
        set(39, potionDisplay("&cPotion de Force", 80, "Force I 3min"), 80, strengthPot());
        set(40, potionDisplay("&bPotion de Soin", 40, "Soin instantané II"), 40, healPot());
        set(41, potionDisplay("&ePotion de Sauts", 60, "Sauts II 3min"), 60, jumpPot());
        set(42, potionDisplay("&dTotem de Résurrection", 500, "Protège de la mort une fois"), 500, new ItemStack(Material.TOTEM_OF_UNDYING));
    }

    private ItemStack[] kitFer() {
        return new ItemStack[]{new ItemStack(Material.IRON_HELMET), new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS),
            ench(new ItemStack(Material.IRON_SWORD), Enchantment.SHARPNESS, 1)};
    }
    private ItemStack[] kitOr() {
        return new ItemStack[]{ench(new ItemStack(Material.GOLDEN_HELMET), Enchantment.PROTECTION, 1),
            ench(new ItemStack(Material.GOLDEN_CHESTPLATE), Enchantment.PROTECTION, 1),
            ench(new ItemStack(Material.GOLDEN_LEGGINGS), Enchantment.PROTECTION, 1),
            ench(new ItemStack(Material.GOLDEN_BOOTS), Enchantment.PROTECTION, 1),
            new ItemStack(Material.GOLDEN_SWORD)};
    }
    private ItemStack[] kitDiam() {
        return new ItemStack[]{ench(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 2)};
    }
    private ItemStack[] kitNetherite() {
        return new ItemStack[]{ench(new ItemStack(Material.NETHERITE_HELMET), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.NETHERITE_CHESTPLATE), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.NETHERITE_LEGGINGS), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.NETHERITE_BOOTS), Enchantment.PROTECTION, 3),
            ench(new ItemStack(Material.NETHERITE_SWORD), Enchantment.SHARPNESS, 3)};
    }

    private ItemStack speedPot() {
        org.bukkit.inventory.ItemStack pot = new org.bukkit.inventory.ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 3600, 1), true);
        pot.setItemMeta(m); return pot;
    }
    private ItemStack strengthPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 3600, 0), true);
        pot.setItemMeta(m); return pot;
    }
    private ItemStack healPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1), true);
        pot.setItemMeta(m); return pot;
    }
    private ItemStack jumpPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3600, 1), true);
        pot.setItemMeta(m); return pot;
    }

    private void set(int slot, ItemStack display, int price, ItemStack... reward) {
        inv.setItem(slot, display);
        entries.put(slot, new ShopEntry(price, reward));
    }

    public void handleClick(InventoryClickEvent e, EconomyManager eco) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ShopEntry entry = entries.get(e.getSlot());
        if (entry == null) return;
        if (!eco.removeBalance(player.getUniqueId(), entry.price())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! &7Il te faut &e" + entry.price() + " $&7."));
            return;
        }
        for (ItemStack item : entry.reward()) {
            if (item != null) player.getInventory().addItem(item.clone());
        }
        player.sendMessage(c("&a✔ Achat effectué &7pour &6" + entry.price() + " $&7."));
    }

    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }

    private ItemStack display(Material mat, String name, int price, String... desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(c(name));
        List<String> lore = new ArrayList<>(Arrays.stream(desc).map(this::c).toList());
        lore.add(""); lore.add(c("&6Prix : &e" + price + " $")); lore.add(c("&7▶ &fClic pour acheter"));
        meta.setLore(lore); meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta); return item;
    }
    private ItemStack potionDisplay(String name, int price, String effect) {
        return display(Material.POTION, name, price, "&7Effet : &b" + effect);
    }
    private ItemStack section(String name) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = item.getItemMeta(); m.setDisplayName(c(name)); item.setItemMeta(m); return item;
    }
    private ItemStack header(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta(); m.setDisplayName(c(name));
        m.setLore(List.of(c(desc))); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(m); return item;
    }
    private ItemStack border(Material mat) {
        ItemStack item = new ItemStack(mat); ItemMeta m = item.getItemMeta();
        m.setDisplayName(" "); item.setItemMeta(m); return item;
    }
    private ItemStack ench(ItemStack item, Enchantment e, int lvl) {
        item.addUnsafeEnchantment(e, lvl); return item;
    }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
