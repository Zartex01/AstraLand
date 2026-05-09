package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {

    private final PvpFactions plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private File dataFile;

    public KitManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "kit_cooldowns.yml");
        load();
    }

    public Set<String> getKitNames() {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("kits");
        return sec != null ? sec.getKeys(false) : new HashSet<>();
    }

    public boolean kitExists(String name) {
        return plugin.getConfig().getConfigurationSection("kits." + name.toUpperCase()) != null;
    }

    public long getCooldownRemaining(UUID uuid, String kit) {
        Map<String, Long> playerCools = cooldowns.get(uuid);
        if (playerCools == null) return 0;
        long lastUse = playerCools.getOrDefault(kit.toUpperCase(), 0L);
        long cooldownSec = plugin.getConfig().getLong("kits." + kit.toUpperCase() + ".cooldown", 3600);
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return Math.max(0, cooldownSec - elapsed);
    }

    public boolean giveKit(Player player, String kitName) {
        kitName = kitName.toUpperCase();
        ConfigurationSection kitSec = plugin.getConfig().getConfigurationSection("kits." + kitName);
        if (kitSec == null) return false;

        String permission = kitSec.getString("permission", "");
        if (!permission.isEmpty() && !player.hasPermission(permission)) return false;

        long remaining = getCooldownRemaining(player.getUniqueId(), kitName);
        if (remaining > 0) return false;

        player.getInventory().clear();

        List<String> items = kitSec.getStringList("items");
        for (String itemStr : items) {
            String[] parts = itemStr.split(":");
            try {
                Material mat = Material.valueOf(parts[0].toUpperCase());
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                player.getInventory().addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }

        if (kitSec.getStringList("armor").size() > 0) {
            List<String> armor = kitSec.getStringList("armor");
            if (armor.size() > 0) trySetArmor(player, armor.get(0), 3);
            if (armor.size() > 1) trySetArmor(player, armor.get(1), 2);
            if (armor.size() > 2) trySetArmor(player, armor.get(2), 1);
            if (armor.size() > 3) trySetArmor(player, armor.get(3), 0);
        }

        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(kitName, System.currentTimeMillis());
        save();
        return true;
    }

    private void trySetArmor(Player player, String matStr, int slot) {
        try {
            ItemStack item = new ItemStack(Material.valueOf(matStr.toUpperCase()));
            switch (slot) {
                case 3 -> player.getInventory().setHelmet(item);
                case 2 -> player.getInventory().setChestplate(item);
                case 1 -> player.getInventory().setLeggings(item);
                case 0 -> player.getInventory().setBoots(item);
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Long>> e : cooldowns.entrySet()) {
            for (Map.Entry<String, Long> c : e.getValue().entrySet()) {
                cfg.set("cooldowns." + e.getKey() + "." + c.getKey(), c.getValue());
            }
        }
        try { cfg.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("cooldowns") == null) return;
        for (String uuidStr : cfg.getConfigurationSection("cooldowns").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection sec = cfg.getConfigurationSection("cooldowns." + uuidStr);
                if (sec == null) continue;
                Map<String, Long> map = new HashMap<>();
                for (String kit : sec.getKeys(false)) map.put(kit, cfg.getLong("cooldowns." + uuidStr + "." + kit));
                cooldowns.put(uuid, map);
            } catch (Exception ignored) {}
        }
    }
}
