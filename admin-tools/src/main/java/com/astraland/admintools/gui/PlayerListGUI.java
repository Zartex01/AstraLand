package com.astraland.admintools.gui;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.gui.holder.PlayerListHolder;
import com.astraland.admintools.session.AdminSession;
import com.astraland.admintools.session.FilterMode;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerListGUI {

    private static final int PLAYER_SLOTS = 36;
    private static final int SLOT_MODE_CONTAINS    = 36;
    private static final int SLOT_MODE_STARTS_WITH = 37;
    private static final int SLOT_MODE_ENDS_WITH   = 38;
    private static final int SLOT_MODE_EXACT        = 39;
    private static final int SLOT_SET_FILTER        = 41;
    private static final int SLOT_CLEAR_FILTER      = 42;
    private static final int SLOT_PLAYER_COUNT      = 44;
    private static final int SLOT_PREV              = 45;
    private static final int SLOT_PAGE_INFO         = 49;
    private static final int SLOT_NEXT              = 53;

    private final AdminTools plugin;

    public PlayerListGUI(AdminTools plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin) {
        AdminSession session = plugin.getSession(admin);
        List<Player> players = getFilteredPlayers(session);

        int totalPages = Math.max(1, (int) Math.ceil(players.size() / (double) PLAYER_SLOTS));
        if (session.getPage() >= totalPages) session.setPage(totalPages - 1);
        int page = session.getPage();

        Inventory inv = Bukkit.createInventory(new PlayerListHolder(), 54,
                ItemBuilder.c("&8» &b&lAstraLand &8- &7Gestion Joueurs «"));

        ItemStack gray = ItemBuilder.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);

        int start = page * PLAYER_SLOTS;
        int end   = Math.min(start + PLAYER_SLOTS, players.size());
        for (int i = start; i < end; i++) {
            Player target = players.get(i);
            String world  = target.getWorld().getName();
            String mode   = target.getGameMode().name().toLowerCase();
            ItemStack skull = ItemBuilder.skull(target,
                    "&7Monde : &e" + world,
                    "&7Mode   : &e" + mode,
                    "&7Ping   : &e" + target.getPing() + "ms",
                    "",
                    "&aCliquez pour gérer ce joueur"
            );
            inv.setItem(i - start, skull);
        }

        FilterMode current = session.getFilterMode();
        for (FilterMode mode : FilterMode.values()) {
            int slot = filterSlot(mode);
            boolean active = mode == current;
            Material mat = active ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String prefix = active ? "&a&l✔ " : "&c";
            String filterInfo = session.hasFilter() ? " &7(filtre: &e" + session.getFilterText() + "&7)" : "";
            ItemStack btn = ItemBuilder.make(mat,
                    prefix + mode.getDisplayName() + filterInfo,
                    mode.getDescription(),
                    "",
                    active ? "&a&l◀ Mode actif" : "&7Cliquez pour activer"
            );
            inv.setItem(slot, btn);
        }

        String filterDisplay = session.hasFilter()
                ? "&7Filtre : &e\"" + session.getFilterText() + "\""
                : "&7Aucun filtre actif";
        inv.setItem(SLOT_SET_FILTER, ItemBuilder.make(Material.NAME_TAG,
                "&e&lDéfinir le filtre",
                "&7Cliquez pour saisir un texte",
                "&7de filtrage dans le chat",
                "",
                filterDisplay
        ));

        inv.setItem(SLOT_CLEAR_FILTER, ItemBuilder.make(Material.BARRIER,
                "&c&lEffacer le filtre",
                "&7Supprime le filtre actuel",
                session.hasFilter() ? "&7Filtre : &e\"" + session.getFilterText() + "\"" : "&7Aucun filtre actif"
        ));

        int online = Bukkit.getOnlinePlayers().size();
        inv.setItem(SLOT_PLAYER_COUNT, ItemBuilder.make(Material.PLAYER_HEAD,
                "&b&lJoueurs en ligne",
                "&7Total : &e" + online + " &7joueur(s)",
                "&7Affichés : &e" + players.size() + " &7après filtre"
        ));

        if (page > 0) {
            inv.setItem(SLOT_PREV, ItemBuilder.make(Material.ARROW,
                    "&e&l◀ Page précédente",
                    "&7Page " + page + " / " + totalPages
            ));
        } else {
            inv.setItem(SLOT_PREV, ItemBuilder.make(Material.GRAY_STAINED_GLASS_PANE, "&8Première page"));
        }

        inv.setItem(SLOT_PAGE_INFO, ItemBuilder.make(Material.BOOK,
                "&f&lPage &e" + (page + 1) + " &f/ &e" + totalPages,
                "&7" + players.size() + " joueur(s) affiché(s)",
                "&7" + online + " joueur(s) en ligne"
        ));

        if (page < totalPages - 1) {
            inv.setItem(SLOT_NEXT, ItemBuilder.make(Material.ARROW,
                    "&e&lPage suivante ▶",
                    "&7Page " + (page + 2) + " / " + totalPages
            ));
        } else {
            inv.setItem(SLOT_NEXT, ItemBuilder.make(Material.GRAY_STAINED_GLASS_PANE, "&8Dernière page"));
        }

        admin.openInventory(inv);
    }

    private List<Player> getFilteredPlayers(AdminSession session) {
        List<Player> result = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (session.getFilterMode().matches(p.getName(), session.getFilterText())) {
                result.add(p);
            }
        }
        result.sort(Comparator.comparing(Player::getName));
        return result;
    }

    private int filterSlot(FilterMode mode) {
        return switch (mode) {
            case CONTAINS    -> SLOT_MODE_CONTAINS;
            case STARTS_WITH -> SLOT_MODE_STARTS_WITH;
            case ENDS_WITH   -> SLOT_MODE_ENDS_WITH;
            case EXACT       -> SLOT_MODE_EXACT;
        };
    }

    public int getFilterSlotForMode(FilterMode mode) {
        return filterSlot(mode);
    }

    public static int getSlotSetFilter()   { return SLOT_SET_FILTER; }
    public static int getSlotClearFilter() { return SLOT_CLEAR_FILTER; }
    public static int getSlotPrev()        { return SLOT_PREV; }
    public static int getSlotNext()        { return SLOT_NEXT; }
    public static int getPlayerSlots()     { return PLAYER_SLOTS; }
}
