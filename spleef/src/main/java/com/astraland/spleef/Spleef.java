package com.astraland.spleef;

import com.astraland.spleef.commands.AHCommand;
import com.astraland.spleef.commands.EconomyCommand;
import com.astraland.spleef.commands.GiveMoneyCommand;
import com.astraland.spleef.commands.ShopCommand;
import com.astraland.spleef.commands.SpleefCommand;
import com.astraland.spleef.listeners.AHListener;
import com.astraland.spleef.listeners.ShopListener;
import com.astraland.spleef.listeners.SpleefListener;
import com.astraland.spleef.managers.AuctionManager;
import com.astraland.spleef.managers.EconomyManager;
import com.astraland.spleef.managers.SpleefManager;
import com.astraland.spleef.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Spleef extends JavaPlugin {

    private static Spleef instance;
    private SpleefManager spleefManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.spleefManager  = new SpleefManager(this);
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);

        getCommand("spleef").setExecutor(new SpleefCommand(this));
        getCommand("spleef").setTabCompleter(new SpleefCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));
        getCommand("givemoney").setExecutor(new GiveMoneyCommand(this));

        getServer().getPluginManager().registerEvents(new SpleefListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Spleef chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (spleefManager != null) spleefManager.stopAll();
        getLogger().info("AstraLand - Spleef désactivé.");
    }

    public static Spleef getInstance()          { return instance; }
    public SpleefManager getSpleefManager()     { return spleefManager; }
    public EconomyManager getEconomyManager()   { return economyManager; }
    public AuctionManager getAuctionManager()   { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("spleef.lobby-world", "world_spleef"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
