package com.astraland.cosmetics;

import com.astraland.cosmetics.command.CosmetiquesCommand;
import com.astraland.cosmetics.command.SkinCommand;
import com.astraland.cosmetics.gui.CosmetiquesGUI;
import com.astraland.cosmetics.listener.CosmetiquesListener;
import com.astraland.cosmetics.manager.CosmetiquesManager;
import com.astraland.cosmetics.manager.SkinManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Cosmetics extends JavaPlugin {

    private static Cosmetics instance;

    private SkinManager          skinManager;
    private CosmetiquesManager   cosmetiquesManager;
    private CosmetiquesGUI       cosmetiquesGUI;
    private CosmetiquesListener  cosmetiquesListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.skinManager         = new SkinManager(this);
        this.cosmetiquesManager  = new CosmetiquesManager(this);
        this.cosmetiquesGUI      = new CosmetiquesGUI(this);
        this.cosmetiquesListener = new CosmetiquesListener(this);

        SkinCommand skinCmd = new SkinCommand(this);
        getCommand("skin").setExecutor(skinCmd);
        getCommand("skin").setTabCompleter(skinCmd);

        getCommand("cosmetique").setExecutor(new CosmetiquesCommand(this));

        getServer().getPluginManager().registerEvents(cosmetiquesListener, this);

        getLogger().info("=====================================");
        getLogger().info(" AstraLand - Cosmetics chargé !");
        getLogger().info(" /skin <pseudo>   — Changer son skin");
        getLogger().info(" /cosmetique      — Menu cosmétiques");
        getLogger().info(" Cosmétiques : " + cosmetiquesManager.getListe().size());
        getLogger().info("=====================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("AstraLand - Cosmetics désactivé.");
    }

    public static Cosmetics getInstance()              { return instance; }
    public SkinManager getSkinManager()                { return skinManager; }
    public CosmetiquesManager getCosmetiquesManager()  { return cosmetiquesManager; }
    public CosmetiquesGUI getCosmetiquesGUI()          { return cosmetiquesGUI; }
    public CosmetiquesListener getCosmetiquesListener(){ return cosmetiquesListener; }
}
