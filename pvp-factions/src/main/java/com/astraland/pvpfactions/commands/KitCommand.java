package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.KitManager;
import com.astraland.pvpfactions.shop.KitGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public KitCommand(PvpFactions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Joueur uniquement.");
            return true;
        }
        if (!plugin.isInPluginWorld(player)) {
            player.sendMessage(plugin.wrongWorldMsg());
            return true;
        }

        if (args.length == 0) {
            new KitGUI(player, plugin).open(player);
            return true;
        }

        KitManager km = plugin.getKitManager();
        String kitName = args[0].toUpperCase();

        if (!km.kitExists(kitName)) {
            player.sendMessage(pre() + c("&cKit introuvable. Utilise &e/kit &cpour voir la liste."));
            return true;
        }

        String perm = plugin.getConfig().getString("kits." + kitName + ".permission", "");
        if (!perm.isEmpty() && !player.hasPermission(perm)) {
            player.sendMessage(pre() + c("&cTu n'as pas la permission d'utiliser ce kit."));
            return true;
        }

        long remaining = km.getCooldownRemaining(player.getUniqueId(), kitName);
        if (remaining > 0) {
            player.sendMessage(pre() + c("&cCe kit sera disponible dans &e" + formatTime(remaining) + "&c."));
            return true;
        }

        if (km.giveKit(player, kitName)) {
            String display = c(plugin.getConfig().getString("kits." + kitName + ".display", kitName));
            player.sendMessage(pre() + c("&aKit " + display + " &aobtenu !"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.0f);
        } else {
            player.sendMessage(pre() + c("&cErreur lors de l'obtention du kit."));
        }
        return true;
    }

    private String formatTime(long seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "min";
        if (seconds >= 60) return (seconds / 60) + "min " + (seconds % 60) + "s";
        return seconds + "s";
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String typed = args[0].toLowerCase();
            return plugin.getKitManager().getKitNames().stream()
                .map(String::toLowerCase)
                .filter(k -> k.startsWith(typed))
                .collect(java.util.stream.Collectors.toList());
        }
        return List.of();
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&c&lFactions&8] &r")); }
}
