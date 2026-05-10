package com.astraland.buildbattle;

import com.astraland.buildbattle.commands.BuildBattleCommand;
import com.astraland.buildbattle.commands.EconomyCommand;
import com.astraland.buildbattle.listeners.BuildBattleListener;
import com.astraland.buildbattle.managers.BuildBattleManager;
import com.astraland.buildbattle.managers.EconomyManager;
import com.astraland.buildbattle.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildBattle extends JavaPlugin {

    private static BuildBattle instance;
    private BuildBattleManager buildBattleManager;
    private EconomyManager economyManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.buildBattleManager = new BuildBattleManager(this);
        this.economyManager     = new EconomyManager(this);

        getCommand("buildbattle").setExecutor(new BuildBattleCommand(this));
        getCommand("buildbattle").setTabCompleter(new BuildBattleCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getServer().getPluginManager().registerEvents(new BuildBattleListener(this), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Build Battle chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (buildBattleManager != null) buildBattleManager.stopAll();
        getLogger().info("AstraLand - Build Battle désactivé.");
    }

    public static BuildBattle getInstance()             { return instance; }
    public BuildBattleManager getBuildBattleManager()   { return buildBattleManager; }
    public EconomyManager getEconomyManager()           { return economyManager; }

    public String getPluginWorld() { return getConfig().getString("buildbattle.lobby-world", "world_buildbattle"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
