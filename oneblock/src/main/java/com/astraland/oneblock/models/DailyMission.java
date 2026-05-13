package com.astraland.oneblock.models;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DailyMission {

    public enum MissionType {
        BREAK_BLOCKS, KILL_MOBS, EARN_COINS, BUY_UPGRADES, REACH_PHASE
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

    public DailyMission(String id, String displayName, String description,
                        Material icon, MissionType type, long target, int reward) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.target = target;
        this.reward = reward;
        this.progress = 0;
        this.completed = false;
        this.claimed = false;
    }

    public static List<DailyMission> generateDaily() {
        List<DailyMission[]> pools = Arrays.asList(
            new DailyMission[]{
                new DailyMission("break_200", "Casseur du jour", "Casse 200 blocs magiques",
                    Material.STONE, MissionType.BREAK_BLOCKS, 200, 250),
                new DailyMission("break_500", "Bûcheron infatigable", "Casse 500 blocs magiques",
                    Material.COBBLESTONE, MissionType.BREAK_BLOCKS, 500, 500),
                new DailyMission("break_1000", "Indestructible", "Casse 1 000 blocs magiques",
                    Material.IRON_ORE, MissionType.BREAK_BLOCKS, 1000, 1000)
            },
            new DailyMission[]{
                new DailyMission("kill_10", "Chasseur débutant", "Tue 10 mobs",
                    Material.BONE, MissionType.KILL_MOBS, 10, 150),
                new DailyMission("kill_30", "Chasseur aguerri", "Tue 30 mobs",
                    Material.BLAZE_ROD, MissionType.KILL_MOBS, 30, 350),
                new DailyMission("kill_75", "Tueur de masse", "Tue 75 mobs",
                    Material.NETHER_STAR, MissionType.KILL_MOBS, 75, 750)
            },
            new DailyMission[]{
                new DailyMission("earn_500", "Petit épargnant", "Gagne 500 pièces",
                    Material.GOLD_NUGGET, MissionType.EARN_COINS, 500, 200),
                new DailyMission("earn_2000", "Marchand", "Gagne 2 000 pièces",
                    Material.GOLD_INGOT, MissionType.EARN_COINS, 2000, 600),
                new DailyMission("earn_5000", "Tycoon", "Gagne 5 000 pièces",
                    Material.GOLD_BLOCK, MissionType.EARN_COINS, 5000, 1500)
            },
            new DailyMission[]{
                new DailyMission("upgrade_1", "Amateur d'upgrades", "Achète 1 amélioration",
                    Material.EXPERIENCE_BOTTLE, MissionType.BUY_UPGRADES, 1, 200),
                new DailyMission("upgrade_3", "Chercheur de puissance", "Achète 3 améliorations",
                    Material.ENCHANTED_BOOK, MissionType.BUY_UPGRADES, 3, 600),
                new DailyMission("upgrade_5", "Perfectionniste", "Achète 5 améliorations",
                    Material.NETHER_STAR, MissionType.BUY_UPGRADES, 5, 1200)
            },
            new DailyMission[]{
                new DailyMission("coins_shop", "Client fidèle", "Dépense 300 pièces dans le shop",
                    Material.EMERALD, MissionType.EARN_COINS, 300, 180),
                new DailyMission("coin_rich", "Croesus", "Accumule 3 000 pièces en une journée",
                    Material.DIAMOND, MissionType.EARN_COINS, 3000, 800),
                new DailyMission("coin_legend", "Légende économique", "Accumule 10 000 pièces en une journée",
                    Material.BEACON, MissionType.EARN_COINS, 10000, 3000)
            }
        );

        java.util.List<DailyMission> result = new java.util.ArrayList<>();
        for (DailyMission[] pool : pools) {
            result.add(pool[RANDOM.nextInt(pool.length)]);
        }
        return result;
    }

    public void addProgress(long amount) {
        if (claimed) return;
        progress = Math.min(progress + amount, target);
        if (progress >= target) completed = true;
    }

    public boolean isClaimed() { return claimed; }
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
    public boolean isClaimable() { return completed && !claimed; }
}
