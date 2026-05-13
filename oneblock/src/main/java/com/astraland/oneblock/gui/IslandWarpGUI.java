package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class IslandWarpGUI {

    public static final String TITLE = ChatColor.GREEN + "" + ChatColor.BOLD + "✦ Warps publics ✦";
    private final OneBlock plugin;

    public IslandWarpGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player, int page) {
        List<OneBlockIsland> warps = plugin.getOneBlockManager().getPublicWarps();
        int perPage = 45;
        int maxPage = Math.max(0, (warps.size() - 1) / perPage);
        if (page > maxPage) page = maxPage;
        int start = page * perPage;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        ItemStack glass = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);

        for (int i = 0; i < 45 && (start + i) < warps.size(); i++) {
            OneBlockIsland isl = warps.get(start + i);
            OfflinePlayer op = Bukkit.getOfflinePlayer(isl.getOwner());
            String name = op.getName() != null ? op.getName() : "?";
            String warpName = isl.getWarpName().isEmpty() ? "Île de " + name : isl.getWarpName();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(op);
            meta.setDisplayName(c("&a&l" + warpName));
            meta.setLore(Arrays.asList(
                c("&7Propriétaire : &e" + name),
                c("&7Blocs cassés : &e" + isl.getBlocksBroken()),
                c("&7Phase : " + isl.getCurrentPhase().getColor() + isl.getCurrentPhase().getDisplayName()),
                c("&7Membres : &e" + (isl.getMembers().size() + 1)),
                c("&8──────────────"),
                c("&aClique pour visiter !")
            ));
            head.setItemMeta(meta);
            inv.setItem(i, head);
        }

        if (warps.isEmpty()) {
            inv.setItem(22, makeItem(Material.BARRIER, "&cAucun warp disponible",
                "&7Aucune île n'a activé son warp public."));
        }

        inv.setItem(45, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        if (page > 0)
            inv.setItem(48, makeItem(Material.SPECTRAL_ARROW, "&e← Page précédente", "&8Page " + page + "/" + (maxPage + 1)));
        inv.setItem(49, makeItem(Material.COMPASS, "&7Page " + (page + 1) + "/" + (maxPage + 1)));
        if (page < maxPage)
            inv.setItem(50, makeItem(Material.SPECTRAL_ARROW, "&ePage suivante →", "&8Page " + (page + 2) + "/" + (maxPage + 1)));

        player.openInventory(inv);
    }
}
