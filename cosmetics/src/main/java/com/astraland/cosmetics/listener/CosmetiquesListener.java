package com.astraland.cosmetics.listener;

import com.astraland.cosmetics.Cosmetics;
import com.astraland.cosmetics.gui.CosmetiquesGUI;
import com.astraland.cosmetics.gui.holder.CosmetiquesHolder;
import com.astraland.cosmetics.model.CosmetiqueData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CosmetiquesListener implements Listener {

    private final Cosmetics plugin;
    private final Map<UUID, Integer> pages = new HashMap<>();

    public CosmetiquesListener(Cosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CosmetiquesHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CosmetiquesHolder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        int currentPage = pages.getOrDefault(player.getUniqueId(), 0);

        if (slot == CosmetiquesGUI.getSlotPrev()) {
            if (currentPage > 0) {
                pages.put(player.getUniqueId(), currentPage - 1);
                plugin.getCosmetiquesGUI().open(player, currentPage - 1);
            }
            return;
        }

        if (slot == CosmetiquesGUI.getSlotNext()) {
            List<CosmetiqueData> tous = plugin.getCosmetiquesManager().getListe();
            int totalPages = Math.max(1, (int) Math.ceil(tous.size() / (double) CosmetiquesGUI.getSlotsParPage()));
            if (currentPage < totalPages - 1) {
                pages.put(player.getUniqueId(), currentPage + 1);
                plugin.getCosmetiquesGUI().open(player, currentPage + 1);
            }
            return;
        }

        if (slot == CosmetiquesGUI.getSlotInfo()) return;

        if (slot >= CosmetiquesGUI.getSlotsParPage()) return;

        List<CosmetiqueData> tous = plugin.getCosmetiquesManager().getListe();
        int index = currentPage * CosmetiquesGUI.getSlotsParPage() + slot;
        if (index < 0 || index >= tous.size()) return;

        CosmetiqueData cosm = tous.get(index);

        if (cosm.hasPermission() && !player.hasPermission(cosm.getPermission())) {
            player.sendMessage(c("&cTu n'as pas la permission d'utiliser &e" + cosm.getNom() + "&c."));
            player.sendMessage(c("&cPermission requise : &e" + cosm.getPermission()));
            return;
        }

        boolean executed = false;

        if (cosm.hasCommandeJoueur()) {
            String cmd = cosm.getCommandeJoueur().replace("{joueur}", player.getName());
            player.performCommand(cmd);
            executed = true;
        }

        if (cosm.hasCommandeConsole()) {
            String cmd = cosm.getCommandeConsole().replace("{joueur}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            executed = true;
        }

        if (!executed) {
            player.sendMessage(c("&d&l✦ &7Cosmétique &d" + cosm.getNom() + " &7sélectionné !"));
            player.sendMessage(c("&8(Aucune commande configurée pour ce cosmétique)"));
        } else {
            player.sendMessage(c("&d&l✦ &7Cosmétique &d" + cosm.getNom() + " &7activé !"));
            player.closeInventory();
        }
    }

    public void setPage(UUID uuid, int page) {
        pages.put(uuid, page);
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
