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
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TopKillsCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public TopKillsCommand(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        StatsManager sm = plugin.getStatsManager();
        if (sender instanceof Player player && !plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        String mode = args.length > 0 ? args[0].toLowerCase() : "kills";

        if (mode.equals("streak")) {
            List<Map.Entry<UUID, Integer>> top = sm.getTopStreaks(10);
            sender.sendMessage(c("&6&l=== Top 10 Meilleures Séries ==="));
            if (top.isEmpty()) { sender.sendMessage(c("&7Aucune donnée.")); return true; }
            for (int i = 0; i < top.size(); i++) {
                UUID uuid = top.get(i).getKey();
                int streak = top.get(i).getValue();
                String name = getPlayerName(uuid);
                String medal = i == 0 ? "&6#1" : i == 1 ? "&7#2" : i == 2 ? "&c#3" : "&f#" + (i + 1);
                sender.sendMessage(c(medal + " &e" + name + " &7- &6" + streak + " kills consécutifs"));
            }
        } else {
            List<Map.Entry<UUID, Integer>> top = sm.getTopKills(10);
            sender.sendMessage(c("&6&l=== Top 10 Kills ==="));
            if (top.isEmpty()) { sender.sendMessage(c("&7Aucune donnée.")); return true; }
            for (int i = 0; i < top.size(); i++) {
                UUID uuid = top.get(i).getKey();
                int killCount = top.get(i).getValue();
                int deaths = sm.getDeaths(uuid);
                double kd = sm.getKD(uuid);
                String name = getPlayerName(uuid);
                String medal = i == 0 ? "&6#1" : i == 1 ? "&7#2" : i == 2 ? "&c#3" : "&f#" + (i + 1);
                sender.sendMessage(c(medal + " &e" + name + " &7- &a" + killCount + " &7kills &c" + deaths + " &7morts &6" + String.format("%.2f", kd) + " &7K/D"));
            }
        }
        return true;
    }

    private String getPlayerName(UUID uuid) {
        org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
        if (p != null) return p.getName();
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : uuid.toString().substring(0, 8);
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("kills", "streak");
        return List.of();
    }
}
