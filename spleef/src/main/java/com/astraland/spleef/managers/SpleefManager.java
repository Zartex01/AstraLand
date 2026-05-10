package com.astraland.spleef.managers;

import com.astraland.spleef.Spleef;
import com.astraland.spleef.models.SpleefGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SpleefManager {

    private final Spleef plugin;
    private final Map<String, SpleefGame> games = new LinkedHashMap<>();
    private final Map<String, BukkitTask> countdownTasks = new HashMap<>();

    public SpleefManager(Spleef plugin) {
        this.plugin = plugin;
    }

    public SpleefGame createGame(String name) {
        int min = plugin.getConfig().getInt("spleef.min-players", 2);
        SpleefGame game = new SpleefGame(name, min, 16);
        games.put(name.toLowerCase(), game);
        return game;
    }

    public SpleefGame getGame(String name) { return games.get(name.toLowerCase()); }

    public SpleefGame getPlayerGame(UUID uuid) {
        for (SpleefGame g : games.values()) if (g.isInGame(uuid)) return g;
        return null;
    }

    public Collection<SpleefGame> getGames() { return games.values(); }

    public boolean joinGame(SpleefGame game, Player player) {
        if (game.getState() != SpleefGame.State.WAITING) return false;
        if (!game.addPlayer(player.getUniqueId())) return false;
        if (game.getSpawn() != null) player.teleport(game.getSpawn());
        giveShovel(player);
        if (game.getPlayers().size() >= game.getMinPlayers()) startCountdown(game);
        return true;
    }

    public void leaveGame(SpleefGame game, Player player) {
        game.removePlayer(player.getUniqueId());
        player.getInventory().clear();
        if (game.getState() == SpleefGame.State.COUNTDOWN && game.getPlayers().size() < game.getMinPlayers()) {
            cancelCountdown(game);
        }
        if (game.getState() == SpleefGame.State.INGAME) checkWin(game);
    }

    private void giveShovel(Player player) {
        player.getInventory().clear();
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        player.getInventory().setItem(0, shovel);
    }

    private void startCountdown(SpleefGame game) {
        game.setState(SpleefGame.State.COUNTDOWN);
        int[] timer = {plugin.getConfig().getInt("spleef.countdown", 20)};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timer[0] <= 0) { startGame(game); return; }
            if (timer[0] <= 5 || timer[0] % 10 == 0)
                broadcast(game, "&aLa partie commence dans &e" + timer[0] + "s&a !");
            timer[0]--;
        }, 0L, 20L);
        countdownTasks.put(game.getName(), task);
    }

    private void cancelCountdown(SpleefGame game) {
        BukkitTask t = countdownTasks.remove(game.getName());
        if (t != null) t.cancel();
        game.setState(SpleefGame.State.WAITING);
        broadcast(game, "&cPas assez de joueurs !");
    }

    private void startGame(SpleefGame game) {
        BukkitTask t = countdownTasks.remove(game.getName());
        if (t != null) t.cancel();
        game.setState(SpleefGame.State.INGAME);
        broadcast(game, plugin.getConfig().getString("messages.game-start", "&aLa partie commence !"));
    }

    public void eliminatePlayer(SpleefGame game, Player player) {
        game.eliminate(player.getUniqueId());
        player.getInventory().clear();
        if (game.getLobby() != null) player.teleport(game.getLobby());
        broadcast(game, plugin.getConfig().getString("messages.player-eliminated", "&c%player% est éliminé !")
            .replace("%player%", player.getName()));
        checkWin(game);
    }

    private void checkWin(SpleefGame game) {
        UUID winner = game.getWinner();
        if (winner != null) {
            Player p = Bukkit.getPlayer(winner);
            String name = p != null ? p.getName() : winner.toString();
            game.setState(SpleefGame.State.FINISHED);
            broadcast(game, plugin.getConfig().getString("messages.winner", "&6%player% gagne !")
                .replace("%player%", name));
            int winReward = plugin.getConfig().getInt("economy.win-reward", 100);
            plugin.getEconomyManager().addBalance(winner, winReward);
            if (p != null) p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a+" + winReward + " pièces &7pour la victoire !"));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                game.getPlayers().clear();
                game.getEliminated().clear();
                game.setState(SpleefGame.State.WAITING);
            }, 100L);
        }
    }

    private void broadcast(SpleefGame game, String msg) {
        String colored = ChatColor.translateAlternateColorCodes('&', msg);
        Set<UUID> all = new HashSet<>(game.getPlayers());
        all.addAll(game.getEliminated());
        all.forEach(uuid -> { if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(colored); });
    }

    public void stopAll() {
        countdownTasks.values().forEach(BukkitTask::cancel);
        countdownTasks.clear();
    }
}
