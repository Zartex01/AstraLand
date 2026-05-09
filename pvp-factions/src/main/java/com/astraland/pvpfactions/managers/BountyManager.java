package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyManager {

    private final PvpFactions plugin;
    private final Map<UUID, Map<UUID, Integer>> bountyDetails = new HashMap<>();
    private File dataFile;

    public BountyManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "bounties.yml");
        load();
    }

    public void placeBounty(UUID setter, UUID target, int amount) {
        bountyDetails.computeIfAbsent(target, k -> new HashMap<>())
            .merge(setter, amount, Integer::sum);
        save();
    }

    public int getTotalBounty(UUID target) {
        Map<UUID, Integer> map = bountyDetails.get(target);
        if (map == null) return 0;
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean hasBounty(UUID target) {
        return getTotalBounty(target) > 0;
    }

    public int claimBounty(UUID killer, UUID victim) {
        Map<UUID, Integer> map = bountyDetails.remove(victim);
        if (map == null) return 0;
        int total = map.values().stream().mapToInt(Integer::intValue).sum();
        save();
        return total;
    }

    public List<Map.Entry<UUID, Integer>> getTopBounties(int limit) {
        Map<UUID, Integer> totals = new HashMap<>();
        for (Map.Entry<UUID, Map<UUID, Integer>> e : bountyDetails.entrySet()) {
            int total = e.getValue().values().stream().mapToInt(Integer::intValue).sum();
            if (total > 0) totals.put(e.getKey(), total);
        }
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(totals.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list.subList(0, Math.min(limit, list.size()));
    }

    public Map<UUID, Integer> getBountyDetails(UUID target) {
        return bountyDetails.getOrDefault(target, new HashMap<>());
    }

    private void save() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Map<UUID, Integer>> e : bountyDetails.entrySet()) {
            for (Map.Entry<UUID, Integer> s : e.getValue().entrySet()) {
                cfg.set("bounties." + e.getKey() + "." + s.getKey(), s.getValue());
            }
        }
        try { cfg.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("bounties") == null) return;
        for (String targetStr : cfg.getConfigurationSection("bounties").getKeys(false)) {
            try {
                UUID target = UUID.fromString(targetStr);
                org.bukkit.configuration.ConfigurationSection sec = cfg.getConfigurationSection("bounties." + targetStr);
                if (sec == null) continue;
                for (String setterStr : sec.getKeys(false)) {
                    try {
                        UUID setter = UUID.fromString(setterStr);
                        int amount = cfg.getInt("bounties." + targetStr + "." + setterStr, 0);
                        bountyDetails.computeIfAbsent(target, k -> new HashMap<>()).put(setter, amount);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }
    }
}
