package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class IslandAdminCommand implements CommandExecutor, TabCompleter {

    private final Skyblock plugin;
    public IslandAdminCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c("&8[&cAdmin&8] "); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("astraland.admin") && !sender.isOp()) {
            sender.sendMessage(c("&cPermission manquante.")); return true;
        }
        if (args.length < 2) { sendHelp(sender); return true; }

        String sub = args[0].toLowerCase();
        IslandManager im = plugin.getIslandManager();

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Island isl = im.getOwnedIsland(target.getUniqueId());
        String tName = target.getName() != null ? target.getName() : args[1];

        switch (sub) {
            case "info" -> {
                if (isl == null) { sender.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                sender.sendMessage(c("&a=== Île de " + tName + " ==="));
                sender.sendMessage(c("&7Niveau : &e" + isl.getLevel() + " &8| Valeur : &6" + isl.getValue()));
                sender.sendMessage(c("&7Membres : &f" + (isl.getMemberCount() + 1) + "/" + (isl.getMemberSlots() + 1)));
                sender.sendMessage(c("&7Générateur : &b" + isl.getGeneratorLevel() + "/7"));
                sender.sendMessage(c("&7Solde : &e" + plugin.getEconomyManager().getBalance(target.getUniqueId()) + " $"));
            }
            case "visit" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Joueurs uniquement."); return true; }
                if (isl == null) { sender.sendMessage(pre() + c("&cIle introuvable.")); return true; }
                p.teleport(isl.getHome());
                sender.sendMessage(pre() + c("&aTéléporté sur l'île de &e" + tName));
            }
            case "setlevel" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /isadmin setlevel <joueur> <niveau>")); return true; }
                if (isl == null) { sender.sendMessage(pre() + c("&cIle introuvable.")); return true; }
                try {
                    int lvl = Integer.parseInt(args[2]);
                    isl.setLevel(lvl);
                    im.saveAll();
                    sender.sendMessage(pre() + c("&aNiveau de &e" + tName + " &afixé à &e" + lvl));
                } catch (NumberFormatException e) { sender.sendMessage(pre() + c("&cNombre invalide.")); }
            }
            case "setbalance" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /isadmin setbalance <joueur> <montant>")); return true; }
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
                    sender.sendMessage(pre() + c("&aSolde de &e" + tName + " &afixé à &e" + amount + " $"));
                } catch (NumberFormatException e) { sender.sendMessage(pre() + c("&cNombre invalide.")); }
            }
            case "reset" -> {
                if (isl == null) { sender.sendMessage(pre() + c("&cIle introuvable.")); return true; }
                im.deleteIsland(target.getUniqueId());
                sender.sendMessage(pre() + c("&aÎle de &e" + tName + " &asupprimée."));
                Player online = Bukkit.getPlayer(target.getUniqueId());
                if (online != null) online.sendMessage(c("&cTon île a été réinitialisée par un administrateur."));
            }
            case "scan" -> {
                if (isl == null) { sender.sendMessage(pre() + c("&cIle introuvable.")); return true; }
                int radius = plugin.getConfig().getInt("island.size", 100);
                sender.sendMessage(pre() + c("&7Scan en cours..."));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    long val = plugin.getLevelManager().scanIsland(isl, radius);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        im.saveAll();
                        sender.sendMessage(pre() + c("&aScan terminé : valeur &e" + val + " &a| niveau &e" + isl.getLevel()));
                    });
                });
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(c("&c=== Admin Skyblock ==="));
        s.sendMessage(c("&e/isadmin info <joueur> &8- &7Infos île"));
        s.sendMessage(c("&e/isadmin visit <joueur> &8- &7Aller sur l'île"));
        s.sendMessage(c("&e/isadmin setlevel <joueur> <n> &8- &7Fixer le niveau"));
        s.sendMessage(c("&e/isadmin setbalance <joueur> <n> &8- &7Fixer le solde"));
        s.sendMessage(c("&e/isadmin scan <joueur> &8- &7Scanner l'île"));
        s.sendMessage(c("&e/isadmin reset <joueur> &8- &7Supprimer l'île"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return List.of("info","visit","setlevel","setbalance","scan","reset").stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        return List.of();
    }
}
