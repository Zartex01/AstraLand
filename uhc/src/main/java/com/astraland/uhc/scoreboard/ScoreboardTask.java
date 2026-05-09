package com.astraland.uhc.scoreboard;

import com.astraland.uhc.UHC;
import com.astraland.uhc.models.UHCGame;
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

    private final UHC plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(UHC plugin) { this.plugin = plugin; }

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
        UHCGame game = plugin.getUhcManager().getGame();

        UHCGame.State state = game.getState();
        String stateLine = switch (state) {
            case WAITING    -> "&7En attente &8(" + game.getPlayers().size() + ")";
            case STARTING   -> "&6Départ imminent...";
            case INGAME     -> "&aEn jeu";
            case DEATHMATCH -> "&cDEATHMATCH !";
            case FINISHED   -> "&cTerminé";
        };

        boolean inGame   = game.isInGame(uid);
        boolean alive    = game.isAlive(uid);
        int myKills      = game.getKills(uid);
        int aliveCount   = game.getAlive().size();
        int hearts       = (int) Math.ceil(p.getHealth());
        String scenario  = game.getScenario();

        String statusLine  = inGame ? (alive ? "&aVivant" : "&cÉliminé") : "&7Spectateur";
        String killsLine   = "&a" + myKills + " &7kill(s)";
        String aliveLine   = "&f" + aliveCount + " &7restant(s)";
        String heartLine   = "&c" + hearts + " &7❤";
        String graceLine   = game.isGracePeriod() && state == UHCGame.State.INGAME ? "&aPériode de grâce" : " ";

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&7─────────");
        setLine(board, 10, "&7Phase: " + stateLine);
        setLine(board, 9,  "&7Scénario: &e" + scenario);
        setLine(board, 8,  "&7─────────");
        setLine(board, 7,  "&7Statut: " + statusLine);
        setLine(board, 6,  "&7Cœurs: " + heartLine);
        setLine(board, 5,  "&7Kills: " + killsLine);
        setLine(board, 4,  aliveLine);
        setLine(board, 3,  graceLine);
        setLine(board, 2,  "&7─────────");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  " ");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
