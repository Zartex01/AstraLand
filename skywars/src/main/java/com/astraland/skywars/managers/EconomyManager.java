package com.astraland.skywars.managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class EconomyManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "economy.yml");
        load();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public int getBalance(UUID uuid) { return config.getInt(uuid.toString(), 0); }

    public void addBalance(UUID uuid, int amount) {
        config.set(uuid.toString(), getBalance(uuid) + amount);
        save();
    }

    public boolean removeBalance(UUID uuid, int amount) {
        int current = getBalance(uuid);
        if (current < amount) return false;
        config.set(uuid.toString(), current - amount);
        save();
        return true;
    }

    public void setBalance(UUID uuid, int amount) {
        config.set(uuid.toString(), Math.max(0, amount));
        save();
    }

    private void save() {
        try { config.save(file); } catch (Exception e) {
            plugin.getLogger().warning("Erreur sauvegarde économie : " + e.getMessage());
        }
    }
}
