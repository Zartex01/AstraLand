package com.astraland.cosmetics.manager;

import com.astraland.cosmetics.Cosmetics;
import com.astraland.cosmetics.model.CosmetiqueData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CosmetiquesManager {

    private final Cosmetics plugin;
    private final Map<String, CosmetiqueData> cosmetiques = new LinkedHashMap<>();
    private File configFile;
    private FileConfiguration config;

    public CosmetiquesManager(Cosmetics plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        cosmetiques.clear();

        configFile = new File(plugin.getDataFolder(), "cosmetiques.yml");
        if (!configFile.exists()) {
            plugin.saveResource("cosmetiques.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection section = config.getConfigurationSection("cosmetiques");

        if (section == null) {
            plugin.getLogger().warning("Aucun cosmétique trouvé dans cosmetiques.yml !");
            return;
        }

        int autoSlot = 0;

        for (String id : section.getKeys(false)) {
            ConfigurationSection cs = section.getConfigurationSection(id);
            if (cs == null) continue;

            String nom         = color(cs.getString("nom", "&7" + id));
            List<String> desc  = colorList(cs.getStringList("description"));
            String item        = cs.getString("item", "PAPER").toUpperCase();
            boolean enchante   = cs.getBoolean("enchante", false);
            int slot           = cs.getInt("slot", -1);
            String permission  = cs.getString("permission", "");
            String cmdJoueur   = cs.getString("commande-joueur", "");
            String cmdConsole  = cs.getString("commande-console", "");

            if (slot < 0 || slot > 53) {
                slot = nextAutoSlot(autoSlot);
                autoSlot = slot + 1;
            }

            cosmetiques.put(id, new CosmetiqueData(id, nom, desc, item, enchante, slot,
                    permission, cmdJoueur, cmdConsole));
        }

        plugin.getLogger().info(cosmetiques.size() + " cosmétique(s) chargé(s).");
    }

    public void reload() {
        load();
    }

    public Map<String, CosmetiqueData> getCosmetiques() {
        return cosmetiques;
    }

    public List<CosmetiqueData> getListe() {
        return new ArrayList<>(cosmetiques.values());
    }

    private int nextAutoSlot(int from) {
        int slot = from;
        while (isControlSlot(slot) && slot < 54) slot++;
        return Math.min(slot, 53);
    }

    private boolean isControlSlot(int slot) {
        return slot >= 45;
    }

    private String color(String text) {
        return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
    }

    private List<String> colorList(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String line : list) result.add(color(line));
        return result;
    }
}
