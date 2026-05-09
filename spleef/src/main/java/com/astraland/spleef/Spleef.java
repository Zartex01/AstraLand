package com.astraland.spleef;

import org.bukkit.plugin.java.JavaPlugin;

public class Spleef extends JavaPlugin {

    private static Spleef instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - Spleef chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Spleef désactivé.");
    }

    public static Spleef getInstance() {
        return instance;
    }
}
