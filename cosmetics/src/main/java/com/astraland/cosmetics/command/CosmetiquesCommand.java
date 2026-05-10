package com.astraland.cosmetics.command;

import com.astraland.cosmetics.Cosmetics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmetiquesCommand implements CommandExecutor {

    private final Cosmetics plugin;

    public CosmetiquesCommand(Cosmetics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(c("&cCette commande est réservée aux joueurs."));
            return true;
        }

        if (!player.hasPermission("astraland.cosmetique")) {
            player.sendMessage(c("&cTu n'as pas la permission d'utiliser cette commande."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && player.hasPermission("astraland.staff")) {
            plugin.getCosmetiquesManager().reload();
            player.sendMessage(c("&a✔ Cosmétiques rechargés depuis cosmetiques.yml !"));
            return true;
        }

        if (plugin.getCosmetiquesManager().getListe().isEmpty()) {
            player.sendMessage(c("&cAucun cosmétique n'est configuré. Éditez &ecosmetiques.yml&c."));
            return true;
        }

        plugin.getCosmetiquesGUI().open(player, 0);
        return true;
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
