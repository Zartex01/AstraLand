package com.astraland.skyblock.ranks;

public enum IslandRank {

    BOIS      (0,   "&7",  "Bois",       "OAK_LOG",         0,   0),
    PIERRE    (5,   "&f",  "Pierre",     "STONE",            5,   1),
    FER       (10,  "&b",  "Fer",        "IRON_INGOT",       10,  1),
    OR        (15,  "&6",  "Or",         "GOLD_INGOT",       15,  2),
    DIAMANT   (20,  "&3",  "Diamant",    "DIAMOND",          20,  3),
    EMERAUDE  (23,  "&a",  "Émeraude",   "EMERALD",          25,  4),
    NETHERITE (25,  "&5",  "Nétherite",  "NETHERITE_INGOT",  30,  5);

    private final int  minLevel;
    private final String color;
    private final String displayName;
    private final String iconMaterial;
    private final int  sellBonus;       // % bonus sur les ventes shop
    private final int  generatorBonus;  // bonus de vitesse du générateur

    IslandRank(int minLevel, String color, String displayName,
               String iconMaterial, int sellBonus, int generatorBonus) {
        this.minLevel       = minLevel;
        this.color          = color;
        this.displayName    = displayName;
        this.iconMaterial   = iconMaterial;
        this.sellBonus      = sellBonus;
        this.generatorBonus = generatorBonus;
    }

    public int    getMinLevel()       { return minLevel; }
    public String getColor()          { return color; }
    public String getDisplayName()    { return displayName; }
    public String getIconMaterial()   { return iconMaterial; }
    public int    getSellBonus()      { return sellBonus; }
    public int    getGeneratorBonus() { return generatorBonus; }

    public String getPrefix()         { return color + "[" + displayName + "] &r"; }
    public String getFullName()       { return color + displayName; }

    public static IslandRank fromLevel(int level) {
        IslandRank result = BOIS;
        for (IslandRank r : values())
            if (level >= r.minLevel) result = r;
        return result;
    }

    public IslandRank next() {
        IslandRank[] vals = values();
        int idx = ordinal();
        return idx < vals.length - 1 ? vals[idx + 1] : null;
    }
}
