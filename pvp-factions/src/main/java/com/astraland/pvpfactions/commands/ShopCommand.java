package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.shop.ShopMenuGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public ShopCommand(PvpFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /shop reload (op uniquement)
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("astraland.admin") && !sender.isOp()) {
                sender.sendMessage(c("&cTu n'as pas la permission."));
                return true;
            }
            plugin.getShopConfigManager().reload();
            sender.sendMessage(c("&a✔ Shop rechargé ! &7Les catégories manquantes ont été ajoutées."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cJoueurs uniquement.");
            return true;
        }
        if (!plugin.isInPluginWorld(player)) {
            player.sendMessage(plugin.wrongWorldMsg());
            return true;
        }
        new ShopMenuGUI(player, plugin.getEconomyManager(), plugin.getShopConfigManager()).open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.isOp()) return List.of("reload");
        return List.of();
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
