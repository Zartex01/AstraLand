package com.astraland.oneblock.scoreboard;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.Boost;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardTask implements Listener {

    private static final String[] E = {
        "§0","§1","§2","§3","§4","§5","§6","§7","§8","§9",
        "§a","§b","§c","§d","§e","§f","§k","§l","§m","§n","§o","§r"
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
        boards.forEach((uuid, b) -> { Player p = Bukkit.getPlayer(uuid); if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); });
        boards.clear();
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> { if (plugin.isInPluginWorld(e.getPlayer())) init(e.getPlayer()); }, 5L);
    }
    @EventHandler public void onQuit(PlayerQuitEvent e) { boards.remove(e.getPlayer().getUniqueId()); }

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

        if (isl != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : "?";
            Phase ph = isl.getCurrentPhase();
            Phase nextPhase = null;
            for (Phase pp : Phase.values()) if (pp.getBlocksRequired() > isl.getBlocksBroken()) { nextPhase = pp; break; }

            Boost activeBoost = plugin.getBoostManager().getBoost(isl.getOwner());
            double totalMult = isl.getPrestigeMultiplier()
                * plugin.getSkillManager().getTotalMoneyMultiplier(uid)
                * plugin.getBoostManager().getMultiplier(isl.getOwner());

            setLine(board, 21, " ");
            setLine(board, 20, "&f" + p.getName());
            setLine(board, 19, "&7─────────────");
            setLine(board, 18, "&7Île : &e" + ownerName);
            setLine(board, 17, "&7Membres : &f" + isl.getAllMemberUUIDs().size());
            setLine(board, 16, "&7─────────────");
            setLine(board, 15, "&7Phase : " + ph.getColor() + "&l" + ph.getDisplayName());
            setLine(board, 14, "&7Blocs : &f" + isl.getBlocksBroken());
            setLine(board, 13, "&7Niveau : &b" + isl.getIslandLevel());
            setLine(board, 12, "&7Valeur : &a" + isl.getIslandWorth() + " ✦");
            setLine(board, 11, isl.getPrestige() > 0 ? "&dPrestige " + isl.getPrestige() : "&8Sans prestige");
            setLine(board, 10, "&7─────────────");
            if (nextPhase != null) {
                long toNext = nextPhase.getBlocksRequired() - isl.getBlocksBroken();
                long total = nextPhase.getBlocksRequired() - ph.getBlocksRequired();
                setLine(board, 9, "&7Prochain : " + buildBar(total - toNext, total) + " &8" + toNext);
            } else {
                setLine(board, 9, "&a✔ Phase max !");
            }
            setLine(board, 8, "&7─────────────");
            setLine(board, 7, "&7Solde : &e" + plugin.getEconomyManager().getBalance(uid) + " &7pièces");
            setLine(board, 6, "&7Mult : &e×" + String.format("%.2f", totalMult));
            if (activeBoost != null) {
                setLine(board, 5, "&c⚡ &e" + activeBoost.getType().getDisplayName() + " &c" + activeBoost.formatRemaining());
            } else {
                setLine(board, 5, "&8Aucun boost");
            }
            setLine(board, 4, "&7─────────────");
            setLine(board, 3, "&7/ob &8| &7/ic &8| &7/sellall");
            setLine(board, 2, " ");
            setLine(board, 1, "&bastraland-fr.com");
            setLine(board, 0, " ");
        } else {
            setLine(board, 21, " ");
            setLine(board, 20, "&f" + p.getName());
            setLine(board, 19, "&7─────────────");
            setLine(board, 18, "&cAucune île OneBlock");
            setLine(board, 17, "&7Utilise &e/ob create");
            for (int i = 16; i >= 4; i--) setLine(board, i, " ");
            setLine(board, 3, "&7/ob &8| &7/ic &8| &7/sellall");
            setLine(board, 2, " ");
            setLine(board, 1, "&bastraland-fr.com");
            setLine(board, 0, " ");
        }
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
