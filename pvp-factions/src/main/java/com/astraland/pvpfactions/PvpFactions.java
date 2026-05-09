package com.astraland.pvpfactions;

import org.bukkit.plugin.java.JavaPlugin;

public class PvpFactions extends JavaPlugin {

    private static PvpFactions instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("AstraLand - PvP/Factions chargé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - PvP/Factions désactivé.");
    }

    public static PvpFactions getInstance() {
        return instance;
    }
}
