package com.astraland.duels.commands;

import com.astraland.duels.Duels;
import com.astraland.duels.managers.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final Duels plugin;
    public DuelCommand(Duels plugin) { this.plugin = plugin; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&5&lDuels&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        DuelManager dm = plugin.getDuelManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "help";

        switch (sub) {
            case "accept" -> {
                if (!dm.acceptRequest(player)) player.sendMessage(pre() + c("&cAucune demande de duel en attente."));
            }
            case "deny" -> {
                dm.denyRequest(player);
            }
            case "kit" -> {
                String kit = args.length > 1 ? args[1].toUpperCase() : "NODEBUFF";
                if (!plugin.getConfig().getConfigurationSection("kits").getKeys(false).contains(kit)) {
                    player.sendMessage(pre() + c("&cKit invalide. Kits : NODEBUFF, SOUP, ARCHER, UHC")); return true;
                }
                if (dm.isInDuel(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu es en duel !")); return true; }
                dm.setKit(player.getUniqueId(), kit);
                String display = c(plugin.getConfig().getString("kits." + kit + ".display", kit));
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.kit-selected", "&aKit &e%kit% &asélectionné.").replace("%kit%", display)));
            }
            case "leave" -> {
                if (!dm.isInDuel(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu n'es pas en duel.")); return true; }
                dm.onDeath(player);
                player.sendMessage(pre() + c("&cTu as abandonné le duel."));
            }
            case "list" -> {
                player.sendMessage(c("&5=== Duels en cours ==="));
                player.sendMessage(c("&7Ton kit actuel : &e" + dm.getSelectedKit(player.getUniqueId())));
                player.sendMessage(c("&7Kits disponibles : &eNODEBUFF, SOUP, ARCHER, UHC"));
            }
            default -> {
                if (args.length == 0 || sub.equals("help")) {
                    player.sendMessage(c("&5=== /duel ==="));
                    player.sendMessage(c("&e/duel <joueur> [kit] &7- Défier un joueur"));
                    player.sendMessage(c("&e/duel accept &7- Accepter un duel"));
                    player.sendMessage(c("&e/duel deny &7- Refuser un duel"));
                    player.sendMessage(c("&e/duel kit <NODEBUFF|SOUP|ARCHER|UHC> &7- Choisir un kit"));
                    player.sendMessage(c("&e/duel leave &7- Abandonner le duel en cours"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(sub);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (target.equals(player)) { player.sendMessage(pre() + c("&cTu ne peux pas te défier toi-même.")); return true; }
                if (dm.isInDuel(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu es déjà en duel.")); return true; }
                if (dm.isInDuel(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur est déjà en duel.")); return true; }
                String kit = args.length > 1 ? args[1].toUpperCase() : dm.getSelectedKit(player.getUniqueId());
                dm.sendRequest(player, target, kit);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new java.util.ArrayList<>(Arrays.asList("accept", "deny", "kit", "leave", "list"));
            Bukkit.getOnlinePlayers().forEach(p -> opts.add(p.getName()));
            return opts;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("kit")) return Arrays.asList("NODEBUFF", "SOUP", "ARCHER", "UHC");
        return List.of();
    }
}
