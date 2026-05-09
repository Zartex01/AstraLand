package com.astraland.oneblock;

import org.bukkit.plugin.java.JavaPlugin;

public class OneBlock extends JavaPlugin {

    private static OneBlock instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - OneBlock chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - OneBlock désactivé.");
    }

    public static OneBlock getInstance() {
        return instance;
    }
}
