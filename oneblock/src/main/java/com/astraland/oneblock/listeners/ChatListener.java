package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.IslandRole;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Skill;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final OneBlock plugin;

    public ChatListener(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;

        OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());

        String prestigeTag = "";
        String skillTag = "";
        String roleTag = "";

        if (island != null) {
            int prestige = island.getPrestige();
            if (prestige >= 10) prestigeTag = c("&5[P" + prestige + "] ");
            else if (prestige >= 5) prestigeTag = c("&d[P" + prestige + "] ");
            else if (prestige >= 1) prestigeTag = c("&9[P" + prestige + "] ");

            IslandRole role = island.getRole(player.getUniqueId());
            roleTag = switch (role) {
                case OWNER    -> c("&6[Proprio] ");
                case CO_OWNER -> c("&e[Co-Proprio] ");
                case MEMBER   -> c("&a[Membre] ");
                default       -> "";
            };

            int miningLevel = plugin.getSkillManager().getLevel(player.getUniqueId(), Skill.MINING);
            if (miningLevel >= 50) skillTag = c("&b[Maître] ");
            else if (miningLevel >= 25) skillTag = c("&3[Expert] ");
        }

        String displayName = player.getDisplayName();
        String message = event.getMessage();

        event.setFormat(prestigeTag + skillTag + roleTag
            + displayName + ChatColor.WHITE + ": " + message);
    }
}
