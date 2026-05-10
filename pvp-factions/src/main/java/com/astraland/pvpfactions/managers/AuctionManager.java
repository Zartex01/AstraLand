package com.astraland.pvpfactions.managers;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.ah.AuctionListing;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

public class AuctionManager {

    public static final int MAX_PER_PLAYER = 5;

    private final PvpFactions plugin;
    private final List<AuctionListing> listings = new ArrayList<>();
    private final File dataFile;
    private YamlConfiguration data;

    public AuctionManager(PvpFactions plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "ah.yml");
        load();
    }

    public void addListing(AuctionListing listing) {
        listings.add(listing);
        saveListing(listing);
    }

    public void removeListing(String id) {
        listings.removeIf(l -> l.getId().equals(id));
        data.set("listings." + id, null);
        save();
    }

    public List<AuctionListing> getListings() { return Collections.unmodifiableList(listings); }

    public long getPlayerListingCount(UUID seller) {
        return listings.stream().filter(l -> l.getSeller().equals(seller)).count();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) try { dataFile.createNewFile(); } catch (Exception ignored) {}
        data = YamlConfiguration.loadConfiguration(dataFile);
        if (!data.contains("listings")) return;
        for (String id : Objects.requireNonNull(data.getConfigurationSection("listings")).getKeys(false)) {
            try {
                String path = "listings." + id + ".";
                UUID seller = UUID.fromString(Objects.requireNonNull(data.getString(path + "seller")));
                String sellerName = data.getString(path + "sellerName", "?");
                int price = data.getInt(path + "price");
                long listedAt = data.getLong(path + "listedAt");
                boolean boosted = data.getBoolean(path + "boosted", false);
                ItemStack item = deserializeItem(data.getString(path + "item"));
                if (item != null) listings.add(new AuctionListing(id, seller, sellerName, item, price, listedAt, boosted));
            } catch (Exception e) { plugin.getLogger().warning("Erreur chargement AH: " + e.getMessage()); }
        }
    }

    private void saveListing(AuctionListing l) {
        String path = "listings." + l.getId() + ".";
        data.set(path + "seller", l.getSeller().toString());
        data.set(path + "sellerName", l.getSellerName());
        data.set(path + "price", l.getPrice());
        data.set(path + "listedAt", l.getListedAt());
        data.set(path + "boosted", l.isBoosted());
        data.set(path + "item", serializeItem(l.getItem()));
        save();
    }

    private void save() { try { data.save(dataFile); } catch (Exception e) { e.printStackTrace(); } }

    public static String serializeItem(ItemStack item) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
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
            ItemStack item = (ItemStack) bis.readObject(); bis.close(); return item;
        } catch (Exception e) { return null; }
    }
}
