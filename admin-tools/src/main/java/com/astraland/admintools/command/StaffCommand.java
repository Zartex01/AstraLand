package com.astraland.admintools.command;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand implements CommandExecutor {

    private final AdminTools plugin;

    public StaffCommand(AdminTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ItemBuilder.c("&cCette commande est réservée aux joueurs."));
            return true;
        }

        if (!player.hasPermission("astraland.staff")) {
            player.sendMessage(ItemBuilder.c("&cTu n'as pas la permission d'utiliser cette commande."));
            return true;
        }

        plugin.getPlayerListGUI().open(player);
        return true;
    }
}
