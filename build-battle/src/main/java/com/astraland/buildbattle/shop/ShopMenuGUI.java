package com.astraland.buildbattle.shop;

import com.astraland.buildbattle.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private static final int[] CAT_SLOTS = {20, 22, 29, 31};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();
    private final List<ShopCategoryData> categories;
    private final EconomyManager eco;

    public ShopMenuGUI(Player player, EconomyManager eco) {
        this.eco = eco;
        this.categories = buildCategories();
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &6&lSHOP &8&l✦ &7Build Battle"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.YELLOW_STAINED_GLASS_PANE, " ");
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
            new ShopCategoryData("laine", "&6&l🎨 Laine & Couleurs", Material.WHITE_WOOL,
                "&7Toutes les couleurs de laine — idéales pour construire", List.of(
                new ShopItemData("&fLaine Blanche ×64", Material.WHITE_WOOL, 20, 10, new String[]{"&7Couleur neutre"}, it(Material.WHITE_WOOL, 64)),
                new ShopItemData("&8Laine Grise ×64", Material.GRAY_WOOL, 20, 10, new String[]{"&7Tons foncés"}, it(Material.GRAY_WOOL, 64)),
                new ShopItemData("&7Laine Gris Clair ×64", Material.LIGHT_GRAY_WOOL, 20, 10, new String[]{"&7Tons clairs"}, it(Material.LIGHT_GRAY_WOOL, 64)),
                new ShopItemData("&0Laine Noire ×64", Material.BLACK_WOOL, 20, 10, new String[]{"&7Contraste maximum"}, it(Material.BLACK_WOOL, 64)),
                new ShopItemData("&cLaine Rouge ×64", Material.RED_WOOL, 20, 10, new String[]{"&7Rouge vif"}, it(Material.RED_WOOL, 64)),
                new ShopItemData("&6Laine Orange ×64", Material.ORANGE_WOOL, 20, 10, new String[]{"&7Orange chaleureux"}, it(Material.ORANGE_WOOL, 64)),
                new ShopItemData("&eLaine Jaune ×64", Material.YELLOW_WOOL, 20, 10, new String[]{"&7Jaune lumineux"}, it(Material.YELLOW_WOOL, 64)),
                new ShopItemData("&aLaine Verte ×64", Material.GREEN_WOOL, 20, 10, new String[]{"&7Vert foncé"}, it(Material.GREEN_WOOL, 64)),
                new ShopItemData("&2Laine Vert Clair ×64", Material.LIME_WOOL, 20, 10, new String[]{"&7Vert clair"}, it(Material.LIME_WOOL, 64)),
                new ShopItemData("&9Laine Bleue ×64", Material.BLUE_WOOL, 20, 10, new String[]{"&7Bleu profond"}, it(Material.BLUE_WOOL, 64)),
                new ShopItemData("&bLaine Bleu Clair ×64", Material.LIGHT_BLUE_WOOL, 20, 10, new String[]{"&7Bleu ciel"}, it(Material.LIGHT_BLUE_WOOL, 64)),
                new ShopItemData("&3Laine Cyan ×64", Material.CYAN_WOOL, 20, 10, new String[]{"&7Cyan"}, it(Material.CYAN_WOOL, 64)),
                new ShopItemData("&5Laine Violette ×64", Material.PURPLE_WOOL, 20, 10, new String[]{"&7Violet"}, it(Material.PURPLE_WOOL, 64)),
                new ShopItemData("&dLaine Magenta ×64", Material.MAGENTA_WOOL, 20, 10, new String[]{"&7Magenta"}, it(Material.MAGENTA_WOOL, 64)),
                new ShopItemData("&dLaine Rose ×64", Material.PINK_WOOL, 20, 10, new String[]{"&7Rose"}, it(Material.PINK_WOOL, 64)),
                new ShopItemData("&6Laine Marron ×64", Material.BROWN_WOOL, 20, 10, new String[]{"&7Marron naturel"}, it(Material.BROWN_WOOL, 64))
            )),
            new ShopCategoryData("beton", "&f&l🏗 Terracotta & Béton", Material.WHITE_CONCRETE,
                "&7Matériaux solides et colorés pour les bâtisseurs", List.of(
                new ShopItemData("&fTerracotta Blanche ×64", Material.WHITE_TERRACOTTA, 25, 12, new String[]{"&7Ton neutre mat"}, it(Material.WHITE_TERRACOTTA, 64)),
                new ShopItemData("&6Terracotta Orange ×64", Material.ORANGE_TERRACOTTA, 25, 12, new String[]{"&7Chaud et naturel"}, it(Material.ORANGE_TERRACOTTA, 64)),
                new ShopItemData("&cTerracotta Rouge ×64", Material.RED_TERRACOTTA, 25, 12, new String[]{"&7Rouge mat"}, it(Material.RED_TERRACOTTA, 64)),
                new ShopItemData("&fBéton Blanc ×64", Material.WHITE_CONCRETE, 30, 15, new String[]{"&7Blanc pur brillant"}, it(Material.WHITE_CONCRETE, 64)),
                new ShopItemData("&6Béton Orange ×64", Material.ORANGE_CONCRETE, 30, 15, new String[]{"&7Orange brillant"}, it(Material.ORANGE_CONCRETE, 64)),
                new ShopItemData("&eBéton Jaune ×64", Material.YELLOW_CONCRETE, 30, 15, new String[]{"&7Jaune vif"}, it(Material.YELLOW_CONCRETE, 64)),
                new ShopItemData("&aBéton Vert Clair ×64", Material.LIME_CONCRETE, 30, 15, new String[]{"&7Vert lumineux"}, it(Material.LIME_CONCRETE, 64)),
                new ShopItemData("&bBéton Cyan ×64", Material.CYAN_CONCRETE, 30, 15, new String[]{"&7Cyan brillant"}, it(Material.CYAN_CONCRETE, 64)),
                new ShopItemData("&9Béton Bleu ×64", Material.BLUE_CONCRETE, 30, 15, new String[]{"&7Bleu profond"}, it(Material.BLUE_CONCRETE, 64)),
                new ShopItemData("&5Béton Violet ×64", Material.PURPLE_CONCRETE, 30, 15, new String[]{"&7Violet royal"}, it(Material.PURPLE_CONCRETE, 64)),
                new ShopItemData("&dBéton Magenta ×64", Material.MAGENTA_CONCRETE, 30, 15, new String[]{"&7Magenta vif"}, it(Material.MAGENTA_CONCRETE, 64)),
                new ShopItemData("&cBéton Rouge ×64", Material.RED_CONCRETE, 30, 15, new String[]{"&7Rouge intense"}, it(Material.RED_CONCRETE, 64)),
                new ShopItemData("&8Béton Gris ×64", Material.GRAY_CONCRETE, 30, 15, new String[]{"&7Gris foncé"}, it(Material.GRAY_CONCRETE, 64)),
                new ShopItemData("&0Béton Noir ×64", Material.BLACK_CONCRETE, 30, 15, new String[]{"&7Noir profond"}, it(Material.BLACK_CONCRETE, 64))
            )),
            new ShopCategoryData("deco", "&e&l🌸 Décorations", Material.OAK_TRAPDOOR,
                "&7Blocs décoratifs pour embellir vos constructions", List.of(
                new ShopItemData("&6Trappe Chêne ×16", Material.OAK_TRAPDOOR, 25, 12, new String[]{"&7Porte décorative"}, it(Material.OAK_TRAPDOOR, 16)),
                new ShopItemData("&6Escaliers Chêne ×32", Material.OAK_STAIRS, 30, 15, new String[]{"&7Escaliers naturels"}, it(Material.OAK_STAIRS, 32)),
                new ShopItemData("&fVitre ×32", Material.GLASS_PANE, 25, 12, new String[]{"&7Fenêtres transparentes"}, it(Material.GLASS_PANE, 32)),
                new ShopItemData("&7Verre ×32", Material.GLASS, 25, 12, new String[]{"&7Blocs de verre"}, it(Material.GLASS, 32)),
                new ShopItemData("&6Pot de Fleur ×8", Material.FLOWER_POT, 20, 10, new String[]{"&7Décoration florale"}, it(Material.FLOWER_POT, 8)),
                new ShopItemData("&eLanterne ×16", Material.LANTERN, 30, 15, new String[]{"&7Éclairage élégant"}, it(Material.LANTERN, 16)),
                new ShopItemData("&fDalle de Pierre ×32", Material.STONE_SLAB, 20, 10, new String[]{"&7Demi-blocs pratiques"}, it(Material.STONE_SLAB, 32)),
                new ShopItemData("&7Clôture ×16", Material.OAK_FENCE, 20, 10, new String[]{"&7Clôture naturelle"}, it(Material.OAK_FENCE, 16)),
                new ShopItemData("&fBlanc Vitré ×16", Material.WHITE_STAINED_GLASS_PANE, 25, 12, new String[]{"&7Vitre colorée"}, it(Material.WHITE_STAINED_GLASS_PANE, 16)),
                new ShopItemData("&9Bleu Vitré ×16", Material.BLUE_STAINED_GLASS_PANE, 25, 12, new String[]{"&7Vitre colorée"}, it(Material.BLUE_STAINED_GLASS_PANE, 16))
            )),
            new ShopCategoryData("nature", "&2&l🌿 Végétation & Lumière", Material.GLOWSTONE,
                "&7Nature et sources de lumière pour vos builds", List.of(
                new ShopItemData("&aFeuilles de Chêne ×32", Material.OAK_LEAVES, 15, 7, new String[]{"&7Végétation naturelle"}, it(Material.OAK_LEAVES, 32)),
                new ShopItemData("&aFeuilles de Bouleau ×32", Material.BIRCH_LEAVES, 15, 7, new String[]{"&7Couleur claire"}, it(Material.BIRCH_LEAVES, 32)),
                new ShopItemData("&aHerbe ×32", Material.GRASS_BLOCK, 15, 7, new String[]{"&7Bloc d'herbe naturel"}, it(Material.GRASS_BLOCK, 32)),
                new ShopItemData("&aTerre ×64", Material.DIRT, 10, 5, new String[]{"&7Base de terrain"}, it(Material.DIRT, 64)),
                new ShopItemData("&eGlowstone ×16", Material.GLOWSTONE, 30, 15, new String[]{"&7Lumière chaleureuse"}, it(Material.GLOWSTONE, 16)),
                new ShopItemData("&bLanterne Marine ×16", Material.SEA_LANTERN, 35, 17, new String[]{"&7Lumière aquatique"}, it(Material.SEA_LANTERN, 16)),
                new ShopItemData("&6Champignon Lumineux ×16", Material.SHROOMLIGHT, 40, 20, new String[]{"&7Lumière chaude orange"}, it(Material.SHROOMLIGHT, 16)),
                new ShopItemData("&eFanal ×8", Material.TORCH, 10, 5, new String[]{"&7Éclairage simple"}, it(Material.TORCH, 8)),
                new ShopItemData("&eFanal d'Âme ×8", Material.SOUL_TORCH, 15, 7, new String[]{"&7Lumière bleue mystérieuse"}, it(Material.SOUL_TORCH, 8))
            ))
        );
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.CRAFTING_TABLE); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&6&l✦ Shop Build Battle ✦"));
        m.setLore(List.of(c("&7Des blocs pour construire le chef-d'œuvre !"), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
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
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
