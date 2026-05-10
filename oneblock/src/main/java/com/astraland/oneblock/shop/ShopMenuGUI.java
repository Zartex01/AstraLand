package com.astraland.oneblock.shop;

import com.astraland.oneblock.managers.EconomyManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenuGUI implements InventoryHolder {

    private static final int[] CAT_SLOTS = {20, 22, 24, 29, 31};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();
    private final List<ShopCategoryData> categories;
    private final EconomyManager eco;

    public ShopMenuGUI(Player player, EconomyManager eco) {
        this.eco = eco;
        this.categories = buildCategories();
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &d&lSHOP &8&l✦ &7OneBlock"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.PURPLE_STAINED_GLASS_PANE, " ");
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
            new ShopCategoryData("outils", "&7&l⛏ Outils", Material.DIAMOND_PICKAXE,
                "&7Pioches, haches et outils améliorés", List.of(
                new ShopItemData("&fPioche de Fer Eff.I", Material.IRON_PICKAXE, 60, 25, new String[]{"&7Eff. I, Sans Brisure I"}, e(e(it(Material.IRON_PICKAXE, 1), Enchantment.EFFICIENCY, 1), Enchantment.UNBREAKING, 1)),
                new ShopItemData("&bPioche Diamant Eff.III", Material.DIAMOND_PICKAXE, 200, 80, new String[]{"&7Eff. III, Sans Brisure II"}, e(e(it(Material.DIAMOND_PICKAXE, 1), Enchantment.EFFICIENCY, 3), Enchantment.UNBREAKING, 2)),
                new ShopItemData("&5Pioche Nétherite Eff.V", Material.NETHERITE_PICKAXE, 500, 200, new String[]{"&7Eff. V, Fortune III"}, e(e(it(Material.NETHERITE_PICKAXE, 1), Enchantment.EFFICIENCY, 5), Enchantment.FORTUNE, 3)),
                new ShopItemData("&bHache Diamant Tr.II", Material.DIAMOND_AXE, 180, 70, new String[]{"&7Tranchant II"}, e(it(Material.DIAMOND_AXE, 1), Enchantment.SHARPNESS, 2)),
                new ShopItemData("&fHoue de Fer Eff.I", Material.IRON_HOE, 30, 15, new String[]{"&7Efficacité I"}, e(it(Material.IRON_HOE, 1), Enchantment.EFFICIENCY, 1)),
                new ShopItemData("&bCanne à Pêche LdM II", Material.FISHING_ROD, 40, 15, new String[]{"&7Chance de la Mer II"}, e(it(Material.FISHING_ROD, 1), Enchantment.LUCK_OF_THE_SEA, 2))
            )),
            new ShopCategoryData("ressources", "&b&l💎 Ressources", Material.DIAMOND,
                "&7Minerais et matériaux issus du bloc unique", List.of(
                new ShopItemData("&7Lingot de Fer ×16", Material.IRON_INGOT, 50, 25, new String[]{"&7Craft de base"}, it(Material.IRON_INGOT, 16)),
                new ShopItemData("&6Lingot d'Or ×8", Material.GOLD_INGOT, 60, 30, new String[]{"&7Or précieux"}, it(Material.GOLD_INGOT, 8)),
                new ShopItemData("&bDiamant ×3", Material.DIAMOND, 100, 50, new String[]{"&7Pierre précieuse"}, it(Material.DIAMOND, 3)),
                new ShopItemData("&aÉmeraude ×3", Material.EMERALD, 90, 45, new String[]{"&7Commerce"}, it(Material.EMERALD, 3)),
                new ShopItemData("&5Nétherite", Material.NETHERITE_INGOT, 400, 200, new String[]{"&7Matériau ultime"}, it(Material.NETHERITE_INGOT, 1)),
                new ShopItemData("&9Lapis-Lazuli ×16", Material.LAPIS_LAZULI, 20, 10, new String[]{"&7Enchantement"}, it(Material.LAPIS_LAZULI, 16)),
                new ShopItemData("&cRedstone ×16", Material.REDSTONE, 15, 7, new String[]{"&7Mécanismes"}, it(Material.REDSTONE, 16))
            )),
            new ShopCategoryData("armures", "&f&l🛡 Armures", Material.DIAMOND_CHESTPLATE,
                "&7Pièces d'armure — fer, diamant et plus", List.of(
                new ShopItemData("&fCasque de Fer", Material.IRON_HELMET, 40, 20, new String[]{"&7Protection de base"}, it(Material.IRON_HELMET, 1)),
                new ShopItemData("&fPlastron de Fer", Material.IRON_CHESTPLATE, 60, 30, new String[]{"&7Protection de base"}, it(Material.IRON_CHESTPLATE, 1)),
                new ShopItemData("&fJambières de Fer", Material.IRON_LEGGINGS, 50, 25, new String[]{"&7Protection de base"}, it(Material.IRON_LEGGINGS, 1)),
                new ShopItemData("&fBottes de Fer", Material.IRON_BOOTS, 40, 20, new String[]{"&7Protection de base"}, it(Material.IRON_BOOTS, 1)),
                new ShopItemData("&bCasque Diamant", Material.DIAMOND_HELMET, 120, 60, new String[]{"&7Protection solide"}, it(Material.DIAMOND_HELMET, 1)),
                new ShopItemData("&bPlastron Diamant", Material.DIAMOND_CHESTPLATE, 180, 90, new String[]{"&7Protection solide"}, it(Material.DIAMOND_CHESTPLATE, 1)),
                new ShopItemData("&bJambières Diamant", Material.DIAMOND_LEGGINGS, 150, 75, new String[]{"&7Protection solide"}, it(Material.DIAMOND_LEGGINGS, 1)),
                new ShopItemData("&bBottes Diamant", Material.DIAMOND_BOOTS, 120, 60, new String[]{"&7Protection solide"}, it(Material.DIAMOND_BOOTS, 1))
            )),
            new ShopCategoryData("blocs", "&e&l🧱 Blocs & Spéciaux", Material.CHEST,
                "&7Blocs de construction et articles spéciaux", List.of(
                new ShopItemData("&7Cobblestone ×64", Material.COBBLESTONE, 40, 20, new String[]{"&7Construction"}, it(Material.COBBLESTONE, 64)),
                new ShopItemData("&6Terre ×64", Material.DIRT, 30, 15, new String[]{"&7Cultures"}, it(Material.DIRT, 64)),
                new ShopItemData("&6Bûche ×32", Material.OAK_LOG, 50, 25, new String[]{"&7Construction et craft"}, it(Material.OAK_LOG, 32)),
                new ShopItemData("&8Obsidienne ×4", Material.OBSIDIAN, 80, 40, new String[]{"&7Résistante"}, it(Material.OBSIDIAN, 4)),
                new ShopItemData("&5Perle d'Ender ×4", Material.ENDER_PEARL, 80, 40, new String[]{"&7Téléportation"}, it(Material.ENDER_PEARL, 4)),
                new ShopItemData("&cTNT ×2", Material.TNT, 75, 35, new String[]{"&7Explosif"}, it(Material.TNT, 2)),
                new ShopItemData("&dBoîte Shulker", Material.SHULKER_BOX, 300, 150, new String[]{"&7Stockage transportable"}, it(Material.SHULKER_BOX, 1))
            )),
            new ShopCategoryData("consommables", "&6&l🍎 Consommables", Material.GOLDEN_APPLE,
                "&7Pommes, totems et autres consommables précieux", List.of(
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 150, 60, new String[]{"&7Régén II + Absorption"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 800, 200, new String[]{"&7Puissance maximale"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 500, 250, new String[]{"&7Survie assurée"}, it(Material.TOTEM_OF_UNDYING, 1)),
                new ShopItemData("&dÉlytre", Material.ELYTRA, 600, 300, new String[]{"&7Volez !"}, it(Material.ELYTRA, 1)),
                new ShopItemData("&aFlacon d'XP ×8", Material.EXPERIENCE_BOTTLE, 80, 40, new String[]{"&7Expérience instantanée"}, it(Material.EXPERIENCE_BOTTLE, 8))
            ))
        );
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&d&l✦ Shop OneBlock ✦"));
        m.setLore(List.of(c("&7Bienvenue dans le shop OneBlock !"), c("&7Clique sur une catégorie pour explorer."), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
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
