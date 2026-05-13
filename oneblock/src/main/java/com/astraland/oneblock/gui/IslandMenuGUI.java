package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class IslandMenuGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Mon Île OneBlock ✦";

    private final OneBlock plugin;

    public IslandMenuGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(c("&cTu n'as pas d'île. Utilise &e/ob create&c."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        fill(inv);

        OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwner());
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(owner);
        sm.setDisplayName(c("&6&l" + (owner.getName() != null ? owner.getName() : "?")));
        Phase ph = island.getCurrentPhase();
        Phase[] phases = Phase.values();
        Phase next = null;
        for (int i = 0; i < phases.length; i++) if (phases[i] == ph && i + 1 < phases.length) { next = phases[i + 1]; break; }
        long toNext = next != null ? next.getBlocksRequired() - island.getBlocksBroken() : 0;
        sm.setLore(Arrays.asList(
            c("&7Propriétaire de l'île"),
            c("&8──────────────"),
            c("&7Blocs cassés : &e" + island.getBlocksBroken()),
            c("&7Niveau : &b" + island.getIslandLevel()),
            c("&7Phase : " + ph.getColor() + ph.getDisplayName()),
            next != null ? c("&7Prochaine phase : &e" + toNext + " &7blocs") : c("&a✔ Phase maximale !"),
            c("&7Membres : &e" + (island.getMembers().size() + 1))
        ));
        head.setItemMeta(sm);
        inv.setItem(4, head);

        inv.setItem(19, makeItem(Material.OAK_DOOR, "&e&lGestion des membres",
            "&7Inviter, expulser ou voir tes coéquipiers", "&8Clique pour ouvrir"));
        inv.setItem(21, makeItem(Material.NETHER_STAR, "&b&lAméliorations",
            "&7Améliore ton générateur, tes drops...", "&8Clique pour ouvrir"));
        inv.setItem(23, makeItem(Material.BOOK, "&d&lDéfis",
            "&7Complète des missions pour gagner des récompenses", "&8Clique pour ouvrir"));
        inv.setItem(25, makeItem(Material.COMPASS, "&a&lWarps publics",
            "&7Visite les îles d'autres joueurs", "&8Clique pour ouvrir"));
        inv.setItem(31, makeItem(Material.REDSTONE, "&c&lParamètres",
            "&7PvP, visiteurs, warp public...", "&8Clique pour ouvrir"));

        int eco = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(48, makeItem(Material.SUNFLOWER, "&6Solde",
            "&7Pièces : &e" + eco));
        inv.setItem(49, makeItem(Material.GRASS_BLOCK, "&aAller à l'île",
            "&7Téléportation vers ton île", "&8Clique pour rentrer"));
        inv.setItem(50, makeItem(Material.CLOCK, "&7Phase actuelle",
            ph.getColor() + "&l" + ph.getDisplayName(),
            "&7Blocs cassés : &e" + island.getBlocksBroken()));

        player.openInventory(inv);
    }

    private void fill(Inventory inv) {
        ItemStack glass = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            int row = i / 9, col = i % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) inv.setItem(i, glass);
        }
        ItemStack black = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 9; i < 45; i++) inv.setItem(i, black);
    }

    public static ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore.length > 0) {
            List<String> l = new java.util.ArrayList<>();
            for (String s : lore) l.add(ChatColor.translateAlternateColorCodes('&', s));
            meta.setLore(l);
        }
        item.setItemMeta(meta);
        return item;
    }
}
