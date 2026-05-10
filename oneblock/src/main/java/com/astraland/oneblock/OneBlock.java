package com.astraland.oneblock;

import com.astraland.oneblock.commands.AHCommand;
import com.astraland.oneblock.commands.EconomyCommand;
import com.astraland.oneblock.commands.OneBlockCommand;
import com.astraland.oneblock.commands.ShopCommand;
import com.astraland.oneblock.listeners.AHListener;
import com.astraland.oneblock.listeners.OneBlockListener;
import com.astraland.oneblock.listeners.ShopListener;
import com.astraland.oneblock.managers.AuctionManager;
import com.astraland.oneblock.managers.EconomyManager;
import com.astraland.oneblock.managers.OneBlockManager;
import com.astraland.oneblock.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class OneBlock extends JavaPlugin {

    private static OneBlock instance;
    private OneBlockManager oneBlockManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.oneBlockManager = new OneBlockManager(this);
        this.economyManager  = new EconomyManager(this);
        this.auctionManager  = new AuctionManager(this);

        getCommand("oneblock").setExecutor(new OneBlockCommand(this));
        getCommand("oneblock").setTabCompleter(new OneBlockCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));

        getServer().getPluginManager().registerEvents(new OneBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - OneBlock chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (oneBlockManager != null) oneBlockManager.saveAll();
        getLogger().info("AstraLand - OneBlock désactivé.");
    }

    public static OneBlock getInstance()          { return instance; }
    public OneBlockManager getOneBlockManager()   { return oneBlockManager; }
    public EconomyManager getEconomyManager()     { return economyManager; }
    public AuctionManager getAuctionManager()     { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("oneblock.world", "world_oneblock"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
