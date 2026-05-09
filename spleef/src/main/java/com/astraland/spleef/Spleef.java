package com.astraland.spleef;

import com.astraland.spleef.commands.SpleefCommand;
import com.astraland.spleef.listeners.SpleefListener;
import com.astraland.spleef.managers.SpleefManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Spleef extends JavaPlugin {

    private static Spleef instance;
    private SpleefManager spleefManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.spleefManager = new SpleefManager(this);

        getCommand("spleef").setExecutor(new SpleefCommand(this));
        getCommand("spleef").setTabCompleter(new SpleefCommand(this));

        getServer().getPluginManager().registerEvents(new SpleefListener(this), this);
        getLogger().info("AstraLand - Spleef chargé !");
    }

    @Override
    public void onDisable() {
        if (spleefManager != null) spleefManager.stopAll();
        getLogger().info("AstraLand - Spleef désactivé.");
    }

    public static Spleef getInstance() { return instance; }
    public SpleefManager getSpleefManager() { return spleefManager; }

    public String getPluginWorld() { return getConfig().getString("spleef.lobby-world", "world_spleef"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
