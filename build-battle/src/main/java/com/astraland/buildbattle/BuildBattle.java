package com.astraland.buildbattle;

import org.bukkit.plugin.java.JavaPlugin;

public class BuildBattle extends JavaPlugin {

    private static BuildBattle instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - Build Battle chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Build Battle désactivé.");
    }

    public static BuildBattle getInstance() {
        return instance;
    }
}
