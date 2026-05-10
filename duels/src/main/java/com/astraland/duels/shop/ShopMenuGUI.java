package com.astraland.duels.shop;

import com.astraland.duels.managers.EconomyManager;
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

    private static final int[] CAT_SLOTS = {20, 22, 24};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();
    private final List<ShopCategoryData> categories;
    private final EconomyManager eco;

    public ShopMenuGUI(Player player, EconomyManager eco) {
        this.eco = eco;
        this.categories = buildCategories();
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &d&lSHOP &8&l✦ &7Duels"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.MAGENTA_STAINED_GLASS_PANE, " ");
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
            new ShopCategoryData("combat", "&d&l⚔ Combat Rapide", Material.GOLDEN_APPLE,
                "&7Consommables et potions pour le duel", List.of(
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 200, 80, new String[]{"&7Régén II + Absorption en combat"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 800, 200, new String[]{"&7Puissance maximale !"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&6Soupe de Champignons ×8", Material.MUSHROOM_STEW, 60, 30, new String[]{"&7Kit Soup UHC — soins rapides"}, it(Material.MUSHROOM_STEW, 8)),
                new ShopItemData("&cPotion de Force II", Material.POTION, 150, 50, new String[]{"&7Force II 2min"}, strengthPot()),
                new ShopItemData("&aPotion de Vitesse II", Material.POTION, 120, 40, new String[]{"&7Vitesse II 2min"}, speedPot()),
                new ShopItemData("&cPotion Dégâts II (Splash)", Material.SPLASH_POTION, 100, 30, new String[]{"&7Dégâts instantanés II"}, damagePot()),
                new ShopItemData("&fSteak ×16", Material.COOKED_BEEF, 40, 20, new String[]{"&7Sustain pendant le duel"}, it(Material.COOKED_BEEF, 16))
            )),
            new ShopCategoryData("equipements", "&5&l🗡 Équipements", Material.DIAMOND_SWORD,
                "&7Armes, armures et équipement de combat", List.of(
                new ShopItemData("&fÉpée de Fer Tr.II", Material.IRON_SWORD, 80, 30, new String[]{"&7Tranchant II"}, e(it(Material.IRON_SWORD, 1), Enchantment.SHARPNESS, 2)),
                new ShopItemData("&bÉpée Diamant Tr.III", Material.DIAMOND_SWORD, 250, 100, new String[]{"&7Tranchant III"}, e(it(Material.DIAMOND_SWORD, 1), Enchantment.SHARPNESS, 3)),
                new ShopItemData("&aArc Puissance II + Recul II", Material.BOW, 150, 60, new String[]{"&7Puissance II, Recul II"}, e(e(it(Material.BOW, 1), Enchantment.POWER, 2), Enchantment.PUNCH, 2)),
                new ShopItemData("&eFlèches ×32", Material.ARROW, 30, 15, new String[]{"&7Munitions pour arc"}, it(Material.ARROW, 32)),
                new ShopItemData("&fCasque Diamant Prot.II", Material.DIAMOND_HELMET, 120, 55, new String[]{"&7Protection II"}, e(it(Material.DIAMOND_HELMET, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fPlastron Diamant Prot.II", Material.DIAMOND_CHESTPLATE, 160, 75, new String[]{"&7Protection II"}, e(it(Material.DIAMOND_CHESTPLATE, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fJambières Diamant Prot.II", Material.DIAMOND_LEGGINGS, 140, 65, new String[]{"&7Protection II"}, e(it(Material.DIAMOND_LEGGINGS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fBottes Diamant Prot.II", Material.DIAMOND_BOOTS, 120, 55, new String[]{"&7Protection II"}, e(it(Material.DIAMOND_BOOTS, 1), Enchantment.PROTECTION, 2))
            )),
            new ShopCategoryData("utilitaires", "&5&l🎒 Utilitaires", Material.ENDER_PEARL,
                "&7Téléportation, survie et objets tactiques", List.of(
                new ShopItemData("&5Perle d'Ender ×4", Material.ENDER_PEARL, 80, 40, new String[]{"&7Téléportation tactique"}, it(Material.ENDER_PEARL, 4)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 700, 280, new String[]{"&7Une chance supplémentaire"}, it(Material.TOTEM_OF_UNDYING, 1)),
                new ShopItemData("&fSteak ×8", Material.COOKED_BEEF, 25, 12, new String[]{"&7Nourriture de combat"}, it(Material.COOKED_BEEF, 8)),
                new ShopItemData("&aFlacon d'XP ×8", Material.EXPERIENCE_BOTTLE, 80, 40, new String[]{"&7Répare l'équipement"}, it(Material.EXPERIENCE_BOTTLE, 8)),
                new ShopItemData("&7Silex et Acier", Material.FLINT_AND_STEEL, 30, 15, new String[]{"&7Pyromanie tactique"}, it(Material.FLINT_AND_STEEL, 1)),
                new ShopItemData("&5Perle d'Ender ×1", Material.ENDER_PEARL, 25, 12, new String[]{"&7À l'unité"}, it(Material.ENDER_PEARL, 1))
            ))
        );
    }

    private static ItemStack strengthPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 2400, 1), true); pot.setItemMeta(m); return pot;
    }
    private static ItemStack speedPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 1), true); pot.setItemMeta(m); return pot;
    }
    private static ItemStack damagePot() {
        ItemStack pot = new ItemStack(Material.SPLASH_POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true); pot.setItemMeta(m); return pot;
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&d&l✦ Shop Duels ✦"));
        m.setLore(List.of(c("&7Prépare-toi avant ton duel !"), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
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
