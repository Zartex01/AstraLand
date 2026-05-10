package com.astraland.pvpfactions.ah;

import com.astraland.pvpfactions.managers.AuctionManager;
import com.astraland.pvpfactions.managers.EconomyManager;
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
import java.util.concurrent.ConcurrentHashMap;

public class AHSellGUI implements InventoryHolder {

    public static class SellSession {
        public final ItemStack item;
        public int price;
        public boolean boosted;
        public boolean confirmed;
        public SellSession(ItemStack item) {
            this.item = item; this.price = -1; this.boosted = false; this.confirmed = false;
        }
    }

    public static final Map<UUID, SellSession> SESSIONS       = new ConcurrentHashMap<>();
    public static final Map<UUID, SellSession> AWAITING_PRICE = new ConcurrentHashMap<>();

    private static final int SLOT_ITEM    = 13;
    private static final int SLOT_PRIX    = 29;
    private static final int SLOT_PUB     = 31;
    private static final int SLOT_ANNULER = 46;
    private static final int SLOT_CONFIRM = 52;

    private final Inventory inv;
    private final Player seller;
    private final AuctionManager am;
    private final EconomyManager eco;
    private final SellSession session;

    public AHSellGUI(Player seller, SellSession session, AuctionManager am, EconomyManager eco) {
        this.seller = seller;
        this.session = session;
        this.am = am;
        this.eco = eco;
        this.inv = Bukkit.createInventory(this, 54, c("&6&l✦ Mettre en Vente &8| &eHôtel des Ventes"));
        build();
    }

    private void build() {
        ItemStack black  = border(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack orange = border(Material.ORANGE_STAINED_GLASS_PANE);
        for (int i = 0;  i < 54; i++) inv.setItem(i, black);
        for (int i = 0;  i <  9; i++) inv.setItem(i, orange);
        for (int i = 45; i < 54; i++) inv.setItem(i, orange);

        // Item preview
        ItemStack preview = session.item.clone();
        ItemMeta pm = preview.getItemMeta();
        List<String> lore = new ArrayList<>(pm.hasLore() ? pm.getLore() : List.of());
        lore.add("");
        lore.add(c("&7Prix : " + (session.price > 0 ? "&6" + session.price + " $" : "&cNon défini")));
        lore.add(c("&7Pub : " + (session.boosted ? "&a✔ Activée &7(frais 5%)" : "&c✗ Désactivée")));
        pm.setLore(lore);
        pm.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        preview.setItemMeta(pm);
        inv.setItem(SLOT_ITEM, preview);

        // Prix button
        ItemStack prix = new ItemStack(session.price > 0 ? Material.EMERALD : Material.REDSTONE);
        ItemMeta mp = prix.getItemMeta();
        mp.setDisplayName(c("&e&l✎ Définir le Prix"));
        mp.setLore(List.of(
            c(session.price > 0 ? "&7Prix actuel : &6" + session.price + " $" : "&7Prix : &cNon défini"),
            c(""),
            c("&7▶ Clic pour écrire le prix dans le chat")
        ));
        prix.setItemMeta(mp);
        inv.setItem(SLOT_PRIX, prix);

        // Pub button
        ItemStack pub = new ItemStack(session.boosted ? Material.GOLD_INGOT : Material.IRON_INGOT);
        ItemMeta mb = pub.getItemMeta();
        mb.setDisplayName(c(session.boosted ? "&6&l★ PUB &a[ACTIVÉE]" : "&7&l★ PUB &c[DÉSACTIVÉE]"));
        mb.setLore(List.of(
            c("&7La pub met ton item en &6avant"),
            c("&7dans l'hôtel des ventes."),
            c(""),
            c("&7Frais : &c5% &7prélevés sur la vente"),
            c(""),
            c("&7▶ Clic pour " + (session.boosted ? "&cdésactiver" : "&aactiver"))
        ));
        pub.setItemMeta(mb);
        inv.setItem(SLOT_PUB, pub);

        // Annuler button
        ItemStack annuler = new ItemStack(Material.BARRIER);
        ItemMeta ma = annuler.getItemMeta();
        ma.setDisplayName(c("&c&l✗ Annuler"));
        ma.setLore(List.of(c("&7Ton item te sera rendu.")));
        annuler.setItemMeta(ma);
        inv.setItem(SLOT_ANNULER, annuler);

        // Confirmer button
        boolean canConfirm = session.price > 0;
        ItemStack confirm = new ItemStack(canConfirm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta mc = confirm.getItemMeta();
        mc.setDisplayName(c(canConfirm ? "&a&l✔ Confirmer la mise en vente" : "&c&l✗ Définir un prix d'abord"));
        mc.setLore(canConfirm ? List.of(
            c("&7Item : &f" + friendly(session.item)),
            c("&7Prix : &6" + session.price + " $"),
            c("&7Pub : " + (session.boosted ? "&a✔ Oui &7(tu recevras &695% &7)" : "&c✗ Non")),
            c(""),
            c("&a▶ Clic pour confirmer")
        ) : List.of(c("&cClique sur '✎ Définir le Prix' d'abord.")));
        confirm.setItemMeta(mc);
        inv.setItem(SLOT_CONFIRM, confirm);
    }

    public void handleClick(InventoryClickEvent e, AuctionManager am, EconomyManager eco, Player clicker) {
        e.setCancelled(true);
        if (e.getRawSlot() >= inv.getSize()) return;
        int slot = e.getRawSlot();

        switch (slot) {
            case SLOT_PRIX -> {
                AWAITING_PRICE.put(clicker.getUniqueId(), session);
                clicker.closeInventory();
                clicker.sendMessage(c("&6&l[AH] &7Écris le &eprix &7dans le chat."));
                clicker.sendMessage(c("&6&l[AH] &7Tape &ctap &7pour annuler."));
            }
            case SLOT_PUB -> {
                session.boosted = !session.boosted;
                build();
                clicker.sendMessage(c("&6[AH] &7Pub " + (session.boosted ? "&aactivée &7(frais 5%)" : "&cdésactivée") + "&7."));
            }
            case SLOT_ANNULER -> {
                cancelSession(clicker);
                clicker.closeInventory();
                clicker.sendMessage(c("&c[AH] &7Mise en vente annulée. Item rendu."));
            }
            case SLOT_CONFIRM -> {
                if (session.price <= 0) {
                    clicker.sendMessage(c("&c[AH] &7Tu dois d'abord définir un prix !"));
                    return;
                }
                if (am.getPlayerListingCount(clicker.getUniqueId()) >= AuctionManager.MAX_PER_PLAYER) {
                    clicker.sendMessage(c("&c[AH] &7Limite de &e" + AuctionManager.MAX_PER_PLAYER + " annonces &7atteinte."));
                    return;
                }
                session.confirmed = true;
                SESSIONS.remove(clicker.getUniqueId());
                AWAITING_PRICE.remove(clicker.getUniqueId());
                am.addListing(new AuctionListing(
                    UUID.randomUUID().toString(), clicker.getUniqueId(), clicker.getName(),
                    session.item.clone(), session.price, System.currentTimeMillis(), session.boosted
                ));
                clicker.closeInventory();
                clicker.sendMessage(c("&a&l[AH] &aItem mis en vente pour &6" + session.price + " $"
                    + (session.boosted ? " &7(avec pub ★)" : "") + " &a!"));
            }
        }
    }

    public static void cancelSession(Player p) {
        SESSIONS.remove(p.getUniqueId());
        AWAITING_PRICE.remove(p.getUniqueId());
    }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }

    private ItemStack border(Material mat) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.setDisplayName(" ");
        i.setItemMeta(m);
        return i;
    }

    private String friendly(ItemStack item) {
        ItemMeta m = item.getItemMeta();
        return (m != null && m.hasDisplayName()) ? m.getDisplayName()
            : item.getType().name().replace('_', ' ').toLowerCase();
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
