package com.astraland.skyblock.managers;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IslandManager {

    private final Skyblock plugin;
    private final Map<UUID, Island> islands = new HashMap<>();
    private final Map<UUID, UUID> memberIsland = new HashMap<>();
    private File dataFile;

    public IslandManager(Skyblock plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        load();
    }

    public boolean hasIsland(UUID uuid) {
        return islands.containsKey(uuid) || memberIsland.containsKey(uuid);
    }

    public Island getIsland(UUID uuid) {
        if (islands.containsKey(uuid)) return islands.get(uuid);
        UUID owner = memberIsland.get(uuid);
        return owner == null ? null : islands.get(owner);
    }

    public Island getOwnedIsland(UUID uuid) { return islands.get(uuid); }

    public Island createIsland(UUID owner) {
        String worldName = plugin.getConfig().getString("island.world", "world_skyblock");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Monde skyblock '" + worldName + "' introuvable !");
            return null;
        }

        int dist = plugin.getConfig().getInt("island.spawn-distance", 200);
        int x = islands.size() * dist;
        Location center = new Location(world, x, 65, 0);

        world.getBlockAt(center).setType(org.bukkit.Material.GRASS_BLOCK);
        world.getBlockAt(center.clone().add(0, 1, 0)).setType(org.bukkit.Material.AIR);

        Island island = new Island(owner, center);
        islands.put(owner, island);
        memberIsland.put(owner, owner);
        saveAll();
        return island;
    }

    public void deleteIsland(UUID owner) {
        Island island = islands.remove(owner);
        if (island == null) return;
        memberIsland.remove(owner);
        for (UUID member : island.getMembers()) memberIsland.remove(member);
        saveAll();
    }

    public void addMember(Island island, UUID uuid) {
        island.addMember(uuid);
        memberIsland.put(uuid, island.getOwner());
        saveAll();
    }

    public void removeMember(Island island, UUID uuid) {
        island.removeMember(uuid);
        memberIsland.remove(uuid);
        saveAll();
    }

    public boolean isInsideOwnIsland(UUID uuid, Location loc) {
        Island island = getIsland(uuid);
        if (island == null) return false;
        int size = plugin.getConfig().getInt("island.size", 100);
        return island.isInsideIsland(loc, size);
    }

    public List<Island> getTopIslands(int limit) {
        List<Island> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> b.getLevel() - a.getLevel());
        return list.subList(0, Math.min(limit, list.size()));
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Island> e : islands.entrySet()) {
            Island isl = e.getValue();
            String path = "islands." + e.getKey();
            cfg.set(path + ".level", isl.getLevel());
            cfg.set(path + ".blocks", isl.getBlocksBroken());
            List<String> memberList = new ArrayList<>();
            isl.getMembers().forEach(m -> memberList.add(m.toString()));
            cfg.set(path + ".members", memberList);
            if (isl.getHome() != null) saveLocation(cfg, path + ".home", isl.getHome());
            if (isl.getCenter() != null) saveLocation(cfg, path + ".center", isl.getCenter());
        }
        try { cfg.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("islands") == null) return;
        for (String uuidStr : cfg.getConfigurationSection("islands").getKeys(false)) {
            try {
                UUID owner = UUID.fromString(uuidStr);
                String path = "islands." + uuidStr;
                Location center = loadLocation(cfg, path + ".center");
                if (center == null) continue;
                Island isl = new Island(owner, center);
                isl.setLevel(cfg.getInt(path + ".level", 0));
                isl.addBlocksBroken(cfg.getLong(path + ".blocks", 0));
                Location home = loadLocation(cfg, path + ".home");
                if (home != null) isl.setHome(home);
                for (String m : cfg.getStringList(path + ".members")) {
                    try { UUID mu = UUID.fromString(m); isl.addMember(mu); memberIsland.put(mu, owner); } catch (Exception ignored) {}
                }
                islands.put(owner, isl);
                memberIsland.put(owner, owner);
            } catch (Exception ignored) {}
        }
    }

    private void saveLocation(FileConfiguration cfg, String path, Location loc) {
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX()); cfg.set(path + ".y", loc.getY()); cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", (double) loc.getYaw()); cfg.set(path + ".pitch", (double) loc.getPitch());
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
