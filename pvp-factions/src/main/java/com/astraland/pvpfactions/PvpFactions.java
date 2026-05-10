package com.astraland.pvpfactions;

import com.astraland.pvpfactions.commands.*;
import com.astraland.pvpfactions.database.DatabaseManager;
import com.astraland.pvpfactions.listeners.ChatListener;
import com.astraland.pvpfactions.listeners.PvpListener;
import com.astraland.pvpfactions.managers.*;
import com.astraland.pvpfactions.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpFactions extends JavaPlugin {

    private static PvpFactions instance;
    private DatabaseManager databaseManager;
    private FactionManager factionManager;
    private StatsManager statsManager;
    private BountyManager bountyManager;
    private KitManager kitManager;
    private EconomyManager economyManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();

        this.factionManager  = new FactionManager(this);
        this.statsManager    = new StatsManager(this);
        this.bountyManager   = new BountyManager(this);
        this.kitManager      = new KitManager(this);
        this.economyManager  = new EconomyManager(this);

        FactionCommand fCmd = new FactionCommand(this);
        getCommand("faction").setExecutor(fCmd);
        getCommand("faction").setTabCompleter(fCmd);

        KillsCommand killsCmd = new KillsCommand(this);
        getCommand("kills").setExecutor(killsCmd);
        getCommand("kills").setTabCompleter(killsCmd);

        TopKillsCommand topCmd = new TopKillsCommand(this);
        getCommand("topkills").setExecutor(topCmd);
        getCommand("topkills").setTabCompleter(topCmd);

        KillStreakCommand streakCmd = new KillStreakCommand(this);
        getCommand("killstreak").setExecutor(streakCmd);
        getCommand("killstreak").setTabCompleter(streakCmd);

        BountyCommand bountyCmd = new BountyCommand(this);
        getCommand("bounty").setExecutor(bountyCmd);
        getCommand("bounty").setTabCompleter(bountyCmd);
        getCommand("bountylist").setExecutor(bountyCmd);

        KitCommand kitCmd = new KitCommand(this);
        getCommand("kit").setExecutor(kitCmd);
        getCommand("kit").setTabCompleter(kitCmd);

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getServer().getPluginManager().registerEvents(new PvpListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - PvP/Factions chargé avec SQLite !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (databaseManager != null) databaseManager.disconnect();
        getLogger().info("AstraLand - PvP/Factions désactivé.");
    }

    public static PvpFactions getInstance()        { return instance; }
    public DatabaseManager getDatabaseManager()    { return databaseManager; }
    public FactionManager getFactionManager()      { return factionManager; }
    public StatsManager getStatsManager()          { return statsManager; }
    public BountyManager getBountyManager()        { return bountyManager; }
    public KitManager getKitManager()              { return kitManager; }
    public EconomyManager getEconomyManager()      { return economyManager; }

    public String getPluginWorld() { return getConfig().getString("world", "world_pvpfactions"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
