package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class IslandStatsGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "✦ Statistiques ✦";
    private final OneBlock plugin;

    public IslandStatsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 36, TITLE);
        for (int i = 0; i < 36; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        long completed = 0;
        for (IslandChallenge ch : IslandChallenge.values()) if (island.isChallengeCompleted(ch)) completed++;

        inv.setItem(10, makeItem(Material.GRASS_BLOCK, "&a&lÎle",
            "&7Blocs cassés : &e" + island.getBlocksBroken(),
            "&7Niveau : &b" + island.getIslandLevel(),
            "&7Phase : " + island.getCurrentPhase().getColor() + island.getCurrentPhase().getDisplayName(),
            "&7Prestige : &d" + island.getPrestige(),
            "&7Membres : &e" + island.getAllMemberUUIDs().size()));

        inv.setItem(12, makeItem(Material.GOLD_INGOT, "&6&lÉconomie",
            "&7Ton solde : &e" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + " &7pièces",
            "&7Banque île : &e" + island.getBankBalance() + " &7pièces",
            "&7Multiplicateur prestige : &e×" + String.format("%.1f", island.getPrestigeMultiplier()),
            "&7Multiplicateur skills : &e×" + String.format("%.2f", plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId())),
            "&7Multiplicateur total : &e×" + String.format("%.2f",
                island.getPrestigeMultiplier() * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId()))));

        inv.setItem(14, makeItem(Material.BOOK, "&d&lDéfis & Missions",
            "&7Défis complétés : &e" + completed + "&7/&e" + IslandChallenge.values().length,
            "&7Missions du jour : voir /ob missions"));

        inv.setItem(16, makeItem(Material.IRON_SWORD, "&b&lCompétences",
            buildSkillLine(player, Skill.MINING),
            buildSkillLine(player, Skill.COMBAT),
            buildSkillLine(player, Skill.FARMING)));

        inv.setItem(22, makeItem(Material.NETHER_STAR, "&6&lRéglages île",
            "&7PvP : " + (island.isPvpEnabled() ? "&aActivé" : "&cDésactivé"),
            "&7Visiteurs : " + (island.isVisitorsAllowed() ? "&aAutorisés" : "&cBloqués"),
            "&7Warp : " + (island.isWarpEnabled() ? "&aActivé" : "&cDésactivé"),
            "&7MOTD : &7" + (island.getMotd().isEmpty() ? "&8Aucune" : island.getMotd())));

        inv.setItem(27, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }

    private String buildSkillLine(Player player, Skill skill) {
        int level = plugin.getSkillManager().getLevel(player.getUniqueId(), skill);
        return c("&7" + skill.getDisplayName() + " : &e" + level + "&7/" + Skill.MAX_LEVEL);
    }
}
