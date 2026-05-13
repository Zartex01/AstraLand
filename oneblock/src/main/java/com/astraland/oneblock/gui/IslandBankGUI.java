package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class IslandBankGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Banque de l'île ✦";
    private final OneBlock plugin;

    public IslandBankGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        long bankBal = island.getBankBalance();
        int persBal = plugin.getEconomyManager().getBalance(player.getUniqueId());

        inv.setItem(4, makeItem(Material.GOLD_BLOCK, "&6&lBanque de l'île",
            "&7Solde banque : &e" + bankBal + " &7pièces",
            "&7Ton solde : &e" + persBal + " &7pièces",
            "&8──────────────",
            "&7La banque est partagée entre tous les membres"));

        inv.setItem(11, makeItem(Material.EMERALD_BLOCK, "&a&lDéposer",
            "&7Clic gauche : &e+100 pièces",
            "&7Clic droit : &e+1 000 pièces",
            "&7Shift+clic : &eToutes tes pièces"));

        inv.setItem(13, makeItem(Material.SUNFLOWER, "&6Solde banque",
            "&e" + bankBal + " &6pièces"));

        inv.setItem(15, makeItem(Material.REDSTONE_BLOCK, "&c&lRetirer",
            "&7Clic gauche : &e-100 pièces",
            "&7Clic droit : &e-1 000 pièces",
            "&7Shift+clic : &eTout retirer",
            island.isOwner(player.getUniqueId()) || island.isCoOwner(player.getUniqueId())
                ? "&a✔ Tu peux retirer" : "&cSeuls les co-proprio/proprio peuvent retirer"));

        inv.setItem(22, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));

        player.openInventory(inv);
    }
}
