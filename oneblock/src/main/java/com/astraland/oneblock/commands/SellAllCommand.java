package com.astraland.oneblock.commands;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.ShopPriceManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellAllCommand implements CommandExecutor {

    private final OneBlock plugin;

    public SellAllCommand(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        int totalEarned = 0;
        int itemsSold = 0;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) continue;
            int price = ShopPriceManager.getSellPrice(item.getType());
            if (price <= 0) continue;
            int earned = price * item.getAmount();
            totalEarned += earned;
            itemsSold += item.getAmount();
            player.getInventory().setItem(i, null);
        }

        if (itemsSold == 0) {
            player.sendMessage(c("&cAucun item vendable dans ton inventaire."));
            return true;
        }

        plugin.getEconomyManager().addBalance(player.getUniqueId(), totalEarned);
        plugin.getPlayerStatsManager().add(player.getUniqueId(),
            com.astraland.oneblock.managers.PlayerStatsManager.Stat.COINS_EARNED, totalEarned);
        plugin.getDailyMissionManager().addProgress(player.getUniqueId(),
            com.astraland.oneblock.models.DailyMission.MissionType.EARN_COINS, totalEarned);
        plugin.getWeeklyMissionManager().addProgress(player.getUniqueId(),
            com.astraland.oneblock.models.WeeklyMission.MissionType.EARN_COINS, totalEarned);

        player.sendMessage(c("&8&m────────────────────────"));
        player.sendMessage(c("&6&l✦ Vente Rapide &8&l✦"));
        player.sendMessage(c("&7Items vendus : &e" + itemsSold));
        player.sendMessage(c("&7Total : &e+" + totalEarned + " &7pièces"));
        player.sendMessage(c("&7Nouveau solde : &e" + plugin.getEconomyManager().getBalance(player.getUniqueId())));
        player.sendMessage(c("&8&m────────────────────────"));
        return true;
    }
}
