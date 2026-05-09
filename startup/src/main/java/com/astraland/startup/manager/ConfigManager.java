package com.astraland.startup.manager;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final AstraLandStartup plugin;

    private int compassSlot;
    private String compassName;
    private List<String> compassLore;
    private String guiTitle;
    private int guiRows;
    private final Map<String, WorldConfig> worlds = new LinkedHashMap<>();

    public ConfigManager(AstraLandStartup plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        worlds.clear();
        plugin.reloadConfig();

        compassSlot = plugin.getConfig().getInt("compass.slot", 4);
        compassName = plugin.getConfig().getString("compass.name", "&b&lSélecteur de Monde");
        compassLore = plugin.getConfig().getStringList("compass.lore");
        guiTitle = plugin.getConfig().getString("gui.title", "&8» &b&lAstraLand &8- &7Mondes «");
        guiRows = plugin.getConfig().getInt("gui.rows", 3);

        ConfigurationSection worldsSection = plugin.getConfig().getConfigurationSection("worlds");
        if (worldsSection == null) return;

        for (String key : worldsSection.getKeys(false)) {
            ConfigurationSection sec = worldsSection.getConfigurationSection(key);
            if (sec == null) continue;

            String name = sec.getString("name", key);
            List<String> lore = sec.getStringList("lore");
            String worldName = sec.getString("world", key);
            int slot = sec.getInt("slot", 0);

            Material material = Material.GRASS_BLOCK;
            String itemStr = sec.getString("item", "GRASS_BLOCK");
            try {
                material = Material.valueOf(itemStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Matériau invalide '" + itemStr + "' pour le monde '" + key + "'. GRASS_BLOCK utilisé.");
            }

            worlds.put(key, new WorldConfig(name, lore, worldName, material, slot));
        }
    }

    public int getCompassSlot() { return compassSlot; }
    public String getCompassName() { return compassName; }
    public List<String> getCompassLore() { return compassLore; }
    public String getGuiTitle() { return guiTitle; }
    public int getGuiRows() { return guiRows; }
    public Map<String, WorldConfig> getWorlds() { return worlds; }

    public static class WorldConfig {
        private final String name;
        private final List<String> lore;
        private final String worldName;
        private final Material material;
        private final int slot;

        public WorldConfig(String name, List<String> lore, String worldName, Material material, int slot) {
            this.name = name;
            this.lore = lore;
            this.worldName = worldName;
            this.material = material;
            this.slot = slot;
        }

        public String getName() { return name; }
        public List<String> getLore() { return lore; }
        public String getWorldName() { return worldName; }
        public Material getMaterial() { return material; }
        public int getSlot() { return slot; }
    }
}
