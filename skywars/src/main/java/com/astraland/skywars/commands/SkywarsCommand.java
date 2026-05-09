package com.astraland.skywars.commands;

import com.astraland.skywars.Skywars;
import com.astraland.skywars.managers.SkywarsManager;
import com.astraland.skywars.models.SkywarsArena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SkywarsCommand implements CommandExecutor, TabCompleter {

    private final Skywars plugin;
    public SkywarsCommand(Skywars plugin) { this.plugin = plugin; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&9&lSkywars&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        SkywarsManager sm = plugin.getSkywarsManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "list";

        switch (sub) {
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /sw join <arène>")); return true; }
                SkywarsArena arena = sm.getArena(args[1]);
                if (arena == null) { player.sendMessage(pre() + c("&cArène introuvable.")); return true; }
                if (sm.getPlayerArena(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es déjà dans une arène.")); return true; }
                if (!sm.joinArena(arena, player)) { player.sendMessage(pre() + c("&cImpossible de rejoindre.")); return true; }
                player.sendMessage(pre() + c("&aRejoint &e" + arena.getName() + " &a(" + arena.getPlayers().size() + "/" + arena.getMaxPlayers() + ")"));
            }
            case "leave" -> {
                SkywarsArena arena = sm.getPlayerArena(player.getUniqueId());
                if (arena == null) { player.sendMessage(pre() + c("&cTu n'es pas dans une arène.")); return true; }
                sm.leaveArena(arena, player);
                player.sendMessage(pre() + c("&aQuitté l'arène."));
            }
            case "kit" -> {
                String kitName = args.length > 1 ? args[1].toUpperCase() : "WARRIOR";
                if (!plugin.getConfig().getConfigurationSection("kits").getKeys(false).contains(kitName)) {
                    player.sendMessage(pre() + c("&cKit invalide. Kits : WARRIOR, ARCHER, MAGE")); return true;
                }
                SkywarsArena arena = sm.getPlayerArena(player.getUniqueId());
                if (arena != null && arena.getState() != SkywarsArena.State.WAITING && arena.getState() != SkywarsArena.State.COUNTDOWN) {
                    player.sendMessage(pre() + c("&cChanger de kit uniquement en attente.")); return true;
                }
                if (arena != null) arena.getPlayerKits().put(player.getUniqueId(), kitName);
                String display = c(plugin.getConfig().getString("kits." + kitName + ".display", kitName));
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.kit-selected", "&aKit &e%kit% &asélectionné !").replace("%kit%", display)));
            }
            case "list" -> {
                player.sendMessage(c("&9=== Arènes Skywars ==="));
                sm.getArenas().forEach(a -> player.sendMessage(c("&e" + a.getName() + " &7- " + a.getState().name() + " (" + a.getPlayers().size() + "/" + a.getMaxPlayers() + ")")));
            }
            case "create" -> {
                if (!player.hasPermission("astraland.skywars.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /sw create <nom>")); return true; }
                sm.createArena(args[1]);
                player.sendMessage(pre() + c("&aArène &e" + args[1] + " &acréée."));
            }
            case "setspawn" -> {
                if (!player.hasPermission("astraland.skywars.admin")) { player.sendMessage(pre() + c("&cPermission refusée.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /sw setspawn <arène>")); return true; }
                SkywarsArena arena = sm.getArena(args[1]);
                if (arena == null) { player.sendMessage(pre() + c("&cArène introuvable.")); return true; }
                arena.getSpawns().add(player.getLocation());
                player.sendMessage(pre() + c("&aSpawn #" + arena.getSpawns().size() + " ajouté pour &e" + args[1] + "&a."));
            }
            default -> {
                player.sendMessage(c("&9=== /skywars ==="));
                player.sendMessage(c("&e/sw join <arène> &7- Rejoindre")); player.sendMessage(c("&e/sw leave &7- Quitter"));
                player.sendMessage(c("&e/sw kit <WARRIOR|ARCHER|MAGE> &7- Kit")); player.sendMessage(c("&e/sw list &7- Liste"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("join", "leave", "kit", "list", "create", "setspawn");
        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) return Arrays.asList("WARRIOR", "ARCHER", "MAGE");
        return List.of();
    }
}
