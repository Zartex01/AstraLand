package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.SkillManager;
import com.astraland.oneblock.models.Skill;
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

public class SkillsGUI {

    public static final String TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "✦ Compétences ✦";
    private final OneBlock plugin;

    public SkillsGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        SkillManager sm = plugin.getSkillManager();
        int[] slots = {10, 13, 16};
        Skill[] skills = Skill.values();

        for (int i = 0; i < skills.length && i < slots.length; i++) {
            Skill skill = skills[i];
            long xp = sm.getXP(player.getUniqueId(), skill);
            int level = skill.levelFromXp(xp);
            long xpInLevel = skill.xpInCurrentLevel(xp);
            long xpNeeded = skill.xpNeededForNext(xp);
            boolean maxed = level >= Skill.MAX_LEVEL;

            ItemStack item = new ItemStack(skill.getIcon());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(c("&b&l" + skill.getDisplayName() + " &8- Niv." + level));

            List<String> lore = new ArrayList<>();
            lore.add(c("&7" + skill.getDescription()));
            lore.add(c("&8──────────────"));
            lore.add(c("&7Niveau : &e" + level + "&7/&e" + Skill.MAX_LEVEL));
            if (maxed) {
                lore.add(c("&a✔ Niveau maximum atteint !"));
            } else {
                lore.add(c("&7XP : &e" + xpInLevel + "&7/&e" + xpNeeded));
                lore.add(c("&7" + buildXpBar(xpInLevel, xpNeeded)));
            }
            lore.add(c("&8──────────────"));
            lore.add(c("&7Bonus actif :"));
            lore.add(c(skill.getPerkDescription()));
            lore.add(c("&8──────────────"));

            double moneyBonus = skill.getMoneyMultiplierBonus(level);
            double lootBonus = skill.getLootChanceBonus(level);
            if (moneyBonus > 0) lore.add(c("&a+" + (int)(moneyBonus * 100) + "% &7argent gagné"));
            if (lootBonus > 0) lore.add(c("&a+" + (int)(lootBonus * 100) + "% &7chance loot bonus"));

            lore.add(c("&8──────────────"));
            lore.add(c("&7Multiplicateur total : &e×" + String.format("%.2f", sm.getTotalMoneyMultiplier(player.getUniqueId()))));

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        double totalMult = sm.getTotalMoneyMultiplier(player.getUniqueId());
        inv.setItem(22, makeItem(Material.NETHER_STAR, "&6&lMultiplicateur total",
            "&7Toutes compétences confondues :",
            "&e×" + String.format("%.2f", totalMult) + " &7sur tous tes gains"));

        inv.setItem(18, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }

    private String buildXpBar(long current, long max) {
        if (max <= 0) return "&a✔ MAX";
        int bars = 10;
        int filled = (int) Math.min(bars, (current * bars) / max);
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < bars; i++) sb.append(i < filled ? "&b█" : "&8█");
        sb.append("&8]");
        return sb.toString();
    }
}
