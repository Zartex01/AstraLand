package com.astraland.oneblock.managers;

import com.astraland.oneblock.models.WeeklyMission;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WeeklyMissionManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    private final Map<UUID, List<WeeklyMission>> missions = new HashMap<>();
    private final Map<UUID, Long> generationTime = new HashMap<>();

    private static final long RESET_INTERVAL = 7L * 24 * 60 * 60 * 1000;

    public WeeklyMissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "weekly-missions.yml");
        load();
    }

    public List<WeeklyMission> getWeeklyMissions(UUID uuid) {
        checkReset(uuid);
        return missions.computeIfAbsent(uuid, k -> {
            generationTime.put(uuid, System.currentTimeMillis());
            save();
            return WeeklyMission.generateWeekly();
        });
    }

    private void checkReset(UUID uuid) {
        long gen = generationTime.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - gen >= RESET_INTERVAL) {
            missions.put(uuid, WeeklyMission.generateWeekly());
            generationTime.put(uuid, System.currentTimeMillis());
            save();
        }
    }

    public long getSecondsUntilReset(UUID uuid) {
        long gen = generationTime.getOrDefault(uuid, 0L);
        return Math.max(0, (RESET_INTERVAL - (System.currentTimeMillis() - gen)) / 1000);
    }

    public void addProgress(UUID uuid, WeeklyMission.MissionType type, long amount) {
        List<WeeklyMission> list = getWeeklyMissions(uuid);
        boolean changed = false;
        for (WeeklyMission m : list) {
            if (m.getType() == type && !m.isClaimed()) { m.addProgress(amount); changed = true; }
        }
        if (changed) save();
    }

    public boolean claimMission(UUID uuid, String missionId) {
        List<WeeklyMission> list = getWeeklyMissions(uuid);
        for (WeeklyMission m : list) {
            if (m.getId().equals(missionId) && m.isClaimable()) { m.claim(); save(); return true; }
        }
        return false;
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("players") == null) return;
        for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String base = "players." + uuidStr;
                generationTime.put(uuid, config.getLong(base + ".generation-time", 0L));
                List<WeeklyMission> list = new ArrayList<>();
                if (config.getConfigurationSection(base + ".missions") != null) {
                    for (String mId : config.getConfigurationSection(base + ".missions").getKeys(false)) {
                        String mp = base + ".missions." + mId;
                        String dname = config.getString(mp + ".name", mId);
                        String desc  = config.getString(mp + ".desc", "");
                        String iconName = config.getString(mp + ".icon", "STONE");
                        org.bukkit.Material icon;
                        try { icon = org.bukkit.Material.valueOf(iconName); } catch (Exception e) { icon = org.bukkit.Material.STONE; }
                        WeeklyMission.MissionType type;
                        try { type = WeeklyMission.MissionType.valueOf(config.getString(mp + ".type", "BREAK_BLOCKS")); }
                        catch (Exception e) { type = WeeklyMission.MissionType.BREAK_BLOCKS; }
                        long target = config.getLong(mp + ".target", 1000);
                        int reward = config.getInt(mp + ".reward", 1000);
                        long progress = config.getLong(mp + ".progress", 0);
                        boolean claimed = config.getBoolean(mp + ".claimed", false);
                        WeeklyMission wm = new WeeklyMission(mId, dname, desc, icon, type, target, reward);
                        wm.setProgress(progress);
                        if (claimed) wm.claim();
                        list.add(wm);
                    }
                }
                if (!list.isEmpty()) missions.put(uuid, list);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        missions.forEach((uuid, list) -> {
            String base = "players." + uuid;
            config.set(base + ".generation-time", generationTime.getOrDefault(uuid, 0L));
            list.forEach(m -> {
                String mp = base + ".missions." + m.getId();
                config.set(mp + ".name", m.getDisplayName());
                config.set(mp + ".desc", m.getDescription());
                config.set(mp + ".icon", m.getIcon().name());
                config.set(mp + ".type", m.getType().name());
                config.set(mp + ".target", m.getTarget());
                config.set(mp + ".reward", m.getReward());
                config.set(mp + ".progress", m.getProgress());
                config.set(mp + ".claimed", m.isClaimed());
            });
        });
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Erreur sauvegarde missions hebdo: " + e.getMessage());
        }
    }
}
