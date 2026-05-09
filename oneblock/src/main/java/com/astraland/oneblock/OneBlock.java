package com.astraland.oneblock;

import com.astraland.oneblock.commands.OneBlockCommand;
import com.astraland.oneblock.listeners.OneBlockListener;
import com.astraland.oneblock.managers.OneBlockManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class OneBlock extends JavaPlugin {

    private static OneBlock instance;
    private OneBlockManager oneBlockManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.oneBlockManager = new OneBlockManager(this);

        getCommand("oneblock").setExecutor(new OneBlockCommand(this));
        getCommand("oneblock").setTabCompleter(new OneBlockCommand(this));

        getServer().getPluginManager().registerEvents(new OneBlockListener(this), this);
        getLogger().info("AstraLand - OneBlock chargé !");
    }

    @Override
    public void onDisable() {
        if (oneBlockManager != null) oneBlockManager.saveAll();
        getLogger().info("AstraLand - OneBlock désactivé.");
    }

    public static OneBlock getInstance() { return instance; }
    public OneBlockManager getOneBlockManager() { return oneBlockManager; }

    public String getPluginWorld() { return getConfig().getString("oneblock.world", "world_oneblock"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
