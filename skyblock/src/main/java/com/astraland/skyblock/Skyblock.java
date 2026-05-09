package com.astraland.skyblock;

import com.astraland.skyblock.commands.IslandCommand;
import com.astraland.skyblock.listeners.IslandListener;
import com.astraland.skyblock.managers.IslandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Skyblock extends JavaPlugin {

    private static Skyblock instance;
    private IslandManager islandManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.islandManager = new IslandManager(this);

        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("island").setTabCompleter(new IslandCommand(this));

        getServer().getPluginManager().registerEvents(new IslandListener(this), this);
        getLogger().info("AstraLand - Skyblock chargé !");
    }

    @Override
    public void onDisable() {
        if (islandManager != null) islandManager.saveAll();
        getLogger().info("AstraLand - Skyblock désactivé.");
    }

    public static Skyblock getInstance() { return instance; }
    public IslandManager getIslandManager() { return islandManager; }
}
