package com.astraland.oneblock.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager {

    public enum Stat {
        BLOCKS_BROKEN, MOBS_KILLED, COINS_EARNED, LUCKY_EVENTS,
        SPAWNERS_FOUND, ACHIEVEMENTS, DAILY_COMPLETED, WEEKLY_COMPLETED,
        UPGRADES_BOUGHT, COLLECTION_MILESTONES_REACHED, PRESTIGE_DONE
    }

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, Map<Stat, Long>> stats = new HashMap<>();

    public PlayerStatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player-stats.yml");
        load();
    }

    public long get(UUID uuid, Stat stat) {
        return stats.getOrDefault(uuid, Map.of()).getOrDefault(stat, 0L);
    }

    public void add(UUID uuid, Stat stat, long amount) {
        stats.computeIfAbsent(uuid, k -> new HashMap<>()).merge(stat, amount, Long::sum);
        save();
    }

    public void increment(UUID uuid, Stat stat) { add(uuid, stat, 1); }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("players") == null) return;
        for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<Stat, Long> map = new HashMap<>();
                for (Stat s : Stat.values()) {
                    long val = config.getLong("players." + uuidStr + "." + s.name(), 0);
                    if (val > 0) map.put(s, val);
                }
                stats.put(uuid, map);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        stats.forEach((uuid, map) ->
            map.forEach((s, val) -> config.set("players." + uuid + "." + s.name(), val)));
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Erreur sauvegarde stats: " + e.getMessage());
        }
    }
}
