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
    private File dataFile;

    public StatsManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    public void addKill(UUID uuid) { kills.merge(uuid, 1, Integer::sum); }
    public void addDeath(UUID uuid) { deaths.merge(uuid, 1, Integer::sum); }
    public int getKills(UUID uuid) { return kills.getOrDefault(uuid, 0); }
    public int getDeaths(UUID uuid) { return deaths.getOrDefault(uuid, 0); }

    public double getKD(UUID uuid) {
        int d = getDeaths(uuid);
        return d == 0 ? getKills(uuid) : (double) getKills(uuid) / d;
    }

    public List<Map.Entry<UUID, Integer>> getTopKills(int limit) {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(kills.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (UUID uuid : kills.keySet()) {
            cfg.set("stats." + uuid + ".kills", kills.get(uuid));
            cfg.set("stats." + uuid + ".deaths", deaths.getOrDefault(uuid, 0));
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
                kills.put(uuid, cfg.getInt("stats." + uuidStr + ".kills", 0));
                deaths.put(uuid, cfg.getInt("stats." + uuidStr + ".deaths", 0));
            } catch (Exception ignored) {}
        }
    }
}
