package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class PrestigeGUI {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ Prestige ✦";
    public static final int MAX_PRESTIGE = 10;
    private final OneBlock plugin;

    public PrestigeGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        int prestige = island.getPrestige();
        boolean canPrestige = island.getCurrentPhase() == Phase.END && island.getBlocksBroken() >= 5000 && prestige < MAX_PRESTIGE;
        boolean maxed = prestige >= MAX_PRESTIGE;

        inv.setItem(4, makeItem(Material.NETHER_STAR,
            "&d&lPrestige " + (prestige > 0 ? prestige : ""),
            "&7Prestige actuel : &d" + prestige + "&7/" + MAX_PRESTIGE,
            "&7Multiplicateur : &e×" + String.format("%.1f", 1.0 + prestige * 0.10),
            "&8──────────────",
            "&7Chaque prestige donne : &e+10% &7argent",
            "&7(Multiplicateur empilable avec les compétences)"));

        buildPrestigeBar(inv, prestige);

        if (maxed) {
            inv.setItem(13, makeItem(Material.BEACON, "&6&l✦ Prestige Maximum !",
                "&7Tu as atteint le niveau prestige maximum.",
                "&7Multiplicateur permanent : &e×" + String.format("%.1f", 1.0 + prestige * 0.10)));
        } else if (canPrestige) {
            inv.setItem(13, makeItem(Material.END_CRYSTAL, "&a&l⚡ PRESTIGE DISPONIBLE !",
                "&7Tu es en phase &5End &7avec 5 000+ blocs.",
                "&8──────────────",
                "&c⚠ Ton île sera réinitialisée (blocs, phase)",
                "&aLes upgrades et défis sont conservés !",
                "&7Nouveau multiplicateur : &e×" + String.format("%.1f", 1.0 + (prestige + 1) * 0.10),
                "&8──────────────",
                "&a&lClique pour effectuer le prestige !"));
        } else {
            String reason;
            if (prestige >= MAX_PRESTIGE) reason = "&cPrestige maximum atteint.";
            else if (island.getCurrentPhase() != Phase.END) reason = "&cNécessite la phase &5End";
            else reason = "&cNécessite 5 000 blocs cassés";

            inv.setItem(13, makeItem(Material.BARRIER, "&c&lPrestige Indisponible",
                "&7Conditions requises :",
                "&7- Phase &5End &7(actuel : " + island.getCurrentPhase().getColor() + island.getCurrentPhase().getDisplayName() + "&7)",
                "&7- 5 000 blocs cassés (actuel : " + island.getBlocksBroken() + ")",
                "&8──────────────",
                reason));
        }

        inv.setItem(22, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));
        player.openInventory(inv);
    }

    private void buildPrestigeBar(Inventory inv, int current) {
        Material[] mats = {
            Material.GRAY_WOOL, Material.WHITE_WOOL, Material.YELLOW_WOOL, Material.ORANGE_WOOL,
            Material.PINK_WOOL, Material.MAGENTA_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
            Material.CYAN_WOOL, Material.LIME_WOOL
        };
        int[] barSlots = {0,1,2,3,4,5,6,7,8};
        for (int i = 0; i < Math.min(barSlots.length, MAX_PRESTIGE); i++) {
            Material mat = i < current ? mats[i] : Material.GRAY_STAINED_GLASS_PANE;
            String name = i < current ? "&d✔ Prestige " + (i + 1) + " accompli" : "&8Prestige " + (i + 1);
            inv.setItem(barSlots[i], makeItem(mat, name));
        }
    }
}
