package com.astraland.bedwars.managers;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.ah.AuctionListing;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

public class AuctionManager {

    public static final int MAX_PER_PLAYER = 5;

    private final Bedwars plugin;
    private final List<AuctionListing> listings = new ArrayList<>();
    private final File dataFile;
    private YamlConfiguration data;

    public AuctionManager(Bedwars plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "ah.yml");
        load();
    }

    public void addListing(AuctionListing listing) { listings.add(listing); saveListing(listing); }

    public void removeListing(String id) {
        listings.removeIf(l -> l.getId().equals(id));
        data.set("listings." + id, null); save();
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
                String p = "listings." + id + ".";
                UUID seller = UUID.fromString(Objects.requireNonNull(data.getString(p + "seller")));
                String sn = data.getString(p + "sellerName", "?");
                int price = data.getInt(p + "price"); long at = data.getLong(p + "listedAt");
                ItemStack item = deserializeItem(data.getString(p + "item"));
                if (item != null) listings.add(new AuctionListing(id, seller, sn, item, price, at));
            } catch (Exception e) { plugin.getLogger().warning("Erreur AH: " + e.getMessage()); }
        }
    }

    private void saveListing(AuctionListing l) {
        String p = "listings." + l.getId() + ".";
        data.set(p + "seller", l.getSeller().toString()); data.set(p + "sellerName", l.getSellerName());
        data.set(p + "price", l.getPrice()); data.set(p + "listedAt", l.getListedAt());
        data.set(p + "item", serializeItem(l.getItem())); save();
    }

    private void save() { try { data.save(dataFile); } catch (Exception e) { e.printStackTrace(); } }

    public static String serializeItem(ItemStack item) {
        try { ByteArrayOutputStream os = new ByteArrayOutputStream(); BukkitObjectOutputStream bos = new BukkitObjectOutputStream(os); bos.writeObject(item); bos.close(); return Base64.getEncoder().encodeToString(os.toByteArray()); } catch (Exception e) { return null; }
    }
    public static ItemStack deserializeItem(String encoded) {
        if (encoded == null) return null;
        try { byte[] bytes = Base64.getDecoder().decode(encoded); BukkitObjectInputStream bis = new BukkitObjectInputStream(new ByteArrayInputStream(bytes)); ItemStack item = (ItemStack) bis.readObject(); bis.close(); return item; } catch (Exception e) { return null; }
    }
}
