package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OBCollection;
import com.astraland.oneblock.models.OneBlockIsland;
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

public class CollectionsGUI {

    public static final String TITLE = ChatColor.AQUA + "" + ChatColor.BOLD + "✦ Collections ✦";
    private final OneBlock plugin;

    public CollectionsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        OBCollection[] cols = OBCollection.values();
        int rows = Math.max(4, (int) Math.ceil((cols.length + 9) / 9.0) + 1);
        rows = Math.min(6, rows);
        Inventory inv = Bukkit.createInventory(null, rows * 9, TITLE);
        for (int i = 0; i < rows * 9; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        for (int i = 0; i < cols.length && i < (rows - 1) * 9; i++) {
            OBCollection col = cols[i];
            long collected = island.getCollection(col.getMaterial().name());
            int claimedMilestone = island.getClaimedMilestone(col.name());
            int nextMilestone = claimedMilestone + 1;
            boolean canClaim = nextMilestone < col.getMilestoneCount()
                && collected >= col.getMilestoneAmount(nextMilestone);

            ItemStack item = new ItemStack(col.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(c("&b&l" + col.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add(c("&7Collecté : &e" + collected));
            lore.add(c("&8──────────────"));
            for (int m = 0; m < col.getMilestoneCount(); m++) {
                long needed = col.getMilestoneAmount(m);
                int reward = col.getMilestoneReward(m);
                if (m <= claimedMilestone) {
                    lore.add(c("&a✔ Palier " + (m + 1) + " (&e" + needed + "&a) → &6+" + reward + " &apièces"));
                } else if (m == nextMilestone) {
                    lore.add(c("&e⚡ Palier " + (m + 1) + " (&f" + needed + "&e) → &6+" + reward + " &epièces"));
                    lore.add(c("&7" + buildBar(collected, needed)));
                } else {
                    lore.add(c("&8✗ Palier " + (m + 1) + " (&8" + needed + "&8) → &8+" + reward + " pièces"));
                }
            }
            if (canClaim) {
                lore.add(c("&8──────────────"));
                lore.add(c("&a&l⚡ Clique pour récupérer la récompense !"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        long totalClaimed = 0;
        for (OBCollection col : cols) totalClaimed += island.getClaimedMilestone(col.name()) + 1;
        long maxMilestones = (long) OBCollection.values().length * 5;

        inv.setItem((rows - 1) * 9, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        inv.setItem((rows - 1) * 9 + 4, makeItem(Material.NETHER_STAR,
            "&b&lCollections : &e" + totalClaimed + "&b/" + maxMilestones + " &bpaliers",
            "&7Collecte des items pour débloquer des récompenses !"));
        player.openInventory(inv);
    }

    private String buildBar(long have, long need) {
        int filled = (int) Math.min(10, need > 0 ? (have * 10) / need : 10);
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) sb.append(i < filled ? "&b█" : "&8█");
        sb.append("&8]");
        return sb.toString();
    }
}
