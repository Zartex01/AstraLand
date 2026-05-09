package com.astraland.spleef.listeners;

import com.astraland.spleef.Spleef;
import com.astraland.spleef.managers.SpleefManager;
import com.astraland.spleef.models.SpleefGame;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpleefListener implements Listener {

    private final Spleef plugin;

    public SpleefListener(Spleef plugin) { this.plugin = plugin; }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        SpleefManager sm = plugin.getSpleefManager();
        SpleefGame game = sm.getPlayerGame(player.getUniqueId());
        if (game == null || game.getState() != SpleefGame.State.INGAME) return;
        if (!game.isAlive(player.getUniqueId())) { event.setCancelled(true); return; }

        Block block = event.getBlock();
        if (block.getType() == Material.SNOW_BLOCK || block.getType() == Material.SNOW) {
            event.setDropItems(false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInPluginWorld(player)) return;
        SpleefManager sm = plugin.getSpleefManager();
        SpleefGame game = sm.getPlayerGame(player.getUniqueId());
        if (game == null || game.getState() != SpleefGame.State.INGAME) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (game.isAlive(player.getUniqueId())) {
                event.setCancelled(true);
                sm.eliminatePlayer(game, player);
            }
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isInPluginWorld(event.getPlayer())) return;
        SpleefManager sm = plugin.getSpleefManager();
        SpleefGame game = sm.getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) sm.leaveGame(game, event.getPlayer());
    }
}
