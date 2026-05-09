package com.astraland.uhc;

import com.astraland.uhc.commands.UHCCommand;
import com.astraland.uhc.listeners.UHCListener;
import com.astraland.uhc.managers.UHCManager;
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
}
