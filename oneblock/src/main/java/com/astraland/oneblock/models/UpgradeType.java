package com.astraland.oneblock.models;

import org.bukkit.Material;

public enum UpgradeType {

    GENERATOR(
        "generator",
        "Générateur",
        "Améliore les blocs obtenus du OneBlock",
        Material.COMMAND_BLOCK,
        3,
        new int[]{500, 1500, 5000}
    ),
    MOB_DROPS(
        "mob_drops",
        "Butins de mobs",
        "Augmente la fréquence de spawn des mobs",
        Material.ZOMBIE_HEAD,
        3,
        new int[]{300, 1000, 3000}
    ),
    CHEST_LUCK(
        "chest_luck",
        "Chance aux coffres",
        "Augmente la chance d'obtenir des items bonus",
        Material.CHEST,
        3,
        new int[]{400, 1200, 4000}
    ),
    ISLAND_SIZE(
        "island_size",
        "Taille de l'île",
        "Agrandit la zone protégée autour du bloc magique",
        Material.GRASS_BLOCK,
        3,
        new int[]{600, 2000, 6000}
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final int maxLevel;
    private final int[] costs;

    UpgradeType(String id, String displayName, String description, Material icon, int maxLevel, int[] costs) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.costs = costs;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public int getMaxLevel() { return maxLevel; }

    public int getCost(int currentLevel) {
        if (currentLevel >= maxLevel) return -1;
        return costs[currentLevel];
    }

    public static UpgradeType fromId(String id) {
        for (UpgradeType t : values()) if (t.id.equals(id)) return t;
        return null;
    }
}
