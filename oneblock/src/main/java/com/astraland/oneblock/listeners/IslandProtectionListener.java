package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class IslandProtectionListener implements Listener {

    private final OneBlock plugin;

    public IslandProtectionListener(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return org.bukkit.ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().getWorld().getName().equals(plugin.getPluginWorld())) return;

        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        if (!(damaged instanceof Player victim)) return;

        Player attacker = null;
        if (damager instanceof Player p) attacker = p;

        if (attacker == null) return;

        OneBlockIsland victimIsland = plugin.getOneBlockManager().getIsland(victim.getUniqueId());
        OneBlockIsland attackerIsland = plugin.getOneBlockManager().getIsland(attacker.getUniqueId());

        if (victimIsland == null || attackerIsland == null) {
            event.setCancelled(true);
            attacker.sendMessage(c("&cLe PvP n'est pas disponible ici."));
            return;
        }

        boolean sameIsland = victimIsland.getOwner().equals(attackerIsland.getOwner());
        if (sameIsland && !victimIsland.isPvpEnabled()) {
            event.setCancelled(true);
            attacker.sendMessage(c("&cLe PvP est désactivé sur cette île."));
            return;
        }

        if (!sameIsland) {
            event.setCancelled(true);
            attacker.sendMessage(c("&cTu ne peux pas attaquer des joueurs sur d'autres îles."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!event.getEntity().getWorld().getName().equals(plugin.getPluginWorld())) return;
        event.blockList().clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!event.getBlock().getWorld().getName().equals(plugin.getPluginWorld())) return;
        event.blockList().clear();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;

        if (event.getTo() == null) return;
        if (event.getTo().getY() < -64) {
            OneBlockIsland island = plugin.getOneBlockManager().getIsland(player.getUniqueId());
            if (island != null && island.getHome() != null) {
                player.teleport(island.getHome());
                player.sendMessage(c("&cTu es tombé dans le vide ! Téléportation vers ton île..."));
            }
        }
    }
}
