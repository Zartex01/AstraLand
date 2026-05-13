package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.WeeklyMission;
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

public class WeeklyMissionsGUI {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ Missions Hebdo ✦";
    private final OneBlock plugin;

    public WeeklyMissionsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        List<WeeklyMission> missions = plugin.getWeeklyMissionManager().getWeeklyMissions(player.getUniqueId());
        long secs = plugin.getWeeklyMissionManager().getSecondsUntilReset(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        int[] slots = {10, 13, 16};
        for (int i = 0; i < missions.size() && i < slots.length; i++) {
            inv.setItem(slots[i], buildItem(missions.get(i)));
        }

        inv.setItem(4, makeItem(Material.CLOCK, "&d&lReset dans : &f" + formatTime(secs),
            "&7Tes missions se renouvèlent chaque semaine",
            "&8──────────────",
            "&7Les missions hebdomadaires offrent de",
            "&7bien meilleures récompenses que les journalières !"));

        inv.setItem(22, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }

    private ItemStack buildItem(WeeklyMission m) {
        ItemStack item = new ItemStack(m.isClaimed() ? Material.LIME_DYE : m.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (m.isClaimed()) {
            meta.setDisplayName(c("&a&l✔ " + m.getDisplayName()));
        } else if (m.isClaimable()) {
            meta.setDisplayName(c("&e&l⚡ " + m.getDisplayName()));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.setDisplayName(c("&d" + m.getDisplayName()));
        }
        List<String> lore = new ArrayList<>();
        lore.add(c("&8" + m.getDescription()));
        lore.add(c("&8──────────────"));
        if (m.isClaimed()) {
            lore.add(c("&a✔ Récompense perçue !"));
        } else {
            lore.add(c("&7Progression : &e" + Math.min(m.getProgress(), m.getTarget()) + "&7/&e" + m.getTarget()));
            lore.add(c("&7" + buildBar(m.getProgress(), m.getTarget())));
            lore.add(c("&8──────────────"));
            lore.add(c("&7Récompense : &e" + m.getReward() + " &7pièces"));
            if (m.isClaimable()) lore.add(c("&a&l⚡ Clique pour récupérer !"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String buildBar(long done, long total) {
        int filled = (int) Math.min(10, total > 0 ? (done * 10) / total : 10);
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) sb.append(i < filled ? "&d█" : "&8█");
        sb.append("&8]");
        return sb.toString();
    }

    private String formatTime(long secs) {
        long d = secs / 86400, h = (secs % 86400) / 3600, m = (secs % 3600) / 60;
        return d + "j " + h + "h " + m + "m";
    }
}
