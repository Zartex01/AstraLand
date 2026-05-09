package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KillsCommand implements CommandExecutor {

    private final PvpFactions plugin;

    public KillsCommand(PvpFactions plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StatsManager sm = plugin.getStatsManager();

        UUID target;
        String name;

        if (args.length > 0) {
            Player p = Bukkit.getPlayerExact(args[0]);
            if (p != null) { target = p.getUniqueId(); name = p.getName(); }
            else {
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
                target = op.getUniqueId(); name = op.getName() != null ? op.getName() : args[0];
            }
        } else if (sender instanceof Player player) {
            target = player.getUniqueId(); name = player.getName();
        } else { sender.sendMessage("Usage: /kills <joueur>"); return true; }

        int kills = sm.getKills(target);
        int deaths = sm.getDeaths(target);
        double kd = sm.getKD(target);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&6=== Stats de &e" + name + " &6===\n" +
            "&7Kills : &a" + kills + "\n" +
            "&7Morts : &c" + deaths + "\n" +
            "&7K/D : &e" + String.format("%.2f", kd)));
        return true;
    }
}
