package com.astraland.skywars;

import com.astraland.skywars.commands.SkywarsCommand;
import com.astraland.skywars.listeners.SkywarsListener;
import com.astraland.skywars.managers.SkywarsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {

    private static Skywars instance;
    private SkywarsManager skywarsManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.skywarsManager = new SkywarsManager(this);

        getCommand("skywars").setExecutor(new SkywarsCommand(this));
        getCommand("skywars").setTabCompleter(new SkywarsCommand(this));

        getServer().getPluginManager().registerEvents(new SkywarsListener(this), this);
        getLogger().info("AstraLand - Skywars chargé !");
    }

    @Override
    public void onDisable() {
        if (skywarsManager != null) skywarsManager.stopAll();
        getLogger().info("AstraLand - Skywars désactivé.");
    }

    public static Skywars getInstance() { return instance; }
    public SkywarsManager getSkywarsManager() { return skywarsManager; }
}
