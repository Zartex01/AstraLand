package com.astraland.bedwars.shop;

import com.astraland.bedwars.managers.EconomyManager;
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
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &c&lSHOP &8&l✦ &7Bedwars"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.RED_STAINED_GLASS_PANE, " ");
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
            new ShopCategoryData("armures", "&f&l🛡 Armures & Défense", Material.DIAMOND_CHESTPLATE,
                "&7Pièces d'armure et équipement défensif", List.of(
                new ShopItemData("&7Casque Maille", Material.CHAINMAIL_HELMET, 25, 12, new String[]{"&7Protection légère"}, it(Material.CHAINMAIL_HELMET, 1)),
                new ShopItemData("&7Plastron Maille", Material.CHAINMAIL_CHESTPLATE, 35, 17, new String[]{"&7Protection légère"}, it(Material.CHAINMAIL_CHESTPLATE, 1)),
                new ShopItemData("&7Jambières Maille", Material.CHAINMAIL_LEGGINGS, 30, 15, new String[]{"&7Protection légère"}, it(Material.CHAINMAIL_LEGGINGS, 1)),
                new ShopItemData("&7Bottes Maille", Material.CHAINMAIL_BOOTS, 25, 12, new String[]{"&7Protection légère"}, it(Material.CHAINMAIL_BOOTS, 1)),
                new ShopItemData("&fCasque de Fer", Material.IRON_HELMET, 45, 22, new String[]{"&7Protection II"}, e(it(Material.IRON_HELMET, 1), Enchantment.PROTECTION, 1)),
                new ShopItemData("&fPlastron de Fer", Material.IRON_CHESTPLATE, 60, 30, new String[]{"&7Protection II"}, e(it(Material.IRON_CHESTPLATE, 1), Enchantment.PROTECTION, 1)),
                new ShopItemData("&fJambières de Fer", Material.IRON_LEGGINGS, 50, 25, new String[]{"&7Protection II"}, e(it(Material.IRON_LEGGINGS, 1), Enchantment.PROTECTION, 1)),
                new ShopItemData("&fBottes de Fer", Material.IRON_BOOTS, 45, 22, new String[]{"&7Protection II"}, e(it(Material.IRON_BOOTS, 1), Enchantment.PROTECTION, 1)),
                new ShopItemData("&bCasque Diamant Prot.II", Material.DIAMOND_HELMET, 90, 45, new String[]{"&7Protection II solide"}, e(it(Material.DIAMOND_HELMET, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&bPlastron Diamant Prot.II", Material.DIAMOND_CHESTPLATE, 130, 65, new String[]{"&7Protection II solide"}, e(it(Material.DIAMOND_CHESTPLATE, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&bJambières Diamant Prot.II", Material.DIAMOND_LEGGINGS, 110, 55, new String[]{"&7Protection II solide"}, e(it(Material.DIAMOND_LEGGINGS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&bBottes Diamant Prot.II", Material.DIAMOND_BOOTS, 90, 45, new String[]{"&7Protection II solide"}, e(it(Material.DIAMOND_BOOTS, 1), Enchantment.PROTECTION, 2)),
                new ShopItemData("&8Obsidienne ×4", Material.OBSIDIAN, 80, 40, new String[]{"&7Protège votre lit !"}, it(Material.OBSIDIAN, 4))
            )),
            new ShopCategoryData("armes", "&c&l⚔ Armes", Material.DIAMOND_SWORD,
                "&7Épées, arcs et munitions de combat", List.of(
                new ShopItemData("&fÉpée de Fer Tr.I", Material.IRON_SWORD, 60, 25, new String[]{"&7Tranchant I"}, e(it(Material.IRON_SWORD, 1), Enchantment.SHARPNESS, 1)),
                new ShopItemData("&bÉpée Diamant Tr.II", Material.DIAMOND_SWORD, 200, 80, new String[]{"&7Tranchant II"}, e(it(Material.DIAMOND_SWORD, 1), Enchantment.SHARPNESS, 2)),
                new ShopItemData("&aArc Puissance II", Material.BOW, 100, 40, new String[]{"&7Puissance II"}, e(it(Material.BOW, 1), Enchantment.POWER, 2)),
                new ShopItemData("&eFlèches ×32", Material.ARROW, 30, 15, new String[]{"&7Munitions pour votre arc"}, it(Material.ARROW, 32)),
                new ShopItemData("&fHache de Fer", Material.IRON_AXE, 50, 20, new String[]{"&7Alternative à l'épée"}, it(Material.IRON_AXE, 1))
            )),
            new ShopCategoryData("utilitaires", "&e&l🔧 Utilitaires", Material.TNT,
                "&7Construction, destruction et déplacement", List.of(
                new ShopItemData("&cTNT", Material.TNT, 80, 35, new String[]{"&7Détruisez les lits !"}, it(Material.TNT, 1)),
                new ShopItemData("&6Boule de Feu", Material.FIRE_CHARGE, 60, 25, new String[]{"&7Tire une boule de feu"}, it(Material.FIRE_CHARGE, 1)),
                new ShopItemData("&5Perle d'Ender ×2", Material.ENDER_PEARL, 100, 50, new String[]{"&7Téléportation rapide"}, it(Material.ENDER_PEARL, 2)),
                new ShopItemData("&6Planches ×32", Material.OAK_PLANKS, 25, 12, new String[]{"&7Construction de ponts"}, it(Material.OAK_PLANKS, 32)),
                new ShopItemData("&fLaine Blanche ×16", Material.WHITE_WOOL, 20, 10, new String[]{"&7Pont rapide"}, it(Material.WHITE_WOOL, 16)),
                new ShopItemData("&7Echelle ×16", Material.LADDER, 20, 10, new String[]{"&7Grimper les tours"}, it(Material.LADDER, 16)),
                new ShopItemData("&7Cobblestone ×32", Material.COBBLESTONE, 30, 15, new String[]{"&7Défense solide"}, it(Material.COBBLESTONE, 32))
            )),
            new ShopCategoryData("consommables", "&6&l🍎 Consommables", Material.GOLDEN_APPLE,
                "&7Soins, potions et équipements défensifs", List.of(
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 120, 50, new String[]{"&7Régénération II + Absorption"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 600, 150, new String[]{"&7Puissance maximale !"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&eFlèche ×1", Material.ARROW, 5, 2, new String[]{"&7Achetez-en à l'unité"}, it(Material.ARROW, 1)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 800, 300, new String[]{"&71 résurrection garantie"}, it(Material.TOTEM_OF_UNDYING, 1)),
                new ShopItemData("&aPotion de Vitesse II", Material.POTION, 70, 25, new String[]{"&7Vitesse II 2min"}, speedPot()),
                new ShopItemData("&cPotion Dégâts (Splash)", Material.SPLASH_POTION, 80, 30, new String[]{"&7Dégâts instantanés II"}, damagePot()),
                new ShopItemData("&fSteak ×8", Material.COOKED_BEEF, 25, 12, new String[]{"&7Regain de faim"}, it(Material.COOKED_BEEF, 8))
            ))
        );
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
        ItemStack item = new ItemStack(Material.RED_BED); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&c&l✦ Shop Bedwars ✦"));
        m.setLore(List.of(c("&7Équipe-toi pour protéger ton lit !"), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
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
