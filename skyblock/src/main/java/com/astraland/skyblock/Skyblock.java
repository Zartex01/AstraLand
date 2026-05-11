package com.astraland.skyblock;

import com.astraland.skyblock.commands.*;
import com.astraland.skyblock.listeners.AHListener;
import com.astraland.skyblock.listeners.IslandListener;
import com.astraland.skyblock.listeners.ShopListener;
import com.astraland.skyblock.managers.AuctionManager;
import com.astraland.skyblock.managers.EconomyManager;
import com.astraland.skyblock.managers.IslandLevelManager;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.scoreboard.ScoreboardTask;
import com.astraland.skyblock.shop.ShopMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Skyblock extends JavaPlugin {

    private static Skyblock instance;
    private IslandManager     islandManager;
    private IslandLevelManager levelManager;
    private EconomyManager    economyManager;
    private AuctionManager    auctionManager;
    private ScoreboardTask    scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.islandManager  = new IslandManager(this);
        this.levelManager   = new IslandLevelManager();
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);

        // /island
        IslandCommand islandCmd = new IslandCommand(this);
        getCommand("island").setExecutor(islandCmd);
        getCommand("island").setTabCompleter(islandCmd);

        // /balance /pay
        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        // /shop
        getCommand("shop").setExecutor(new ShopCommand(this));

        // /sell all
        SellAllCommand sellCmd = new SellAllCommand(this);
        getCommand("sell").setExecutor(sellCmd);
        getCommand("sell").setTabCompleter(sellCmd);

        // /ah
        getCommand("ah").setExecutor(new AHCommand(this));

        // /givemoney
        getCommand("givemoney").setExecutor(new GiveMoneyCommand(this));

        // /isadmin
        IslandAdminCommand adminCmd = new IslandAdminCommand(this);
        getCommand("isadmin").setExecutor(adminCmd);
        getCommand("isadmin").setTabCompleter(adminCmd);

        // Listeners
        getServer().getPluginManager().registerEvents(new IslandListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        // Scoreboard
        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Skyblock chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (islandManager  != null) islandManager.saveAll();
        getLogger().info("AstraLand - Skyblock désactivé.");
    }

    public static Skyblock getInstance()            { return instance; }
    public IslandManager getIslandManager()         { return islandManager; }
    public IslandLevelManager getLevelManager()     { return levelManager; }
    public EconomyManager getEconomyManager()       { return economyManager; }
    public AuctionManager getAuctionManager()       { return auctionManager; }

    public ShopMenuGUI newShopMenuGUI(Player player) { return new ShopMenuGUI(player, economyManager); }

    public String getPluginWorld()                  { return getConfig().getString("island.world", "world_skyblock"); }
    public boolean isInPluginWorld(Player player)   { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg()                   { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
