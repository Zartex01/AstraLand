package com.astraland.pvpfactions.commands;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.FactionManager;
import com.astraland.pvpfactions.models.Faction;
import com.astraland.pvpfactions.models.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionCommand implements CommandExecutor, TabCompleter {

    private final PvpFactions plugin;

    public FactionCommand(PvpFactions plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&c&lFactions&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (args.length == 0) { sendHelp(player); return true; }

        FactionManager fm = plugin.getFactionManager();
        String sub = args[0].toLowerCase();

        switch (sub) {

            // ─── CRÉATION / SUPPRESSION ───────────────────────────────────────
            case "create" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f create <nom>")); return true; }
                String name = args[1];
                int min = plugin.getConfig().getInt("faction.min-name-length", 3);
                int max = plugin.getConfig().getInt("faction.max-name-length", 20);
                if (name.length() < min || name.length() > max) { player.sendMessage(pre() + c("&cNom invalide (" + min + "-" + max + " caractères).")); return true; }
                if (fm.hasPlayerFaction(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.already-in-faction"))); return true; }
                if (fm.factionExists(name)) { player.sendMessage(pre() + c("&cCette faction existe déjà.")); return true; }
                fm.createFaction(name, player.getUniqueId());
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.faction-created", "&aFaction &e%faction% &acréée !").replace("%faction%", name)));
            }
            case "disband" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                String fname = f.getName();
                broadcast(f, pre() + c("&cLa faction &e" + fname + " &ca été dissoute."));
                fm.disbandFaction(f);
            }

            // ─── TAG / DESCRIPTION / MOTD / OPEN ─────────────────────────────
            case "tag" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f tag <tag>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                int maxTag = plugin.getConfig().getInt("faction.max-tag-length", 5);
                if (args[1].length() > maxTag) { player.sendMessage(pre() + c("&cTag trop long (max " + maxTag + " caractères).")); return true; }
                f.setTag(args[1].toUpperCase());
                fm.saveAll();
                player.sendMessage(pre() + c("&aTag de faction défini : &e[" + f.getTag() + "]"));
            }
            case "desc" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f desc <description>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                String desc = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                f.setDescription(desc);
                fm.saveAll();
                player.sendMessage(pre() + c("&aDescription mise à jour."));
            }
            case "motd" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                if (args.length < 2) {
                    player.sendMessage(pre() + c("&7MOTD actuel : &f" + (f.getMotd() != null ? f.getMotd() : "&8Aucun")));
                    return true;
                }
                String motd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                f.setMotd(motd);
                fm.saveAll();
                player.sendMessage(pre() + c("&aMessage du jour mis à jour."));
                broadcast(f, pre() + c("&7[MOTD] &f" + motd));
            }
            case "open" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                f.setOpen(!f.isOpen());
                fm.saveAll();
                player.sendMessage(pre() + c("&aFaction " + (f.isOpen() ? "&aOUVERTE &7(tout le monde peut rejoindre)" : "&cFERMÉE &7(sur invitation uniquement)")));
                broadcast(f, pre() + c("&7La faction est maintenant " + (f.isOpen() ? "&aOUVERTE" : "&cFERMÉE")));
            }

            // ─── MEMBRES ──────────────────────────────────────────────────────
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f invite <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.player-not-found"))); return true; }
                if (fm.hasPlayerFaction(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur est déjà dans une faction.")); return true; }
                if (f.getMembers().size() >= plugin.getConfig().getInt("faction.max-members", 30)) { player.sendMessage(pre() + c("&cFaction pleine.")); return true; }
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'invite dans la faction &e[" + f.getTag() + "] " + f.getName() + "&a. Utilise &e/f join " + f.getName()));
                player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a."));
                broadcast(f, pre() + c("&7" + player.getName() + " a invité &e" + target.getName() + "&7."));
            }
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f join <faction>")); return true; }
                if (fm.hasPlayerFaction(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.already-in-faction"))); return true; }
                Faction f = fm.getFaction(args[1]);
                if (f == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                if (!f.isOpen()) { player.sendMessage(pre() + c("&cCette faction est fermée. Attends une invitation.")); return true; }
                if (f.getMembers().size() >= plugin.getConfig().getInt("faction.max-members", 30)) { player.sendMessage(pre() + c("&cCette faction est pleine.")); return true; }
                fm.joinFaction(f, player.getUniqueId());
                player.sendMessage(pre() + c("&aRejoint la faction &e[" + f.getTag() + "] " + f.getName() + "&a !"));
                if (f.getMotd() != null) player.sendMessage(pre() + c("&7[MOTD] &f" + f.getMotd()));
                broadcast(f, pre() + c("&e" + player.getName() + " &aa rejoint la faction !"));
            }
            case "leave" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu es chef ! Utilise &e/f disband &cou &e/f setowner <joueur> &cpuis /f leave.")); return true; }
                broadcast(f, pre() + c("&e" + player.getName() + " &ca quitté la faction."));
                fm.leaveFaction(f, player.getUniqueId());
                player.sendMessage(pre() + c("&aQuitté la faction &e" + f.getName() + "&a."));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f kick <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                UUID targetId = target != null ? target.getUniqueId() : null;
                if (targetId == null || !f.isMember(targetId)) { player.sendMessage(pre() + c("&cCe joueur n'est pas dans ta faction.")); return true; }
                if (f.isLeader(targetId)) { player.sendMessage(pre() + c("&cTu ne peux pas expulser le chef.")); return true; }
                if (f.isOfficer(targetId) && !f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le chef peut expulser un officier.")); return true; }
                fm.leaveFaction(f, targetId);
                if (target != null) target.sendMessage(pre() + c("&cTu as été expulsé de la faction &e" + f.getName() + "&c."));
                broadcast(f, pre() + c("&e" + args[1] + " &ca été expulsé par &e" + player.getName() + "&c."));
            }
            case "promote" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f promote <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                f.promote(target.getUniqueId());
                fm.saveAll();
                broadcast(f, pre() + c("&e" + target.getName() + " &aest maintenant Officier !"));
            }
            case "demote" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f demote <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                f.demote(target.getUniqueId());
                fm.saveAll();
                broadcast(f, pre() + c("&e" + target.getName() + " &aest maintenant Membre."));
            }
            case "setowner" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f setowner <joueur>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                f.getMembers().put(player.getUniqueId(), FactionRole.OFFICER);
                f.getMembers().put(target.getUniqueId(), FactionRole.LEADER);
                f.setLeader(target.getUniqueId());
                fm.saveAll();
                broadcast(f, pre() + c("&e" + target.getName() + " &aest le nouveau chef de la faction !"));
                player.sendMessage(pre() + c("&aLeadership transféré à &e" + target.getName() + "&a."));
            }

            // ─── TERRITOIRE ───────────────────────────────────────────────────
            case "claim" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                if (f.getClaims().size() >= f.getMaxClaims()) { player.sendMessage(pre() + c("&cClaims maximum atteint (&e" + f.getMaxClaims() + "&c). Augmente ta puissance.")); return true; }
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                Faction existing = fm.getFactionByClaim(chunk);
                if (existing != null) { player.sendMessage(pre() + c("&cCe chunk appartient déjà à &e" + existing.getName() + "&c.")); return true; }
                f.addClaim(chunk);
                fm.saveAll();
                player.sendMessage(pre() + c("&aChunk claim ! &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
            }
            case "unclaim" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                if (!f.hasClaim(chunk)) { player.sendMessage(pre() + c("&cTu n'as pas claim ce chunk.")); return true; }
                f.removeClaim(chunk);
                fm.saveAll();
                player.sendMessage(pre() + c("&aChunk unclaim. &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
            }
            case "unclaimall" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                int count = f.getClaims().size();
                f.getClaims().clear();
                fm.saveAll();
                player.sendMessage(pre() + c("&a" + count + " chunk(s) unclaim."));
                broadcast(f, pre() + c("&e" + player.getName() + " &aa unclaim tout le territoire (&e" + count + " chunks&7)."));
            }
            case "autoclaim" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                fm.toggleAutoclaim(player.getUniqueId());
                boolean on = fm.isAutoclaiming(player.getUniqueId());
                player.sendMessage(pre() + c("&aAuto-claim : &e" + (on ? "ACTIVÉ &7(marche pour claim automatiquement)" : "DÉSACTIVÉ")));
            }

            // ─── HOME ─────────────────────────────────────────────────────────
            case "sethome" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                f.setHome(player.getLocation());
                fm.saveAll();
                player.sendMessage(pre() + c("&aHome de faction défini ici."));
            }
            case "home" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (f.getHome() == null) { player.sendMessage(pre() + c("&cAucun home défini. Utilise &e/f sethome&c.")); return true; }
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.teleporting")));
                final Location dest = f.getHome().clone();
                final Location startLoc = player.getLocation().clone();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && player.getLocation().distanceSquared(startLoc) < 1) player.teleport(dest);
                    else if (player.isOnline()) player.sendMessage(pre() + c("&cTéléportation annulée : tu as bougé !"));
                }, 60L);
            }

            // ─── WARPS ────────────────────────────────────────────────────────
            case "setwarp" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f setwarp <nom>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                int maxWarps = plugin.getConfig().getInt("faction.max-warps", 5);
                if (f.getWarps().size() >= maxWarps && !f.getWarps().containsKey(args[1].toLowerCase())) {
                    player.sendMessage(pre() + c("&cWarps maximum atteint (&e" + maxWarps + "&c).")); return true;
                }
                f.getWarps().put(args[1].toLowerCase(), player.getLocation());
                fm.saveAll();
                player.sendMessage(pre() + c("&aWarp &e" + args[1] + " &acréé. &7(" + f.getWarps().size() + "/" + maxWarps + ")"));
            }
            case "warp" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f warp <nom>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                Location warp = f.getWarps().get(args[1].toLowerCase());
                if (warp == null) { player.sendMessage(pre() + c("&cWarp &e" + args[1] + " &cintrouvable. Utilise &e/f warps &cpour la liste.")); return true; }
                player.sendMessage(pre() + c("&aTéléportation au warp &e" + args[1] + "&a..."));
                final Location dest = warp.clone();
                Bukkit.getScheduler().runTaskLater(plugin, () -> { if (player.isOnline()) player.teleport(dest); }, 20L);
            }
            case "warps" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (f.getWarps().isEmpty()) { player.sendMessage(pre() + c("&7Aucun warp défini. Utilise &e/f setwarp <nom>&7.")); return true; }
                player.sendMessage(c("&6=== Warps de &e" + f.getName() + " &6==="));
                f.getWarps().forEach((name, loc) -> player.sendMessage(c("&e" + name + " &7- " + loc.getWorld().getName() + " " + (int)loc.getX() + " " + (int)loc.getY() + " " + (int)loc.getZ())));
            }
            case "delwarp" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f delwarp <nom>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                if (f.getWarps().remove(args[1].toLowerCase()) == null) { player.sendMessage(pre() + c("&cWarp introuvable.")); return true; }
                fm.saveAll();
                player.sendMessage(pre() + c("&aWarp &e" + args[1] + " &asupprimé."));
            }

            // ─── RELATIONS ────────────────────────────────────────────────────
            case "ally" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f ally <faction>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                if (target.getName().equalsIgnoreCase(f.getName())) { player.sendMessage(pre() + c("&cTu ne peux pas t'allier à toi-même.")); return true; }
                f.getAllies().add(target.getName().toLowerCase());
                f.getEnemies().remove(target.getName().toLowerCase());
                fm.saveAll();
                player.sendMessage(pre() + c("&aAllié avec &e" + target.getName() + "&a."));
                broadcast(target, pre() + c("&a[" + f.getTag() + "] " + f.getName() + " &avous a déclaré allié."));
            }
            case "enemy" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f enemy <faction>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                f.getEnemies().add(target.getName().toLowerCase());
                f.getAllies().remove(target.getName().toLowerCase());
                fm.saveAll();
                player.sendMessage(pre() + c("&cEnnemi déclaré avec &e" + target.getName() + "&c."));
                broadcast(target, pre() + c("&c[" + f.getTag() + "] " + f.getName() + " &cvous a déclaré ennemi !"));
            }
            case "neutral" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f neutral <faction>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                f.getAllies().remove(target.getName().toLowerCase());
                f.getEnemies().remove(target.getName().toLowerCase());
                fm.saveAll();
                player.sendMessage(pre() + c("&7Neutre avec &e" + target.getName() + "&7."));
            }

            // ─── INFORMATION ──────────────────────────────────────────────────
            case "info" -> {
                Faction f = args.length > 1 ? fm.getFaction(args[1]) : fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                player.sendMessage(c("&8&m-=-=- &6[&e" + f.getTag() + "&6] " + f.getName() + " &8&m-=-=-"));
                player.sendMessage(c("&7Description : &f" + f.getDescription()));
                if (f.getMotd() != null) player.sendMessage(c("&7MOTD : &f" + f.getMotd()));
                player.sendMessage(c("&7Statut : " + (f.isOpen() ? "&aOuverte" : "&cFermée")));
                player.sendMessage(c("&7Chef : &6" + getPlayerName(f.getLeader())));
                long officers = f.getMembers().values().stream().filter(r -> r == FactionRole.OFFICER).count();
                player.sendMessage(c("&7Membres : &e" + f.getMembers().size() + " &7(Officiers : &e" + officers + "&7)"));
                player.sendMessage(c("&7Puissance : &e" + String.format("%.1f", f.getPower())));
                player.sendMessage(c("&7Territoire : &e" + f.getClaims().size() + "/" + f.getMaxClaims() + " chunks"));
                player.sendMessage(c("&7Warps : &e" + f.getWarps().size() + "/" + plugin.getConfig().getInt("faction.max-warps", 5)));
                if (!f.getAllies().isEmpty()) player.sendMessage(c("&7Alliés : &a" + String.join(", ", f.getAllies())));
                if (!f.getEnemies().isEmpty()) player.sendMessage(c("&7Ennemis : &c" + String.join(", ", f.getEnemies())));
                int bounty = plugin.getBountyManager().getTotalBounty(f.getLeader());
                if (bounty > 0) player.sendMessage(c("&7Prime chef : &c" + bounty + " &7pièces"));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "who" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f who <joueur>")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.player-not-found"))); return true; }
                Faction f = fm.getPlayerFaction(target.getUniqueId());
                int kills = plugin.getStatsManager().getKills(target.getUniqueId());
                int deaths = plugin.getStatsManager().getDeaths(target.getUniqueId());
                int streak = plugin.getStatsManager().getCurrentStreak(target.getUniqueId());
                int bounty = plugin.getBountyManager().getTotalBounty(target.getUniqueId());
                player.sendMessage(c("&8&m--- &7Profil de &e" + target.getName() + " &8&m---"));
                player.sendMessage(c("&7Faction : " + (f != null ? "&e[" + f.getTag() + "] " + f.getName() : "&8Aucune")));
                if (f != null) player.sendMessage(c("&7Rôle : &e" + f.getMembers().getOrDefault(target.getUniqueId(), FactionRole.MEMBER).getDisplay()));
                player.sendMessage(c("&7Kills : &a" + kills + " &7/ Morts : &c" + deaths + " &7/ K/D : &6" + String.format("%.2f", plugin.getStatsManager().getKD(target.getUniqueId()))));
                player.sendMessage(c("&7Série actuelle : &6" + streak));
                if (bounty > 0) player.sendMessage(c("&7Prime : &c" + bounty + " &7pièces"));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "list" -> {
                player.sendMessage(c("&6&l=== Factions (" + fm.getAllFactions().size() + ") ==="));
                fm.getAllFactions().forEach(f -> player.sendMessage(c(
                    "&e[" + f.getTag() + "] &f" + f.getName() +
                    " &7" + f.getMembers().size() + " membres" +
                    " &8| &7Puissance : &e" + String.format("%.0f", f.getPower()) +
                    (f.isOpen() ? " &a[O]" : ""))));
            }
            case "top" -> {
                List<Faction> sorted = new ArrayList<>(fm.getAllFactions());
                sorted.sort((a, b) -> b.getMembers().size() - a.getMembers().size());
                player.sendMessage(c("&6&l=== Top Factions ==="));
                for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                    Faction f = sorted.get(i);
                    String medal = i == 0 ? "&6" : i == 1 ? "&7" : i == 2 ? "&c" : "&f";
                    player.sendMessage(c(medal + "#" + (i+1) + " &e[" + f.getTag() + "] " + f.getName() + " &7- &f" + f.getMembers().size() + " membres &7- Puissance &e" + String.format("%.0f", f.getPower())));
                }
            }

            // ─── PUISSANCE ────────────────────────────────────────────────────
            case "power" -> {
                if (args.length > 1) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.player-not-found"))); return true; }
                    Faction f = fm.getPlayerFaction(target.getUniqueId());
                    if (f == null) { player.sendMessage(pre() + c("&e" + target.getName() + " &cn'a pas de faction.")); return true; }
                    player.sendMessage(c("&7Puissance de &e" + target.getName() + " &7(faction &e" + f.getName() + "&7) : &6" + String.format("%.1f", f.getPower()) + "/" + f.getMaxClaims()));
                } else {
                    Faction f = fm.getPlayerFaction(player.getUniqueId());
                    if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                    player.sendMessage(c("&7Puissance de &e" + f.getName() + " &7: &6" + String.format("%.1f", f.getPower()) + " &7(Claims max : &e" + f.getMaxClaims() + "&7)"));
                }
            }

            // ─── MAP ──────────────────────────────────────────────────────────
            case "map" -> {
                Faction myFaction = fm.getPlayerFaction(player.getUniqueId());
                int cx = player.getLocation().getChunk().getX();
                int cz = player.getLocation().getChunk().getZ();
                player.sendMessage(c("&8&m-=-=- &7Carte du territoire &8&m-=-=-"));
                for (int z = cz - 3; z <= cz + 3; z++) {
                    StringBuilder row = new StringBuilder("&8| ");
                    for (int x = cx - 8; x <= cx + 8; x++) {
                        if (x == cx && z == cz) { row.append("&f+"); }
                        else {
                            org.bukkit.Chunk chunk = player.getWorld().getChunkAt(x, z);
                            Faction owner = fm.getFactionByClaim(chunk);
                            if (owner == null) { row.append("&8-"); }
                            else if (myFaction != null && owner.getName().equalsIgnoreCase(myFaction.getName())) { row.append("&aF"); }
                            else if (myFaction != null && myFaction.getAllies().contains(owner.getName().toLowerCase())) { row.append("&bA"); }
                            else if (myFaction != null && myFaction.getEnemies().contains(owner.getName().toLowerCase())) { row.append("&cE"); }
                            else { row.append("&eO"); }
                        }
                    }
                    row.append(" &8|");
                    player.sendMessage(c(row.toString()));
                }
                player.sendMessage(c("&a■ F&7=Ta faction  &b■ A&7=Allié  &c■ E&7=Ennemi  &e■ O&7=Autre  &f+&7=Toi  &8■ -&7=Libre"));
            }

            // ─── STUCK ────────────────────────────────────────────────────────
            case "stuck" -> {
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                Faction chunkOwner = fm.getFactionByClaim(chunk);
                Faction myFaction = fm.getPlayerFaction(player.getUniqueId());
                boolean isEnemy = chunkOwner != null && (myFaction == null || !myFaction.getName().equalsIgnoreCase(chunkOwner.getName()));
                if (!isEnemy) { player.sendMessage(pre() + c("&cTu n'es pas bloqué en territoire ennemi.")); return true; }
                player.sendMessage(pre() + c("&eTéléportation aléatoire dans &e30 secondes&7..."));
                final Location startLoc = player.getLocation().clone();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (player.getLocation().distanceSquared(startLoc) > 4) { player.sendMessage(pre() + c("&cStuck annulé : tu as bougé !")); return; }
                    java.util.Random rand = new java.util.Random();
                    int rx = rand.nextInt(400) - 200;
                    int rz = rand.nextInt(400) - 200;
                    int ry = player.getWorld().getHighestBlockYAt(rx, rz) + 1;
                    player.teleport(new Location(player.getWorld(), rx, ry, rz));
                    player.sendMessage(pre() + c("&aTéléporté hors du territoire ennemi."));
                }, 600L);
            }

            // ─── COMMUNICATION ────────────────────────────────────────────────
            case "chat" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                fm.toggleFactionChat(player.getUniqueId());
                boolean on = fm.isFactionChat(player.getUniqueId());
                player.sendMessage(pre() + c("&aChat faction : &e" + (on ? "ACTIVÉ &7(seuls tes membres te lisent)" : "DÉSACTIVÉ")));
            }
            case "announce" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f announce <message>")); return true; }
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                broadcast(f, c("&6&l[Annonce] &e[" + f.getTag() + "] &6" + player.getName() + " &7: &f" + msg));
            }
            case "coords" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction"))); return true; }
                Location loc = player.getLocation();
                String coordMsg = c("&e" + player.getName() + " &7est en &f" + loc.getWorld().getName() + " &7X:&e" + (int)loc.getX() + " &7Y:&e" + (int)loc.getY() + " &7Z:&e" + (int)loc.getZ());
                broadcast(f, pre() + coordMsg);
                player.sendMessage(pre() + c("&aCoordonnées partagées en chat faction."));
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(c("&6&l=== /faction — Aide ==="));
        p.sendMessage(c("&e/f create/disband &7- Créer/Dissoudre"));
        p.sendMessage(c("&e/f tag/desc/motd/open &7- Personnaliser la faction"));
        p.sendMessage(c("&e/f invite/join/leave/kick &7- Gestion des membres"));
        p.sendMessage(c("&e/f promote/demote/setowner &7- Rôles"));
        p.sendMessage(c("&e/f sethome/home &7- Home faction"));
        p.sendMessage(c("&e/f setwarp/warp/warps/delwarp &7- Warps faction"));
        p.sendMessage(c("&e/f claim/unclaim/unclaimall/autoclaim &7- Territoire"));
        p.sendMessage(c("&e/f ally/enemy/neutral &7- Relations"));
        p.sendMessage(c("&e/f info/who/list/top/power/map &7- Informations"));
        p.sendMessage(c("&e/f chat/announce/coords &7- Communication"));
        p.sendMessage(c("&e/f stuck &7- Téléportation hors territoire ennemi"));
    }

    private void broadcast(Faction f, String msg) {
        f.getMembers().keySet().forEach(uuid -> { Player p = Bukkit.getPlayer(uuid); if (p != null) p.sendMessage(msg); });
    }

    private String getPlayerName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) return p.getName();
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : uuid.toString().substring(0, 8);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList(
            "create","disband","tag","desc","motd","open",
            "invite","join","leave","kick","promote","demote","setowner",
            "sethome","home","setwarp","warp","warps","delwarp",
            "claim","unclaim","unclaimall","autoclaim",
            "ally","enemy","neutral",
            "info","who","list","top","power","map",
            "chat","announce","coords","stuck");
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (List.of("info","ally","enemy","neutral","join","delwarp","warp").contains(sub)) {
                return plugin.getFactionManager().getAllFactions().stream().map(Faction::getName).toList();
            }
            if (List.of("invite","kick","promote","demote","setowner","who","power").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        return new ArrayList<>();
    }
}
