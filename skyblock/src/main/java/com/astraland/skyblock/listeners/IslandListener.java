package com.astraland.skyblock.listeners;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class IslandListener implements Listener {

    private final Skyblock plugin;

    public IslandListener(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.skyblock.admin")) return;

        String worldName = plugin.getConfig().getString("island.world", "world_skyblock");
        if (!event.getBlock().getWorld().getName().equals(worldName)) return;

        IslandManager im = plugin.getIslandManager();
        if (!im.isInsideOwnIsland(player.getUniqueId(), event.getBlock().getLocation())) {
            Island island = im.getIsland(player.getUniqueId());
            if (island == null || !island.isInsideIsland(event.getBlock().getLocation(), plugin.getConfig().getInt("island.size", 100))) {
                event.setCancelled(true);
                player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cCette zone est protégée !")));
            } else {
                island.addBlocksBroken(1);
            }
        } else {
            Island island = im.getIsland(player.getUniqueId());
            if (island != null) island.addBlocksBroken(1);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.skyblock.admin")) return;

        String worldName = plugin.getConfig().getString("island.world", "world_skyblock");
        if (!event.getBlock().getWorld().getName().equals(worldName)) return;

        IslandManager im = plugin.getIslandManager();
        Island ownIsland = im.getOwnedIsland(player.getUniqueId());
        int size = plugin.getConfig().getInt("island.size", 100);

        if (ownIsland != null && ownIsland.isInsideIsland(event.getBlock().getLocation(), size)) return;

        Island memberIsland = im.getIsland(player.getUniqueId());
        if (memberIsland != null && memberIsland.isInsideIsland(event.getBlock().getLocation(), size)) return;

        event.setCancelled(true);
        player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cCette zone est protégée !")));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        String worldName = plugin.getConfig().getString("island.world", "world_skyblock");
        if (!victim.getWorld().getName().equals(worldName)) return;

        IslandManager im = plugin.getIslandManager();
        Island victimIsland = im.getIsland(victim.getUniqueId());
        Island attackerIsland = im.getIsland(attacker.getUniqueId());

        if (victimIsland != null && attackerIsland != null
                && victimIsland.getOwner().equals(attackerIsland.getOwner())) return;

        int size = plugin.getConfig().getInt("island.size", 100);
        if (victimIsland != null && victimIsland.isInsideIsland(victim.getLocation(), size)) {
            if (!victimIsland.isMember(attacker.getUniqueId())) {
                event.setCancelled(true);
                attacker.sendMessage(c("&cTu ne peux pas attaquer sur cette île."));
            }
        }
    }
}
