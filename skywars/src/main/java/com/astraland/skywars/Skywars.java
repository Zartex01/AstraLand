package com.astraland.skywars;

import com.astraland.skywars.commands.SkywarsCommand;
import com.astraland.skywars.listeners.SkywarsListener;
import com.astraland.skywars.managers.SkywarsManager;
import com.astraland.skywars.scoreboard.ScoreboardTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Skywars extends JavaPlugin {

    private static Skywars instance;
    private SkywarsManager skywarsManager;
    private ScoreboardTask scoreboardTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.skywarsManager = new SkywarsManager(this);

        getCommand("skywars").setExecutor(new SkywarsCommand(this));
        getCommand("skywars").setTabCompleter(new SkywarsCommand(this));

        getServer().getPluginManager().registerEvents(new SkywarsListener(this), this);

        this.scoreboardTask = new ScoreboardTask(this);
        this.scoreboardTask.start();

        getLogger().info("AstraLand - Skywars chargé !");
    }

    @Override
    public void onDisable() {
        if (scoreboardTask != null) scoreboardTask.stop();
        if (skywarsManager != null) skywarsManager.stopAll();
        getLogger().info("AstraLand - Skywars désactivé.");
    }

    public static Skywars getInstance() { return instance; }
    public SkywarsManager getSkywarsManager() { return skywarsManager; }

    public String getPluginWorld() { return getConfig().getString("skywars.lobby-world", "world_skywars"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
