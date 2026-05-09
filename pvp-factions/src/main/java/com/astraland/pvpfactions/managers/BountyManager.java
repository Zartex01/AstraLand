package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.database.DatabaseManager;

import java.sql.ResultSet;
import java.util.*;

public class BountyManager {

    private final PvpFactions plugin;
    private final DatabaseManager db;

    // Cache en mémoire : target -> (setter -> montant)
    private final Map<UUID, Map<UUID, Integer>> bountyDetails = new HashMap<>();

    public BountyManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        loadAll();
    }

    private void loadAll() {
        try {
            ResultSet rs = db.query("SELECT * FROM bounties");
            while (rs.next()) {
                UUID target = UUID.fromString(rs.getString("target_uuid"));
                UUID setter = UUID.fromString(rs.getString("setter_uuid"));
                int amount = rs.getInt("amount");
                bountyDetails.computeIfAbsent(target, k -> new HashMap<>()).put(setter, amount);
            }
            rs.close();
        } catch (Exception e) {
            plugin.getLogger().severe("[DB] Erreur chargement bounties : " + e.getMessage());
        }
    }

    public void placeBounty(UUID setter, UUID target, int amount) {
        bountyDetails.computeIfAbsent(target, k -> new HashMap<>()).merge(setter, amount, Integer::sum);
        db.upsertBounty(target.toString(), setter.toString(), amount);
    }

    public int getTotalBounty(UUID target) {
        Map<UUID, Integer> map = bountyDetails.get(target);
        return map == null ? 0 : map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean hasBounty(UUID target) { return getTotalBounty(target) > 0; }

    public int claimBounty(UUID killer, UUID victim) {
        Map<UUID, Integer> map = bountyDetails.remove(victim);
        if (map == null) return 0;
        int total = map.values().stream().mapToInt(Integer::intValue).sum();
        db.deleteBountiesOnTarget(victim.toString());
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
}
