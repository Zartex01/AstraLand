package com.astraland.skyblock;

import com.astraland.skyblock.commands.AHCommand;
import com.astraland.skyblock.commands.EconomyCommand;
import com.astraland.skyblock.commands.GiveMoneyCommand;
import com.astraland.skyblock.commands.IslandCommand;
import com.astraland.skyblock.commands.ShopCommand;
import com.astraland.skyblock.listeners.AHListener;
import com.astraland.skyblock.listeners.IslandListener;
import com.astraland.skyblock.listeners.ShopListener;
import com.astraland.skyblock.managers.AuctionManager;
import com.astraland.skyblock.managers.EconomyManager;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Skyblock extends JavaPlugin {

    private static Skyblock instance;
    private IslandManager islandManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.islandManager  = new IslandManager(this);
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);

        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("island").setTabCompleter(new IslandCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));
        getCommand("givemoney").setExecutor(new GiveMoneyCommand(this));

        getServer().getPluginManager().registerEvents(new IslandListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Skyblock chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (islandManager != null) islandManager.saveAll();
        getLogger().info("AstraLand - Skyblock désactivé.");
    }

    public static Skyblock getInstance()          { return instance; }
    public IslandManager getIslandManager()       { return islandManager; }
    public EconomyManager getEconomyManager()     { return economyManager; }
    public AuctionManager getAuctionManager()     { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("island.world", "world_skyblock"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
