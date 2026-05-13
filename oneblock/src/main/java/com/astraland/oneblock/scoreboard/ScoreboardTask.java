package com.astraland.oneblock.scoreboard;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
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
        "§0","§1","§2","§3","§4","§5","§6","§7","§8","§9",
        "§a","§b","§c","§d","§e","§f","§k","§l","§m","§n"
    };

    private final OneBlock plugin;
    private BukkitTask task;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardTask(OneBlock plugin) { this.plugin = plugin; }

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
        OneBlockIsland isl = plugin.getOneBlockManager().getIsland(uid);

        String ownerLine, phaseLine, blocksLine, membLine, levelLine, balanceLine, nextLine;

        if (isl != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : "?";
            Phase ph = isl.getCurrentPhase();

            Phase nextPhase = null;
            for (Phase pp : Phase.values()) if (pp.getBlocksRequired() > isl.getBlocksBroken()) { nextPhase = pp; break; }

            ownerLine  = "&e" + ownerName + (isl.isOwner(uid) ? " &7(toi)" : "");
            phaseLine  = ph.getColor() + "&l" + ph.getDisplayName();
            blocksLine = "&f" + isl.getBlocksBroken();
            membLine   = "&f" + (isl.getMembers().size() + 1) + " &7membre(s)";
            levelLine  = "&b" + isl.getIslandLevel();
            balanceLine = "&e" + plugin.getEconomyManager().getBalance(uid) + " &7pièces";

            if (nextPhase != null) {
                long toNext = nextPhase.getBlocksRequired() - isl.getBlocksBroken();
                long total = nextPhase.getBlocksRequired() - ph.getBlocksRequired();
                long done = total - toNext;
                nextLine = "&7" + buildBar(done, total) + " &8" + toNext + " blocs";
            } else {
                nextLine = "&a✔ Phase max !";
            }
        } else {
            ownerLine  = "&8Aucune île";
            phaseLine  = "&8-";
            blocksLine = "&80";
            membLine   = "&8-";
            levelLine  = "&80";
            balanceLine = "&80 &7pièces";
            nextLine   = "&8-";
        }

        setLine(board, 19, " ");
        setLine(board, 18, "&f" + p.getName());
        setLine(board, 17, "&7──────────");
        setLine(board, 16, "&7Île : " + ownerLine);
        setLine(board, 15, "&7Membres : " + membLine);
        setLine(board, 14, "&7──────────");
        setLine(board, 13, "&7Phase : " + phaseLine);
        setLine(board, 12, "&7Blocs : " + blocksLine);
        setLine(board, 11, "&7Niveau : " + levelLine);
        setLine(board, 10, "&7──────────");
        setLine(board, 9,  "&7Prochaine phase :");
        setLine(board, 8,  nextLine);
        setLine(board, 7,  "&7──────────");
        setLine(board, 6,  "&7Solde : " + balanceLine);
        setLine(board, 5,  "&7──────────");
        setLine(board, 4,  "&7Mode : &aOneBlock");
        setLine(board, 3,  "&7» &e/ob");
        setLine(board, 2,  " ");
        setLine(board, 1,  "&bastraland-fr.com");
        setLine(board, 0,  " ");
    }

    private String buildBar(long done, long total) {
        int bars = 8;
        int filled = total > 0 ? (int) Math.min(bars, (done * bars) / total) : bars;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars; i++) sb.append(i < filled ? "&a|" : "&7|");
        return sb.toString();
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
