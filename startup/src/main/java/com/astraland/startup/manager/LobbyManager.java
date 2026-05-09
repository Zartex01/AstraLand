package com.astraland.startup.manager;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class LobbyManager {

    private final AstraLandStartup plugin;
    private final File file;
    private YamlConfiguration config;
    private Location lobbyLocation;

    public LobbyManager(AstraLandStartup plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "lobby.yml");
        load();
    }

    private void load() {
        config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("lobby.world")) return;

        String worldName = config.getString("lobby.world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Le monde du lobby '" + worldName + "' est introuvable.");
            return;
        }

        double x     = config.getDouble("lobby.x");
        double y     = config.getDouble("lobby.y");
        double z     = config.getDouble("lobby.z");
        float  yaw   = (float) config.getDouble("lobby.yaw");
        float  pitch = (float) config.getDouble("lobby.pitch");

        lobbyLocation = new Location(world, x, y, z, yaw, pitch);
    }

    public void setLobby(Location loc) {
        this.lobbyLocation = loc.clone();

        config.set("lobby.world", loc.getWorld().getName());
        config.set("lobby.x",     loc.getX());
        config.set("lobby.y",     loc.getY());
        config.set("lobby.z",     loc.getZ());
        config.set("lobby.yaw",   (double) loc.getYaw());
        config.set("lobby.pitch", (double) loc.getPitch());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder le lobby : " + e.getMessage());
        }
    }

    public Location getLobby() {
        return lobbyLocation != null ? lobbyLocation.clone() : null;
    }

    public boolean hasLobby() {
        return lobbyLocation != null;
    }
}
