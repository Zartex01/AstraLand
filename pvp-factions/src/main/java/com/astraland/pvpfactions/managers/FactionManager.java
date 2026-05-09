package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.database.DatabaseManager;
import com.astraland.pvpfactions.models.Faction;
import com.astraland.pvpfactions.models.FactionRole;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.util.*;

public class FactionManager {

    private final PvpFactions plugin;
    private final DatabaseManager db;

    private final Map<String, Faction> factions = new LinkedHashMap<>();
    private final Map<UUID, String> playerFaction = new HashMap<>();
    private final Map<UUID, Boolean> factionChat = new HashMap<>();
    private final Set<UUID> autoclaimPlayers = new HashSet<>();

    public FactionManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        loadAll();
    }

    // ─── CHARGEMENT ──────────────────────────────────────────────────────────

    private void loadAll() {
        try {
            // Factions de base
            ResultSet rs = db.query("SELECT * FROM factions");
            while (rs.next()) {
                String name = rs.getString("name");
                UUID leader = UUID.fromString(rs.getString("leader_uuid"));
                Faction f = new Faction(name, leader);
                f.setTag(rs.getString("tag"));
                f.setDescription(rs.getString("description"));
                f.setMotd(rs.getString("motd"));
                f.setOpen(rs.getInt("open") == 1);
                f.setPower(rs.getDouble("power"));
                factions.put(name.toLowerCase(), f);
            }
            rs.close();

            // Membres
            rs = db.query("SELECT * FROM faction_members");
            while (rs.next()) {
                String fname = rs.getString("faction_name").toLowerCase();
                Faction f = factions.get(fname);
                if (f == null) continue;
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                FactionRole role = FactionRole.valueOf(rs.getString("role"));
                f.getMembers().put(uuid, role);
                playerFaction.put(uuid, fname);
            }
            rs.close();

            // Alliés
            rs = db.query("SELECT * FROM faction_allies");
            while (rs.next()) {
                Faction f = factions.get(rs.getString("faction_name").toLowerCase());
                if (f != null) f.getAllies().add(rs.getString("ally_name").toLowerCase());
            }
            rs.close();

            // Ennemis
            rs = db.query("SELECT * FROM faction_enemies");
            while (rs.next()) {
                Faction f = factions.get(rs.getString("faction_name").toLowerCase());
                if (f != null) f.getEnemies().add(rs.getString("enemy_name").toLowerCase());
            }
            rs.close();

            // Claims
            rs = db.query("SELECT * FROM faction_claims");
            while (rs.next()) {
                Faction f = factions.get(rs.getString("faction_name").toLowerCase());
                if (f != null) f.getClaims().add(rs.getString("claim_key"));
            }
            rs.close();

            // Homes
            rs = db.query("SELECT * FROM faction_home");
            while (rs.next()) {
                Faction f = factions.get(rs.getString("faction_name").toLowerCase());
                if (f == null) continue;
                Location loc = DatabaseManager.rsToLocation(rs);
                if (loc != null) f.setHome(loc);
            }
            rs.close();

            // Warps
            rs = db.query("SELECT * FROM faction_warps");
            while (rs.next()) {
                Faction f = factions.get(rs.getString("faction_name").toLowerCase());
                if (f == null) continue;
                Location loc = DatabaseManager.rsToLocation(rs);
                if (loc != null) f.getWarps().put(rs.getString("warp_name"), loc);
            }
            rs.close();

            plugin.getLogger().info("[DB] " + factions.size() + " faction(s) chargée(s).");
        } catch (Exception e) {
            plugin.getLogger().severe("[DB] Erreur lors du chargement des factions : " + e.getMessage());
        }
    }

    // ─── OPÉRATIONS ──────────────────────────────────────────────────────────

    public Faction createFaction(String name, UUID leader) {
        Faction f = new Faction(name, leader);
        factions.put(name.toLowerCase(), f);
        playerFaction.put(leader, name.toLowerCase());
        db.upsertFaction(name, f.getTag(), f.getDescription(), f.getMotd(), f.isOpen(), f.getPower(), leader.toString());
        db.upsertMember(name, leader.toString(), FactionRole.LEADER.name());
        return f;
    }

    public void disbandFaction(Faction f) {
        for (UUID uuid : f.getMembers().keySet()) {
            playerFaction.remove(uuid);
            factionChat.remove(uuid);
            autoclaimPlayers.remove(uuid);
        }
        factions.remove(f.getName().toLowerCase());
        db.deleteFaction(f.getName());
    }

    public void saveFaction(Faction f) {
        db.upsertFaction(f.getName(), f.getTag(), f.getDescription(), f.getMotd(),
                f.isOpen(), f.getPower(), f.getLeader().toString());
    }

    public void joinFaction(Faction f, UUID uuid) {
        f.addMember(uuid);
        playerFaction.put(uuid, f.getName().toLowerCase());
        db.upsertMember(f.getName(), uuid.toString(), FactionRole.MEMBER.name());
    }

    public void leaveFaction(Faction f, UUID uuid) {
        f.removeMember(uuid);
        playerFaction.remove(uuid);
        factionChat.remove(uuid);
        autoclaimPlayers.remove(uuid);
        db.deleteMember(f.getName(), uuid.toString());
    }

    public void promoteMember(Faction f, UUID uuid) {
        f.promote(uuid);
        db.upsertMember(f.getName(), uuid.toString(), f.getMembers().get(uuid).name());
    }

    public void demoteMember(Faction f, UUID uuid) {
        f.demote(uuid);
        db.upsertMember(f.getName(), uuid.toString(), f.getMembers().get(uuid).name());
    }

    public void setLeader(Faction f, UUID oldLeader, UUID newLeader) {
        f.getMembers().put(oldLeader, FactionRole.OFFICER);
        f.getMembers().put(newLeader, FactionRole.LEADER);
        f.setLeader(newLeader);
        db.upsertFaction(f.getName(), f.getTag(), f.getDescription(), f.getMotd(), f.isOpen(), f.getPower(), newLeader.toString());
        db.upsertMember(f.getName(), oldLeader.toString(), FactionRole.OFFICER.name());
        db.upsertMember(f.getName(), newLeader.toString(), FactionRole.LEADER.name());
    }

    public void addAlly(Faction f, Faction target) {
        f.getAllies().add(target.getName().toLowerCase());
        f.getEnemies().remove(target.getName().toLowerCase());
        db.setAlly(f.getName(), target.getName().toLowerCase());
        db.removeEnemy(f.getName(), target.getName().toLowerCase());
    }

    public void addEnemy(Faction f, Faction target) {
        f.getEnemies().add(target.getName().toLowerCase());
        f.getAllies().remove(target.getName().toLowerCase());
        db.setEnemy(f.getName(), target.getName().toLowerCase());
        db.removeAlly(f.getName(), target.getName().toLowerCase());
    }

    public void setNeutral(Faction f, Faction target) {
        f.getAllies().remove(target.getName().toLowerCase());
        f.getEnemies().remove(target.getName().toLowerCase());
        db.removeAlly(f.getName(), target.getName().toLowerCase());
        db.removeEnemy(f.getName(), target.getName().toLowerCase());
    }

    public void addClaim(Faction f, Chunk chunk) {
        String key = chunkKey(chunk);
        f.getClaims().add(key);
        db.addClaim(f.getName(), key);
    }

    public void removeClaim(Faction f, Chunk chunk) {
        String key = chunkKey(chunk);
        f.getClaims().remove(key);
        db.removeClaim(key);
    }

    public void removeAllClaims(Faction f) {
        f.getClaims().clear();
        db.removeAllClaims(f.getName());
    }

    public void setHome(Faction f, Location loc) {
        f.setHome(loc);
        db.setHome(f.getName(), loc);
    }

    public void setWarp(Faction f, String warpName, Location loc) {
        f.getWarps().put(warpName.toLowerCase(), loc);
        db.setWarp(f.getName(), warpName.toLowerCase(), loc);
    }

    public void deleteWarp(Faction f, String warpName) {
        f.getWarps().remove(warpName.toLowerCase());
        db.deleteWarp(f.getName(), warpName.toLowerCase());
    }

    public void updatePower(Faction f) {
        db.upsertFaction(f.getName(), f.getTag(), f.getDescription(), f.getMotd(), f.isOpen(), f.getPower(), f.getLeader().toString());
    }

    // ─── CHAT / AUTOCLAIM ────────────────────────────────────────────────────

    public boolean isFactionChat(UUID uuid) { return factionChat.getOrDefault(uuid, false); }
    public void toggleFactionChat(UUID uuid) { factionChat.put(uuid, !factionChat.getOrDefault(uuid, false)); }
    public boolean isAutoclaiming(UUID uuid) { return autoclaimPlayers.contains(uuid); }
    public void toggleAutoclaim(UUID uuid) {
        if (autoclaimPlayers.contains(uuid)) autoclaimPlayers.remove(uuid);
        else autoclaimPlayers.add(uuid);
    }

    // ─── GETTERS ─────────────────────────────────────────────────────────────

    public Faction getFaction(String name) { return name == null ? null : factions.get(name.toLowerCase()); }
    public Faction getPlayerFaction(UUID uuid) {
        String name = playerFaction.get(uuid);
        return name == null ? null : factions.get(name);
    }
    public boolean hasPlayerFaction(UUID uuid) { return playerFaction.containsKey(uuid); }
    public boolean factionExists(String name) { return factions.containsKey(name.toLowerCase()); }
    public Collection<Faction> getAllFactions() { return factions.values(); }

    public Faction getFactionByClaim(Chunk chunk) {
        String key = chunkKey(chunk);
        for (Faction f : factions.values()) if (f.getClaims().contains(key)) return f;
        return null;
    }

    private String chunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    // Compatibilité — appelé à l'arrêt du plugin (plus rien à faire, tout est déjà persisté)
    public void saveAll() {}
}
