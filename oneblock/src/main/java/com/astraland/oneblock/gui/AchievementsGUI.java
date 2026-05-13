package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OBAchievement;
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
import java.util.Set;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class AchievementsGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Succès ✦";
    private final OneBlock plugin;

    public AchievementsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OBAchievement[] achs = OBAchievement.values();
        int rows = Math.min(6, Math.max(4, (int) Math.ceil((achs.length + 9) / 9.0) + 1));
        Inventory inv = Bukkit.createInventory(null, rows * 9, TITLE);
        for (int i = 0; i < rows * 9; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        Set<String> unlocked = plugin.getAchievementManager().getUnlocked(player.getUniqueId());

        for (int i = 0; i < achs.length && i < (rows - 1) * 9; i++) {
            OBAchievement ach = achs[i];
            boolean done = unlocked.contains(ach.name());
            ItemStack item = new ItemStack(done ? ach.getIcon() : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(done ? c("&a&l✔ " + ach.getDisplayName()) : c("&7" + ach.getDisplayName()));
            List<String> lore = new ArrayList<>();
            lore.add(c("&8" + ach.getDescription()));
            lore.add(c("&8──────────────"));
            lore.add(c("&7Récompense : &e" + ach.getReward() + " &7pièces"));
            if (done) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                lore.add(c("&a✔ Accompli !"));
            } else {
                lore.add(c("&8Objectif : &7" + ach.getThreshold() + " " + formatType(ach.getType())));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        int count = unlocked.size();
        inv.setItem((rows - 1) * 9, makeItem(Material.ARROW, "&7← Retour"));
        inv.setItem((rows - 1) * 9 + 4, makeItem(Material.NETHER_STAR,
            "&6&lSuccès : &e" + count + "&6/" + achs.length,
            "&7Débloque des succès pour gagner des pièces !"));
        player.openInventory(inv);
    }

    private String formatType(OBAchievement.AchType t) {
        return switch (t) {
            case BLOCKS_BROKEN -> "blocs cassés";
            case MOBS_KILLED -> "mobs tués";
            case COINS_EARNED -> "pièces gagnées";
            case PHASE_UNLOCKED -> "phases débloquées";
            case PRESTIGE -> "prestiges";
            case LUCKY_EVENTS -> "lucky events";
            case SPAWNERS_FOUND -> "spawners trouvés";
            case MAX_SKILL -> "niveau de compétence";
            case UPGRADES_BOUGHT -> "améliorations achetées";
            case COLLECTION_MILESTONES -> "paliers de collection";
        };
    }
}
