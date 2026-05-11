package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.challenges.ChallengeGUI;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.ranks.IslandRank;
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
        // Fond
        ItemStack border = glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, border);

        IslandRank rank   = IslandRank.fromLevel(island.getLevel());
        int questDone     = plugin.getQuestManager().countCompleted(player.getUniqueId());
        int questTotal    = 5;
        int defiDone      = plugin.getChallengeManager().countCompleted(island.getOwner());
        int defiTotal     = plugin.getChallengeManager().getAllChallenges().size();
        boolean border_on = plugin.getBorderTask().hasBorder(player.getUniqueId());

        // ── Tête du joueur ──
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(player);
            sm.setDisplayName(c("&e&l" + island.getName()));
            sm.setLore(List.of(
                c("&7Rang : " + rank.getFullName()),
                c("&7Niveau : &a" + island.getLevel() + "  &8| &7Valeur : &6" + fmt(island.getValue()) + " pts"),
                c("&7Quêtes : &e" + questDone + "/" + questTotal + "  &8| &7Défis : &a" + defiDone + "/" + defiTotal),
                c("&7Membres : &f" + (island.getMemberCount()+1) + "/" + (island.getMemberSlots()+1)),
                c("&7Banque : &6" + fmt(island.getBankBalance()) + " $")
            ));
            skull.setItemMeta(sm);
        }
        inv.setItem(4, skull);

        // ── Ligne 1 (slots 19-25) ──
        inv.setItem(19, btn(Material.RED_BED,        "&a🏠 Téléportation",       "&7/is home",       "&7Retourner à ton île"));
        inv.setItem(20, btn(Material.MAP,            "&7📋 Informations",        "&7/is info",       "&7Voir les stats de l'île"));
        inv.setItem(21, btn(Material.PLAYER_HEAD,    "&e👥 Membres",             "&7/is membres",    "&7Gérer les membres et rôles",
                            "&7Slots : &f" + (island.getMemberCount()+1) + "/" + (island.getMemberSlots()+1)));
        inv.setItem(22, btn(Material.NETHER_STAR,    "&6⚔ Défis",               "&7/is challenges",
                            "&7Complétés : &a" + defiDone + " &8/ &f" + defiTotal));
        inv.setItem(23, btn(Material.COAL_ORE,       "&b⚙ Générateur",          "&7/is generator",
                            "&7Niveau actuel : &b" + island.getGeneratorLevel() + " &8/ &b7"));
        inv.setItem(24, btn(Material.EXPERIENCE_BOTTLE, "&d✦ Améliorations",    "&7/is upgrades",
                            "&7Vol : " + (island.hasFlyUpgrade() ? "&a✔" : "&c✗") +
                            "  &7Keep inv : " + (island.hasKeepInventoryUpgrade() ? "&a✔" : "&c✗")));
        inv.setItem(25, btn(Material.COMPARATOR,     "&7⚙ Paramètres",          "&7/is settings",   "&7Permissions des visiteurs"));

        // ── Ligne 2 (slots 28-34) ──
        inv.setItem(28, btn(Material.SUNFLOWER,      "&e⭐ Quêtes du Jour",      "&7/is quetes",
                            "&7Complétées : &e" + questDone + " &8/ &e" + questTotal,
                            "&7Se renouvellent chaque jour à minuit"));
        inv.setItem(29, btn(Material.GOLDEN_SWORD,   "&6🏆 Classement",          "&7/is top",        "&7Top 10 des meilleures îles"));
        inv.setItem(30, btn(Material.CHEST,          "&e🏦 Banque d'île",        "&7/is bank",
                            "&7Solde : &6" + fmt(island.getBankBalance()) + " $"));
        inv.setItem(31, btn(Material.WRITABLE_BOOK,  "&a💬 Chat île",            "&7/is chat",
                            plugin.getIslandManager().isIslandChatEnabled(player.getUniqueId())
                                ? c("&a▶ Actif") : c("&7Inactif")));
        inv.setItem(32, btn(Material.ENDER_PEARL,    "&5🌀 Warps publics",       "&7/is warps",      "&7Visiter d'autres îles"));
        inv.setItem(33, btn(border_on ? Material.LIME_STAINED_GLASS_PANE : Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                            border_on ? "&a🔲 Bordure : &aActivée" : "&7🔲 Bordure : &cDésactivée",
                            "&7/is border", "&7Affiche les limites de ton île en particules"));
        inv.setItem(34, btn(island.isPvpEnabled() ? Material.IRON_SWORD : Material.WOODEN_SWORD,
                            island.isPvpEnabled() ? "&cPvP : &aActivé" : "&7PvP : &cDésactivé",
                            "&7/is pvp", "&7Activer/Désactiver le PvP"));

        // ── Rang (slot 40) ──
        IslandRank next = rank.next();
        ItemStack rankItem = new ItemStack(Material.valueOf(rank.getIconMaterial()));
        ItemMeta rm = rankItem.getItemMeta();
        if (rm != null) {
            rm.setDisplayName(c("&f✦ Rang d'île : " + rank.getFullName()));
            java.util.List<String> rl = new java.util.ArrayList<>();
            rl.add(c("&7Bonus vente : &6+" + rank.getSellBonus() + "%"));
            if (rank.getGeneratorBonus() > 0) rl.add(c("&7Bonus générateur : &b+" + rank.getGeneratorBonus()));
            if (next != null) {
                rl.add(c(""));
                rl.add(c("&7Prochain rang : " + next.getFullName() + " &8(Niv. " + next.getMinLevel() + ")"));
            } else {
                rl.add(c(""));
                rl.add(c("&5✦ Rang maximum atteint !"));
            }
            rm.setLore(rl);
            rankItem.setItemMeta(rm);
        }
        inv.setItem(40, rankItem);

        // ── Danger / Fermer ──
        inv.setItem(46, btn(Material.BARRIER,        "&c⛔ Expulser visiteurs", "&7/is expel",      "&7Chasse les visiteurs de ton île"));
        inv.setItem(47, btn(island.isLocked() ? Material.RED_CONCRETE : Material.GREEN_CONCRETE,
                            island.isLocked() ? "&cÎle : &cVerrouillée" : "&aÎle : &aOuverte",
                            island.isLocked() ? "&7/is unlock" : "&7/is lock",
                            "&7Verrouiller/déverrouiller l'île"));
        inv.setItem(49, btn(Material.TNT, "&c⚠ Supprimer l'île", "&7/is delete", "&cAction irréversible !"));
        inv.setItem(53, closeBtn());
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        switch (slot) {
            case 19 -> { player.closeInventory(); player.performCommand("is home"); }
            case 20 -> { player.closeInventory(); player.performCommand("is info"); }
            case 21 -> { new IslandMembersGUI(plugin, island, player).open(player); }
            case 22 -> { new ChallengeGUI(plugin, player, island, null, 0).open(player); }
            case 23 -> { player.closeInventory(); new IslandGeneratorGUI(island, plugin).open(player); }
            case 24 -> { player.closeInventory(); new IslandUpgradesGUI(island, plugin).open(player); }
            case 25 -> { player.closeInventory(); new IslandSettingsGUI(island, plugin).open(player); }
            case 28 -> {
                new com.astraland.skyblock.quests.DailyQuestGUI(plugin, player).open(player);
            }
            case 29 -> { player.closeInventory(); new IslandTopGUI(plugin).open(player); }
            case 30 -> { player.closeInventory(); player.performCommand("is bank"); }
            case 31 -> {
                player.closeInventory();
                boolean on = plugin.getIslandManager().toggleIslandChat(player.getUniqueId());
                player.sendMessage(c("&8[&a&lSkyblock&8] " + (on ? "&aChat île activé (🏝)" : "&7Chat île désactivé.")));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, on ? 1.2f : 0.8f);
            }
            case 32 -> { player.closeInventory(); new IslandWarpGUI(plugin).open(player); }
            case 33 -> {
                boolean now = plugin.getBorderTask().toggleBorder(player.getUniqueId());
                player.sendMessage(c("&8[&a&lSkyblock&8] &7Bordure d'île : " + (now ? "&aActivée" : "&7Désactivée")));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, now ? 1.3f : 0.7f);
                new IslandMenuGUI(plugin, player, island).open(player);
            }
            case 34 -> {
                player.closeInventory();
                island.setPvpEnabled(!island.isPvpEnabled());
                plugin.getIslandManager().saveAll();
                player.sendMessage(c("&8[&a&lSkyblock&8] &7PvP : " + (island.isPvpEnabled() ? "&cActivé" : "&aDesactivé")));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            }
            case 46 -> { player.closeInventory(); player.performCommand("is expel"); }
            case 47 -> {
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
            java.util.List<String> l = new java.util.ArrayList<>();
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

    private String fmt(long v) { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }
}
