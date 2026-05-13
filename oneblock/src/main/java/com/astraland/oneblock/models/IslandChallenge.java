package com.astraland.oneblock.models;

import org.bukkit.Material;

public enum IslandChallenge {

    FIRST_BREAK("first_break", "Premier pas", "Casser ton premier bloc magique",
        Material.GRASS_BLOCK, ChallengeType.BLOCKS_BROKEN, 1, 50),
    BREAK_100("break_100", "Débutant", "Casser 100 blocs magiques",
        Material.STONE, ChallengeType.BLOCKS_BROKEN, 100, 200),
    BREAK_500("break_500", "Explorateur", "Casser 500 blocs magiques",
        Material.COBBLESTONE, ChallengeType.BLOCKS_BROKEN, 500, 750),
    BREAK_1000("break_1000", "Aventurier", "Casser 1 000 blocs magiques",
        Material.IRON_ORE, ChallengeType.BLOCKS_BROKEN, 1000, 1500),
    BREAK_2500("break_2500", "Vétéran", "Casser 2 500 blocs magiques",
        Material.GOLD_ORE, ChallengeType.BLOCKS_BROKEN, 2500, 4000),
    BREAK_5000("break_5000", "Maître OneBlock", "Casser 5 000 blocs magiques",
        Material.DIAMOND_ORE, ChallengeType.BLOCKS_BROKEN, 5000, 10000),

    PHASE_FORET("phase_foret", "Forestier", "Atteindre la phase Forêt",
        Material.OAK_LOG, ChallengeType.PHASE_REACHED, 2, 300),
    PHASE_DESERT("phase_desert", "Déserteur", "Atteindre la phase Désert",
        Material.SAND, ChallengeType.PHASE_REACHED, 3, 600),
    PHASE_NEIGE("phase_neige", "Givré", "Atteindre la phase Neige",
        Material.SNOW_BLOCK, ChallengeType.PHASE_REACHED, 4, 1000),
    PHASE_ENFER("phase_enfer", "Démoniaque", "Atteindre la phase Enfer",
        Material.NETHERRACK, ChallengeType.PHASE_REACHED, 5, 2000),
    PHASE_END("phase_end", "Maître de l'End", "Atteindre la phase End",
        Material.END_STONE, ChallengeType.PHASE_REACHED, 6, 5000),

    KILL_MOBS_10("kill_10", "Chasseur", "Tuer 10 mobs sur ton île",
        Material.BONE, ChallengeType.MOBS_KILLED, 10, 200),
    KILL_MOBS_50("kill_50", "Guerrier", "Tuer 50 mobs sur ton île",
        Material.BLAZE_ROD, ChallengeType.MOBS_KILLED, 50, 750),
    KILL_MOBS_200("kill_200", "Champion", "Tuer 200 mobs sur ton île",
        Material.NETHER_STAR, ChallengeType.MOBS_KILLED, 200, 3000),

    INVITE_MEMBER("invite_member", "Chef d'équipe", "Inviter un joueur sur ton île",
        Material.PLAYER_HEAD, ChallengeType.MEMBERS_INVITED, 1, 500),

    UPGRADE_ONCE("upgrade_once", "Améliorateur", "Acheter une amélioration",
        Material.EXPERIENCE_BOTTLE, ChallengeType.UPGRADES_BOUGHT, 1, 250),
    UPGRADE_MAX("upgrade_max", "Perfectionniste", "Monter une amélioration au niveau max",
        Material.ENCHANTED_BOOK, ChallengeType.UPGRADES_MAX, 1, 2000);

    public enum ChallengeType {
        BLOCKS_BROKEN, PHASE_REACHED, MOBS_KILLED, MEMBERS_INVITED, UPGRADES_BOUGHT, UPGRADES_MAX
    }

    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final ChallengeType type;
    private final long target;
    private final int reward;

    IslandChallenge(String id, String displayName, String description, Material icon,
                    ChallengeType type, long target, int reward) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.target = target;
        this.reward = reward;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public ChallengeType getType() { return type; }
    public long getTarget() { return target; }
    public int getReward() { return reward; }

    public static IslandChallenge fromId(String id) {
        for (IslandChallenge c : values()) if (c.id.equals(id)) return c;
        return null;
    }
}
