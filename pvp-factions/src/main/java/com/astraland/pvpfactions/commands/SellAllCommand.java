package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.EconomyManager;
import com.astraland.pvpfactions.shop.ShopConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SellAllCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public SellAllCommand(PvpFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cJoueurs uniquement.");
            return true;
        }
        if (!plugin.isInPluginWorld(player)) {
            player.sendMessage(plugin.wrongWorldMsg());
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("all")) {
            player.sendMessage(c("&8[&6Shop&8] &7Usage : &e/sell all &7— vendre tous les items vendables"));
            return true;
        }

        // Construire la map Material → prix à l'item depuis le shop
        ShopConfigManager shopCfg = plugin.getShopConfigManager();
        Map<Material, Integer> sellPrices = shopCfg.getSellPriceMap();

        if (sellPrices.isEmpty()) {
            player.sendMessage(c("&c✗ Aucun item vendable configuré dans le shop."));
            return true;
        }

        EconomyManager eco = plugin.getEconomyManager();
        ItemStack[] contents = player.getInventory().getContents();

        // Map : item material → quantité vendue (pour le résumé)
        Map<Material, int[]> sold = new LinkedHashMap<>(); // [0]=quantité, [1]=gain

        int totalGain = 0;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;

            Integer pricePerItem = sellPrices.get(item.getType());
            if (pricePerItem == null || pricePerItem <= 0) continue;

            int qty   = item.getAmount();
            int gain  = qty * pricePerItem;

            sold.computeIfAbsent(item.getType(), k -> new int[]{0, 0});
            sold.get(item.getType())[0] += qty;
            sold.get(item.getType())[1] += gain;

            totalGain += gain;
            player.getInventory().setItem(i, null);
        }

        if (totalGain == 0) {
            player.sendMessage(c("&7Aucun item vendable dans ton inventaire."));
            return true;
        }

        eco.addBalance(player.getUniqueId(), totalGain);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f);

        // Résumé dans le chat
        player.sendMessage(c("&8&m                                    "));
        player.sendMessage(c("  &6&l💰 Vente automatique — résumé"));
        player.sendMessage(c("&8&m                                    "));

        for (Map.Entry<Material, int[]> entry : sold.entrySet()) {
            String matName = formatMat(entry.getKey());
            int qty  = entry.getValue()[0];
            int gain = entry.getValue()[1];
            player.sendMessage(c("  &7" + matName + " &8×" + qty + " &8→ &6+" + gain + " $"));
        }

        player.sendMessage(c("&8&m                                    "));
        player.sendMessage(c("  &a✔ Total gagné : &6&l+" + totalGain + " $"));
        player.sendMessage(c("  &7Nouveau solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
        player.sendMessage(c("&8&m                                    "));

        return true;
    }

    private String formatMat(Material mat) {
        String raw = mat.name().toLowerCase().replace("_", " ");
        String[] words = raw.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return List.of("all");
        return List.of();
    }
}
