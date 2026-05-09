package com.astraland.buildbattle.commands;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.managers.BuildBattleManager;
import com.astraland.buildbattle.models.BBGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BuildBattleCommand implements CommandExecutor, TabCompleter {

    private final BuildBattle plugin;
    public BuildBattleCommand(BuildBattle plugin) { this.plugin = plugin; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&e&lBuildBattle&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        BuildBattleManager bm = plugin.getBuildBattleManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "list";

        switch (sub) {
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bb join <partie>")); return true; }
                BBGame game = bm.getGame(args[1]);
                if (game == null) { player.sendMessage(pre() + c("&cPartie introuvable.")); return true; }
                if (bm.getPlayerGame(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es déjà dans une partie.")); return true; }
                if (!bm.joinGame(game, player)) { player.sendMessage(pre() + c("&cImpossible de rejoindre.")); return true; }
                player.sendMessage(pre() + c("&aRejoint &e" + game.getName() + " &a(" + game.getPlayers().size() + "/" + game.getMaxPlayers() + ")"));
            }
            case "leave" -> {
                BBGame game = bm.getPlayerGame(player.getUniqueId());
                if (game == null) { player.sendMessage(pre() + c("&cTu n'es pas dans une partie.")); return true; }
                bm.leaveGame(game, player);
                player.sendMessage(pre() + c("&aQuitté la partie."));
            }
            case "vote" -> {
                BBGame game = bm.getPlayerGame(player.getUniqueId());
                if (game == null) { player.sendMessage(pre() + c("&cTu n'es pas dans une partie.")); return true; }
                if (game.getState() != BBGame.State.VOTING) { player.sendMessage(pre() + c("&cPas en phase de vote.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bb vote <1-5>")); return true; }
                try {
                    int score = Integer.parseInt(args[1]);
                    if (score < 1 || score > 5) throw new NumberFormatException();
                    bm.vote(game, player.getUniqueId(), score);
                    player.sendMessage(pre() + c("&aVote &e" + score + "/5 &aenregistré."));
                } catch (NumberFormatException e) { player.sendMessage(pre() + c("&cVote invalide (1-5).")); }
            }
            case "list" -> {
                player.sendMessage(c("&e=== Parties Build Battle ==="));
                bm.getGames().forEach(g -> player.sendMessage(c("&e" + g.getName() + " &7- " + g.getState().name() + " (" + g.getPlayers().size() + "/" + g.getMaxPlayers() + ")")));
            }
            case "create" -> {
                if (!player.hasPermission("astraland.buildbattle.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bb create <nom>")); return true; }
                bm.createGame(args[1]);
                player.sendMessage(pre() + c("&aPartie &e" + args[1] + " &acréée."));
            }
            default -> { player.sendMessage(c("&e=== /buildbattle ===")); player.sendMessage(c("&e/bb join/leave/vote/list/create")); }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        String typed = args[args.length - 1].toLowerCase();
        if (args.length == 1)
            return Arrays.asList("join","leave","vote","list","create").stream()
                .filter(sub -> sub.startsWith(typed)).collect(java.util.stream.Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("vote"))
            return Arrays.asList("1","2","3","4","5").stream()
                .filter(v -> v.startsWith(typed)).collect(java.util.stream.Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("join"))
            return plugin.getBuildBattleManager().getGames().stream()
                .map(com.astraland.buildbattle.models.BBGame::getName)
                .filter(n -> n.toLowerCase().startsWith(typed)).collect(java.util.stream.Collectors.toList());
        return List.of();
    }
}
