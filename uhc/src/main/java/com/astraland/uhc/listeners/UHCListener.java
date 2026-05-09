package com.astraland.uhc.listeners;

import com.astraland.uhc.UHC;
import com.astraland.uhc.managers.UHCManager;
import com.astraland.uhc.models.UHCGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public class UHCListener implements Listener {

    private final UHC plugin;

    public UHCListener(UHC plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("uhc.golden-head", true)) {
            registerGoldenHeadRecipe();
        }
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.isInPluginWorld(player)) return;
        UHCManager um = plugin.getUhcManager();
        UHCGame game = um.getGame();
        if (!game.isInGame(player.getUniqueId())) return;
        if (game.getState() != UHCGame.State.INGAME) return;

        if (!plugin.getConfig().getBoolean("uhc.natural-regen", false)
                && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!plugin.isInPluginWorld(victim)) return;
        UHCManager um = plugin.getUhcManager();
        UHCGame game = um.getGame();
        if (!game.isInGame(victim.getUniqueId())) return;

        event.setDeathMessage(null);

        UUID killerId = null;
        if (victim.getKiller() != null) killerId = victim.getKiller().getUniqueId();

        um.killPlayer(victim.getUniqueId(), killerId);

        if (victim.getKiller() != null && plugin.getConfig().getBoolean("uhc.golden-head", true)) {
            victim.getKiller().sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&aUne tête en or a été déposée à l'emplacement de &e" + victim.getName() + "&a."));
        }

        victim.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTu es éliminé du UHC !"));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                victim.setGameMode(org.bukkit.GameMode.SPECTATOR);
                victim.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Tu es maintenant spectateur."));
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isInPluginWorld(event.getPlayer())) return;
        UHCManager um = plugin.getUhcManager();
        UHCGame game = um.getGame();
        if (game.isInGame(event.getPlayer().getUniqueId())) {
            um.killPlayer(event.getPlayer().getUniqueId(), null);
        }
    }

    private void registerGoldenHeadRecipe() {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "golden_head");
            ItemStack goldenHead = new ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.ItemMeta meta = goldenHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "Tête en or");
                goldenHead.setItemMeta(meta);
            }
            ShapedRecipe recipe = new ShapedRecipe(key, goldenHead);
            recipe.shape("GGG", "GHG", "GGG");
            recipe.setIngredient('G', Material.GOLD_INGOT);
            recipe.setIngredient('H', Material.PLAYER_HEAD);
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Recette tête en or enregistrée.");
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible d'enregistrer la recette tête en or : " + e.getMessage());
        }
    }
}
