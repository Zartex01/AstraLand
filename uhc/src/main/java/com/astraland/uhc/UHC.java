package com.astraland.uhc;

import org.bukkit.plugin.java.JavaPlugin;

public class UHC extends JavaPlugin {

    private static UHC instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - UHC chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - UHC désactivé.");
    }

    public static UHC getInstance() {
        return instance;
    }
}
