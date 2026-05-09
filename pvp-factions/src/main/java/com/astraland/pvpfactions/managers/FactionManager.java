package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.models.Faction;
import com.astraland.pvpfactions.models.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FactionManager {

    private final PvpFactions plugin;
    private final Map<String, Faction> factions = new LinkedHashMap<>();
    private final Map<UUID, String> playerFaction = new HashMap<>();
    private final Map<UUID, Boolean> factionChat = new HashMap<>();
    private File dataFile;
    private FileConfiguration data;

    public FactionManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "factions.yml");
        load();
    }

    public Faction createFaction(String name, UUID leader) {
        Faction f = new Faction(name, leader);
        factions.put(name.toLowerCase(), f);
        playerFaction.put(leader, name.toLowerCase());
        saveAll();
        return f;
    }

    public void disbandFaction(Faction f) {
        for (UUID uuid : f.getMembers().keySet()) {
            playerFaction.remove(uuid);
            factionChat.remove(uuid);
        }
        factions.remove(f.getName().toLowerCase());
        saveAll();
    }

    public Faction getFaction(String name) {
        return factions.get(name.toLowerCase());
    }

    public Faction getPlayerFaction(UUID uuid) {
        String name = playerFaction.get(uuid);
        return name == null ? null : factions.get(name);
    }

    public boolean hasPlayerFaction(UUID uuid) {
        return playerFaction.containsKey(uuid);
    }

    public boolean factionExists(String name) {
        return factions.containsKey(name.toLowerCase());
    }

    public void joinFaction(Faction f, UUID uuid) {
        f.addMember(uuid);
        playerFaction.put(uuid, f.getName().toLowerCase());
        saveAll();
    }

    public void leaveFaction(Faction f, UUID uuid) {
        f.removeMember(uuid);
        playerFaction.remove(uuid);
        factionChat.remove(uuid);
        saveAll();
    }

    public boolean isFactionChat(UUID uuid) {
        return factionChat.getOrDefault(uuid, false);
    }

    public void toggleFactionChat(UUID uuid) {
        factionChat.put(uuid, !factionChat.getOrDefault(uuid, false));
    }

    public Collection<Faction> getAllFactions() {
        return factions.values();
    }

    public Faction getFactionByClaim(org.bukkit.Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        for (Faction f : factions.values()) {
            if (f.getClaims().contains(key)) return f;
        }
        return null;
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        data = new YamlConfiguration();

        for (Faction f : factions.values()) {
            String path = "factions." + f.getName();
            data.set(path + ".leader", f.getLeader().toString());
            data.set(path + ".description", f.getDescription());
            data.set(path + ".power", f.getPower());

            Map<String, String> membersMap = new LinkedHashMap<>();
            for (Map.Entry<UUID, FactionRole> e : f.getMembers().entrySet()) {
                membersMap.put(e.getKey().toString(), e.getValue().name());
            }
            data.set(path + ".members", membersMap);
            data.set(path + ".allies", new ArrayList<>(f.getAllies()));
            data.set(path + ".enemies", new ArrayList<>(f.getEnemies()));
            data.set(path + ".claims", new ArrayList<>(f.getClaims()));

            if (f.getHome() != null) {
                Location h = f.getHome();
                data.set(path + ".home.world", h.getWorld().getName());
                data.set(path + ".home.x", h.getX());
                data.set(path + ".home.y", h.getY());
                data.set(path + ".home.z", h.getZ());
                data.set(path + ".home.yaw", (double) h.getYaw());
                data.set(path + ".home.pitch", (double) h.getPitch());
            }
        }

        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection factionsSection = data.getConfigurationSection("factions");
        if (factionsSection == null) return;

        for (String name : factionsSection.getKeys(false)) {
            String path = "factions." + name;
            UUID leader = UUID.fromString(data.getString(path + ".leader"));
            Faction f = new Faction(name, leader);
            f.setDescription(data.getString(path + ".description", ""));
            f.setPower(data.getDouble(path + ".power", 10.0));

            ConfigurationSection membersSection = data.getConfigurationSection(path + ".members");
            if (membersSection != null) {
                f.getMembers().clear();
                for (String uuidStr : membersSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        FactionRole role = FactionRole.valueOf(membersSection.getString(uuidStr, "MEMBER"));
                        f.getMembers().put(uuid, role);
                        playerFaction.put(uuid, name.toLowerCase());
                    } catch (Exception ignored) {}
                }
            }

            for (String ally : data.getStringList(path + ".allies")) f.getAllies().add(ally);
            for (String enemy : data.getStringList(path + ".enemies")) f.getEnemies().add(enemy);
            for (String claim : data.getStringList(path + ".claims")) f.getClaims().add(claim);

            String homeWorld = data.getString(path + ".home.world");
            if (homeWorld != null && Bukkit.getWorld(homeWorld) != null) {
                double hx = data.getDouble(path + ".home.x");
                double hy = data.getDouble(path + ".home.y");
                double hz = data.getDouble(path + ".home.z");
                float hyaw = (float) data.getDouble(path + ".home.yaw");
                float hpitch = (float) data.getDouble(path + ".home.pitch");
                f.setHome(new Location(Bukkit.getWorld(homeWorld), hx, hy, hz, hyaw, hpitch));
            }

            factions.put(name.toLowerCase(), f);
        }
    }
}
