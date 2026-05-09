package com.astraland.duels;

import com.astraland.duels.commands.DuelCommand;
import com.astraland.duels.listeners.DuelListener;
import com.astraland.duels.managers.DuelManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Duels extends JavaPlugin {

    private static Duels instance;
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.duelManager = new DuelManager(this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duel").setTabCompleter(new DuelCommand(this));

        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getLogger().info("AstraLand - Duels chargé !");
    }

    @Override
    public void onDisable() {
        if (duelManager != null) duelManager.endAll();
        getLogger().info("AstraLand - Duels désactivé.");
    }

    public static Duels getInstance() { return instance; }
    public DuelManager getDuelManager() { return duelManager; }
}
