package com.astraland.bedwars.commands;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.managers.ArenaManager;
import com.astraland.bedwars.models.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BedwarsCommand implements CommandExecutor, TabCompleter {

    private final Bedwars plugin;
    public BedwarsCommand(Bedwars plugin) { this.plugin = plugin; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&c&lBedwars&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        ArenaManager am = plugin.getArenaManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "list";

        switch (sub) {
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bw join <arène>")); return true; }
                Arena arena = am.getArena(args[1]);
                if (arena == null) { player.sendMessage(pre() + c("&cArène introuvable.")); return true; }
                if (am.getPlayerArena(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es déjà dans une arène.")); return true; }
                if (!am.joinArena(arena, player.getUniqueId())) { player.sendMessage(pre() + c("&cImpossible de rejoindre cette arène.")); return true; }
                if (arena.getLobby() != null) player.teleport(arena.getLobby());
                player.sendMessage(pre() + c("&aRejoint l'arène &e" + arena.getName() + " &a(" + arena.getPlayerCount() + "/" + arena.getMaxPlayers() + ")"));
            }
            case "leave" -> {
                Arena arena = am.getPlayerArena(player.getUniqueId());
                if (arena == null) { player.sendMessage(pre() + c("&cTu n'es pas dans une arène.")); return true; }
                am.leaveArena(arena, player.getUniqueId());
                if (arena.getLobby() != null) player.teleport(arena.getLobby());
                player.sendMessage(pre() + c("&aQuitté l'arène."));
            }
            case "list" -> {
                player.sendMessage(c("&6=== Arènes Bedwars ==="));
                am.getArenas().forEach(a -> player.sendMessage(c("&e" + a.getName() + " &7- &f" + a.getState().name() + " &7(" + a.getPlayerCount() + "/" + a.getMaxPlayers() + ")")));
            }
            case "create" -> {
                if (!player.hasPermission("astraland.bedwars.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bw create <nom>")); return true; }
                if (am.getArena(args[1]) != null) { player.sendMessage(pre() + c("&cCette arène existe déjà.")); return true; }
                am.createArena(args[1]);
                player.sendMessage(pre() + c("&aArène &e" + args[1] + " &acréée."));
            }
            case "setspawn" -> {
                if (!player.hasPermission("astraland.bedwars.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 3) { player.sendMessage(pre() + c("&cUsage : /bw setspawn <arène> <équipe>")); return true; }
                Arena arena = am.getArena(args[1]);
                if (arena == null) { player.sendMessage(pre() + c("&cArène introuvable.")); return true; }
                com.astraland.bedwars.models.BedwarsTeam team = arena.getTeams().get(args[2].toUpperCase());
                if (team == null) { player.sendMessage(pre() + c("&cÉquipe invalide (RED/BLUE/GREEN/YELLOW).")); return true; }
                team.setSpawn(player.getLocation());
                am.saveAll();
                player.sendMessage(pre() + c("&aSpawn de l'équipe &e" + args[2] + " &adéfini."));
            }
            case "setbed" -> {
                if (!player.hasPermission("astraland.bedwars.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 3) { player.sendMessage(pre() + c("&cUsage : /bw setbed <arène> <équipe>")); return true; }
                Arena arena = am.getArena(args[1]);
                if (arena == null) { player.sendMessage(pre() + c("&cArène introuvable.")); return true; }
                com.astraland.bedwars.models.BedwarsTeam team = arena.getTeams().get(args[2].toUpperCase());
                if (team == null) { player.sendMessage(pre() + c("&cÉquipe invalide.")); return true; }
                team.setBedLocation(player.getLocation());
                am.saveAll();
                player.sendMessage(pre() + c("&aLit de l'équipe &e" + args[2] + " &adéfini à ta position."));
            }
            case "start" -> {
                if (!player.hasPermission("astraland.bedwars.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bw start <arène>")); return true; }
                Arena arena = am.getArena(args[1]);
                if (arena == null) { player.sendMessage(pre() + c("&cArène introuvable.")); return true; }
                if (arena.getPlayerCount() < arena.getMinPlayers()) { player.sendMessage(pre() + c("&cPas assez de joueurs.")); return true; }
                player.sendMessage(pre() + c("&aForce le démarrage de l'arène &e" + args[1] + "&a."));
            }
            default -> {
                player.sendMessage(c("&c&l=== /bedwars ==="));
                player.sendMessage(c("&e/bw join <arène> &7- Rejoindre")); player.sendMessage(c("&e/bw leave &7- Quitter"));
                player.sendMessage(c("&e/bw list &7- Arènes disponibles")); player.sendMessage(c("&e/bw create <nom> &7[admin]"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("join", "leave", "list", "create", "setspawn", "setbed", "start");
        return List.of();
    }
}
