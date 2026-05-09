package com.astraland.bedwars;

import org.bukkit.plugin.java.JavaPlugin;

public class Bedwars extends JavaPlugin {

    private static Bedwars instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - Bedwars chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Bedwars désactivé.");
    }

    public static Bedwars getInstance() {
        return instance;
    }
}
