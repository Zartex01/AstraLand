package com.astraland.uhc.commands;

import com.astraland.uhc.UHC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyCommand implements CommandExecutor {

    private final UHC plugin;

    public EconomyCommand(UHC plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "balance" -> handleBalance(sender, args);
            case "pay"     -> handlePay(sender, args);
        }
        return true;
    }

    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) { sender.sendMessage(c("&cUtilise /balance <joueur> depuis la console.")); return; }
            if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return; }
            player.sendMessage(c("&7Ton solde : &6" + plugin.getEconomyManager().getBalance(player.getUniqueId()) + " &7pièces"));
        } else {
            @SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore()) { sender.sendMessage(c("&cJoueur introuvable.")); return; }
            sender.sendMessage(c("&7Solde de &e" + args[0] + " &7: &6" + plugin.getEconomyManager().getBalance(target.getUniqueId()) + " &7pièces"));
        }
    }

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(c("&cCette commande est réservée aux joueurs.")); return; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return; }
        if (args.length < 2) { player.sendMessage(c("&cUsage : /pay <joueur> <montant>")); return; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { player.sendMessage(c("&cJoueur introuvable ou non connecté.")); return; }
        if (target.equals(player)) { player.sendMessage(c("&cTu ne peux pas te payer toi-même.")); return; }
        int amount;
        try { amount = Integer.parseInt(args[1]); } catch (NumberFormatException e) { player.sendMessage(c("&cMontant invalide.")); return; }
        if (amount <= 0) { player.sendMessage(c("&cLe montant doit être positif.")); return; }
        if (!plugin.getEconomyManager().removeBalance(player.getUniqueId(), amount)) { player.sendMessage(c("&cSolde insuffisant.")); return; }
        plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
        player.sendMessage(c("&a✔ &7Tu as envoyé &6" + amount + " pièces &7à &e" + target.getName() + "&7."));
        target.sendMessage(c("&a✔ &7Tu as reçu &6" + amount + " pièces &7de &e" + player.getName() + "&7."));
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
