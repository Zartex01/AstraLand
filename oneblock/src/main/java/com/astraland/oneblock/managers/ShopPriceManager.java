package com.astraland.oneblock.managers;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ShopPriceManager {

    private static final Map<Material, Integer> SELL_PRICES = new HashMap<>();

    static {
        // Minéraux
        put(Material.COAL, 3);
        put(Material.IRON_INGOT, 8);
        put(Material.GOLD_INGOT, 15);
        put(Material.DIAMOND, 50);
        put(Material.EMERALD, 40);
        put(Material.LAPIS_LAZULI, 5);
        put(Material.REDSTONE, 3);
        put(Material.QUARTZ, 4);
        put(Material.AMETHYST_SHARD, 8);
        put(Material.RAW_IRON, 6);
        put(Material.RAW_GOLD, 12);
        put(Material.RAW_COPPER, 4);
        put(Material.COPPER_INGOT, 5);

        // Bois et végétaux
        put(Material.OAK_LOG, 3);
        put(Material.BIRCH_LOG, 3);
        put(Material.SPRUCE_LOG, 3);
        put(Material.JUNGLE_LOG, 3);
        put(Material.ACACIA_LOG, 3);
        put(Material.DARK_OAK_LOG, 3);
        put(Material.OAK_SAPLING, 2);
        put(Material.APPLE, 4);
        put(Material.WHEAT, 2);
        put(Material.CARROT, 2);
        put(Material.POTATO, 2);
        put(Material.BEETROOT, 2);
        put(Material.MELON_SLICE, 2);
        put(Material.PUMPKIN, 5);
        put(Material.SUGAR_CANE, 2);
        put(Material.CACTUS, 2);
        put(Material.BAMBOO, 1);
        put(Material.VINE, 1);

        // Drops de mobs
        put(Material.BONE, 4);
        put(Material.ROTTEN_FLESH, 1);
        put(Material.SPIDER_EYE, 5);
        put(Material.STRING, 3);
        put(Material.FEATHER, 3);
        put(Material.LEATHER, 6);
        put(Material.GUNPOWDER, 6);
        put(Material.SLIME_BALL, 8);
        put(Material.BLAZE_ROD, 12);
        put(Material.GHAST_TEAR, 25);
        put(Material.MAGMA_CREAM, 10);
        put(Material.ENDER_PEARL, 20);
        put(Material.NETHER_STAR, 500);
        put(Material.WITHER_SKELETON_SKULL, 250);
        put(Material.WITHER_ROSE, 20);
        put(Material.PHANTOM_MEMBRANE, 15);
        put(Material.SHULKER_SHELL, 60);
        put(Material.PRISMARINE_SHARD, 8);
        put(Material.PRISMARINE_CRYSTALS, 10);
        put(Material.NAUTILUS_SHELL, 30);
        put(Material.HEART_OF_THE_SEA, 200);
        put(Material.TRIDENT, 150);

        // Nether
        put(Material.NETHERRACK, 1);
        put(Material.SOUL_SAND, 3);
        put(Material.NETHER_BRICK, 3);
        put(Material.NETHER_WART, 5);
        put(Material.GLOWSTONE_DUST, 4);
        put(Material.BASALT, 2);
        put(Material.ANCIENT_DEBRIS, 200);
        put(Material.NETHERITE_SCRAP, 300);
        put(Material.NETHERITE_INGOT, 1500);

        // End
        put(Material.END_STONE, 3);
        put(Material.PURPUR_BLOCK, 6);
        put(Material.CHORUS_FRUIT, 5);
        put(Material.POPPED_CHORUS_FRUIT, 8);
        put(Material.DRAGON_BREATH, 80);

        // Misc
        put(Material.OBSIDIAN, 15);
        put(Material.GRAVEL, 1);
        put(Material.SAND, 2);
        put(Material.CLAY_BALL, 3);
        put(Material.FLINT, 2);
        put(Material.INK_SAC, 4);
        put(Material.WHITE_WOOL, 4);
        put(Material.EGG, 1);
        put(Material.HONEY_BOTTLE, 8);
        put(Material.HONEYCOMB, 6);
        put(Material.EXPERIENCE_BOTTLE, 10);
    }

    private static void put(Material mat, int price) { SELL_PRICES.put(mat, price); }

    public static int getSellPrice(Material mat) { return SELL_PRICES.getOrDefault(mat, 0); }

    public static Map<Material, Integer> getAllPrices() { return SELL_PRICES; }

    public static boolean isSellable(Material mat) { return SELL_PRICES.containsKey(mat); }
}
