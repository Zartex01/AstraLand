package com.astraland.oneblock.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandVaultManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<UUID, Inventory> vaults = new HashMap<>();

    public static final String VAULT_TITLE = ChatColor.translateAlternateColorCodes('&', "&8[&6Coffre d'Île&8]");

    public IslandVaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "island-vaults.yml");
        load();
    }

    public Inventory getVault(UUID islandOwner) {
        return vaults.computeIfAbsent(islandOwner,
            k -> Bukkit.createInventory(null, 54, VAULT_TITLE));
    }

    public void saveVault(UUID islandOwner) {
        Inventory inv = vaults.get(islandOwner);
        if (inv == null) return;
        try {
            config.set("vaults." + islandOwner, itemsToBase64(inv.getContents()));
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Erreur sauvegarde vault " + islandOwner + ": " + e.getMessage());
        }
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) try { file.createNewFile(); } catch (Exception ignored) {}
        config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("vaults") == null) return;
        for (String uuidStr : config.getConfigurationSection("vaults").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String encoded = config.getString("vaults." + uuidStr);
                if (encoded == null || encoded.isEmpty()) continue;
                ItemStack[] items = itemsFromBase64(encoded);
                Inventory inv = Bukkit.createInventory(null, 54, VAULT_TITLE);
                inv.setContents(items);
                vaults.put(uuid, inv);
            } catch (Exception ignored) {}
        }
    }

    private String itemsToBase64(ItemStack[] items) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream data = new BukkitObjectOutputStream(bos)) {
            data.writeInt(items.length);
            for (ItemStack item : items) data.writeObject(item);
        }
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private ItemStack[] itemsFromBase64(String data) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(data);
        try (BukkitObjectInputStream bis = new BukkitObjectInputStream(new ByteArrayInputStream(bytes))) {
            int len = bis.readInt();
            ItemStack[] items = new ItemStack[len];
            for (int i = 0; i < len; i++) items[i] = (ItemStack) bis.readObject();
            return items;
        }
    }
}
