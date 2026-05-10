package com.astraland.cosmetics.command;

import com.astraland.cosmetics.Cosmetics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SkinCommand implements CommandExecutor, TabCompleter {

    private final Cosmetics plugin;

    public SkinCommand(Cosmetics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(c("&cCette commande est réservée aux joueurs."));
            return true;
        }

        if (!player.hasPermission("astraland.skin")) {
            player.sendMessage(c("&cTu n'as pas la permission d'utiliser cette commande."));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(c("&cUsage : &e/skin <pseudo>"));
            player.sendMessage(c("&7Exemple : &e/skin FuzeIII"));
            return true;
        }

        String pseudo = args[0];

        if (pseudo.length() < 1 || pseudo.length() > 16) {
            player.sendMessage(c("&cPseudo invalide. (1-16 caractères)"));
            return true;
        }

        if (!pseudo.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(c("&cPseudo invalide. (lettres, chiffres et _ seulement)"));
            return true;
        }

        plugin.getSkinManager().changeSkin(player, pseudo);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
