package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Bukkit;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class IslandSettingsGUI {

    public static final String TITLE = ChatColor.RED + "" + ChatColor.BOLD + "✦ Paramètres de l'île ✦";
    private final OneBlock plugin;

    public IslandSettingsGUI(OneBlock plugin) { this.plugin = plugin; }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));

        boolean pvp = island.isPvpEnabled();
        inv.setItem(10, makeItem(
            pvp ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD,
            pvp ? "&a&lPvP : Activé" : "&c&lPvP : Désactivé",
            "&7Clique pour " + (pvp ? "&cdésactiver" : "&aactiver"),
            "&8Les joueurs " + (pvp ? "peuvent" : "ne peuvent pas") + " se battre sur ton île"
        ));

        boolean visitors = island.isVisitorsAllowed();
        inv.setItem(12, makeItem(
            visitors ? Material.OAK_DOOR : Material.BARRIER,
            visitors ? "&a&lVisiteurs : Autorisés" : "&c&lVisiteurs : Bloqués",
            "&7Clique pour " + (visitors ? "&cinterdire" : "&aautoriser") + " les visiteurs"
        ));

        boolean warp = island.isWarpEnabled();
        String warpName = island.getWarpName().isEmpty() ? "non défini" : island.getWarpName();
        inv.setItem(14, makeItem(
            warp ? Material.ENDER_PEARL : Material.ENDER_EYE,
            warp ? "&a&lWarp public : Activé" : "&c&lWarp public : Désactivé",
            "&7Clique pour " + (warp ? "&cdésactiver" : "&aactiver"),
            "&7Nom du warp : &e" + warpName,
            "&8Utilise &e/ob setwarp <nom> &8pour le nommer"
        ));

        inv.setItem(16, makeItem(Material.NAME_TAG, "&e&lRenommer le warp",
            "&7Tape &e/ob setwarp <nom>",
            "&7pour définir le nom de ton warp public"));

        inv.setItem(22, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));

        player.openInventory(inv);
    }
}
