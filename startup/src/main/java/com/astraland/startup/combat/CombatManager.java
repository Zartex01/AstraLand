package com.astraland.startup.combat;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CombatManager {

    private static final long COMBAT_MS = 30_000L;

    private final AstraLandStartup plugin;
    private final Map<UUID, Long> tagged = new HashMap<>();
    private final Set<UUID> quitInCombat = new HashSet<>();
    private final File dataFile;
    private YamlConfiguration data;

    public CombatManager(AstraLandStartup plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "combat_quit.yml");
        load();
    }

    public void tag(UUID uuid) {
        tagged.put(uuid, System.currentTimeMillis());
    }

    public boolean isInCombat(UUID uuid) {
        Long last = tagged.get(uuid);
        if (last == null) return false;
        if (System.currentTimeMillis() - last > COMBAT_MS) {
            tagged.remove(uuid);
            return false;
        }
        return true;
    }

    public long remainingSeconds(UUID uuid) {
        Long last = tagged.get(uuid);
        if (last == null) return 0;
        return Math.max(0, (COMBAT_MS - (System.currentTimeMillis() - last)) / 1000);
    }

    public void clearTag(UUID uuid) {
        tagged.remove(uuid);
    }

    public void flagQuit(UUID uuid) {
        quitInCombat.add(uuid);
        data.set(uuid.toString(), true);
        save();
    }

    public boolean hasQuitFlag(UUID uuid) {
        return quitInCombat.contains(uuid);
    }

    public void clearQuitFlag(UUID uuid) {
        quitInCombat.remove(uuid);
        data.set(uuid.toString(), null);
        save();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) try { dataFile.createNewFile(); } catch (Exception ignored) {}
        data = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : data.getKeys(false)) {
            try { quitInCombat.add(UUID.fromString(key)); } catch (Exception ignored) {}
        }
    }

    private void save() {
        try { data.save(dataFile); } catch (Exception e) {
            plugin.getLogger().warning("Erreur sauvegarde combat : " + e.getMessage());
        }
    }
}
