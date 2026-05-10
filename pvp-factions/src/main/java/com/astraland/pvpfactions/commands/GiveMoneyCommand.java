package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveMoneyCommand implements CommandExecutor {

    private final PvpFactions plugin;

    public GiveMoneyCommand(PvpFactions plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("astraland.admin")) {
            sender.sendMessage(c("&cTu n'as pas la permission d'utiliser cette commande.")); return true;
        }
        if (args.length < 2) {
            sender.sendMessage(c("&cUsage : /givemoney <joueur> <montant>")); return true;
        }
        @SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(c("&cJoueur introuvable.")); return true;
        }
        int amount;
        try { amount = Integer.parseInt(args[1]); }
        catch (NumberFormatException e) { sender.sendMessage(c("&cMontant invalide.")); return true; }
        if (amount <= 0) { sender.sendMessage(c("&cLe montant doit être positif.")); return true; }
        plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
        sender.sendMessage(c("&a✔ &6" + amount + " $ &7donné à &e" + args[0] + "&7."));
        Player online = Bukkit.getPlayer(target.getUniqueId());
        if (online != null) online.sendMessage(c("&a✔ &7Tu as reçu &6" + amount + " $ &7d'un administrateur."));
        return true;
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
