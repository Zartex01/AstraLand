package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.database.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.util.*;

public class KitManager {

    private final PvpFactions plugin;
    private final DatabaseManager db;

    // Cache en mémoire : joueur -> (kit -> lastUse timestamp)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public KitManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        loadAll();
    }

    private void loadAll() {
        try {
            ResultSet rs = db.query("SELECT * FROM kit_cooldowns");
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                String kit = rs.getString("kit_name");
                long lastUse = rs.getLong("last_use");
                cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(kit, lastUse);
            }
            rs.close();
        } catch (Exception e) {
            plugin.getLogger().severe("[DB] Erreur chargement kit cooldowns : " + e.getMessage());
        }
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

        if (getCooldownRemaining(player.getUniqueId(), kitName) > 0) return false;

        player.getInventory().clear();

        for (String itemStr : kitSec.getStringList("items")) {
            String[] parts = itemStr.split(":");
            try {
                Material mat = Material.valueOf(parts[0].toUpperCase());
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                player.getInventory().addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }

        List<String> armor = kitSec.getStringList("armor");
        if (armor.size() > 0) trySetArmor(player, armor.get(0), 3);
        if (armor.size() > 1) trySetArmor(player, armor.get(1), 2);
        if (armor.size() > 2) trySetArmor(player, armor.get(2), 1);
        if (armor.size() > 3) trySetArmor(player, armor.get(3), 0);

        long now = System.currentTimeMillis();
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(kitName, now);
        db.upsertKitCooldown(player.getUniqueId().toString(), kitName, now);
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
}
