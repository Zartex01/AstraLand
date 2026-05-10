package com.astraland.uhc.shop;

import com.astraland.uhc.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenuGUI implements InventoryHolder {

    private static final int[] CAT_SLOTS = {20, 22, 29, 31};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();
    private final List<ShopCategoryData> categories;
    private final EconomyManager eco;

    public ShopMenuGUI(Player player, EconomyManager eco) {
        this.eco = eco;
        this.categories = buildCategories();
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &e&lSHOP &8&l✦ &7UHC"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.ORANGE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);
        inv.setItem(4, makeHeader());
        inv.setItem(49, makeBalance(eco.getBalance(player.getUniqueId())));
        for (int i = 0; i < categories.size() && i < CAT_SLOTS.length; i++) {
            ShopCategoryData cat = categories.get(i);
            inv.setItem(CAT_SLOTS[i], makeCategoryBtn(cat));
            categorySlots.put(CAT_SLOTS[i], cat);
        }
    }

    private List<ShopCategoryData> buildCategories() {
        return List.of(
            new ShopCategoryData("soins", "&6&l💊 Soins & Survie", Material.GOLDEN_APPLE,
                "&7UHC — aucune régénération naturelle !", List.of(
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 200, 80, new String[]{"&7Régén II 5s + Absorption I"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée (Notch)", Material.ENCHANTED_GOLDEN_APPLE, 800, 250, new String[]{"&7Régén IV + Absorption IV + Résis. I"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&dPotion de Régén II", Material.POTION, 150, 50, new String[]{"&7Régénération II 1min"}, regenPot()),
                new ShopItemData("&bPotion de Soin I", Material.POTION, 120, 40, new String[]{"&7Soin instantané I"}, healPot()),
                new ShopItemData("&bPotion de Soin II", Material.POTION, 180, 60, new String[]{"&7Soin instantané II"}, healPot2()),
                new ShopItemData("&fSteak ×16", Material.COOKED_BEEF, 50, 25, new String[]{"&7Regain de vie via la faim"}, it(Material.COOKED_BEEF, 16)),
                new ShopItemData("&ePain ×16", Material.BREAD, 35, 17, new String[]{"&7Nourriture de base"}, it(Material.BREAD, 16))
            )),
            new ShopCategoryData("armures", "&f&l🛡 Armures", Material.DIAMOND_CHESTPLATE,
                "&7Pièces d'armure individuelles", List.of(
                new ShopItemData("&fCasque de Fer Prot.II", Material.IRON_HELMET, 55, 25, new String[]{"&7Protection II"}, e(it(Material.IRON_HELMET, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fPlastron de Fer Prot.II", Material.IRON_CHESTPLATE, 75, 35, new String[]{"&7Protection II"}, e(it(Material.IRON_CHESTPLATE, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fJambières de Fer Prot.II", Material.IRON_LEGGINGS, 65, 30, new String[]{"&7Protection II"}, e(it(Material.IRON_LEGGINGS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fBottes de Fer Prot.II", Material.IRON_BOOTS, 55, 25, new String[]{"&7Protection II"}, e(it(Material.IRON_BOOTS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&bCasque Diamant Prot.III", Material.DIAMOND_HELMET, 150, 70, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_HELMET, 1), Enchantment.PROTECTION, 3)),
                new ShopItemData("&bPlastron Diamant Prot.III", Material.DIAMOND_CHESTPLATE, 200, 90, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_CHESTPLATE, 1), Enchantment.PROTECTION, 3)),
                new ShopItemData("&bJambières Diamant Prot.III", Material.DIAMOND_LEGGINGS, 170, 80, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_LEGGINGS, 1), Enchantment.PROTECTION, 3)),
                new ShopItemData("&bBottes Diamant Prot.III", Material.DIAMOND_BOOTS, 150, 70, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_BOOTS, 1), Enchantment.PROTECTION, 3))
            )),
            new ShopCategoryData("armes", "&c&l⚔ Armes", Material.DIAMOND_SWORD,
                "&7Épées, arcs et munitions UHC", List.of(
                new ShopItemData("&bÉpée Diamant Tr.III", Material.DIAMOND_SWORD, 300, 120, new String[]{"&7Tranchant III"}, e(it(Material.DIAMOND_SWORD, 1), Enchantment.SHARPNESS, 3)),
                new ShopItemData("&aArc Puissance III + Infini", Material.BOW, 200, 80, new String[]{"&7Puissance III + Infini"}, e(e(it(Material.BOW, 1), Enchantment.POWER, 3), Enchantment.INFINITY, 1)),
                new ShopItemData("&eFlèche", Material.ARROW, 20, 10, new String[]{"&7Munition pour arc"}, it(Material.ARROW, 1)),
                new ShopItemData("&eFlèches ×32", Material.ARROW, 30, 15, new String[]{"&7Pack de flèches"}, it(Material.ARROW, 32)),
                new ShopItemData("&fHache de Fer", Material.IRON_AXE, 50, 20, new String[]{"&7Alternative efficace"}, it(Material.IRON_AXE, 1))
            )),
            new ShopCategoryData("utilitaires", "&e&l🎒 Utilitaires", Material.ENDER_PEARL,
                "&7Objets de survie et de déplacement", List.of(
                new ShopItemData("&5Perle d'Ender ×4", Material.ENDER_PEARL, 100, 50, new String[]{"&7Téléportation tactique"}, it(Material.ENDER_PEARL, 4)),
                new ShopItemData("&aFlacon d'XP ×8", Material.EXPERIENCE_BOTTLE, 80, 40, new String[]{"&7Répare l'équipement"}, it(Material.EXPERIENCE_BOTTLE, 8)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 1000, 400, new String[]{"&7UHC — précieux !"}, it(Material.TOTEM_OF_UNDYING, 1)),
                new ShopItemData("&7Silex et Acier", Material.FLINT_AND_STEEL, 30, 15, new String[]{"&7Utile contre les araignées"}, it(Material.FLINT_AND_STEEL, 1)),
                new ShopItemData("&fCobblestone ×64", Material.COBBLESTONE, 40, 20, new String[]{"&7Construction d'abri"}, it(Material.COBBLESTONE, 64)),
                new ShopItemData("&8Obsidienne ×4", Material.OBSIDIAN, 80, 40, new String[]{"&7Abri résistant"}, it(Material.OBSIDIAN, 4))
            ))
        );
    }

    private static ItemStack regenPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 1200, 1), true); pot.setItemMeta(m); return pot;
    }
    private static ItemStack healPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0), true); pot.setItemMeta(m); return pot;
    }
    private static ItemStack healPot2() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1), true); pot.setItemMeta(m); return pot;
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&e&l✦ Shop UHC ✦"));
        m.setLore(List.of(c("&7Survie hardcore — aucune regen naturelle !"), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); item.setItemMeta(m); return item;
    }

    private ItemStack makeBalance(int balance) {
        ItemStack item = new ItemStack(Material.SUNFLOWER); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&e&lTon Solde : &6" + balance + " $"));
        m.setLore(List.of(c("&7/balance &8— &7voir ton solde"), c("&7/pay &8— &7payer un joueur")));
        item.setItemMeta(m); return item;
    }

    private ItemStack makeCategoryBtn(ShopCategoryData cat) {
        ItemStack item = new ItemStack(cat.icon()); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c(cat.displayName()));
        m.setLore(List.of(c(cat.description()), c(""), c("&f" + cat.items().size() + " &7articles"), c(""), c("&7▶ &fClic pour ouvrir")));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        item.setItemMeta(m); return item;
    }

    public ShopCategoryData getCategoryAt(int slot) { return categorySlots.get(slot); }
    public EconomyManager getEconomyManager() { return eco; }
    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }

    private ItemStack glass(Material mat, String name) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(name); i.setItemMeta(m); return i; }
    private static ItemStack it(Material mat, int amt) { return new ItemStack(mat, amt); }
    private static ItemStack e(ItemStack item, Enchantment ench, int lvl) { item.addUnsafeEnchantment(ench, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
