package com.astraland.skywars.commands;

import com.astraland.skywars.Skywars;
import com.astraland.skywars.shop.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final Skywars plugin;
    public ShopCommand(Skywars plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        new ShopGUI().open(player);
        return true;
    }
}
