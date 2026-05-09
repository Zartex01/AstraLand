package com.astraland.pvpfactions.database;

import com.astraland.pvpfactions.PvpFactions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final PvpFactions plugin;
    private Connection connection;

    public DatabaseManager(PvpFactions plugin) {
        this.plugin = plugin;
    }

    // ─── CONNEXION ────────────────────────────────────────────────────────────

    public void connect() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            File dbFile = new File(plugin.getDataFolder(), "astraland.db");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
                stmt.execute("PRAGMA foreign_keys=ON;");
            }
            createTables();
            plugin.getLogger().info("[DB] Base de données SQLite connectée : " + dbFile.getName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[DB] Impossible de se connecter à la base de données !", e);
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("[DB] Base de données SQLite fermée.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[DB] Erreur à la fermeture.", e);
        }
    }

    public Connection getConnection() { return connection; }

    // ─── CRÉATION DES TABLES ─────────────────────────────────────────────────

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS factions (
                    name        TEXT PRIMARY KEY,
                    tag         TEXT NOT NULL,
                    description TEXT DEFAULT '',
                    motd        TEXT,
                    open        INTEGER DEFAULT 0,
                    power       REAL DEFAULT 10.0,
                    leader_uuid TEXT NOT NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faction_members (
                    faction_name TEXT NOT NULL,
                    player_uuid  TEXT NOT NULL,
                    role         TEXT NOT NULL DEFAULT 'MEMBER',
                    PRIMARY KEY (faction_name, player_uuid)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faction_allies (
                    faction_name TEXT NOT NULL,
                    ally_name    TEXT NOT NULL,
                    PRIMARY KEY (faction_name, ally_name)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faction_enemies (
                    faction_name TEXT NOT NULL,
                    enemy_name   TEXT NOT NULL,
                    PRIMARY KEY (faction_name, enemy_name)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faction_claims (
                    faction_name TEXT NOT NULL,
                    claim_key    TEXT NOT NULL,
                    PRIMARY KEY (claim_key)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faction_home (
                    faction_name TEXT PRIMARY KEY,
                    world        TEXT NOT NULL,
                    x REAL, y REAL, z REAL,
                    yaw REAL DEFAULT 0, pitch REAL DEFAULT 0
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS faction_warps (
                    faction_name TEXT NOT NULL,
                    warp_name    TEXT NOT NULL,
                    world        TEXT NOT NULL,
                    x REAL, y REAL, z REAL,
                    yaw REAL DEFAULT 0, pitch REAL DEFAULT 0,
                    PRIMARY KEY (faction_name, warp_name)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    player_uuid     TEXT PRIMARY KEY,
                    kills           INTEGER DEFAULT 0,
                    deaths          INTEGER DEFAULT 0,
                    current_streak  INTEGER DEFAULT 0,
                    best_streak     INTEGER DEFAULT 0
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bounties (
                    target_uuid TEXT NOT NULL,
                    setter_uuid TEXT NOT NULL,
                    amount      INTEGER DEFAULT 0,
                    PRIMARY KEY (target_uuid, setter_uuid)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS kit_cooldowns (
                    player_uuid TEXT NOT NULL,
                    kit_name    TEXT NOT NULL,
                    last_use    INTEGER DEFAULT 0,
                    PRIMARY KEY (player_uuid, kit_name)
                )""");
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    public void execute(String sql, Object... params) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[DB] Erreur SQL : " + sql, e);
        }
    }

    public ResultSet query(String sql, Object... params) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        return ps.executeQuery();
    }

    // ─── FACTIONS ────────────────────────────────────────────────────────────

    public void upsertFaction(String name, String tag, String description, String motd,
                               boolean open, double power, String leaderUuid) {
        execute("""
            INSERT INTO factions (name, tag, description, motd, open, power, leader_uuid)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(name) DO UPDATE SET
                tag=excluded.tag, description=excluded.description,
                motd=excluded.motd, open=excluded.open,
                power=excluded.power, leader_uuid=excluded.leader_uuid
            """, name, tag, description, motd, open ? 1 : 0, power, leaderUuid);
    }

    public void deleteFaction(String name) {
        execute("DELETE FROM factions WHERE name=?", name);
        execute("DELETE FROM faction_members WHERE faction_name=?", name);
        execute("DELETE FROM faction_allies WHERE faction_name=?", name);
        execute("DELETE FROM faction_enemies WHERE faction_name=?", name);
        execute("DELETE FROM faction_claims WHERE faction_name=?", name);
        execute("DELETE FROM faction_home WHERE faction_name=?", name);
        execute("DELETE FROM faction_warps WHERE faction_name=?", name);
    }

    public void upsertMember(String factionName, String playerUuid, String role) {
        execute("""
            INSERT INTO faction_members (faction_name, player_uuid, role) VALUES (?, ?, ?)
            ON CONFLICT(faction_name, player_uuid) DO UPDATE SET role=excluded.role
            """, factionName, playerUuid, role);
    }

    public void deleteMember(String factionName, String playerUuid) {
        execute("DELETE FROM faction_members WHERE faction_name=? AND player_uuid=?", factionName, playerUuid);
    }

    public void setAlly(String factionName, String allyName) {
        execute("INSERT OR IGNORE INTO faction_allies (faction_name, ally_name) VALUES (?, ?)", factionName, allyName);
    }

    public void removeAlly(String factionName, String allyName) {
        execute("DELETE FROM faction_allies WHERE faction_name=? AND ally_name=?", factionName, allyName);
    }

    public void setEnemy(String factionName, String enemyName) {
        execute("INSERT OR IGNORE INTO faction_enemies (faction_name, enemy_name) VALUES (?, ?)", factionName, enemyName);
    }

    public void removeEnemy(String factionName, String enemyName) {
        execute("DELETE FROM faction_enemies WHERE faction_name=? AND enemy_name=?", factionName, enemyName);
    }

    public void addClaim(String factionName, String claimKey) {
        execute("INSERT OR IGNORE INTO faction_claims (faction_name, claim_key) VALUES (?, ?)", factionName, claimKey);
    }

    public void removeClaim(String claimKey) {
        execute("DELETE FROM faction_claims WHERE claim_key=?", claimKey);
    }

    public void removeAllClaims(String factionName) {
        execute("DELETE FROM faction_claims WHERE faction_name=?", factionName);
    }

    public void setHome(String factionName, Location loc) {
        execute("""
            INSERT INTO faction_home (faction_name, world, x, y, z, yaw, pitch)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(faction_name) DO UPDATE SET
                world=excluded.world, x=excluded.x, y=excluded.y,
                z=excluded.z, yaw=excluded.yaw, pitch=excluded.pitch
            """, factionName, loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public void setWarp(String factionName, String warpName, Location loc) {
        execute("""
            INSERT INTO faction_warps (faction_name, warp_name, world, x, y, z, yaw, pitch)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(faction_name, warp_name) DO UPDATE SET
                world=excluded.world, x=excluded.x, y=excluded.y,
                z=excluded.z, yaw=excluded.yaw, pitch=excluded.pitch
            """, factionName, warpName, loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public void deleteWarp(String factionName, String warpName) {
        execute("DELETE FROM faction_warps WHERE faction_name=? AND warp_name=?", factionName, warpName);
    }

    // ─── STATS ───────────────────────────────────────────────────────────────

    public void upsertStats(String playerUuid, int kills, int deaths, int streak, int bestStreak) {
        execute("""
            INSERT INTO player_stats (player_uuid, kills, deaths, current_streak, best_streak)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                kills=excluded.kills, deaths=excluded.deaths,
                current_streak=excluded.current_streak, best_streak=excluded.best_streak
            """, playerUuid, kills, deaths, streak, bestStreak);
    }

    public void incrementKill(String playerUuid, int streak, int bestStreak) {
        execute("""
            INSERT INTO player_stats (player_uuid, kills, current_streak, best_streak)
            VALUES (?, 1, ?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                kills=kills+1, current_streak=excluded.current_streak,
                best_streak=CASE WHEN excluded.best_streak > best_streak THEN excluded.best_streak ELSE best_streak END
            """, playerUuid, streak, bestStreak);
    }

    public void incrementDeath(String playerUuid) {
        execute("""
            INSERT INTO player_stats (player_uuid, deaths, current_streak)
            VALUES (?, 1, 0)
            ON CONFLICT(player_uuid) DO UPDATE SET deaths=deaths+1, current_streak=0
            """, playerUuid);
    }

    // ─── BOUNTIES ────────────────────────────────────────────────────────────

    public void upsertBounty(String targetUuid, String setterUuid, int amount) {
        execute("""
            INSERT INTO bounties (target_uuid, setter_uuid, amount) VALUES (?, ?, ?)
            ON CONFLICT(target_uuid, setter_uuid) DO UPDATE SET amount=amount+excluded.amount
            """, targetUuid, setterUuid, amount);
    }

    public void deleteBountiesOnTarget(String targetUuid) {
        execute("DELETE FROM bounties WHERE target_uuid=?", targetUuid);
    }

    // ─── KIT COOLDOWNS ───────────────────────────────────────────────────────

    public void upsertKitCooldown(String playerUuid, String kitName, long lastUse) {
        execute("""
            INSERT INTO kit_cooldowns (player_uuid, kit_name, last_use) VALUES (?, ?, ?)
            ON CONFLICT(player_uuid, kit_name) DO UPDATE SET last_use=excluded.last_use
            """, playerUuid, kitName, lastUse);
    }

    // ─── LOCATION HELPER ─────────────────────────────────────────────────────

    public static Location rsToLocation(ResultSet rs) throws SQLException {
        String worldName = rs.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world,
            rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
            rs.getFloat("yaw"), rs.getFloat("pitch"));
    }
}
