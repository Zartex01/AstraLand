package com.astraland.skyblock.quests;

import com.astraland.skyblock.Skyblock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DailyQuestManager {

    private final Skyblock plugin;
    private final File     dataFile;
    private YamlConfiguration data;

    private final List<DailyQuest> questPool = new ArrayList<>();
    private final Map<UUID, List<DailyQuest>> playerQuests    = new HashMap<>();
    private final Map<String, Integer>        progress        = new HashMap<>();
    private final Set<String>                 completed       = new HashSet<>();

    private static final int QUESTS_PER_DAY = 5;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public DailyQuestManager(Skyblock plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "quests.yml");
        buildPool();
        load();
        scheduleMidnightReset();
    }

    // ─── Génération du pool ───────────────────────────────────────────────────

    private void buildPool() {
        questPool.add(new DailyQuest("mine_100",   DailyQuest.Type.BREAK_BLOCKS, "&7Mineur débutant",      "Casse 100 blocs",              100,   150,  50, Material.IRON_PICKAXE));
        questPool.add(new DailyQuest("mine_500",   DailyQuest.Type.BREAK_BLOCKS, "&7Mineur expérimenté",   "Casse 500 blocs",              500,   500, 150, Material.DIAMOND_PICKAXE));
        questPool.add(new DailyQuest("mine_2000",  DailyQuest.Type.BREAK_BLOCKS, "&7Mineur chevronné",     "Casse 2 000 blocs",           2000,  1500, 400, Material.NETHERITE_PICKAXE));
        questPool.add(new DailyQuest("place_100",  DailyQuest.Type.PLACE_BLOCKS, "&aBâtisseur junior",     "Pose 100 blocs",               100,   150,  50, Material.BRICKS));
        questPool.add(new DailyQuest("place_500",  DailyQuest.Type.PLACE_BLOCKS, "&aBâtisseur confirmé",   "Pose 500 blocs",               500,   500, 150, Material.STONE_BRICKS));
        questPool.add(new DailyQuest("place_2000", DailyQuest.Type.PLACE_BLOCKS, "&aBâtisseur expert",     "Pose 2 000 blocs",            2000,  1500, 400, Material.POLISHED_DEEPSLATE));
        questPool.add(new DailyQuest("earn_500",   DailyQuest.Type.EARN_MONEY,   "&6Petit entrepreneur",   "Gagne 500 pièces",             500,   200,  75, Material.GOLD_NUGGET));
        questPool.add(new DailyQuest("earn_2000",  DailyQuest.Type.EARN_MONEY,   "&6Commerçant",           "Gagne 2 000 pièces",          2000,   600, 200, Material.GOLD_INGOT));
        questPool.add(new DailyQuest("earn_10000", DailyQuest.Type.EARN_MONEY,   "&6Homme d'affaires",     "Gagne 10 000 pièces",        10000,  2000, 500, Material.GOLD_BLOCK));
        questPool.add(new DailyQuest("kill_20",    DailyQuest.Type.KILL_MOBS,    "&cChasseur",             "Tue 20 monstres",               20,   300, 100, Material.BONE));
        questPool.add(new DailyQuest("kill_100",   DailyQuest.Type.KILL_MOBS,    "&cGuerrier",             "Tue 100 monstres",             100,   800, 250, Material.IRON_SWORD));
        questPool.add(new DailyQuest("kill_500",   DailyQuest.Type.KILL_MOBS,    "&cÉradicateur",          "Tue 500 monstres",             500,  2000, 600, Material.DIAMOND_SWORD));
        questPool.add(new DailyQuest("fish_10",    DailyQuest.Type.FISH,         "&bPêcheur amateur",      "Pêche 10 poissons",             10,   200,  80, Material.COD));
        questPool.add(new DailyQuest("fish_50",    DailyQuest.Type.FISH,         "&bPêcheur expérimenté",  "Pêche 50 poissons",             50,   700, 200, Material.SALMON));
        questPool.add(new DailyQuest("grow_30",    DailyQuest.Type.GROW_CROPS,   "&aAgriculteur",          "Récolte 30 cultures",           30,   250,  90, Material.WHEAT));
        questPool.add(new DailyQuest("grow_150",   DailyQuest.Type.GROW_CROPS,   "&aGrand agriculteur",    "Récolte 150 cultures",         150,   800, 250, Material.HAY_BLOCK));
    }

    // ─── Attribution des quêtes du jour ──────────────────────────────────────

    public List<DailyQuest> getPlayerQuests(UUID uuid) {
        if (!playerQuests.containsKey(uuid) || !hasTodayQuests(uuid)) {
            assignDailyQuests(uuid);
        }
        return playerQuests.get(uuid);
    }

    private boolean hasTodayQuests(UUID uuid) {
        String key = uuid + ".date";
        String stored = data.getString(key, "");
        return LocalDate.now().format(FMT).equals(stored);
    }

    private void assignDailyQuests(UUID uuid) {
        List<DailyQuest> pool = new ArrayList<>(questPool);
        Collections.shuffle(pool, new Random(uuid.hashCode() + LocalDate.now().toEpochDay()));
        List<DailyQuest> assigned = pool.subList(0, Math.min(QUESTS_PER_DAY, pool.size()));
        playerQuests.put(uuid, new ArrayList<>(assigned));

        data.set(uuid + ".date", LocalDate.now().format(FMT));
        List<String> ids = assigned.stream().map(DailyQuest::getId).toList();
        data.set(uuid + ".quests", ids);

        for (DailyQuest q : assigned) {
            String pKey = progressKey(uuid, q.getId());
            if (!progress.containsKey(pKey)) progress.put(pKey, 0);
        }
        save();
    }

    // ─── Progression ─────────────────────────────────────────────────────────

    public int getProgress(UUID uuid, String questId) {
        return progress.getOrDefault(progressKey(uuid, questId), 0);
    }

    public boolean isCompleted(UUID uuid, String questId) {
        return completed.contains(progressKey(uuid, questId));
    }

    public void addProgress(UUID uuid, DailyQuest.Type type, int amount) {
        List<DailyQuest> quests = getPlayerQuests(uuid);
        for (DailyQuest q : quests) {
            if (q.getType() != type) continue;
            if (isCompleted(uuid, q.getId())) continue;
            String key = progressKey(uuid, q.getId());
            int current = progress.getOrDefault(key, 0);
            int newVal  = current + amount;
            progress.put(key, newVal);
            data.set(uuid + ".progress." + q.getId(), newVal);

            if (newVal >= q.getTarget()) {
                // Auto-complétion
                completed.add(key);
                data.set(uuid + ".completed." + q.getId(), true);
                plugin.getEconomyManager().addBalance(uuid, q.getRewardMoney());
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.giveExpLevels(q.getRewardXP() / 50);
                    p.giveExp(q.getRewardXP() % 50 * 7);
                    p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
                    p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        "&8[&e&l✦ Quête&8] &a✔ Quête &e" + q.getDisplayName() +
                        " &acomplétée ! &6+" + q.getRewardMoney() + " $ &a+ &b" + q.getRewardXP() + " XP"));
                }
            }
        }
        save();
    }

    public int countCompleted(UUID uuid) {
        List<DailyQuest> quests = getPlayerQuests(uuid);
        int count = 0;
        for (DailyQuest q : quests) if (isCompleted(uuid, q.getId())) count++;
        return count;
    }

    // ─── Reset à minuit ───────────────────────────────────────────────────────

    private void scheduleMidnightReset() {
        long now       = System.currentTimeMillis();
        java.time.LocalDateTime nextMidnight = LocalDate.now().plusDays(1).atStartOfDay();
        long msUntilMidnight = java.time.Duration.between(
            java.time.LocalDateTime.now(), nextMidnight).toMillis();
        long ticksUntilMidnight = msUntilMidnight / 50;
        long ticksPerDay = 20L * 60 * 60 * 24;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            playerQuests.clear();
            for (String key : data.getKeys(false)) {
                try { UUID uuid = UUID.fromString(key); assignDailyQuests(uuid); } catch (Exception ignored) {}
            }
            plugin.getLogger().info("[Quêtes] Reset quotidien effectué !");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.isInPluginWorld(p))
                    p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        "&8[&e&l✦ Quêtes&8] &e⭐ Nouvelles quêtes disponibles ! &7Tape &e/is quetes"));
            }
        }, ticksUntilMidnight, ticksPerDay);
    }

    // ─── Persistance ──────────────────────────────────────────────────────────

    private String progressKey(UUID uuid, String questId) { return uuid + ":" + questId; }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) try { dataFile.createNewFile(); } catch (Exception ignored) {}
        data = YamlConfiguration.loadConfiguration(dataFile);
        for (String uuidStr : data.getKeys(false)) {
            try {
                UUID uuid  = UUID.fromString(uuidStr);
                String date = data.getString(uuidStr + ".date", "");
                List<String> ids = data.getStringList(uuidStr + ".quests");
                List<DailyQuest> assigned = new ArrayList<>();
                for (String id : ids) {
                    questPool.stream().filter(q -> q.getId().equals(id)).findFirst().ifPresent(assigned::add);
                }
                if (!assigned.isEmpty()) playerQuests.put(uuid, assigned);

                for (DailyQuest q : assigned) {
                    int prog = data.getInt(uuidStr + ".progress." + q.getId(), 0);
                    progress.put(progressKey(uuid, q.getId()), prog);
                    if (data.getBoolean(uuidStr + ".completed." + q.getId(), false))
                        completed.add(progressKey(uuid, q.getId()));
                }
            } catch (Exception ignored) {}
        }
    }

    private void save() {
        try { data.save(dataFile); } catch (Exception e) { e.printStackTrace(); }
    }
}
