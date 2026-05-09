package com.astraland.buildbattle.managers;

import com.astraland.buildbattle.BuildBattle;
import com.astraland.buildbattle.models.BBGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BuildBattleManager {

    private final BuildBattle plugin;
    private final Map<String, BBGame> games = new LinkedHashMap<>();
    private final Map<String, BukkitTask> tasks = new HashMap<>();

    public BuildBattleManager(BuildBattle plugin) {
        this.plugin = plugin;
    }

    public BBGame createGame(String name) {
        int min = plugin.getConfig().getInt("buildbattle.min-players", 2);
        BBGame game = new BBGame(name, min, 12);
        games.put(name.toLowerCase(), game);
        return game;
    }

    public BBGame getGame(String name) { return games.get(name.toLowerCase()); }
    public BBGame getPlayerGame(UUID uuid) { return games.values().stream().filter(g -> g.isInGame(uuid)).findFirst().orElse(null); }
    public Collection<BBGame> getGames() { return games.values(); }

    public boolean joinGame(BBGame game, Player player) {
        if (game.getState() != BBGame.State.WAITING && game.getState() != BBGame.State.COUNTDOWN) return false;
        if (!game.addPlayer(player.getUniqueId())) return false;
        if (game.getLobby() != null) player.teleport(game.getLobby());
        if (game.getPlayers().size() >= game.getMinPlayers() && game.getState() == BBGame.State.WAITING)
            startCountdown(game);
        return true;
    }

    public void leaveGame(BBGame game, Player player) {
        game.removePlayer(player.getUniqueId());
        if (game.getState() == BBGame.State.COUNTDOWN && game.getPlayers().size() < game.getMinPlayers()) {
            BukkitTask t = tasks.remove(game.getName()); if (t != null) t.cancel();
            game.setState(BBGame.State.WAITING);
            broadcast(game, "&cPas assez de joueurs !");
        }
    }

    private void startCountdown(BBGame game) {
        game.setState(BBGame.State.COUNTDOWN);
        int[] timer = {30};
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timer[0] <= 0) { startBuild(game); return; }
            if (timer[0] <= 5 || timer[0] % 10 == 0)
                broadcast(game, "&aLa partie commence dans &e" + timer[0] + "s&a !");
            timer[0]--;
        }, 0L, 20L);
        tasks.put(game.getName(), t);
    }

    private void startBuild(BBGame game) {
        BukkitTask t = tasks.remove(game.getName()); if (t != null) t.cancel();
        game.setState(BBGame.State.BUILDING);
        game.startBuildOrder();

        List<String> themes = plugin.getConfig().getStringList("themes");
        String theme = themes.isEmpty() ? "Maison" : themes.get(new Random().nextInt(themes.size()));
        game.setCurrentTheme(theme);

        broadcast(game, plugin.getConfig().getString("messages.theme-announced", "&6Thème : &e&l%theme%").replace("%theme%", theme));

        int buildTime = plugin.getConfig().getInt("buildbattle.build-time", 180);
        broadcast(game, plugin.getConfig().getString("messages.build-start", "&aC'est parti ! &e%time%s pour construire !").replace("%time%", String.valueOf(buildTime)));

        int[] buildTimer = {buildTime};
        BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (buildTimer[0] <= 0) { startVote(game); return; }
            if (buildTimer[0] <= 10 || buildTimer[0] % 30 == 0)
                broadcast(game, "&e" + buildTimer[0] + "s &arestantes pour construire !");
            buildTimer[0]--;
        }, 20L, 20L);
        tasks.put(game.getName(), bt);
    }

    private void startVote(BBGame game) {
        BukkitTask t = tasks.remove(game.getName()); if (t != null) t.cancel();
        game.setState(BBGame.State.VOTING);
        broadcast(game, plugin.getConfig().getString("messages.vote-start", "&aVotez de 1 à 5 avec /bb vote <1-5>"));

        int voteTime = plugin.getConfig().getInt("buildbattle.vote-time", 60);
        int[] voteTimer = {voteTime};
        BukkitTask vt = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (voteTimer[0] <= 0) { endGame(game); return; }
            voteTimer[0]--;
        }, 20L, 20L);
        tasks.put(game.getName(), vt);
    }

    public void vote(BBGame game, UUID voter, int score) {
        if (game.getState() != BBGame.State.VOTING) return;
        if (score < 1 || score > 5) return;
        game.addVote(voter, score);
    }

    private void endGame(BBGame game) {
        BukkitTask t = tasks.remove(game.getName()); if (t != null) t.cancel();
        game.tallyVotes();
        game.setState(BBGame.State.FINISHED);
        UUID top = game.getTopScorer();
        if (top != null) {
            Player p = Bukkit.getPlayer(top);
            String name = p != null ? p.getName() : top.toString();
            int score = game.getScores().getOrDefault(top, 0);
            broadcast(game, plugin.getConfig().getString("messages.winner", "&6%player% gagne avec &e%score%/5 !")
                .replace("%player%", name).replace("%score%", String.valueOf(score)));
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> { game.getPlayers().clear(); game.setState(BBGame.State.WAITING); }, 100L);
    }

    private void broadcast(BBGame game, String msg) {
        String colored = ChatColor.translateAlternateColorCodes('&', msg);
        game.getPlayers().forEach(uuid -> { if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(colored); });
    }

    public void stopAll() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
    }
}
