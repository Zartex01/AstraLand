package com.astraland.buildbattle.listeners;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.managers.BuildBattleManager;
import com.astraland.buildbattle.models.BBGame;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BuildBattleListener implements Listener {

    private final BuildBattle plugin;

    public BuildBattleListener(BuildBattle plugin) { this.plugin = plugin; }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        BuildBattleManager bm = plugin.getBuildBattleManager();
        BBGame game = bm.getPlayerGame(player.getUniqueId());
        if (game == null) return;

        if (game.getState() != BBGame.State.BUILDING) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTu ne peux pas construire pendant cette phase."));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        BuildBattleManager bm = plugin.getBuildBattleManager();
        BBGame game = bm.getPlayerGame(player.getUniqueId());
        if (game == null) return;

        if (game.getState() != BBGame.State.BUILDING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (!plugin.isInPluginWorld(attacker)) return;

        BuildBattleManager bm = plugin.getBuildBattleManager();
        BBGame game = bm.getPlayerGame(attacker.getUniqueId());
        if (game != null) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isInPluginWorld(event.getPlayer())) return;
        BuildBattleManager bm = plugin.getBuildBattleManager();
        BBGame game = bm.getPlayerGame(event.getPlayer().getUniqueId());
        if (game != null) bm.leaveGame(game, event.getPlayer());
    }
}
