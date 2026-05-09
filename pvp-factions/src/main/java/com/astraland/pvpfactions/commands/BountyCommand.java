package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.BountyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public BountyCommand(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&c&lFactions&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        BountyManager bm = plugin.getBountyManager();

        if (sender instanceof Player player && !plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        if (label.equalsIgnoreCase("bountylist") || (args.length > 0 && args[0].equalsIgnoreCase("list"))) {
            List<Map.Entry<UUID, Integer>> top = bm.getTopBounties(10);
            sender.sendMessage(c("&6&l=== Primes actives ==="));
            if (top.isEmpty()) { sender.sendMessage(c("&7Aucune prime active.")); return true; }
            for (int i = 0; i < top.size(); i++) {
                UUID uuid = top.get(i).getKey();
                int amount = top.get(i).getValue();
                sender.sendMessage(c("&e#" + (i + 1) + " &f" + getPlayerName(uuid) + " &7- &c" + amount + " &7pièces de prime"));
            }
            return true;
        }

        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }

        if (args.length < 1) {
            player.sendMessage(c("&6=== /bounty ==="));
            player.sendMessage(c("&e/bounty <joueur> <montant> &7- Placer une prime"));
            player.sendMessage(c("&e/bounty list &7- Voir les primes actives"));
            player.sendMessage(c("&e/bounty info <joueur> &7- Voir la prime d'un joueur"));
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bounty info <joueur>")); return true; }
            Player target = Bukkit.getPlayerExact(args[1]);
            UUID targetId = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[1]).getUniqueId();
            int total = bm.getTotalBounty(targetId);
            player.sendMessage(c("&6Prime sur &e" + args[1] + "&6 : &c" + total + " &6pièces"));
            return true;
        }

        if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /bounty <joueur> <montant>")); return true; }

        Player target = Bukkit.getPlayerExact(args[0]);
        UUID targetId;
        String targetName;
        if (target != null) { targetId = target.getUniqueId(); targetName = target.getName(); }
        else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
            targetId = op.getUniqueId();
            targetName = op.getName() != null ? op.getName() : args[0];
        }

        if (targetId.equals(player.getUniqueId())) {
            player.sendMessage(pre() + c("&cTu ne peux pas mettre une prime sur toi-même."));
            return true;
        }

        int amount;
        try { amount = Integer.parseInt(args[1]); } catch (NumberFormatException e) {
            player.sendMessage(pre() + c("&cMontant invalide.")); return true;
        }
        if (amount <= 0) { player.sendMessage(pre() + c("&cLe montant doit être positif.")); return true; }

        bm.placeBounty(player.getUniqueId(), targetId, amount);
        player.sendMessage(pre() + c("&aPrime de &e" + amount + " &apièces placée sur &e" + targetName + "&a !"));
        if (target != null)
            target.sendMessage(pre() + c("&c" + player.getName() + " &aa mis une prime de &e" + amount + " &cpièces sur ta tête !"));

        int total = bm.getTotalBounty(targetId);
        Bukkit.broadcastMessage(c("&6[Prime] &e" + player.getName() + " &aoffre &c" + amount + " &apièces pour la tête de &e" + targetName + " &7(Total : &c" + total + "&7)"));
        return true;
    }

    private String getPlayerName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) return p.getName();
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : uuid.toString().substring(0, 8);
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(List.of("list", "info"));
            Bukkit.getOnlinePlayers().forEach(p -> opts.add(p.getName()));
            return opts;
        }
        return List.of();
    }
}
