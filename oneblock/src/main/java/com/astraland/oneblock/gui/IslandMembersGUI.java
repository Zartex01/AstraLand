package com.astraland.oneblock.gui;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.astraland.oneblock.gui.IslandMenuGUI.makeItem;

public class IslandMembersGUI {

    public static final String TITLE = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "✦ Membres de l'île ✦";
    private final OneBlock plugin;

    public IslandMembersGUI(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player player) {
        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        ItemStack glass = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);

        int slot = 0;
        OfflinePlayer ownerOp = Bukkit.getOfflinePlayer(island.getOwner());
        ItemStack ownerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta ownerMeta = (SkullMeta) ownerHead.getItemMeta();
        ownerMeta.setOwningPlayer(ownerOp);
        ownerMeta.setDisplayName(c("&6&l" + (ownerOp.getName() != null ? ownerOp.getName() : "?")));
        ownerMeta.setLore(Arrays.asList(c("&6★ Propriétaire"), c("&8Clic droit : rien")));
        ownerHead.setItemMeta(ownerMeta);
        inv.setItem(slot++, ownerHead);

        for (UUID memberUuid : island.getMembers()) {
            OfflinePlayer memberOp = Bukkit.getOfflinePlayer(memberUuid);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(memberOp);
            meta.setDisplayName(c("&a" + (memberOp.getName() != null ? memberOp.getName() : "?")));
            List<String> lore = new ArrayList<>();
            lore.add(c("&7Membre de l'île"));
            if (island.isOwner(player.getUniqueId())) {
                lore.add(c("&8──────────────"));
                lore.add(c("&cClic gauche : Expulser"));
            }
            meta.setLore(lore);
            head.setItemMeta(meta);
            if (slot < 45) inv.setItem(slot++, head);
        }

        inv.setItem(45, makeItem(Material.ARROW, "&7← Retour", "&8Clic pour revenir au menu"));

        if (island.isOwner(player.getUniqueId())) {
            inv.setItem(49, makeItem(Material.EMERALD, "&a&lInviter un joueur",
                "&7Utilise &e/ob invite <joueur>", "&7pour inviter quelqu'un"));
            inv.setItem(53, makeItem(Material.BARRIER, "&c&lQuitter l'île",
                "&cTu ne peux pas quitter ton île car tu en es propriétaire"));
        } else {
            inv.setItem(49, makeItem(Material.BARRIER, "&c&lQuitter l'île",
                "&7Clique pour quitter cette île", "&c⚠ Cette action est irréversible"));
        }

        player.openInventory(inv);
    }
}
