package com.astraland.skyblock.scoreboard;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
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

    private final Skyblock plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(Skyblock plugin) { this.plugin = plugin; }

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
        Island isl = plugin.getIslandManager().getIsland(uid);

        String ownerLine, membLine, levelLine, blocksLine;
        if (isl != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : "?";
            boolean isOwner = isl.isOwner(uid);
            ownerLine = "&e" + ownerName + (isOwner ? " &7(toi)" : "");
            membLine  = "&f" + (isl.getMembers().size() + 1) + " &7membre(s)";
            levelLine = "&a" + isl.getLevel();
            blocksLine = "&f" + isl.getBlocksBroken() + " &7blocs";
        } else {
            ownerLine  = "&8Aucune île";
            membLine   = "&8-";
            levelLine  = "&80";
            blocksLine = "&8-";
        }

        setLine(board, 13, " ");
        setLine(board, 12, "&f" + p.getName());
        setLine(board, 11, "&7─────────");
        setLine(board, 10, "&7Île de: " + ownerLine);
        setLine(board, 9,  "&7Membres: " + membLine);
        setLine(board, 8,  "&7Niveau: " + levelLine);
        setLine(board, 7,  "&7Blocs: " + blocksLine);
        setLine(board, 6,  "&7─────────");
        setLine(board, 5,  "&7Mode: &aSkyBlock");
        setLine(board, 4,  "&7» &e/is create");
        setLine(board, 3,  "&7» &e/is home");
        setLine(board, 2,  " ");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  " ");
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
