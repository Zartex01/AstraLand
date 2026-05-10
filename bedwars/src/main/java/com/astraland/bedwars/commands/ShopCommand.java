package com.astraland.bedwars.commands;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.shop.ShopMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final Bedwars plugin;
    public ShopCommand(Bedwars plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        new ShopMenuGUI(player, plugin.getEconomyManager()).open(player);
        return true;
    }
}
