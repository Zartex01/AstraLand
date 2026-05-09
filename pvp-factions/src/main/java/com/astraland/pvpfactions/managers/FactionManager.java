package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.models.Faction;
import com.astraland.pvpfactions.models.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    private final Set<UUID> autoclaimPlayers = new HashSet<>();
    private File dataFile;

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
            autoclaimPlayers.remove(uuid);
        }
        factions.remove(f.getName().toLowerCase());
        saveAll();
    }

    public Faction getFaction(String name) { return factions.get(name.toLowerCase()); }

    public Faction getPlayerFaction(UUID uuid) {
        String name = playerFaction.get(uuid);
        return name == null ? null : factions.get(name);
    }

    public boolean hasPlayerFaction(UUID uuid) { return playerFaction.containsKey(uuid); }
    public boolean factionExists(String name) { return factions.containsKey(name.toLowerCase()); }

    public void joinFaction(Faction f, UUID uuid) {
        f.addMember(uuid);
        playerFaction.put(uuid, f.getName().toLowerCase());
        saveAll();
    }

    public void leaveFaction(Faction f, UUID uuid) {
        f.removeMember(uuid);
        playerFaction.remove(uuid);
        factionChat.remove(uuid);
        autoclaimPlayers.remove(uuid);
        saveAll();
    }

    public boolean isFactionChat(UUID uuid) { return factionChat.getOrDefault(uuid, false); }
    public void toggleFactionChat(UUID uuid) { factionChat.put(uuid, !factionChat.getOrDefault(uuid, false)); }

    public boolean isAutoclaiming(UUID uuid) { return autoclaimPlayers.contains(uuid); }
    public void toggleAutoclaim(UUID uuid) {
        if (autoclaimPlayers.contains(uuid)) autoclaimPlayers.remove(uuid);
        else autoclaimPlayers.add(uuid);
    }

    public Collection<Faction> getAllFactions() { return factions.values(); }

    public Faction getFactionByClaim(org.bukkit.Chunk chunk) {
        String key = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        for (Faction f : factions.values()) if (f.getClaims().contains(key)) return f;
        return null;
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration data = new YamlConfiguration();

        for (Faction f : factions.values()) {
            String path = "factions." + f.getName();
            data.set(path + ".leader", f.getLeader().toString());
            data.set(path + ".tag", f.getTag());
            data.set(path + ".description", f.getDescription());
            data.set(path + ".motd", f.getMotd());
            data.set(path + ".open", f.isOpen());
            data.set(path + ".power", f.getPower());

            Map<String, String> membersMap = new LinkedHashMap<>();
            for (Map.Entry<UUID, FactionRole> e : f.getMembers().entrySet())
                membersMap.put(e.getKey().toString(), e.getValue().name());
            data.set(path + ".members", membersMap);

            data.set(path + ".allies", new ArrayList<>(f.getAllies()));
            data.set(path + ".enemies", new ArrayList<>(f.getEnemies()));
            data.set(path + ".claims", new ArrayList<>(f.getClaims()));

            if (f.getHome() != null) saveLocation(data, path + ".home", f.getHome());

            int warpIdx = 0;
            for (Map.Entry<String, Location> w : f.getWarps().entrySet()) {
                data.set(path + ".warps." + w.getKey() + ".world", w.getValue().getWorld().getName());
                data.set(path + ".warps." + w.getKey() + ".x", w.getValue().getX());
                data.set(path + ".warps." + w.getKey() + ".y", w.getValue().getY());
                data.set(path + ".warps." + w.getKey() + ".z", w.getValue().getZ());
                data.set(path + ".warps." + w.getKey() + ".yaw", (double) w.getValue().getYaw());
                data.set(path + ".warps." + w.getKey() + ".pitch", (double) w.getValue().getPitch());
            }
        }

        try { data.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection factionsSection = data.getConfigurationSection("factions");
        if (factionsSection == null) return;

        for (String name : factionsSection.getKeys(false)) {
            String path = "factions." + name;
            String leaderStr = data.getString(path + ".leader");
            if (leaderStr == null) continue;
            UUID leader = UUID.fromString(leaderStr);

            Faction f = new Faction(name, leader);
            f.setTag(data.getString(path + ".tag", name.substring(0, Math.min(4, name.length())).toUpperCase()));
            f.setDescription(data.getString(path + ".description", ""));
            f.setMotd(data.getString(path + ".motd", null));
            f.setOpen(data.getBoolean(path + ".open", false));
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

            Location home = loadLocation(data, path + ".home");
            if (home != null) f.setHome(home);

            ConfigurationSection warpsSection = data.getConfigurationSection(path + ".warps");
            if (warpsSection != null) {
                for (String warpName : warpsSection.getKeys(false)) {
                    String wp = path + ".warps." + warpName;
                    String ww = data.getString(wp + ".world");
                    if (ww == null) continue;
                    World world = Bukkit.getWorld(ww);
                    if (world == null) continue;
                    Location wLoc = new Location(world,
                        data.getDouble(wp + ".x"), data.getDouble(wp + ".y"), data.getDouble(wp + ".z"),
                        (float) data.getDouble(wp + ".yaw"), (float) data.getDouble(wp + ".pitch"));
                    f.getWarps().put(warpName, wLoc);
                }
            }

            factions.put(name.toLowerCase(), f);
        }
    }

    private void saveLocation(FileConfiguration cfg, String path, Location loc) {
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", (double) loc.getYaw());
        cfg.set(path + ".pitch", (double) loc.getPitch());
    }

    private Location loadLocation(FileConfiguration cfg, String path) {
        String w = cfg.getString(path + ".world");
        if (w == null) return null;
        World world = Bukkit.getWorld(w);
        if (world == null) return null;
        return new Location(world, cfg.getDouble(path + ".x"), cfg.getDouble(path + ".y"),
                cfg.getDouble(path + ".z"), (float) cfg.getDouble(path + ".yaw"), (float) cfg.getDouble(path + ".pitch"));
    }
}
