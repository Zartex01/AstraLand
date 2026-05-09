package com.astraland.bedwars.listeners;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.managers.ArenaManager;
import com.astraland.bedwars.models.Arena;
import com.astraland.bedwars.models.BedwarsTeam;
import com.astraland.bedwars.models.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BedwarsListener implements Listener {

    private final Bedwars plugin;
    private final Map<UUID, Long> respawnTimers = new HashMap<>();

    public BedwarsListener(Bedwars plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        ArenaManager am = plugin.getArenaManager();
        Arena arena = am.getPlayerArena(victim.getUniqueId());
        if (arena == null || arena.getState() != GameState.INGAME) return;

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        BedwarsTeam team = arena.getPlayerTeam(victim.getUniqueId());
        if (team == null) return;

        if (!team.isBedAlive()) {
            arena.removePlayer(victim.getUniqueId());
            victim.sendMessage(c("&cTon lit est détruit ! Tu es éliminé."));
            broadcastArena(arena, c("&c" + victim.getName() + " &aest éliminé !"));
            am.checkWin(arena);
        } else {
            respawnTimers.put(victim.getUniqueId(), System.currentTimeMillis());
            victim.sendMessage(c("&eRespawn dans 5 secondes..."));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline() && team.getSpawn() != null) {
                    victim.teleport(team.getSpawn());
                    victim.setHealth(victim.getMaxHealth());
                    victim.setFoodLevel(20);
                    victim.sendMessage(c("&aRespawn !"));
                }
            }, 100L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ArenaManager am = plugin.getArenaManager();
        Arena arena = am.getPlayerArena(player.getUniqueId());
        if (arena == null || arena.getState() != GameState.INGAME) return;

        Material type = event.getBlock().getType();
        if (type == Material.RED_BED || type == Material.BLUE_BED
                || type == Material.GREEN_BED || type == Material.YELLOW_BED
                || type.name().endsWith("_BED")) {

            BedwarsTeam bedOwner = null;
            for (BedwarsTeam t : arena.getTeams().values()) {
                if (t.getBedLocation() != null && t.getBedLocation().getBlock().equals(event.getBlock())) {
                    bedOwner = t;
                    break;
                }
            }

            if (bedOwner != null) {
                BedwarsTeam attackerTeam = arena.getPlayerTeam(player.getUniqueId());
                if (attackerTeam != null && attackerTeam.getName().equals(bedOwner.getName())) {
                    event.setCancelled(true);
                    player.sendMessage(c("&cTu ne peux pas détruire ton propre lit !"));
                    return;
                }
                bedOwner.setBedAlive(false);
                broadcastArena(arena, c(bedOwner.getColor() + "Le lit de l'équipe " + bedOwner.getName() + " &caété détruit par &e" + player.getName() + "&c !"));
                bedOwner.getPlayers().forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) p.sendMessage(c("&c&lTon lit a été détruit ! Plus de respawn pour toi."));
                });
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ArenaManager am = plugin.getArenaManager();
        Arena arena = am.getPlayerArena(event.getPlayer().getUniqueId());
        if (arena != null) am.leaveArena(arena, event.getPlayer().getUniqueId());
    }

    private void broadcastArena(Arena arena, String msg) {
        arena.getPlayerTeamMap().keySet().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(msg);
        });
    }
}
