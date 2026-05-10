package com.astraland.bedwars.managers;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.models.Arena;
import com.astraland.bedwars.models.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final Bedwars plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private final Map<String, BukkitTask> countdownTasks = new HashMap<>();
    private File dataFile;

    public ArenaManager(Bedwars plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public Arena createArena(String name) {
        int min = plugin.getConfig().getInt("bedwars.min-players", 2);
        Arena arena = new Arena(name, min, min * 4);
        arenas.put(name.toLowerCase(), arena);
        saveAll();
        return arena;
    }

    public Arena getArena(String name) { return arenas.get(name.toLowerCase()); }

    public Arena getPlayerArena(UUID uuid) {
        for (Arena a : arenas.values()) if (a.isInArena(uuid)) return a;
        return null;
    }

    public Collection<Arena> getArenas() { return arenas.values(); }

    public boolean joinArena(Arena arena, UUID uuid) {
        if (arena.getState() != GameState.WAITING && arena.getState() != GameState.COUNTDOWN) return false;
        if (!arena.addPlayer(uuid)) return false;
        if (arena.getPlayerCount() >= arena.getMinPlayers() && arena.getState() == GameState.WAITING) {
            startCountdown(arena);
        }
        return true;
    }

    public void leaveArena(Arena arena, UUID uuid) {
        arena.removePlayer(uuid);
        if (arena.getState() == GameState.COUNTDOWN && arena.getPlayerCount() < arena.getMinPlayers()) {
            cancelCountdown(arena);
            arena.setState(GameState.WAITING);
        }
    }

    private void startCountdown(Arena arena) {
        arena.setState(GameState.COUNTDOWN);
        int[] timer = {plugin.getConfig().getInt("bedwars.countdown", 30)};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timer[0] <= 0) {
                startGame(arena);
                return;
            }
            if (timer[0] <= 5 || timer[0] % 10 == 0) {
                broadcastToArena(arena, plugin.getConfig().getString("prefix", "&8[&c&lBedwars&8] &r")
                    + "&aLa partie commence dans &e" + timer[0] + " &asecondes !");
            }
            timer[0]--;
        }, 0L, 20L);
        countdownTasks.put(arena.getName(), task);
    }

    private void cancelCountdown(Arena arena) {
        BukkitTask t = countdownTasks.remove(arena.getName());
        if (t != null) t.cancel();
        broadcastToArena(arena, plugin.getConfig().getString("prefix", "&8[&c&lBedwars&8] &r")
            + "&cPas assez de joueurs. Compte à rebours annulé.");
    }

    private void startGame(Arena arena) {
        BukkitTask t = countdownTasks.remove(arena.getName());
        if (t != null) t.cancel();
        arena.setState(GameState.INGAME);
        broadcastToArena(arena, plugin.getConfig().getString("prefix", "") + "&aLa partie commence !");
        arena.getTeams().forEach((name, team) -> {
            team.getPlayers().forEach(uuid -> {
                if (Bukkit.getPlayer(uuid) != null && team.getSpawn() != null)
                    Bukkit.getPlayer(uuid).teleport(team.getSpawn());
            });
        });
    }

    public void checkWin(Arena arena) {
        if (arena.getState() != GameState.INGAME) return;
        List<com.astraland.bedwars.models.BedwarsTeam> alive = arena.getAliveTeams();
        if (alive.size() <= 1) {
            arena.setState(GameState.FINISHED);
            String winner = alive.isEmpty() ? "&cPersonne" : alive.get(0).getColor() + alive.get(0).getName();
            broadcastToArena(arena, "&6Gagnant : " + winner + " &6!");
            if (!alive.isEmpty()) {
                int winReward = plugin.getConfig().getInt("economy.win-reward", 100);
                for (java.util.UUID uuid : alive.get(0).getPlayers()) {
                    plugin.getEconomyManager().addBalance(uuid, winReward);
                    if (Bukkit.getPlayer(uuid) != null)
                        Bukkit.getPlayer(uuid).sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&a+" + winReward + " pièces &7pour la victoire !"));
                }
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> resetArena(arena), 100L);
        }
    }

    public void resetArena(Arena arena) {
        arena.getPlayerTeamMap().forEach((uuid, team) -> {
            if (Bukkit.getPlayer(uuid) != null && arena.getLobby() != null)
                Bukkit.getPlayer(uuid).teleport(arena.getLobby());
        });
        arenas.put(arena.getName().toLowerCase(), new Arena(arena.getName(), arena.getMinPlayers(), arena.getMaxPlayers()));
    }

    private void broadcastToArena(Arena arena, String msg) {
        String colored = ChatColor.translateAlternateColorCodes('&', msg);
        arena.getPlayerTeamMap().keySet().forEach(uuid -> {
            if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(colored);
        });
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Arena a : arenas.values()) {
            cfg.set("arenas." + a.getName() + ".min", a.getMinPlayers());
            cfg.set("arenas." + a.getName() + ".max", a.getMaxPlayers());
        }
        try { cfg.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("arenas") == null) return;
        for (String name : cfg.getConfigurationSection("arenas").getKeys(false)) {
            int min = cfg.getInt("arenas." + name + ".min", 2);
            int max = cfg.getInt("arenas." + name + ".max", 8);
            arenas.put(name.toLowerCase(), new Arena(name, min, max));
        }
    }
}
