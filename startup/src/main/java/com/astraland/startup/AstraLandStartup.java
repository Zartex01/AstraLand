package com.astraland.startup;

import com.astraland.startup.gui.WorldSelectorGUI;
import com.astraland.startup.listener.CompassListener;
import com.astraland.startup.listener.PlayerJoinListener;
import com.astraland.startup.manager.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AstraLandStartup extends JavaPlugin {

    private static AstraLandStartup instance;
    private ConfigManager configManager;
    private WorldSelectorGUI worldSelectorGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.worldSelectorGUI = new WorldSelectorGUI(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new CompassListener(this), this);
        getServer().getPluginManager().registerEvents(worldSelectorGUI, this);

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

    public static AstraLandStartup getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public WorldSelectorGUI getWorldSelectorGUI() {
        return worldSelectorGUI;
    }
}
