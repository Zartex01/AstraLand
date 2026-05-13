package com.astraland.oneblock.managers;

import com.astraland.oneblock.models.Boost;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoostManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, Boost> boosts = new HashMap<>();

    public BoostManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "boosts.yml");
        load();
    }

    public boolean hasBoost(UUID islandOwner) {
        Boost b = boosts.get(islandOwner);
        if (b != null && !b.isActive()) { boosts.remove(islandOwner); return false; }
        return b != null;
    }

    public Boost getBoost(UUID islandOwner) {
        Boost b = boosts.get(islandOwner);
        if (b != null && !b.isActive()) { boosts.remove(islandOwner); return null; }
        return b;
    }

    public double getMultiplier(UUID islandOwner) {
        Boost b = getBoost(islandOwner);
        return b != null ? b.getMultiplier() : 1.0;
    }

    public void activateBoost(UUID islandOwner, Boost.BoostType type) {
        boosts.put(islandOwner, new Boost(type));
        save();
    }

    public void removeBoost(UUID islandOwner) {
        boosts.remove(islandOwner);
        save();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("boosts") == null) return;
        for (String uuidStr : config.getConfigurationSection("boosts").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String typeName = config.getString("boosts." + uuidStr + ".type");
                long expiry = config.getLong("boosts." + uuidStr + ".expiry");
                Boost.BoostType type = Boost.BoostType.valueOf(typeName);
                Boost boost = new Boost(type, expiry);
                if (boost.isActive()) boosts.put(uuid, boost);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        boosts.forEach((uuid, boost) -> {
            if (!boost.isActive()) return;
            String path = "boosts." + uuid;
            config.set(path + ".type", boost.getType().name());
            config.set(path + ".expiry", boost.getExpiryTime());
        });
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Erreur sauvegarde boosts: " + e.getMessage());
        }
    }
}
