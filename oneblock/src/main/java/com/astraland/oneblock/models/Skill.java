package com.astraland.oneblock.models;

import org.bukkit.Material;

public enum Skill {

    MINING("mining", "Minage", "XP en cassant le bloc magique",
        Material.IRON_PICKAXE, "§a+5% argent par palier (max +25%)"),
    COMBAT("combat", "Combat", "XP en tuant des mobs sur ton île",
        Material.IRON_SWORD, "§a+8% argent mobs par palier (max +40%)"),
    FARMING("farming", "Récolte", "XP quand tu reçois des items bonus",
        Material.WHEAT, "§a+3% chance loot bonus par palier (max +15%)");

    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final String perkDescription;

    public static final int MAX_LEVEL = 50;

    Skill(String id, String displayName, String description, Material icon, String perkDescription) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.perkDescription = perkDescription;
    }

    public long xpForLevel(int level) {
        if (level <= 0) return 0;
        return (long) level * 100;
    }

    public long totalXpForLevel(int level) {
        long total = 0;
        for (int i = 1; i <= level; i++) total += xpForLevel(i);
        return total;
    }

    public int levelFromXp(long xp) {
        int level = 0;
        long total = 0;
        while (level < MAX_LEVEL) {
            total += xpForLevel(level + 1);
            if (xp < total) break;
            level++;
        }
        return level;
    }

    public long xpInCurrentLevel(long totalXp) {
        int level = levelFromXp(totalXp);
        return totalXp - totalXpForLevel(level);
    }

    public long xpNeededForNext(long totalXp) {
        int level = levelFromXp(totalXp);
        if (level >= MAX_LEVEL) return 0;
        return xpForLevel(level + 1);
    }

    public double getMoneyMultiplierBonus(int level) {
        return switch (this) {
            case MINING -> Math.min(level / 10 * 0.05, 0.25);
            case COMBAT -> Math.min(level / 10 * 0.08, 0.40);
            case FARMING -> 0;
        };
    }

    public double getLootChanceBonus(int level) {
        return switch (this) {
            case FARMING -> Math.min(level / 10 * 0.03, 0.15);
            default -> 0;
        };
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public String getPerkDescription() { return perkDescription; }

    public static Skill fromId(String id) {
        for (Skill s : values()) if (s.id.equals(id)) return s;
        return null;
    }
}
