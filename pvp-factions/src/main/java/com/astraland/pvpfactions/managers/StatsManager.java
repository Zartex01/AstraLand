package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatsManager {

    private final PvpFactions plugin;
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Map<UUID, Integer> currentStreak = new HashMap<>();
    private final Map<UUID, Integer> bestStreak = new HashMap<>();
    private File dataFile;

    public StatsManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    public void addKill(UUID uuid) {
        kills.merge(uuid, 1, Integer::sum);
        int streak = currentStreak.merge(uuid, 1, Integer::sum);
        int best = bestStreak.getOrDefault(uuid, 0);
        if (streak > best) bestStreak.put(uuid, streak);
    }

    public void addDeath(UUID uuid) {
        deaths.merge(uuid, 1, Integer::sum);
        currentStreak.put(uuid, 0);
    }

    public int getKills(UUID uuid) { return kills.getOrDefault(uuid, 0); }
    public int getDeaths(UUID uuid) { return deaths.getOrDefault(uuid, 0); }
    public int getCurrentStreak(UUID uuid) { return currentStreak.getOrDefault(uuid, 0); }
    public int getBestStreak(UUID uuid) { return bestStreak.getOrDefault(uuid, 0); }

    public double getKD(UUID uuid) {
        int d = getDeaths(uuid);
        int k = getKills(uuid);
        return d == 0 ? k : (double) k / d;
    }

    public List<Map.Entry<UUID, Integer>> getTopKills(int limit) {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(kills.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<Map.Entry<UUID, Integer>> getTopStreaks(int limit) {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(bestStreak.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (UUID uuid : kills.keySet()) {
            String path = "stats." + uuid;
            cfg.set(path + ".kills", kills.get(uuid));
            cfg.set(path + ".deaths", deaths.getOrDefault(uuid, 0));
            cfg.set(path + ".streak", currentStreak.getOrDefault(uuid, 0));
            cfg.set(path + ".bestStreak", bestStreak.getOrDefault(uuid, 0));
        }
        try { cfg.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("stats") == null) return;
        for (String uuidStr : cfg.getConfigurationSection("stats").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String path = "stats." + uuidStr;
                kills.put(uuid, cfg.getInt(path + ".kills", 0));
                deaths.put(uuid, cfg.getInt(path + ".deaths", 0));
                currentStreak.put(uuid, cfg.getInt(path + ".streak", 0));
                bestStreak.put(uuid, cfg.getInt(path + ".bestStreak", 0));
            } catch (Exception ignored) {}
        }
    }
}
