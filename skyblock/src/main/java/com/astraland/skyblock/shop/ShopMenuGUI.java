package com.astraland.skyblock.shop;

import com.astraland.skyblock.managers.EconomyManager;
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

    private static final int[] CAT_SLOTS = {20, 22, 24, 29, 31, 33};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();
    private final List<ShopCategoryData> categories;
    private final EconomyManager eco;

    public ShopMenuGUI(Player player, EconomyManager eco) {
        this.eco = eco;
        this.categories = buildCategories();
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &a&lSHOP &8&l✦ &7Skyblock"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.LIME_STAINED_GLASS_PANE, " ");
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
            new ShopCategoryData("cultures", "&a&l🌾 Graines & Cultures", Material.WHEAT,
                "&7Semences, récoltes et engrais", List.of(
                new ShopItemData("&aGraines de Blé ×16", Material.WHEAT_SEEDS, 5, 2, new String[]{"&7Cultivez du blé"}, it(Material.WHEAT_SEEDS, 16)),
                new ShopItemData("&6Carottes ×8", Material.CARROT, 8, 4, new String[]{"&7Récoltez des carottes"}, it(Material.CARROT, 8)),
                new ShopItemData("&ePommes de Terre ×8", Material.POTATO, 8, 4, new String[]{"&7Récoltez des pommes de terre"}, it(Material.POTATO, 8)),
                new ShopItemData("&aSem. de Melon ×8", Material.MELON_SEEDS, 15, 7, new String[]{"&7Poussent sur des blocs d'herbe"}, it(Material.MELON_SEEDS, 8)),
                new ShopItemData("&eSem. de Citrouille ×8", Material.PUMPKIN_SEEDS, 15, 7, new String[]{"&7Décoration et utilité"}, it(Material.PUMPKIN_SEEDS, 8)),
                new ShopItemData("&aCanne à Sucre ×16", Material.SUGAR_CANE, 10, 5, new String[]{"&7Besoin d'eau à proximité"}, it(Material.SUGAR_CANE, 16)),
                new ShopItemData("&aCacao ×8", Material.COCOA_BEANS, 10, 5, new String[]{"&7Pousse sur les bûches de jungle"}, it(Material.COCOA_BEANS, 8)),
                new ShopItemData("&cGraines de Betterave ×16", Material.BEETROOT_SEEDS, 8, 4, new String[]{"&7Potager coloré"}, it(Material.BEETROOT_SEEDS, 16)),
                new ShopItemData("&fFarine d'Os ×16", Material.BONE_MEAL, 25, 12, new String[]{"&7Accélère toutes les cultures"}, it(Material.BONE_MEAL, 16))
            )),
            new ShopCategoryData("outils", "&7&l⛏ Outils", Material.DIAMOND_PICKAXE,
                "&7Pioches, haches, houes et cannes à pêche", List.of(
                new ShopItemData("&fHoue de Fer", Material.IRON_HOE, 30, 15, new String[]{"&7Efficacité I"}, e(it(Material.IRON_HOE, 1), Enchantment.EFFICIENCY, 1)),
                new ShopItemData("&fPelle de Fer", Material.IRON_SHOVEL, 30, 15, new String[]{"&7Outil polyvalent"}, it(Material.IRON_SHOVEL, 1)),
                new ShopItemData("&fPioche de Fer Eff.I", Material.IRON_PICKAXE, 60, 25, new String[]{"&7Efficacité I"}, e(it(Material.IRON_PICKAXE, 1), Enchantment.EFFICIENCY, 1)),
                new ShopItemData("&fHache de Fer", Material.IRON_AXE, 50, 20, new String[]{"&7Bucheronnage efficace"}, it(Material.IRON_AXE, 1)),
                new ShopItemData("&bCanne à Pêche LdM II", Material.FISHING_ROD, 40, 15, new String[]{"&7Chance de la Mer II"}, e(it(Material.FISHING_ROD, 1), Enchantment.LUCK_OF_THE_SEA, 2)),
                new ShopItemData("&bPioche Diamant Eff.III", Material.DIAMOND_PICKAXE, 200, 80, new String[]{"&7Eff. III, Sans Brisure II"}, e(e(it(Material.DIAMOND_PICKAXE, 1), Enchantment.EFFICIENCY, 3), Enchantment.UNBREAKING, 2)),
                new ShopItemData("&bHache Diamant Tr.II", Material.DIAMOND_AXE, 180, 70, new String[]{"&7Tranchant II"}, e(it(Material.DIAMOND_AXE, 1), Enchantment.SHARPNESS, 2)),
                new ShopItemData("&5Pioche Nétherite Eff.V", Material.NETHERITE_PICKAXE, 500, 200, new String[]{"&7Eff. V, Fortune III"}, e(e(it(Material.NETHERITE_PICKAXE, 1), Enchantment.EFFICIENCY, 5), Enchantment.FORTUNE, 3))
            )),
            new ShopCategoryData("blocs", "&7&l🪨 Blocs", Material.COBBLESTONE,
                "&7Matériaux de construction pour votre île", List.of(
                new ShopItemData("&7Cobblestone ×64", Material.COBBLESTONE, 40, 20, new String[]{"&7Base de toute construction"}, it(Material.COBBLESTONE, 64)),
                new ShopItemData("&6Terre ×64", Material.DIRT, 30, 15, new String[]{"&7Idéale pour les cultures"}, it(Material.DIRT, 64)),
                new ShopItemData("&eSable ×64", Material.SAND, 35, 17, new String[]{"&7Fabrication du verre"}, it(Material.SAND, 64)),
                new ShopItemData("&7Gravier ×64", Material.GRAVEL, 30, 15, new String[]{"&7Chance de silex"}, it(Material.GRAVEL, 64)),
                new ShopItemData("&6Bûche de Chêne ×32", Material.OAK_LOG, 50, 25, new String[]{"&7Construction et craft"}, it(Material.OAK_LOG, 32)),
                new ShopItemData("&7Pierre ×64", Material.STONE, 45, 22, new String[]{"&7Plus solide que la cobblestone"}, it(Material.STONE, 64)),
                new ShopItemData("&fVerre ×32", Material.GLASS, 35, 17, new String[]{"&7Transparent et décoratif"}, it(Material.GLASS, 32)),
                new ShopItemData("&8Obsidienne ×4", Material.OBSIDIAN, 80, 40, new String[]{"&7Très résistante"}, it(Material.OBSIDIAN, 4))
            )),
            new ShopCategoryData("ressources", "&b&l💎 Ressources", Material.DIAMOND,
                "&7Minerais et matériaux précieux", List.of(
                new ShopItemData("&7Lingot de Fer ×8", Material.IRON_INGOT, 50, 25, new String[]{"&7Craft d'outils et d'armures"}, it(Material.IRON_INGOT, 8)),
                new ShopItemData("&6Lingot d'Or ×4", Material.GOLD_INGOT, 60, 30, new String[]{"&7Or précieux"}, it(Material.GOLD_INGOT, 4)),
                new ShopItemData("&bDiamant", Material.DIAMOND, 100, 50, new String[]{"&7Pierre précieuse"}, it(Material.DIAMOND, 1)),
                new ShopItemData("&aÉmeraude", Material.EMERALD, 90, 45, new String[]{"&7Commerce avec les villageois"}, it(Material.EMERALD, 1)),
                new ShopItemData("&5Nétherite", Material.NETHERITE_INGOT, 400, 200, new String[]{"&7Matériau ultime"}, it(Material.NETHERITE_INGOT, 1)),
                new ShopItemData("&9Lapis-Lazuli ×16", Material.LAPIS_LAZULI, 20, 10, new String[]{"&7Teinture et enchantement"}, it(Material.LAPIS_LAZULI, 16)),
                new ShopItemData("&cPoudre de Redstone ×16", Material.REDSTONE, 15, 7, new String[]{"&7Mécanismes et craft"}, it(Material.REDSTONE, 16)),
                new ShopItemData("&fQuartz ×16", Material.QUARTZ, 20, 10, new String[]{"&7Décoration et craft"}, it(Material.QUARTZ, 16))
            )),
            new ShopCategoryData("armures", "&f&l🛡 Armures", Material.IRON_CHESTPLATE,
                "&7Pièces d'armure individuelles — toutes vendables", List.of(
                new ShopItemData("&fCasque de Fer", Material.IRON_HELMET, 40, 20, new String[]{"&7Protection de base"}, it(Material.IRON_HELMET, 1)),
                new ShopItemData("&fPlastron de Fer", Material.IRON_CHESTPLATE, 60, 30, new String[]{"&7Protection de base"}, it(Material.IRON_CHESTPLATE, 1)),
                new ShopItemData("&fJambières de Fer", Material.IRON_LEGGINGS, 50, 25, new String[]{"&7Protection de base"}, it(Material.IRON_LEGGINGS, 1)),
                new ShopItemData("&fBottes de Fer", Material.IRON_BOOTS, 40, 20, new String[]{"&7Protection de base"}, it(Material.IRON_BOOTS, 1)),
                new ShopItemData("&bCasque Diamant", Material.DIAMOND_HELMET, 120, 60, new String[]{"&7Protection solide"}, it(Material.DIAMOND_HELMET, 1)),
                new ShopItemData("&bPlastron Diamant", Material.DIAMOND_CHESTPLATE, 180, 90, new String[]{"&7Protection solide"}, it(Material.DIAMOND_CHESTPLATE, 1)),
                new ShopItemData("&bJambières Diamant", Material.DIAMOND_LEGGINGS, 150, 75, new String[]{"&7Protection solide"}, it(Material.DIAMOND_LEGGINGS, 1)),
                new ShopItemData("&bBottes Diamant", Material.DIAMOND_BOOTS, 120, 60, new String[]{"&7Protection solide"}, it(Material.DIAMOND_BOOTS, 1)),
                new ShopItemData("&dÉlytre", Material.ELYTRA, 600, 300, new String[]{"&7Volez au-dessus de votre île !"}, it(Material.ELYTRA, 1))
            )),
            new ShopCategoryData("speciaux", "&e&l⭐ Spéciaux", Material.NETHER_STAR,
                "&7Articles rares et très utiles", List.of(
                new ShopItemData("&5Perle d'Ender ×4", Material.ENDER_PEARL, 80, 40, new String[]{"&7Téléportation instantanée"}, it(Material.ENDER_PEARL, 4)),
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 150, 60, new String[]{"&7Régénération II + Absorption"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 800, 200, new String[]{"&7Régén IV + Absorption IV"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&cTNT ×2", Material.TNT, 75, 35, new String[]{"&7Explosif puissant"}, it(Material.TNT, 2)),
                new ShopItemData("&dBoîte Shulker", Material.SHULKER_BOX, 300, 150, new String[]{"&7Stockage transportable"}, it(Material.SHULKER_BOX, 1)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 500, 250, new String[]{"&7Survie garantie !"}, it(Material.TOTEM_OF_UNDYING, 1))
            ))
        );
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&a&l✦ Shop Skyblock ✦"));
        m.setLore(List.of(c("&7Bienvenue dans le shop de ton île !"), c("&7Clique sur une catégorie pour explorer."), c(""), c("&a▶ Clic gauche &f: Acheter un item"), c("&6▶ Clic droit &f: Vendre un item"), c(""), c("&eGagne de l'argent en développant ton île.")));
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
