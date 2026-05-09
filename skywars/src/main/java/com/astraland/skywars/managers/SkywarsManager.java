package com.astraland.skywars.managers;

import com.astraland.skywars.Skywars;
import com.astraland.skywars.models.SkywarsArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SkywarsManager {

    private final Skywars plugin;
    private final Map<String, SkywarsArena> arenas = new LinkedHashMap<>();
    private final Map<String, BukkitTask> countdownTasks = new HashMap<>();

    public SkywarsManager(Skywars plugin) {
        this.plugin = plugin;
    }

    public SkywarsArena createArena(String name) {
        int min = plugin.getConfig().getInt("skywars.min-players", 2);
        SkywarsArena arena = new SkywarsArena(name, min, 12);
        arenas.put(name.toLowerCase(), arena);
        return arena;
    }

    public SkywarsArena getArena(String name) { return arenas.get(name.toLowerCase()); }

    public SkywarsArena getPlayerArena(UUID uuid) {
        for (SkywarsArena a : arenas.values()) if (a.isInArena(uuid)) return a;
        return null;
    }

    public Collection<SkywarsArena> getArenas() { return arenas.values(); }

    public boolean joinArena(SkywarsArena arena, Player player) {
        if (arena.getState() != SkywarsArena.State.WAITING && arena.getState() != SkywarsArena.State.COUNTDOWN) return false;
        if (!arena.addPlayer(player.getUniqueId())) return false;
        if (arena.getLobby() != null) player.teleport(arena.getLobby());
        if (arena.getPlayers().size() >= arena.getMinPlayers() && arena.getState() == SkywarsArena.State.WAITING)
            startCountdown(arena);
        return true;
    }

    public void leaveArena(SkywarsArena arena, Player player) {
        arena.removePlayer(player.getUniqueId());
        player.getInventory().clear();
        if (arena.getState() == SkywarsArena.State.COUNTDOWN && arena.getPlayers().size() < arena.getMinPlayers()) {
            cancelCountdown(arena);
        }
        if (arena.getState() == SkywarsArena.State.INGAME) eliminatePlayer(arena, player);
    }

    public void eliminatePlayer(SkywarsArena arena, Player player) {
        arena.removePlayer(player.getUniqueId());
        broadcast(arena, "&c" + player.getName() + " &aest éliminé ! &7(" + arena.getPlayers().size() + " restants)");
        UUID winner = arena.getWinner();
        if (winner != null) {
            Player w = Bukkit.getPlayer(winner);
            broadcast(arena, "&6" + (w != null ? w.getName() : "?") + " &agagne la partie !");
            arena.setState(SkywarsArena.State.FINISHED);
            Bukkit.getScheduler().runTaskLater(plugin, () -> resetArena(arena), 100L);
        }
    }

    private void startCountdown(SkywarsArena arena) {
        arena.setState(SkywarsArena.State.COUNTDOWN);
        int[] timer = {arena.getCountdown()};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timer[0] <= 0) { startGame(arena); return; }
            if (timer[0] <= 5 || timer[0] % 10 == 0)
                broadcast(arena, "&aLa partie commence dans &e" + timer[0] + "s&a !");
            timer[0]--;
        }, 0L, 20L);
        countdownTasks.put(arena.getName(), task);
    }

    private void cancelCountdown(SkywarsArena arena) {
        BukkitTask t = countdownTasks.remove(arena.getName());
        if (t != null) t.cancel();
        arena.setState(SkywarsArena.State.WAITING);
        broadcast(arena, "&cPas assez de joueurs !");
    }

    private void startGame(SkywarsArena arena) {
        BukkitTask t = countdownTasks.remove(arena.getName());
        if (t != null) t.cancel();
        arena.setState(SkywarsArena.State.INGAME);
        arena.getPlayers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) return;
            Location spawnLoc = arena.getSpawnFor(uuid);
            if (spawnLoc != null) p.teleport(spawnLoc);
            giveKit(p, arena.getPlayerKits().getOrDefault(uuid, "WARRIOR"));
        });
        broadcast(arena, "&aLa partie Skywars commence !");
    }

    private void giveKit(Player player, String kitName) {
        player.getInventory().clear();
        switch (kitName.toUpperCase()) {
            case "ARCHER" -> {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW));
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW, 32));
                player.getInventory().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.LEATHER_CHESTPLATE));
            }
            case "MAGE" -> {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_ROD));
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ENDER_PEARL, 4));
            }
            default -> {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_SWORD));
                player.getInventory().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
            }
        }
    }

    private void resetArena(SkywarsArena arena) {
        arena.getPlayers().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && arena.getLobby() != null) p.teleport(arena.getLobby());
        });
        SkywarsArena fresh = new SkywarsArena(arena.getName(), arena.getMinPlayers(), arena.getMaxPlayers());
        fresh.setLobby(arena.getLobby());
        fresh.getSpawns().addAll(arena.getSpawns());
        arenas.put(arena.getName().toLowerCase(), fresh);
    }

    private void broadcast(SkywarsArena arena, String msg) {
        String colored = ChatColor.translateAlternateColorCodes('&', msg);
        arena.getPlayers().forEach(uuid -> { if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(colored); });
    }

    public void stopAll() {
        countdownTasks.values().forEach(BukkitTask::cancel);
        countdownTasks.clear();
    }
}
