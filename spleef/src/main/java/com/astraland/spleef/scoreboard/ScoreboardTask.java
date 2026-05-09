package com.astraland.spleef.scoreboard;

import com.astraland.spleef.Spleef;
import com.astraland.spleef.models.SpleefGame;
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

    private final Spleef plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(Spleef plugin) { this.plugin = plugin; }

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
            Scoreboard board = boards.get(p.getUniqueId());
            if (!p.getScoreboard().equals(board)) p.setScoreboard(board);
            update(p, board);
        }
    }

    private void update(Player p, Scoreboard board) {
        UUID uid = p.getUniqueId();
        SpleefGame game = plugin.getSpleefManager().getPlayerGame(uid);

        String gameLine, stateLine, playersLine, aliveLine, statusLine;
        if (game != null) {
            gameLine = "&e" + game.getName();
            stateLine = switch (game.getState()) {
                case WAITING   -> "&7En attente...";
                case COUNTDOWN -> "&6Départ dans &e" + game.getCountdown() + "s";
                case INGAME    -> "&aEn jeu";
                case FINISHED  -> "&cTerminé";
            };
            playersLine = "&f" + (game.getPlayers().size() + game.getEliminated().size())
                + "&7/" + game.getMaxPlayers();
            boolean alive = game.isAlive(uid);
            aliveLine   = alive ? "&aVivant" : "&cÉliminé";
            statusLine  = "&f" + game.getPlayers().size() + " &7restant(s)";
        } else {
            gameLine    = "&8Lobby";
            stateLine   = "&7En attente...";
            playersLine = "&8-";
            aliveLine   = "&8-";
            statusLine  = "&8-";
        }

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&7─────────");
        setLine(board, 10, "&7Partie: " + gameLine);
        setLine(board, 9,  "&7Joueurs: " + playersLine);
        setLine(board, 8,  "&7État: " + stateLine);
        setLine(board, 7,  "&7─────────");
        setLine(board, 6,  "&7Statut: " + aliveLine);
        setLine(board, 5,  "&7Restants: " + statusLine);
        setLine(board, 4,  "&7─────────");
        setLine(board, 3,  "&7Mode: &eSpleef");
        setLine(board, 2,  " ");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  " ");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
