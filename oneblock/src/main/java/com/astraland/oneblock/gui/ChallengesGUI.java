package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
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

public class ChallengesGUI {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ Défis de l'île ✦";
    private final OneBlock plugin;

    public ChallengesGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        IslandChallenge[] challenges = IslandChallenge.values();
        int rows = Math.max(3, (int) Math.ceil((challenges.length + 9) / 9.0) + 1);
        rows = Math.min(rows, 6);

        Inventory inv = Bukkit.createInventory(null, rows * 9, TITLE);

        for (int i = 0; i < challenges.length && i < (rows - 1) * 9; i++) {
            IslandChallenge ch = challenges[i];
            inv.setItem(i, buildChallengeItem(ch, island));
        }

        int bottom = (rows - 1) * 9;
        ItemStack glass = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = bottom; i < rows * 9; i++) inv.setItem(i, glass);

        long completed = countCompleted(island);
        inv.setItem(bottom + 4, makeItem(Material.NETHER_STAR,
            "&6&lProgressions",
            "&7Défis complétés : &e" + completed + "&7/&e" + challenges.length));
        inv.setItem(bottom, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));

        player.openInventory(inv);
    }

    private ItemStack buildChallengeItem(IslandChallenge ch, OneBlockIsland island) {
        boolean completed = island.isChallengeCompleted(ch);
        boolean claimable = island.isChallengeClaimable(ch);
        long progress = island.getChallengeProgress(ch);
        long target = ch.getTarget();

        ItemStack item = new ItemStack(completed ? Material.LIME_DYE : (claimable ? Material.YELLOW_DYE : ch.getIcon()));
        if (claimable) item = new ItemStack(ch.getIcon());

        ItemMeta meta = item.getItemMeta();
        if (completed) {
            meta.setDisplayName(c("&a&l✔ " + ch.getDisplayName()));
        } else if (claimable) {
            meta.setDisplayName(c("&e&l⚡ " + ch.getDisplayName()));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.setDisplayName(c("&7" + ch.getDisplayName()));
        }

        List<String> lore = new ArrayList<>();
        lore.add(c("&8" + ch.getDescription()));
        lore.add(c("&8──────────────"));

        if (completed) {
            lore.add(c("&a✔ Défi complété !"));
            lore.add(c("&7Récompense perçue : &e" + ch.getReward() + " &7pièces"));
        } else {
            lore.add(c("&7Progression : &e" + Math.min(progress, target) + "&7/&e" + target));
            lore.add(c("&7" + buildProgressBar(progress, target)));
            lore.add(c("&8──────────────"));
            lore.add(c("&7Récompense : &e" + ch.getReward() + " &7pièces"));
            if (claimable) {
                lore.add(c("&a⚡ Clique pour récupérer ta récompense !"));
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String buildProgressBar(long progress, long target) {
        int filled = (int) Math.min(10, (progress * 10) / Math.max(1, target));
        StringBuilder sb = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) sb.append(i < filled ? "&a█" : "&7█");
        sb.append("&8]");
        return sb.toString();
    }

    private long countCompleted(OneBlockIsland island) {
        long count = 0;
        for (IslandChallenge ch : IslandChallenge.values()) {
            if (island.isChallengeCompleted(ch)) count++;
        }
        return count;
    }
}
