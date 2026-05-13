package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.Boost;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class BoostGUI {

    public static final String TITLE = ChatColor.RED + "" + ChatColor.BOLD + "✦ Boosts d'île ✦";
    private final OneBlock plugin;

    public BoostGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        Boost active = plugin.getBoostManager().getBoost(island.getOwner());
        if (active != null) {
            inv.setItem(4, makeItem(Material.BEACON,
                "&a&l⚡ Boost actif : " + active.getType().getDisplayName(),
                "&7Multiplicateur : &e×" + active.getType().getMultiplier(),
                "&7Temps restant : &e" + active.formatRemaining(),
                "&8──────────────",
                "&7Le boost s'applique à tous les gains de l'île."));
        } else {
            inv.setItem(4, makeItem(Material.BARRIER,
                "&c&lAucun boost actif",
                "&7Achète un boost pour multiplier tes gains !"));
        }

        int[] slots = {10, 12, 14, 16};
        Boost.BoostType[] types = Boost.BoostType.values();
        int bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
        for (int i = 0; i < types.length && i < slots.length; i++) {
            Boost.BoostType t = types[i];
            boolean canAfford = bal >= t.getCost();
            inv.setItem(slots[i], makeItem(t.getIcon(),
                (canAfford ? "&e" : "&c") + "&l" + t.getDisplayName(),
                "&7" + t.getDescription(),
                "&7Durée : &e" + t.formatDuration(),
                "&7Coût : " + (canAfford ? "&a" : "&c") + t.getCost() + " &7pièces",
                "&8──────────────",
                canAfford ? "&a&lClique pour activer !" : "&cPas assez de pièces."));
        }

        inv.setItem(22, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }
}
