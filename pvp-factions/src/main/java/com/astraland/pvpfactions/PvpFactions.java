package com.astraland.pvpfactions;

import com.astraland.pvpfactions.commands.FactionCommand;
import com.astraland.pvpfactions.commands.KillsCommand;
import com.astraland.pvpfactions.listeners.ChatListener;
import com.astraland.pvpfactions.listeners.PvpListener;
import com.astraland.pvpfactions.managers.FactionManager;
import com.astraland.pvpfactions.managers.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpFactions extends JavaPlugin {

    private static PvpFactions instance;
    private FactionManager factionManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.factionManager = new FactionManager(this);
        this.statsManager = new StatsManager(this);

        getCommand("faction").setExecutor(new FactionCommand(this));
        getCommand("faction").setTabCompleter(new FactionCommand(this));
        getCommand("kills").setExecutor(new KillsCommand(this));

        getServer().getPluginManager().registerEvents(new PvpListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("AstraLand - PvP/Factions chargé !");
    }

    @Override
    public void onDisable() {
        if (factionManager != null) factionManager.saveAll();
        if (statsManager != null) statsManager.saveAll();
        getLogger().info("AstraLand - PvP/Factions désactivé.");
    }

    public static PvpFactions getInstance() { return instance; }
    public FactionManager getFactionManager() { return factionManager; }
    public StatsManager getStatsManager() { return statsManager; }
}
