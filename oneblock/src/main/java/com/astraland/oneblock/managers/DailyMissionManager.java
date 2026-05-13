package com.astraland.oneblock.managers;

import com.astraland.oneblock.models.DailyMission;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DailyMissionManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    private final Map<UUID, List<DailyMission>> missions = new HashMap<>();
    private final Map<UUID, Long> generationTime = new HashMap<>();

    private static final long RESET_INTERVAL = 24L * 60 * 60 * 1000;

    public DailyMissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "missions.yml");
        load();
    }

    public List<DailyMission> getDailyMissions(UUID uuid) {
        checkReset(uuid);
        return missions.computeIfAbsent(uuid, k -> {
            List<DailyMission> list = DailyMission.generateDaily();
            generationTime.put(uuid, System.currentTimeMillis());
            save();
            return list;
        });
    }

    private void checkReset(UUID uuid) {
        long gen = generationTime.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - gen >= RESET_INTERVAL) {
            missions.put(uuid, DailyMission.generateDaily());
            generationTime.put(uuid, System.currentTimeMillis());
            save();
        }
    }

    public long getSecondsUntilReset(UUID uuid) {
        long gen = generationTime.getOrDefault(uuid, 0L);
        long elapsed = System.currentTimeMillis() - gen;
        long remaining = RESET_INTERVAL - elapsed;
        return Math.max(0, remaining / 1000);
    }

    public void addProgress(UUID uuid, DailyMission.MissionType type, long amount) {
        List<DailyMission> list = getDailyMissions(uuid);
        boolean changed = false;
        for (DailyMission m : list) {
            if (m.getType() == type && !m.isClaimed()) {
                m.addProgress(amount);
                changed = true;
            }
        }
        if (changed) save();
    }

    public boolean claimMission(UUID uuid, String missionId) {
        List<DailyMission> list = getDailyMissions(uuid);
        for (DailyMission m : list) {
            if (m.getId().equals(missionId) && m.isClaimable()) {
                m.claim();
                save();
                return true;
            }
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
                List<DailyMission> list = new ArrayList<>();
                if (config.getConfigurationSection(base + ".missions") != null) {
                    for (String mId : config.getConfigurationSection(base + ".missions").getKeys(false)) {
                        String mp = base + ".missions." + mId;
                        String displayName = config.getString(mp + ".name", mId);
                        String desc = config.getString(mp + ".desc", "");
                        String iconName = config.getString(mp + ".icon", "STONE");
                        org.bukkit.Material icon;
                        try { icon = org.bukkit.Material.valueOf(iconName); } catch (Exception e) { icon = org.bukkit.Material.STONE; }
                        String typeName = config.getString(mp + ".type", "BREAK_BLOCKS");
                        DailyMission.MissionType type;
                        try { type = DailyMission.MissionType.valueOf(typeName); } catch (Exception e) { type = DailyMission.MissionType.BREAK_BLOCKS; }
                        long target = config.getLong(mp + ".target", 100);
                        int reward = config.getInt(mp + ".reward", 200);
                        long progress = config.getLong(mp + ".progress", 0);
                        boolean claimed = config.getBoolean(mp + ".claimed", false);

                        DailyMission dm = new DailyMission(mId, displayName, desc, icon, type, target, reward);
                        dm.setProgress(progress);
                        if (claimed) dm.claim();
                        list.add(dm);
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
            plugin.getLogger().warning("Erreur sauvegarde missions : " + e.getMessage());
        }
    }
}
