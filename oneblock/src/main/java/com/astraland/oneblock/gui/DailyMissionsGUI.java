package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.DailyMission;
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

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class DailyMissionsGUI {

    public static final String TITLE = ChatColor.YELLOW + "" + ChatColor.BOLD + "✦ Missions du jour ✦";
    private final OneBlock plugin;

    public DailyMissionsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        List<DailyMission> missions = plugin.getDailyMissionManager().getDailyMissions(player.getUniqueId());
        long seconds = plugin.getDailyMissionManager().getSecondsUntilReset(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 36, TITLE);
        for (int i = 0; i < 36; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        int[] slots = {10, 12, 14, 16, 13};
        for (int i = 0; i < missions.size() && i < slots.length; i++) {
            inv.setItem(slots[i], buildMissionItem(missions.get(i)));
        }

        String timeStr = formatTime(seconds);
        inv.setItem(4, makeItem(Material.CLOCK, "&e&lReset dans : &f" + timeStr,
            "&7Tes missions se renouvèlent chaque 24h",
            "&8──────────────",
            "&7Complète toutes les missions pour",
            "&7maximiser tes gains quotidiens !"));

        long claimed = missions.stream().filter(DailyMission::isClaimed).count();
        inv.setItem(31, makeItem(Material.NETHER_STAR,
            "&6&lProgressions : &e" + claimed + "&6/&e" + missions.size(),
            "&7Missions complétées aujourd'hui"));

        inv.setItem(27, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }

    private ItemStack buildMissionItem(DailyMission m) {
        boolean claimable = m.isClaimable();
        boolean claimed = m.isClaimed();

        ItemStack item = new ItemStack(m.getIcon());
        if (claimed) item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();

        if (claimed) {
            meta.setDisplayName(c("&a&l✔ " + m.getDisplayName()));
        } else if (claimable) {
            meta.setDisplayName(c("&e&l⚡ " + m.getDisplayName()));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.setDisplayName(c("&7" + m.getDisplayName()));
        }

        List<String> lore = new ArrayList<>();
        lore.add(c("&8" + m.getDescription()));
        lore.add(c("&8──────────────"));

        if (claimed) {
            lore.add(c("&a✔ Mission accomplie ! Récompense perçue."));
        } else {
            long progress = Math.min(m.getProgress(), m.getTarget());
            lore.add(c("&7Progression : &e" + progress + "&7/&e" + m.getTarget()));
            lore.add(c("&7" + buildBar(progress, m.getTarget())));
            lore.add(c("&8──────────────"));
            lore.add(c("&7Récompense : &e" + m.getReward() + " &7pièces"));
            if (claimable) {
                lore.add(c("&a⚡ Clique pour récupérer ta récompense !"));
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String buildBar(long done, long total) {
        int filled = (int) Math.min(10, total > 0 ? (done * 10) / total : 10);
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) sb.append(i < filled ? "&e█" : "&7█");
        sb.append("&8]");
        return sb.toString();
    }

    private String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return h + "h " + m + "m " + s + "s";
    }
}
