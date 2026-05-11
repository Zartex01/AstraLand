package com.astraland.skyblock;

import com.astraland.skyblock.challenges.ChallengeManager;
import com.astraland.skyblock.commands.*;
import com.astraland.skyblock.listeners.AHListener;
import com.astraland.skyblock.listeners.IslandListener;
import com.astraland.skyblock.listeners.ShopListener;
import com.astraland.skyblock.managers.AuctionManager;
import com.astraland.skyblock.managers.EconomyManager;
import com.astraland.skyblock.managers.IslandLevelManager;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.quests.DailyQuestManager;
import com.astraland.skyblock.scoreboard.ScoreboardTask;
import com.astraland.skyblock.shop.ShopMenuGUI;
import com.astraland.skyblock.tasks.IslandBorderTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Skyblock extends JavaPlugin {

    private static Skyblock instance;
    private IslandManager      islandManager;
    private IslandLevelManager levelManager;
    private EconomyManager     economyManager;
    private AuctionManager     auctionManager;
    private ChallengeManager   challengeManager;
    private DailyQuestManager  questManager;
    private IslandBorderTask   borderTask;
    private ScoreboardTask     scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.islandManager    = new IslandManager(this);
        this.levelManager     = new IslandLevelManager();
        this.economyManager   = new EconomyManager(this);
        this.auctionManager   = new AuctionManager(this);
        this.challengeManager = new ChallengeManager(this);
        this.questManager     = new DailyQuestManager(this);
        this.borderTask       = new IslandBorderTask(this);

        // /island (alias /is /sb /skyblock)
        IslandCommand islandCmd = new IslandCommand(this);
        getCommand("island").setExecutor(islandCmd);
        getCommand("island").setTabCompleter(islandCmd);

        // /balance /baltop
        EconomyCommand ecoCmd = new EconomyCommand(this);
        getCommand("balance").setExecutor(ecoCmd);
        getCommand("balance").setTabCompleter(ecoCmd);
        getCommand("baltop").setExecutor(ecoCmd);

        // /pay
        getCommand("pay").setExecutor(ecoCmd);
        getCommand("pay").setTabCompleter(ecoCmd);

        // /shop
        getCommand("shop").setExecutor(new ShopCommand(this));

        // /sell all | /sell hand
        SellAllCommand sellCmd = new SellAllCommand(this);
        getCommand("sell").setExecutor(sellCmd);
        getCommand("sell").setTabCompleter(sellCmd);

        // /ah
        AHCommand ahCmd = new AHCommand(this);
        getCommand("ah").setExecutor(ahCmd);
        getCommand("ah").setTabCompleter(ahCmd);

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

        // Bordure de particules
        this.borderTask.start();

        getLogger().info("AstraLand - Skyblock chargé avec "
            + challengeManager.getAllChallenges().size() + " défis, "
            + questManager.getClass().getSimpleName() + " actif !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (borderTask     != null) borderTask.stop();
        if (islandManager  != null) islandManager.saveAll();
        getLogger().info("AstraLand - Skyblock désactivé.");
    }

    public static Skyblock getInstance()           { return instance; }
    public IslandManager getIslandManager()        { return islandManager; }
    public IslandLevelManager getLevelManager()    { return levelManager; }
    public EconomyManager getEconomyManager()      { return economyManager; }
    public AuctionManager getAuctionManager()      { return auctionManager; }
    public ChallengeManager getChallengeManager()  { return challengeManager; }
    public DailyQuestManager getQuestManager()     { return questManager; }
    public IslandBorderTask getBorderTask()        { return borderTask; }

    public ShopMenuGUI newShopMenuGUI(Player player) { return new ShopMenuGUI(player, economyManager); }

    public String getPluginWorld()               { return getConfig().getString("island.world", "world_skyblock"); }
    public boolean isInPluginWorld(Player player){ return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg()                {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&',
            "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c.");
    }
}
