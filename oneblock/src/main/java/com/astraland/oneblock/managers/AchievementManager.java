package com.astraland.oneblock.managers;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OBAchievement;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AchievementManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, Set<String>> achievements = new HashMap<>();

    public AchievementManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "achievements.yml");
        load();
    }

    public boolean hasAchievement(UUID uuid, OBAchievement ach) {
        return achievements.getOrDefault(uuid, Set.of()).contains(ach.name());
    }

    public Set<String> getUnlocked(UUID uuid) {
        return achievements.getOrDefault(uuid, new HashSet<>());
    }

    public int countUnlocked(UUID uuid) { return getUnlocked(uuid).size(); }

    public boolean unlock(UUID uuid, OBAchievement ach) {
        Set<String> set = achievements.computeIfAbsent(uuid, k -> new HashSet<>());
        if (set.contains(ach.name())) return false;
        set.add(ach.name());
        save();
        return true;
    }

    public void checkAndUnlock(Player player, OneBlockIsland island,
                               OBAchievement.AchType type, long value,
                               OneBlock plugin, boolean broadcast) {
        for (OBAchievement ach : OBAchievement.values()) {
            if (ach.getType() != type) continue;
            if (hasAchievement(player.getUniqueId(), ach)) continue;
            if (value >= ach.getThreshold()) {
                if (unlock(player.getUniqueId(), ach)) {
                    plugin.getPlayerStatsManager().increment(player.getUniqueId(),
                        PlayerStatsManager.Stat.ACHIEVEMENTS);
                    if (ach.getReward() > 0) {
                        plugin.getEconomyManager().addBalance(player.getUniqueId(), ach.getReward());
                    }
                    org.bukkit.ChatColor cc = org.bukkit.ChatColor.GOLD;
                    player.sendTitle(cc + "" + org.bukkit.ChatColor.BOLD + "✦ Succès Débloqué ✦",
                        org.bukkit.ChatColor.YELLOW + ach.getDisplayName(), 10, 70, 20);
                    player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        "&6&l★ SUCCÈS : &e" + ach.getDisplayName()
                            + (ach.getReward() > 0 ? " &6+&e" + ach.getReward() + " &6pièces !" : " !")));
                    player.playSound(player.getLocation(),
                        org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
                    if (broadcast) {
                        org.bukkit.Bukkit.broadcastMessage(
                            org.bukkit.ChatColor.translateAlternateColorCodes('&',
                                "&6[Succès] &e" + player.getName() + " &7a débloqué : &6" + ach.getDisplayName() + " !"));
                    }
                }
            }
        }
    }

    private EconomyManager getEconomyManager() {
        return ((OneBlock) plugin).getEconomyManager();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("players") == null) return;
        for (String uuidStr : config.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                achievements.put(uuid, new HashSet<>(config.getStringList("players." + uuidStr + ".unlocked")));
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        achievements.forEach((uuid, set) ->
            config.set("players." + uuid + ".unlocked", new ArrayList<>(set)));
        try { config.save(file); } catch (IOException e) {
            plugin.getLogger().warning("Erreur sauvegarde achievements: " + e.getMessage());
        }
    }
}
