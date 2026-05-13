package com.astraland.oneblock.managers;

import com.astraland.oneblock.models.Skill;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SkillManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public SkillManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "skills.yml");
        load();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    private String key(UUID uuid, Skill skill) {
        return uuid.toString() + "." + skill.getId();
    }

    public long getXP(UUID uuid, Skill skill) {
        return config.getLong(key(uuid, skill), 0);
    }

    public int getLevel(UUID uuid, Skill skill) {
        return skill.levelFromXp(getXP(uuid, skill));
    }

    public void addXP(UUID uuid, Skill skill, long amount) {
        long current = getXP(uuid, skill);
        config.set(key(uuid, skill), current + amount);
        save();
    }

    public double getTotalMoneyMultiplier(UUID uuid) {
        double bonus = 0;
        for (Skill skill : Skill.values()) {
            bonus += skill.getMoneyMultiplierBonus(getLevel(uuid, skill));
        }
        return 1.0 + bonus;
    }

    public double getTotalLootChanceBonus(UUID uuid) {
        double bonus = 0;
        for (Skill skill : Skill.values()) {
            bonus += skill.getLootChanceBonus(getLevel(uuid, skill));
        }
        return bonus;
    }

    private void save() {
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Erreur sauvegarde skills : " + e.getMessage());
        }
    }
}
