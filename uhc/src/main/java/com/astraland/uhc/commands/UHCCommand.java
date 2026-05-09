package com.astraland.uhc.commands;

import com.astraland.uhc.UHC;
import com.astraland.uhc.managers.UHCManager;
import com.astraland.uhc.models.UHCGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class UHCCommand implements CommandExecutor, TabCompleter {

    private final UHC plugin;
    public UHCCommand(UHC plugin) { this.plugin = plugin; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&4&lUHC&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player && !plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        UHCManager um = plugin.getUhcManager();
        UHCGame game = um.getGame();
        String sub = args.length > 0 ? args[0].toLowerCase() : "list";

        switch (sub) {
            case "join" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
                if (!um.joinGame(player)) player.sendMessage(pre() + c("&cImpossible de rejoindre (partie en cours ?)."));
            }
            case "leave" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
                if (!game.isInGame(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu n'es pas dans la file.")); return true; }
                um.leaveGame(player);
            }
            case "start" -> {
                if (!sender.hasPermission("astraland.uhc.admin")) { sender.sendMessage(c("&cPermission refusée.")); return true; }
                if (game.getState() != UHCGame.State.WAITING) { sender.sendMessage(pre() + c("&cUne partie est déjà en cours.")); return true; }
                if (game.getPlayers().size() < 2) { sender.sendMessage(pre() + c("&cPas assez de joueurs (min 2).")); return true; }
                um.startGame();
                sender.sendMessage(pre() + c("&aPartie UHC lancée avec &e" + game.getPlayers().size() + " &ajoueurs !"));
            }
            case "stop" -> {
                if (!sender.hasPermission("astraland.uhc.admin")) { sender.sendMessage(c("&cPermission refusée.")); return true; }
                um.stopGame();
                sender.sendMessage(pre() + c("&aPartie UHC arrêtée."));
            }
            case "list" -> {
                sender.sendMessage(c("&4=== UHC ==="));
                sender.sendMessage(c("&7Statut : &e" + game.getState().name()));
                sender.sendMessage(c("&7Joueurs en attente : &e" + game.getPlayers().size()));
                sender.sendMessage(c("&7Scénario : &e" + game.getScenario()));
                if (game.getState() == UHCGame.State.INGAME)
                    sender.sendMessage(c("&7Joueurs en vie : &e" + game.getAlive().size()));
            }
            case "scenario" -> {
                if (!sender.hasPermission("astraland.uhc.admin")) { sender.sendMessage(c("&cPermission refusée.")); return true; }
                if (args.length < 2) { sender.sendMessage(pre() + c("&cUsage : /uhc scenario <VANILLA|CUTCLEAN|TIMEBOMB|NOFALL>")); return true; }
                List<String> valid = plugin.getConfig().getStringList("scenarios");
                if (!valid.contains(args[1].toUpperCase())) { sender.sendMessage(pre() + c("&cScénario invalide.")); return true; }
                game.setScenario(args[1].toUpperCase());
                sender.sendMessage(pre() + c("&aScénario défini : &e" + args[1].toUpperCase()));
            }
            default -> {
                sender.sendMessage(c("&4=== /uhc ==="));
                sender.sendMessage(c("&e/uhc join/leave &7- File d'attente"));
                sender.sendMessage(c("&e/uhc start/stop &7[admin] - Contrôle"));
                sender.sendMessage(c("&e/uhc list &7- Statut"));
                sender.sendMessage(c("&e/uhc scenario <scén> &7[admin]"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        String typed = args[args.length - 1].toLowerCase();
        if (args.length == 1)
            return Arrays.asList("join","leave","start","stop","list","scenario").stream()
                .filter(sub -> sub.startsWith(typed)).collect(java.util.stream.Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("scenario"))
            return Arrays.asList("VANILLA","CUTCLEAN","TIMEBOMB","NOFALL").stream()
                .filter(sc -> sc.toLowerCase().startsWith(typed)).collect(java.util.stream.Collectors.toList());
        return List.of();
    }
}
