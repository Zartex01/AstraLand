package com.astraland.buildbattle;

import com.astraland.buildbattle.commands.AHCommand;
import com.astraland.buildbattle.commands.BuildBattleCommand;
import com.astraland.buildbattle.commands.EconomyCommand;
import com.astraland.buildbattle.commands.ShopCommand;
import com.astraland.buildbattle.listeners.AHListener;
import com.astraland.buildbattle.listeners.BuildBattleListener;
import com.astraland.buildbattle.listeners.ShopListener;
import com.astraland.buildbattle.managers.AuctionManager;
import com.astraland.buildbattle.managers.BuildBattleManager;
import com.astraland.buildbattle.managers.EconomyManager;
import com.astraland.buildbattle.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildBattle extends JavaPlugin {

    private static BuildBattle instance;
    private BuildBattleManager buildBattleManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.buildBattleManager = new BuildBattleManager(this);
        this.economyManager     = new EconomyManager(this);
        this.auctionManager     = new AuctionManager(this);

        getCommand("buildbattle").setExecutor(new BuildBattleCommand(this));
        getCommand("buildbattle").setTabCompleter(new BuildBattleCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));

        getServer().getPluginManager().registerEvents(new BuildBattleListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

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
    public AuctionManager getAuctionManager()           { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("buildbattle.lobby-world", "world_buildbattle"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
