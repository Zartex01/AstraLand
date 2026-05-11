package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.gui.IslandGeneratorGUI;
import com.astraland.skyblock.gui.IslandSettingsGUI;
import com.astraland.skyblock.gui.IslandWarpGUI;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
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
        "settings","generator","help"
    );

    private final Skyblock plugin;
    public IslandCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&a&lSkyblock&8] &r")); }
    private String msg(String key) { return pre() + c(plugin.getConfig().getString("messages." + key, "&c" + key)); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        IslandManager im = plugin.getIslandManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "help";

        switch (sub) {

            // ── Création ────────────────────────────────────────────────────────
            case "create" -> {
                if (im.hasIsland(player.getUniqueId())) { player.sendMessage(msg("island-exists")); return true; }
                player.sendMessage(pre() + c("&7Génération de ton île en cours..."));
                Island isl = im.createIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cErreur : le monde &e" + plugin.getPluginWorld() + " &cn'est pas chargé.")); return true; }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(isl.getHome());
                    player.sendMessage(msg("island-created"));
                }, 5L);
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
                isl.setHome(player.getLocation());
                im.saveAll();
                player.sendMessage(pre() + c("&aHome défini ici !"));
            }

            // ── Membres ─────────────────────────────────────────────────────────
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is invite <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                if (isl.getMemberCount() >= isl.getMemberSlots()) { player.sendMessage(pre() + c("&cTon île est pleine ! (&e" + isl.getMemberSlots() + " &cslots)")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (isl.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur est déjà membre.")); return true; }
                isl.invite(target.getUniqueId());
                player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a."));
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'a invité sur son île ! &e/is join " + player.getName()));
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
                if (im.getOwnedIsland(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es propriétaire. Utilise &e/is delete &cpour supprimer.")); return true; }
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                im.removeMember(isl, player.getUniqueId());
                player.sendMessage(pre() + c("&aQuitté l'île."));
            }

            // ── Supprimer ────────────────────────────────────────────────────────
            case "delete" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                if (!im.hasPendingDelete(player.getUniqueId())) {
                    im.requestDelete(player.getUniqueId());
                    int sec = plugin.getConfig().getInt("island.delete-confirm-seconds", 30);
                    player.sendMessage(pre() + c("&c⚠ Suppression irréversible ! Tape à nouveau &e/is delete &cdans &e" + sec + "s &cpour confirmer."));
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
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &a&lÎle &8— &f" + isl.getName()));
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &7Propriétaire : &e" + (owner.getName() != null ? owner.getName() : "?")));
                player.sendMessage(c("  &7Membres : &f" + (isl.getMemberCount() + 1) + " &8/ &f" + (isl.getMemberSlots() + 1)));
                player.sendMessage(c("  &7Niveau : &a" + isl.getLevel() + "  &7Valeur : &6" + fmt(isl.getValue())));
                player.sendMessage(c("  &7Générateur : &b" + isl.getGeneratorLevel() + "/7"));
                player.sendMessage(c("  &7PvP : " + (isl.isPvpEnabled() ? "&cActivé" : "&aDesactivé")));
                player.sendMessage(c("  &7Verrou : " + (isl.isLocked() ? "&cVerrouillée" : "&aOuverte")));
                player.sendMessage(c("  &7Warp public : " + (isl.isWarpEnabled() ? "&a" + (isl.getWarpName().isBlank() ? "Activé" : isl.getWarpName()) : "&cDésactivé")));
                player.sendMessage(c("  &7Blocs cassés : &f" + fmt(isl.getBlocksBroken())));
                player.sendMessage(c("&8&m─────────────────────────────"));
            }
            case "top" -> {
                List<Island> top = im.getTopIslands(10);
                player.sendMessage(c("&6&l=== Top 10 Îles Skyblock ==="));
                if (top.isEmpty()) { player.sendMessage(c("&7Aucune île.")); return true; }
                for (int i = 0; i < top.size(); i++) {
                    Island isl = top.get(i);
                    org.bukkit.OfflinePlayer o = Bukkit.getOfflinePlayer(isl.getOwner());
                    String medal = i == 0 ? "&6🥇" : i == 1 ? "&7🥈" : i == 2 ? "&c🥉" : "&8#" + (i + 1);
                    player.sendMessage(c(medal + " &f" + (o.getName() != null ? o.getName() : "?") +
                        " &7- Niveau &a" + isl.getLevel() + " &8| &7Valeur &6" + fmt(isl.getValue())));
                }
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
            case "warps" -> { new IslandWarpGUI(plugin).open(player); }
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
                isl.setWarpName(warpName);
                isl.setWarpEnabled(true);
                im.saveAll();
                player.sendMessage(pre() + c("&aWarp public &e" + warpName + " &acréé ! Ton île est visible via &e/is warps"));
            }
            case "delwarp" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                isl.setWarpEnabled(false);
                isl.setWarpName("");
                im.saveAll();
                player.sendMessage(pre() + c("&aWarp supprimé."));
            }

            // ── Sécurité ────────────────────────────────────────────────────────
            case "lock" -> { toggleLock(player, im, true); }
            case "unlock" -> { toggleLock(player, im, false); }
            case "pvp" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                isl.setPvpEnabled(!isl.isPvpEnabled());
                im.saveAll();
                player.sendMessage(pre() + c("&7PvP : " + (isl.isPvpEnabled() ? "&cActivé" : "&aDesactivé")));
            }
            case "ban" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is ban <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                if (!target.hasPlayedBefore() && Bukkit.getPlayerExact(args[1]) == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (isl.isOwner(target.getUniqueId())) { player.sendMessage(pre() + c("&cTu ne peux pas te bannir toi-même.")); return true; }
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
                isl.unbanPlayer(target.getUniqueId());
                im.saveAll();
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
                player.sendMessage(pre() + c("&e" + target.getName() + " &apeut maintenant construire sur ton île (coop)."));
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'a ajouté en coop sur son île !"));
            }
            case "uncoop" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is uncoop <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                im.removeCoop(isl, target.getUniqueId());
                player.sendMessage(pre() + c("&eCoop &aretiré."));
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
                player.sendMessage(c("&8&m─────────────────────────────"));
                player.sendMessage(c("  &a&lNiveau de l'île : &e" + isl.getLevel()));
                player.sendMessage(c("  &7Valeur : &6" + fmt(isl.getValue()) + " pts"));
                player.sendMessage(c("  &7Prochain niveau : &6" + fmt(next) + " pts"));
                long diff = next - isl.getValue();
                if (diff > 0) player.sendMessage(c("  &7Manque : &c" + fmt(diff) + " pts"));
                else player.sendMessage(c("  &aProchain niveau atteint ! Fais &e/is scan&a."));
                player.sendMessage(c("  &7Fais &e/is scan &7pour recalculer."));
                player.sendMessage(c("&8&m─────────────────────────────"));
            }
            case "scan" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(msg("no-island")); return true; }
                player.sendMessage(pre() + c("&7Scan de l'île en cours... ça peut prendre un moment."));
                int radius = plugin.getConfig().getInt("island.size", 100);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    long value = plugin.getLevelManager().scanIsland(isl, radius);
                    int newLevel = isl.getLevel();
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
                if (name.length() > 32) { player.sendMessage(pre() + c("&cNom trop long (max 32 caractères).")); return true; }
                isl.setName(name);
                im.saveAll();
                player.sendMessage(pre() + c("&aNom de l'île : &f" + name));
            }
            case "expel" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return true; }
                int radius = plugin.getConfig().getInt("island.size", 100);
                int count = 0;
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

            default -> sendHelp(player);
        }
        return true;
    }

    private void toggleLock(Player player, IslandManager im, boolean lock) {
        Island isl = im.getOwnedIsland(player.getUniqueId());
        if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire.")); return; }
        isl.setLocked(lock);
        im.saveAll();
        player.sendMessage(pre() + c(lock ? "&cÎle &l🔒 Verrouillée&c." : "&aÎle &l🔓 Déverrouillée&a."));
    }

    private void sendHelp(Player p) {
        p.sendMessage(c("&8&m══════════════════════════════"));
        p.sendMessage(c("  &a&l✦ Commandes /island (/is)"));
        p.sendMessage(c("&8&m══════════════════════════════"));
        p.sendMessage(c("  &e/is create &8- &7Créer ton île"));
        p.sendMessage(c("  &e/is home &8- &7Aller à ton île"));
        p.sendMessage(c("  &e/is sethome &8- &7Définir le point de téléportation"));
        p.sendMessage(c("  &e/is setname <nom> &8- &7Renommer l'île"));
        p.sendMessage(c("  &e/is info &8- &7Infos de l'île"));
        p.sendMessage(c("  &e/is top &8- &7Classement des îles"));
        p.sendMessage(c("  &e/is level &8- &7Voir le niveau / valeur de l'île"));
        p.sendMessage(c("  &e/is scan &8- &7Recalculer le niveau de l'île"));
        p.sendMessage(c("  &6/is invite <joueur> &8- &7Inviter un joueur"));
        p.sendMessage(c("  &6/is join <proprio> &8- &7Rejoindre une île"));
        p.sendMessage(c("  &6/is kick <joueur> &8- &7Expulser un membre"));
        p.sendMessage(c("  &6/is leave &8- &7Quitter une île"));
        p.sendMessage(c("  &6/is coop <joueur> &8- &7Faire confiance (construire)"));
        p.sendMessage(c("  &6/is uncoop <joueur> &8- &7Retirer la confiance"));
        p.sendMessage(c("  &c/is ban <joueur> &8- &7Bannir de l'île"));
        p.sendMessage(c("  &c/is unban <joueur> &8- &7Débannir"));
        p.sendMessage(c("  &c/is banlist &8- &7Liste des bannis"));
        p.sendMessage(c("  &c/is expel &8- &7Expulser tous les visiteurs"));
        p.sendMessage(c("  &b/is visit <joueur> &8- &7Visiter l'île d'un joueur"));
        p.sendMessage(c("  &b/is warps &8- &7Voir les warps publics"));
        p.sendMessage(c("  &b/is setwarp [nom] &8- &7Créer un warp public"));
        p.sendMessage(c("  &b/is delwarp &8- &7Supprimer ton warp"));
        p.sendMessage(c("  &d/is lock &8/ &d/is unlock &8- &7Verrouiller l'île"));
        p.sendMessage(c("  &d/is pvp &8- &7Toggle PvP sur l'île"));
        p.sendMessage(c("  &d/is chat &8- &7Toggle chat privé île"));
        p.sendMessage(c("  &d/is settings &8- &7Paramètres (GUI)"));
        p.sendMessage(c("  &d/is generator &8- &7Améliorer le générateur"));
        p.sendMessage(c("  &4/is delete &8- &7Supprimer ton île"));
        p.sendMessage(c("&8&m══════════════════════════════"));
    }

    private String fmt(long v) { return NumberFormat.getInstance().format(v); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        String typed = args[args.length - 1].toLowerCase();
        if (args.length == 1)
            return SUBS.stream().filter(s -> s.startsWith(typed)).collect(Collectors.toList());
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Set.of("invite","kick","ban","unban","coop","uncoop","visit","join").contains(sub))
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(typed)).collect(Collectors.toList());
        }
        return List.of();
    }
}
