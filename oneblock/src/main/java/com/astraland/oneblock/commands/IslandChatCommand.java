package com.astraland.oneblock.commands;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.IslandRole;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IslandChatCommand implements CommandExecutor {

    private final OneBlock plugin;

    public IslandChatCommand(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Joueur uniquement."); return true; }
        if (!plugin.isInPluginWorld(player)) { player.sendMessage(plugin.wrongWorldMsg()); return true; }

        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
        if (island == null) {
            player.sendMessage(c("&cTu n'as pas d'île. Utilise &e/ob create&c."));
            return true;
        }
        if (!island.isMember(player.getUniqueId())) {
            player.sendMessage(c("&cTu n'es pas membre d'une île."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(c("&cUsage : /ic <message>"));
            return true;
        }

        String message = String.join(" ", args);
        IslandRole role = island.getRole(player.getUniqueId());
        String prefix = c(role.getPrefix());
        String formatted = c("&8[&aÎle&8] " + prefix + " &f" + player.getName() + "&8: &7" + message);

        for (UUID uid : island.getAllMemberUUIDs()) {
            Player member = Bukkit.getPlayer(uid);
            if (member != null) member.sendMessage(formatted);
        }
        return true;
    }
}
