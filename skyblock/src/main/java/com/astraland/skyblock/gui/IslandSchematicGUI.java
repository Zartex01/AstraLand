package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class IslandSchematicGUI implements InventoryHolder {

    public enum Schematic {
        CLASSIQUE("Île Classique",    Material.GRASS_BLOCK,   "&aGrasse et verdoyante",        "&7Île de départ standard",         "&7Un chêne, un coffre de départ,",  "&7une source de lave et d'eau."),
        JUNGLE   ("Île Jungle",       Material.JUNGLE_LOG,    "&2Dense et tropicale",           "&7Végétation luxuriante",           "&7Bambous, lianes et un grand arbre",  "&7de jungle. Ressources en bois++."),
        DESERT   ("Île Désert",       Material.SANDSTONE,     "&6Aride et dorée",               "&7Sable, grès et cactus",           "&7Un puits artésien, des cactus",   "&7et du sable en abondance."),
        IGLOO    ("Île Igloo",        Material.PACKED_ICE,    "&bFroide et cristalline",        "&7Glace, neige et sapins",          "&7Un igloo, de la glace bleue",     "&7et de la neige. Ambiance polaire.");

        public final String displayName;
        public final Material icon;
        public final String subtitle;
        public final String[] desc;

        Schematic(String displayName, Material icon, String subtitle, String... desc) {
            this.displayName = displayName;
            this.icon        = icon;
            this.subtitle    = subtitle;
            this.desc        = desc;
        }
    }

    private final Skyblock plugin;
    private final Inventory inv;
    private static final int[] SLOTS = {20, 22, 24, 31};

    public IslandSchematicGUI(Skyblock plugin) {
        this.plugin = plugin;
        this.inv    = Bukkit.createInventory(this, 54, c("&8🌍 &a&lChoisir ton île"));
        build();
    }

    private void build() {
        ItemStack border = glass(Material.GREEN_STAINED_GLASS_PANE);
        ItemStack dark   = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, dark);
        for (int i = 0; i < 9; i++)  inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        // Header
        inv.setItem(4, header());

        Schematic[] types = Schematic.values();
        for (int i = 0; i < types.length && i < SLOTS.length; i++) {
            inv.setItem(SLOTS[i], buildBtn(types[i]));
        }

        inv.setItem(49, closeBtn());
    }

    private ItemStack buildBtn(Schematic s) {
        ItemStack it = new ItemStack(s.icon);
        ItemMeta  m  = it.getItemMeta();
        if (m == null) return it;
        m.setDisplayName(c("&f&l" + s.displayName));
        List<String> lore = new java.util.ArrayList<>();
        lore.add(c(s.subtitle));
        lore.add(c(""));
        for (String d : s.desc) lore.add(c(d));
        lore.add(c(""));
        lore.add(c("&e▶ Cliquer pour choisir cette île"));
        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == 49) { player.closeInventory(); return; }

        Schematic[] types = Schematic.values();
        for (int i = 0; i < types.length; i++) {
            if (SLOTS[i] == slot) {
                Schematic chosen = types[i];
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                player.sendMessage(c("&8[&a&lSkyblock&8] &7Génération de l'île &a" + chosen.displayName + " &7en cours..."));

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    var isl = plugin.getIslandManager().createIsland(player.getUniqueId(), chosen);
                    if (isl == null) {
                        player.sendMessage(c("&cErreur : monde introuvable."));
                        return;
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.teleport(isl.getHome());
                        player.sendMessage(c(plugin.getConfig().getString("messages.island-created", "&aÎle créée !")));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                    }, 5L);
                }, 1L);
                return;
            }
        }
    }

    private ItemStack header() {
        ItemStack it = new ItemStack(Material.COMPASS);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c("&a&l✦ Choisis ton île de départ"));
            m.setLore(List.of(
                c("&7Chaque île a ses propres ressources"),
                c("&7et son propre style de jeu."),
                c(""),
                c("&c⚠ Ce choix est définitif !")
            ));
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack closeBtn() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(c("&cAnnuler")); it.setItemMeta(m); }
        return it;
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
