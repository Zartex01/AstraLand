package com.astraland.buildbattle.commands;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.ah.AHGui;
import com.astraland.buildbattle.ah.AuctionListing;
import com.astraland.buildbattle.managers.AuctionManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AHCommand implements CommandExecutor {

    private final BuildBattle plugin;

    public AHCommand(BuildBattle plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        AuctionManager am = plugin.getAuctionManager();

        if (args.length == 0) {
            new AHGui(player, am, plugin.getEconomyManager(), 0).open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            if (args.length < 2) { player.sendMessage("\u00a7cUsage: /ah sell <prix>"); return true; }
            int price;
            try { price = Integer.parseInt(args[1]); } catch (NumberFormatException e) { player.sendMessage("\u00a7cPrix invalide."); return true; }
            if (price <= 0) { player.sendMessage("\u00a7cLe prix doit \u00eatre positif."); return true; }
            if (price > 1000000) { player.sendMessage("\u00a7cPrix maximum : 1 000 000 $."); return true; }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) { player.sendMessage("\u00a7cTu ne tiens rien en main."); return true; }
            if (am.getPlayerListingCount(player.getUniqueId()) >= AuctionManager.MAX_PER_PLAYER) {
                player.sendMessage("\u00a7cTu as atteint la limite de \u00a7e" + AuctionManager.MAX_PER_PLAYER + " \u00a7cannonces."); return true;
            }
            ItemStack toSell = hand.clone(); toSell.setAmount(1);
            hand.setAmount(hand.getAmount() - 1);
            am.addListing(new AuctionListing(UUID.randomUUID().toString(), player.getUniqueId(), player.getName(), toSell, price, System.currentTimeMillis()));
            player.sendMessage("\u00a7a\u2714 Item mis en vente pour \u00a76" + price + " $ !");
            return true;
        }

        player.sendMessage("\u00a7cUsage: /ah | /ah sell <prix>");
        return true;
    }
}
