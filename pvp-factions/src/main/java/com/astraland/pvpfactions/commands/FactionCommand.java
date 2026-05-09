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
                if (name.length() < min || name.length() > max) { player.sendMessage(pre() + c("&cNom invalide (" + min + "-" + max + " car.).")); return true; }
                if (fm.hasPlayerFaction(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.already-in-faction", "&cTu es déjà dans une faction."))); return true; }
                if (fm.factionExists(name)) { player.sendMessage(pre() + c("&cCette faction existe déjà.")); return true; }
                fm.createFaction(name, player.getUniqueId());
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.faction-created", "&aFaction &e%faction% &acréée !").replace("%faction%", name)));
            }
            case "disband" -> {
                Faction f = fm.getPlayerFaction(player.getUniqueId());
                if (f == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction", "&cTu n'as pas de faction."))); return true; }
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader", "&cChef uniquement."))); return true; }
                broadcast(f, pre() + c("&cLa faction &e" + f.getName() + " &ca été dissoute par &e" + player.getName() + "&c."));
                fm.disbandFaction(f);
            }

            // ─── PERSONNALISATION ─────────────────────────────────────────────
            case "tag" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f tag <tag>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer", "&cOfficier+ requis."))); return true; }
                int maxTag = plugin.getConfig().getInt("faction.max-tag-length", 5);
                if (args[1].length() > maxTag) { player.sendMessage(pre() + c("&cTag trop long (max " + maxTag + " car.).")); return true; }
                f.setTag(args[1].toUpperCase());
                fm.saveFaction(f);
                player.sendMessage(pre() + c("&aTag défini : &e[" + f.getTag() + "]"));
            }
            case "desc" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f desc <texte>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                f.setDescription(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                fm.saveFaction(f);
                player.sendMessage(pre() + c("&aDescription mise à jour."));
            }
            case "motd" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&7MOTD : &f" + (f.getMotd() != null ? f.getMotd() : "&8Aucun"))); return true; }
                String motd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                f.setMotd(motd);
                fm.saveFaction(f);
                player.sendMessage(pre() + c("&aMessage du jour mis à jour."));
                broadcast(f, pre() + c("&7[MOTD] &f" + motd));
            }
            case "open" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                f.setOpen(!f.isOpen());
                fm.saveFaction(f);
                broadcast(f, pre() + c("&7La faction est maintenant " + (f.isOpen() ? "&aOUVERTE" : "&cFERMÉE")));
                player.sendMessage(pre() + c("&aFaction " + (f.isOpen() ? "&aOUVERTE &7(rejoindre sans invitation)" : "&cFERMÉE &7(invitation requise)")));
            }

            // ─── MEMBRES ──────────────────────────────────────────────────────
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f invite <joueur>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.player-not-found", "&cJoueur introuvable."))); return true; }
                if (fm.hasPlayerFaction(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur est déjà dans une faction.")); return true; }
                if (f.getMembers().size() >= plugin.getConfig().getInt("faction.max-members", 30)) { player.sendMessage(pre() + c("&cFaction pleine.")); return true; }
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'invite dans &e[" + f.getTag() + "] " + f.getName() + "&a. Tape &e/f join " + f.getName()));
                player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a."));
                broadcast(f, pre() + c("&7" + player.getName() + " a invité &e" + target.getName() + "&7."));
            }
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f join <faction>")); return true; }
                if (fm.hasPlayerFaction(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.already-in-faction"))); return true; }
                Faction f = fm.getFaction(args[1]);
                if (f == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                if (!f.isOpen()) { player.sendMessage(pre() + c("&cCette faction est fermée (invitation requise).")); return true; }
                if (f.getMembers().size() >= plugin.getConfig().getInt("faction.max-members", 30)) { player.sendMessage(pre() + c("&cFaction pleine.")); return true; }
                fm.joinFaction(f, player.getUniqueId());
                player.sendMessage(pre() + c("&aRejoint &e[" + f.getTag() + "] " + f.getName() + "&a !"));
                if (f.getMotd() != null) player.sendMessage(pre() + c("&7[MOTD] &f" + f.getMotd()));
                broadcast(f, pre() + c("&e" + player.getName() + " &aa rejoint la faction !"));
            }
            case "leave" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu es chef. Utilise &e/f setowner <joueur> &cd'abord.")); return true; }
                broadcast(f, pre() + c("&e" + player.getName() + " &ca quitté la faction."));
                fm.leaveFaction(f, player.getUniqueId());
                player.sendMessage(pre() + c("&aQuitté &e" + f.getName() + "&a."));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f kick <joueur>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                if (f.isLeader(target.getUniqueId())) { player.sendMessage(pre() + c("&cImpossible d'expulser le chef.")); return true; }
                if (f.isOfficer(target.getUniqueId()) && !f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le chef peut expulser un officier.")); return true; }
                target.sendMessage(pre() + c("&cTu as été expulsé de &e" + f.getName() + "&c."));
                broadcast(f, pre() + c("&e" + target.getName() + " &ca été expulsé par &e" + player.getName() + "&c."));
                fm.leaveFaction(f, target.getUniqueId());
            }
            case "promote" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f promote <joueur>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                if (f.getMembers().get(target.getUniqueId()) != FactionRole.MEMBER) { player.sendMessage(pre() + c("&cCe joueur est déjà officier ou chef.")); return true; }
                fm.promoteMember(f, target.getUniqueId());
                broadcast(f, pre() + c("&e" + target.getName() + " &aest maintenant &6Officier &a!"));
            }
            case "demote" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f demote <joueur>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                if (f.getMembers().get(target.getUniqueId()) != FactionRole.OFFICER) { player.sendMessage(pre() + c("&cCe joueur n'est pas officier.")); return true; }
                fm.demoteMember(f, target.getUniqueId());
                broadcast(f, pre() + c("&e" + target.getName() + " &aest maintenant Membre."));
            }
            case "setowner" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f setowner <joueur>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !f.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cJoueur introuvable dans ta faction.")); return true; }
                fm.setLeader(f, player.getUniqueId(), target.getUniqueId());
                broadcast(f, pre() + c("&e" + target.getName() + " &aest le nouveau &6Chef &ade la faction !"));
                player.sendMessage(pre() + c("&aLeadership transféré à &e" + target.getName() + "&a."));
            }

            // ─── TERRITOIRE ───────────────────────────────────────────────────
            case "claim" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                if (f.getClaims().size() >= f.getMaxClaims()) { player.sendMessage(pre() + c("&cClaims max (&e" + f.getMaxClaims() + "&c). Augmente ta puissance.")); return true; }
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                Faction existing = fm.getFactionByClaim(chunk);
                if (existing != null) { player.sendMessage(pre() + c("&cChunk déjà claim par &e" + existing.getName() + "&c.")); return true; }
                fm.addClaim(f, chunk);
                player.sendMessage(pre() + c("&aChunk claim ! &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
            }
            case "unclaim" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                org.bukkit.Chunk chunk = player.getLocation().getChunk();
                if (!f.hasClaim(chunk)) { player.sendMessage(pre() + c("&cCe chunk ne t'appartient pas.")); return true; }
                fm.removeClaim(f, chunk);
                player.sendMessage(pre() + c("&aChunk unclaim. &7(" + f.getClaims().size() + "/" + f.getMaxClaims() + ")"));
            }
            case "unclaimall" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                int count = f.getClaims().size();
                fm.removeAllClaims(f);
                player.sendMessage(pre() + c("&a" + count + " chunk(s) unclaim."));
                broadcast(f, pre() + c("&e" + player.getName() + " &aa unclaim tout le territoire (&e" + count + " chunks&7)."));
            }
            case "autoclaim" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                fm.toggleAutoclaim(player.getUniqueId());
                boolean on = fm.isAutoclaiming(player.getUniqueId());
                player.sendMessage(pre() + c("&aAuto-claim : &e" + (on ? "ACTIVÉ &7(marche pour claim)" : "DÉSACTIVÉ")));
            }

            // ─── HOME ─────────────────────────────────────────────────────────
            case "sethome" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                fm.setHome(f, player.getLocation());
                player.sendMessage(pre() + c("&aHome de faction défini ici."));
            }
            case "home" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (f.getHome() == null) { player.sendMessage(pre() + c("&cAucun home. Utilise &e/f sethome&c.")); return true; }
                final Location dest = f.getHome().clone();
                final Location start = player.getLocation().clone();
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.teleporting", "&aTéléportation dans &e3s&a...")));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!player.isOnline()) return;
                    if (player.getLocation().distanceSquared(start) < 1) player.teleport(dest);
                    else player.sendMessage(pre() + c("&cTéléportation annulée : tu as bougé !"));
                }, 60L);
            }

            // ─── WARPS ────────────────────────────────────────────────────────
            case "setwarp" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f setwarp <nom>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                int maxWarps = plugin.getConfig().getInt("faction.max-warps", 5);
                String wn = args[1].toLowerCase();
                if (f.getWarps().size() >= maxWarps && !f.getWarps().containsKey(wn)) { player.sendMessage(pre() + c("&cWarps max (&e" + maxWarps + "&c) atteint.")); return true; }
                fm.setWarp(f, wn, player.getLocation());
                player.sendMessage(pre() + c("&aWarp &e" + wn + " &acréé. &7(" + f.getWarps().size() + "/" + maxWarps + ")"));
            }
            case "warp" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f warp <nom>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                Location warp = f.getWarps().get(args[1].toLowerCase());
                if (warp == null) { player.sendMessage(pre() + c("&cWarp &e" + args[1] + " &cintrouvable. &7(/f warps)")); return true; }
                player.sendMessage(pre() + c("&aTéléportation vers &e" + args[1] + "&a..."));
                final Location dest = warp.clone();
                Bukkit.getScheduler().runTaskLater(plugin, () -> { if (player.isOnline()) player.teleport(dest); }, 20L);
            }
            case "warps" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (f.getWarps().isEmpty()) { player.sendMessage(pre() + c("&7Aucun warp. Utilise &e/f setwarp <nom>&7.")); return true; }
                player.sendMessage(c("&6=== Warps de &e" + f.getName() + " &6==="));
                f.getWarps().forEach((n, loc) -> player.sendMessage(c("&e" + n + " &7- " + loc.getWorld().getName() + " X:" + (int)loc.getX() + " Y:" + (int)loc.getY() + " Z:" + (int)loc.getZ())));
            }
            case "delwarp" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f delwarp <nom>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                String wn = args[1].toLowerCase();
                if (!f.getWarps().containsKey(wn)) { player.sendMessage(pre() + c("&cWarp introuvable.")); return true; }
                fm.deleteWarp(f, wn);
                player.sendMessage(pre() + c("&aWarp &e" + wn + " &asupprimé."));
            }

            // ─── RELATIONS ────────────────────────────────────────────────────
            case "ally" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f ally <faction>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                if (target.getName().equalsIgnoreCase(f.getName())) { player.sendMessage(pre() + c("&cImpossible.")); return true; }
                fm.addAlly(f, target);
                player.sendMessage(pre() + c("&aAllié avec &e" + target.getName() + "&a."));
                broadcast(target, pre() + c("&a[" + f.getTag() + "] " + f.getName() + " &avous a déclaré allié."));
            }
            case "enemy" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f enemy <faction>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isLeader(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-leader"))); return true; }
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                fm.addEnemy(f, target);
                player.sendMessage(pre() + c("&cEnnemi déclaré avec &e" + target.getName() + "&c."));
                broadcast(target, pre() + c("&c[" + f.getTag() + "] " + f.getName() + " &cvous a déclaré ennemi !"));
            }
            case "neutral" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f neutral <faction>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                Faction target = fm.getFaction(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cFaction introuvable.")); return true; }
                fm.setNeutral(f, target);
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
                long members  = f.getMembers().values().stream().filter(r -> r == FactionRole.MEMBER).count();
                player.sendMessage(c("&7Membres : &e" + f.getMembers().size() + " &7(Chef: &61 &7| Officiers: &e" + officers + " &7| Membres: &f" + members + "&7)"));
                player.sendMessage(c("&7Puissance : &e" + String.format("%.1f", f.getPower()) + " &7| Claims : &e" + f.getClaims().size() + "/" + f.getMaxClaims()));
                player.sendMessage(c("&7Warps : &e" + f.getWarps().size() + "/" + plugin.getConfig().getInt("faction.max-warps", 5)));
                if (!f.getAllies().isEmpty())  player.sendMessage(c("&7Alliés : &a" + String.join("&7, &a", f.getAllies())));
                if (!f.getEnemies().isEmpty()) player.sendMessage(c("&7Ennemis : &c" + String.join("&7, &c", f.getEnemies())));
                int bounty = plugin.getBountyManager().getTotalBounty(f.getLeader());
                if (bounty > 0) player.sendMessage(c("&7Prime sur le chef : &c" + bounty + " &7pièces"));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "who" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f who <joueur>")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.player-not-found"))); return true; }
                Faction f = fm.getPlayerFaction(target.getUniqueId());
                int kills   = plugin.getStatsManager().getKills(target.getUniqueId());
                int deaths  = plugin.getStatsManager().getDeaths(target.getUniqueId());
                int streak  = plugin.getStatsManager().getCurrentStreak(target.getUniqueId());
                int bounty  = plugin.getBountyManager().getTotalBounty(target.getUniqueId());
                player.sendMessage(c("&8&m--- &7Profil de &e" + target.getName() + " &8&m---"));
                player.sendMessage(c("&7Faction : " + (f != null ? "&e[" + f.getTag() + "] " + f.getName() : "&8Aucune")));
                if (f != null) player.sendMessage(c("&7Rôle : &e" + f.getMembers().getOrDefault(target.getUniqueId(), FactionRole.MEMBER).getDisplay()));
                player.sendMessage(c("&7Kills : &a" + kills + " &7| Morts : &c" + deaths + " &7| K/D : &6" + String.format("%.2f", plugin.getStatsManager().getKD(target.getUniqueId()))));
                player.sendMessage(c("&7Série actuelle : &6" + streak));
                if (bounty > 0) player.sendMessage(c("&7Prime : &c" + bounty + " &7pièces"));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "list" -> {
                player.sendMessage(c("&6&l=== Factions (" + fm.getAllFactions().size() + ") ==="));
                fm.getAllFactions().forEach(f -> player.sendMessage(c(
                    "&e[" + f.getTag() + "] &f" + f.getName() + " &7" + f.getMembers().size() + " membres" +
                    " | Puissance : &e" + String.format("%.0f", f.getPower()) + (f.isOpen() ? " &a[Ouverte]" : ""))));
            }
            case "top" -> {
                List<Faction> sorted = new ArrayList<>(fm.getAllFactions());
                sorted.sort((a, b) -> b.getMembers().size() - a.getMembers().size());
                player.sendMessage(c("&6&l=== Top Factions ==="));
                for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                    Faction f = sorted.get(i);
                    String medal = i == 0 ? "&6" : i == 1 ? "&7" : i == 2 ? "&c" : "&f";
                    player.sendMessage(c(medal + "#" + (i+1) + " &e[" + f.getTag() + "] " + f.getName() + " &7- &f" + f.getMembers().size() + " membres | Puissance &e" + String.format("%.0f", f.getPower())));
                }
            }

            // ─── PUISSANCE ────────────────────────────────────────────────────
            case "power" -> {
                if (args.length > 1) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.player-not-found"))); return true; }
                    Faction f = fm.getPlayerFaction(target.getUniqueId());
                    if (f == null) { player.sendMessage(pre() + c("&e" + target.getName() + " &cn'a pas de faction.")); return true; }
                    player.sendMessage(c("&7Puissance de &e" + f.getName() + " &7(&e" + target.getName() + "&7) : &6" + String.format("%.1f", f.getPower()) + " &7| Claims max : &e" + f.getMaxClaims()));
                } else {
                    Faction f = needFaction(player, fm); if (f == null) return true;
                    player.sendMessage(c("&7Puissance de &e" + f.getName() + " &7: &6" + String.format("%.1f", f.getPower()) + " &7| Claims : &e" + f.getClaims().size() + "/" + f.getMaxClaims()));
                }
            }

            // ─── CARTE ────────────────────────────────────────────────────────
            case "map" -> {
                Faction myFaction = fm.getPlayerFaction(player.getUniqueId());
                int cx = player.getLocation().getChunk().getX();
                int cz = player.getLocation().getChunk().getZ();
                player.sendMessage(c("&8&m-=-=- &7Carte du territoire &8&m-=-=-"));
                for (int z = cz - 3; z <= cz + 3; z++) {
                    StringBuilder row = new StringBuilder("&8| ");
                    for (int x = cx - 8; x <= cx + 8; x++) {
                        if (x == cx && z == cz) { row.append("&f+"); continue; }
                        Faction owner = fm.getFactionByClaim(player.getWorld().getChunkAt(x, z));
                        if (owner == null) row.append("&8-");
                        else if (myFaction != null && owner.getName().equalsIgnoreCase(myFaction.getName())) row.append("&aF");
                        else if (myFaction != null && myFaction.getAllies().contains(owner.getName().toLowerCase())) row.append("&bA");
                        else if (myFaction != null && myFaction.getEnemies().contains(owner.getName().toLowerCase())) row.append("&cE");
                        else row.append("&eO");
                    }
                    row.append(" &8|");
                    player.sendMessage(c(row.toString()));
                }
                player.sendMessage(c("&a■ F&7=Ta faction  &b■ A&7=Allié  &c■ E&7=Ennemi  &e■ O&7=Autre  &f+&7=Toi  &8■&7=Libre"));
            }

            // ─── STUCK ────────────────────────────────────────────────────────
            case "stuck" -> {
                Faction chunkOwner = fm.getFactionByClaim(player.getLocation().getChunk());
                Faction myFaction  = fm.getPlayerFaction(player.getUniqueId());
                boolean stuck = chunkOwner != null && (myFaction == null || !myFaction.getName().equalsIgnoreCase(chunkOwner.getName()));
                if (!stuck) { player.sendMessage(pre() + c("&cTu n'es pas en territoire ennemi.")); return true; }
                final Location startLoc = player.getLocation().clone();
                player.sendMessage(pre() + c("&eTéléportation aléatoire dans &e30s&7... Ne bouge pas !"));
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
                Faction f = needFaction(player, fm); if (f == null) return true;
                fm.toggleFactionChat(player.getUniqueId());
                player.sendMessage(pre() + c("&aChat faction : &e" + (fm.isFactionChat(player.getUniqueId()) ? "ACTIVÉ" : "DÉSACTIVÉ")));
            }
            case "announce" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /f announce <message>")); return true; }
                Faction f = needFaction(player, fm); if (f == null) return true;
                if (!f.isOfficer(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.not-officer"))); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                broadcast(f, c("&6&l[Annonce &e" + f.getName() + "&6] &f" + player.getName() + " &7: &f" + msg));
            }
            case "coords" -> {
                Faction f = needFaction(player, fm); if (f == null) return true;
                Location loc = player.getLocation();
                broadcast(f, pre() + c("&e" + player.getName() + " &7se trouve en &f" + loc.getWorld().getName() + " &7X:&e" + (int)loc.getX() + " &7Y:&e" + (int)loc.getY() + " &7Z:&e" + (int)loc.getZ()));
                player.sendMessage(pre() + c("&aCoordonnées partagées en chat faction."));
            }

            default -> sendHelp(player);
        }
        return true;
    }

    // ─── UTILITAIRES ─────────────────────────────────────────────────────────

    private Faction needFaction(Player player, FactionManager fm) {
        Faction f = fm.getPlayerFaction(player.getUniqueId());
        if (f == null) player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-faction", "&cTu n'as pas de faction.")));
        return f;
    }

    private void sendHelp(Player p) {
        p.sendMessage(c("&6&l=== /faction — Aide ==="));
        p.sendMessage(c("&e/f create/disband &7— Créer/Dissoudre"));
        p.sendMessage(c("&e/f tag/desc/motd/open &7— Personnaliser la faction"));
        p.sendMessage(c("&e/f invite/join/leave/kick &7— Gestion des membres"));
        p.sendMessage(c("&e/f promote/demote/setowner &7— Rôles"));
        p.sendMessage(c("&e/f sethome/home &7— Home faction"));
        p.sendMessage(c("&e/f setwarp/warp/warps/delwarp &7— Warps faction"));
        p.sendMessage(c("&e/f claim/unclaim/unclaimall/autoclaim &7— Territoire"));
        p.sendMessage(c("&e/f ally/enemy/neutral &7— Relations"));
        p.sendMessage(c("&e/f info/who/list/top/power/map &7— Informations"));
        p.sendMessage(c("&e/f chat/announce/coords &7— Communication"));
        p.sendMessage(c("&e/f stuck &7— Sortir du territoire ennemi"));
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
            String s = args[0].toLowerCase();
            if (List.of("info","ally","enemy","neutral","join","delwarp","warp").contains(s))
                return plugin.getFactionManager().getAllFactions().stream().map(Faction::getName).toList();
            if (List.of("invite","kick","promote","demote","setowner","who","power").contains(s))
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return new ArrayList<>();
    }
}
