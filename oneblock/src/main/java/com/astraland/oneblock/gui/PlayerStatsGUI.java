package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.PlayerStatsManager;
import com.astraland.oneblock.models.OBAchievement;
import com.astraland.oneblock.models.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class PlayerStatsGUI {

    public static final String TITLE = ChatColor.YELLOW + "" + ChatColor.BOLD + "✦ Mes Stats ✦";
    private final OneBlock plugin;

    public PlayerStatsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);
        for (int i = 0; i < 45; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        // Player head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        sm.setDisplayName(c("&e&l" + player.getName()));
        sm.setLore(List.of(c("&7Tes statistiques personnelles")));
        head.setItemMeta(sm);
        inv.setItem(4, head);

        PlayerStatsManager ps = plugin.getPlayerStatsManager();

        // Combat stats
        inv.setItem(10, buildStat(Material.DIAMOND_SWORD, "&c&lCombat",
            "&7Mobs tués : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.MOBS_KILLED),
            "&7Lucky Events : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.LUCKY_EVENTS),
            "&7Spawners trouvés : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.SPAWNERS_FOUND)));

        // Mining stats
        inv.setItem(12, buildStat(Material.DIAMOND_PICKAXE, "&b&lMinage",
            "&7Blocs cassés : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.BLOCKS_BROKEN),
            "&7Améliorations achetées : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.UPGRADES_BOUGHT)));

        // Economy stats
        inv.setItem(14, buildStat(Material.GOLD_INGOT, "&6&lÉconomie",
            "&7Pièces gagnées (total) : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.COINS_EARNED),
            "&7Solde actuel : &e" + plugin.getEconomyManager().getBalance(player.getUniqueId())));

        // Missions stats
        inv.setItem(16, buildStat(Material.BOOK, "&e&lMissions",
            "&7Missions journalières : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.DAILY_COMPLETED),
            "&7Missions hebdo : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.WEEKLY_COMPLETED),
            "&7Paliers collection : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.COLLECTION_MILESTONES_REACHED)));

        // Skills summary
        inv.setItem(22, buildStat(Material.EXPERIENCE_BOTTLE, "&a&lCompétences",
            "&7Minage : Niv. &e" + plugin.getSkillManager().getLevel(player.getUniqueId(), Skill.MINING) + " &7/ " + Skill.MAX_LEVEL,
            "&7Combat : Niv. &e" + plugin.getSkillManager().getLevel(player.getUniqueId(), Skill.COMBAT) + " &7/ " + Skill.MAX_LEVEL,
            "&7Récolte : Niv. &e" + plugin.getSkillManager().getLevel(player.getUniqueId(), Skill.FARMING) + " &7/ " + Skill.MAX_LEVEL,
            "&7Multiplicateur total : &e×" + String.format("%.2f", plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId()))));

        // Achievements
        int achCount = plugin.getAchievementManager().countUnlocked(player.getUniqueId());
        inv.setItem(28, buildStat(Material.NETHER_STAR, "&6&lSuccès",
            "&7Débloqués : &e" + achCount + "&7/" + OBAchievement.values().length,
            "&7Récompenses totales gagnées via succès !"));

        // Prestige
        inv.setItem(34, buildStat(Material.END_CRYSTAL, "&d&lPrestige",
            "&7Prestiges effectués : &e" + ps.get(player.getUniqueId(), PlayerStatsManager.Stat.PRESTIGE_DONE)));

        inv.setItem(36, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }

    private ItemStack buildStat(Material mat, String title, String... lines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(c(title));
        List<String> lore = new ArrayList<>();
        for (String l : lines) lore.add(c(l));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
