package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class IslandVaultGUI {

    public static final String TITLE_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Coffre d'Île" + ChatColor.DARK_GRAY + "]";
    private final OneBlock plugin;

    public IslandVaultGUI(OneBlock plugin) { this.plugin = plugin; }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTu n'as pas d'île."));
            return;
        }
        if (!island.isMember(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTu n'es pas membre de cette île."));
            return;
        }
        Inventory vault = plugin.getIslandVaultManager().getVault(island.getOwner());
        player.openInventory(vault);
    }
}
