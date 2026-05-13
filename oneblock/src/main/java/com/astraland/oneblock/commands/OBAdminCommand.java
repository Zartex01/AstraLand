package com.astraland.oneblock.commands;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.OneBlockManager;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OBAdminCommand implements CommandExecutor, TabCompleter {

    private final OneBlock plugin;

    public OBAdminCommand(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c("&8[&c&lOBAdmin&8] &r"); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("astraland.admin")) {
            sender.sendMessage(c("&cTu n'as pas la permission."));
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        OneBlockManager om = plugin.getOneBlockManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "info" -> {
                if (args.length < 2) { sender.sendMessage(pre() + c("&cUsage : /obadmin info <joueur>")); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                OneBlockIsland isl = om.getIsland(target.getUniqueId());
                if (isl == null) { sender.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                sender.sendMessage(c("&8&m──── &cAdmin Info Île &8&m────"));
                sender.sendMessage(c("&7Joueur : &e" + args[1]));
                sender.sendMessage(c("&7Blocs : &e" + isl.getBlocksBroken()));
                sender.sendMessage(c("&7Phase : " + isl.getCurrentPhase().getColor() + isl.getCurrentPhase().getDisplayName()));
                sender.sendMessage(c("&7Niveau : &b" + isl.getIslandLevel()));
                sender.sendMessage(c("&7Membres : &e" + (isl.getMembers().size() + 1)));
                sender.sendMessage(c("&7PvP : " + (isl.isPvpEnabled() ? "&aActivé" : "&cDésactivé")));
                sender.sendMessage(c("&7Visiteurs : " + (isl.isVisitorsAllowed() ? "&aOui" : "&cNon")));
                sender.sendMessage(c("&7Warp : " + (isl.isWarpEnabled() ? "&aActivé (" + isl.getWarpName() + ")" : "&cDésactivé")));
            }
            case "tp" -> {
                if (!(sender instanceof Player admin)) { sender.sendMessage("Joueur uniquement."); return true; }
                if (args.length < 2) { sender.sendMessage(pre() + c("&cUsage : /obadmin tp <joueur>")); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                OneBlockIsland isl = om.getIsland(target.getUniqueId());
                if (isl == null) { sender.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                ((Player) admin).teleport(isl.getHome());
                sender.sendMessage(pre() + c("&aTéléporté sur l'île de &e" + args[1] + "&a."));
            }
            case "setblocks" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /obadmin setblocks <joueur> <montant>")); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                OneBlockIsland isl = om.getIsland(target.getUniqueId());
                if (isl == null) { sender.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                try {
                    long amount = Long.parseLong(args[2]);
                    isl.setBlocksBroken(amount);
                    om.saveAll();
                    sender.sendMessage(pre() + c("&aBlocs de &e" + args[1] + " &adéfinis à &e" + amount + "&a."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(pre() + c("&cNombre invalide."));
                }
            }
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage(pre() + c("&cUsage : /obadmin reset <joueur>")); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                UUID uuid = target.getUniqueId();
                OneBlockIsland isl = om.getIsland(uuid);
                if (isl == null) { sender.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                om.deleteIsland(isl.getOwner());
                sender.sendMessage(pre() + c("&aÎle de &e" + args[1] + " &asupprimée."));
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) online.sendMessage(pre() + c("&cTon île a été réinitialisée par un administrateur."));
            }
            case "give" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /obadmin give <joueur> <montant>")); return true; }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getEconomyManager().addBalance(target.getUniqueId(), amount);
                    sender.sendMessage(pre() + c("&a+" + amount + " &7pièces données à &e" + args[1] + "&a."));
                    Player online = Bukkit.getPlayer(target.getUniqueId());
                    if (online != null) online.sendMessage(pre() + c("&aUn admin t'a donné &e" + amount + " &apièces."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(pre() + c("&cNombre invalide."));
                }
            }
            case "list" -> {
                sender.sendMessage(c("&8&m──── &cListe des îles &8&m────"));
                for (OneBlockIsland isl : om.getAllIslands()) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(isl.getOwner());
                    sender.sendMessage(c("&7- &e" + (op.getName() != null ? op.getName() : "?")
                        + " &8| " + isl.getCurrentPhase().getColor() + isl.getCurrentPhase().getDisplayName()
                        + " &8| &e" + isl.getBlocksBroken() + " &7blocs"));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(pre() + c("&aConfiguration rechargée."));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(c("&8&m──── &c&lOBAdmin &7- Aide &8&m────"));
        sender.sendMessage(c("&e/obadmin info <joueur>"));
        sender.sendMessage(c("&e/obadmin tp <joueur>"));
        sender.sendMessage(c("&e/obadmin setblocks <joueur> <montant>"));
        sender.sendMessage(c("&e/obadmin reset <joueur>"));
        sender.sendMessage(c("&e/obadmin give <joueur> <montant>"));
        sender.sendMessage(c("&e/obadmin list"));
        sender.sendMessage(c("&e/obadmin reload"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("astraland.admin")) return List.of();
        if (args.length == 1) {
            return Arrays.asList("info","tp","setblocks","reset","give","list","reload").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
