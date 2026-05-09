package com.astraland.spleef.commands;

import com.astraland.spleef.Spleef;
import com.astraland.spleef.managers.SpleefManager;
import com.astraland.spleef.models.SpleefGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SpleefCommand implements CommandExecutor, TabCompleter {

    private final Spleef plugin;
    public SpleefCommand(Spleef plugin) { this.plugin = plugin; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&b&lSpleef&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        SpleefManager sm = plugin.getSpleefManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "list";

        switch (sub) {
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /spleef join <partie>")); return true; }
                SpleefGame game = sm.getGame(args[1]);
                if (game == null) { player.sendMessage(pre() + c("&cPartie introuvable.")); return true; }
                if (sm.getPlayerGame(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es déjà dans une partie.")); return true; }
                if (!sm.joinGame(game, player)) { player.sendMessage(pre() + c("&cImpossible de rejoindre cette partie.")); return true; }
                player.sendMessage(pre() + c("&aRejoint la partie &e" + game.getName() + " &a(" + game.getPlayers().size() + "/" + game.getMaxPlayers() + ")"));
            }
            case "leave" -> {
                SpleefGame game = sm.getPlayerGame(player.getUniqueId());
                if (game == null) { player.sendMessage(pre() + c("&cTu n'es pas dans une partie.")); return true; }
                sm.leaveGame(game, player);
                player.sendMessage(pre() + c("&aQuitté la partie."));
            }
            case "list" -> {
                player.sendMessage(c("&b=== Parties Spleef ==="));
                sm.getGames().forEach(g -> player.sendMessage(c("&e" + g.getName() + " &7- " + g.getState().name() + " (" + g.getPlayers().size() + "/" + g.getMaxPlayers() + ")")));
            }
            case "create" -> {
                if (!player.hasPermission("astraland.spleef.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /spleef create <nom>")); return true; }
                sm.createGame(args[1]);
                player.sendMessage(pre() + c("&aPartie &e" + args[1] + " &acréée."));
            }
            case "setspawn" -> {
                if (!player.hasPermission("astraland.spleef.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /spleef setspawn <partie>")); return true; }
                SpleefGame game = sm.getGame(args[1]);
                if (game == null) { player.sendMessage(pre() + c("&cPartie introuvable.")); return true; }
                game.setSpawn(player.getLocation());
                player.sendMessage(pre() + c("&aSpawn défini."));
            }
            case "start" -> {
                if (!player.hasPermission("astraland.spleef.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /spleef start <partie>")); return true; }
                SpleefGame game = sm.getGame(args[1]);
                if (game == null) { player.sendMessage(pre() + c("&cPartie introuvable.")); return true; }
                if (game.getPlayers().size() < game.getMinPlayers()) { player.sendMessage(pre() + c("&cPas assez de joueurs.")); return true; }
                player.sendMessage(pre() + c("&aDémarrage forcé de &e" + args[1] + "&a."));
            }
            default -> { player.sendMessage(c("&b=== /spleef ===")); player.sendMessage(c("&e/spleef join/leave/list/create/setspawn/start")); }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        String typed = args[args.length - 1].toLowerCase();
        if (args.length == 1)
            return Arrays.asList("join","leave","list","create","setspawn","start").stream()
                .filter(sub -> sub.startsWith(typed)).collect(java.util.stream.Collectors.toList());
        if (args.length == 2 && Arrays.asList("join","setspawn","start").contains(args[0].toLowerCase()))
            return plugin.getSpleefManager().getGames().stream()
                .map(com.astraland.spleef.models.SpleefGame::getName)
                .filter(n -> n.toLowerCase().startsWith(typed)).collect(java.util.stream.Collectors.toList());
        return List.of();
    }
}
