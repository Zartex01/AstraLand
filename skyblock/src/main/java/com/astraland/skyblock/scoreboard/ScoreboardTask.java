package com.astraland.skyblock.scoreboard;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.ranks.IslandRank;
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

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ScoreboardTask implements Listener {

    private static final String[] E = {
        "§0","§1","§2","§3","§4","§5","§6","§7","§8","§9","§a","§b","§c","§d","§e","§f","§r"
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
            Score score = obj.getScore(E[i]);
            score.setScore(i);
            applyBlankFormat(score);
        }
        boards.put(p.getUniqueId(), board);
        p.setScoreboard(board);
    }

    private void setLine(Scoreboard b, int slot, String text) {
        if (slot < 0 || slot >= E.length) return;
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
        Island isl = plugin.getIslandManager().getIsland(uid);
        int balance = plugin.getEconomyManager().getBalance(uid);

        // ── Infos île ──
        String islName, levelLine, genLine, warpLine, rankLine;
        int questDone = 0, questTotal = 0;

        if (isl != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
            IslandRank rank = IslandRank.fromLevel(isl.getLevel());
            islName   = "&f" + isl.getName();
            levelLine = "&a" + isl.getLevel() + " &8| &7" + fmt(isl.getValue()) + " pts";
            genLine   = "&b" + isl.getGeneratorLevel() + " &8/ &b7";
            warpLine  = isl.isWarpEnabled() ? "&aOuvert" : "&7Fermé";
            rankLine  = rank.getColor() + rank.getDisplayName()
                + (rank.getSellBonus() > 0 ? " &8[&6+" + rank.getSellBonus() + "%&8]" : "");
            questDone  = plugin.getQuestManager().countCompleted(uid);
            questTotal = 5;
        } else {
            islName   = "&8Aucune île";
            levelLine = "&80";
            genLine   = "&80";
            warpLine  = "&8-";
            rankLine  = "&7Bois";
        }

        // Slots 0..16 (17 lignes max)
        setLine(board, 16, " ");
        setLine(board, 15, "&f" + p.getName());
        setLine(board, 14, "&7──────");
        setLine(board, 13, "&7Île : " + islName);
        setLine(board, 12, "&7Rang : " + rankLine);
        setLine(board, 11, "&7Niveau : " + levelLine);
        setLine(board, 10, "&7Gén : " + genLine);
        setLine(board, 9,  "&7Warp : " + warpLine);
        setLine(board, 8,  "&7──────");
        setLine(board, 7,  "&7Argent : &6" + fmt(balance) + " $");
        setLine(board, 6,  "&7──────");
        setLine(board, 5,  "&7Quêtes : &e" + questDone + " &8/ &e" + questTotal);
        setLine(board, 4,  "&7──────");
        setLine(board, 3,  isl == null ? "&e/is create" : "&a/is quetes");
        setLine(board, 2,  "&bastraland.fr");
        setLine(board, 1,  " ");
        setLine(board, 0,  " ");
    }

    private void applyBlankFormat(Score score) {
        try {
            Class<?> nfClass = Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat");
            Method blankMethod = nfClass.getMethod("blank");
            Object blank = blankMethod.invoke(null);
            Method apply = score.getClass().getMethod("numberFormat", nfClass);
            apply.invoke(score, blank);
        } catch (Exception ignored) {}
    }

    private String fmt(long v) { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String fmt(int v)  { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
