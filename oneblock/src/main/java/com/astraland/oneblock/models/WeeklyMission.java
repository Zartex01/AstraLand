package com.astraland.oneblock.models;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeeklyMission {

    public enum MissionType {
        BREAK_BLOCKS, KILL_MOBS, EARN_COINS, BUY_UPGRADES, REACH_MILESTONE
    }

    private static final Random RANDOM = new Random();

    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final MissionType type;
    private final long target;
    private final int reward;
    private long progress;
    private boolean completed;
    private boolean claimed;

    public WeeklyMission(String id, String displayName, String description,
                         Material icon, MissionType type, long target, int reward) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.target = target;
        this.reward = reward;
    }

    public static List<WeeklyMission> generateWeekly() {
        WeeklyMission[][] pools = {
            {
                new WeeklyMission("w_break_5k", "Briseur d'îles", "Casse 5 000 blocs magiques",
                    Material.DIAMOND_PICKAXE, MissionType.BREAK_BLOCKS, 5000, 3000),
                new WeeklyMission("w_break_15k", "Force implacable", "Casse 15 000 blocs magiques",
                    Material.NETHERITE_PICKAXE, MissionType.BREAK_BLOCKS, 15000, 8000),
                new WeeklyMission("w_break_30k", "Démolisseur légendaire", "Casse 30 000 blocs",
                    Material.BEACON, MissionType.BREAK_BLOCKS, 30000, 20000)
            },
            {
                new WeeklyMission("w_kill_250", "Chasseur de prime", "Tue 250 mobs",
                    Material.DIAMOND_SWORD, MissionType.KILL_MOBS, 250, 2500),
                new WeeklyMission("w_kill_750", "Fléau des créatures", "Tue 750 mobs",
                    Material.NETHERITE_SWORD, MissionType.KILL_MOBS, 750, 7500),
                new WeeklyMission("w_kill_2000", "Guerrier de l'ombre", "Tue 2 000 mobs",
                    Material.NETHER_STAR, MissionType.KILL_MOBS, 2000, 20000)
            },
            {
                new WeeklyMission("w_earn_10k", "Banquier", "Gagne 10 000 pièces cette semaine",
                    Material.GOLD_BLOCK, MissionType.EARN_COINS, 10000, 2000),
                new WeeklyMission("w_earn_50k", "Magnat", "Gagne 50 000 pièces cette semaine",
                    Material.DIAMOND, MissionType.EARN_COINS, 50000, 8000),
                new WeeklyMission("w_earn_200k", "Oligarque", "Gagne 200 000 pièces cette semaine",
                    Material.BEACON, MissionType.EARN_COINS, 200000, 30000)
            }
        };

        List<WeeklyMission> result = new ArrayList<>();
        for (WeeklyMission[] pool : pools) result.add(pool[RANDOM.nextInt(pool.length)]);
        return result;
    }

    public void addProgress(long amount) {
        if (claimed) return;
        progress = Math.min(progress + amount, target);
        if (progress >= target) completed = true;
    }

    public void claim() { this.claimed = true; }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public MissionType getType() { return type; }
    public long getTarget() { return target; }
    public int getReward() { return reward; }
    public long getProgress() { return progress; }
    public void setProgress(long p) { this.progress = p; if (p >= target) completed = true; }
    public boolean isCompleted() { return completed; }
    public boolean isClaimed() { return claimed; }
    public boolean isClaimable() { return completed && !claimed; }
}
