package com.astraland.oneblock.commands;

import com.astraland.oneblock.OneBlock;
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

import java.util.Arrays;
import java.util.List;

public class OneBlockCommand implements CommandExecutor, TabCompleter {

    private final OneBlock plugin;
    public OneBlockCommand(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&6&lOneBlock&8] &r")); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }
        OneBlockManager om = plugin.getOneBlockManager();
        String sub = args.length > 0 ? args[0].toLowerCase() : "home";

        switch (sub) {
            case "create" -> {
                if (om.hasIsland(player.getUniqueId())) { player.sendMessage(pre() + c("&cTu as déjà une île OneBlock !")); return true; }
                OneBlockIsland isl = om.createIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cErreur. Le monde oneblock est-il chargé ?")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aIle OneBlock créée ! Casse le bloc magique pour commencer."));
            }
            case "home" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île. Utilise &e/ob create&c.")); return true; }
                player.teleport(isl.getHome());
                player.sendMessage(pre() + c("&aTéléportation vers ton île..."));
            }
            case "info" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                Phase phase = isl.getCurrentPhase();
                player.sendMessage(c("&8&m------ &6&lOneBlock &8&m------"));
                player.sendMessage(c("&7Blocs cassés : &e" + isl.getBlocksBroken()));
                player.sendMessage(c("&7Phase actuelle : " + phase.getColor() + "&l" + phase.getDisplayName()));
                Phase nextPhase = null;
                for (Phase p : Phase.values()) { if (p.getBlocksRequired() > isl.getBlocksBroken()) { nextPhase = p; break; } }
                if (nextPhase != null) player.sendMessage(c("&7Prochaine phase : &e" + nextPhase.getDisplayName() + " &7dans &e" + (nextPhase.getBlocksRequired() - isl.getBlocksBroken()) + " &7blocs"));
                player.sendMessage(c("&7Membres : &e" + isl.getMembers().size()));
                player.sendMessage(c("&8&m---------------------"));
            }
            case "phase" -> {
                OneBlockIsland isl = om.getIsland(player.getUniqueId());
                if (isl == null) { player.sendMessage(pre() + c("&cTu n'as pas d'île.")); return true; }
                player.sendMessage(c("&6=== Phases OneBlock ==="));
                for (Phase p : Phase.values()) {
                    boolean current = p == isl.getCurrentPhase();
                    boolean unlocked = isl.getBlocksBroken() >= p.getBlocksRequired();
                    String status = current ? " &6[ACTUELLE]" : unlocked ? " &a[DÉBLOQUÉE]" : " &7[" + p.getBlocksRequired() + " blocs]";
                    player.sendMessage(c(p.getColor() + p.getDisplayName() + status));
                }
            }
            case "top" -> {
                player.sendMessage(c("&6=== Top OneBlock ==="));
                List<OneBlockIsland> top = om.getTop(10);
                for (int i = 0; i < top.size(); i++) {
                    OneBlockIsland isl = top.get(i);
                    Player owner = Bukkit.getPlayer(isl.getOwner());
                    player.sendMessage(c("&e#" + (i+1) + " &f" + (owner != null ? owner.getName() : "?") + " &7- &e" + isl.getBlocksBroken() + " &7blocs - " + isl.getCurrentPhase().getColor() + isl.getCurrentPhase().getDisplayName()));
                }
            }
            default -> {
                player.sendMessage(c("&6=== /oneblock ==="));
                player.sendMessage(c("&e/ob create &7- Créer ton île")); player.sendMessage(c("&e/ob home &7- Aller à ton île"));
                player.sendMessage(c("&e/ob info &7- Infos île")); player.sendMessage(c("&e/ob phase &7- Voir les phases"));
                player.sendMessage(c("&e/ob top &7- Classement"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("create", "home", "info", "phase", "top");
        return List.of();
    }
}
