package com.astraland.pvpfactions.listeners;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.ah.AHSellGUI;
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
        if (!plugin.isInPluginWorld(player)) return;

        // AH price input interception
        if (AHSellGUI.AWAITING_PRICE.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            AHSellGUI.SellSession session = AHSellGUI.AWAITING_PRICE.remove(player.getUniqueId());
            String msg = event.getMessage().trim();

            if (msg.equalsIgnoreCase("tap") || msg.equalsIgnoreCase("annuler") || msg.equalsIgnoreCase("cancel")) {
                AHSellGUI.SESSIONS.remove(player.getUniqueId());
                player.getInventory().addItem(session.item.clone());
                player.sendMessage(c("&c[AH] &7Mise en vente annulée. Item rendu."));
                return;
            }

            int price;
            try { price = Integer.parseInt(msg); }
            catch (NumberFormatException e) {
                player.sendMessage(c("&c[AH] &7Prix invalide. Tape un nombre entier ou &ctap &7pour annuler."));
                AHSellGUI.AWAITING_PRICE.put(player.getUniqueId(), session);
                return;
            }
            if (price <= 0) {
                player.sendMessage(c("&c[AH] &7Le prix doit être positif. Réessaie ou tape &ctap &7pour annuler."));
                AHSellGUI.AWAITING_PRICE.put(player.getUniqueId(), session);
                return;
            }
            if (price > 10_000_000) {
                player.sendMessage(c("&c[AH] &7Prix maximum : &e10 000 000 $&c. Réessaie ou tape &ctap&7."));
                AHSellGUI.AWAITING_PRICE.put(player.getUniqueId(), session);
                return;
            }

            session.price = price;
            player.sendMessage(c("&a[AH] &7Prix défini à &6" + price + " $&7."));

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!AHSellGUI.SESSIONS.containsKey(player.getUniqueId())) {
                    AHSellGUI.SESSIONS.put(player.getUniqueId(), session);
                }
                new AHSellGUI(player, session, plugin.getAuctionManager(), plugin.getEconomyManager()).open(player);
            });
            return;
        }

        // Faction chat
        FactionManager fm = plugin.getFactionManager();
        if (!fm.isFactionChat(player.getUniqueId())) return;

        Faction faction = fm.getPlayerFaction(player.getUniqueId());
        if (faction == null) { fm.toggleFactionChat(player.getUniqueId()); return; }

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

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
