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

public class KillStreakCommand implements CommandExecutor {

    private final PvpFactions plugin;

    public KillStreakCommand(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

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
                target = op.getUniqueId();
                name = op.getName() != null ? op.getName() : args[0];
            }
        } else if (sender instanceof Player player) {
            target = player.getUniqueId(); name = player.getName();
        } else { sender.sendMessage("Usage: /killstreak <joueur>"); return true; }

        int current = sm.getCurrentStreak(target);
        int best = sm.getBestStreak(target);

        sender.sendMessage(c("&6=== Série de &e" + name + " &6==="));
        sender.sendMessage(c("&7Série actuelle : " + getStreakColor(current) + current + " kill(s)"));
        sender.sendMessage(c("&7Meilleure série : &6" + best + " kill(s)"));
        return true;
    }

    private String getStreakColor(int streak) {
        if (streak >= 20) return "&4&l";
        if (streak >= 10) return "&c&l";
        if (streak >= 5) return "&6";
        if (streak >= 3) return "&e";
        return "&f";
    }
}
