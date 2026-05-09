package com.astraland.bedwars.scoreboard;

import com.astraland.bedwars.Bedwars;
import com.astraland.bedwars.models.Arena;
import com.astraland.bedwars.models.BedwarsTeam;
import com.astraland.bedwars.models.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardTask implements Listener {

    private static final String[] E = {
        "§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d"
    };

    private final Bedwars plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(Bedwars plugin) { this.plugin = plugin; }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
        for (Player p : Bukkit.getOnlinePlayers()) if (plugin.isInPluginWorld(p)) init(p);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
        HandlerList.unregisterAll(this);
        boards.forEach((uuid, b) -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        });
        boards.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.isInPluginWorld(e.getPlayer())) init(e.getPlayer());
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { boards.remove(e.getPlayer().getUniqueId()); }

    private void init(Player p) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("astraland", "dummy", c("&6&l✦ AstraLand ✦"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < E.length; i++) {
            Team t = board.registerNewTeam("l" + i);
            t.addEntry(E[i]);
            obj.getScore(E[i]).setScore(i);
        }
        boards.put(p.getUniqueId(), board);
        p.setScoreboard(board);
    }

    private void setLine(Scoreboard b, int slot, String text) {
        Team t = b.getTeam("l" + slot);
        if (t == null) return;
        String s = c(text);
        t.setPrefix(s.length() > 64 ? s.substring(0, 64) : s);
    }

    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!plugin.isInPluginWorld(p)) continue;
            if (!boards.containsKey(p.getUniqueId())) init(p);
            update(p, boards.get(p.getUniqueId()));
        }
    }

    private void update(Player p, Scoreboard board) {
        UUID uid = p.getUniqueId();
        Arena arena = plugin.getArenaManager().getPlayerArena(uid);

        String arenaLine, stateLine, teamLine, playersLine, bedLine, countdownLine;
        if (arena != null) {
            arenaLine = "&e" + arena.getName();
            GameState gs = arena.getState();
            stateLine = switch (gs) {
                case WAITING   -> "&7En attente...";
                case COUNTDOWN -> "&6Départ dans &e" + arena.getCountdown() + "s";
                case INGAME    -> "&aEn jeu";
                case FINISHED  -> "&cTerminé";
            };
            BedwarsTeam bt = arena.getPlayerTeam(uid);
            teamLine = bt != null ? c(bt.getColor() + bt.getName()) : "&8Aucune";
            playersLine = "&f" + arena.getPlayerCount() + "&7/" + arena.getMaxPlayers();
            long aliveTeams = arena.getAliveTeams().size();
            bedLine = "&f" + aliveTeams + " &7équipe(s) restante(s)";
            countdownLine = gs == GameState.COUNTDOWN ? "&6" + arena.getCountdown() + "s" : "&8-";
        } else {
            arenaLine    = "&8Lobby";
            stateLine    = "&7En attente d'une partie";
            teamLine     = "&8-";
            playersLine  = "&8-";
            bedLine      = "&8-";
            countdownLine = "&8-";
        }

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&8──────────────");
        setLine(board, 10, "&7Arène: " + arenaLine);
        setLine(board, 9,  "&7Équipe: " + teamLine);
        setLine(board, 8,  "&7Joueurs: " + playersLine);
        setLine(board, 7,  "&7État: " + stateLine);
        setLine(board, 6,  "&8──────────────");
        setLine(board, 5,  "&7Équipes restantes: " + bedLine);
        setLine(board, 4,  "&8──────────────");
        setLine(board, 3,  "&7Mode: &eBedWars");
        setLine(board, 2,  " ");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  "&e    » /vote");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
