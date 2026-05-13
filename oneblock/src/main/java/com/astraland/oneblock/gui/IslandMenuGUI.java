package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
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
        if (island == null) {
            player.sendMessage(c("&cTu n'as pas d'île. Utilise &e/ob create&c."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        fill(inv);

        OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwner());
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(owner);
        sm.setDisplayName(c("&6&l" + (owner.getName() != null ? owner.getName() : "?")));
        Phase ph = island.getCurrentPhase();
        Phase[] phases = Phase.values();
        Phase next = null;
        for (int i = 0; i < phases.length; i++) if (phases[i] == ph && i + 1 < phases.length) { next = phases[i + 1]; break; }
        long toNext = next != null ? next.getBlocksRequired() - island.getBlocksBroken() : 0;

        double totalMult = island.getPrestigeMultiplier() * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId());

        List<String> headLore = new ArrayList<>(Arrays.asList(
            c("&7Propriétaire de l'île"),
            c("&8──────────────────"),
            c("&7Blocs cassés : &e" + island.getBlocksBroken()),
            c("&7Niveau : &b" + island.getIslandLevel()),
            c("&7Phase : " + ph.getColor() + ph.getDisplayName()),
            next != null ? c("&7Prochaine : &e" + toNext + " &7blocs") : c("&a✔ Phase maximale !"),
            c("&7Prestige : &d" + island.getPrestige()),
            c("&7Multiplicateur : &e×" + String.format("%.2f", totalMult)),
            c("&7Membres : &e" + island.getAllMemberUUIDs().size()),
            c("&7Banque : &e" + island.getBankBalance() + " &7pièces")
        ));
        sm.setLore(headLore);
        head.setItemMeta(sm);
        inv.setItem(4, head);

        // Row 2 — Island management
        inv.setItem(19, makeItem(Material.OAK_DOOR, "&e&lMembres",
            "&7Gère ton équipe", "&8Clic pour ouvrir"));
        inv.setItem(21, makeItem(Material.NETHER_STAR, "&b&lAméliorations",
            "&7Améliore ton générateur et tes drops", "&8Clic pour ouvrir"));
        inv.setItem(23, makeItem(Material.BOOK, "&d&lDéfis",
            "&7Complète des missions permanentes", "&8Clic pour ouvrir"));
        inv.setItem(25, makeItem(Material.COMPASS, "&a&lWarps publics",
            "&7Visite des îles d'autres joueurs", "&8Clic pour ouvrir"));

        // Row 3 — New features
        inv.setItem(28, makeItem(Material.IRON_PICKAXE, "&3&lCompétences",
            "&7Minage, Combat, Récolte", "&8Clic pour ouvrir"));
        inv.setItem(30, makeItem(Material.CLOCK, "&e&lMissions du jour",
            "&73 missions journalières avec récompenses", "&8Clic pour ouvrir"));
        inv.setItem(32, makeItem(Material.GOLD_BLOCK, "&6&lBanque de l'île",
            "&7Économie partagée entre membres", "&8Clic pour ouvrir"));
        inv.setItem(34, makeItem(Material.END_CRYSTAL, "&d&lPrestige",
            "&7Réinitialise l'île pour un multiplicateur", "&7Prestige actuel : &d" + island.getPrestige(), "&8Clic pour ouvrir"));

        // Row 4 — Settings and Stats
        inv.setItem(37, makeItem(Material.REDSTONE, "&c&lParamètres",
            "&7PvP, visiteurs, warp, MOTD...", "&8Clic pour ouvrir"));
        inv.setItem(43, makeItem(Material.PAPER, "&7&lStatistiques",
            "&7Vue détaillée de ton île", "&8Clic pour ouvrir"));

        // Footer
        int eco = plugin.getEconomyManager().getBalance(player.getUniqueId());
        inv.setItem(46, makeItem(Material.SUNFLOWER, "&6Ton solde : &e" + eco + " &6pièces"));
        inv.setItem(49, makeItem(Material.GRASS_BLOCK, "&aRetourner à l'île",
            "&7Téléportation vers ton bloc magique", "&8Clique pour rentrer"));
        inv.setItem(52, makeItem(Material.CLOCK, ph.getColor() + "&lPhase : " + ph.getDisplayName(),
            "&7Blocs cassés : &e" + island.getBlocksBroken()));

        player.openInventory(inv);
    }

    private void fill(Inventory inv) {
        ItemStack border = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        ItemStack inner = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
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
}
