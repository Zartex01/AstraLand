package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.FactionManager;
import com.astraland.pvpfactions.models.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FactionCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public FactionCommand(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String prefix() { return c(plugin.getConfig().getString("prefix", "&8[&c&lFactions&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (args.length == 0) { sendHelp(player); return true; }

        FactionManager fm = plugin.getFactionManager();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f create <nom>")); return true; }
                String name = args[1];
                int min = plugin.getConfig().getInt("faction.min-name-length", 3);
                int max = plugin.getConfig().getInt("faction.max-name-length", 20);
                if (name.length() < min || name.length() > max) { player.sendMessage(prefix() + c("&cNom invalide (" + min + "-" + max + " caractères).")); return true; }
                if (fm.hasPlayerFaction(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.already-in-faction", "&cTu es déjà dans une faction."))); return true; }
                if (fm.factionExists(name)) { player.sendMessage(prefix() + c("&cCette faction existe déjà.")); return true; }
                fm.createFaction(name, player.getUniqueId());
                player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.faction-created", "&aFaction &e%faction% &acréée !").replace("%faction%", name)));
            }
            case "disband" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                String fname = f.getName();
                fm.disbandFaction(f);
                player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.faction-disbanded", "&cFaction &e%faction% &cdissoute.").replace("%faction%", fname)));
            }
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f invite <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.player-not-found"))); return true; }
                if (fm.hasPlayerFaction(target.getUniqueId())) { player.sendMessage(prefix() + c("&cCe joueur est déjà dans une faction.")); return true; }
                target.sendMessage(prefix() + c("&e" + player.getName() + " &at'invite dans la faction &e" + f.getName() + "&a. Utilise &e/f join " + f.getName() + " &apour rejoindre."));
                player.sendMessage(prefix() + c("&aInvitation envoyée à &e" + target.getName() + "&a."));
            }
            case "join" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f join <faction>")); return true; }
                if (fm.hasPlayerFaction(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.already-in-faction"))); return true; }
                Faction f = fm.getFaction(args[1]);
                if (f == null) { player.sendMessage(prefix() + c("&cFaction introuvable.")); return true; }
                int maxMembers = plugin.getConfig().getInt("faction.max-members", 30);
                if (f.getMembers().size() >= maxMembers) { player.sendMessage(prefix() + c("&cCette faction est pleine.")); return true; }
                fm.joinFaction(f, player.getUniqueId());
                player.sendMessage(prefix() + c("&aRejoint la faction &e" + f.getName() + "&a !"));
                broadcast(f, prefix() + c("&e" + player.getName() + " &aa rejoint la faction !"));
            }
            case "leave" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (f.isLeader(player.getUniqueId())) { player.sendMessage(prefix() + c("&cTu es chef ! Utilise &e/f disband &cou &e/f promote <joueur> &cpuis /f leave.")); return true; }
                fm.leaveFaction(f, player.getUniqueId());
                player.sendMessage(prefix() + c("&aQuitté la faction &e" + f.getName() + "&a."));
                broadcast(f, prefix() + c("&e" + player.getName() + " &ca quitté la faction."));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f kick <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                UUID targetId = target != null ? target.getUniqueId() : null;
                if (targetId == null || !f.isMember(targetId)) { player.sendMessage(prefix() + c("&cCe joueur n'est pas dans ta faction.")); return true; }
                if (f.isLeader(targetId)) { player.sendMessage(prefix() + c("&cTu ne peux pas expulser le chef.")); return true; }
                fm.leaveFaction(f, targetId);
                if (target != null) target.sendMessage(prefix() + c("&cTu as été expulsé de la faction &e" + f.getName() + "&c."));
                player.sendMessage(prefix() + c("&e" + args[1] + " &aexpulsé de la faction."));
            }
            case "promote" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f promote <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(prefix() + c("&cJoueur introuvable dans ta faction.")); return true; }
                f.promote(target.getUniqueId());
                plugin.getFactionManager().saveAll();
                player.sendMessage(prefix() + c("&e" + target.getName() + " &apromue Officier."));
                target.sendMessage(prefix() + c("&aTu as été promu Officier dans &e" + f.getName() + "&a."));
            }
            case "demote" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f demote <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(prefix() + c("&cJoueur introuvable dans ta faction.")); return true; }
                f.demote(target.getUniqueId());
                plugin.getFactionManager().saveAll();
                player.sendMessage(prefix() + c("&e" + target.getName() + " &arétrograder à Membre."));
                target.sendMessage(prefix() + c("&cTu as été rétrogradé à Membre dans &e" + f.getName() + "&c."));
            }
            case "sethome" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                f.setHome(player.getLocation());
                fm.saveAll();
                player.sendMessage(prefix() + c("&aHome de faction défini ici."));
            }
            case "home" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (f.getHome() == null) { player.sendMessage(prefix() + c("&cAucun home défini. Utilise &e/f sethome&c.")); return true; }
                player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.teleporting", "&aTéléportation dans 3s...")));
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(f.getHome()), 60L);
            }
            case "claim" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                if (f.getClaims().size() >= f.getMaxClaims()) { player.sendMessage(prefix() + c("&cClaims maximum atteint (&e" + f.getMaxClaims() + "&c). Augmente ta puissance.")); return true; }
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                if (fm.getFactionByClaim(chunk) != null) { player.sendMessage(prefix() + c("&cCe chunk est déjà claim.")); return true; }
                f.addClaim(chunk);
                fm.saveAll();
                player.sendMessage(prefix() + c("&aChunk claim ! &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
            }
            case "unclaim" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                if (!f.hasClaim(chunk)) { player.sendMessage(prefix() + c("&cTu n'as pas claim ce chunk.")); return true; }
                f.removeClaim(chunk);
                fm.saveAll();
                player.sendMessage(prefix() + c("&aChunk unclaim."));
            }
            case "ally" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f ally <faction>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(prefix() + c("&cFaction introuvable.")); return true; }
                f.getAllies().add(target.getName().toLowerCase());
                f.getEnemies().remove(target.getName().toLowerCase());
                fm.saveAll();
                player.sendMessage(prefix() + c("&aAllié avec &e" + target.getName() + "&a."));
                broadcast(target, prefix() + c("&e" + f.getName() + " &avous a déclaré allié."));
            }
            case "enemy" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f enemy <faction>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(prefix() + c("&cFaction introuvable.")); return true; }
                f.getEnemies().add(target.getName().toLowerCase());
                f.getAllies().remove(target.getName().toLowerCase());
                fm.saveAll();
                player.sendMessage(prefix() + c("&cEnnemis avec &e" + target.getName() + "&c."));
                broadcast(target, prefix() + c("&c" + f.getName() + " &cvous a déclaré ennemi !"));
            }
            case "neutral" -> {
                if (args.length < 2) { player.sendMessage(prefix() + c("&cUsage : /f neutral <faction>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(prefix() + c("&cFaction introuvable.")); return true; }
                f.getAllies().remove(target.getName().toLowerCase());
                f.getEnemies().remove(target.getName().toLowerCase());
                fm.saveAll();
                player.sendMessage(prefix() + c("&7Neutre avec &e" + target.getName() + "&7."));
            }
            case "info" -> {
                Faction f = args.length > 1 ? fm.getFaction(args[1]) : fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c("&cFaction introuvable.")); return true; }
                player.sendMessage(c("&8&m---------------------"));
                player.sendMessage(c("&6&lFaction : &e" + f.getName()));
                player.sendMessage(c("&7Description : &f" + f.getDescription()));
                player.sendMessage(c("&7Chef : &e" + getPlayerName(f.getLeader())));
                player.sendMessage(c("&7Membres : &e" + f.getMembers().size()));
                player.sendMessage(c("&7Puissance : &e" + String.format("%.1f", f.getPower())));
                player.sendMessage(c("&7Claims : &e" + f.getClaims().size() + "/" + f.getMaxClaims()));
                player.sendMessage(c("&7Alliés : &a" + String.join(", ", f.getAllies())));
                player.sendMessage(c("&7Ennemis : &c" + String.join(", ", f.getEnemies())));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "list" -> {
                player.sendMessage(c("&6=== Factions ==="));
                fm.getAllFactions().forEach(f ->
                    player.sendMessage(c("&e" + f.getName() + " &7- &f" + f.getMembers().size() + " membres - Puissance : " + String.format("%.1f", f.getPower()))));
            }
            case "top" -> {
                List<Faction> sorted = new ArrayList<>(fm.getAllFactions());
                sorted.sort((a, b) -> b.getMembers().size() - a.getMembers().size());
                player.sendMessage(c("&6=== Top Factions ==="));
                for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                    Faction f = sorted.get(i);
                    player.sendMessage(c("&e#" + (i + 1) + " &f" + f.getName() + " &7- &f" + f.getMembers().size() + " membres"));
                }
            }
            case "chat" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(prefix() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                fm.toggleFactionChat(player.getUniqueId());
                boolean on = fm.isFactionChat(player.getUniqueId());
                player.sendMessage(prefix() + c("&aChat faction : &e" + (on ? "ACTIVÉ" : "DÉSACTIVÉ")));
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(c("&6&l=== Commandes /faction ==="));
        p.sendMessage(c("&e/f create <nom> &7- Créer une faction"));
        p.sendMessage(c("&e/f disband &7- Dissoudre sa faction"));
        p.sendMessage(c("&e/f invite <joueur> &7- Inviter un joueur"));
        p.sendMessage(c("&e/f join <faction> &7- Rejoindre une faction"));
        p.sendMessage(c("&e/f leave &7- Quitter sa faction"));
        p.sendMessage(c("&e/f kick <joueur> &7- Expulser un joueur"));
        p.sendMessage(c("&e/f promote/demote <joueur> &7- Promouvoir/rétrograder"));
        p.sendMessage(c("&e/f sethome / home &7- Définir/aller au home"));
        p.sendMessage(c("&e/f claim / unclaim &7- Claim/unclaim un chunk"));
        p.sendMessage(c("&e/f ally/enemy/neutral <faction> &7- Relations"));
        p.sendMessage(c("&e/f info [faction] &7- Infos sur une faction"));
        p.sendMessage(c("&e/f list / top &7- Lister les factions"));
        p.sendMessage(c("&e/f chat &7- Basculer le chat faction"));
    }

    private void broadcast(Faction f, String msg) {
        f.getMembers().keySet().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(msg);
        });
    }

    private String getPlayerName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : Bukkit.getOfflinePlayer(uuid).getName() != null ? Bukkit.getOfflinePlayer(uuid).getName() : uuid.toString().substring(0, 8);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "disband", "invite", "join", "leave", "kick", "promote", "demote",
                "sethome", "home", "claim", "unclaim", "ally", "enemy", "neutral", "info", "list", "top", "chat");
        return new ArrayList<>();
    }
}
