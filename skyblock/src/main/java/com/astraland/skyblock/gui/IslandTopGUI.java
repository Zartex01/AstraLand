package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.ranks.IslandRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IslandTopGUI implements InventoryHolder {

    private static final int[] SLOTS = {11,12,13,14,15, 29,30,31,32,33};

    private final Inventory inv;
    private final Skyblock  plugin;

    public IslandTopGUI(Skyblock plugin) {
        this.plugin = plugin;
        this.inv    = Bukkit.createInventory(this, 54, c("&6&l🏆 Top Îles Skyblock"));
        build();
    }

    private void build() {
        // Fond
        ItemStack gold = glass(Material.YELLOW_STAINED_GLASS_PANE);
        ItemStack dark = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, dark);
        for (int i = 0; i < 9; i++)  inv.setItem(i, gold);
        for (int i = 45; i < 54; i++) inv.setItem(i, gold);

        // Header
        inv.setItem(4, header());

        List<Island> top = plugin.getIslandManager().getTopIslands(10);
        String[] medals = {"&6✦ #1","&7✦ #2","&c✦ #3","&8#4","&8#5","&8#6","&8#7","&8#8","&8#9","&8#10"};
        Material[] rankMats = {
            Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.COPPER_BLOCK,
            Material.STONE, Material.STONE, Material.STONE, Material.STONE, Material.STONE, Material.STONE, Material.STONE
        };

        for (int i = 0; i < top.size() && i < SLOTS.length; i++) {
            Island isl = top.get(i);
            OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
            IslandRank rank     = IslandRank.fromLevel(isl.getLevel());

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm    = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(owner);
                sm.setDisplayName(c(medals[i] + " &f" + (owner.getName() != null ? owner.getName() : "?")));
                List<String> lore = new ArrayList<>();
                lore.add(c("&7Île : &f" + isl.getName()));
                lore.add(c("&7Rang : " + rank.getFullName()));
                lore.add(c("&7Niveau : &a" + isl.getLevel() + "  &7Valeur : &6" + fmt(isl.getValue()) + " pts"));
                lore.add(c("&7Membres : &f" + (isl.getMemberCount() + 1)));
                lore.add(c("&7Générateur : &b" + isl.getGeneratorLevel() + "/7"));
                if (isl.isWarpEnabled() && !isl.isLocked())
                    lore.add(c(""));
                lore.add(c("&8ID : #" + (i + 1)));
                sm.setLore(lore);
                skull.setItemMeta(sm);
            }
            inv.setItem(SLOTS[i], skull);
        }

        // Fermer
        inv.setItem(49, closeBtn());
    }

    private ItemStack header() {
        ItemStack it = new ItemStack(Material.NETHER_STAR);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c("&6&l🏆 Classement des Îles"));
            m.setLore(List.of(
                c("&7Top 10 des meilleures îles du serveur"),
                c("&7Classé par valeur de blocs posés"),
                c(""),
                c("&8Mis à jour en temps réel")
            ));
            it.setItemMeta(m);
        }
        return it;
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        if (e.getRawSlot() == 49) player.closeInventory();
    }

    private ItemStack closeBtn() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(c("&cFermer")); it.setItemMeta(m); }
        return it;
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String fmt(long v)  { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String c(String s)  { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
