package com.astraland.duels;

import org.bukkit.plugin.java.JavaPlugin;

public class Duels extends JavaPlugin {

    private static Duels instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - Duels chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Duels désactivé.");
    }

    public static Duels getInstance() {
        return instance;
    }
}
