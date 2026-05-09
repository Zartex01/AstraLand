package com.astraland.buildbattle.scoreboard;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.models.BBGame;
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

    private final BuildBattle plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(BuildBattle plugin) { this.plugin = plugin; }

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
        BBGame game = plugin.getBuildBattleManager().getPlayerGame(uid);

        String gameLine, stateLine, themeLine, scoreLine, playersLine, builderLine;
        if (game != null) {
            gameLine = "&e" + game.getName();
            stateLine = switch (game.getState()) {
                case WAITING   -> "&7En attente...";
                case COUNTDOWN -> "&6Départ imminent...";
                case BUILDING  -> "&aConstruction en cours";
                case VOTING    -> "&dVote en cours";
                case FINISHED  -> "&cTerminé";
            };
            String theme = game.getCurrentTheme();
            themeLine = theme != null ? "&d" + theme : "&8Non défini";
            int myScore = game.getScores().getOrDefault(uid, 0);
            scoreLine = "&a" + myScore + " &7pt(s)";
            playersLine = "&f" + game.getPlayers().size() + "&7/" + game.getMaxPlayers();
            UUID builder = game.getCurrentBuilder();
            if (builder != null && game.getState() == BBGame.State.BUILDING) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(builder);
                String bName = op.getName() != null ? op.getName() : "?";
                builderLine = builder.equals(uid) ? "&a&lToi !" : "&e" + bName;
            } else {
                builderLine = "&8-";
            }
        } else {
            gameLine    = "&8Lobby";
            stateLine   = "&7En attente d'une partie";
            themeLine   = "&8-";
            scoreLine   = "&80";
            playersLine = "&8-";
            builderLine = "&8-";
        }

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&8──────────────");
        setLine(board, 10, "&7Partie: " + gameLine);
        setLine(board, 9,  "&7Joueurs: " + playersLine);
        setLine(board, 8,  "&7État: " + stateLine);
        setLine(board, 7,  "&8──────────────");
        setLine(board, 6,  "&7Thème: " + themeLine);
        setLine(board, 5,  "&7Constructeur: " + builderLine);
        setLine(board, 4,  "&7Tes points: " + scoreLine);
        setLine(board, 3,  "&8──────────────");
        setLine(board, 2,  "&7Mode: &eBuild Battle");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  "&e    » /vote");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
