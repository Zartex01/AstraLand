package com.astraland.pvpfactions.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public enum ShopCategory {

    ARMES("&c&l⚔ Armes", Material.DIAMOND_SWORD,
        "&7Épées, arcs, arbalètes et munitions"),

    ARMURES("&b&l🛡 Armures", Material.DIAMOND_CHESTPLATE,
        "&7Casques, plastrons, jambières et bottes"),

    NOURRITURE("&e&l🍖 Nourriture", Material.COOKED_BEEF,
        "&7Aliments et pommes dorées"),

    POTIONS("&d&l🧪 Potions", Material.SPLASH_POTION,
        "&7Potions de combat et de survie"),

    UTILITAIRES("&a&l🔧 Utilitaires", Material.CHEST,
        "&7Perles, TNT, blocs et outils"),

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
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }

    public List<ShopItem> getItems() {
        return switch (this) {
            case ARMES -> buildArmes();
            case ARMURES -> buildArmures();
            case NOURRITURE -> buildNourriture();
            case POTIONS -> buildPotions();
            case UTILITAIRES -> buildUtilitaires();
            case ENCHANTEMENTS -> buildEnchantements();
        };
    }

    private static List<ShopItem> buildArmes() {
        return List.of(
            item("&fÉpée de Fer", Material.IRON_SWORD, 50,
                new String[]{"&7Tranchant I"},
                ench(new ItemStack(Material.IRON_SWORD), Enchantment.SHARPNESS, 1)),
            item("&bÉpée de Diamant", Material.DIAMOND_SWORD, 200,
                new String[]{"&7Tranchant II"},
                ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 2)),
            item("&bÉpée Diamant &7[Sharp III]", Material.DIAMOND_SWORD, 350,
                new String[]{"&7Tranchant III"},
                ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 3)),
            item("&5Épée Nétherite", Material.NETHERITE_SWORD, 700,
                new String[]{"&7Tranchant III + Solidité III"},
                enchMulti(new ItemStack(Material.NETHERITE_SWORD),
                    Enchantment.SHARPNESS, 3, Enchantment.UNBREAKING, 3)),
            item("&aArc Standard", Material.BOW, 80,
                new String[]{"&7Puissance II"},
                ench(new ItemStack(Material.BOW), Enchantment.POWER, 2)),
            item("&aArc Infini", Material.BOW, 220,
                new String[]{"&7Puissance III + Infini"},
                enchMulti(new ItemStack(Material.BOW),
                    Enchantment.POWER, 3, Enchantment.INFINITY, 1)),
            item("&cArbalète", Material.CROSSBOW, 150,
                new String[]{"&7Chargement Rapide III"},
                ench(new ItemStack(Material.CROSSBOW), Enchantment.QUICK_CHARGE, 3)),
            item("&6Trident", Material.TRIDENT, 300,
                new String[]{"&7Fidélité III"},
                ench(new ItemStack(Material.TRIDENT), Enchantment.LOYALTY, 3)),
            item("&eHache de Diamant", Material.DIAMOND_AXE, 180,
                new String[]{"&7Tranchant II"},
                ench(new ItemStack(Material.DIAMOND_AXE), Enchantment.SHARPNESS, 2)),
            item("&fFlèches &7×64", Material.ARROW, 30,
                new String[]{"&764 flèches normales"},
                new ItemStack(Material.ARROW, 64)),
            item("&dFlèches Spectrales &7×16", Material.SPECTRAL_ARROW, 60,
                new String[]{"&716 flèches spectrales", "&7(révèle l'ennemi)"},
                new ItemStack(Material.SPECTRAL_ARROW, 16)),
            item("&cFlèches de Feu &7×8", Material.TIPPED_ARROW, 80,
                new String[]{"&78 flèches enflammées"},
                fireTipped())
        );
    }

    private static List<ShopItem> buildArmures() {
        return List.of(
            item("&fArmure de Fer complète", Material.IRON_CHESTPLATE, 150,
                new String[]{"&7Casque + Plastron + Jambières + Bottes"},
                new ItemStack(Material.IRON_HELMET),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_BOOTS)),
            item("&bArmure Diamant &7[Prot II]", Material.DIAMOND_CHESTPLATE, 400,
                new String[]{"&7Armure complète Protection II"},
                enchArmor(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 2),
                enchArmor(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 2),
                enchArmor(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 2),
                enchArmor(Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 2)),
            item("&5Armure Nétherite &7[Prot III]", Material.NETHERITE_CHESTPLATE, 900,
                new String[]{"&7Armure complète Protection III + Solidité III"},
                enchMulti(new ItemStack(Material.NETHERITE_HELMET), Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                enchMulti(new ItemStack(Material.NETHERITE_CHESTPLATE), Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                enchMulti(new ItemStack(Material.NETHERITE_LEGGINGS), Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3),
                enchMulti(new ItemStack(Material.NETHERITE_BOOTS), Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3)),
            item("&bCasque de Diamant", Material.DIAMOND_HELMET, 100,
                new String[]{"&7Protection II"},
                enchArmor(Material.DIAMOND_HELMET, Enchantment.PROTECTION, 2)),
            item("&bPlastron de Diamant", Material.DIAMOND_CHESTPLATE, 150,
                new String[]{"&7Protection II"},
                enchArmor(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION, 2)),
            item("&bJambières de Diamant", Material.DIAMOND_LEGGINGS, 130,
                new String[]{"&7Protection II"},
                enchArmor(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION, 2)),
            item("&bBottes de Diamant", Material.DIAMOND_BOOTS, 100,
                new String[]{"&7Protection II + Chute de Plumes III"},
                enchMulti(new ItemStack(Material.DIAMOND_BOOTS),
                    Enchantment.PROTECTION, 2, Enchantment.FEATHER_FALLING, 3)),
            item("&7Bouclier", Material.SHIELD, 80,
                new String[]{"&7Bloque les projectiles"},
                new ItemStack(Material.SHIELD)),
            item("&6Élytres", Material.ELYTRA, 500,
                new String[]{"&7Vole dans les airs"},
                new ItemStack(Material.ELYTRA)),
            item("&fBottes de Fer", Material.IRON_BOOTS, 30,
                new String[]{"&7Chute de Plumes II"},
                ench(new ItemStack(Material.IRON_BOOTS), Enchantment.FEATHER_FALLING, 2)),
            item("&aCasque en Cuir &7(teint)", Material.LEATHER_HELMET, 25,
                new String[]{"&7Casque basique coloré"},
                new ItemStack(Material.LEATHER_HELMET))
        );
    }

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
                new ItemStack(Material.GOLDEN_APPLE)),
            item("&6Pommes Dorées &7×4", Material.GOLDEN_APPLE, 500,
                new String[]{"&74 pommes dorées"},
                new ItemStack(Material.GOLDEN_APPLE, 4)),
            item("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 800,
                new String[]{"&7Régénération V + Résistance + Absorption"},
                new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)),
            item("&aCarotte Dorée &7×8", Material.GOLDEN_CARROT, 60,
                new String[]{"&78 carottes dorées"},
                new ItemStack(Material.GOLDEN_CARROT, 8)),
            item("&bMelon &7×32", Material.MELON_SLICE, 15,
                new String[]{"&732 tranches de melon"},
                new ItemStack(Material.MELON_SLICE, 32)),
            item("&dGâteau", Material.CAKE, 50,
                new String[]{"&7Un délicieux gâteau (7 morceaux)"},
                new ItemStack(Material.CAKE)),
            item("&5Fruit du Chorus &7×8", Material.CHORUS_FRUIT, 60,
                new String[]{"&78 fruits du chorus", "&7(téléportation aléatoire)"},
                new ItemStack(Material.CHORUS_FRUIT, 8))
        );
    }

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
                new ItemStack(Material.TOTEM_OF_UNDYING))
        );
    }

    private static List<ShopItem> buildUtilitaires() {
        return List.of(
            item("&bEnder Pearl &7×4", Material.ENDER_PEARL, 100,
                new String[]{"&74 perles d'ender"},
                new ItemStack(Material.ENDER_PEARL, 4)),
            item("&bEnder Pearl &7×16", Material.ENDER_PEARL, 350,
                new String[]{"&716 perles d'ender"},
                new ItemStack(Material.ENDER_PEARL, 16)),
            item("&cTNT &7×4", Material.TNT, 80,
                new String[]{"&74 TNT"},
                new ItemStack(Material.TNT, 4)),
            item("&8Obsidienne &7×16", Material.OBSIDIAN, 100,
                new String[]{"&716 blocs d'obsidienne"},
                new ItemStack(Material.OBSIDIAN, 16)),
            item("&fLaine &7×64", Material.WHITE_WOOL, 40,
                new String[]{"&764 blocs de laine blanche"},
                new ItemStack(Material.WHITE_WOOL, 64)),
            item("&aCorde &7×16", Material.STRING, 30,
                new String[]{"&716 fils de corde"},
                new ItemStack(Material.STRING, 16)),
            item("&eCoffre &7×8", Material.CHEST, 30,
                new String[]{"&78 coffres en bois"},
                new ItemStack(Material.CHEST, 8)),
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
                new ItemStack(Material.GRINDSTONE)),
            item("&7Boussole", Material.COMPASS, 20,
                new String[]{"&7Indique la direction"},
                new ItemStack(Material.COMPASS))
        );
    }

    private static List<ShopItem> buildEnchantements() {
        return List.of(
            book("&cTranchant III", 200, Enchantment.SHARPNESS, 3),
            book("&7Protection III", 200, Enchantment.PROTECTION, 3),
            book("&bProtection Proj. III", 180, Enchantment.PROJECTILE_PROTECTION, 3),
            book("&5Solidité III", 180, Enchantment.UNBREAKING, 3),
            book("&aInfini", 150, Enchantment.INFINITY, 1),
            book("&eChute de Plumes IV", 120, Enchantment.FEATHER_FALLING, 4),
            book("&6Efficacité IV", 160, Enchantment.EFFICIENCY, 4),
            book("&dFortune III", 250, Enchantment.FORTUNE, 3),
            book("&bSoie Touchante", 200, Enchantment.SILK_TOUCH, 1),
            book("&aRéparation", 300, Enchantment.MENDING, 1),
            book("&cAspect de Feu II", 150, Enchantment.FIRE_ASPECT, 2),
            book("&ePillage III", 180, Enchantment.LOOTING, 3)
        );
    }

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

    private static ItemStack enchArmor(Material mat, Enchantment e, int lvl) {
        return ench(new ItemStack(mat), e, lvl);
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

    private static ItemStack fireTipped() {
        ItemStack arr = new ItemStack(Material.TIPPED_ARROW, 8);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) arr.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0), true);
        arr.setItemMeta(m);
        return arr;
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
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(lvl);
        };
    }
}
