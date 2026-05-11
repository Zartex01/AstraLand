package com.astraland.skyblock.ah;

import com.astraland.skyblock.managers.AuctionManager;
import com.astraland.skyblock.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AHGui implements InventoryHolder {

    public enum Mode { ALL, MINE }

    private static final int[] SLOTS   = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
    private static final int   PER_PAGE = SLOTS.length;

    private final Inventory     inv;
    private final Player        viewer;
    private final AuctionManager am;
    private final EconomyManager eco;
    private final int            page;
    private final Mode           mode;
    private final Map<Integer, AuctionListing> slotMap = new HashMap<>();

    public AHGui(Player viewer, AuctionManager am, EconomyManager eco, int page, Mode mode) {
        this.viewer = viewer; this.am = am; this.eco = eco; this.mode = mode;

        List<AuctionListing> source = buildSource();
        int total = Math.max(1, (int) Math.ceil(source.size() / (double) PER_PAGE));
        this.page  = Math.min(Math.max(page, 0), total - 1);

        String title = mode == Mode.MINE
            ? c("&e&l📦 Mes annonces &8| &ePage " + (this.page + 1) + "/" + total)
            : c("&6&l🏪 Hôtel des Ventes &8| &ePage " + (this.page + 1) + "/" + total);
        this.inv = Bukkit.createInventory(this, 54, title);
        build(source, total);
    }

    // ─── Construction ─────────────────────────────────────────────────────────

    private List<AuctionListing> buildSource() {
        return mode == Mode.MINE
            ? am.getPlayerListings(viewer.getUniqueId())
            : new ArrayList<>(am.getListings());
    }

    private void build(List<AuctionListing> source, int total) {
        Material borderMat = mode == Mode.MINE ? Material.BLUE_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE;
        ItemStack bdr = bdr(borderMat);
        for (int i = 0; i < 9; i++)  inv.setItem(i, bdr);
        for (int i = 45; i < 54; i++) inv.setItem(i, bdr);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, bdr); inv.setItem(r * 9 + 8, bdr); }

        // Onglets
        inv.setItem(2, tabBtn(Mode.ALL));
        inv.setItem(6, tabBtn(Mode.MINE));

        // Contenu
        int start = page * PER_PAGE;
        for (int i = 0; i < SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= source.size()) break;
            AuctionListing l = source.get(idx);
            inv.setItem(SLOTS[i], display(l));
            slotMap.put(SLOTS[i], l);
        }

        // Navigation
        if (page > 0) inv.setItem(46, btn(Material.ARROW, "&ePage précédente", "&7Revenir en arrière"));
        inv.setItem(49, infoBtn(source.size()));
        if ((page + 1) * PER_PAGE < source.size()) inv.setItem(52, btn(Material.ARROW, "&ePage suivante", "&7Voir la suite"));
        inv.setItem(53, btn(Material.BARRIER, "&cFermer", "&7Ferme l'hôtel des ventes"));

        // Claimed
        List<ItemStack> claimed = am.getClaimedItems(viewer.getUniqueId());
        if (!claimed.isEmpty()) {
            inv.setItem(45, claimedBtn(claimed.size()));
        }
    }

    // ─── Clics ────────────────────────────────────────────────────────────────

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player clicker)) return;
        int slot = e.getSlot();

        // Onglets
        if (slot == 2) { new AHGui(clicker, am, eco, 0, Mode.ALL).open(clicker); return; }
        if (slot == 6) { new AHGui(clicker, am, eco, 0, Mode.MINE).open(clicker); return; }

        // Navigation
        if (slot == 46 && page > 0)                                { new AHGui(clicker, am, eco, page - 1, mode).open(clicker); return; }
        if (slot == 52 && (page + 1) * PER_PAGE < buildSource().size()) { new AHGui(clicker, am, eco, page + 1, mode).open(clicker); return; }
        if (slot == 53) { clicker.closeInventory(); return; }

        // Items réclamés
        if (slot == 45) {
            List<ItemStack> claimed = am.getClaimedItems(clicker.getUniqueId());
            if (!claimed.isEmpty()) {
                for (ItemStack item : claimed) {
                    var leftover = clicker.getInventory().addItem(item);
                    leftover.values().forEach(i -> clicker.getWorld().dropItemNaturally(clicker.getLocation(), i));
                }
                am.clearClaimedItems(clicker.getUniqueId());
                clicker.sendMessage(c("&a✔ " + claimed.size() + " item(s) récupéré(s) !"));
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                new AHGui(clicker, am, eco, page, mode).open(clicker);
            }
            return;
        }

        AuctionListing listing = slotMap.get(slot);
        if (listing == null) return;

        // Vérifier que l'annonce existe toujours
        if (am.getListings().stream().noneMatch(l -> l.getId().equals(listing.getId()))) {
            clicker.sendMessage(c("&cCette annonce n'existe plus."));
            new AHGui(clicker, am, eco, page, mode).open(clicker);
            return;
        }

        if (listing.getSeller().equals(clicker.getUniqueId())) {
            // Annuler sa propre annonce
            am.removeListing(listing.getId());
            var leftover = clicker.getInventory().addItem(listing.getItem().clone());
            leftover.values().forEach(i -> clicker.getWorld().dropItemNaturally(clicker.getLocation(), i));
            clicker.sendMessage(c("&a✔ Annonce annulée. Item récupéré."));
            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
            new AHGui(clicker, am, eco, page, mode).open(clicker);
        } else {
            // Acheter
            if (!eco.removeBalance(clicker.getUniqueId(), listing.getPrice())) {
                clicker.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + listing.getPrice() + " $&c.")); return;
            }
            eco.addBalance(listing.getSeller(), listing.getPrice());
            var leftover = clicker.getInventory().addItem(listing.getItem().clone());
            leftover.values().forEach(i -> clicker.getWorld().dropItemNaturally(clicker.getLocation(), i));
            am.removeListing(listing.getId());

            Player seller = Bukkit.getPlayer(listing.getSeller());
            if (seller != null)
                seller.sendMessage(c("&a💰 &e" + clicker.getName() + " &aa acheté &f"
                    + itemName(listing.getItem()) + " &apour &6" + listing.getPrice() + " $ !"));
            else {
                // Seller hors ligne → notifie à sa prochaine connexion via claimed
                // (on lui crédite déjà l'argent, pas besoin d'autre action)
            }
            clicker.sendMessage(c("&a✔ Achat effectué : &f" + itemName(listing.getItem())
                + " &apour &6" + listing.getPrice() + " $ !"));
            clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            new AHGui(clicker, am, eco, page, mode).open(clicker);
        }
    }

    // ─── Builders d'items ─────────────────────────────────────────────────────

    private ItemStack display(AuctionListing l) {
        ItemStack d = l.getItem().clone();
        ItemMeta  m = d.getItemMeta();
        List<String> lore = new ArrayList<>(m.hasLore() ? m.getLore() : List.of());
        lore.add("");
        lore.add(c("&7Vendeur : &e" + l.getSellerName()));
        lore.add(c("&6Prix : &a" + l.getPrice() + " $"));

        long remainMs = AuctionManager.EXPIRY_MILLIS - (System.currentTimeMillis() - l.getListedAt());
        long remainH  = Math.max(0, TimeUnit.MILLISECONDS.toHours(remainMs));
        long remainM  = Math.max(0, TimeUnit.MILLISECONDS.toMinutes(remainMs) % 60);
        lore.add(c("&7Expire dans : &c" + remainH + "h " + remainM + "m"));

        lore.add("");
        if (l.getSeller().equals(viewer.getUniqueId()))
            lore.add(c("&c▶ Clic pour &lANNULER"));
        else
            lore.add(c("&a▶ Clic pour &lACHETER"));

        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        d.setItemMeta(m);
        return d;
    }

    private ItemStack tabBtn(Mode m) {
        boolean active = this.mode == m;
        Material mat   = m == Mode.ALL ? Material.BOOK : Material.PLAYER_HEAD;
        String   name  = m == Mode.ALL ? "&6&l🏪 Toutes les annonces" : "&e&l📦 Mes annonces";
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(c(name));
        meta.setLore(List.of(
            active ? c("&a● Onglet actif") : c("&7▶ Cliquer pour ouvrir"),
            m == Mode.ALL
                ? c("&7" + am.getListings().size() + " annonces disponibles")
                : c("&7" + am.getPlayerListings(viewer.getUniqueId()).size() + "/" + AuctionManager.MAX_PER_PLAYER + " slots utilisés")
        ));
        if (active) meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack infoBtn(int listCount) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta  m    = item.getItemMeta();
        m.setDisplayName(c("&e&l📊 Hôtel des Ventes"));
        m.setLore(List.of(
            c("&7" + listCount + " annonce(s) " + (mode == Mode.MINE ? "de toi" : "au total")),
            c("&7Tes annonces : &e" + am.getPlayerListingCount(viewer.getUniqueId()) + "/" + AuctionManager.MAX_PER_PLAYER),
            c(""),
            c("&7Durée des annonces : &c48h"),
            c("&7Récupère les expirés avec &e/ah claimed")
        ));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack claimedBtn(int count) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta  m    = item.getItemMeta();
        m.setDisplayName(c("&a&l📥 Items à récupérer &8(" + count + ")"));
        m.setLore(List.of(c("&7Clique pour récupérer tes items"), c("&7(annonces expirées / annulées)")));
        item.setItemMeta(m);
        return item;
    }

    private ItemStack btn(Material mat, String name, String lore) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta();
        m.setDisplayName(c(name)); m.setLore(List.of(c(lore))); i.setItemMeta(m); return i;
    }

    private ItemStack bdr(Material mat) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i;
    }

    private String itemName(ItemStack item) {
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName())
            return item.getItemMeta().getDisplayName();
        String raw = item.getType().name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String w : raw.split(" "))
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }
}
