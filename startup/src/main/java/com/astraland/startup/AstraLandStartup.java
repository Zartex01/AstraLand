package com.astraland.startup;

import org.bukkit.plugin.java.JavaPlugin;

public class AstraLandStartup extends JavaPlugin {

    private static AstraLandStartup instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("=================================");
        getLogger().info("   AstraLand - Startup chargé !  ");
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Startup désactivé.");
    }

    public static AstraLandStartup getInstance() {
        return instance;
    }
}
