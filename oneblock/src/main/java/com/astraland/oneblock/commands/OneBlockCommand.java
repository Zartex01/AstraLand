package com.astraland.oneblock.commands;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.gui.*;
import com.astraland.oneblock.managers.OneBlockManager;
import com.astraland.oneblock.models.IslandChallenge;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import com.astraland.oneblock.models.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class OneBlockCommand implements CommandExecutor, TabCompleter {

    private final OneBlock plugin;
    private final IslandMenuGUI menuGUI;
    private final IslandMembersGUI membersGUI;
    private final IslandUpgradesGUI upgradesGUI;
    private final IslandWarpGUI warpGUI;
    private final ChallengesGUI challengesGUI;
    private final IslandSettingsGUI settingsGUI;
    private final SkillsGUI skillsGUI;
    private final IslandBankGUI bankGUI;
    private final PrestigeGUI prestigeGUI;
    private final DailyMissionsGUI missionsGUI;
    private final IslandStatsGUI statsGUI;

    public OneBlockCommand(OneBlock plugin) {
        this.plugin = plugin;
        this.menuGUI = new IslandMenuGUI(plugin);
        this.membersGUI = new IslandMembersGUI(plugin);
        this.upgradesGUI = new IslandUpgradesGUI(plugin);
        this.warpGUI = new IslandWarpGUI(plugin);
        this.challengesGUI = new ChallengesGUI(plugin);
        this.settingsGUI = new IslandSettingsGUI(plugin);
        this.skillsGUI = new SkillsGUI(plugin);
        this.bankGUI = new IslandBankGUI(plugin);
        this.prestigeGUI = new PrestigeGUI(plugin);
        this.missionsGUI = new DailyMissionsGUI(plugin);
        this.statsGUI = new IslandStatsGUI(plugin);
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&6&lOneBlock&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        OneBlockManager om = plugin.getOneBlockManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "menu";

        switch (sub) {
            case "create" -> {
                if (om.hasIsland(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu as déjà une île OneBlock !")); return true; }
                OneBlockIsland isl = om.createIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cErreur. Le monde oneblock est-il chargé ?")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aÎle OneBlock créée ! Casse le bloc magique pour commencer."));
            }
            case "home" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île. Utilise &e/ob create")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aTéléportation vers ton île..."));
            }
            case "sethome" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                if (!isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut définir le home.")); return true; }
                isl.setHome(player.getLocation()); om.saveAll();
                player.sendMessage(pre() + c("&aHome défini à ta position actuelle !"));
            }
            case "info" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                Phase ph = isl.getCurrentPhase();
                Phase next = null;
                for (Phase p : Phase.values()) if (p.getBlocksRequired() > isl.getBlocksBroken()) { next = p; break; }
                double totalMult = isl.getPrestigeMultiplier() * plugin.getSkillManager().getTotalMoneyMultiplier(player.getUniqueId());
                player.sendMessage(c("&8&m──────── &6&lOneBlock &8&m────────"));
                player.sendMessage(c("&7Propriétaire : &e" + om.getOwnerName(isl)));
                player.sendMessage(c("&7Blocs cassés : &e" + isl.getBlocksBroken()));
                player.sendMessage(c("&7Niveau : &b" + isl.getIslandLevel()));
                player.sendMessage(c("&7Phase : " + ph.getColor() + "&l" + ph.getDisplayName()));
                if (next != null) player.sendMessage(c("&7Prochaine phase : &e" + next.getDisplayName() + " &7dans &e" + (next.getBlocksRequired() - isl.getBlocksBroken()) + " &7blocs"));
                else player.sendMessage(c("&a✔ Phase maximale !"));
                player.sendMessage(c("&7Prestige : &d" + isl.getPrestige() + " &7(×" + String.format("%.1f", isl.getPrestigeMultiplier()) + ")"));
                player.sendMessage(c("&7Multiplicateur total : &e×" + String.format("%.2f", totalMult)));
                player.sendMessage(c("&7Membres : &e" + isl.getAllMemberUUIDs().size()));
                player.sendMessage(c("&7Pièces : &e" + plugin.getEconomyManager().getBalance(player.getUniqueId())));
                player.sendMessage(c("&7Banque île : &e" + isl.getBankBalance()));
                player.sendMessage(c("&8&m──────────────────────────"));
            }
            case "phase" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                player.sendMessage(c("&6&l=== Phases OneBlock ==="));
                for (Phase p : Phase.values()) {
                    boolean current = p == isl.getCurrentPhase();
                    boolean unlocked = isl.getBlocksBroken() >= p.getBlocksRequired();
                    String status = current ? " &6[ACTUELLE]" : unlocked ? " &a[DÉBLOQUÉE]" : " &7[" + p.getBlocksRequired() + " blocs]";
                    player.sendMessage(c(p.getColor() + (unlocked ? "✔ " : "✗ ") + p.getDisplayName() + status));
                }
            }
            case "top" -> {
                player.sendMessage(c("&6&l=== Top 10 OneBlock (blocs) ==="));
                List<OneBlockIsland> top = om.getTop(10);
                for (int i = 0; i < top.size(); i++) {
                    OneBlockIsland isl = top.get(i);
                    String medal = i == 0 ? "&6🥇" : i == 1 ? "&7🥈" : i == 2 ? "&c🥉" : "&8#" + (i + 1);
                    player.sendMessage(c(medal + " &f" + om.getOwnerName(isl)
                        + " &8| &e" + isl.getBlocksBroken() + " &7blocs"
                        + " &8| " + isl.getCurrentPhase().getColor() + isl.getCurrentPhase().getDisplayName()
                        + " &8| &dP" + isl.getPrestige()));
                }
            }
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /ob invite <joueur>")); return true; }
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut inviter.")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (isl.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&c" + target.getName() + " est déjà membre.")); return true; }
                om.invitePlayer(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a !"));
                target.sendMessage(pre() + c("&e" + player.getName() + " &7t'invite sur son île ! (/ob accept ou /ob decline)"));
            }
            case "accept" -> {
                boolean ok = om.acceptInvite(player.getUniqueId());
                if (ok) {
                    player.sendMessage(pre() + c("&aInvitation acceptée !"));
                    OneBlockIsland isl = om.getIsland(player.getUniqueId());
                    if (isl != null) {
                        player.teleport(isl.getHome());
                        if (!isl.getMotd().isEmpty()) player.sendMessage(c("&8[MOTD] &7" + isl.getMotd()));
                    }
                } else { player.sendMessage(pre() + c("&cAucune invitation en attente.")); }
            }
            case "decline" -> {
                boolean ok = om.declineInvite(player.getUniqueId());
                player.sendMessage(pre() + c(ok ? "&7Invitation refusée." : "&cAucune invitation."));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /ob kick <joueur>")); return true; }
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut expulser.")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                boolean ok = om.kickMember(player.getUniqueId(), target.getUniqueId());
                if (ok) { player.sendMessage(pre() + c("&e" + target.getName() + " &7expulsé.")); target.sendMessage(pre() + c("&cTu as été expulsé de l'île.")); }
                else player.sendMessage(pre() + c("&cImpossible d'expulser."));
            }
            case "leave" -> {
                boolean ok = om.leaveIsland(player.getUniqueId());
                player.sendMessage(pre() + c(ok ? "&cTu as quitté l'île." : "&cTu ne peux pas quitter (propriétaire ou sans île)."));
            }
            case "coowner", "coproprietaire" -> {
                if (args.length < 3) { player.sendMessage(pre() + c("&cUsage : /ob coowner <add|remove> <joueur>")); return true; }
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut gérer les co-propriétaires.")); return true; }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                if (args[1].equalsIgnoreCase("add")) {
                    boolean ok = om.setCoOwner(player.getUniqueId(), target.getUniqueId());
                    if (ok) { player.sendMessage(pre() + c("&e" + target.getName() + " &aest maintenant co-propriétaire !")); target.sendMessage(pre() + c("&aTu es maintenant co-propriétaire de l'île de &e" + player.getName() + "&a !")); }
                    else player.sendMessage(pre() + c("&cImpossible."));
                } else if (args[1].equalsIgnoreCase("remove")) {
                    boolean ok = om.removeCoOwner(player.getUniqueId(), target.getUniqueId());
                    if (ok) { player.sendMessage(pre() + c("&e" + target.getName() + " &7n'est plus co-propriétaire.")); target.sendMessage(pre() + c("&7Tu n'es plus co-propriétaire de l'île de &e" + player.getName())); }
                    else player.sendMessage(pre() + c("&cImpossible."));
                }
            }
            case "delete" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut supprimer l'île.")); return true; }
                if (args.length < 2 || !args[1].equals("confirmer")) {
                    player.sendMessage(pre() + c("&c⚠ Utilise &e/ob delete confirmer &cpour confirmer.")); return true;
                }
                om.deleteIsland(player.getUniqueId());
                player.sendMessage(pre() + c("&cTon île a été supprimée."));
            }
            case "warp" -> {
                if (args.length >= 2) {
                    String warpName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    for (OneBlockIsland isl : om.getPublicWarps()) {
                        String name = isl.getWarpName().isEmpty() ? om.getOwnerName(isl) : isl.getWarpName();
                        if (name.equalsIgnoreCase(warpName)) {
                            player.teleport(isl.getHome());
                            player.sendMessage(pre() + c("&aTéléporté vers &e" + name + "&a !"));
                            if (!isl.getMotd().isEmpty()) player.sendMessage(c("&8[MOTD] &7" + isl.getMotd()));
                            return true;
                        }
                    }
                    player.sendMessage(pre() + c("&cWarp introuvable."));
                } else warpGUI.open(player, 0);
            }
            case "setwarp" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut définir le warp.")); return true; }
                String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
                isl.setWarpName(name); om.saveAll();
                player.sendMessage(pre() + c("&aNom du warp : &e" + (name.isEmpty() ? "(aucun)" : name)));
            }
            case "motd" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut définir la MOTD.")); return true; }
                String motd = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
                isl.setMotd(motd); om.saveAll();
                player.sendMessage(pre() + c("&aMOTD définie : &7" + (motd.isEmpty() ? "(aucune)" : motd)));
            }
            case "ic", "chat" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /ob ic <message>")); return true; }
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                String rolePrefix = c(isl.getRole(player.getUniqueId()).getPrefix());
                String formatted = c("&8[&aÎle&8] " + rolePrefix + " &f" + player.getName() + "&8: &7" + msg);
                isl.getAllMemberUUIDs().forEach(uid -> { Player m = Bukkit.getPlayer(uid); if (m != null) m.sendMessage(formatted); });
            }
            case "prestige" -> prestigeGUI.open(player);
            case "skills", "competences" -> skillsGUI.open(player);
            case "bank", "banque" -> bankGUI.open(player);
            case "missions", "daily" -> missionsGUI.open(player);
            case "stats", "statistiques" -> statsGUI.open(player);
            case "membres", "members", "team" -> membersGUI.open(player);
            case "upgrades", "ameliorations", "up" -> upgradesGUI.open(player);
            case "defis", "challenges" -> challengesGUI.open(player);
            case "warps" -> warpGUI.open(player, 0);
            case "settings", "parametres" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl != null && !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut accéder aux paramètres.")); return true; }
                settingsGUI.open(player);
            }
            case "help", "aide" -> sendHelp(player);
            default -> {
                if (om.hasIsland(player.getUniqueId())) menuGUI.open(player);
                else sendHelp(player);
            }
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(c("&8&m────── &6&lOneBlock &7- Aide &8&m──────"));
        player.sendMessage(c("&e/ob &7— Menu principal"));
        player.sendMessage(c("&e/ob create &7— Créer ton île"));
        player.sendMessage(c("&e/ob home / sethome &7— Aller/définir le home"));
        player.sendMessage(c("&e/ob invite/kick/accept/decline/leave &7— Membres"));
        player.sendMessage(c("&e/ob coowner add/remove <joueur> &7— Co-propriétaire"));
        player.sendMessage(c("&e/ob upgrades &7— Améliorations"));
        player.sendMessage(c("&e/ob defis &7— Défis permanents"));
        player.sendMessage(c("&e/ob missions &7— Missions journalières"));
        player.sendMessage(c("&e/ob skills &7— Compétences (Minage, Combat, Récolte)"));
        player.sendMessage(c("&e/ob bank &7— Banque de l'île"));
        player.sendMessage(c("&e/ob prestige &7— Prestige (multiplicateur permanent)"));
        player.sendMessage(c("&e/ob stats &7— Statistiques détaillées"));
        player.sendMessage(c("&e/ob warps &7— Warps publics"));
        player.sendMessage(c("&e/ob motd <msg> &7— Message de bienvenue"));
        player.sendMessage(c("&e/ic <msg> &7— Chat d'île"));
        player.sendMessage(c("&e/ob top &7— Classement"));
        player.sendMessage(c("&8&m────────────────────────────"));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList("create","home","sethome","info","phase","top",
                "invite","kick","accept","decline","leave","coowner","delete","warp","setwarp","motd",
                "ic","membres","upgrades","defis","missions","skills","bank","prestige","stats","warps","settings","help");
            return subs.stream().filter(sub -> sub.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && Arrays.asList("invite","kick","coowner").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("coowner"))
            return Arrays.asList("add","remove").stream().filter(x -> x.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        if (args.length == 3 && args[0].equalsIgnoreCase("coowner"))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        return List.of();
    }
}
