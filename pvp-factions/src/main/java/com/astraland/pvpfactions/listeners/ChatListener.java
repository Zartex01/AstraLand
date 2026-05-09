package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.FactionManager;
import com.astraland.pvpfactions.models.Faction;
import com.astraland.pvpfactions.models.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final PvpFactions plugin;

    public ChatListener(PvpFactions plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        FactionManager fm = plugin.getFactionManager();

        if (!fm.isFactionChat(player.getUniqueId())) return;

        Faction faction = fm.getPlayerFaction(player.getUniqueId());
        if (faction == null) {
            fm.toggleFactionChat(player.getUniqueId());
            return;
        }

        event.setCancelled(true);
        FactionRole role = faction.getMembers().get(player.getUniqueId());
        String roleColor = role == FactionRole.LEADER ? "&6" : role == FactionRole.OFFICER ? "&e" : "&f";
        String msg = ChatColor.translateAlternateColorCodes('&',
            "&8[&cF&8] " + roleColor + player.getName() + "&7: &f" + event.getMessage());

        for (UUID uuid : faction.getMembers().keySet()) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) member.sendMessage(msg);
        }

        plugin.getLogger().info("[FactionChat][" + faction.getName() + "] " + player.getName() + ": " + event.getMessage());
    }
}
