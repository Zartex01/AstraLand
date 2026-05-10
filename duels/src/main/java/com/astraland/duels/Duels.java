package com.astraland.duels;

import com.astraland.duels.commands.AHCommand;
import com.astraland.duels.commands.DuelCommand;
import com.astraland.duels.commands.EconomyCommand;
import com.astraland.duels.commands.GiveMoneyCommand;
import com.astraland.duels.commands.ShopCommand;
import com.astraland.duels.listeners.AHListener;
import com.astraland.duels.listeners.DuelListener;
import com.astraland.duels.listeners.ShopListener;
import com.astraland.duels.managers.AuctionManager;
import com.astraland.duels.managers.DuelManager;
import com.astraland.duels.managers.EconomyManager;
import com.astraland.duels.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Duels extends JavaPlugin {

    private static Duels instance;
    private DuelManager duelManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.duelManager    = new DuelManager(this);
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);

        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duel").setTabCompleter(new DuelCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));
        getCommand("givemoney").setExecutor(new GiveMoneyCommand(this));

        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Duels chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (duelManager != null) duelManager.endAll();
        getLogger().info("AstraLand - Duels désactivé.");
    }

    public static Duels getInstance()           { return instance; }
    public DuelManager getDuelManager()         { return duelManager; }
    public EconomyManager getEconomyManager()   { return economyManager; }
    public AuctionManager getAuctionManager()   { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("duels.world", "world_duels"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
