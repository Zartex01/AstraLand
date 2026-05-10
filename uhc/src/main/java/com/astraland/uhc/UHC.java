package com.astraland.uhc;

import com.astraland.uhc.commands.AHCommand;
import com.astraland.uhc.commands.EconomyCommand;
import com.astraland.uhc.commands.ShopCommand;
import com.astraland.uhc.commands.UHCCommand;
import com.astraland.uhc.listeners.AHListener;
import com.astraland.uhc.listeners.ShopListener;
import com.astraland.uhc.listeners.UHCListener;
import com.astraland.uhc.managers.AuctionManager;
import com.astraland.uhc.managers.EconomyManager;
import com.astraland.uhc.managers.UHCManager;
import com.astraland.uhc.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UHC extends JavaPlugin {

    private static UHC instance;
    private UHCManager uhcManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.uhcManager     = new UHCManager(this);
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);

        getCommand("uhc").setExecutor(new UHCCommand(this));
        getCommand("uhc").setTabCompleter(new UHCCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));

        getServer().getPluginManager().registerEvents(new UHCListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - UHC chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (uhcManager != null) uhcManager.stopGame();
        getLogger().info("AstraLand - UHC désactivé.");
    }

    public static UHC getInstance()             { return instance; }
    public UHCManager getUhcManager()           { return uhcManager; }
    public EconomyManager getEconomyManager()   { return economyManager; }
    public AuctionManager getAuctionManager()   { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("uhc.world", "world_uhc"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
