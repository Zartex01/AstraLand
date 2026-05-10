package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.shop.ShopMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final PvpFactions plugin;

    public ShopCommand(PvpFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cJoueurs uniquement.");
            return true;
        }
        if (!plugin.isInPluginWorld(player)) {
            player.sendMessage(plugin.wrongWorldMsg());
            return true;
        }
        new ShopMenuGUI(player, plugin.getEconomyManager()).open(player);
        return true;
    }
}
