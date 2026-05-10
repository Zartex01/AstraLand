package com.astraland.pvpfactions.managers;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class SellManager {

    private final Map<Material, Integer> sellPrices = new HashMap<>();

    public SellManager() {
        registerAll();
    }

    private void registerAll() {
        /* ── Blocs ── */
        sell(Material.OAK_LOG, 1);
        sell(Material.BIRCH_LOG, 1);
        sell(Material.SPRUCE_LOG, 1);
        sell(Material.JUNGLE_LOG, 1);
        sell(Material.ACACIA_LOG, 1);
        sell(Material.DARK_OAK_LOG, 1);
        sell(Material.OAK_PLANKS, 1);
        sell(Material.BIRCH_PLANKS, 1);
        sell(Material.COBBLESTONE, 1);
        sell(Material.STONE, 1);
        sell(Material.SAND, 1);
        sell(Material.GRAVEL, 1);
        sell(Material.DIRT, 1);
        sell(Material.GLOWSTONE, 5);
        sell(Material.WHITE_WOOL, 2);
        sell(Material.GLASS, 1);
        sell(Material.OBSIDIAN, 8);
        sell(Material.TNT, 12);
        sell(Material.TORCH, 1);
        sell(Material.CHEST, 3);
        sell(Material.NETHERRACK, 1);
        sell(Material.SOUL_SAND, 2);
        sell(Material.QUARTZ_BLOCK, 4);

        /* ── Minerais ── */
        sell(Material.COAL, 2);
        sell(Material.COAL_ORE, 2);
        sell(Material.IRON_INGOT, 5);
        sell(Material.IRON_ORE, 4);
        sell(Material.RAW_IRON, 4);
        sell(Material.GOLD_INGOT, 8);
        sell(Material.GOLD_ORE, 7);
        sell(Material.RAW_GOLD, 7);
        sell(Material.GOLD_NUGGET, 1);
        sell(Material.DIAMOND, 40);
        sell(Material.DIAMOND_ORE, 35);
        sell(Material.LAPIS_LAZULI, 2);
        sell(Material.REDSTONE, 2);
        sell(Material.EMERALD, 15);
        sell(Material.EMERALD_ORE, 12);
        sell(Material.NETHER_QUARTZ_ORE, 2);
        sell(Material.QUARTZ, 2);
        sell(Material.ANCIENT_DEBRIS, 200);
        sell(Material.NETHERITE_SCRAP, 120);
        sell(Material.NETHERITE_INGOT, 500);
        sell(Material.COPPER_INGOT, 3);
        sell(Material.RAW_COPPER, 3);

        /* ── Nourriture ── */
        sell(Material.COOKED_BEEF, 2);
        sell(Material.COOKED_PORKCHOP, 2);
        sell(Material.COOKED_CHICKEN, 1);
        sell(Material.BREAD, 1);
        sell(Material.GOLDEN_APPLE, 100);
        sell(Material.GOLDEN_CARROT, 5);
        sell(Material.MELON_SLICE, 1);

        /* ── Potions (aucun remboursement direct par défaut) ── */

        /* ── Armes & Armures ── */
        sell(Material.IRON_SWORD, 25);
        sell(Material.DIAMOND_SWORD, 100);
        sell(Material.NETHERITE_SWORD, 400);
        sell(Material.BOW, 40);
        sell(Material.IRON_HELMET, 20);
        sell(Material.IRON_CHESTPLATE, 30);
        sell(Material.IRON_LEGGINGS, 25);
        sell(Material.IRON_BOOTS, 20);
        sell(Material.DIAMOND_HELMET, 60);
        sell(Material.DIAMOND_CHESTPLATE, 90);
        sell(Material.DIAMOND_LEGGINGS, 75);
        sell(Material.DIAMOND_BOOTS, 60);
        sell(Material.NETHERITE_HELMET, 200);
        sell(Material.NETHERITE_CHESTPLATE, 300);
        sell(Material.NETHERITE_LEGGINGS, 250);
        sell(Material.NETHERITE_BOOTS, 200);
        sell(Material.SHIELD, 40);
        sell(Material.ARROW, 1);
        sell(Material.ENDER_PEARL, 20);
        sell(Material.TOTEM_OF_UNDYING, 350);
    }

    private void sell(Material mat, int pricePerUnit) {
        sellPrices.put(mat, pricePerUnit);
    }

    public boolean canSell(Material mat) {
        return sellPrices.containsKey(mat);
    }

    public int getSellPrice(Material mat) {
        return sellPrices.getOrDefault(mat, 0);
    }

    public int getTotalSellPrice(Material mat, int amount) {
        return getSellPrice(mat) * amount;
    }

    public Map<Material, Integer> getAllPrices() {
        return sellPrices;
    }
}
