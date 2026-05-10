package com.astraland.uhc.ah;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class AuctionListing {
    private final String id;
    private final UUID seller;
    private final String sellerName;
    private final ItemStack item;
    private final int price;
    private final long listedAt;

    public AuctionListing(String id, UUID seller, String sellerName, ItemStack item, int price, long listedAt) {
        this.id = id; this.seller = seller; this.sellerName = sellerName;
        this.item = item; this.price = price; this.listedAt = listedAt;
    }

    public String getId()        { return id; }
    public UUID getSeller()      { return seller; }
    public String getSellerName(){ return sellerName; }
    public ItemStack getItem()   { return item; }
    public int getPrice()        { return price; }
    public long getListedAt()    { return listedAt; }
}
