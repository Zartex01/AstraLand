package com.astraland.startup.combat;

import com.astraland.startup.AstraLandStartup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Set;

public class CombatListener implements Listener {

    private static final Set<String> ALLOWED_CMDS = Set.of(
        "tell", "msg", "whisper", "w", "r", "reply", "me", "helpop",
        "help", "list", "plugins", "version", "ver"
    );

    private final AstraLandStartup plugin;
    private final CombatManager combat;

    public CombatListener(AstraLandStartup plugin) {
        this.plugin = plugin;
        this.combat = plugin.getCombatManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (event.getDamager() instanceof Player p) attacker = p;
        else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) attacker = p;
        if (attacker == null || attacker.equals(victim)) return;

        boolean freshV = !combat.isInCombat(victim.getUniqueId());
        boolean freshA = !combat.isInCombat(attacker.getUniqueId());

        combat.tag(victim.getUniqueId());
        combat.tag(attacker.getUniqueId());

        if (freshV) victim.sendMessage(c("&c⚔ &lCOMBAT TAG&c ! Commandes bloquées. Fuis pas..."));
        if (freshA) attacker.sendMessage(c("&c⚔ &lCOMBAT TAG&c ! Commandes bloquées. Fuis pas..."));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.combat.bypass")) return;
        if (!combat.isInCombat(player.getUniqueId())) return;
        String cmd = event.getMessage().substring(1).split(" ")[0].toLowerCase();
        if (ALLOWED_CMDS.contains(cmd)) return;
        event.setCancelled(true);
        player.sendMessage(c("&c⚔ &lCOMBAT TAG &c— Commandes bloquées ! &7(&e"
            + combat.remainingSeconds(player.getUniqueId()) + "s&7 restantes)"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!combat.isInCombat(player.getUniqueId())) return;
        combat.flagQuit(player.getUniqueId());
        combat.clearTag(player.getUniqueId());
        Bukkit.broadcastMessage(c("&c⚔ &e" + player.getName()
            + " &ca &lfui le combat &c! Il sera éliminé à sa reconnexion."));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!combat.hasQuitFlag(player.getUniqueId())) return;
        combat.clearQuitFlag(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.setHealth(0);
            Bukkit.broadcastMessage(c("&c⚔ &e" + player.getName()
                + " &ca été &léliminé &cpour avoir fui le combat !"));
        }, 20L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        combat.clearTag(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        combat.clearTag(event.getPlayer().getUniqueId());
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
