package com.astraland.oneblock.managers;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OneBlockManager {

    private final OneBlock plugin;
    private final Map<UUID, OneBlockIsland> islands = new HashMap<>();
    private final Map<UUID, UUID> memberIsland = new HashMap<>();
    private File dataFile;

    public OneBlockManager(OneBlock plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        load();
    }

    public boolean hasIsland(UUID uuid) {
        return islands.containsKey(uuid) || memberIsland.containsKey(uuid);
    }

    public OneBlockIsland getIsland(UUID uuid) {
        if (islands.containsKey(uuid)) return islands.get(uuid);
        UUID owner = memberIsland.get(uuid);
        return owner == null ? null : islands.get(owner);
    }

    public OneBlockIsland createIsland(UUID owner) {
        String worldName = plugin.getConfig().getString("oneblock.world", "world_oneblock");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Monde oneblock '" + worldName + "' introuvable !");
            return null;
        }
        int dist = plugin.getConfig().getInt("oneblock.spawn-distance", 300);
        int x = islands.size() * dist;
        Location blockLoc = new Location(world, x, 65, 0);

        world.getBlockAt(blockLoc).setType(Material.GRASS_BLOCK);

        OneBlockIsland island = new OneBlockIsland(owner, blockLoc);
        islands.put(owner, island);
        memberIsland.put(owner, owner);
        saveAll();
        return island;
    }

    public void regenerateBlock(OneBlockIsland island) {
        Location loc = island.getBlockLocation();
        if (loc == null || loc.getWorld() == null) return;
        island.incrementBlocks();
        Phase phase = island.getCurrentPhase();
        Material mat = phase.getRandomBlock();
        loc.getBlock().setType(mat);
    }

    public void deleteIsland(UUID owner) {
        OneBlockIsland island = islands.remove(owner);
        if (island == null) return;
        memberIsland.remove(owner);
        for (UUID m : island.getMembers()) memberIsland.remove(m);
        saveAll();
    }

    public List<OneBlockIsland> getTop(int limit) {
        List<OneBlockIsland> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> Long.compare(b.getBlocksBroken(), a.getBlocksBroken()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, OneBlockIsland> e : islands.entrySet()) {
            OneBlockIsland isl = e.getValue();
            String path = "islands." + e.getKey();
            cfg.set(path + ".blocks", isl.getBlocksBroken());
            cfg.set(path + ".phase", isl.getCurrentPhase().name());
            List<String> members = new ArrayList<>();
            isl.getMembers().forEach(m -> members.add(m.toString()));
            cfg.set(path + ".members", members);
            if (isl.getBlockLocation() != null) saveLocation(cfg, path + ".blockloc", isl.getBlockLocation());
            if (isl.getHome() != null) saveLocation(cfg, path + ".home", isl.getHome());
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
                Location blockLoc = loadLocation(cfg, path + ".blockloc");
                if (blockLoc == null) continue;
                OneBlockIsland isl = new OneBlockIsland(owner, blockLoc);
                isl.setBlocksBroken(cfg.getLong(path + ".blocks", 0));
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
    }

    private Location loadLocation(FileConfiguration cfg, String path) {
        String w = cfg.getString(path + ".world");
        if (w == null) return null;
        World world = Bukkit.getWorld(w);
        if (world == null) return null;
        return new Location(world, cfg.getDouble(path + ".x"), cfg.getDouble(path + ".y"), cfg.getDouble(path + ".z"));
    }
}
