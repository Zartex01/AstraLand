package com.astraland.skywars;

import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {

    private static Skywars instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - Skywars chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Skywars désactivé.");
    }

    public static Skywars getInstance() {
        return instance;
    }
}
