package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopQuantityGUI implements InventoryHolder {

    public enum Mode { BUY, SELL }

    private static final int SLOT_ITEM    = 4;
    private static final int SLOT_MINUS64 = 19;
    private static final int SLOT_MINUS10 = 20;
    private static final int SLOT_MINUS1  = 21;
    private static final int SLOT_QTY     = 22;
    private static final int SLOT_PLUS1   = 23;
    private static final int SLOT_PLUS10  = 24;
    private static final int SLOT_PLUS64  = 25;
    private static final int SLOT_INFO    = 31;
    private static final int SLOT_BACK    = 45;
    private static final int SLOT_CONFIRM = 49;

    private final Inventory inv;
    private final ShopItemData data;
    private final EconomyManager eco;
    private final Mode mode;
    private final Runnable backAction;
    private int quantity;

    public ShopQuantityGUI(ShopItemData data, EconomyManager eco, Mode mode, Player player, Runnable backAction) {
        this.data       = data;
        this.eco        = eco;
        this.mode       = mode;
        this.backAction = backAction;
        this.quantity   = 1;

        String title = mode == Mode.BUY
            ? c("&a&lAcheter &8- &f") + c(data.name())
            : c("&6&lVendre &8- &f") + c(data.name());

        this.inv = Bukkit.createInventory(this, 54, title);
        build(player);
    }

    public void build(Player player) {
        inv.clear();

        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack accent = glass(mode == Mode.BUY ? Material.GREEN_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE);

        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);

        // Item preview
        ItemStack preview = data.icon() != null ? new ItemStack(data.icon()) : new ItemStack(Material.PAPER);
        ItemMeta pm = preview.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(c(data.name()));
            List<String> lore = new ArrayList<>();
            for (String l : data.lore()) lore.add(c(l));
            lore.add("");
            if (mode == Mode.BUY) {
                lore.add(c("&7Prix unitaire : &e" + data.buyPrice() + " $"));
                lore.add(c("&7Items par unité : &f" + data.reward().getAmount()));
            } else {
                int pricePerItem = (int) Math.floor((double) data.sellPrice() / data.reward().getAmount());
                lore.add(c("&7Prix à l'item : &6" + pricePerItem + " $"));
            }
            pm.setLore(lore);
            pm.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
            preview.setItemMeta(pm);
        }
        inv.setItem(SLOT_ITEM, preview);

        // Quantity controls
        inv.setItem(SLOT_MINUS64, makeQtyBtn(Material.RED_STAINED_GLASS_PANE,  c("&c&l-64"),  -64));
        inv.setItem(SLOT_MINUS10, makeQtyBtn(Material.RED_STAINED_GLASS_PANE,  c("&c&l-10"),  -10));
        inv.setItem(SLOT_MINUS1,  makeQtyBtn(Material.RED_STAINED_GLASS_PANE,  c("&c&l-1"),    -1));
        inv.setItem(SLOT_PLUS1,   makeQtyBtn(Material.LIME_STAINED_GLASS_PANE, c("&a&l+1"),    +1));
        inv.setItem(SLOT_PLUS10,  makeQtyBtn(Material.LIME_STAINED_GLASS_PANE, c("&a&l+10"),  +10));
        inv.setItem(SLOT_PLUS64,  makeQtyBtn(Material.LIME_STAINED_GLASS_PANE, c("&a&l+64"),  +64));

        // Quantity display
        ItemStack qtyItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta qm = qtyItem.getItemMeta();
        if (qm != null) {
            qm.setDisplayName(c("&e&lQuantité : &6" + quantity));
            qm.setLore(List.of(
                c("&7Utilise les boutons &a+&7/&c- &7pour ajuster."),
                c("&7Clic droit ici → max disponible")
            ));
            qtyItem.setItemMeta(qm);
        }
        inv.setItem(SLOT_QTY, qtyItem);

        // Info / total
        int total;
        int maxQty;
        int balance = eco.getBalance(player.getUniqueId());
        if (mode == Mode.BUY) {
            total  = quantity * data.buyPrice();
            maxQty = balance / Math.max(1, data.buyPrice());
        } else {
            int pricePerItem = (int) Math.floor((double) data.sellPrice() / data.reward().getAmount());
            total  = quantity * pricePerItem;
            maxQty = countMat(player, data.reward().getType());
        }

        ItemStack info = new ItemStack(Material.SUNFLOWER);
        ItemMeta im = info.getItemMeta();
        if (im != null) {
            im.setDisplayName(mode == Mode.BUY
                ? c("&a&lRécapitulatif - Achat")
                : c("&6&lRécapitulatif - Vente"));
            List<String> infoLore = new ArrayList<>();
            infoLore.add(c("&7Quantité : &f" + quantity
                + (mode == Mode.BUY
                    ? " &7(+" + (quantity * data.reward().getAmount()) + " items)"
                    : " &7items")));
            infoLore.add(mode == Mode.BUY
                ? c("&7Coût total : &c" + total + " $")
                : c("&7Gain total : &6+" + total + " $"));
            infoLore.add(c("&7Ton solde : &e" + balance + " $"));
            if (mode == Mode.BUY) {
                infoLore.add(c("&7Après : &e" + Math.max(0, balance - total) + " $"));
                if (total > balance) infoLore.add(c("&c⚠ Fonds insuffisants !"));
            } else {
                infoLore.add(c("&7Dans ton inv : &f" + countMat(player, data.reward().getType())
                    + "x &7" + data.reward().getType().name().toLowerCase().replace("_", " ")));
                if (quantity > maxQty) infoLore.add(c("&c⚠ Pas assez dans ton inventaire !"));
            }
            im.setLore(infoLore);
            info.setItemMeta(im);
        }
        inv.setItem(SLOT_INFO, info);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(c("&c&l← Retour"));
            bm.setLore(List.of(c("&7Retourner à la catégorie")));
            back.setItemMeta(bm);
        }
        inv.setItem(SLOT_BACK, back);

        // Confirm button
        boolean canConfirm = mode == Mode.BUY
            ? total <= balance && quantity > 0
            : quantity > 0 && quantity <= maxQty;

        ItemStack confirm = new ItemStack(canConfirm ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta cm = confirm.getItemMeta();
        if (cm != null) {
            cm.setDisplayName(canConfirm
                ? (mode == Mode.BUY ? c("&a&l✔ Acheter " + quantity + " unité(s)") : c("&a&l✔ Vendre " + quantity + " item(s)"))
                : c("&c&l✗ Impossible"));
            List<String> cLore = new ArrayList<>();
            if (mode == Mode.BUY) {
                if (!canConfirm && total > balance) cLore.add(c("&cFonds insuffisants !"));
                else cLore.add(c("&7Coût : &c" + total + " $"));
                cLore.add(c("&7Tu recevras : &f" + (quantity * data.reward().getAmount()) + "x item(s)"));
            } else {
                if (!canConfirm && quantity > maxQty) cLore.add(c("&cPas assez dans l'inventaire !"));
                else cLore.add(c("&7Gain : &6+" + total + " $"));
            }
            cm.setLore(cLore);
            confirm.setItemMeta(cm);
        }
        inv.setItem(SLOT_CONFIRM, confirm);
    }

    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) return;

        switch (slot) {
            case SLOT_BACK -> { backAction.run(); return; }
            case SLOT_MINUS64 -> adjust(-64, player);
            case SLOT_MINUS10 -> adjust(-10, player);
            case SLOT_MINUS1  -> adjust(-1, player);
            case SLOT_PLUS1   -> adjust(+1, player);
            case SLOT_PLUS10  -> adjust(+10, player);
            case SLOT_PLUS64  -> adjust(+64, player);
            case SLOT_QTY     -> setMax(player);
            case SLOT_CONFIRM -> confirm(player);
        }
    }

    private void adjust(int delta, Player player) {
        quantity = Math.max(1, quantity + delta);
        build(player);
    }

    private void setMax(Player player) {
        if (mode == Mode.BUY) {
            int balance = eco.getBalance(player.getUniqueId());
            quantity = Math.max(1, balance / Math.max(1, data.buyPrice()));
        } else {
            quantity = Math.max(1, countMat(player, data.reward().getType()));
        }
        build(player);
    }

    private void confirm(Player player) {
        int balance = eco.getBalance(player.getUniqueId());

        if (mode == Mode.BUY) {
            int total = quantity * data.buyPrice();
            if (total > balance) {
                player.sendMessage(c("&c✗ Fonds insuffisants ! &7Il te faut &e" + total
                    + " $ &7| Solde : &e" + balance + " $"));
                return;
            }
            if (!eco.removeBalance(player.getUniqueId(), total)) {
                player.sendMessage(c("&c✗ Fonds insuffisants !"));
                return;
            }
            for (int i = 0; i < quantity; i++) {
                ItemStack reward = data.reward().clone();
                java.util.Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
                leftover.values().forEach(item ->
                    player.getWorld().dropItemNaturally(player.getLocation(), item));
            }
            int items = quantity * data.reward().getAmount();
            player.sendMessage(c("&a✔ Acheté : &f" + items + "x " + c(data.name())
                + " &apour &e" + total + " $ &8| &7Solde : &e"
                + eco.getBalance(player.getUniqueId()) + " $"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);

        } else {
            Material mat = data.reward().getType();
            int inInv = countMat(player, mat);
            if (quantity > inInv) {
                player.sendMessage(c("&c✗ Tu n'as que &f" + inInv + "x " + c(data.name())
                    + " &cdans ton inventaire."));
                return;
            }
            int pricePerItem = (int) Math.floor((double) data.sellPrice() / data.reward().getAmount());
            int gained = quantity * pricePerItem;
            removeFromInventory(player, mat, quantity);
            eco.addBalance(player.getUniqueId(), gained);
            player.sendMessage(c("&e💰 Vendu : &f" + quantity + "x " + c(data.name())
                + " &epour &6+" + gained + " $ &8| &7Solde : &e"
                + eco.getBalance(player.getUniqueId()) + " $"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 0.8f);
        }
        player.closeInventory();
    }

    private int countMat(Player player, Material mat) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) count += item.getAmount();
        }
        return count;
    }

    private void removeFromInventory(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != mat) continue;
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
    }

    private ItemStack makeQtyBtn(Material mat, String name, int delta) {
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

    public int getQuantity()     { return quantity; }
    public ShopItemData getData() { return data; }
    public Mode getMode()         { return mode; }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
