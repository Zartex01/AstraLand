package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.ah.AHGui;
import com.astraland.pvpfactions.ah.AHSellGUI;
import com.astraland.pvpfactions.managers.AuctionManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AHCommand implements CommandExecutor {

    private final PvpFactions plugin;

    public AHCommand(PvpFactions plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("\u00a7cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        AuctionManager am = plugin.getAuctionManager();

        if (args.length > 0 && args[0].equalsIgnoreCase("sell")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                player.sendMessage("\u00a7cTu ne tiens rien dans ta main principale."); return true;
            }
            if (AHSellGUI.SESSIONS.containsKey(player.getUniqueId())) {
                player.sendMessage("\u00a7cTu as déjà une mise en vente en cours."); return true;
            }
            if (am.getPlayerListingCount(player.getUniqueId()) >= AuctionManager.MAX_PER_PLAYER) {
                player.sendMessage("\u00a7cTu as atteint la limite de \u00a7e" + AuctionManager.MAX_PER_PLAYER + " \u00a7cannonces."); return true;
            }
            ItemStack toSell = hand.clone(); toSell.setAmount(1);
            hand.setAmount(hand.getAmount() - 1);
            AHSellGUI.SellSession session = new AHSellGUI.SellSession(toSell);
            AHSellGUI.SESSIONS.put(player.getUniqueId(), session);
            new AHSellGUI(player, session, am, plugin.getEconomyManager()).open(player);
            return true;
        }

        new AHGui(player, am, plugin.getEconomyManager(), 0).open(player);
        return true;
    }
}
