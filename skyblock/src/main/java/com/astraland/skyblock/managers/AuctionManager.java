package com.astraland.skyblock.managers;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.ah.AuctionListing;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AuctionManager {

    public static final int  MAX_PER_PLAYER    = 5;
    public static final long EXPIRY_MILLIS      = 48L * 60 * 60 * 1000; // 48 heures

    private final Skyblock plugin;
    private final List<AuctionListing>         listings      = new ArrayList<>();
    private final Map<UUID, List<ItemStack>>   claimedItems  = new HashMap<>();
    private final File dataFile;
    private YamlConfiguration data;

    public AuctionManager(Skyblock plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "ah.yml");
        load();
        scheduleExpiryCheck();
    }

    // ─── API ──────────────────────────────────────────────────────────────────

    public void addListing(AuctionListing listing) { listings.add(listing); saveListing(listing); }

    public void removeListing(String id) {
        listings.removeIf(l -> l.getId().equals(id));
        data.set("listings." + id, null);
        save();
    }

    public List<AuctionListing> getListings() { return Collections.unmodifiableList(listings); }

    public List<AuctionListing> getPlayerListings(UUID seller) {
        return listings.stream().filter(l -> l.getSeller().equals(seller)).collect(Collectors.toList());
    }

    public long getPlayerListingCount(UUID seller) {
        return listings.stream().filter(l -> l.getSeller().equals(seller)).count();
    }

    // ─── Items réclamés (expiré ou annulé hors ligne) ─────────────────────────

    public List<ItemStack> getClaimedItems(UUID uuid) {
        return claimedItems.getOrDefault(uuid, Collections.emptyList());
    }

    public void clearClaimedItems(UUID uuid) {
        claimedItems.remove(uuid);
        data.set("claimed." + uuid, null);
        save();
    }

    private void addClaimedItem(UUID uuid, ItemStack item) {
        claimedItems.computeIfAbsent(uuid, k -> new ArrayList<>()).add(item);
        // Persister
        List<String> encoded = new ArrayList<>();
        for (ItemStack it : claimedItems.get(uuid)) {
            String s = serializeItem(it);
            if (s != null) encoded.add(s);
        }
        data.set("claimed." + uuid, encoded);
        save();
    }

    // ─── Expiration automatique ────────────────────────────────────────────────

    private void scheduleExpiryCheck() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            List<AuctionListing> expired = listings.stream()
                .filter(l -> now - l.getListedAt() > EXPIRY_MILLIS)
                .collect(Collectors.toList());
            if (expired.isEmpty()) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (AuctionListing l : expired) {
                    listings.remove(l);
                    data.set("listings." + l.getId(), null);
                    addClaimedItem(l.getSeller(), l.getItem());
                    var online = Bukkit.getPlayer(l.getSeller());
                    if (online != null)
                        online.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                            "&8[&6AH&8] &7Ton annonce a expiré. Récupère ton item avec &e/ah claimed&7."));
                }
                save();
            });
        }, 20L * 60 * 5, 20L * 60 * 5); // toutes les 5 minutes
    }

    // ─── Persistance ──────────────────────────────────────────────────────────

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) try { dataFile.createNewFile(); } catch (Exception ignored) {}
        data = YamlConfiguration.loadConfiguration(dataFile);

        // Chargement des annonces
        if (data.contains("listings")) {
            for (String id : Objects.requireNonNull(data.getConfigurationSection("listings")).getKeys(false)) {
                try {
                    String p  = "listings." + id + ".";
                    UUID   seller = UUID.fromString(Objects.requireNonNull(data.getString(p + "seller")));
                    String sn     = data.getString(p + "sellerName", "?");
                    int    price  = data.getInt(p + "price");
                    long   at     = data.getLong(p + "listedAt");
                    ItemStack item = deserializeItem(data.getString(p + "item"));
                    if (item != null) listings.add(new AuctionListing(id, seller, sn, item, price, at));
                } catch (Exception e) { plugin.getLogger().warning("Erreur AH listing: " + e.getMessage()); }
            }
        }

        // Chargement des items réclamés
        if (data.contains("claimed")) {
            for (String uuidStr : Objects.requireNonNull(data.getConfigurationSection("claimed")).getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    for (String encoded : data.getStringList("claimed." + uuidStr)) {
                        ItemStack item = deserializeItem(encoded);
                        if (item != null) claimedItems.computeIfAbsent(uuid, k -> new ArrayList<>()).add(item);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void saveListing(AuctionListing l) {
        String p = "listings." + l.getId() + ".";
        data.set(p + "seller",     l.getSeller().toString());
        data.set(p + "sellerName", l.getSellerName());
        data.set(p + "price",      l.getPrice());
        data.set(p + "listedAt",   l.getListedAt());
        data.set(p + "item",       serializeItem(l.getItem()));
        save();
    }

    private void save() {
        try { data.save(dataFile); } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Sérialisation ────────────────────────────────────────────────────────

    public static String serializeItem(ItemStack item) {
        try {
            ByteArrayOutputStream os  = new ByteArrayOutputStream();
            BukkitObjectOutputStream bos = new BukkitObjectOutputStream(os);
            bos.writeObject(item); bos.close();
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) { return null; }
    }

    public static ItemStack deserializeItem(String encoded) {
        if (encoded == null) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            BukkitObjectInputStream bis = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
            ItemStack item = (ItemStack) bis.readObject(); bis.close();
            return item;
        } catch (Exception e) { return null; }
    }
}
