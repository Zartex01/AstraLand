package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.challenges.ChallengeGUI;
import com.astraland.skyblock.gui.*;
import com.astraland.skyblock.managers.EconomyManager;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.quests.DailyQuestGUI;
import com.astraland.skyblock.ranks.IslandRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class IslandCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBS = List.of(
        "create","home","sethome","invite","join","kick","leave","delete",
        "info","top","visit","warp","warps","setwarp","delwarp",
        "lock","unlock","pvp","ban","unban","banlist",
        "coop","uncoop","chat","level","scan","setname","expel",
        "settings","generator","upgrades","challenges","bank",
        "quetes","border","membres","rang","help"
    );

    private final Skyblock plugin;
    public IslandCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s)    { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre()          { return c(plugin.getConfig().getString("prefix", "&8[&a&lSkyblock&8] &r")); }
    private String msg(String key){ return pre() + c(plugin.getConfig().getString("messages." + key, "&c" + key)); }
    private String fmt(long v)    { return NumberFormat.getInstance(Locale.FRENCH).format(v); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        IslandManager im = plugin.getIslandManager();

        if (args.length == 0) {
            Island isl = im.getIsland(player.getUniqueId());
            if (isl == null) {
                player.sendMessage(msg("no-island"));
                player.sendMessage(pre() + c("&7Utilise &e/is create &7pour créer ton île."));
            } else {
                new IslandMenuGUI(plugin, player, isl).open(player);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            // ── Création ────────────────────────────────────────────────────────
            case "create" -> {
                if (im.hasIsland(player.getUniqueId())) { player.sendMessage(msg("island-exists")); return true; }
                // Ouvrir le sélecteur de schéma
                new IslandSchematicGUI(plugin).open(player);
            }

            // ── Téléportation ──────────────────────────────────────────────────
            case "home" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(msg("teleporting"));
            }
            case "sethome" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire d'une île.")); return true; }
                if (!im.isInsideOwnIsland(player.getUniqueId(), player.getLocation())) { player.sendMessage(pre() + c("&cTu dois être sur ton île.")); return true; }
                isl.setHome(player.getLocation()); im.saveAll();
                player.sendMessage(pre() + c("&aHome défini ici !"));
            }

            // ── Membres ─────────────────────────────────────────────────────────
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is invite <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                if (isl.getMemberCount() >= isl.getMemberSlots()) { player.sendMessage(pre() + c("&cTon île est pleine !")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (isl.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur est déjà membre.")); return true; }
                isl.invite(target.getUniqueId());
                player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a."));
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'a invité ! &e/is join " + player.getName()));
            }
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is join <propriétaire>")); return true; }
                if (im.hasIsland(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu as déjà une île.")); return true; }
                Player ownerP = Bukkit.getPlayerExact(args[1]);
                UUID ownerId; String ownerName;
                if (ownerP != null) { ownerId = ownerP.getUniqueId(); ownerName = ownerP.getName(); }
                else {
                    org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                    if (!op.hasPlayedBefore()) { player.sendMessage(pre() + c("&cProprietaire introuvable.")); return true; }
                    ownerId = op.getUniqueId(); ownerName = op.getName() != null ? op.getName() : args[1];
                }
                Island isl = im.getOwnedIsland(ownerId);
                if (isl == null || !isl.isInvited(player.getUniqueId())) { player.sendMessage(pre() + c("&cAucune invitation valide.")); return true; }
                im.addMember(isl, player.getUniqueId());
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aRejoint l'île de &e" + ownerName + " &a!"));
                if (ownerP != null) ownerP.sendMessage(pre() + c("&e" + player.getName() + " &aa rejoint ton île !"));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is kick <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !isl.isMember(target.getUniqueId()) || isl.isOwner(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur n'est pas membre.")); return true; }
                im.removeMember(isl, target.getUniqueId());
                if (plugin.isInPluginWorld(target)) target.teleport(target.getWorld().getSpawnLocation());
                target.sendMessage(msg("kicked"));
                player.sendMessage(pre() + c("&e" + target.getName() + " &aexpulsé de l'île."));
            }
            case "leave" -> {
                if (im.getOwnedIsland(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es propriétaire. Utilise &e/is delete&c.")); return true; }
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                im.removeMember(isl, player.getUniqueId());
                player.sendMessage(pre() + c("&aQuitté l'île."));
            }
            case "membres" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                new IslandMembersGUI(plugin, isl, player).open(player);
            }

            // ── Supprimer ────────────────────────────────────────────────────────
            case "delete" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                if (!im.hasPendingDelete(player.getUniqueId())) {
                    im.requestDelete(player.getUniqueId());
                    int sec = plugin.getConfig().getInt("island.delete-confirm-seconds", 30);
                    player.sendMessage(pre() + c("&c⚠ Suppression irréversible ! Retape &e/is delete &cdans &e" + sec + "s &cpour confirmer."));
                } else {
                    im.deleteIsland(player.getUniqueId());
                    player.sendMessage(pre() + c("&aÎle supprimée."));
                }
            }

            // ── Informations ─────────────────────────────────────────────────────
            case "info" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                org.bukkit.OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
                IslandRank rank = IslandRank.fromLevel(isl.getLevel());
                int defiDone = plugin.getChallengeManager().countCompleted(isl.getOwner());
                int defiTotal = plugin.getChallengeManager().getAllChallenges().size();
                int qDone = plugin.getQuestManager().countCompleted(player.getUniqueId());
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &a&lÎle &8— &f" + isl.getName() + " &8| &7Rang : " + rank.getFullName()));
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &7Propriétaire : &e" + (owner.getName() != null ? owner.getName() : "?")));
                player.sendMessage(c("  &7Membres : &f" + (isl.getMemberCount()+1) + " &8/ &f" + (isl.getMemberSlots()+1)));
                player.sendMessage(c("  &7Niveau : &a" + isl.getLevel() + "  &7Valeur : &6" + fmt(isl.getValue()) + " pts"));
                player.sendMessage(c("  &7Générateur : &b" + isl.getGeneratorLevel() + "/7"));
                player.sendMessage(c("  &7Défis : &a" + defiDone + "/" + defiTotal + "  &7Quêtes : &e" + qDone + "/5"));
                player.sendMessage(c("  &7Banque d'île : &6" + fmt(isl.getBankBalance()) + " $"));
                player.sendMessage(c("  &7Vol : " + (isl.hasFlyUpgrade() ? "&a✔" : "&c✗") + "  &7Keep Inv : " + (isl.hasKeepInventoryUpgrade() ? "&a✔" : "&c✗")));
                player.sendMessage(c("  &7PvP : " + (isl.isPvpEnabled() ? "&cActivé" : "&aDesactivé") + "  &7Verrou : " + (isl.isLocked() ? "&cOui" : "&aOuvert")));
                player.sendMessage(c("  &7Warp public : " + (isl.isWarpEnabled() ? "&a" + (isl.getWarpName().isBlank() ? "Activé" : isl.getWarpName()) : "&cDésactivé")));
                player.sendMessage(c("  &7Blocs cassés : &f" + fmt(isl.getBlocksBroken())));
                player.sendMessage(c("  &7Schéma d'île : &f" + isl.getSchematicType()));
                if (rank.getSellBonus() > 0) player.sendMessage(c("  &7Bonus vente : &6+" + rank.getSellBonus() + "%"));
                player.sendMessage(c("&8&m─────────────────────────────"));
            }
            case "top" -> { new IslandTopGUI(plugin).open(player); }

            // ── Rang ────────────────────────────────────────────────────────────
            case "rang" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                IslandRank rank = IslandRank.fromLevel(isl.getLevel());
                IslandRank next = rank.next();
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &f✦ Rang : " + rank.getFullName()));
                player.sendMessage(c("  &7Bonus vente : &6+" + rank.getSellBonus() + "%"));
                if (rank.getGeneratorBonus() > 0) player.sendMessage(c("  &7Bonus générateur : &b+" + rank.getGeneratorBonus()));
                if (next != null) player.sendMessage(c("  &7Prochain rang : " + next.getFullName() + " &8(niveau &a" + next.getMinLevel() + "&8 requis)"));
                else player.sendMessage(c("  &5✦ Rang maximum atteint !"));
                player.sendMessage(c("&8&m─────────────────────────────"));
            }

            // ── Quêtes du Jour ────────────────────────────────────────────────
            case "quetes", "quête", "quete", "quest", "quests", "daily" -> {
                new DailyQuestGUI(plugin, player).open(player);
            }

            // ── Bordure de l'île ──────────────────────────────────────────────
            case "border" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                boolean on = plugin.getBorderTask().toggleBorder(player.getUniqueId());
                player.sendMessage(pre() + c("&7Bordure d'île : " + (on ? "&aActivée &8(particules vertes)" : "&7Désactivée")));
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, on ? 1.3f : 0.7f);
            }

            // ── Visiter & Warps ──────────────────────────────────────────────────
            case "visit" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is visit <joueur>")); return true; }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                Island isl = im.getOwnedIsland(target.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cCe joueur n'a pas d'île.")); return true; }
                if (isl.isBanned(player.getUniqueId())) { player.sendMessage(msg("banned")); return true; }
                if (isl.isLocked() && !isl.isMember(player.getUniqueId())) { player.sendMessage(msg("locked")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aTu visites l'île de &e" + (target.getName() != null ? target.getName() : args[1]) + "&a."));
            }
            case "warps" -> new IslandWarpGUI(plugin).open(player);
            case "warp" -> {
                if (args.length < 2) { new IslandWarpGUI(plugin).open(player); return true; }
                String search = args[1].toLowerCase();
                Island found = null;
                for (Island isl : im.getAllIslands()) {
                    if (!isl.isWarpEnabled() || isl.isLocked()) continue;
                    org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(isl.getOwner());
                    String oName = op.getName() != null ? op.getName() : "";
                    if (oName.equalsIgnoreCase(search) || isl.getWarpName().equalsIgnoreCase(search)) { found = isl; break; }
                }
                if (found == null) { player.sendMessage(pre() + c("&cWarp introuvable.")); return true; }
                if (found.isBanned(player.getUniqueId())) { player.sendMessage(msg("banned")); return true; }
                player.teleport(found.getHome());
                player.sendMessage(pre() + c("&aTéléporté !"));
            }
            case "setwarp" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                String warpName = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : player.getName();
                isl.setWarpName(warpName); isl.setWarpEnabled(true); im.saveAll();
                player.sendMessage(pre() + c("&aWarp public &e" + warpName + " &acréé !"));
            }
            case "delwarp" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                isl.setWarpEnabled(false); isl.setWarpName(""); im.saveAll();
                player.sendMessage(pre() + c("&aWarp supprimé."));
            }

            // ── Sécurité ────────────────────────────────────────────────────────
            case "lock"   -> toggleLock(player, im, true);
            case "unlock" -> toggleLock(player, im, false);
            case "pvp" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                isl.setPvpEnabled(!isl.isPvpEnabled()); im.saveAll();
                player.sendMessage(pre() + c("&7PvP : " + (isl.isPvpEnabled() ? "&cActivé" : "&aDesactivé")));
            }
            case "ban" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is ban <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!target.hasPlayedBefore() && Bukkit.getPlayerExact(args[1]) == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (isl.isOwner(target.getUniqueId())) { player.sendMessage(pre() + c("&cTu ne peux pas te bannir.")); return true; }
                isl.banPlayer(target.getUniqueId());
                if (isl.isMember(target.getUniqueId())) im.removeMember(isl, target.getUniqueId());
                im.saveAll();
                Player onlineTarget = Bukkit.getPlayer(target.getUniqueId());
                if (onlineTarget != null && plugin.isInPluginWorld(onlineTarget)) onlineTarget.teleport(onlineTarget.getWorld().getSpawnLocation());
                if (onlineTarget != null) onlineTarget.sendMessage(msg("banned"));
                player.sendMessage(pre() + c("&e" + (target.getName() != null ? target.getName() : args[1]) + " &abanni de l'île."));
            }
            case "unban" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is unban <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                isl.unbanPlayer(target.getUniqueId()); im.saveAll();
                player.sendMessage(pre() + c("&e" + (target.getName() != null ? target.getName() : args[1]) + " &adébanni."));
            }
            case "banlist" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                if (isl.getBannedPlayers().isEmpty()) { player.sendMessage(pre() + c("&7Aucun joueur banni.")); return true; }
                player.sendMessage(c("&c&lJoueurs bannis :"));
                for (UUID bid : isl.getBannedPlayers()) {
                    org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(bid);
                    player.sendMessage(c("  &8- &f" + (op.getName() != null ? op.getName() : bid.toString())));
                }
            }

            // ── Coop ─────────────────────────────────────────────────────────────
            case "coop" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is coop <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cCe joueur doit être en ligne.")); return true; }
                if (isl.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cDéjà membre.")); return true; }
                im.addCoop(isl, target.getUniqueId());
                player.sendMessage(pre() + c("&e" + target.getName() + " &apeut construire sur ton île (coop)."));
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'a ajouté en coop !"));
            }
            case "uncoop" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is uncoop <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                im.removeCoop(isl, target.getUniqueId());
                player.sendMessage(pre() + c("&eCoop retiré."));
            }

            // ── Chat île ─────────────────────────────────────────────────────────
            case "chat" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                boolean on = im.toggleIslandChat(player.getUniqueId());
                player.sendMessage(pre() + c(on ? "&aChat île activé (&e🏝&a)" : "&7Chat île désactivé."));
            }

            // ── Niveau & Scan ─────────────────────────────────────────────────────
            case "level" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                long next = plugin.getLevelManager().levelThreshold(isl.getLevel() + 1);
                IslandRank rank = IslandRank.fromLevel(isl.getLevel());
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &a&lNiveau de l'île : &e" + isl.getLevel() + "  &8| &7Rang : " + rank.getFullName()));
                player.sendMessage(c("  &7Valeur : &6" + fmt(isl.getValue()) + " pts"));
                player.sendMessage(c("  &7Prochain niveau à : &6" + fmt(next) + " pts"));
                long diff = next - isl.getValue();
                if (diff > 0) player.sendMessage(c("  &7Manque : &c" + fmt(diff) + " pts"));
                else          player.sendMessage(c("  &aMaximum atteint !"));
                player.sendMessage(c("&8&m─────────────────────────────"));
            }
            case "scan" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                player.sendMessage(pre() + c("&7Scan de l'île en cours..."));
                int radius = plugin.getConfig().getInt("island.size", 100);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    long value    = plugin.getLevelManager().scanIsland(isl, radius);
                    int  newLevel = isl.getLevel();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        im.saveAll();
                        player.sendMessage(pre() + c("&aScan terminé ! Valeur : &6" + fmt(value) + " pts &8| &7Niveau : &a" + newLevel));
                    });
                });
            }

            // ── Divers ───────────────────────────────────────────────────────────
            case "setname" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is setname <nom>")); return true; }
                String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (name.length() > 32) { player.sendMessage(pre() + c("&cNom trop long (max 32 car.).")); return true; }
                isl.setName(name); im.saveAll();
                player.sendMessage(pre() + c("&aNom : &f" + name));
            }
            case "expel" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                int radius = plugin.getConfig().getInt("island.size", 100);
                int count  = 0;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.equals(player) || !plugin.isInPluginWorld(p)) continue;
                    if (!isl.isInsideIsland(p.getLocation(), radius)) continue;
                    if (isl.isMember(p.getUniqueId())) continue;
                    p.teleport(p.getWorld().getSpawnLocation());
                    p.sendMessage(pre() + c("&cTu as été expulsé de l'île."));
                    count++;
                }
                player.sendMessage(pre() + c("&a" + count + " joueur(s) expulsé(s)."));
            }

            // ── GUIs ─────────────────────────────────────────────────────────────
            case "settings" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                new IslandSettingsGUI(isl, plugin).open(player);
            }
            case "generator" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                new IslandGeneratorGUI(isl, plugin).open(player);
            }
            case "upgrades" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                new IslandUpgradesGUI(isl, plugin).open(player);
            }
            case "challenges" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                new ChallengeGUI(plugin, player, isl, null, 0).open(player);
            }

            // ── Banque d'île ──────────────────────────────────────────────────────
            case "bank" -> handleBank(player, im, args);

            // ── Aide ────────────────────────────────────────────────────────────
            default -> showHelp(player);
        }
        return true;
    }

    // ── Banque ────────────────────────────────────────────────────────────────

    private void handleBank(Player player, IslandManager im, String[] args) {
        Island isl = im.getIsland(player.getUniqueId());
        if (isl == null) { player.sendMessage(msg("no-island")); return; }
        EconomyManager eco = plugin.getEconomyManager();

        if (args.length < 2) {
            player.sendMessage(c("&8&m─────────────────────────────"));
            player.sendMessage(c("  &e&l🏦 Banque d'île"));
            player.sendMessage(c("  &7Solde banque : &6" + fmt(isl.getBankBalance()) + " $"));
            player.sendMessage(c("  &7Ton solde perso : &e" + fmt(eco.getBalance(player.getUniqueId())) + " $"));
            player.sendMessage(c(""));
            player.sendMessage(c("  &7/is bank deposit <montant>  &8— &fdéposer"));
            player.sendMessage(c("  &7/is bank withdraw <montant> &8— &fretirer"));
            player.sendMessage(c("&8&m─────────────────────────────"));
            return;
        }
        String action = args[1].toLowerCase();
        if (args.length < 3) { player.sendMessage(pre() + c("&cUsage : /is bank " + action + " <montant>")); return; }
        long amount;
        try { amount = Long.parseLong(args[2]); } catch (NumberFormatException e) { player.sendMessage(pre() + c("&cMontant invalide.")); return; }
        if (amount <= 0) { player.sendMessage(pre() + c("&cLe montant doit être positif.")); return; }

        switch (action) {
            case "deposit" -> {
                if (eco.getBalance(player.getUniqueId()) < amount) { player.sendMessage(pre() + c("&cFonds insuffisants.")); return; }
                eco.removeBalance(player.getUniqueId(), (int) amount);
                isl.depositToBank(amount); im.saveAll();
                player.sendMessage(pre() + c("&a+&6" + fmt(amount) + " $ &adéposés. Banque : &6" + fmt(isl.getBankBalance()) + " $"));
                for (UUID uid : isl.getMembers()) { Player m = Bukkit.getPlayer(uid); if (m != null && !m.equals(player)) m.sendMessage(pre() + c("&e" + player.getName() + " &a→ banque d'île &6+" + fmt(amount) + " $")); }
            }
            case "withdraw" -> {
                if (!isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut retirer.")); return; }
                if (!isl.withdrawFromBank(amount)) { player.sendMessage(pre() + c("&cFonds insuffisants (&6" + fmt(isl.getBankBalance()) + " $&c).")); return; }
                eco.addBalance(player.getUniqueId(), (int) amount);
                im.saveAll();
                player.sendMessage(pre() + c("&6" + fmt(amount) + " $ &aretirés. Perso : &6" + fmt(eco.getBalance(player.getUniqueId())) + " $"));
            }
            default -> player.sendMessage(pre() + c("&cUsage : /is bank [deposit|withdraw] <montant>"));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void toggleLock(Player player, IslandManager im, boolean locked) {
        Island isl = im.getOwnedIsland(player.getUniqueId());
        if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return; }
        isl.setLocked(locked); im.saveAll();
        player.sendMessage(pre() + c("&7Île " + (locked ? "&cVerrouillée" : "&aOuverte") + "&7."));
    }

    private void showHelp(Player player) {
        player.sendMessage(c("&8&m─────────────────────────────"));
        player.sendMessage(c("  &a&lSkyblock &8— &7Commandes principales"));
        player.sendMessage(c("&8&m─────────────────────────────"));
        player.sendMessage(c("  &e/is &8— &7Ouvrir le menu GUI"));
        player.sendMessage(c("  &e/is create &8— &7Créer une île (sélection schéma)"));
        player.sendMessage(c("  &e/is home &8— &7Aller à ton île"));
        player.sendMessage(c("  &e/is info &8— &7Stats complètes de l'île"));
        player.sendMessage(c("  &e/is top &8— &7Classement des îles (GUI)"));
        player.sendMessage(c("  &e/is quetes &8— &7Quêtes du jour (5/jour)"));
        player.sendMessage(c("  &e/is border &8— &7Afficher la bordure d'île"));
        player.sendMessage(c("  &e/is rang &8— &7Ton rang d'île actuel"));
        player.sendMessage(c("  &e/is membres &8— &7Gérer les membres et rôles"));
        player.sendMessage(c("  &e/is challenges &8— &7Défis permanents"));
        player.sendMessage(c("  &e/is upgrades &8— &7Améliorations (vol, keep inv...)"));
        player.sendMessage(c("  &e/is bank &8— &7Banque d'île partagée"));
        player.sendMessage(c("  &e/is warps &8— &7Visiter d'autres îles"));
        player.sendMessage(c("  &e/is chat &8— &7Canal de chat privé d'île"));
        player.sendMessage(c("&8&m─────────────────────────────"));
    }

    // ── Tab completion ─────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) return SUBS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if (args.length == 2 && args[0].equalsIgnoreCase("bank")) return List.of("deposit", "withdraw");
        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("visit")
            || args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
