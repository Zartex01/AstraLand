package com.astraland.startup;

import com.astraland.startup.command.LobbyCommand;
import com.astraland.startup.gui.WorldSelectorGUI;
import com.astraland.startup.listener.CompassLockListener;
import com.astraland.startup.listener.CompassListener;
import com.astraland.startup.listener.PlayerJoinListener;
import com.astraland.startup.listener.PlayerVisibilityListener;
import com.astraland.startup.listener.WorldChangeListener;
import com.astraland.startup.manager.ConfigManager;
import com.astraland.startup.manager.LobbyManager;
import com.astraland.startup.manager.LocationManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AstraLandStartup extends JavaPlugin {

    private static AstraLandStartup instance;
    private ConfigManager configManager;
    private LocationManager locationManager;
    private LobbyManager lobbyManager;
    private WorldSelectorGUI worldSelectorGUI;
    private PlayerJoinListener playerJoinListener;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager      = new ConfigManager(this);
        this.locationManager    = new LocationManager(this);
        this.lobbyManager       = new LobbyManager(this);
        this.worldSelectorGUI   = new WorldSelectorGUI(this);
        this.playerJoinListener = new PlayerJoinListener(this);

        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(worldSelectorGUI, this);
        getServer().getPluginManager().registerEvents(new CompassLockListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerVisibilityListener(this), this);

        LobbyCommand lobbyCommand = new LobbyCommand(this);
        getCommand("setlobby").setExecutor(lobbyCommand);
        getCommand("lobby").setExecutor(lobbyCommand);

        getLogger().info("=================================");
        getLogger().info("   AstraLand - Startup chargé !  ");
        getLogger().info("  Boussole activée sur le slot " + configManager.getCompassSlot());
        getLogger().info("  Mondes chargés : " + configManager.getWorlds().size());
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Startup désactivé.");
    }

    public static AstraLandStartup getInstance()          { return instance; }
    public ConfigManager getConfigManager()               { return configManager; }
    public LocationManager getLocationManager()           { return locationManager; }
    public LobbyManager getLobbyManager()                 { return lobbyManager; }
    public WorldSelectorGUI getWorldSelectorGUI()         { return worldSelectorGUI; }
    public PlayerJoinListener getPlayerJoinListener()     { return playerJoinListener; }
}
