package com.astraland.duels.managers;

import com.astraland.duels.Duels;
import com.astraland.duels.models.DuelMatch;
import com.astraland.duels.models.DuelRequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DuelManager {

    private final Duels plugin;
    private final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, DuelMatch> activeMatches = new HashMap<>();
    private final Map<UUID, String> selectedKits = new HashMap<>();

    public DuelManager(Duels plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player challenger, Player challenged, String kit) {
        long timeout = plugin.getConfig().getLong("duels.request-timeout", 30) * 1000L;
        DuelRequest existing = pendingRequests.get(challenged.getUniqueId());
        if (existing != null && !existing.isExpired(timeout)) {
            challenger.sendMessage(color("&cCe joueur a déjà une demande en attente."));
            return;
        }
        DuelRequest req = new DuelRequest(challenger.getUniqueId(), challenged.getUniqueId(), kit);
        pendingRequests.put(challenged.getUniqueId(), req);

        challenger.sendMessage(color(plugin.getConfig().getString("messages.request-sent", "&aDemande envoyée à &e%player%&a.")
            .replace("%player%", challenged.getName())));
        challenged.sendMessage(color(plugin.getConfig().getString("messages.request-received",
            "&e%player% &ate défie en duel ! Kit: &e%kit%\n&e/duel accept &aou &e/duel deny")
            .replace("%player%", challenger.getName()).replace("%kit%", kit)));

        long timeoutTicks = plugin.getConfig().getLong("duels.request-timeout", 30) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            DuelRequest r = pendingRequests.get(challenged.getUniqueId());
            if (r != null && r.getChallenger().equals(challenger.getUniqueId())) {
                pendingRequests.remove(challenged.getUniqueId());
                if (challenger.isOnline())
                    challenger.sendMessage(color(plugin.getConfig().getString("messages.request-expired", "&cDemande expirée.")));
            }
        }, timeoutTicks);
    }

    public boolean acceptRequest(Player challenged) {
        DuelRequest req = pendingRequests.remove(challenged.getUniqueId());
        if (req == null) return false;
        long timeout = plugin.getConfig().getLong("duels.request-timeout", 30) * 1000L;
        if (req.isExpired(timeout)) return false;

        Player challenger = Bukkit.getPlayer(req.getChallenger());
        if (challenger == null || !challenger.isOnline()) {
            challenged.sendMessage(color("&cLe joueur n'est plus en ligne."));
            return false;
        }

        startDuel(challenger, challenged, req.getKit());
        return true;
    }

    public void denyRequest(Player challenged) {
        DuelRequest req = pendingRequests.remove(challenged.getUniqueId());
        if (req == null) { challenged.sendMessage(color("&cAucune demande de duel en attente.")); return; }
        Player challenger = Bukkit.getPlayer(req.getChallenger());
        if (challenger != null) challenger.sendMessage(color("&c" + challenged.getName() + " &aa refusé le duel."));
        challenged.sendMessage(color("&aDemande refusée."));
    }

    private void startDuel(Player p1, Player p2, String kit) {
        String worldName = plugin.getConfig().getString("duels.world", "world_duels");
        World world = Bukkit.getWorld(worldName);

        Location arena1, arena2;
        if (world != null) {
            int offset = activeMatches.size() * 60;
            arena1 = new Location(world, offset, 65, -15);
            arena2 = new Location(world, offset, 65, 15);
        } else {
            arena1 = p1.getLocation();
            arena2 = p2.getLocation();
        }

        DuelMatch match = new DuelMatch(p1.getUniqueId(), p2.getUniqueId(), kit, arena1, arena2);
        activeMatches.put(p1.getUniqueId(), match);
        activeMatches.put(p2.getUniqueId(), match);

        p1.teleport(arena1);
        p2.teleport(arena2);

        giveKit(p1, kit);
        giveKit(p2, kit);

        int countdown = plugin.getConfig().getInt("duels.countdown", 5);
        int[] timer = {countdown};
        Bukkit.getScheduler().runTaskTimer(plugin, t -> {
            if (timer[0] <= 0) {
                t.cancel();
                p1.sendMessage(color(plugin.getConfig().getString("messages.duel-start", "&aDuel contre &e%player%&a !")
                    .replace("%player%", p2.getName())));
                p2.sendMessage(color(plugin.getConfig().getString("messages.duel-start", "&aDuel contre &e%player%&a !")
                    .replace("%player%", p1.getName())));
                return;
            }
            p1.sendMessage(color("&eDuel dans &6" + timer[0] + "s"));
            p2.sendMessage(color("&eDuel dans &6" + timer[0] + "s"));
            timer[0]--;
        }, 0L, 20L);
    }

    public void onDeath(Player loser) {
        DuelMatch match = activeMatches.remove(loser.getUniqueId());
        if (match == null) return;
        UUID winnerId = match.getOpponent(loser.getUniqueId());
        activeMatches.remove(winnerId);

        Player winner = winnerId != null ? Bukkit.getPlayer(winnerId) : null;
        if (winner != null) {
            winner.sendMessage(color(plugin.getConfig().getString("messages.duel-won", "&6Tu as gagné le duel contre &e%player%&6 !")
                .replace("%player%", loser.getName())));
            winner.getInventory().clear();
            winner.setHealth(winner.getMaxHealth());
        }
        loser.sendMessage(color(plugin.getConfig().getString("messages.duel-lost", "&cTu as perdu le duel contre &e%player%&c.")
            .replace("%player%", winner != null ? winner.getName() : "?")));
    }

    public boolean isInDuel(UUID uuid) { return activeMatches.containsKey(uuid); }
    public DuelMatch getMatch(UUID uuid) { return activeMatches.get(uuid); }
    public String getSelectedKit(UUID uuid) { return selectedKits.getOrDefault(uuid, "NODEBUFF"); }
    public void setKit(UUID uuid, String kit) { selectedKits.put(uuid, kit); }

    private void giveKit(Player player, String kitName) {
        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        switch (kitName.toUpperCase()) {
            case "SOUP" -> {
                player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));
                for (int i = 1; i <= 8; i++) player.getInventory().setItem(i, new ItemStack(Material.MUSHROOM_STEW));
            }
            case "ARCHER" -> {
                player.getInventory().setItem(0, new ItemStack(Material.BOW));
                player.getInventory().setItem(1, new ItemStack(Material.ARROW, 64));
                player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
            }
            case "UHC" -> {
                player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
                player.getInventory().setItem(1, new ItemStack(Material.GOLDEN_APPLE, 8));
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            }
            default -> {
                player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            }
        }
    }

    public void endAll() {
        activeMatches.keySet().forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) { p.getInventory().clear(); p.sendMessage(color("&cLe serveur s'arrête, duel terminé.")); }
        });
        activeMatches.clear();
        pendingRequests.clear();
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
