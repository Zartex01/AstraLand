package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.EconomyManager;
import com.astraland.pvpfactions.managers.SellManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class SellListener implements Listener {

    private final PvpFactions plugin;

    public SellListener(PvpFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClickInInventory(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!plugin.isInPluginWorld(player)) return;

        if (e.getClick() != ClickType.RIGHT) return;

        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;

        if (e.getInventory().getHolder() != null
                && !(e.getInventory().getHolder() instanceof Player)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        SellManager sell = plugin.getSellManager();
        EconomyManager eco = plugin.getEconomyManager();
        Material mat = item.getType();

        if (!sell.canSell(mat)) {
            player.sendMessage(c("&c✗ &7Cet item ne peut pas être vendu."));
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        int amount = item.getAmount();
        int total = sell.getTotalSellPrice(mat, amount);
        int unitPrice = sell.getSellPrice(mat);

        player.getInventory().setItem(e.getSlot(), null);
        eco.addBalance(player.getUniqueId(), total);

        String itemName = formatMaterial(mat);
        player.sendMessage(c("&a✔ &fVendu &e" + amount + "x " + itemName
                + " &apour &e" + total + " $ &7(&e" + unitPrice + " $/unité&7)"
                + " | Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
        player.playSound(player.getLocation(),
                org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.5f);
    }

    private String formatMaterial(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty())
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
