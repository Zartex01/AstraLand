package com.astraland.oneblock.commands;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.gui.*;
import com.astraland.oneblock.managers.OneBlockManager;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
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

    public OneBlockCommand(OneBlock plugin) {
        this.plugin = plugin;
        this.menuGUI = new IslandMenuGUI(plugin);
        this.membersGUI = new IslandMembersGUI(plugin);
        this.upgradesGUI = new IslandUpgradesGUI(plugin);
        this.warpGUI = new IslandWarpGUI(plugin);
        this.challengesGUI = new ChallengesGUI(plugin);
        this.settingsGUI = new IslandSettingsGUI(plugin);
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
                if (om.hasIsland(player.getUniqueId())) {
                    player.sendMessage(pre() + c("&cTu as déjà une île OneBlock !"));
                    return true;
                }
                OneBlockIsland isl = om.createIsland(player.getUniqueId());
                if (isl == null) {
                    player.sendMessage(pre() + c("&cErreur. Le monde oneblock est-il chargé ?"));
                    return true;
                }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aÎle OneBlock créée ! Casse le bloc magique pour commencer."));
            }
            case "home" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île. Utilise &e/ob create&c.")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aTéléportation vers ton île..."));
            }
            case "sethome" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                if (!isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut définir le home.")); return true; }
                isl.setHome(player.getLocation());
                om.saveAll();
                player.sendMessage(pre() + c("&aHome défini à ta position actuelle !"));
            }
            case "info" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                Phase phase = isl.getCurrentPhase();
                Phase next = null;
                for (Phase p : Phase.values()) if (p.getBlocksRequired() > isl.getBlocksBroken()) { next = p; break; }
                player.sendMessage(c("&8&m──────── &6&lOneBlock &8&m────────"));
                player.sendMessage(c("&7Propriétaire : &e" + om.getOwnerName(isl)));
                player.sendMessage(c("&7Blocs cassés : &e" + isl.getBlocksBroken()));
                player.sendMessage(c("&7Niveau : &b" + isl.getIslandLevel()));
                player.sendMessage(c("&7Phase : " + phase.getColor() + "&l" + phase.getDisplayName()));
                if (next != null) player.sendMessage(c("&7Prochaine phase : &e" + next.getDisplayName() + " &7dans &e" + (next.getBlocksRequired() - isl.getBlocksBroken()) + " &7blocs"));
                else player.sendMessage(c("&a✔ Phase maximale atteinte !"));
                player.sendMessage(c("&7Membres : &e" + (isl.getMembers().size() + 1)));
                player.sendMessage(c("&7Pièces : &e" + plugin.getEconomyManager().getBalance(player.getUniqueId())));
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
                player.sendMessage(c("&6&l=== Top 10 OneBlock ==="));
                List<OneBlockIsland> top = om.getTop(10);
                for (int i = 0; i < top.size(); i++) {
                    OneBlockIsland isl = top.get(i);
                    String medal = i == 0 ? "&6⬛" : i == 1 ? "&7⬛" : i == 2 ? "&c⬛" : "&8#" + (i+1);
                    player.sendMessage(c(medal + " &f" + om.getOwnerName(isl)
                        + " &8| &e" + isl.getBlocksBroken() + " &7blocs"
                        + " &8| " + isl.getCurrentPhase().getColor() + isl.getCurrentPhase().getDisplayName()
                        + " &8| &bNiv." + isl.getIslandLevel()));
                }
            }
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /ob invite <joueur>")); return true; }
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                if (!isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut inviter.")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable ou hors ligne.")); return true; }
                if (isl.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&c" + target.getName() + " est déjà membre.")); return true; }
                boolean sent = om.invitePlayer(player.getUniqueId(), target.getUniqueId());
                if (sent) {
                    player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a !"));
                    target.sendMessage(pre() + c("&e" + player.getName() + " &7t'invite sur son île OneBlock !"));
                    target.sendMessage(pre() + c("&aTape &e/ob accept &aou &c/ob decline &c(expire dans 60s)"));
                } else {
                    player.sendMessage(pre() + c("&cImpossible d'inviter ce joueur."));
                }
            }
            case "accept" -> {
                boolean accepted = om.acceptInvite(player.getUniqueId());
                if (accepted) {
                    player.sendMessage(pre() + c("&aInvitation acceptée ! Téléportation vers l'île..."));
                    OneBlockIsland isl = om.getIsland(player.getUniqueId());
                    if (isl != null) player.teleport(isl.getHome());
                } else {
                    player.sendMessage(pre() + c("&cAucune invitation en attente."));
                }
            }
            case "decline" -> {
                boolean declined = om.declineInvite(player.getUniqueId());
                player.sendMessage(pre() + c(declined ? "&7Invitation refusée." : "&cAucune invitation en attente."));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /ob kick <joueur>")); return true; }
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut expulser.")); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                UUID targetId = target != null ? target.getUniqueId() : null;
                if (targetId == null) {
                    player.sendMessage(pre() + c("&cJoueur introuvable. Utilise le menu /ob membres."));
                    return true;
                }
                boolean kicked = om.kickMember(player.getUniqueId(), targetId);
                if (kicked) {
                    player.sendMessage(pre() + c("&e" + args[1] + " &7a été expulsé de l'île."));
                    if (target != null) target.sendMessage(pre() + c("&cTu as été expulsé de l'île de &e" + player.getName() + "&c."));
                } else {
                    player.sendMessage(pre() + c("&cImpossible d'expulser ce joueur."));
                }
            }
            case "leave" -> {
                boolean left = om.leaveIsland(player.getUniqueId());
                player.sendMessage(pre() + c(left ? "&cTu as quitté l'île." : "&cTu ne peux pas quitter l'île (tu en es propriétaire ou tu n'as pas d'île)."));
            }
            case "delete" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut supprimer l'île.")); return true; }
                if (args.length < 2 || !args[1].equals("confirmer")) {
                    player.sendMessage(pre() + c("&c⚠ Attention ! Cette action est irréversible."));
                    player.sendMessage(pre() + c("&cTape &e/ob delete confirmer &cpour supprimer définitivement ton île."));
                    return true;
                }
                om.deleteIsland(player.getUniqueId());
                player.sendMessage(pre() + c("&cTon île a été supprimée."));
            }
            case "warp" -> {
                if (args.length < 2) {
                    warpGUI.open(player, 0);
                    return true;
                }
                String warpName = args[1];
                for (OneBlockIsland isl : om.getPublicWarps()) {
                    String name = isl.getWarpName().isEmpty() ? om.getOwnerName(isl) : isl.getWarpName();
                    if (name.equalsIgnoreCase(warpName)) {
                        player.teleport(isl.getHome());
                        player.sendMessage(pre() + c("&aTéléporté vers le warp &e" + name + "&a !"));
                        return true;
                    }
                }
                player.sendMessage(pre() + c("&cWarp introuvable : &e" + warpName));
            }
            case "setwarp" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null || !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut définir le warp.")); return true; }
                String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
                isl.setWarpName(name);
                om.saveAll();
                player.sendMessage(pre() + c("&aNom du warp défini : &e" + (name.isEmpty() ? "(aucun)" : name)));
            }
            case "membres", "members", "team" -> membersGUI.open(player);
            case "upgrades", "ameliorations", "up" -> upgradesGUI.open(player);
            case "defis", "challenges", "quetes" -> challengesGUI.open(player);
            case "warps" -> warpGUI.open(player, 0);
            case "settings", "parametres" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl != null && !isl.isOwner(player.getUniqueId())) { player.sendMessage(pre() + c("&cSeul le propriétaire peut accéder aux paramètres.")); return true; }
                settingsGUI.open(player);
            }
            case "help", "aide" -> sendHelp(player);
            default -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl != null) menuGUI.open(player);
                else sendHelp(player);
            }
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(c("&8&m────── &6&lOneBlock &7- Aide &8&m──────"));
        player.sendMessage(c("&e/ob &7— Ouvre le menu principal"));
        player.sendMessage(c("&e/ob create &7— Créer ton île"));
        player.sendMessage(c("&e/ob home &7— Aller à ton île"));
        player.sendMessage(c("&e/ob sethome &7— Définir ton point d'apparition"));
        player.sendMessage(c("&e/ob info &7— Infos de ton île"));
        player.sendMessage(c("&e/ob invite <joueur> &7— Inviter un joueur"));
        player.sendMessage(c("&e/ob kick <joueur> &7— Expulser un membre"));
        player.sendMessage(c("&e/ob leave &7— Quitter une île"));
        player.sendMessage(c("&e/ob upgrades &7— Améliorations"));
        player.sendMessage(c("&e/ob defis &7— Voir les défis"));
        player.sendMessage(c("&e/ob warps &7— Warps publics"));
        player.sendMessage(c("&e/ob top &7— Classement"));
        player.sendMessage(c("&8&m────────────────────────────"));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList("create","home","sethome","info","phase","top",
                "invite","kick","accept","decline","leave","delete","warp","setwarp",
                "membres","upgrades","defis","warps","settings","help");
            return subs.stream().filter(sub -> sub.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick"))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
