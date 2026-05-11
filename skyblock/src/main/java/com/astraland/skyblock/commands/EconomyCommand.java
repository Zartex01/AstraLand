package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    private final Skyblock plugin;

    public EconomyCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String fmt(long v) { return NumberFormat.getInstance(Locale.FRENCH).format(v); }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "balance" -> { handleBalance(sender, args); yield true; }
            case "pay"     -> { handlePay(sender, args);     yield true; }
            case "baltop"  -> { handleBaltop(sender);        yield true; }
            default        -> true;
        };
    }

    // ─── /balance ─────────────────────────────────────────────────────────────

    private void handleBalance(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) { sender.sendMessage(c("&cUtilise /balance <joueur> depuis la console.")); return; }
            if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return; }
            int bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
            sender.sendMessage(c("&8&m──────────────────────────────"));
            sender.sendMessage(c("  &e&l💰 Ton solde Skyblock"));
            sender.sendMessage(c("  &6" + fmt(bal) + " $"));
            sender.sendMessage(c("&8&m──────────────────────────────"));
        } else {
            @SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore()) { sender.sendMessage(c("&cJoueur introuvable.")); return; }
            int bal = plugin.getEconomyManager().getBalance(target.getUniqueId());
            sender.sendMessage(c("&7Solde de &e" + (target.getName() != null ? target.getName() : args[0])
                + " &7: &6" + fmt(bal) + " $"));
        }
    }

    // ─── /pay ─────────────────────────────────────────────────────────────────

    private void handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(c("&cCette commande est réservée aux joueurs.")); return; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return; }
        if (args.length < 2) { player.sendMessage(c("&cUsage : /pay <joueur> <montant>")); return; }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null)         { player.sendMessage(c("&cJoueur introuvable ou non connecté.")); return; }
        if (target.equals(player)) { player.sendMessage(c("&cTu ne peux pas te payer toi-même.")); return; }

        long amount;
        try { amount = Long.parseLong(args[1]); } catch (NumberFormatException e) { player.sendMessage(c("&cMontant invalide.")); return; }
        if (amount <= 0) { player.sendMessage(c("&cLe montant doit être positif.")); return; }
        if (amount > 1_000_000L) { player.sendMessage(c("&cTu ne peux pas transférer plus de &e1 000 000 $ &cd'un coup.")); return; }

        EconomyManager eco = plugin.getEconomyManager();
        if (!eco.removeBalance(player.getUniqueId(), (int) amount)) {
            player.sendMessage(c("&cSolde insuffisant. &7Ton solde : &6" + fmt(eco.getBalance(player.getUniqueId())) + " $")); return;
        }
        eco.addBalance(target.getUniqueId(), (int) amount);
        player.sendMessage(c("&a✔ &7Tu as envoyé &6" + fmt(amount) + " $ &7à &e" + target.getName()
            + " &8| &7Solde restant : &6" + fmt(eco.getBalance(player.getUniqueId())) + " $"));
        target.sendMessage(c("&a✔ &7Tu as reçu &6" + fmt(amount) + " $ &7de &e" + player.getName()
            + " &8| &7Ton solde : &6" + fmt(eco.getBalance(target.getUniqueId())) + " $"));
    }

    // ─── /baltop ──────────────────────────────────────────────────────────────

    private void handleBaltop(CommandSender sender) {
        EconomyManager eco = plugin.getEconomyManager();
        List<Map.Entry<UUID, Integer>> top = eco.getTopBalances(10);

        sender.sendMessage(c("&8&m─────────────────────────────────"));
        sender.sendMessage(c("  &e&l💰 Top 10 — Joueurs les plus riches"));
        sender.sendMessage(c("&8&m─────────────────────────────────"));

        if (top.isEmpty()) { sender.sendMessage(c("  &7Aucune donnée disponible.")); return; }

        String[] medals = {"&6🥇","&7🥈","&c🥉"};
        for (int i = 0; i < top.size(); i++) {
            Map.Entry<UUID, Integer> entry = top.get(i);
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
            String name  = op.getName() != null ? op.getName() : entry.getKey().toString().substring(0,8) + "...";
            String medal = i < 3 ? medals[i] : c("&8#" + (i + 1));
            sender.sendMessage(c(medal + " &f" + name + " &8— &6" + fmt(entry.getValue()) + " $"));
        }
        sender.sendMessage(c("&8&m─────────────────────────────────"));
    }

    // ─── Tab ──────────────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("balance") && args.length == 1)
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (command.getName().equalsIgnoreCase("pay") && args.length == 1)
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        return Collections.emptyList();
    }
}
