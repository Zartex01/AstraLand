package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class IslandUpgradesGUI {

    public static final String TITLE = ChatColor.AQUA + "" + ChatColor.BOLD + "✦ Améliorations de l'île ✦";
    private final OneBlock plugin;

    public IslandUpgradesGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        int[] slots = {10, 12, 14, 16};
        UpgradeType[] types = UpgradeType.values();

        int balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        for (int i = 0; i < types.length; i++) {
            UpgradeType type = types[i];
            int level = island.getUpgradeLevel(type);
            int cost = type.getCost(level);
            boolean maxed = level >= type.getMaxLevel();

            ItemStack item = new ItemStack(type.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(c("&b&l" + type.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add(c("&7" + type.getDescription()));
            lore.add(c("&8──────────────"));
            lore.add(c("&7Niveau : " + buildLevelBar(level, type.getMaxLevel())));
            lore.add(c("&7Niveau actuel : &e" + level + "/" + type.getMaxLevel()));
            lore.add(c("&8──────────────"));
            if (maxed) {
                lore.add(c("&a✔ Amélioration maximale !"));
            } else {
                lore.add(c("&7Coût : &e" + cost + " &7pièces"));
                lore.add(balance >= cost
                    ? c("&aClique pour améliorer !")
                    : c("&cPas assez de pièces (" + balance + "/" + cost + ")"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        int eco = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(18, makeItem(Material.SUNFLOWER, "&6Ton solde : &e" + eco + " &6pièces"));
        inv.setItem(26, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));

        player.openInventory(inv);
    }

    private String buildLevelBar(int level, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            sb.append(i <= level ? "&a█" : "&8█");
        }
        return sb.toString();
    }
}
