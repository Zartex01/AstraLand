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

import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class IslandAdminCommand implements CommandExecutor, TabCompleter {

    private final Skyblock plugin;
    public IslandAdminCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s)  { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre()        { return c("&8[&cAdmin&8] "); }
    private String fmt(long v)  { return NumberFormat.getInstance(Locale.FRENCH).format(v); }

    private static final List<String> SUBS = List.of(
        "info","visit","setlevel","setbalance","setgen","give","scan","reset","list","help"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("astraland.admin") && !sender.isOp()) {
            sender.sendMessage(c("&cPermission manquante.")); return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        String sub = args[0].toLowerCase();
        IslandManager im = plugin.getIslandManager();

        // ── Commandes sans joueur cible ────────────────────────────────────────
        if (sub.equals("list")) {
            Collection<Island> all = im.getAllIslands();
            sender.sendMessage(c("&8&m──────────────────────────────────"));
            sender.sendMessage(c("  &c&l🗺 Liste des îles &8(" + all.size() + " îles)"));
            sender.sendMessage(c("&8&m──────────────────────────────────"));
            int rank = 1;
            for (Island isl : all.stream().sorted((a, b) -> Long.compare(b.getValue(), a.getValue())).toList()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(isl.getOwner());
                String name = op.getName() != null ? op.getName() : isl.getOwner().toString().substring(0, 8);
                sender.sendMessage(c("  &7#" + rank + " &f" + name
                    + " &8| &7Nv.&a" + isl.getLevel()
                    + " &8| &6" + fmt(isl.getValue()) + " pts"
                    + " &8| &f" + (isl.getMemberCount() + 1) + " membre(s)"));
                rank++;
            }
            sender.sendMessage(c("&8&m──────────────────────────────────"));
            return true;
        }
        if (sub.equals("help")) { sendHelp(sender); return true; }

        // ── Commandes avec joueur cible ────────────────────────────────────────
        if (args.length < 2) { sendHelp(sender); return true; }

        @SuppressWarnings("deprecation") OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Island isl   = im.getOwnedIsland(target.getUniqueId());
        String tName = target.getName() != null ? target.getName() : args[1];

        switch (sub) {

            case "info" -> {
                if (isl == null) { sender.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                int completed = plugin.getChallengeManager().countCompleted(target.getUniqueId());
                int total     = plugin.getChallengeManager().getAllChallenges().size();
                sender.sendMessage(c("&8&m──────────────────────────────────"));
                sender.sendMessage(c("  &c&lÎle de &f" + tName));
                sender.sendMessage(c("&8&m──────────────────────────────────"));
                sender.sendMessage(c("  &7Niveau : &a" + isl.getLevel() + "  &7Valeur : &6" + fmt(isl.getValue()) + " pts"));
                sender.sendMessage(c("  &7Membres : &f" + (isl.getMemberCount() + 1) + "/" + (isl.getMemberSlots() + 1)));
                sender.sendMessage(c("  &7Générateur : &b" + isl.getGeneratorLevel() + "/7"));
                sender.sendMessage(c("  &7Solde perso : &e" + fmt(plugin.getEconomyManager().getBalance(target.getUniqueId())) + " $"));
                sender.sendMessage(c("  &7Banque île : &6" + fmt(isl.getBankBalance()) + " $"));
                sender.sendMessage(c("  &7Défis : &a" + completed + " &8/ &f" + total));
                sender.sendMessage(c("  &7Vol : " + (isl.hasFlyUpgrade() ? "&a✔" : "&c✗")
                    + "  &7Keep Inv : " + (isl.hasKeepInventoryUpgrade() ? "&a✔" : "&c✗")));
                sender.sendMessage(c("  &7Blocs cassés : &f" + fmt(isl.getBlocksBroken())));
                sender.sendMessage(c("&8&m──────────────────────────────────"));
            }

            case "visit" -> {
                if (!(sender instanceof Player p)) { sender.sendMessage("Joueurs uniquement."); return true; }
                if (isl == null) { sender.sendMessage(pre() + c("&cÎle introuvable.")); return true; }
                p.teleport(isl.getHome());
                sender.sendMessage(pre() + c("&aTéléporté sur l'île de &e" + tName));
            }

            case "setlevel" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /isadmin setlevel <joueur> <niveau>")); return true; }
                if (isl == null) { sender.sendMessage(pre() + c("&cÎle introuvable.")); return true; }
                try {
                    int lvl = Integer.parseInt(args[2]);
                    isl.setLevel(lvl); im.saveAll();
                    sender.sendMessage(pre() + c("&aNiveau de &e" + tName + " &afixé à &e" + lvl));
                } catch (NumberFormatException e) { sender.sendMessage(pre() + c("&cNombre invalide.")); }
            }

            case "setbalance" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /isadmin setbalance <joueur> <montant>")); return true; }
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
                    sender.sendMessage(pre() + c("&aSolde de &e" + tName + " &afixé à &e" + fmt(amount) + " $"));
                } catch (NumberFormatException e) { sender.sendMessage(pre() + c("&cNombre invalide.")); }
            }

            case "setgen" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /isadmin setgen <joueur> <0-7>")); return true; }
                if (isl == null) { sender.sendMessage(pre() + c("&cÎle introuvable.")); return true; }
                try {
                    int gen = Math.max(0, Math.min(7, Integer.parseInt(args[2])));
                    isl.setGeneratorLevel(gen); im.saveAll();
                    sender.sendMessage(pre() + c("&aGénérateur de &e" + tName + " &afixé au niveau &e" + gen));
                    Player online = Bukkit.getPlayer(target.getUniqueId());
                    if (online != null) online.sendMessage(c("&8[&cAdmin&8] &7Ton générateur a été fixé au niveau &b" + gen + "&7 par un administrateur."));
                } catch (NumberFormatException e) { sender.sendMessage(pre() + c("&cNiveau invalide (0-7).")); }
            }

            case "give" -> {
                if (args.length < 3) { sender.sendMessage(pre() + c("&cUsage : /isadmin give <joueur> <fly|keepinv|memberslot>")); return true; }
                if (isl == null) { sender.sendMessage(pre() + c("&cÎle introuvable.")); return true; }
                switch (args[2].toLowerCase()) {
                    case "fly" -> {
                        isl.setFlyUpgrade(true); im.saveAll();
                        sender.sendMessage(pre() + c("&aUpgrade &e✈ Vol &aofferte à &e" + tName + "&a."));
                        Player online = Bukkit.getPlayer(target.getUniqueId());
                        if (online != null) online.sendMessage(c("&8[&cAdmin&8] &a✈ L'amélioration &eVol &aa été accordée à ton île !"));
                    }
                    case "keepinv" -> {
                        isl.setKeepInventoryUpgrade(true); im.saveAll();
                        sender.sendMessage(pre() + c("&aUpgrade &e🛡 Keep Inventory &aofferte à &e" + tName + "&a."));
                        Player online = Bukkit.getPlayer(target.getUniqueId());
                        if (online != null) online.sendMessage(c("&8[&cAdmin&8] &a🛡 L'amélioration &eKeep Inventory &aa été accordée à ton île !"));
                    }
                    case "memberslot" -> {
                        int cur = isl.getMemberSlotsUpgrade();
                        if (cur >= 3) { sender.sendMessage(pre() + c("&cDéjà au maximum (3 upgrades).")); return true; }
                        isl.setMemberSlotsUpgrade(cur + 1);
                        isl.setMemberSlots(isl.getMemberSlots() + 2);
                        im.saveAll();
                        sender.sendMessage(pre() + c("&aSlot membre offert à &e" + tName + " &a(total : &e" + isl.getMemberSlotsUpgrade() + "/3&a)."));
                    }
                    default -> sender.sendMessage(pre() + c("&cUpgrade inconnue. Choix : &efly, keepinv, memberslot"));
                }
            }

            case "scan" -> {
                if (isl == null) { sender.sendMessage(pre() + c("&cÎle introuvable.")); return true; }
                int radius = plugin.getConfig().getInt("island.size", 100);
                sender.sendMessage(pre() + c("&7Scan en cours..."));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    long val = plugin.getLevelManager().scanIsland(isl, radius);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        im.saveAll();
                        sender.sendMessage(pre() + c("&aScan terminé : valeur &e" + fmt(val) + " &a| niveau &e" + isl.getLevel()));
                    });
                });
            }

            case "reset" -> {
                if (isl == null) { sender.sendMessage(pre() + c("&cÎle introuvable.")); return true; }
                im.deleteIsland(target.getUniqueId());
                sender.sendMessage(pre() + c("&aÎle de &e" + tName + " &asupprimée."));
                Player online = Bukkit.getPlayer(target.getUniqueId());
                if (online != null) online.sendMessage(c("&cTon île a été réinitialisée par un administrateur."));
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(c("&8&m───────────────────────────────────"));
        s.sendMessage(c("  &c&l⚙ Admin Skyblock — Commandes"));
        s.sendMessage(c("&8&m───────────────────────────────────"));
        s.sendMessage(c("  &e/isadmin list &8— &7Lister toutes les îles"));
        s.sendMessage(c("  &e/isadmin info &8<joueur> &8— &7Infos complètes"));
        s.sendMessage(c("  &e/isadmin visit &8<joueur> &8— &7Aller sur l'île"));
        s.sendMessage(c("  &e/isadmin setlevel &8<joueur> &8<n> &8— &7Fixer le niveau"));
        s.sendMessage(c("  &e/isadmin setgen &8<joueur> &8<0-7> &8— &7Fixer le générateur"));
        s.sendMessage(c("  &e/isadmin setbalance &8<joueur> &8<n> &8— &7Fixer le solde"));
        s.sendMessage(c("  &e/isadmin give &8<joueur> &8<fly|keepinv|memberslot>"));
        s.sendMessage(c("  &e/isadmin scan &8<joueur> &8— &7Scanner l'île"));
        s.sendMessage(c("  &e/isadmin reset &8<joueur> &8— &7Supprimer l'île"));
        s.sendMessage(c("&8&m───────────────────────────────────"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("help"))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        if (args.length == 3 && args[0].equalsIgnoreCase("give"))
            return List.of("fly", "keepinv", "memberslot").stream()
                .filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        return List.of();
    }
}
