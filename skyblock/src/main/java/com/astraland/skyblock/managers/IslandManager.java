package com.astraland.skyblock.managers;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IslandManager {

    private final Skyblock plugin;
    private final Map<UUID, Island>  islands      = new HashMap<>();
    private final Map<UUID, UUID>    memberIsland = new HashMap<>();
    private final Map<UUID, Long>    deleteConfirm = new HashMap<>();
    private final Set<UUID>          islandChatEnabled = new HashSet<>();
    private File dataFile;

    public IslandManager(Skyblock plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        load();
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    public boolean hasIsland(UUID uuid)       { return islands.containsKey(uuid) || memberIsland.containsKey(uuid); }
    public Island getIsland(UUID uuid)        { if (islands.containsKey(uuid)) return islands.get(uuid); UUID o = memberIsland.get(uuid); return o == null ? null : islands.get(o); }
    public Island getOwnedIsland(UUID uuid)   { return islands.get(uuid); }
    public Collection<Island> getAllIslands()  { return islands.values(); }

    public boolean isInsideOwnIsland(UUID uuid, Location loc) {
        Island isl = getIsland(uuid);
        if (isl == null) return false;
        return isl.isInsideIsland(loc, plugin.getConfig().getInt("island.size", 100));
    }

    public Island getIslandAt(Location loc) {
        int size = plugin.getConfig().getInt("island.size", 100);
        for (Island isl : islands.values())
            if (isl.isInsideIsland(loc, size)) return isl;
        return null;
    }

    public List<Island> getTopIslands(int limit) {
        List<Island> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<Island> getWarps() {
        List<Island> list = new ArrayList<>();
        for (Island isl : islands.values())
            if (isl.isWarpEnabled() && !isl.isLocked()) list.add(isl);
        return list;
    }

    // ─── Island chat ──────────────────────────────────────────────────────────

    public boolean toggleIslandChat(UUID uuid) {
        if (islandChatEnabled.contains(uuid)) { islandChatEnabled.remove(uuid); return false; }
        else { islandChatEnabled.add(uuid); return true; }
    }
    public boolean isIslandChatEnabled(UUID uuid) { return islandChatEnabled.contains(uuid); }

    // ─── Delete confirmation ──────────────────────────────────────────────────

    public boolean hasPendingDelete(UUID uuid) {
        if (!deleteConfirm.containsKey(uuid)) return false;
        long seconds = plugin.getConfig().getInt("island.delete-confirm-seconds", 30);
        if (System.currentTimeMillis() - deleteConfirm.get(uuid) > seconds * 1000L) {
            deleteConfirm.remove(uuid); return false;
        }
        return true;
    }
    public void requestDelete(UUID uuid)  { deleteConfirm.put(uuid, System.currentTimeMillis()); }
    public void cancelDelete(UUID uuid)   { deleteConfirm.remove(uuid); }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    public Island createIsland(UUID owner) {
        String worldName = plugin.getConfig().getString("island.world", "world_skyblock");
        World world = Bukkit.getWorld(worldName);
        if (world == null) { plugin.getLogger().warning("Monde skyblock '" + worldName + "' introuvable !"); return null; }

        int dist = plugin.getConfig().getInt("island.spawn-distance", 200);
        int x    = islands.size() * dist;
        Location center = new Location(world, x, 65, 0);

        generateIsland(world, center.getBlockX(), center.getBlockY(), center.getBlockZ());

        Island island = new Island(owner, center);
        island.setHome(center.clone().add(0.5, 2, 0.5));
        islands.put(owner, island);
        memberIsland.put(owner, owner);

        int startBalance = plugin.getConfig().getInt("economy.starting-balance", 500);
        plugin.getEconomyManager().addBalance(owner, startBalance);

        saveAll();
        return island;
    }

    public void deleteIsland(UUID owner) {
        Island island = islands.remove(owner);
        if (island == null) return;
        memberIsland.remove(owner);
        for (UUID m : island.getMembers()) memberIsland.remove(m);
        for (UUID m : island.getCoopPlayers()) memberIsland.remove(m);
        deleteConfirm.remove(owner);
        saveAll();
    }

    public void addMember(Island island, UUID uuid) {
        island.addMember(uuid);
        memberIsland.put(uuid, island.getOwner());
        saveAll();
    }

    public void removeMember(Island island, UUID uuid) {
        island.removeMember(uuid);
        memberIsland.remove(uuid);
        saveAll();
    }

    public void addCoop(Island island, UUID uuid) {
        island.addCoop(uuid);
        memberIsland.put(uuid, island.getOwner());
        saveAll();
    }

    public void removeCoop(Island island, UUID uuid) {
        island.removeCoop(uuid);
        memberIsland.remove(uuid);
        saveAll();
    }

    // ─── Island Generation ────────────────────────────────────────────────────

    private void generateIsland(World world, int cx, int cy, int cz) {
        // ── Plateforme principale 5×5 (coins coupés) ──
        int[][] grassPattern = {
            {-1,-2},{0,-2},{1,-2},
            {-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},
            {-2,0},{-1,0},{0,0},{1,0},{2,0},
            {-2,1},{-1,1},{0,1},{1,1},{2,1},
            {-1,2},{0,2},{1,2}
        };
        for (int[] pos : grassPattern) {
            int bx = cx + pos[0], bz = cz + pos[1];
            world.getBlockAt(bx, cy,   bz).setType(Material.GRASS_BLOCK);
            world.getBlockAt(bx, cy-1, bz).setType(Material.DIRT);
            world.getBlockAt(bx, cy-2, bz).setType(Material.DIRT);
        }
        // Centre inférieur
        world.getBlockAt(cx, cy-3, cz).setType(Material.DIRT);
        world.getBlockAt(cx, cy-4, cz).setType(Material.STONE);

        // ── Arbre (au centre) ──
        for (int y = cy+1; y <= cy+3; y++) world.getBlockAt(cx, y, cz).setType(Material.OAK_LOG);
        placeLeaves(world, cx, cy+4, cz, 2);
        placeLeaves(world, cx, cy+5, cz, 1);
        world.getBlockAt(cx, cy+6, cz).setType(Material.OAK_LEAVES);

        // ── Coffre de démarrage ──
        Block chestBlock = world.getBlockAt(cx + 1, cy + 1, cz + 1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            fillStarterChest(chest);
        }

        // ── Générateur de cobblestone basique (côté gauche de l'île) ──
        // Pierre de support, laver, et source d'eau de l'autre côté
        // Le joueur les disposera pour faire son générateur
        // Structure: [STONE][STONE][STONE] extension de l'île vers -X
        world.getBlockAt(cx-3, cy-1, cz).setType(Material.STONE);
        world.getBlockAt(cx-3, cy,   cz).setType(Material.STONE);
        world.getBlockAt(cx-4, cy-1, cz).setType(Material.STONE);
        world.getBlockAt(cx-4, cy,   cz).setType(Material.STONE);

        // Lava source sur la plateforme gauche
        world.getBlockAt(cx-4, cy+1, cz).setType(Material.LAVA);

        // Plateforme eau à droite
        world.getBlockAt(cx+3, cy-1, cz).setType(Material.STONE);
        world.getBlockAt(cx+3, cy,   cz).setType(Material.STONE);
        world.getBlockAt(cx+4, cy-1, cz).setType(Material.STONE);
        world.getBlockAt(cx+4, cy,   cz).setType(Material.STONE);
        world.getBlockAt(cx+4, cy+1, cz).setType(Material.WATER);
    }

    private void placeLeaves(World w, int cx, int cy, int cz, int radius) {
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++)
                if (Math.abs(dx) + Math.abs(dz) <= radius + 1)
                    w.getBlockAt(cx+dx, cy, cz+dz).setType(Material.OAK_LEAVES);
    }

    private void fillStarterChest(Chest chest) {
        var inv = chest.getInventory();
        inv.setItem(0,  item(Material.OAK_LOG,         8,  "&eChêne de départ"));
        inv.setItem(1,  item(Material.LAVA_BUCKET,     1,  "&cSeau de lave &7(générateur)"));
        inv.setItem(2,  item(Material.ICE,              1,  "&bGlaçon &7(→ source d'eau)"));
        inv.setItem(3,  item(Material.WHEAT_SEEDS,     32, "&eGraines de blé"));
        inv.setItem(4,  item(Material.BONE_MEAL,       16, "&fEngrais"));
        inv.setItem(5,  item(Material.DIRT,            32, "&6Terre"));
        inv.setItem(6,  item(Material.MELON_SEEDS,      4, "&aGraines de melon"));
        inv.setItem(7,  item(Material.PUMPKIN_SEEDS,    4, "&6Graines de citrouille"));
        inv.setItem(8,  item(Material.SUGAR_CANE,       4, "&fCanne à sucre"));
        inv.setItem(9,  item(Material.BREAD,            8,  "&eNourriture de départ"));
        inv.setItem(10, item(Material.STONE_PICKAXE,   1,  "&7Pioche de départ"));
        inv.setItem(11, item(Material.STONE_SWORD,     1,  "&7Épée de départ"));
        inv.setItem(12, item(Material.STONE_AXE,       1,  "&7Hache de départ"));
        inv.setItem(13, item(Material.STONE_SHOVEL,    1,  "&7Pelle de départ"));
        inv.setItem(14, item(Material.COBBLESTONE,     32, "&7Cobblestone de départ"));
        inv.setItem(15, item(Material.TORCH,           16, "&eTorches"));
        inv.setItem(16, item(Material.SAND,             8, "&eSable"));
        inv.setItem(17, item(Material.CACTUS,           2, "&aCactus"));
    }

    private ItemStack item(Material m, int amount, String name) {
        ItemStack it = new ItemStack(m, amount);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            it.setItemMeta(meta);
        }
        return it;
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Island> e : islands.entrySet()) {
            Island isl = e.getValue();
            String p = "islands." + e.getKey();
            cfg.set(p + ".name",           isl.getName());
            cfg.set(p + ".level",          isl.getLevel());
            cfg.set(p + ".value",          isl.getValue());
            cfg.set(p + ".blocks",         isl.getBlocksBroken());
            cfg.set(p + ".locked",         isl.isLocked());
            cfg.set(p + ".pvp",            isl.isPvpEnabled());
            cfg.set(p + ".warpEnabled",    isl.isWarpEnabled());
            cfg.set(p + ".warpName",       isl.getWarpName());
            cfg.set(p + ".generatorLevel", isl.getGeneratorLevel());
            cfg.set(p + ".memberSlots",    isl.getMemberSlots());
            cfg.set(p + ".visitorsCanBuild",      isl.isVisitorsCanBuild());
            cfg.set(p + ".visitorsCanBreak",      isl.isVisitorsCanBreak());
            cfg.set(p + ".visitorsCanOpenChests", isl.isVisitorsCanOpenChests());
            List<String> members = new ArrayList<>();
            isl.getMembers().forEach(m -> members.add(m.toString()));
            cfg.set(p + ".members", members);
            List<String> coops = new ArrayList<>();
            isl.getCoopPlayers().forEach(m -> coops.add(m.toString()));
            cfg.set(p + ".coops", coops);
            List<String> bans = new ArrayList<>();
            isl.getBannedPlayers().forEach(m -> bans.add(m.toString()));
            cfg.set(p + ".banned", bans);
            if (isl.getHome()   != null) saveLocation(cfg, p + ".home",   isl.getHome());
            if (isl.getCenter() != null) saveLocation(cfg, p + ".center", isl.getCenter());
        }
        try { cfg.save(dataFile); } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void load() {
        if (!dataFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        if (cfg.getConfigurationSection("islands") == null) return;
        for (String uuidStr : cfg.getConfigurationSection("islands").getKeys(false)) {
            try {
                UUID owner = UUID.fromString(uuidStr);
                String p   = "islands." + uuidStr;
                Location center = loadLocation(cfg, p + ".center");
                if (center == null) continue;
                Island isl = new Island(owner, center);
                isl.setName(cfg.getString(p + ".name", "Île"));
                isl.setLevel(cfg.getInt(p + ".level", 0));
                isl.setValue(cfg.getLong(p + ".value", 0));
                isl.addBlocksBroken(cfg.getLong(p + ".blocks", 0));
                isl.setLocked(cfg.getBoolean(p + ".locked", false));
                isl.setPvpEnabled(cfg.getBoolean(p + ".pvp", false));
                isl.setWarpEnabled(cfg.getBoolean(p + ".warpEnabled", false));
                isl.setWarpName(cfg.getString(p + ".warpName", ""));
                isl.setGeneratorLevel(cfg.getInt(p + ".generatorLevel", 0));
                isl.setMemberSlots(cfg.getInt(p + ".memberSlots", 3));
                isl.setVisitorsCanBuild(cfg.getBoolean(p + ".visitorsCanBuild", false));
                isl.setVisitorsCanBreak(cfg.getBoolean(p + ".visitorsCanBreak", false));
                isl.setVisitorsCanOpenChests(cfg.getBoolean(p + ".visitorsCanOpenChests", false));
                Location home = loadLocation(cfg, p + ".home");
                if (home != null) isl.setHome(home);
                for (String m : cfg.getStringList(p + ".members"))   { try { UUID mu = UUID.fromString(m); isl.addMember(mu); memberIsland.put(mu, owner); } catch (Exception ignored) {} }
                for (String m : cfg.getStringList(p + ".coops"))     { try { UUID mu = UUID.fromString(m); isl.addCoop(mu);   memberIsland.put(mu, owner); } catch (Exception ignored) {} }
                for (String m : cfg.getStringList(p + ".banned"))    { try { UUID mu = UUID.fromString(m); isl.banPlayer(mu); } catch (Exception ignored) {} }
                islands.put(owner, isl);
                memberIsland.put(owner, owner);
            } catch (Exception ignored) {}
        }
    }

    private void saveLocation(FileConfiguration cfg, String path, Location loc) {
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX()); cfg.set(path + ".y", loc.getY()); cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", (double) loc.getYaw()); cfg.set(path + ".pitch", (double) loc.getPitch());
    }

    private Location loadLocation(FileConfiguration cfg, String path) {
        String w = cfg.getString(path + ".world");
        if (w == null) return null;
        World world = Bukkit.getWorld(w);
        if (world == null) return null;
        return new Location(world, cfg.getDouble(path + ".x"), cfg.getDouble(path + ".y"),
            cfg.getDouble(path + ".z"), (float) cfg.getDouble(path + ".yaw"), (float) cfg.getDouble(path + ".pitch"));
    }
}
