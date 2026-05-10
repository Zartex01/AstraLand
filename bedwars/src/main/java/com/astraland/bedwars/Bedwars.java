package com.astraland.bedwars;

import com.astraland.bedwars.commands.AHCommand;
import com.astraland.bedwars.commands.BedwarsCommand;
import com.astraland.bedwars.commands.EconomyCommand;
import com.astraland.bedwars.commands.ShopCommand;
import com.astraland.bedwars.listeners.AHListener;
import com.astraland.bedwars.listeners.BedwarsListener;
import com.astraland.bedwars.listeners.ShopListener;
import com.astraland.bedwars.managers.ArenaManager;
import com.astraland.bedwars.managers.AuctionManager;
import com.astraland.bedwars.managers.EconomyManager;
import com.astraland.bedwars.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Bedwars extends JavaPlugin {

    private static Bedwars instance;
    private ArenaManager arenaManager;
    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.arenaManager   = new ArenaManager(this);
        this.economyManager = new EconomyManager(this);
        this.auctionManager = new AuctionManager(this);

        getCommand("bedwars").setExecutor(new BedwarsCommand(this));
        getCommand("bedwars").setTabCompleter(new BedwarsCommand(this));

        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("pay").setExecutor(ecoCmd);

        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("ah").setExecutor(new AHCommand(this));

        getServer().getPluginManager().registerEvents(new BedwarsListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new AHListener(), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Bedwars chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (arenaManager != null) arenaManager.saveAll();
        getLogger().info("AstraLand - Bedwars désactivé.");
    }

    public static Bedwars getInstance()         { return instance; }
    public ArenaManager getArenaManager()       { return arenaManager; }
    public EconomyManager getEconomyManager()   { return economyManager; }
    public AuctionManager getAuctionManager()   { return auctionManager; }

    public String getPluginWorld() { return getConfig().getString("bedwars.lobby-world", "world_bedwars"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
