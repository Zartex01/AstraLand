package com.astraland.skyblock;

import org.bukkit.plugin.java.JavaPlugin;

public class Skyblock extends JavaPlugin {

    private static Skyblock instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - Skyblock chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Skyblock désactivé.");
    }

    public static Skyblock getInstance() {
        return instance;
    }
}
