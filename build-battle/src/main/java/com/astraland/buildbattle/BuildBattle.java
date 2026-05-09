package com.astraland.buildbattle;

import com.astraland.buildbattle.commands.BuildBattleCommand;
import com.astraland.buildbattle.listeners.BuildBattleListener;
import com.astraland.buildbattle.managers.BuildBattleManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildBattle extends JavaPlugin {

    private static BuildBattle instance;
    private BuildBattleManager buildBattleManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.buildBattleManager = new BuildBattleManager(this);

        getCommand("buildbattle").setExecutor(new BuildBattleCommand(this));
        getCommand("buildbattle").setTabCompleter(new BuildBattleCommand(this));

        getServer().getPluginManager().registerEvents(new BuildBattleListener(this), this);
        getLogger().info("AstraLand - Build Battle chargé !");
    }

    @Override
    public void onDisable() {
        if (buildBattleManager != null) buildBattleManager.stopAll();
        getLogger().info("AstraLand - Build Battle désactivé.");
    }

    public static BuildBattle getInstance() { return instance; }
    public BuildBattleManager getBuildBattleManager() { return buildBattleManager; }
}
