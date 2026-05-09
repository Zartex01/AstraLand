package com.astraland.startup.command;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {

    private final AstraLandStartup plugin;

    public LobbyCommand(AstraLandStartup plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(c("&cCette commande est réservée aux joueurs."));
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "setlobby" -> handleSetLobby(player);
            case "lobby"    -> handleLobby(player);
        }

        return true;
    }

    private void handleSetLobby(Player player) {
        if (!player.hasPermission("astraland.setlobby")) {
            player.sendMessage(c("&cTu n'as pas la permission d'utiliser cette commande."));
            return;
        }

        Location loc = player.getLocation();
        plugin.getLobbyManager().setLobby(loc);

        player.sendMessage(c("&a✔ &7Le spawn du lobby a été défini à &e"
            + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ())
            + " &7dans &e" + loc.getWorld().getName() + "&7."));
    }

    private void handleLobby(Player player) {
        if (!plugin.getLobbyManager().hasLobby()) {
            player.sendMessage(c("&cAucun spawn de lobby n'a été défini. Utilise &e/setlobby&c d'abord."));
            return;
        }

        Location dest = plugin.getLobbyManager().getLobby();
        player.teleport(dest);
        player.sendMessage(c("&a✔ &7Téléportation au &6lobby &7!"));
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
