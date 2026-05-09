package com.astraland.uhc.managers;

import com.astraland.uhc.UHC;
import com.astraland.uhc.models.UHCGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class UHCManager {

    private final UHC plugin;
    private UHCGame game;
    private BukkitTask gracePeriodTask;
    private BukkitTask borderTask;

    public UHCManager(UHC plugin) {
        this.plugin = plugin;
        this.game = new UHCGame();
    }

    public UHCGame getGame() { return game; }

    public boolean joinGame(Player player) {
        if (game.getState() != UHCGame.State.WAITING) return false;
        game.addPlayer(player.getUniqueId());
        player.sendMessage(color("&aRejoint la file UHC. Joueurs: &e" + game.getPlayers().size()));
        return true;
    }

    public void leaveGame(Player player) {
        game.removePlayer(player.getUniqueId());
        player.sendMessage(color("&cQuitté la file UHC."));
    }

    public void startGame() {
        if (game.getPlayers().size() < 2) {
            Bukkit.broadcastMessage(color("&cPas assez de joueurs pour lancer un UHC !"));
            return;
        }
        game.setState(UHCGame.State.INGAME);
        game.setStartTime(System.currentTimeMillis());
        game.setGracePeriod(true);

        String worldName = plugin.getConfig().getString("uhc.world", "world_uhc");
        World world = Bukkit.getWorld(worldName);
        int radius = plugin.getConfig().getInt("uhc.scatter-radius", 800);

        scatterPlayers(world, radius);
        setWorldBorder(world, plugin.getConfig().getDouble("uhc.border-start", 2000));
        applyUHCEffects();

        broadcastAll("&4La partie UHC commence !");
        if (game.isGracePeriod()) {
            int grace = plugin.getConfig().getInt("uhc.grace-period", 600);
            gracePeriodTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                game.setGracePeriod(false);
                broadcastAll(plugin.getConfig().getString("messages.grace-end", "&cLa période de grâce est terminée !"));
                scheduleBorderShrink(world);
            }, grace * 20L);
        }
    }

    private void scatterPlayers(World world, int radius) {
        if (world == null) return;
        Random rand = new Random();
        List<UUID> playerList = new ArrayList<>(game.getPlayers());
        for (UUID uuid : playerList) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            double angle = rand.nextDouble() * 2 * Math.PI;
            double dist = 50 + rand.nextDouble() * (radius - 50);
            int x = (int) (Math.cos(angle) * dist);
            int z = (int) (Math.sin(angle) * dist);
            int y = world.getHighestBlockYAt(x, z) + 1;
            p.teleport(new org.bukkit.Location(world, x, y, z));
        }
    }

    private void setWorldBorder(World world, double size) {
        if (world == null) return;
        WorldBorder border = world.getWorldBorder();
        border.setSize(size);
        border.setCenter(0, 0);
    }

    private void scheduleBorderShrink(World world) {
        if (world == null) return;
        int shrinkAfter = plugin.getConfig().getInt("uhc.border-shrink-after", 900);
        double finalSize = plugin.getConfig().getDouble("uhc.border-final", 50);
        broadcastAll(plugin.getConfig().getString("messages.border-shrink", "&cLa bordure rétrécit !"));
        WorldBorder border = world.getWorldBorder();
        border.setSize(finalSize, shrinkAfter);
    }

    private void applyUHCEffects() {
        for (UUID uuid : game.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.setHealthScaled(false);
            if (!plugin.getConfig().getBoolean("uhc.natural-regen", false)) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.REGENERATION, 0, 0, false, false));
            }
        }
    }

    public void killPlayer(UUID victim, UUID killer) {
        game.killPlayer(victim, killer);
        Player vp = Bukkit.getPlayer(victim);
        Player kp = killer != null ? Bukkit.getPlayer(killer) : null;
        if (vp != null && kp != null)
            broadcastAll("&c" + vp.getName() + " &7a été tué par &e" + kp.getName() + " &7[" + game.getKills(killer) + " kills]");
        else if (vp != null)
            broadcastAll("&c" + vp.getName() + " &7est mort.");

        checkWin();
    }

    private void checkWin() {
        UUID winner = game.getWinner();
        if (winner != null) {
            Player p = Bukkit.getPlayer(winner);
            String name = p != null ? p.getName() : winner.toString();
            broadcastAll(plugin.getConfig().getString("messages.winner", "&6%player% gagne le UHC !").replace("%player%", name));
            game.setState(UHCGame.State.FINISHED);
        }
    }

    public void stopGame() {
        if (gracePeriodTask != null) gracePeriodTask.cancel();
        if (borderTask != null) borderTask.cancel();
        if (game.getState() == UHCGame.State.INGAME) broadcastAll("&cLa partie UHC a été arrêtée.");
        game = new UHCGame();
    }

    private void broadcastAll(String msg) {
        String c = color(msg);
        game.getPlayers().forEach(uuid -> { if (Bukkit.getPlayer(uuid) != null) Bukkit.getPlayer(uuid).sendMessage(c); });
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
