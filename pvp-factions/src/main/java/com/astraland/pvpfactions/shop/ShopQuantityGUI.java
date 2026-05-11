package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.managers.EconomyManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopQuantityGUI implements InventoryHolder {

    public enum Mode { BUY, SELL }

    // ─── Slots ────────────────────────────────────────────────────────────────
    private static final int SLOT_BUY_TAB  = 1;
    private static final int SLOT_ITEM     = 4;
    private static final int SLOT_SELL_TAB = 7;
    private static final int SLOT_MINUS64  = 19;
    private static final int SLOT_MINUS10  = 20;
    private static final int SLOT_MINUS1   = 21;
    private static final int SLOT_QTY      = 22;
    private static final int SLOT_PLUS1    = 23;
    private static final int SLOT_PLUS10   = 24;
    private static final int SLOT_PLUS64   = 25;
    private static final int SLOT_INFO     = 31;
    private static final int SLOT_BACK     = 45;
    private static final int SLOT_CONFIRM  = 49;

    private final Inventory    inv;
    private final ShopItemData data;
    private final EconomyManager eco;
    private final Runnable     backAction;
    private Mode   mode;
    private int    quantity;

    public ShopQuantityGUI(ShopItemData data, EconomyManager eco, Mode defaultMode,
                           Player player, Runnable backAction) {
        this.data       = data;
        this.eco        = eco;
        this.mode       = defaultMode;
        this.backAction = backAction;
        this.quantity   = 1;
        this.inv = Bukkit.createInventory(this, 54, c("&8» &6&lShop &8- &f") + c(data.name()));
        build(player);
    }

    // ─── Construction du GUI ──────────────────────────────────────────────────

    public void build(Player player) {
        inv.clear();

        ItemStack black = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, black);

        // Onglet ACHETER (slot 1)
        boolean canBuy   = data.buyPrice() > 0;
        boolean activeBuy = mode == Mode.BUY;
        ItemStack buyTab = new ItemStack(activeBuy ? Material.LIME_STAINED_GLASS_PANE
                                                   : (canBuy ? Material.GRAY_STAINED_GLASS_PANE
                                                             : Material.RED_STAINED_GLASS_PANE));
        ItemMeta btm = buyTab.getItemMeta();
        if (btm != null) {
            btm.setDisplayName(activeBuy ? c("&a&l▶ ACHETER ◀") : c("&7ACHETER"));
            List<String> btl = new ArrayList<>();
            if (!canBuy) {
                btl.add(c("&cCet item n'est pas en vente."));
            } else {
                btl.add(c("&7Prix : &e" + data.buyPrice() + " $"));
                btl.add(c("&7Items reçus : &f" + data.reward().getAmount() + "x"));
                if (!activeBuy) btl.add(c("&a▶ Cliquer pour passer en mode Achat"));
            }
            btm.setLore(btl);
            buyTab.setItemMeta(btm);
        }
        inv.setItem(SLOT_BUY_TAB, buyTab);

        // Aperçu de l'item (slot 4)
        ItemStack preview = new ItemStack(data.icon() != null ? data.icon() : Material.PAPER);
        ItemMeta pm = preview.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(c(data.name()));
            List<String> lore = new ArrayList<>();
            for (String l : data.lore()) lore.add(c(l));
            lore.add("");
            if (mode == Mode.BUY && canBuy) {
                lore.add(c("&7Prix à l'achat : &e" + data.buyPrice() + " $"));
                lore.add(c("&7Items reçus : &f" + data.reward().getAmount() + "x par unité"));
            } else if (mode == Mode.SELL && data.isSellable()) {
                int ppi = pricePerItem();
                lore.add(c("&7Prix de vente : &6" + ppi + " $ &7/ item"));
            }
            pm.setLore(lore);
            pm.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
            preview.setItemMeta(pm);
        }
        inv.setItem(SLOT_ITEM, preview);

        // Onglet VENDRE (slot 7)
        boolean canSell   = data.isSellable();
        boolean activeSell = mode == Mode.SELL;
        ItemStack sellTab = new ItemStack(activeSell ? Material.ORANGE_STAINED_GLASS_PANE
                                                     : (canSell ? Material.GRAY_STAINED_GLASS_PANE
                                                                : Material.RED_STAINED_GLASS_PANE));
        ItemMeta stm = sellTab.getItemMeta();
        if (stm != null) {
            stm.setDisplayName(activeSell ? c("&6&l▶ VENDRE ◀") : c("&7VENDRE"));
            List<String> stl = new ArrayList<>();
            if (!canSell) {
                stl.add(c("&cCet item n'est pas vendable."));
            } else {
                stl.add(c("&7Prix : &6" + pricePerItem() + " $ &7/ item"));
                stl.add(c("&7En inventaire : &f" + countMat(player, data.reward().getType()) + "x"));
                if (!activeSell) stl.add(c("&6▶ Cliquer pour passer en mode Vente"));
            }
            stm.setLore(stl);
            sellTab.setItemMeta(stm);
        }
        inv.setItem(SLOT_SELL_TAB, sellTab);

        // Séparateurs (lignes 2 et 4)
        ItemStack sep = glass(mode == Mode.BUY ? Material.GREEN_STAINED_GLASS_PANE
                                               : Material.ORANGE_STAINED_GLASS_PANE);
        for (int i = 9; i < 18; i++)  inv.setItem(i, sep);
        for (int i = 27; i < 36; i++) inv.setItem(i, sep);
        for (int i = 36; i < 45; i++) inv.setItem(i, black);

        // Boutons de quantité (ligne 3)
        inv.setItem(SLOT_MINUS64, qtyBtn(Material.RED_STAINED_GLASS_PANE,  c("&c&l-64"), -64));
        inv.setItem(SLOT_MINUS10, qtyBtn(Material.RED_STAINED_GLASS_PANE,  c("&c&l-10"), -10));
        inv.setItem(SLOT_MINUS1,  qtyBtn(Material.RED_STAINED_GLASS_PANE,  c("&c&l-1"),   -1));
        inv.setItem(SLOT_PLUS1,   qtyBtn(Material.LIME_STAINED_GLASS_PANE, c("&a&l+1"),   +1));
        inv.setItem(SLOT_PLUS10,  qtyBtn(Material.LIME_STAINED_GLASS_PANE, c("&a&l+10"), +10));
        inv.setItem(SLOT_PLUS64,  qtyBtn(Material.LIME_STAINED_GLASS_PANE, c("&a&l+64"), +64));

        // Affichage quantité (centre ligne 3)
        ItemStack qtyItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta qm = qtyItem.getItemMeta();
        if (qm != null) {
            qm.setDisplayName(c("&e&lQuantité : &6" + quantity));
            qm.setLore(List.of(
                c("&7Ajuste avec les boutons &c-&7/&a+ &7ci-dessus."),
                c("&7Cliquer ici &f→ &7régler au maximum disponible")
            ));
            qtyItem.setItemMeta(qm);
        }
        inv.setItem(SLOT_QTY, qtyItem);

        // Récapitulatif (ligne 4 centre)
        int balance = eco.getBalance(player.getUniqueId());
        int total;
        int maxQty;
        if (mode == Mode.BUY) {
            total  = quantity * data.buyPrice();
            maxQty = data.buyPrice() > 0 ? balance / data.buyPrice() : 0;
        } else {
            total  = quantity * pricePerItem();
            maxQty = countMat(player, data.reward().getType());
        }

        ItemStack info = new ItemStack(Material.SUNFLOWER);
        ItemMeta im = info.getItemMeta();
        if (im != null) {
            im.setDisplayName(mode == Mode.BUY ? c("&a&lRécapitulatif — Achat") : c("&6&lRécapitulatif — Vente"));
            List<String> il = new ArrayList<>();
            if (mode == Mode.BUY) {
                il.add(c("&7Quantité : &f" + quantity + " unité(s) → &f+"
                    + (quantity * data.reward().getAmount()) + " item(s)"));
                il.add(c("&7Coût total : &c-" + total + " $"));
                il.add(c("&7Ton solde : &e" + balance + " $"));
                il.add(c("&7Après achat : &e" + Math.max(0, balance - total) + " $"));
                if (total > balance) il.add(c("&c⚠ Fonds insuffisants !"));
            } else {
                il.add(c("&7Quantité : &f" + quantity + " item(s)"));
                il.add(c("&7Gain total : &6+" + total + " $"));
                il.add(c("&7Ton solde : &e" + balance + " $"));
                il.add(c("&7En inventaire : &f" + countMat(player, data.reward().getType()) + "x"));
                if (quantity > maxQty) il.add(c("&c⚠ Pas assez dans ton inventaire !"));
            }
            im.setLore(il);
            info.setItemMeta(im);
        }
        inv.setItem(SLOT_INFO, info);

        // Bouton Retour
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bkm = back.getItemMeta();
        if (bkm != null) {
            bkm.setDisplayName(c("&c&l← Retour"));
            bkm.setLore(List.of(c("&7Retourner à la catégorie")));
            back.setItemMeta(bkm);
        }
        inv.setItem(SLOT_BACK, back);

        // Bouton Confirmer
        boolean ok = mode == Mode.BUY
            ? (canBuy && total <= balance && quantity > 0)
            : (canSell && quantity > 0 && quantity <= maxQty);

        ItemStack confirm = new ItemStack(ok ? Material.LIME_STAINED_GLASS_PANE
                                            : Material.RED_STAINED_GLASS_PANE);
        ItemMeta cm = confirm.getItemMeta();
        if (cm != null) {
            if (ok) {
                cm.setDisplayName(mode == Mode.BUY
                    ? c("&a&l✔ Acheter " + quantity + " unité(s)")
                    : c("&a&l✔ Vendre " + quantity + " item(s)"));
                cm.setLore(List.of(
                    mode == Mode.BUY
                        ? c("&7Coût : &c" + total + " $ &7→ &f+" + (quantity * data.reward().getAmount()) + " item(s)")
                        : c("&7Gain : &6+" + total + " $")
                ));
            } else {
                cm.setDisplayName(c("&c&l✗ Impossible"));
                List<String> cl = new ArrayList<>();
                if (mode == Mode.BUY && !canBuy)  cl.add(c("&cItem non disponible à l'achat."));
                else if (mode == Mode.BUY && total > balance) cl.add(c("&cFonds insuffisants."));
                if (mode == Mode.SELL && !canSell) cl.add(c("&cItem non vendable."));
                else if (mode == Mode.SELL && quantity > maxQty) cl.add(c("&cPas assez dans l'inventaire."));
                cm.setLore(cl);
            }
            confirm.setItemMeta(cm);
        }
        inv.setItem(SLOT_CONFIRM, confirm);
    }

    // ─── Gestion des clics ────────────────────────────────────────────────────

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) return;

        switch (slot) {
            case SLOT_BACK    -> backAction.run();
            case SLOT_BUY_TAB -> { if (data.buyPrice() > 0) { mode = Mode.BUY;  quantity = 1; build(player); } }
            case SLOT_SELL_TAB-> { if (data.isSellable())   { mode = Mode.SELL; quantity = 1; build(player); } }
            case SLOT_MINUS64 -> adjust(-64, player);
            case SLOT_MINUS10 -> adjust(-10, player);
            case SLOT_MINUS1  -> adjust(-1,  player);
            case SLOT_PLUS1   -> adjust(+1,  player);
            case SLOT_PLUS10  -> adjust(+10, player);
            case SLOT_PLUS64  -> adjust(+64, player);
            case SLOT_QTY     -> setMax(player);
            case SLOT_CONFIRM -> confirm(player);
        }
    }

    // ─── Logique interne ──────────────────────────────────────────────────────

    private void adjust(int delta, Player player) {
        quantity = Math.max(1, quantity + delta);
        build(player);
    }

    private void setMax(Player player) {
        int balance = eco.getBalance(player.getUniqueId());
        if (mode == Mode.BUY) {
            quantity = Math.max(1, data.buyPrice() > 0 ? balance / data.buyPrice() : 1);
        } else {
            quantity = Math.max(1, countMat(player, data.reward().getType()));
        }
        build(player);
    }

    private void confirm(Player player) {
        int balance = eco.getBalance(player.getUniqueId());

        if (mode == Mode.BUY) {
            if (data.buyPrice() <= 0) { player.sendMessage(c("&c✗ Item non disponible à l'achat.")); return; }
            int total = quantity * data.buyPrice();
            if (total > balance) {
                player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + total + " $ &c| Solde : &e" + balance + " $"));
                return;
            }
            if (!eco.removeBalance(player.getUniqueId(), total)) { player.sendMessage(c("&c✗ Fonds insuffisants !")); return; }
            for (int i = 0; i < quantity; i++) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(data.reward().clone());
                leftover.values().forEach(it -> player.getWorld().dropItemNaturally(player.getLocation(), it));
            }
            int received = quantity * data.reward().getAmount();
            player.sendMessage(c("&a✔ Acheté : &f" + received + "x " + c(data.name())
                + " &apour &e" + total + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);

        } else {
            if (!data.isSellable()) { player.sendMessage(c("&c✗ Item non vendable.")); return; }
            Material mat = data.reward().getType();
            int inInv = countMat(player, mat);
            if (quantity > inInv) {
                player.sendMessage(c("&c✗ Tu n'as que &f" + inInv + "x &cdans ton inventaire."));
                return;
            }
            int gained = quantity * pricePerItem();
            removeFromInventory(player, mat, quantity);
            eco.addBalance(player.getUniqueId(), gained);
            player.sendMessage(c("&e💰 Vendu : &f" + quantity + "x " + c(data.name())
                + " &epour &6+" + gained + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 0.8f);
        }
        player.closeInventory();
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    private int pricePerItem() {
        int amount = data.reward().getAmount();
        return (int) Math.floor((double) data.sellPrice() / Math.max(1, amount));
    }

    private int countMat(Player player, Material mat) {
        int count = 0;
        for (ItemStack it : player.getInventory().getContents())
            if (it != null && it.getType() == mat) count += it.getAmount();
        return count;
    }

    private void removeFromInventory(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack it = contents[i];
            if (it == null || it.getType() != mat) continue;
            if (it.getAmount() <= remaining) { remaining -= it.getAmount(); player.getInventory().setItem(i, null); }
            else { it.setAmount(it.getAmount() - remaining); remaining = 0; }
        }
    }

    private ItemStack qtyBtn(Material mat, String name, int delta) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            m.setLore(List.of(c("&7Ajuster de &f" + (delta > 0 ? "+" : "") + delta)));
            item.setItemMeta(m);
        }
        return item;
    }

    private ItemStack glass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) { m.setDisplayName(" "); item.setItemMeta(m); }
        return item;
    }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
