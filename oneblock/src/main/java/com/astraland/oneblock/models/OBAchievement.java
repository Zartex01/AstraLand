package com.astraland.oneblock.models;

import org.bukkit.Material;

public enum OBAchievement {

    FIRST_BLOCK("Naissance", "Casse ton premier bloc magique", Material.GRASS_BLOCK, 1, AchType.BLOCKS_BROKEN, 100),
    CENTURION("Centurion", "Casse 100 blocs magiques", Material.STONE, 100, AchType.BLOCKS_BROKEN, 300),
    EXPLORER("Explorateur", "Casse 1 000 blocs magiques", Material.IRON_PICKAXE, 1000, AchType.BLOCKS_BROKEN, 1000),
    LEGEND("Légende", "Casse 10 000 blocs magiques", Material.DIAMOND_PICKAXE, 10000, AchType.BLOCKS_BROKEN, 8000),
    IMMORTAL("Immortel", "Casse 100 000 blocs magiques", Material.NETHERITE_PICKAXE, 100000, AchType.BLOCKS_BROKEN, 75000),

    FIRST_KILL("Premier Sang", "Tue ton premier mob", Material.BONE, 1, AchType.MOBS_KILLED, 150),
    HUNTER("Chasseur", "Tue 100 mobs", Material.IRON_SWORD, 100, AchType.MOBS_KILLED, 750),
    SLAYER("Massacreur", "Tue 1 000 mobs", Material.DIAMOND_SWORD, 1000, AchType.MOBS_KILLED, 5000),
    DEMON("Démon", "Tue 10 000 mobs", Material.NETHERITE_SWORD, 10000, AchType.MOBS_KILLED, 40000),

    FIRST_COINS("Premières Pièces", "Gagne 100 pièces", Material.GOLD_NUGGET, 100, AchType.COINS_EARNED, 0),
    RICH("Riche", "Gagne 10 000 pièces au total", Material.GOLD_INGOT, 10000, AchType.COINS_EARNED, 1000),
    TYCOON("Tycoon", "Gagne 100 000 pièces au total", Material.GOLD_BLOCK, 100000, AchType.COINS_EARNED, 8000),
    MILLIONAIRE("Millionnaire", "Gagne 1 000 000 pièces au total", Material.BEACON, 1000000, AchType.COINS_EARNED, 75000),

    FOREST("Forêt", "Atteins la phase Forêt", Material.OAK_LOG, 1, AchType.PHASE_UNLOCKED, 300),
    DESERT("Désert", "Atteins la phase Désert", Material.SAND, 1, AchType.PHASE_UNLOCKED, 600),
    NETHER_PHASE("Nether", "Atteins la phase Nether", Material.NETHERRACK, 1, AchType.PHASE_UNLOCKED, 2000),
    END_PHASE("End", "Atteins la phase End", Material.END_STONE, 1, AchType.PHASE_UNLOCKED, 8000),

    FIRST_PRESTIGE("Prestige I", "Effectue ton premier prestige", Material.NETHER_STAR, 1, AchType.PRESTIGE, 5000),
    PRESTIGE_V("Prestige V", "Atteins le prestige 5", Material.BEACON, 5, AchType.PRESTIGE, 30000),
    PRESTIGE_GODLIKE("Prestige X", "Atteins le prestige maximum (10)", Material.END_CRYSTAL, 10, AchType.PRESTIGE, 150000),

    LUCKY_ONE("Chanceux", "Déclenche un Lucky Event", Material.EMERALD, 1, AchType.LUCKY_EVENTS, 500),
    LUCKY_FIVE("Très Chanceux", "Déclenche 5 Lucky Events", Material.NETHER_STAR, 5, AchType.LUCKY_EVENTS, 3000),

    SPAWNER_FOUND("Éleveur", "Obtiens un spawner depuis le OneBlock", Material.SPAWNER, 1, AchType.SPAWNERS_FOUND, 2000),
    SPAWNER_MASTER("Maître Éleveur", "Obtiens 5 spawners depuis le OneBlock", Material.SPAWNER, 5, AchType.SPAWNERS_FOUND, 15000),

    SKILL_10("Apprenti", "Atteins le niveau 10 dans une compétence", Material.BOOK, 10, AchType.MAX_SKILL, 1000),
    SKILL_MAX("Maître de Compétence", "Atteins le niveau maximum dans une compétence", Material.ENCHANTED_BOOK, 50, AchType.MAX_SKILL, 25000),

    UPGRADE_ONE("Amélioration", "Achète ta première amélioration", Material.ANVIL, 1, AchType.UPGRADES_BOUGHT, 200),
    UPGRADE_MASTER("Maître des Améliorations", "Achète 20 améliorations", Material.ANVIL, 20, AchType.UPGRADES_BOUGHT, 5000),

    COLLECTION_ONE("Collectionneur", "Atteins le premier palier d'une collection", Material.CHEST, 1, AchType.COLLECTION_MILESTONES, 500),
    COLLECTION_MASTER("Maître Collectionneur", "Atteins 25 paliers de collection", Material.ENDER_CHEST, 25, AchType.COLLECTION_MILESTONES, 25000);

    public enum AchType {
        BLOCKS_BROKEN, MOBS_KILLED, COINS_EARNED, PHASE_UNLOCKED,
        PRESTIGE, LUCKY_EVENTS, SPAWNERS_FOUND, MAX_SKILL,
        UPGRADES_BOUGHT, COLLECTION_MILESTONES
    }

    private final String displayName;
    private final String description;
    private final Material icon;
    private final long threshold;
    private final AchType type;
    private final int reward;

    OBAchievement(String displayName, String description, Material icon,
                  long threshold, AchType type, int reward) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.threshold = threshold;
        this.type = type;
        this.reward = reward;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public long getThreshold() { return threshold; }
    public AchType getType() { return type; }
    public int getReward() { return reward; }
}
