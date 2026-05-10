package com.astraland.pvpfactions.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public enum ShopCategory {

    BLOCS("&6&l🧱 Blocs", Material.BRICKS,
        "&7Blocs de construction et décoratifs"),

    MINERAIS("&b&l⛏ Minerais", Material.DIAMOND,
        "&7Lingots, minerais et ressources précieuses"),

    NOURRITURE("&e&l🍖 Nourriture", Material.COOKED_BEEF,
        "&7Aliments et pommes dorées"),

    POTIONS("&d&l🧪 Potions", Material.SPLASH_POTION,
        "&7Potions de combat et de survie"),

    UTILITAIRES("&a&l🔧 Utilitaires", Material.CHEST,
        "&7Perles, TNT, outils et divers"),

    ENCHANTEMENTS("&3&l✨ Enchantements", Material.ENCHANTED_BOOK,
        "&7Livres enchantés pour améliorer ton équipement");

    private final String displayName;
    private final Material icon;
    private final String description;

    ShopCategory(String displayName, Material icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon()      { return icon; }
    public String getDescription() { return description; }

    public List<ShopItem> getItems() {
        return switch (this) {
            case BLOCS        -> buildBlocs();
            case MINERAIS     -> buildMinerais();
            case NOURRITURE   -> buildNourriture();
            case POTIONS      -> buildPotions();
            case UTILITAIRES  -> buildUtilitaires();
            case ENCHANTEMENTS -> buildEnchantements();
        };
    }

    /* ──────────────────────────────── BLOCS ──────────────────────────────── */
    private static List<ShopItem> buildBlocs() {
        return List.of(
            item("&fBois de Chêne &7×64", Material.OAK_LOG, 20,
                new String[]{"&764 bûches de chêne"},
                new ItemStack(Material.OAK_LOG, 64)),
            item("&fBois de Bouleau &7×64", Material.BIRCH_LOG, 20,
                new String[]{"&764 bûches de bouleau"},
                new ItemStack(Material.BIRCH_LOG, 64)),
            item("&fPlanches de Chêne &7×64", Material.OAK_PLANKS, 25,
                new String[]{"&764 planches de chêne"},
                new ItemStack(Material.OAK_PLANKS, 64)),
            item("&7Cobblestone &7×64", Material.COBBLESTONE, 10,
                new String[]{"&764 blocs de cobblestone"},
                new ItemStack(Material.COBBLESTONE, 64)),
            item("&fPierre &7×64", Material.STONE, 15,
                new String[]{"&764 blocs de pierre"},
                new ItemStack(Material.STONE, 64)),
            item("&eSable &7×32", Material.SAND, 15,
                new String[]{"&732 blocs de sable"},
                new ItemStack(Material.SAND, 32)),
            item("&7Gravier &7×32", Material.GRAVEL, 15,
                new String[]{"&732 blocs de gravier"},
                new ItemStack(Material.GRAVEL, 32)),
            item("&6Terre &7×64", Material.DIRT, 8,
                new String[]{"&764 blocs de terre"},
                new ItemStack(Material.DIRT, 64)),
            item("&eGlowstone &7×16", Material.GLOWSTONE, 60,
                new String[]{"&716 blocs de glowstone"},
                new ItemStack(Material.GLOWSTONE, 16)),
            item("&fLaine Blanche &7×64", Material.WHITE_WOOL, 40,
                new String[]{"&764 blocs de laine blanche"},
                new ItemStack(Material.WHITE_WOOL, 64)),
            item("&bVerre &7×32", Material.GLASS, 20,
                new String[]{"&732 blocs de verre"},
                new ItemStack(Material.GLASS, 32)),
            item("&8Obsidienne &7×8", Material.OBSIDIAN, 50,
                new String[]{"&78 blocs d'obsidienne"},
                new ItemStack(Material.OBSIDIAN, 8)),
            item("&cTNT &7×4", Material.TNT, 80,
                new String[]{"&74 TNT"},
                new ItemStack(Material.TNT, 4)),
            item("&eTorche &7×64", Material.TORCH, 10,
                new String[]{"&764 torches"},
                new ItemStack(Material.TORCH, 64)),
            item("&6Coffre &7×8", Material.CHEST, 30,
                new String[]{"&78 coffres en bois"},
                new ItemStack(Material.CHEST, 8)),
            item("&cNetherack &7×64", Material.NETHERRACK, 10,
                new String[]{"&764 blocs de netherack"},
                new ItemStack(Material.NETHERRACK, 64)),
            item("&8Sable des Âmes &7×32", Material.SOUL_SAND, 20,
                new String[]{"&732 blocs de sable des âmes"},
                new ItemStack(Material.SOUL_SAND, 32)),
            item("&fQuartz &7×32", Material.QUARTZ_BLOCK, 40,
                new String[]{"&732 blocs de quartz"},
                new ItemStack(Material.QUARTZ_BLOCK, 32))
        );
    }

    /* ─────────────────────────────── MINERAIS ────────────────────────────── */
    private static List<ShopItem> buildMinerais() {
        return List.of(
            item("&8Charbon &7×64", Material.COAL, 15,
                new String[]{"&764 charbons"},
                new ItemStack(Material.COAL, 64)),
            item("&fLingots de Fer &7×16", Material.IRON_INGOT, 60,
                new String[]{"&716 lingots de fer"},
                new ItemStack(Material.IRON_INGOT, 16)),
            item("&fLingots de Fer &7×64", Material.IRON_INGOT, 220,
                new String[]{"&764 lingots de fer"},
                new ItemStack(Material.IRON_INGOT, 64)),
            item("&6Lingots d'Or &7×8", Material.GOLD_INGOT, 80,
                new String[]{"&78 lingots d'or"},
                new ItemStack(Material.GOLD_INGOT, 8)),
            item("&6Pépites d'Or &7×64", Material.GOLD_NUGGET, 40,
                new String[]{"&764 pépites d'or"},
                new ItemStack(Material.GOLD_NUGGET, 64)),
            item("&bDiamants &7×4", Material.DIAMOND, 200,
                new String[]{"&74 diamants"},
                new ItemStack(Material.DIAMOND, 4)),
            item("&bDiamants &7×16", Material.DIAMOND, 750,
                new String[]{"&716 diamants"},
                new ItemStack(Material.DIAMOND, 16)),
            item("&aLapis-Lazuli &7×32", Material.LAPIS_LAZULI, 30,
                new String[]{"&732 lapis-lazuli"},
                new ItemStack(Material.LAPIS_LAZULI, 32)),
            item("&cRedstone &7×32", Material.REDSTONE, 40,
                new String[]{"&732 redstone"},
                new ItemStack(Material.REDSTONE, 32)),
            item("&aÉmeraudes &7×8", Material.EMERALD, 120,
                new String[]{"&78 émeraudes"},
                new ItemStack(Material.EMERALD, 8)),
            item("&fQuartz du Nether &7×32", Material.QUARTZ, 25,
                new String[]{"&732 quartz du nether"},
                new ItemStack(Material.QUARTZ, 32)),
            item("&6Lingots de Cuivre &7×16", Material.COPPER_INGOT, 30,
                new String[]{"&716 lingots de cuivre"},
                new ItemStack(Material.COPPER_INGOT, 16)),
            item("&4Ancient Debris", Material.ANCIENT_DEBRIS, 400,
                new String[]{"&71 ancient debris", "&7Nécessaire pour le Nétherite !"},
                new ItemStack(Material.ANCIENT_DEBRIS, 1)),
            item("&5Netherite Scrap", Material.NETHERITE_SCRAP, 150,
                new String[]{"&71 netherite scrap"},
                new ItemStack(Material.NETHERITE_SCRAP, 1)),
            item("&5Lingot de Nétherite", Material.NETHERITE_INGOT, 600,
                new String[]{"&71 lingot de nétherite", "&7Le matériau le plus résistant !"},
                new ItemStack(Material.NETHERITE_INGOT, 1))
        );
    }

    /* ─────────────────────────────── NOURRITURE ──────────────────────────── */
    private static List<ShopItem> buildNourriture() {
        return List.of(
            item("&eSteak &7×16", Material.COOKED_BEEF, 30,
                new String[]{"&716 steaks cuits"},
                new ItemStack(Material.COOKED_BEEF, 16)),
            item("&ePain &7×32", Material.BREAD, 20,
                new String[]{"&732 pains"},
                new ItemStack(Material.BREAD, 32)),
            item("&ePorc Cuit &7×16", Material.COOKED_PORKCHOP, 25,
                new String[]{"&716 côtelettes cuites"},
                new ItemStack(Material.COOKED_PORKCHOP, 16)),
            item("&ePoulet Cuit &7×16", Material.COOKED_CHICKEN, 20,
                new String[]{"&716 poulets cuits"},
                new ItemStack(Material.COOKED_CHICKEN, 16)),
            item("&6Pomme Dorée", Material.GOLDEN_APPLE, 150,
                new String[]{"&7Absorption + Régénération II"},
                new ItemStack(Material.GOLDEN_APPLE, 1)),
            item("&6Pommes Dorées &7×4", Material.GOLDEN_APPLE, 500,
                new String[]{"&74 pommes dorées"},
                new ItemStack(Material.GOLDEN_APPLE, 4)),
            item("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 800,
                new String[]{"&7Régénération V + Résistance + Absorption"},
                new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1)),
            item("&aCarotte Dorée &7×8", Material.GOLDEN_CARROT, 60,
                new String[]{"&78 carottes dorées"},
                new ItemStack(Material.GOLDEN_CARROT, 8)),
            item("&bMelon &7×32", Material.MELON_SLICE, 15,
                new String[]{"&732 tranches de melon"},
                new ItemStack(Material.MELON_SLICE, 32)),
            item("&dGâteau", Material.CAKE, 50,
                new String[]{"&7Un délicieux gâteau (7 morceaux)"},
                new ItemStack(Material.CAKE, 1)),
            item("&5Fruit du Chorus &7×8", Material.CHORUS_FRUIT, 60,
                new String[]{"&78 fruits du chorus", "&7(téléportation aléatoire)"},
                new ItemStack(Material.CHORUS_FRUIT, 8))
        );
    }

    /* ─────────────────────────────── POTIONS ─────────────────────────────── */
    private static List<ShopItem> buildPotions() {
        return List.of(
            item("&aPotion de Vitesse II", Material.POTION, 60,
                new String[]{"&7Vitesse II - 3 minutes"},
                splashPotion(PotionEffectType.SPEED, 3600, 1)),
            item("&cPotion de Force I", Material.POTION, 80,
                new String[]{"&7Force I - 3 minutes"},
                splashPotion(PotionEffectType.STRENGTH, 3600, 0)),
            item("&4Potion de Force II", Material.POTION, 150,
                new String[]{"&7Force II - 1 minute 30"},
                splashPotion(PotionEffectType.STRENGTH, 1800, 1)),
            item("&bPotion de Soin II", Material.POTION, 40,
                new String[]{"&7Soin instantané II"},
                potion(PotionEffectType.INSTANT_HEALTH, 1, 1)),
            item("&5Potion de Régénération", Material.POTION, 100,
                new String[]{"&7Régénération II - 45 secondes"},
                potion(PotionEffectType.REGENERATION, 900, 1)),
            item("&7Potion de Résistance", Material.POTION, 120,
                new String[]{"&7Résistance I - 3 minutes"},
                potion(PotionEffectType.RESISTANCE, 3600, 0)),
            item("&fPotion d'Invisibilité", Material.POTION, 80,
                new String[]{"&7Invisibilité - 3 minutes"},
                potion(PotionEffectType.INVISIBILITY, 3600, 0)),
            item("&ePotion de Sauts II", Material.POTION, 60,
                new String[]{"&7Sauts II - 3 minutes"},
                potion(PotionEffectType.JUMP_BOOST, 3600, 1)),
            item("&2Splash Poison", Material.SPLASH_POTION, 50,
                new String[]{"&7Poison II - 45 secondes", "&7À lancer sur l'ennemi !"},
                splashPotion(PotionEffectType.POISON, 900, 1)),
            item("&8Splash Lenteur", Material.SPLASH_POTION, 50,
                new String[]{"&7Lenteur IV - 15 secondes", "&7À lancer sur l'ennemi !"},
                splashPotion(PotionEffectType.SLOWNESS, 300, 3)),
            item("&6Splash Faiblesse", Material.SPLASH_POTION, 70,
                new String[]{"&7Faiblesse I - 1 minute 30", "&7Réduit les dégâts ennemis !"},
                splashPotion(PotionEffectType.WEAKNESS, 1800, 0)),
            item("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 500,
                new String[]{"&7Te protège de la mort une fois !"},
                new ItemStack(Material.TOTEM_OF_UNDYING, 1))
        );
    }

    /* ────────────────────────────── UTILITAIRES ──────────────────────────── */
    private static List<ShopItem> buildUtilitaires() {
        return List.of(
            item("&bEnder Pearl &7×4", Material.ENDER_PEARL, 100,
                new String[]{"&74 perles d'ender"},
                new ItemStack(Material.ENDER_PEARL, 4)),
            item("&bEnder Pearl &7×16", Material.ENDER_PEARL, 350,
                new String[]{"&716 perles d'ender"},
                new ItemStack(Material.ENDER_PEARL, 16)),
            item("&fLaine &7×64", Material.WHITE_WOOL, 40,
                new String[]{"&764 blocs de laine blanche"},
                new ItemStack(Material.WHITE_WOOL, 64)),
            item("&aCorde &7×16", Material.STRING, 30,
                new String[]{"&716 fils de corde"},
                new ItemStack(Material.STRING, 16)),
            item("&bPioche Diamant &7[Eff III]", Material.DIAMOND_PICKAXE, 200,
                new String[]{"&7Efficacité III + Solidité II"},
                enchMulti(new ItemStack(Material.DIAMOND_PICKAXE),
                    Enchantment.EFFICIENCY, 3, Enchantment.UNBREAKING, 2)),
            item("&bPelle Diamant &7[Eff III]", Material.DIAMOND_SHOVEL, 150,
                new String[]{"&7Efficacité III"},
                ench(new ItemStack(Material.DIAMOND_SHOVEL), Enchantment.EFFICIENCY, 3)),
            item("&6Hache Dorée &7[Eff V]", Material.GOLDEN_AXE, 100,
                new String[]{"&7Efficacité V (pour couper vite)"},
                ench(new ItemStack(Material.GOLDEN_AXE), Enchantment.EFFICIENCY, 5)),
            item("&ePierre à Aiguiser", Material.GRINDSTONE, 30,
                new String[]{"&7Retire les enchantements"},
                new ItemStack(Material.GRINDSTONE, 1)),
            item("&7Boussole", Material.COMPASS, 20,
                new String[]{"&7Indique la direction du spawn"},
                new ItemStack(Material.COMPASS, 1)),
            item("&fPapier &7×16", Material.PAPER, 10,
                new String[]{"&716 feuilles de papier"},
                new ItemStack(Material.PAPER, 16)),
            item("&6Seau d'Eau", Material.WATER_BUCKET, 30,
                new String[]{"&7Un seau rempli d'eau"},
                new ItemStack(Material.WATER_BUCKET, 1)),
            item("&cSeau de Lave", Material.LAVA_BUCKET, 50,
                new String[]{"&7Un seau rempli de lave"},
                new ItemStack(Material.LAVA_BUCKET, 1))
        );
    }

    /* ──────────────────────────── ENCHANTEMENTS ──────────────────────────── */
    private static List<ShopItem> buildEnchantements() {
        return List.of(
            book("&cTranchant III",        200, Enchantment.SHARPNESS,            3),
            book("&7Protection III",       200, Enchantment.PROTECTION,           3),
            book("&bProtection Proj. III", 180, Enchantment.PROJECTILE_PROTECTION,3),
            book("&5Solidité III",         180, Enchantment.UNBREAKING,           3),
            book("&aInfini",               150, Enchantment.INFINITY,             1),
            book("&eChute de Plumes IV",   120, Enchantment.FEATHER_FALLING,      4),
            book("&6Efficacité IV",        160, Enchantment.EFFICIENCY,           4),
            book("&dFortune III",          250, Enchantment.FORTUNE,              3),
            book("&bSoie Touchante",       200, Enchantment.SILK_TOUCH,           1),
            book("&aRéparation",           300, Enchantment.MENDING,              1),
            book("&cAspect de Feu II",     150, Enchantment.FIRE_ASPECT,          2),
            book("&ePillage III",          180, Enchantment.LOOTING,              3)
        );
    }

    /* ─────────────────────────── HELPERS ─────────────────────────────────── */
    private static ShopItem item(String name, Material icon, int price, String[] lore, ItemStack... reward) {
        return new ShopItem(name, icon, price, lore, reward);
    }

    private static ItemStack ench(ItemStack item, Enchantment e, int lvl) {
        item.addUnsafeEnchantment(e, lvl);
        return item;
    }

    private static ItemStack enchMulti(ItemStack item, Enchantment e1, int l1, Enchantment e2, int l2) {
        item.addUnsafeEnchantment(e1, l1);
        item.addUnsafeEnchantment(e2, l2);
        return item;
    }

    private static ItemStack potion(PotionEffectType type, int duration, int amplifier) {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        pot.setItemMeta(m);
        return pot;
    }

    private static ItemStack splashPotion(PotionEffectType type, int duration, int amplifier) {
        ItemStack pot = new ItemStack(Material.SPLASH_POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        pot.setItemMeta(m);
        return pot;
    }

    private static ShopItem book(String name, int price, Enchantment enchantment, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(enchantment, level, true);
        book.setItemMeta(meta);
        return new ShopItem(name, Material.ENCHANTED_BOOK, price,
            new String[]{"&7" + enchantment.getKey().getKey().replace("_", " ") + " " + romanLevel(level)},
            new ItemStack[]{book});
    }

    private static String romanLevel(int lvl) {
        return switch (lvl) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(lvl);
        };
    }
}
