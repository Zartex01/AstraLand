package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.challenges.ChallengeGUI;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class IslandMenuGUI implements InventoryHolder {

    private final Skyblock plugin;
    private final Player   player;
    private final Island   island;
    private final Inventory inv;

    public IslandMenuGUI(Skyblock plugin, Player player, Island island) {
        this.plugin  = plugin;
        this.player  = player;
        this.island  = island;
        this.inv     = Bukkit.createInventory(this, 54, c("&8🏝 &a&lMon Île"));
        build();
    }

    private void build() {
        // Bordure
        ItemStack border = glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        // ── Tête du joueur (info île) ──
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(player);
            sm.setDisplayName(c("&e&l" + island.getName()));
            int completed = plugin.getChallengeManager().countCompleted(island.getOwner());
            int total = plugin.getChallengeManager().getAllChallenges().size();
            sm.setLore(List.of(
                c("&7Propriétaire : &f" + ownerName()),
                c("&7Niveau : &a" + island.getLevel() + "  &7Valeur : &6" + fmt(island.getValue()) + " pts"),
                c("&7Membres : &f" + (island.getMemberCount() + 1) + " &8/ &f" + (island.getMemberSlots() + 1)),
                c("&7Défis : &a" + completed + " &8/ &f" + total),
                c("&7Banque d'île : &6" + fmt(island.getBankBalance()) + " $")
            ));
            skull.setItemMeta(sm);
        }
        inv.setItem(4, skull);

        // ── Boutons principaux ──
        // Home
        inv.setItem(19, btn(Material.BED,           "&a🏠 Téléportation",      "&7/is home",      "&7Retourner à ton île"));
        // Info
        inv.setItem(20, btn(Material.MAP,            "&7📋 Informations",       "&7/is info",      "&7Voir les stats de l'île"));
        // Membres
        inv.setItem(21, btn(Material.PLAYER_HEAD,    "&e👥 Membres",            "&7/is invite",    "&7Gérer les membres de l'île"));
        // Défis
        inv.setItem(22, btn(Material.NETHER_STAR,    "&6⚔ Défis",              "&7/is challenges","&7Voir et réclamer les défis",
                            "&7Complétés : &a" + plugin.getChallengeManager().countCompleted(island.getOwner())
                            + " &8/ &f" + plugin.getChallengeManager().getAllChallenges().size()));
        // Générateur
        inv.setItem(23, btn(Material.COAL_ORE,       "&b⚙ Générateur",         "&7/is generator",
                            "&7Niveau actuel : &b" + island.getGeneratorLevel() + " &8/ &b7",
                            "&7Améliore ton générateur de cobblestone"));
        // Upgrades
        inv.setItem(24, btn(Material.EXPERIENCE_BOTTLE,"&d✦ Améliorations",    "&7/is upgrades",
                            "&7Vol sur l'île, inventaire sauvegardé...",
                            "&7" + (island.hasFlyUpgrade() ? "&a✔ Vol activé" : "&7Vol : non acheté"),
                            "&7" + (island.hasKeepInventoryUpgrade() ? "&a✔ Keep inventory activé" : "&7Keep inv : non acheté")));
        // Paramètres
        inv.setItem(25, btn(Material.COMPARATOR,     "&7⚙ Paramètres",         "&7/is settings",  "&7Permissions des visiteurs"));

        // ── 2ème rangée ──
        // Warps
        inv.setItem(28, btn(Material.ENDER_PEARL,    "&5🌀 Warps publics",      "&7/is warps",     "&7Visiter d'autres îles"));
        // Classement
        inv.setItem(29, btn(Material.GOLDEN_SWORD,   "&6🏆 Classement",         "&7/is top",       "&7Top 10 des meilleures îles"));
        // Banque
        inv.setItem(30, btn(Material.CHEST,          "&e🏦 Banque d'île",       "&7/is bank",
                            "&7Solde : &6" + fmt(island.getBankBalance()) + " $",
                            "&7Solde partagé entre les membres"));
        // Chat île
        inv.setItem(31, btn(Material.WRITABLE_BOOK,  "&a💬 Chat île",           "&7/is chat",      "&7Canal privé de l'île",
                            plugin.getIslandManager().isIslandChatEnabled(player.getUniqueId())
                                ? c("&a▶ Actif") : c("&7Inactif")));
        // Expulser visiteurs
        inv.setItem(32, btn(Material.BARRIER,        "&c⛔ Expulser visiteurs", "&7/is expel",     "&7Chasse les visiteurs de ton île"));
        // PvP / Verrou
        inv.setItem(33, btn(island.isPvpEnabled() ? Material.IRON_SWORD : Material.WOODEN_SWORD,
                            island.isPvpEnabled() ? "&cPvP : &aActivé" : "&7PvP : &cDésactivé",
                            "&7/is pvp",       "&7Activer/Désactiver le PvP"));
        inv.setItem(34, btn(island.isLocked() ? Material.RED_CONCRETE : Material.GREEN_CONCRETE,
                            island.isLocked() ? "&cÎle : &cVerrouillée" : "&aÎle : &aOuverte",
                            island.isLocked() ? "&7/is unlock" : "&7/is lock",
                            "&7Verrouiller/déverrouiller l'île"));

        // ── Danger zone ──
        inv.setItem(49, btn(Material.TNT,            "&c⚠ Supprimer l'île",    "&7/is delete",    "&cAction irréversible !",
                            "&7Tape &e/is delete &7pour confirmer"));
        // Fermer
        inv.setItem(53, closeBtn());
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            case 19 -> { player.closeInventory(); player.performCommand("is home"); }
            case 20 -> { player.closeInventory(); player.performCommand("is info"); }
            case 21 -> { player.closeInventory(); player.sendMessage(c("&8[&a&lSkyblock&8] &7Utilise &e/is invite <joueur>&7, &e/is kick <joueur>&7, &e/is coop <joueur>")); }
            case 22 -> {
                new ChallengeGUI(plugin, player, island, null, 0).open(player);
            }
            case 23 -> {
                player.closeInventory();
                new IslandGeneratorGUI(island, plugin).open(player);
            }
            case 24 -> {
                player.closeInventory();
                new IslandUpgradesGUI(island, plugin).open(player);
            }
            case 25 -> {
                player.closeInventory();
                new IslandSettingsGUI(island, plugin).open(player);
            }
            case 28 -> { player.closeInventory(); new IslandWarpGUI(plugin).open(player); }
            case 29 -> { player.closeInventory(); player.performCommand("is top"); }
            case 30 -> { player.closeInventory(); player.performCommand("is bank"); }
            case 31 -> {
                player.closeInventory();
                boolean on = plugin.getIslandManager().toggleIslandChat(player.getUniqueId());
                player.sendMessage(c("&8[&a&lSkyblock&8] " + (on ? "&aChat île activé (🏝)" : "&7Chat île désactivé.")));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, on ? 1.2f : 0.8f);
            }
            case 32 -> { player.closeInventory(); player.performCommand("is expel"); }
            case 33 -> {
                player.closeInventory();
                island.setPvpEnabled(!island.isPvpEnabled());
                plugin.getIslandManager().saveAll();
                player.sendMessage(c("&8[&a&lSkyblock&8] &7PvP : " + (island.isPvpEnabled() ? "&cActivé" : "&aDesactivé")));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            }
            case 34 -> {
                player.closeInventory();
                island.setLocked(!island.isLocked());
                plugin.getIslandManager().saveAll();
                player.sendMessage(c("&8[&a&lSkyblock&8] &7Île " + (island.isLocked() ? "&cVerrouillée" : "&aOuverte") + "&7."));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            }
            case 49 -> { player.closeInventory(); player.performCommand("is delete"); }
            case 53 -> player.closeInventory();
        }
    }

    private ItemStack btn(Material mat, String name, String... lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c(name));
            List<String> l = new java.util.ArrayList<>();
            for (String s : lore) l.add(c(s));
            m.setLore(l);
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack closeBtn() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(c("&cFermer")); it.setItemMeta(m); }
        return it;
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String ownerName() {
        org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(island.getOwner());
        return op.getName() != null ? op.getName() : "?";
    }

    private String fmt(long v) { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }
}
