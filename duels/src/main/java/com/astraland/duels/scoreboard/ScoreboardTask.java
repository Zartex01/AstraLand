package com.astraland.duels.scoreboard;

import com.astraland.duels.Duels;
import com.astraland.duels.models.DuelMatch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

    private final Duels plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private final Map<UUID, Integer> wins   = new HashMap<>();
    private final Map<UUID, Integer> losses = new HashMap<>();

    public ScoreboardTask(Duels plugin) { this.plugin = plugin; }

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

    public void addWin(UUID uuid)  { wins.merge(uuid, 1, Integer::sum); }
    public void addLoss(UUID uuid) { losses.merge(uuid, 1, Integer::sum); }

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
        DuelMatch match = plugin.getDuelManager().getMatch(uid);
        String kit = plugin.getDuelManager().getSelectedKit(uid);
        int w = wins.getOrDefault(uid, 0);
        int l = losses.getOrDefault(uid, 0);

        String opponentLine, stateLine, durationLine, kitLine;
        if (match != null) {
            UUID oppId = match.getOpponent(uid);
            OfflinePlayer opp = oppId != null ? Bukkit.getOfflinePlayer(oppId) : null;
            String oppName = (opp != null && opp.getName() != null) ? opp.getName() : "?";
            opponentLine = "&c" + oppName;
            stateLine    = "&aEn duel";
            long elapsed = (System.currentTimeMillis() - match.getStartTime()) / 1000;
            long min = elapsed / 60, sec = elapsed % 60;
            durationLine = "&f" + min + ":" + String.format("%02d", sec);
            kitLine      = "&e" + match.getKit();
        } else {
            opponentLine = "&8Aucun";
            stateLine    = "&7En attente...";
            durationLine = "&8-";
            kitLine      = "&e" + kit;
        }

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&7─────────");
        setLine(board, 10, "&7Kit: " + kitLine);
        setLine(board, 9,  "&7─────────");
        setLine(board, 8,  "&7Adversaire: " + opponentLine);
        setLine(board, 7,  "&7Statut: " + stateLine);
        setLine(board, 6,  "&7Durée: " + durationLine);
        setLine(board, 5,  "&7─────────");
        setLine(board, 4,  "&7V: &a" + w + "  &7D: &c" + l);
        setLine(board, 3,  "&7─────────");
        setLine(board, 2,  "&7Mode: &eDuels");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  " ");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
