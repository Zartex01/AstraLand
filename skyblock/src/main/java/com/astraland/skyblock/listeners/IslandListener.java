package com.astraland.skyblock.listeners;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.challenges.ChallengeGUI;
import com.astraland.skyblock.gui.*;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.quests.DailyQuest;
import com.astraland.skyblock.quests.DailyQuestGUI;
import com.astraland.skyblock.ranks.IslandRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class IslandListener implements Listener {

    private final Skyblock plugin;
    private final Random random = new Random();

    private static final Material[][] GEN_BLOCKS = {
        {Material.COBBLESTONE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
         Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
         Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE, Material.ANDESITE, Material.GRANITE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
         Material.STONE, Material.STONE, Material.STONE, Material.ANDESITE, Material.COAL_ORE, Material.GRANITE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE,
         Material.STONE, Material.STONE, Material.IRON_ORE, Material.IRON_ORE, Material.COAL_ORE, Material.ANDESITE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE,
         Material.STONE, Material.IRON_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.ANDESITE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.STONE,
         Material.IRON_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.DIAMOND_ORE, Material.ANDESITE},
        {Material.COBBLESTONE, Material.COBBLESTONE, Material.STONE, Material.IRON_ORE,
         Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_ORE, Material.DIAMOND_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE}
    };

    public IslandListener(Skyblock plugin) { this.plugin = plugin; }

    private String c(String s)         { return ChatColor.translateAlternateColorCodes('&', s); }
    private String pre()               { return c(plugin.getConfig().getString("prefix", "&8[&a&lSkyblock&8] &r")); }
    private String fmt(long v)         { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
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

        // Valeur incrémentale
        int blockVal = plugin.getLevelManager().getBlockValue(event.getBlock().getType());
        if (blockVal > 0) {
            isl.setValue(isl.getValue() - blockVal);
            updateIslandLevel(isl);
        }

        isl.addBlocksBroken(1);
        int reward = plugin.getConfig().getInt("economy.block-reward", 1);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);

        // Quête : casser des blocs
        plugin.getQuestManager().addProgress(player.getUniqueId(), DailyQuest.Type.BREAK_BLOCKS, 1);
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
            return;
        }

        int blockVal = plugin.getLevelManager().getBlockValue(event.getBlock().getType());
        if (blockVal > 0) {
            int oldLevel = isl.getLevel();
            isl.setValue(isl.getValue() + blockVal);
            updateIslandLevel(isl);
            if (isl.getLevel() > oldLevel) {
                notifyLevelUp(player, isl, oldLevel);
            }
        }

        // Quête : poser des blocs
        plugin.getQuestManager().addProgress(player.getUniqueId(), DailyQuest.Type.PLACE_BLOCKS, 1);
    }

    private void updateIslandLevel(Island isl) {
        int newLevel = plugin.getLevelManager().valueToLevel(isl.getValue());
        isl.setLevel(newLevel);
    }

    private void notifyLevelUp(Player player, Island isl, int oldLevel) {
        int newLevel = isl.getLevel();
        IslandRank oldRank = IslandRank.fromLevel(oldLevel);
        IslandRank newRank = IslandRank.fromLevel(newLevel);
        boolean rankUp    = !oldRank.equals(newRank);

        // Joueur
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(pre() + c("&a🎉 Ton île est passée au &e&lNiveau " + newLevel + " &a! (&6" + fmt(isl.getValue()) + " pts&a)"));
        if (rankUp) player.sendMessage(pre() + c("&5✦ Nouveau rang : " + newRank.getFullName() + " &5! Bonus vente : &6+" + newRank.getSellBonus() + "%"));

        // Membres en ligne
        for (UUID uid : isl.getMembers()) {
            Player member = Bukkit.getPlayer(uid);
            if (member != null && !member.equals(player)) {
                member.playSound(member.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                member.sendMessage(pre() + c("&a🎉 L'île est passée au &e&lNiveau " + newLevel + " &a!"));
                if (rankUp) member.sendMessage(pre() + c("&5✦ Nouveau rang : " + newRank.getFullName()));
            }
        }

        // Annonce globale tous les 5 niveaux ou à chaque rang
        if (newLevel % 5 == 0 || rankUp) {
            String ownerName = Bukkit.getOfflinePlayer(isl.getOwner()).getName();
            String announce = rankUp
                ? c("&6&l[AstraLand] &e" + ownerName + " &7a atteint le rang &5" + newRank.getFullName() + " &7! &8(Niveau " + newLevel + ")")
                : c("&6&l[AstraLand] &7L'île de &e" + ownerName + " &7est passée au &aNiveau " + newLevel + " &7! 🎉");
            for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(announce);
        }
    }

    // ─── Kill de monstres (quête) ─────────────────────────────────────────────

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (!isSkyWorld(event.getEntity().getWorld())) return;
        plugin.getQuestManager().addProgress(killer.getUniqueId(), DailyQuest.Type.KILL_MOBS, 1);
    }

    // ─── Pêche (quête) ────────────────────────────────────────────────────────

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!isSkyWorld(event.getPlayer().getWorld())) return;
        plugin.getQuestManager().addProgress(event.getPlayer().getUniqueId(), DailyQuest.Type.FISH, 1);
    }

    // ─── Récolte (quête) ──────────────────────────────────────────────────────

    @EventHandler
    public void onHarvest(BlockBreakEvent event) {
        if (!isSkyWorld(event.getBlock().getWorld())) return;
        Material mat = event.getBlock().getType();
        boolean isCrop = mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES
            || mat == Material.BEETROOTS || mat == Material.MELON || mat == Material.PUMPKIN
            || mat == Material.SUGAR_CANE || mat == Material.BAMBOO || mat == Material.CACTUS
            || mat == Material.NETHER_WART || mat == Material.SWEET_BERRY_BUSH;
        if (!isCrop) return;
        plugin.getQuestManager().addProgress(event.getPlayer().getUniqueId(), DailyQuest.Type.GROW_CROPS, 1);
    }

    // ─── Protection Buckets ───────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.skyblock.admin")) return;
        if (!isSkyWorld(player.getWorld())) return;
        Block target = event.getBlockClicked().getRelative(event.getBlockFace());
        Island isl = plugin.getIslandManager().getIslandAt(target.getLocation());
        if (isl == null) { event.setCancelled(true); player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !"))); return; }
        boolean canBuild = isl.isMember(player.getUniqueId()) || (isl.isVisitorsCanBuild() && !isl.isLocked());
        if (!canBuild) { event.setCancelled(true); player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !"))); }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("astraland.skyblock.admin")) return;
        if (!isSkyWorld(player.getWorld())) return;
        Block target = event.getBlockClicked();
        Island isl = plugin.getIslandManager().getIslandAt(target.getLocation());
        if (isl == null) { event.setCancelled(true); player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !"))); return; }
        boolean canBreak = isl.isMember(player.getUniqueId()) || (isl.isVisitorsCanBreak() && !isl.isLocked());
        if (!canBreak) { event.setCancelled(true); player.sendMessage(c(plugin.getConfig().getString("messages.protected", "&cZone protégée !"))); }
    }

    // ─── Protection Pistons ───────────────────────────────────────────────────

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!isSkyWorld(event.getBlock().getWorld())) return;
        Island pistonIsland = plugin.getIslandManager().getIslandAt(event.getBlock().getLocation());
        for (Block block : event.getBlocks()) {
            Island blockIsland = plugin.getIslandManager().getIslandAt(block.getLocation());
            if (!isSameIsland(pistonIsland, blockIsland)) { event.setCancelled(true); return; }
            Block dest = block.getRelative(event.getDirection());
            Island destIsland = plugin.getIslandManager().getIslandAt(dest.getLocation());
            if (!isSameIsland(pistonIsland, destIsland)) { event.setCancelled(true); return; }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!isSkyWorld(event.getBlock().getWorld())) return;
        Island pistonIsland = plugin.getIslandManager().getIslandAt(event.getBlock().getLocation());
        for (Block block : event.getBlocks()) {
            Island blockIsland = plugin.getIslandManager().getIslandAt(block.getLocation());
            if (!isSameIsland(pistonIsland, blockIsland)) { event.setCancelled(true); return; }
        }
    }

    private boolean isSameIsland(Island a, Island b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getOwner().equals(b.getOwner());
    }

    // ─── Générateur amélioré ──────────────────────────────────────────────────

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
        event.blockList().removeIf(block -> plugin.getIslandManager().getIslandAt(block.getLocation()) != null);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!isSkyWorld(event.getBlock().getWorld())) return;
        event.blockList().removeIf(block -> plugin.getIslandManager().getIslandAt(block.getLocation()) != null);
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

    // ─── Keep Inventory ───────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!isSkyWorld(player.getWorld())) return;
        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIsland(player.getUniqueId());
        if (isl == null || !isl.hasKeepInventoryUpgrade()) return;
        int islandSize = plugin.getConfig().getInt("island.size", 100);
        if (!isl.isInsideIsland(player.getLocation(), islandSize)) return;
        im.saveInventoryForDeath(player.getUniqueId(), player.getInventory().getContents().clone(), player.getInventory().getArmorContents().clone(), player.getTotalExperience());
        event.getDrops().clear();
        event.setDroppedExp(0);
        player.sendMessage(pre() + c("&a🛡 Inventaire sauvegardé !"));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        IslandManager im = plugin.getIslandManager();
        if (!im.hasSavedInventory(player.getUniqueId())) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> im.restoreInventory(player), 1L);
    }

    // ─── Vol sur l'île ────────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateFlightState(event.getPlayer()), 10L);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateFlightState(event.getPlayer()), 2L);
    }

    private void updateFlightState(Player player) {
        if (!isSkyWorld(player.getWorld())) {
            if (player.getAllowFlight() && !player.isOp() && !player.hasPermission("astraland.skyblock.admin")) {
                player.setAllowFlight(false); player.setFlying(false);
            }
            return;
        }
        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIsland(player.getUniqueId());
        int size = plugin.getConfig().getInt("island.size", 100);
        boolean onOwnIsland = isl != null && isl.hasFlyUpgrade() && isl.isInsideIsland(player.getLocation(), size);
        if (onOwnIsland) {
            if (!player.getAllowFlight()) { player.setAllowFlight(true); player.sendMessage(pre() + c("&a✈ Vol activé !")); }
        } else if (!player.isOp() && !player.hasPermission("astraland.skyblock.admin")) {
            if (player.getAllowFlight()) { player.setAllowFlight(false); player.setFlying(false); }
        }
    }

    // ─── Déplacement / bannissement / vol ────────────────────────────────────

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isSkyWorld(player.getWorld())) return;
        if (player.hasPermission("astraland.skyblock.admin")) return;
        Location to = event.getTo();
        if (to == null) return;
        if (event.getFrom().getBlockX() == to.getBlockX() && event.getFrom().getBlockZ() == to.getBlockZ()) return;

        IslandManager im = plugin.getIslandManager();
        Island isl = im.getIslandAt(to);
        Island ownIsland = im.getIsland(player.getUniqueId());
        int size = plugin.getConfig().getInt("island.size", 100);

        if (ownIsland != null && ownIsland.hasFlyUpgrade()) {
            boolean onOwn = ownIsland.isInsideIsland(to, size);
            if (onOwn && !player.getAllowFlight()) {
                player.setAllowFlight(true);
            } else if (!onOwn && player.getAllowFlight() && !player.isOp() && !player.hasPermission("astraland.skyblock.admin")) {
                player.setAllowFlight(false); player.setFlying(false);
                player.sendMessage(pre() + c("&7✈ Vol désactivé hors de ton île."));
            }
        }

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

    // ─── Interactions contenants ──────────────────────────────────────────────

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
        for (Player p : Bukkit.getOnlinePlayers()) if (isl.isMember(p.getUniqueId())) p.sendMessage(msg);
        plugin.getLogger().info("[IslandChat] " + player.getName() + ": " + event.getMessage());
    }

    // ─── GUI clicks ───────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if      (event.getInventory().getHolder() instanceof IslandSettingsGUI  gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandGeneratorGUI gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandWarpGUI      gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandMenuGUI      gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandUpgradesGUI  gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof ChallengeGUI       gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof DailyQuestGUI      gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandTopGUI       gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandMembersGUI   gui) { gui.handleClick(event, player); }
        else if (event.getInventory().getHolder() instanceof IslandSchematicGUI gui) { gui.handleClick(event, player); }
    }
}
