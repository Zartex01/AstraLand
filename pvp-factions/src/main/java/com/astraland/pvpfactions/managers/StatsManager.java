package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.database.DatabaseManager;

import java.sql.ResultSet;
import java.util.*;

public class StatsManager {

    private final PvpFactions plugin;
    private final DatabaseManager db;

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Map<UUID, Integer> currentStreak = new HashMap<>();
    private final Map<UUID, Integer> bestStreak = new HashMap<>();

    public StatsManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        loadAll();
    }

    private void loadAll() {
        try {
            ResultSet rs = db.query("SELECT * FROM player_stats");
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                kills.put(uuid, rs.getInt("kills"));
                deaths.put(uuid, rs.getInt("deaths"));
                currentStreak.put(uuid, rs.getInt("current_streak"));
                bestStreak.put(uuid, rs.getInt("best_streak"));
            }
            rs.close();
            plugin.getLogger().info("[DB] " + kills.size() + " profil(s) de stats chargé(s).");
        } catch (Exception e) {
            plugin.getLogger().severe("[DB] Erreur chargement stats : " + e.getMessage());
        }
    }

    public void addKill(UUID uuid) {
        int streak = currentStreak.merge(uuid, 1, Integer::sum);
        kills.merge(uuid, 1, Integer::sum);
        int best = bestStreak.getOrDefault(uuid, 0);
        if (streak > best) bestStreak.put(uuid, streak);
        db.incrementKill(uuid.toString(), streak, bestStreak.get(uuid));
    }

    public void addDeath(UUID uuid) {
        deaths.merge(uuid, 1, Integer::sum);
        currentStreak.put(uuid, 0);
        db.incrementDeath(uuid.toString());
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

    // Appelé à l'arrêt — tout est déjà persisté en temps réel
    public void saveAll() {}
}
