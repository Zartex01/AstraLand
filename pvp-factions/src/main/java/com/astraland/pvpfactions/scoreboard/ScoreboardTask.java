package com.astraland.pvpfactions.scoreboard;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.models.Faction;
import com.astraland.pvpfactions.models.FactionRole;
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

    private final PvpFactions plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(PvpFactions plugin) { this.plugin = plugin; }

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
        Objective obj = board.registerNewObjective("astraland", "dummy",
            c("&6&l✦ AstraLand ✦"));
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

        int kills  = plugin.getStatsManager().getKills(uid);
        int deaths = plugin.getStatsManager().getDeaths(uid);
        double kd  = plugin.getStatsManager().getKD(uid);
        int streak = plugin.getStatsManager().getCurrentStreak(uid);
        int bounty = plugin.getBountyManager().getTotalBounty(uid);

        Faction f = plugin.getFactionManager().getPlayerFaction(uid);
        String fLine    = f != null ? "&e[&6" + f.getTag() + "&e] &f" + f.getName() : "&8Aucune";
        String roleLine = f != null ? "&e" + f.getMembers().getOrDefault(uid, FactionRole.MEMBER).getDisplay() : "&8-";
        String maxM     = String.valueOf(plugin.getConfig().getInt("faction.max-members", 30));
        String membLine = f != null ? "&f" + f.getMembers().size() + "&7/" + maxM : "&8-";
        String powLine  = f != null ? "&f" + (int) f.getPower() : "&8-";

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&7─────────");
        setLine(board, 10, "&7Faction: " + fLine);
        setLine(board, 9,  "&7Rôle: " + roleLine);
        setLine(board, 8,  "&7Membres: " + membLine);
        setLine(board, 7,  "&7Puiss: " + powLine);
        setLine(board, 6,  "&7─────────");
        setLine(board, 5,  "&7Kills: &a" + kills + "  &7Morts: &c" + deaths);
        setLine(board, 4,  "&7K/D: &e" + String.format("%.2f", kd));
        setLine(board, 3,  "&7Série: &6" + streak + " kill" + (streak > 1 ? "s" : ""));
        setLine(board, 2,  bounty > 0 ? "&7Prime: &c" + bounty + "$" : "&7Prime: &8aucune");
        setLine(board, 1,  "&bastraland.fr");
        setLine(board, 0,  " ");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
