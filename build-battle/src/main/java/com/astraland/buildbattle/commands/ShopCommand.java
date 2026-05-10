package com.astraland.buildbattle.commands;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.shop.ShopGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final BuildBattle plugin;
    public ShopCommand(BuildBattle plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cJoueurs uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        new ShopGUI().open(player);
        return true;
    }
}
