package com.astraland.skyblock.challenges;

import org.bukkit.Material;

public class Challenge {

    public enum Type {
        ISLAND_LEVEL,
        GENERATOR_LEVEL,
        BLOCKS_BROKEN,
        BALANCE,
        ISLAND_VALUE,
        MEMBER_COUNT,
        BANK_BALANCE
    }

    public enum Category {
        PROGRESSION,
        ECONOMIE,
        CONSTRUCTION,
        SOCIAL
    }

    private final String    id;
    private final String    displayName;
    private final String    description;
    private final Material  icon;
    private final Type      type;
    private final long      requiredValue;
    private final int       rewardMoney;
    private final int       rewardXP;
    private final Category  category;

    public Challenge(String id, String displayName, String description,
                     Material icon, Type type, long requiredValue,
                     int rewardMoney, int rewardXP, Category category) {
        this.id           = id;
        this.displayName  = displayName;
        this.description  = description;
        this.icon         = icon;
        this.type         = type;
        this.requiredValue = requiredValue;
        this.rewardMoney  = rewardMoney;
        this.rewardXP     = rewardXP;
        this.category     = category;
    }

    public String    getId()           { return id; }
    public String    getDisplayName()  { return displayName; }
    public String    getDescription()  { return description; }
    public Material  getIcon()         { return icon; }
    public Type      getType()         { return type; }
    public long      getRequiredValue(){ return requiredValue; }
    public int       getRewardMoney()  { return rewardMoney; }
    public int       getRewardXP()     { return rewardXP; }
    public Category  getCategory()     { return category; }
}
