package com.astraland.bedwars.ah;

import com.astraland.bedwars.managers.AuctionManager;
import com.astraland.bedwars.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AHGui implements InventoryHolder {

    private static final int[] SLOTS = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private static final int PER_PAGE = 28;

    private final Inventory inv;
    private final Player viewer;
    private final AuctionManager am;
    private final EconomyManager eco;
    private final int page;
    private final Map<Integer, AuctionListing> slotMap = new HashMap<>();

    public AHGui(Player viewer, AuctionManager am, EconomyManager eco, int page) {
        this.viewer = viewer; this.am = am; this.eco = eco;
        List<AuctionListing> all = new ArrayList<>(am.getListings());
        int total = Math.max(1, (int) Math.ceil(all.size() / (double) PER_PAGE));
        this.page = Math.min(Math.max(page, 0), total - 1);
        this.inv = Bukkit.createInventory(this, 54, c("&6&l🏪 Hôtel des Ventes &8| &ePage " + (this.page + 1) + "/" + total));
        build(all, total);
    }

    private void build(List<AuctionListing> all, int total) {
        ItemStack bdr = bdr(Material.ORANGE_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, bdr);
        for (int i = 45; i < 54; i++) inv.setItem(i, bdr);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, bdr); inv.setItem(r * 9 + 8, bdr); }
        int start = page * PER_PAGE;
        for (int i = 0; i < SLOTS.length; i++) {
            int idx = start + i; if (idx >= all.size()) break;
            AuctionListing l = all.get(idx); inv.setItem(SLOTS[i], display(l)); slotMap.put(SLOTS[i], l);
        }
        if (page > 0) inv.setItem(46, btn(Material.ARROW, "&ePage précédente", "&7Revenir en arrière"));
        inv.setItem(49, btn(Material.BOOK, "&e" + all.size() + " annonce(s)", "&7Tes annonces : " + am.getPlayerListingCount(viewer.getUniqueId()) + "/" + AuctionManager.MAX_PER_PLAYER));
        if ((page + 1) * PER_PAGE < all.size()) inv.setItem(52, btn(Material.ARROW, "&ePage suivante", "&7Voir la suite"));
        inv.setItem(53, btn(Material.BARRIER, "&cFermer", "&7Ferme l'hôtel des ventes"));
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player clicker)) return;
        int slot = e.getSlot();
        if (slot == 46 && page > 0) { new AHGui(clicker, am, eco, page - 1).open(clicker); return; }
        if (slot == 52) { new AHGui(clicker, am, eco, page + 1).open(clicker); return; }
        if (slot == 53) { clicker.closeInventory(); return; }
        AuctionListing listing = slotMap.get(slot); if (listing == null) return;
        if (am.getListings().stream().noneMatch(l -> l.getId().equals(listing.getId()))) {
            clicker.sendMessage(c("&cCette annonce n'existe plus.")); new AHGui(clicker, am, eco, page).open(clicker); return;
        }
        if (listing.getSeller().equals(clicker.getUniqueId())) {
            am.removeListing(listing.getId()); clicker.getInventory().addItem(listing.getItem().clone());
            clicker.sendMessage(c("&a✔ Annonce annulée. Item récupéré.")); new AHGui(clicker, am, eco, page).open(clicker);
        } else {
            if (!eco.removeBalance(clicker.getUniqueId(), listing.getPrice())) { clicker.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + listing.getPrice() + " $&c.")); return; }
            eco.addBalance(listing.getSeller(), listing.getPrice()); clicker.getInventory().addItem(listing.getItem().clone());
            am.removeListing(listing.getId());
            Player seller = Bukkit.getPlayer(listing.getSeller());
            if (seller != null) seller.sendMessage(c("&a💰 &e" + clicker.getName() + " &aa acheté ton item pour &6" + listing.getPrice() + " $ !"));
            clicker.sendMessage(c("&a✔ Achat effectué pour &6" + listing.getPrice() + " $ !")); new AHGui(clicker, am, eco, page).open(clicker);
        }
    }

    private ItemStack display(AuctionListing l) {
        ItemStack d = l.getItem().clone(); ItemMeta m = d.getItemMeta();
        List<String> lore = new ArrayList<>(m.hasLore() ? m.getLore() : List.of());
        lore.add(""); lore.add(c("&7Vendeur : &e" + l.getSellerName())); lore.add(c("&6Prix : &a" + l.getPrice() + " $"));
        lore.add(l.getSeller().equals(viewer.getUniqueId()) ? c("&c▶ Clic pour &lANNULER") : c("&a▶ Clic pour &lACHETER"));
        m.setLore(lore); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); d.setItemMeta(m); return d;
    }

    private ItemStack btn(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); m.setLore(List.of(c(lore))); i.setItemMeta(m); return i;
    }
    private ItemStack bdr(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }
}
