package com.astraland.skyblock.listeners;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.gui.IslandGeneratorGUI;
import com.astraland.skyblock.gui.IslandSettingsGUI;
import com.astraland.skyblock.gui.IslandWarpGUI;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Random;

public class IslandListener implements Listener {

    private final Skyblock plugin;
    private final Random random = new Random();

    // Blocs par niveau de générateur (avec poids)
    private static final Material[][] GEN_BLOCKS = {
        // Level 0: cobblestone only
        {Material.COBBLESTONE},
        // Level 1: stone 30 / cobble 70
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE},
        // Level 2: cobble 50 / stone 30 / variants 20
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE, Material.ANDESITE, Material.GRANITE},
        // Level 3: charbon 10%
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE, Material.ANDESITE, Material.COAL_ORE, Material.GRANITE},
        // Level 4: fer 15%
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE, Material.IRON_ORE, Material.IRON_ORE, Material.COAL_ORE, Material.ANDESITE},
        // Level 5: or 10%
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.IRON_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.ANDESITE},
        // Level 6: diamant 5%
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.IRON_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.DIAMOND_ORE, Material.ANDESITE},
        // Level 7: émeraude 2%
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.IRON_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE}
    };

    public IslandListener(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre() { return c(plugin.getConfig().getString("prefix", "&8[&a&lSkyblock&8] &r")); }
    private boolean isSkyWorld(org.bukkit.World w) { return w.getName().equals(plugin.getConfig().getString("island.world", "world_skyblock")); }

    // ─── Protection Blocs ─────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.skyblock.admin")) return;
        if (!isSkyWorld(event.getBlock().getWorld())) return;

        IslandManager im = plugin.getIslandManager();
        Location loc = event.getBlock().getLocation();
        Island isl = im.getIslandAt(loc);

        if (isl == null) {
            event.setCancelled(true);
            player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !")));
            return;
        }

        boolean canBreak = isl.isMember(player.getUniqueId())
            || (isl.isVisitorsCanBreak() && !isl.isLocked());

        if (!canBreak) {
            event.setCancelled(true);
            player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !")));
            return;
        }

        isl.addBlocksBroken(1);
        int reward = plugin.getConfig().getInt("economy.block-reward", 1);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.skyblock.admin")) return;
        if (!isSkyWorld(event.getBlock().getWorld())) return;

        IslandManager im = plugin.getIslandManager();
        Location loc = event.getBlock().getLocation();
        Island isl = im.getIslandAt(loc);

        if (isl == null) {
            event.setCancelled(true);
            player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !")));
            return;
        }

        boolean canBuild = isl.isMember(player.getUniqueId())
            || (isl.isVisitorsCanBuild() && !isl.isLocked());

        if (!canBuild) {
            event.setCancelled(true);
            player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !")));
        }
    }

    // ─── Générateur de cobblestone amélioré ──────────────────────────────────

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        if (!isSkyWorld(event.getBlock().getWorld())) return;
        if (event.getNewState().getType() != Material.COBBLESTONE) return;

        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIslandAt(event.getBlock().getLocation());
        if (isl == null) return;

        int genLevel = Math.min(isl.getGeneratorLevel(), GEN_BLOCKS.length - 1);
        if (genLevel <= 0) return;

        Material[] pool = GEN_BLOCKS[genLevel];
        Material chosen = pool[random.nextInt(pool.length)];
        if (chosen == Material.COBBLESTONE) return;

        event.getNewState().setType(chosen);
    }

    // ─── Explosions ───────────────────────────────────────────────────────────

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!isSkyWorld(event.getLocation().getWorld())) return;
        event.blockList().removeIf(block -> {
            Island isl = plugin.getIslandManager().getIslandAt(block.getLocation());
            return isl != null;
        });
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!isSkyWorld(event.getBlock().getWorld())) return;
        event.blockList().removeIf(block -> {
            Island isl = plugin.getIslandManager().getIslandAt(block.getLocation());
            return isl != null;
        });
    }

    // ─── PvP ─────────────────────────────────────────────────────────────────

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!isSkyWorld(victim.getWorld())) return;

        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIslandAt(victim.getLocation());
        if (isl == null) return;
        if (isl.isMember(attacker.getUniqueId()) && isl.isMember(victim.getUniqueId())) return;
        if (!isl.isPvpEnabled()) {
            event.setCancelled(true);
            attacker.sendMessage(c("&cLe PvP est désactivé sur cette île."));
        }
    }

    // ─── Visites / bannissement ──────────────────────────────────────────────

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isSkyWorld(player.getWorld())) return;
        if (player.hasPermission("astraland.skyblock.admin")) return;
        Location to = event.getTo();
        if (to == null) return;
        // Ne vérifie que les changements de bloc (pas mouvements de caméra)
        if (event.getFrom().getBlockX() == to.getBlockX() && event.getFrom().getBlockZ() == to.getBlockZ()) return;

        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIslandAt(to);
        if (isl == null) return;

        if (isl.isBanned(player.getUniqueId())) {
            player.teleport(event.getFrom());
            player.sendMessage(c(plugin.getConfig().getString("messages.banned", "&cTu es banni de cette île.")));
            return;
        }

        if (isl.isLocked() && !isl.isMember(player.getUniqueId())) {
            player.teleport(event.getFrom());
            player.sendMessage(c(plugin.getConfig().getString("messages.locked", "&cCette île est verrouillée.")));
        }
    }

    // ─── Interactions contenants ─────────────────────────────────────────────

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isSkyWorld(player.getWorld())) return;
        if (player.hasPermission("astraland.skyblock.admin")) return;
        if (event.getClickedBlock() == null) return;
        Material mat = event.getClickedBlock().getType();
        boolean isContainer = mat == Material.CHEST || mat == Material.TRAPPED_CHEST
            || mat == Material.BARREL || mat == Material.HOPPER
            || mat == Material.DROPPER || mat == Material.DISPENSER
            || mat == Material.FURNACE || mat == Material.BLAST_FURNACE
            || mat == Material.SMOKER  || mat == Material.ENDER_CHEST;
        if (!isContainer) return;

        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIslandAt(event.getClickedBlock().getLocation());
        if (isl == null) return;
        if (isl.isMember(player.getUniqueId())) return;
        if (!isl.isVisitorsCanOpenChests()) {
            event.setCancelled(true);
            player.sendMessage(c("&cTu ne peux pas ouvrir les contenants ici."));
        }
    }

    // ─── Chat île ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isInPluginWorld(player)) return;
        if (!plugin.getIslandManager().isIslandChatEnabled(player.getUniqueId())) return;

        Island isl = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (isl == null) return;

        event.setCancelled(true);
        String msg = c("&8[&a🏝 Île&8] &e" + player.getName() + " &8» &f" + event.getMessage());
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (isl.isMember(p.getUniqueId())) p.sendMessage(msg);
        }
        plugin.getLogger().info("[IslandChat] " + player.getName() + ": " + event.getMessage());
    }

    // ─── GUI clicks ───────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof IslandSettingsGUI gui) {
            gui.handleClick(event, player);
        } else if (event.getInventory().getHolder() instanceof IslandGeneratorGUI gui) {
            gui.handleClick(event, player);
        } else if (event.getInventory().getHolder() instanceof IslandWarpGUI gui) {
            gui.handleClick(event, player);
        }
    }
}
