package com.astraland.skyblock.quests;

import org.bukkit.Material;

public class DailyQuest {

    public enum Type {
        BREAK_BLOCKS,
        PLACE_BLOCKS,
        EARN_MONEY,
        KILL_MOBS,
        FISH,
        GROW_CROPS
    }

    private final String  id;
    private final Type    type;
    private final String  displayName;
    private final String  description;
    private final int     target;
    private final int     rewardMoney;
    private final int     rewardXP;
    private final Material icon;

    public DailyQuest(String id, Type type, String displayName, String description,
                      int target, int rewardMoney, int rewardXP, Material icon) {
        this.id          = id;
        this.type        = type;
        this.displayName = displayName;
        this.description = description;
        this.target      = target;
        this.rewardMoney = rewardMoney;
        this.rewardXP    = rewardXP;
        this.icon        = icon;
    }

    public String   getId()          { return id; }
    public Type     getType()        { return type; }
    public String   getDisplayName() { return displayName; }
    public String   getDescription() { return description; }
    public int      getTarget()      { return target; }
    public int      getRewardMoney() { return rewardMoney; }
    public int      getRewardXP()    { return rewardXP; }
    public Material getIcon()        { return icon; }
}
