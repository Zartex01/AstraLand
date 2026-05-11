package com.astraland.skyblock.managers;

import com.astraland.skyblock.models.Island;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.EnumMap;
import java.util.Map;

public class IslandLevelManager {

    private final Map<Material, Integer> blockValues = new EnumMap<>(Material.class);

    public IslandLevelManager() {
        buildValueMap();
    }

    private void buildValueMap() {
        // Commun
        set(Material.COBBLESTONE, 1);
        set(Material.STONE, 2);
        set(Material.DIRT, 1);
        set(Material.GRASS_BLOCK, 1);
        set(Material.SAND, 2);
        set(Material.GRAVEL, 2);
        set(Material.ANDESITE, 2);
        set(Material.DIORITE, 2);
        set(Material.GRANITE, 2);
        set(Material.OAK_LOG, 5);
        set(Material.BIRCH_LOG, 5);
        set(Material.SPRUCE_LOG, 5);
        set(Material.JUNGLE_LOG, 5);
        set(Material.ACACIA_LOG, 5);
        set(Material.DARK_OAK_LOG, 5);
        set(Material.MANGROVE_LOG, 5);
        set(Material.CHERRY_LOG, 5);
        set(Material.OAK_PLANKS, 2);
        set(Material.GLASS, 3);
        set(Material.NETHERRACK, 2);
        set(Material.SOUL_SAND, 3);
        set(Material.OBSIDIAN, 20);
        set(Material.TORCH, 1);
        // Minerais
        set(Material.COAL_ORE, 10);
        set(Material.DEEPSLATE_COAL_ORE, 12);
        set(Material.IRON_ORE, 30);
        set(Material.DEEPSLATE_IRON_ORE, 35);
        set(Material.GOLD_ORE, 60);
        set(Material.DEEPSLATE_GOLD_ORE, 70);
        set(Material.DIAMOND_ORE, 300);
        set(Material.DEEPSLATE_DIAMOND_ORE, 350);
        set(Material.EMERALD_ORE, 500);
        set(Material.DEEPSLATE_EMERALD_ORE, 600);
        set(Material.LAPIS_ORE, 20);
        set(Material.REDSTONE_ORE, 15);
        set(Material.NETHER_QUARTZ_ORE, 10);
        set(Material.ANCIENT_DEBRIS, 400);
        // Blocs compressés
        set(Material.COAL_BLOCK, 100);
        set(Material.IRON_BLOCK, 250);
        set(Material.GOLD_BLOCK, 500);
        set(Material.DIAMOND_BLOCK, 2700);
        set(Material.EMERALD_BLOCK, 4500);
        set(Material.LAPIS_BLOCK, 180);
        set(Material.REDSTONE_BLOCK, 120);
        set(Material.NETHERITE_BLOCK, 8000);
        set(Material.COPPER_BLOCK, 80);
        // Agriculture
        set(Material.HAY_BLOCK, 30);
        set(Material.MELON, 20);
        set(Material.PUMPKIN, 20);
        set(Material.CACTUS, 3);
        set(Material.SUGAR_CANE, 2);
        set(Material.BAMBOO, 2);
        set(Material.NETHER_WART_BLOCK, 20);
        // Déco
        set(Material.SPONGE, 50);
        set(Material.WET_SPONGE, 40);
        set(Material.GLOWSTONE, 25);
        set(Material.SEA_LANTERN, 30);
        set(Material.SHROOMLIGHT, 25);
        set(Material.BEACON, 5000);
        set(Material.NETHER_STAR, 5000);
        set(Material.CONDUIT, 3000);
        // Nether
        set(Material.MAGMA_BLOCK, 10);
        set(Material.NETHER_BRICK, 5);
        set(Material.NETHER_BRICKS, 20);
        set(Material.WARPED_STEM, 5);
        set(Material.CRIMSON_STEM, 5);
        set(Material.CRYING_OBSIDIAN, 40);
        set(Material.BLACKSTONE, 3);
    }

    private void set(Material m, int v) { blockValues.put(m, v); }

    public int getBlockValue(Material m) { return blockValues.getOrDefault(m, 0); }

    /**
     * Scanne tous les blocs dans le rayon de l'île et recalcule sa valeur + niveau.
     * Renvoie la valeur calculée.
     */
    public long scanIsland(Island island, int radius) {
        Location center = island.getCenter();
        if (center == null) return 0;
        World world = center.getWorld();
        if (world == null) return 0;

        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int half = radius / 2;

        long total = 0;
        int minY = Math.max(-64, center.getBlockY() - 30);
        int maxY = Math.min(320, center.getBlockY() + 80);

        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - half; z <= cz + half; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Material m = world.getBlockAt(x, y, z).getType();
                    if (m.isAir() || m == Material.WATER || m == Material.LAVA) continue;
                    total += getBlockValue(m);
                }
            }
        }

        island.setValue(total);
        island.setLevel(valueToLevel(total));
        return total;
    }

    public int valueToLevel(long value) {
        if (value < 500) return 0;
        if (value < 2000) return 1;
        if (value < 5000) return 2;
        if (value < 12000) return 3;
        if (value < 25000) return 4;
        if (value < 50000) return 5;
        if (value < 100000) return 6;
        if (value < 200000) return 7;
        if (value < 400000) return 8;
        if (value < 800000) return 9;
        return 10;
    }

    public long levelThreshold(int level) {
        return switch (level) {
            case 0 -> 0;
            case 1 -> 500;
            case 2 -> 2000;
            case 3 -> 5000;
            case 4 -> 12000;
            case 5 -> 25000;
            case 6 -> 50000;
            case 7 -> 100000;
            case 8 -> 200000;
            case 9 -> 400000;
            default -> 800000;
        };
    }
}
