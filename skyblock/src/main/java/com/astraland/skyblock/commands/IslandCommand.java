package com.astraland.skyblock.commands;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class IslandCommand implements CommandExecutor, TabCompleter {

    private final Skyblock plugin;
    public IslandCommand(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&a&lSkyblock&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        IslandManager im = plugin.getIslandManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "home";

        switch (sub) {
            case "create" -> {
                if (im.hasIsland(player.getUniqueId())) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.island-exists", "&cTu as déjà une île !"))); return true; }
                Island isl = im.createIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cErreur lors de la création. Le monde skyblock est-il chargé ?")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.island-created", "&aIle créée !")));
            }
            case "home" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-island", "&cTu n'as pas d'île."))); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c(plugin.getConfig().getString("messages.teleporting", "&aTéléportation vers ton île...")));
            }
            case "sethome" -> {
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire d'une île.")); return true; }
                if (!im.isInsideOwnIsland(player.getUniqueId(), player.getLocation())) { player.sendMessage(pre() + c("&cTu dois être sur ton île pour définir le home.")); return true; }
                isl.setHome(player.getLocation());
                im.saveAll();
                player.sendMessage(pre() + c("&aHome défini ici."));
            }
            case "invite" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is invite <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire d'une île.")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { player.sendMessage(pre() + c("&cJoueur introuvable.")); return true; }
                isl.invite(target.getUniqueId());
                player.sendMessage(pre() + c("&aInvitation envoyée à &e" + target.getName() + "&a. Il peut utiliser &e/is join " + player.getName()));
                target.sendMessage(pre() + c("&e" + player.getName() + " &at'invite sur son île ! Utilise &e/is join " + player.getName()));
            }
            case "join" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is join <propriétaire>")); return true; }
                Player owner = Bukkit.getPlayerExact(args[1]);
                if (owner == null) { player.sendMessage(pre() + c("&cProprietaire introuvable.")); return true; }
                Island isl = im.getOwnedIsland(owner.getUniqueId());
                if (isl == null || !isl.isInvited(player.getUniqueId())) { player.sendMessage(pre() + c("&cAucune invitation valide.")); return true; }
                im.addMember(isl, player.getUniqueId());
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aRejoint l'île de &e" + owner.getName() + "&a !"));
                owner.sendMessage(pre() + c("&e" + player.getName() + " &aa rejoint ton île."));
            }
            case "kick" -> {
                if (args.length < 2) { player.sendMessage(pre() + c("&cUsage : /is kick <joueur>")); return true; }
                Island isl = im.getOwnedIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu dois être propriétaire d'une île.")); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null || !isl.isMember(target.getUniqueId())) { player.sendMessage(pre() + c("&cCe joueur n'est pas sur ton île.")); return true; }
                im.removeMember(isl, target.getUniqueId());
                target.sendMessage(pre() + c(plugin.getConfig().getString("messages.kicked", "&cTu as été expulsé de l'île.")));
                player.sendMessage(pre() + c("&e" + target.getName() + " &aexpulsé."));
            }
            case "info" -> {
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-island"))); return true; }
                Player owner = Bukkit.getPlayer(isl.getOwner());
                player.sendMessage(c("&8&m------- &a&lIle &8&m-------"));
                player.sendMessage(c("&7Propriétaire : &e" + (owner != null ? owner.getName() : "Hors ligne")));
                player.sendMessage(c("&7Membres : &e" + isl.getMembers().size()));
                player.sendMessage(c("&7Niveau : &e" + isl.getLevel()));
                player.sendMessage(c("&7Blocs cassés : &e" + isl.getBlocksBroken()));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "top" -> {
                player.sendMessage(c("&6=== Top Îles Skyblock ==="));
                List<Island> top = im.getTopIslands(10);
                for (int i = 0; i < top.size(); i++) {
                    Island isl = top.get(i);
                    Player owner = Bukkit.getPlayer(isl.getOwner());
                    player.sendMessage(c("&e#" + (i + 1) + " &f" + (owner != null ? owner.getName() : "?") + " &7- Niveau &e" + isl.getLevel()));
                }
            }
            case "leave" -> {
                if (im.getOwnedIsland(player.getUniqueId()) != null) { player.sendMessage(pre() + c("&cTu es propriétaire. Utilise &e/is delete &cpour supprimer ton île.")); return true; }
                Island isl = im.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c(plugin.getConfig().getString("messages.no-island"))); return true; }
                im.removeMember(isl, player.getUniqueId());
                player.sendMessage(pre() + c("&aQuitté l'île."));
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(c("&a&l=== Commandes /island ==="));
        p.sendMessage(c("&e/is create &7- Créer ton île")); p.sendMessage(c("&e/is home &7- Aller à ton île"));
        p.sendMessage(c("&e/is sethome &7- Définir le home")); p.sendMessage(c("&e/is invite <joueur> &7- Inviter"));
        p.sendMessage(c("&e/is join <proprio> &7- Rejoindre une île")); p.sendMessage(c("&e/is kick <joueur> &7- Expulser"));
        p.sendMessage(c("&e/is info &7- Infos île")); p.sendMessage(c("&e/is top &7- Classement"));
        p.sendMessage(c("&e/is leave &7- Quitter une île"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("create", "home", "sethome", "invite", "join", "kick", "info", "top", "leave");
        return List.of();
    }
}
