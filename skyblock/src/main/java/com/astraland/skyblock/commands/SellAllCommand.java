package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.EconomyManager;
import com.astraland.skyblock.shop.ShopCategoryData;
import com.astraland.skyblock.shop.ShopItemData;
import com.astraland.skyblock.shop.ShopMenuGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SellAllCommand implements CommandExecutor, TabCompleter {

    private final Skyblock plugin;
    private Map<Material, Integer> priceCache;

    public SellAllCommand(Skyblock plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        if (args.length == 0 || !args[0].equalsIgnoreCase("all")) {
            player.sendMessage(c("&8[&aSkyblock&8] &7Usage : &e/sell all"));
            return true;
        }

        Map<Material, Integer> prices = getPriceMap();
        EconomyManager eco = plugin.getEconomyManager();
        ItemStack[] contents = player.getInventory().getContents();
        Map<Material, int[]> sold = new LinkedHashMap<>();
        int totalGain = 0;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            Integer ppi = prices.get(item.getType());
            if (ppi == null || ppi <= 0) continue;
            int qty = item.getAmount();
            int gain = qty * ppi;
            sold.computeIfAbsent(item.getType(), k -> new int[]{0, 0});
            sold.get(item.getType())[0] += qty;
            sold.get(item.getType())[1] += gain;
            totalGain += gain;
            player.getInventory().setItem(i, null);
        }

        if (totalGain == 0) { player.sendMessage(c("&7Aucun item vendable dans ton inventaire.")); return true; }

        eco.addBalance(player.getUniqueId(), totalGain);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.0f);

        player.sendMessage(c("&8&m                                    "));
        player.sendMessage(c("  &a&l💰 Vente automatique — résumé"));
        player.sendMessage(c("&8&m                                    "));
        for (Map.Entry<Material, int[]> e : sold.entrySet()) {
            String name = formatMat(e.getKey());
            player.sendMessage(c("  &7" + name + " &8×" + e.getValue()[0] + " &8→ &6+" + e.getValue()[1] + " $"));
        }
        player.sendMessage(c("&8&m                                    "));
        player.sendMessage(c("  &a✔ Total gagné : &6&l+" + totalGain + " $"));
        player.sendMessage(c("  &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
        player.sendMessage(c("&8&m                                    "));
        return true;
    }

    private Map<Material, Integer> getPriceMap() {
        if (priceCache != null) return priceCache;
        priceCache = new HashMap<>();
        for (ShopCategoryData cat : ShopMenuGUI.getShopCategories()) {
            for (ShopItemData item : cat.items()) {
                if (!item.isSellable() || item.reward() == null) continue;
                Material mat = item.reward().getType();
                int ppi = (int) Math.floor((double) item.sellPrice() / Math.max(1, item.reward().getAmount()));
                if (ppi <= 0) continue;
                priceCache.merge(mat, ppi, Math::max);
            }
        }
        return priceCache;
    }

    private String formatMat(Material mat) {
        String raw = mat.name().toLowerCase().replace("_", " ");
        StringBuilder sb = new StringBuilder();
        for (String w : raw.split(" "))
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        return sb.toString().trim();
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return List.of("all");
        return List.of();
    }
}
