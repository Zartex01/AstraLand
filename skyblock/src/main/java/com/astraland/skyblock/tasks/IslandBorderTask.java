package com.astraland.skyblock.tasks;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IslandBorderTask {

    private final Skyblock    plugin;
    private final Set<UUID>   borderEnabled = new HashSet<>();
    private BukkitTask        task;

    public IslandBorderTask(Skyblock plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public boolean toggleBorder(UUID uuid) {
        if (borderEnabled.contains(uuid)) { borderEnabled.remove(uuid); return false; }
        else { borderEnabled.add(uuid); return true; }
    }

    public boolean hasBorder(UUID uuid) { return borderEnabled.contains(uuid); }

    private void tick() {
        for (UUID uuid : borderEnabled) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !plugin.isInPluginWorld(player)) continue;

            Island isl = plugin.getIslandManager().getIsland(uuid);
            if (isl == null || isl.getCenter() == null) continue;

            int size  = plugin.getConfig().getInt("island.size", 100);
            int half  = size / 2;
            Location  center = isl.getCenter();
            int       cx     = center.getBlockX();
            int       cy     = player.getLocation().getBlockY();
            int       cz     = center.getBlockZ();

            // Particules sur les 4 bords (N, S, E, W)
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0, 255, 100), 1.5f);

            // Nord et Sud (Z fixe)
            for (int x = cx - half; x <= cx + half; x += 3) {
                spawnParticle(player, x, cy, cz - half, dust);
                spawnParticle(player, x, cy, cz + half, dust);
            }
            // Est et Ouest (X fixe)
            for (int z = cz - half; z <= cz + half; z += 3) {
                spawnParticle(player, cx - half, cy, z, dust);
                spawnParticle(player, cx + half, cy, z, dust);
            }
        }
    }

    private void spawnParticle(Player player, int x, int y, int z, Particle.DustOptions dust) {
        Location loc = new Location(player.getWorld(), x + 0.5, y + 0.5, z + 0.5);
        if (loc.distanceSquared(player.getLocation()) > 2500) return; // 50 blocs max
        player.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0, dust);
    }
}
