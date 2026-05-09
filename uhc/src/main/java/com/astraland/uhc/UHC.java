package com.astraland.uhc;

import com.astraland.uhc.commands.UHCCommand;
import com.astraland.uhc.listeners.UHCListener;
import com.astraland.uhc.managers.UHCManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UHC extends JavaPlugin {

    private static UHC instance;
    private UHCManager uhcManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.uhcManager = new UHCManager(this);

        getCommand("uhc").setExecutor(new UHCCommand(this));
        getCommand("uhc").setTabCompleter(new UHCCommand(this));

        getServer().getPluginManager().registerEvents(new UHCListener(this), this);
        getLogger().info("AstraLand - UHC chargé !");
    }

    @Override
    public void onDisable() {
        if (uhcManager != null) uhcManager.stopGame();
        getLogger().info("AstraLand - UHC désactivé.");
    }

    public static UHC getInstance() { return instance; }
    public UHCManager getUhcManager() { return uhcManager; }

    public String getPluginWorld() { return getConfig().getString("uhc.world", "world_uhc"); }
    public boolean isInPluginWorld(Player player) { return player.getWorld().getName().equals(getPluginWorld()); }
    public String wrongWorldMsg() { return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cCette commande est uniquement disponible dans le monde &e" + getPluginWorld() + "&c."); }
}
