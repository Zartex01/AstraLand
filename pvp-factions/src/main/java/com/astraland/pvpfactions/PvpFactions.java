package com.astraland.pvpfactions;

import com.astraland.pvpfactions.commands.*;
import com.astraland.pvpfactions.listeners.ChatListener;
import com.astraland.pvpfactions.listeners.PvpListener;
import com.astraland.pvpfactions.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpFactions extends JavaPlugin {

    private static PvpFactions instance;
    private FactionManager factionManager;
    private StatsManager statsManager;
    private BountyManager bountyManager;
    private KitManager kitManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.factionManager = new FactionManager(this);
        this.statsManager = new StatsManager(this);
        this.bountyManager = new BountyManager(this);
        this.kitManager = new KitManager(this);

        FactionCommand fCmd = new FactionCommand(this);
        getCommand("faction").setExecutor(fCmd);
        getCommand("faction").setTabCompleter(fCmd);

        KillsCommand killsCmd = new KillsCommand(this);
        getCommand("kills").setExecutor(killsCmd);

        TopKillsCommand topCmd = new TopKillsCommand(this);
        getCommand("topkills").setExecutor(topCmd);
        getCommand("topkills").setTabCompleter(topCmd);

        getCommand("killstreak").setExecutor(new KillStreakCommand(this));

        BountyCommand bountyCmd = new BountyCommand(this);
        getCommand("bounty").setExecutor(bountyCmd);
        getCommand("bounty").setTabCompleter(bountyCmd);
        getCommand("bountylist").setExecutor(bountyCmd);

        KitCommand kitCmd = new KitCommand(this);
        getCommand("kit").setExecutor(kitCmd);
        getCommand("kit").setTabCompleter(kitCmd);

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
    public BountyManager getBountyManager() { return bountyManager; }
    public KitManager getKitManager() { return kitManager; }
}
