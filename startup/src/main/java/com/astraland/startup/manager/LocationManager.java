package com.astraland.startup.manager;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocationManager {

    private final AstraLandStartup plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, Location> cache = new HashMap<>();

    public LocationManager(AstraLandStartup plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "locations.yml");
        load();
    }

    private void load() {
        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                double x     = config.getDouble(key + ".x");
                double y     = config.getDouble(key + ".y");
                double z     = config.getDouble(key + ".z");
                float  yaw   = (float) config.getDouble(key + ".yaw");
                float  pitch = (float) config.getDouble(key + ".pitch");
                World  world = Bukkit.getWorld("world");
                if (world != null) {
                    cache.put(uuid, new Location(world, x, y, z, yaw, pitch));
                }
            } catch (Exception ignored) {}
        }
    }

    public void save(UUID uuid, Location loc) {
        cache.put(uuid, loc.clone());
        config.set(uuid + ".x",     loc.getX());
        config.set(uuid + ".y",     loc.getY());
        config.set(uuid + ".z",     loc.getZ());
        config.set(uuid + ".yaw",   (double) loc.getYaw());
        config.set(uuid + ".pitch", (double) loc.getPitch());
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder la position : " + e.getMessage());
        }
    }

    public Location get(UUID uuid) {
        return cache.containsKey(uuid) ? cache.get(uuid).clone() : null;
    }

    public boolean has(UUID uuid) {
        return cache.containsKey(uuid);
    }
}
