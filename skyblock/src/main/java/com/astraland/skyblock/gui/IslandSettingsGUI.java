package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class IslandSettingsGUI implements InventoryHolder {

    private final Inventory inv;
    private final Island island;
    private final Skyblock plugin;

    public IslandSettingsGUI(Island island, Skyblock plugin) {
        this.island = island;
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(this, 27, c("&8⚙ &a&lParamètres de l'île"));
        build();
    }

    private void build() {
        inv.clear();
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, border);

        // Slot 10 — Verrou
        inv.setItem(10, toggle(
            island.isLocked() ? Material.RED_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE,
            island.isLocked() ? "&c🔒 Île Verrouillée" : "&a🔓 Île Ouverte",
            island.isLocked(),
            island.isLocked()
                ? List.of("&7Les visiteurs ne peuvent pas entrer.", "&e▶ Cliquer pour déverrouiller")
                : List.of("&7Tout le monde peut visiter.", "&e▶ Cliquer pour verrouiller")
        ));

        // Slot 12 — PvP
        inv.setItem(12, toggle(
            island.isPvpEnabled() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
            island.isPvpEnabled() ? "&cPvP Activé" : "&aPvP Désactivé",
            !island.isPvpEnabled(),
            List.of(
                "&7PvP entre joueurs sur l'île.",
                island.isPvpEnabled() ? "&e▶ Cliquer pour désactiver" : "&e▶ Cliquer pour activer"
            )
        ));

        // Slot 14 — Warp public
        inv.setItem(14, toggle(
            island.isWarpEnabled() ? Material.ENDER_PEARL : Material.ENDER_EYE,
            island.isWarpEnabled() ? "&aWarp Public Activé" : "&cWarp Public Désactivé",
            !island.isWarpEnabled(),
            List.of(
                "&7Les autres peuvent &e/is warp &7ton île.",
                island.isWarpEnabled() ? "&e▶ Cliquer pour désactiver" : "&e▶ Cliquer pour activer"
            )
        ));

        // Slot 16 — Visiteurs peuvent construire
        inv.setItem(16, toggle(
            island.isVisitorsCanBuild() ? Material.OAK_PLANKS : Material.BARRIER,
            island.isVisitorsCanBuild() ? "&aVisiteurs Peuvent Construire" : "&cVisiteurs Ne Peuvent Pas Construire",
            !island.isVisitorsCanBuild(),
            List.of("&7Autoriser les visiteurs à poser des blocs.", "&e▶ Cliquer pour basculer")
        ));

        // Slot 11 — Visiteurs peuvent casser
        inv.setItem(11, toggle(
            island.isVisitorsCanBreak() ? Material.IRON_PICKAXE : Material.BARRIER,
            island.isVisitorsCanBreak() ? "&aVisiteurs Peuvent Casser" : "&cVisiteurs Ne Peuvent Pas Casser",
            !island.isVisitorsCanBreak(),
            List.of("&7Autoriser les visiteurs à casser des blocs.", "&e▶ Cliquer pour basculer")
        ));

        // Slot 15 — Visiteurs peuvent ouvrir coffres
        inv.setItem(15, toggle(
            island.isVisitorsCanOpenChests() ? Material.CHEST : Material.TRAPPED_CHEST,
            island.isVisitorsCanOpenChests() ? "&aVisiteurs Peuvent Ouvrir Coffres" : "&cCoffres Protégés",
            !island.isVisitorsCanOpenChests(),
            List.of("&7Autoriser les visiteurs à ouvrir les coffres.", "&e▶ Cliquer pour basculer")
        ));

        // Slot 13 — Fermer
        ItemStack close = new ItemStack(Material.ARROW);
        ItemMeta cm = close.getItemMeta();
        if (cm != null) { cm.setDisplayName(c("&c← Fermer")); close.setItemMeta(cm); }
        inv.setItem(22, close);
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        if (!island.isOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire peut modifier les paramètres."));
            return;
        }
        int slot = e.getRawSlot();
        switch (slot) {
            case 10 -> { island.setLocked(!island.isLocked());                        plugin.getIslandManager().saveAll(); }
            case 12 -> { island.setPvpEnabled(!island.isPvpEnabled());                plugin.getIslandManager().saveAll(); }
            case 14 -> { island.setWarpEnabled(!island.isWarpEnabled());              plugin.getIslandManager().saveAll(); }
            case 16 -> { island.setVisitorsCanBuild(!island.isVisitorsCanBuild());    plugin.getIslandManager().saveAll(); }
            case 11 -> { island.setVisitorsCanBreak(!island.isVisitorsCanBreak());    plugin.getIslandManager().saveAll(); }
            case 15 -> { island.setVisitorsCanOpenChests(!island.isVisitorsCanOpenChests()); plugin.getIslandManager().saveAll(); }
            case 22 -> { player.closeInventory(); return; }
            default -> { return; }
        }
        build();
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    private ItemStack toggle(Material mat, String name, boolean active, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        if (m != null) { m.setDisplayName(c(name)); m.setLore(lore.stream().map(this::c).toList()); item.setItemMeta(m); }
        return item;
    }
    private ItemStack toggle(Material mat, String name, boolean active) {
        return toggle(mat, name, active, List.of());
    }
    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
