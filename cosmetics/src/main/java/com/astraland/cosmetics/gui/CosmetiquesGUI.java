package com.astraland.cosmetics.gui;

import com.astraland.cosmetics.Cosmetics;
import com.astraland.cosmetics.gui.holder.CosmetiquesHolder;
import com.astraland.cosmetics.model.CosmetiqueData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CosmetiquesGUI {

    private static final int SLOTS_PAR_PAGE = 45;
    private static final int SLOT_PREV      = 45;
    private static final int SLOT_INFO      = 49;
    private static final int SLOT_NEXT      = 53;

    private final Cosmetics plugin;

    public CosmetiquesGUI(Cosmetics plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        List<CosmetiqueData> tous = plugin.getCosmetiquesManager().getListe();
        List<CosmetiqueData> visibles = filtrerVisibles(player, tous);

        int totalPages = Math.max(1, (int) Math.ceil(visibles.size() / (double) SLOTS_PAR_PAGE));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inv = Bukkit.createInventory(new CosmetiquesHolder(), 54,
                c("&8» &d&lAstraLand &8- &7Cosmétiques «"));

        ItemStack gray = glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);

        int start = page * SLOTS_PAR_PAGE;
        int end   = Math.min(start + SLOTS_PAR_PAGE, visibles.size());

        for (int i = start; i < end; i++) {
            CosmetiqueData cosm = visibles.get(i);
            int slot = i - start;

            Material mat = parseMaterial(cosm.getItem());
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(cosm.getNom());

                List<String> lore = new ArrayList<>(cosm.getDescription());
                if (cosm.hasPermission() && !player.hasPermission(cosm.getPermission())) {
                    lore.add("");
                    lore.add(c("&c✗ &7Permission requise : &e" + cosm.getPermission()));
                }
                meta.setLore(lore);

                if (cosm.isEnchante()) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }

            if (cosm.hasPermission() && !player.hasPermission(cosm.getPermission())) {
                item = toGrayedOut(item, cosm);
            }

            inv.setItem(slot, item);
        }

        if (page > 0) {
            inv.setItem(SLOT_PREV, makeItem(Material.ARROW,
                    c("&e&l◀ Page précédente"),
                    c("&7Page ") + page + c(" / ") + totalPages));
        } else {
            inv.setItem(SLOT_PREV, glass(Material.GRAY_STAINED_GLASS_PANE));
        }

        inv.setItem(SLOT_INFO, makeItem(Material.COMPASS,
                c("&d&lCosmétiques"),
                c("&7Page : &e") + (page + 1) + c(" / &e") + totalPages,
                c("&7Total : &e") + visibles.size() + c(" cosmétique(s)")));

        if (page < totalPages - 1) {
            inv.setItem(SLOT_NEXT, makeItem(Material.ARROW,
                    c("&e&lPage suivante ▶"),
                    c("&7Page ") + (page + 2) + c(" / ") + totalPages));
        } else {
            inv.setItem(SLOT_NEXT, glass(Material.GRAY_STAINED_GLASS_PANE));
        }

        player.openInventory(inv);
    }

    private List<CosmetiqueData> filtrerVisibles(Player player, List<CosmetiqueData> tous) {
        return new ArrayList<>(tous);
    }

    private ItemStack toGrayedOut(ItemStack original, CosmetiqueData cosm) {
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = gray.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(cosm.getNom() + c(" &8(Verrouillé)"));
            List<String> lore = new ArrayList<>(cosm.getDescription());
            lore.add("");
            lore.add(c("&c✗ Permission requise : &e" + cosm.getPermission()));
            meta.setLore(lore);
            gray.setItemMeta(meta);
        }
        return gray;
    }

    private Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.PAPER;
        }
    }

    private ItemStack makeItem(Material mat, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && lore.length > 0) {
            meta.setDisplayName(lore[0]);
            List<String> loreList = new ArrayList<>();
            for (int i = 1; i < lore.length; i++) loreList.add(lore[i]);
            if (!loreList.isEmpty()) meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack glass(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static int getSlotPrev() { return SLOT_PREV; }
    public static int getSlotNext() { return SLOT_NEXT; }
    public static int getSlotInfo() { return SLOT_INFO; }
    public static int getSlotsParPage() { return SLOTS_PAR_PAGE; }
}
