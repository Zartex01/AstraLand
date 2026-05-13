package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.PlayerStatsManager;
import com.astraland.oneblock.models.Boost;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IslandMenuGUI {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Mon Île OneBlock ✦";
    private final OneBlock plugin;

    public IslandMenuGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) { player.sendMessage(c("&cTu n'as pas d'île. Utilise &e/ob create")); return; }

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        // Head (top center)
        OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwner());
        String ownerName = owner.getName() != null ? owner.getName() : "?";
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(owner);
        sm.setDisplayName(c("&6&l" + ownerName));
        Phase ph = island.getCurrentPhase();
        Phase[] phases = Phase.values();
        Phase next = null;
        for (int i = 0; i < phases.length; i++) if (phases[i] == ph && i + 1 < phases.length) { next = phases[i + 1]; break; }
        long toNext = next != null ? next.getBlocksRequired() - island.getBlocksBroken() : 0;
        Boost activeBoost = plugin.getBoostManager().getBoost(island.getOwner());
        double totalMult = island.getPrestigeMultiplier()
            * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId())
            * plugin.getBoostManager().getMultiplier(island.getOwner());

        List<String> headLore = new ArrayList<>(Arrays.asList(
            c("&8──────────────────"),
            c("&7Blocs cassés : &e" + island.getBlocksBroken()),
            c("&7Niveau : &b" + island.getIslandLevel()),
            c("&7Valeur île : &a" + island.getIslandWorth() + " ✦"),
            c("&7Phase : " + ph.getColor() + ph.getDisplayName()),
            next != null ? c("&7Prochain : &e" + toNext + " blocs") : c("&a✔ Phase max !"),
            c("&7Prestige : &d" + island.getPrestige()),
            c("&7Multiplicateur : &e×" + String.format("%.2f", totalMult)),
            activeBoost != null ? c("&c⚡ Boost actif : &e" + activeBoost.formatRemaining()) : c("&8Aucun boost"),
            c("&7Membres : &e" + island.getAllMemberUUIDs().size()),
            c("&7Banque : &e" + island.getBankBalance() + " pièces")));
        sm.setLore(headLore);
        head.setItemMeta(sm);
        inv.setItem(4, head);

        // Row 2 — Base Island management
        inv.setItem(10, makeItem(Material.OAK_DOOR, "&e&lMembres", "&7Gère ton équipe"));
        inv.setItem(12, makeItem(Material.NETHER_STAR, "&b&lAméliorations", "&7Booste ton générateur & drops"));
        inv.setItem(14, makeItem(Material.BOOK, "&d&lDéfis", "&7Missions permanentes avec récompenses"));
        inv.setItem(16, makeItem(Material.COMPASS, "&a&lWarps publics", "&7Visite des îles d'autres joueurs"));

        // Row 3 — Progression features
        inv.setItem(19, makeItem(Material.IRON_PICKAXE, "&3&lCompétences", "&7Minage, Combat, Récolte (50 niveaux)"));
        inv.setItem(21, makeItem(Material.CLOCK, "&e&lMissions du jour", "&75 missions journalières renouvelables"));
        inv.setItem(23, makeItem(Material.DIAMOND_SWORD, "&d&lMissions hebdo", "&73 missions difficiles par semaine"));
        inv.setItem(25, makeItem(Material.CHEST, "&b&lCollections", "&720 items × 5 paliers de récompenses"));

        // Row 4 — Economy & Power
        inv.setItem(28, makeItem(Material.GOLD_BLOCK, "&6&lBanque de l'île", "&7Économie partagée entre membres"));
        inv.setItem(30, makeItem(Material.END_CRYSTAL, "&d&lPrestige",
            "&7Phase End + 5 000 blocs → +10% permanent",
            "&7Prestige actuel : &d" + island.getPrestige()));
        inv.setItem(32, makeItem(Material.BEACON, "&c&lBoosts",
            activeBoost != null ? "&c⚡ Boost actif : " + activeBoost.formatRemaining() : "&7Aucun boost actif",
            "&7×1.5/×2/×2.5/×3 temporaires"));
        inv.setItem(34, makeItem(Material.NETHER_STAR, "&6&lSuccès", "&725 succès permanents avec récompenses",
            "&7Débloqués : &e" + plugin.getAchievementManager().countUnlocked(player.getUniqueId()) + "&7/" + com.astraland.oneblock.models.OBAchievement.values().length));

        // Row 5 — Utility
        inv.setItem(37, makeItem(Material.ENDER_CHEST, "&5&lCoffre d'Île", "&7Stockage partagé entre tous les membres"));
        inv.setItem(39, makeItem(Material.GRASS_BLOCK, "&a&lStats de l'Île", "&7Vue détaillée de l'île"));
        inv.setItem(41, makeItem(Material.EXPERIENCE_BOTTLE, "&7&lMes Stats", "&7Tes statistiques personnelles"));
        inv.setItem(43, makeItem(Material.REDSTONE, "&c&lParamètres", "&7PvP, visiteurs, warp, MOTD..."));

        // Footer
        inv.setItem(46, makeItem(Material.SUNFLOWER, "&6Solde : &e" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + " &6pièces"));
        inv.setItem(49, makeItem(Material.GRASS_BLOCK, "&aRetourner à l'île", "&7Clique pour rentrer"));
        inv.setItem(52, makeItem(ph.getColor() + "&lPhase : " + ph.getDisplayName(),
            Material.CLOCK, "&7Blocs : &e" + island.getBlocksBroken()));

        player.openInventory(inv);
    }

    private void fill(Inventory inv) {
        ItemStack border = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack inner  = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            int row = i / 9, col = i % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) inv.setItem(i, border);
            else inv.setItem(i, inner);
        }
    }

    public static ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore.length > 0) {
            List<String> l = new ArrayList<>();
            for (String s : lore) l.add(ChatColor.translateAlternateColorCodes('&', s));
            meta.setLore(l);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(String name, Material mat, String... lore) {
        return makeItem(mat, name, lore);
    }

}
