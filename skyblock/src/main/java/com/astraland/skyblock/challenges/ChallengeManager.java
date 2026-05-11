package com.astraland.skyblock.challenges;

import com.astraland.skyblock.models.Island;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ChallengeManager {

    private final JavaPlugin plugin;
    private final File dataFile;
    private final List<Challenge> challenges = new ArrayList<>();
    private final Map<UUID, Set<String>> completed = new HashMap<>();

    public ChallengeManager(JavaPlugin plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "challenges.yml");
        buildChallenges();
        load();
    }

    private void buildChallenges() {
        // ── PROGRESSION ────────────────────────────────────────────────────────
        add("level_1",   "&aÎle Niveau 1",      "Atteindre le niveau 1 de ton île",
            Material.OAK_SAPLING,  Challenge.Type.ISLAND_LEVEL,   1,   500,  50,  Challenge.Category.PROGRESSION);
        add("level_3",   "&aÎle Niveau 3",      "Atteindre le niveau 3 de ton île",
            Material.OAK_LOG,      Challenge.Type.ISLAND_LEVEL,   3,  2000, 150,  Challenge.Category.PROGRESSION);
        add("level_5",   "&6Île Niveau 5",      "Atteindre le niveau 5 de ton île",
            Material.IRON_BLOCK,   Challenge.Type.ISLAND_LEVEL,   5,  5000, 300,  Challenge.Category.PROGRESSION);
        add("level_8",   "&bÎle Niveau 8",      "Atteindre le niveau 8 de ton île",
            Material.GOLD_BLOCK,   Challenge.Type.ISLAND_LEVEL,   8, 15000, 600,  Challenge.Category.PROGRESSION);
        add("level_10",  "&5Île Niveau 10",     "Atteindre le niveau maximum (10) !",
            Material.DIAMOND_BLOCK,Challenge.Type.ISLAND_LEVEL,  10, 50000,1000,  Challenge.Category.PROGRESSION);
        add("gen_1",     "&aGénérateur Nv.1",   "Améliorer le générateur au niveau 1",
            Material.STONE,        Challenge.Type.GENERATOR_LEVEL, 1,  300,  30,  Challenge.Category.PROGRESSION);
        add("gen_4",     "&6Générateur Nv.4",   "Améliorer le générateur au niveau 4",
            Material.IRON_ORE,     Challenge.Type.GENERATOR_LEVEL, 4, 5000, 200,  Challenge.Category.PROGRESSION);
        add("gen_7",     "&5Générateur Nv.7",   "Atteindre le niveau maximum du générateur !",
            Material.EMERALD_ORE,  Challenge.Type.GENERATOR_LEVEL, 7,20000, 800,  Challenge.Category.PROGRESSION);

        // ── ÉCONOMIE ───────────────────────────────────────────────────────────
        add("bal_1k",    "&aRiche débutant",    "Accumuler 1 000 pièces",
            Material.GOLD_NUGGET,  Challenge.Type.BALANCE,      1000,   200,  20,  Challenge.Category.ECONOMIE);
        add("bal_10k",   "&6Bon gestionnaire",  "Accumuler 10 000 pièces",
            Material.GOLD_INGOT,   Challenge.Type.BALANCE,     10000,  1000, 100,  Challenge.Category.ECONOMIE);
        add("bal_100k",  "&bMarchand prospère", "Accumuler 100 000 pièces",
            Material.GOLD_BLOCK,   Challenge.Type.BALANCE,    100000,  8000, 400,  Challenge.Category.ECONOMIE);
        add("bal_1m",    "&5Millionnaire",      "Accumuler 1 000 000 pièces !",
            Material.NETHER_STAR,  Challenge.Type.BALANCE,   1000000, 50000,2000,  Challenge.Category.ECONOMIE);
        add("bank_50k",  "&6Banque d'île",      "Déposer 50 000 pièces dans la banque d'île",
            Material.CHEST,        Challenge.Type.BANK_BALANCE, 50000, 5000, 250,  Challenge.Category.ECONOMIE);

        // ── CONSTRUCTION ───────────────────────────────────────────────────────
        add("break_100",   "&aMineur débutant",   "Casser 100 blocs sur ton île",
            Material.WOODEN_PICKAXE, Challenge.Type.BLOCKS_BROKEN,    100,  150,  15, Challenge.Category.CONSTRUCTION);
        add("break_1k",    "&6Mineur actif",       "Casser 1 000 blocs sur ton île",
            Material.STONE_PICKAXE,  Challenge.Type.BLOCKS_BROKEN,   1000,  800,  80, Challenge.Category.CONSTRUCTION);
        add("break_10k",   "&bMineur expert",      "Casser 10 000 blocs sur ton île",
            Material.IRON_PICKAXE,   Challenge.Type.BLOCKS_BROKEN,  10000, 4000, 350, Challenge.Category.CONSTRUCTION);
        add("break_100k",  "&5Maître mineur",      "Casser 100 000 blocs sur ton île !",
            Material.DIAMOND_PICKAXE,Challenge.Type.BLOCKS_BROKEN, 100000,20000,1500, Challenge.Category.CONSTRUCTION);
        add("value_500",   "&aConstructeur",       "Atteindre 500 points de valeur d'île",
            Material.COBBLESTONE,    Challenge.Type.ISLAND_VALUE,    500,  300,  30, Challenge.Category.CONSTRUCTION);
        add("value_10k",   "&6Bâtisseur",          "Atteindre 10 000 points de valeur d'île",
            Material.SMOOTH_STONE,   Challenge.Type.ISLAND_VALUE,  10000, 3000, 200, Challenge.Category.CONSTRUCTION);
        add("value_100k",  "&bArchitecte",         "Atteindre 100 000 points de valeur d'île",
            Material.QUARTZ_BLOCK,   Challenge.Type.ISLAND_VALUE, 100000,15000, 700, Challenge.Category.CONSTRUCTION);
        add("value_1m",    "&5Grand architecte",   "Atteindre 1 000 000 points de valeur !",
            Material.BEACON,         Challenge.Type.ISLAND_VALUE,1000000,75000,3000, Challenge.Category.CONSTRUCTION);

        // ── SOCIAL ─────────────────────────────────────────────────────────────
        add("member_1",  "&aDuo",                 "Avoir 1 membre sur ton île",
            Material.PLAYER_HEAD,  Challenge.Type.MEMBER_COUNT,   1,  500,  50,  Challenge.Category.SOCIAL);
        add("member_3",  "&6Équipe",              "Avoir 3 membres sur ton île",
            Material.PLAYER_HEAD,  Challenge.Type.MEMBER_COUNT,   3, 2000, 150,  Challenge.Category.SOCIAL);
    }

    private void add(String id, String name, String desc, Material icon,
                     Challenge.Type type, long req, int money, int xp, Challenge.Category cat) {
        challenges.add(new Challenge(id, name, desc, icon, type, req, money, xp, cat));
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public List<Challenge> getAllChallenges() { return Collections.unmodifiableList(challenges); }

    public Challenge getById(String id) {
        return challenges.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }

    public boolean isCompleted(UUID islandOwner, String id) {
        return completed.getOrDefault(islandOwner, Collections.emptySet()).contains(id);
    }

    public int countCompleted(UUID islandOwner) {
        return completed.getOrDefault(islandOwner, Collections.emptySet()).size();
    }

    /**
     * Vérifie si la condition d'un défi est remplie pour une île donnée.
     */
    public boolean meetsCondition(UUID islandOwner, Challenge challenge, Island island, int balance) {
        if (island == null) return false;
        return switch (challenge.getType()) {
            case ISLAND_LEVEL     -> island.getLevel()          >= challenge.getRequiredValue();
            case GENERATOR_LEVEL  -> island.getGeneratorLevel() >= challenge.getRequiredValue();
            case BLOCKS_BROKEN    -> island.getBlocksBroken()   >= challenge.getRequiredValue();
            case BALANCE          -> balance                    >= challenge.getRequiredValue();
            case ISLAND_VALUE     -> island.getValue()          >= challenge.getRequiredValue();
            case MEMBER_COUNT     -> (island.getMemberCount())  >= challenge.getRequiredValue();
            case BANK_BALANCE     -> island.getBankBalance()    >= challenge.getRequiredValue();
        };
    }

    /**
     * Marque un défi comme complété et sauvegarde.
     */
    public void complete(UUID islandOwner, String id) {
        completed.computeIfAbsent(islandOwner, k -> new HashSet<>()).add(id);
        save();
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("completed") == null) return;
        for (String uuidStr : cfg.getConfigurationSection("completed").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> ids = cfg.getStringList("completed." + uuidStr);
                completed.put(uuid, new HashSet<>(ids));
            } catch (Exception ignored) {}
        }
    }

    private void save() {
        plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        completed.forEach((uuid, ids) ->
            cfg.set("completed." + uuid.toString(), new ArrayList<>(ids)));
        try { cfg.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }
}
