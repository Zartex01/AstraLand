package com.astraland.skywars.shop;

import com.astraland.skywars.managers.EconomyManager;
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
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &b&lSHOP &8&l✦ &7Skywars"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.CYAN_STAINED_GLASS_PANE, " ");
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
            new ShopCategoryData("armures", "&f&l🛡 Armures", Material.DIAMOND_CHESTPLATE,
                "&7Armures légères jusqu'aux armures lourdes", List.of(
                new ShopItemData("&6Casque Cuir", Material.LEATHER_HELMET, 15, 7, new String[]{"&7Protection légère"}, it(Material.LEATHER_HELMET, 1)),
                new ShopItemData("&6Plastron Cuir", Material.LEATHER_CHESTPLATE, 20, 10, new String[]{"&7Protection légère"}, it(Material.LEATHER_CHESTPLATE, 1)),
                new ShopItemData("&6Jambières Cuir", Material.LEATHER_LEGGINGS, 18, 9, new String[]{"&7Protection légère"}, it(Material.LEATHER_LEGGINGS, 1)),
                new ShopItemData("&6Bottes Cuir", Material.LEATHER_BOOTS, 15, 7, new String[]{"&7Protection légère"}, it(Material.LEATHER_BOOTS, 1)),
                new ShopItemData("&fCasque de Fer Prot.II", Material.IRON_HELMET, 45, 22, new String[]{"&7Protection II"}, e(it(Material.IRON_HELMET, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fPlastron de Fer Prot.II", Material.IRON_CHESTPLATE, 60, 30, new String[]{"&7Protection II"}, e(it(Material.IRON_CHESTPLATE, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fJambières de Fer Prot.II", Material.IRON_LEGGINGS, 50, 25, new String[]{"&7Protection II"}, e(it(Material.IRON_LEGGINGS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&fBottes de Fer Prot.II", Material.IRON_BOOTS, 45, 22, new String[]{"&7Protection II"}, e(it(Material.IRON_BOOTS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&bCasque Diamant Prot.III", Material.DIAMOND_HELMET, 110, 55, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_HELMET, 1), Enchantment.PROTECTION, 3)),
                new ShopItemData("&bPlastron Diamant Prot.III", Material.DIAMOND_CHESTPLATE, 160, 80, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_CHESTPLATE, 1), Enchantment.PROTECTION, 3)),
                new ShopItemData("&bJambières Diamant Prot.III", Material.DIAMOND_LEGGINGS, 140, 70, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_LEGGINGS, 1), Enchantment.PROTECTION, 3)),
                new ShopItemData("&bBottes Diamant Prot.III", Material.DIAMOND_BOOTS, 110, 55, new String[]{"&7Protection III"}, e(it(Material.DIAMOND_BOOTS, 1), Enchantment.PROTECTION, 3))
            )),
            new ShopCategoryData("armes", "&3&l⚔ Armes", Material.DIAMOND_SWORD,
                "&7Épées, arcs et munitions pour dominer le ciel", List.of(
                new ShopItemData("&fÉpée de Fer Tr.II", Material.IRON_SWORD, 70, 28, new String[]{"&7Tranchant II"}, e(it(Material.IRON_SWORD, 1), Enchantment.SHARPNESS, 2)),
                new ShopItemData("&bÉpée Diamant Tr.III", Material.DIAMOND_SWORD, 220, 88, new String[]{"&7Tranchant III"}, e(it(Material.DIAMOND_SWORD, 1), Enchantment.SHARPNESS, 3)),
                new ShopItemData("&aArc Puissance III", Material.BOW, 130, 52, new String[]{"&7Puissance III"}, e(it(Material.BOW, 1), Enchantment.POWER, 3)),
                new ShopItemData("&eFlèches ×32", Material.ARROW, 30, 15, new String[]{"&7Munitions pour arc"}, it(Material.ARROW, 32)),
                new ShopItemData("&eFlèche ×1", Material.ARROW, 5, 2, new String[]{"&7Unité"}, it(Material.ARROW, 1)),
                new ShopItemData("&fHache de Fer Tr.I", Material.IRON_AXE, 55, 22, new String[]{"&7Tranchant I"}, e(it(Material.IRON_AXE, 1), Enchantment.SHARPNESS, 1))
            )),
            new ShopCategoryData("utilitaires", "&b&l✈ Utilitaires", Material.ELYTRA,
                "&7Déplacement, construction et mobilité aérienne", List.of(
                new ShopItemData("&5Perle d'Ender ×2", Material.ENDER_PEARL, 80, 40, new String[]{"&7Téléportation rapide"}, it(Material.ENDER_PEARL, 2)),
                new ShopItemData("&6Planches ×16", Material.OAK_PLANKS, 25, 12, new String[]{"&7Construire des ponts"}, it(Material.OAK_PLANKS, 16)),
                new ShopItemData("&fLaine ×16", Material.WHITE_WOOL, 20, 10, new String[]{"&7Pont léger"}, it(Material.WHITE_WOOL, 16)),
                new ShopItemData("&aPotion de Vitesse II", Material.POTION, 60, 20, new String[]{"&7Vitesse II 2min"}, speedPot()),
                new ShopItemData("&dÉlytre", Material.ELYTRA, 800, 400, new String[]{"&7Volez dans le ciel !"}, it(Material.ELYTRA, 1)),
                new ShopItemData("&7Cobblestone ×32", Material.COBBLESTONE, 30, 15, new String[]{"&7Construction défensive"}, it(Material.COBBLESTONE, 32))
            )),
            new ShopCategoryData("ravitaillement", "&e&l🍎 Ravitaillement", Material.COOKED_BEEF,
                "&7Nourriture, soins et équipements spéciaux", List.of(
                new ShopItemData("&fSteak ×8", Material.COOKED_BEEF, 30, 15, new String[]{"&7Regain de faim rapide"}, it(Material.COOKED_BEEF, 8)),
                new ShopItemData("&fSteak ×32", Material.COOKED_BEEF, 100, 50, new String[]{"&7Grande quantité"}, it(Material.COOKED_BEEF, 32)),
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 150, 60, new String[]{"&7Régénération + Absorption"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 700, 200, new String[]{"&7Puissance maximale !"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 900, 350, new String[]{"&7Dernière chance !"}, it(Material.TOTEM_OF_UNDYING, 1))
            ))
        );
    }

    private static ItemStack speedPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 1), true); pot.setItemMeta(m); return pot;
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.ELYTRA); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&3&l✦ Shop Skywars ✦"));
        m.setLore(List.of(c("&7Domine le ciel ! Équipe-toi bien."), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
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
