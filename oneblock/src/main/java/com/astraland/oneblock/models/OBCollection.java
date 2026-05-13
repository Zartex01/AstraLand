package com.astraland.oneblock.models;

import org.bukkit.Material;

public enum OBCollection {

    DIAMOND("Diamant", Material.DIAMOND,
        new long[]{100, 500, 2000, 7500, 25000},
        new int[]{500, 2500, 10000, 35000, 150000}),
    EMERALD("Émeraude", Material.EMERALD,
        new long[]{100, 500, 2000, 7500, 25000},
        new int[]{400, 2000, 8000, 28000, 120000}),
    IRON_INGOT("Lingot de Fer", Material.IRON_INGOT,
        new long[]{200, 1000, 5000, 20000, 75000},
        new int[]{100, 500, 2000, 8000, 30000}),
    GOLD_INGOT("Lingot d'Or", Material.GOLD_INGOT,
        new long[]{150, 750, 3000, 12000, 50000},
        new int[]{200, 1000, 4000, 15000, 60000}),
    COAL("Charbon", Material.COAL,
        new long[]{500, 2500, 10000, 40000, 150000},
        new int[]{50, 250, 1000, 4000, 15000}),
    BLAZE_ROD("Bâton de Blaze", Material.BLAZE_ROD,
        new long[]{50, 250, 1000, 4000, 15000},
        new int[]{300, 1500, 6000, 22000, 80000}),
    ENDER_PEARL("Perle de l'Ender", Material.ENDER_PEARL,
        new long[]{50, 200, 750, 3000, 10000},
        new int[]{400, 2000, 8000, 30000, 100000}),
    NETHER_STAR("Étoile du Nether", Material.NETHER_STAR,
        new long[]{5, 20, 75, 250, 1000},
        new int[]{2000, 10000, 40000, 150000, 500000}),
    BONE("Os", Material.BONE,
        new long[]{200, 1000, 4000, 15000, 50000},
        new int[]{80, 400, 1600, 6000, 20000}),
    STRING("Ficelle", Material.STRING,
        new long[]{300, 1500, 6000, 25000, 80000},
        new int[]{60, 300, 1200, 4500, 15000}),
    FEATHER("Plume", Material.FEATHER,
        new long[]{300, 1500, 6000, 25000, 80000},
        new int[]{60, 300, 1200, 4500, 15000}),
    LEATHER("Cuir", Material.LEATHER,
        new long[]{200, 1000, 4000, 15000, 50000},
        new int[]{80, 400, 1600, 6000, 20000}),
    GUNPOWDER("Poudre à Canon", Material.GUNPOWDER,
        new long[]{150, 750, 3000, 12000, 40000},
        new int[]{120, 600, 2400, 9000, 30000}),
    SLIME_BALL("Balle de Slime", Material.SLIME_BALL,
        new long[]{100, 500, 2000, 8000, 30000},
        new int[]{200, 1000, 4000, 15000, 50000}),
    SPIDER_EYE("Œil d'Araignée", Material.SPIDER_EYE,
        new long[]{150, 750, 3000, 12000, 40000},
        new int[]{120, 600, 2400, 9000, 30000}),
    OBSIDIAN("Obsidienne", Material.OBSIDIAN,
        new long[]{50, 250, 1000, 5000, 20000},
        new int[]{400, 2000, 8000, 30000, 100000}),
    GHAST_TEAR("Larme de Ghast", Material.GHAST_TEAR,
        new long[]{20, 100, 400, 1500, 6000},
        new int[]{1000, 5000, 20000, 75000, 250000}),
    MAGMA_CREAM("Crème de Magma", Material.MAGMA_CREAM,
        new long[]{80, 400, 1600, 6000, 25000},
        new int[]{200, 1000, 4000, 15000, 50000}),
    ROTTEN_FLESH("Chair Pourrie", Material.ROTTEN_FLESH,
        new long[]{500, 2500, 10000, 40000, 150000},
        new int[]{40, 200, 800, 3000, 10000}),
    WITHER_SKULL("Crâne de Wither", Material.WITHER_SKELETON_SKULL,
        new long[]{3, 10, 50, 200, 750},
        new int[]{5000, 25000, 100000, 400000, 1500000});

    private final String displayName;
    private final Material material;
    private final long[] milestoneAmounts;
    private final int[] milestoneRewards;

    OBCollection(String displayName, Material material, long[] milestoneAmounts, int[] milestoneRewards) {
        this.displayName = displayName;
        this.material = material;
        this.milestoneAmounts = milestoneAmounts;
        this.milestoneRewards = milestoneRewards;
    }

    public int getMilestoneCount() { return milestoneAmounts.length; }

    public long getMilestoneAmount(int index) {
        if (index < 0 || index >= milestoneAmounts.length) return Long.MAX_VALUE;
        return milestoneAmounts[index];
    }

    public int getMilestoneReward(int index) {
        if (index < 0 || index >= milestoneRewards.length) return 0;
        return milestoneRewards[index];
    }

    public int getHighestClaimedMilestone(long collected) {
        for (int i = milestoneAmounts.length - 1; i >= 0; i--)
            if (collected >= milestoneAmounts[i]) return i;
        return -1;
    }

    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public long[] getMilestoneAmounts() { return milestoneAmounts; }
    public int[] getMilestoneRewards() { return milestoneRewards; }

    public static OBCollection fromMaterial(Material mat) {
        for (OBCollection c : values()) if (c.material == mat) return c;
        return null;
    }
}
