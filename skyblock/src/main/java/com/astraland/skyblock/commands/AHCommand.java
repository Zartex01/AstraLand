package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.ah.AHGui;
import com.astraland.skyblock.ah.AuctionListing;
import com.astraland.skyblock.managers.AuctionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class AHCommand implements CommandExecutor, TabCompleter {

    private final Skyblock plugin;

    public AHCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        AuctionManager am = plugin.getAuctionManager();

        if (args.length == 0) {
            new AHGui(player, am, plugin.getEconomyManager(), 0, AHGui.Mode.ALL).open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "sell" -> {
                if (args.length < 2) { player.sendMessage(c("&cUsage : /ah sell <prix>")); return true; }
                int price;
                try { price = Integer.parseInt(args[1]); } catch (NumberFormatException e) { player.sendMessage(c("&cPrix invalide.")); return true; }
                if (price <= 0)          { player.sendMessage(c("&cLe prix doit être positif.")); return true; }
                if (price > 10_000_000) { player.sendMessage(c("&cPrix maximum : &e10 000 000 $&c.")); return true; }

                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR) { player.sendMessage(c("&cTu ne tiens rien en main.")); return true; }

                if (am.getPlayerListingCount(player.getUniqueId()) >= AuctionManager.MAX_PER_PLAYER) {
                    player.sendMessage(c("&cLimite atteinte (&e" + AuctionManager.MAX_PER_PLAYER + " &cannonces max). Annule une annonce via &e/ah me&c.")); return true;
                }

                ItemStack toSell = hand.clone(); toSell.setAmount(1);
                hand.setAmount(hand.getAmount() - 1);
                am.addListing(new AuctionListing(
                    UUID.randomUUID().toString(),
                    player.getUniqueId(),
                    player.getName(),
                    toSell, price,
                    System.currentTimeMillis()
                ));
                player.sendMessage(c("&a✔ Item mis en vente pour &6" + price + " $ &a! Expire dans &e48h&a."));
            }

            case "me" -> new AHGui(player, am, plugin.getEconomyManager(), 0, AHGui.Mode.MINE).open(player);

            case "claimed" -> {
                List<ItemStack> claimed = am.getClaimedItems(player.getUniqueId());
                if (claimed.isEmpty()) { player.sendMessage(c("&7Aucun item à récupérer.")); return true; }
                player.sendMessage(c("&a✔ Récupération de &e" + claimed.size() + " &aitem(s)..."));
                for (ItemStack item : claimed) {
                    var leftover = player.getInventory().addItem(item);
                    leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
                }
                am.clearClaimedItems(player.getUniqueId());
            }

            default -> player.sendMessage(c("&cUsage : &e/ah &7| &e/ah sell <prix> &7| &e/ah me &7| &e/ah claimed"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return List.of("sell", "me", "claimed").stream()
            .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        return List.of();
    }
}
