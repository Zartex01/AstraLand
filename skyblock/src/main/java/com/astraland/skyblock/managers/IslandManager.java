package com.astraland.skyblock.managers;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.gui.IslandSchematicGUI;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.models.IslandRole;
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
    private final Map<UUID, Island>  islands       = new HashMap<>();
    private final Map<UUID, UUID>    memberIsland  = new HashMap<>();
    private final Map<UUID, Long>    deleteConfirm = new HashMap<>();
    private final Set<UUID>          islandChatEnabled = new HashSet<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor       = new HashMap<>();
    private final Map<UUID, Integer>     savedExp         = new HashMap<>();
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

    // ─── Keep inventory ───────────────────────────────────────────────────────

    public void saveInventoryForDeath(UUID uuid, ItemStack[] contents, ItemStack[] armor, int xp) {
        savedInventories.put(uuid, contents);
        savedArmor.put(uuid, armor);
        savedExp.put(uuid, xp);
    }

    public boolean hasSavedInventory(UUID uuid) { return savedInventories.containsKey(uuid); }

    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] contents = savedInventories.remove(uuid);
        ItemStack[] armor    = savedArmor.remove(uuid);
        Integer xp           = savedExp.remove(uuid);
        if (contents != null) player.getInventory().setContents(contents);
        if (armor != null)    player.getInventory().setArmorContents(armor);
        if (xp != null)       player.setTotalExperience(xp);
    }

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
        return createIsland(owner, IslandSchematicGUI.Schematic.CLASSIQUE);
    }

    public Island createIsland(UUID owner, IslandSchematicGUI.Schematic schematic) {
        String worldName = plugin.getConfig().getString("island.world", "world_skyblock");
        World world = Bukkit.getWorld(worldName);
        if (world == null) { plugin.getLogger().warning("Monde skyblock '" + worldName + "' introuvable !"); return null; }

        int dist = plugin.getConfig().getInt("island.spawn-distance", 200);
        int x    = islands.size() * dist;
        Location center = new Location(world, x, 65, 0);

        generateIsland(world, center.getBlockX(), center.getBlockY(), center.getBlockZ(), schematic);

        Island island = new Island(owner, center);
        island.setHome(center.clone().add(0.5, 2, 0.5));
        island.setSchematicType(schematic.name());
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

    private void generateIsland(World world, int cx, int cy, int cz, IslandSchematicGUI.Schematic schematic) {
        switch (schematic) {
            case CLASSIQUE -> generateClassique(world, cx, cy, cz);
            case JUNGLE    -> generateJungle(world, cx, cy, cz);
            case DESERT    -> generateDesert(world, cx, cy, cz);
            case IGLOO     -> generateIgloo(world, cx, cy, cz);
        }
    }

    // ─── Classique ─────────────────────────────────────────────────────────────

    private void generateClassique(World world, int cx, int cy, int cz) {
        int[][] pattern = {
            {-1,-2},{0,-2},{1,-2},
            {-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},
            {-2,0},{-1,0},{0,0},{1,0},{2,0},
            {-2,1},{-1,1},{0,1},{1,1},{2,1},
            {-1,2},{0,2},{1,2}
        };
        for (int[] pos : pattern) {
            world.getBlockAt(cx+pos[0], cy,   cz+pos[1]).setType(Material.GRASS_BLOCK);
            world.getBlockAt(cx+pos[0], cy-1, cz+pos[1]).setType(Material.DIRT);
            world.getBlockAt(cx+pos[0], cy-2, cz+pos[1]).setType(Material.DIRT);
        }
        world.getBlockAt(cx, cy-3, cz).setType(Material.DIRT);
        world.getBlockAt(cx, cy-4, cz).setType(Material.STONE);

        for (int y = cy+1; y <= cy+3; y++) world.getBlockAt(cx, y, cz).setType(Material.OAK_LOG);
        placeLeaves(world, cx, cy+4, cz, 2, Material.OAK_LEAVES);
        placeLeaves(world, cx, cy+5, cz, 1, Material.OAK_LEAVES);
        world.getBlockAt(cx, cy+6, cz).setType(Material.OAK_LEAVES);

        Block chestBlock = world.getBlockAt(cx+1, cy+1, cz+1);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) fillStarterChest(chest, schematicItems(IslandSchematicGUI.Schematic.CLASSIQUE));

        buildCobbleGenerator(world, cx, cy, cz, Material.STONE);
    }

    // ─── Jungle ────────────────────────────────────────────────────────────────

    private void generateJungle(World world, int cx, int cy, int cz) {
        int[][] pattern = {
            {-2,-2},{-1,-2},{0,-2},{1,-2},{2,-2},
            {-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},
            {-2,0},{-1,0},{0,0},{1,0},{2,0},
            {-2,1},{-1,1},{0,1},{1,1},{2,1},
            {-2,2},{-1,2},{0,2},{1,2},{2,2}
        };
        for (int[] pos : pattern) {
            world.getBlockAt(cx+pos[0], cy,   cz+pos[1]).setType(Material.GRASS_BLOCK);
            world.getBlockAt(cx+pos[0], cy-1, cz+pos[1]).setType(Material.DIRT);
            world.getBlockAt(cx+pos[0], cy-2, cz+pos[1]).setType(Material.DIRT);
        }
        world.getBlockAt(cx, cy-3, cz).setType(Material.DIRT);

        // Grand arbre jungle
        for (int y = cy+1; y <= cy+7; y++) world.getBlockAt(cx, y, cz).setType(Material.JUNGLE_LOG);
        placeLeaves(world, cx, cy+6, cz, 3, Material.JUNGLE_LEAVES);
        placeLeaves(world, cx, cy+7, cz, 2, Material.JUNGLE_LEAVES);
        placeLeaves(world, cx, cy+8, cz, 1, Material.JUNGLE_LEAVES);

        // Bambous
        for (int y = cy+1; y <= cy+4; y++) world.getBlockAt(cx-2, y, cz-2).setType(Material.BAMBOO);
        for (int y = cy+1; y <= cy+3; y++) world.getBlockAt(cx+2, y, cz-1).setType(Material.BAMBOO);

        // Lianes
        world.getBlockAt(cx-1, cy+3, cz+2).setType(Material.VINE);
        world.getBlockAt(cx+1, cy+4, cz-2).setType(Material.VINE);

        Block chestBlock = world.getBlockAt(cx+2, cy+1, cz+2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) fillStarterChest(chest, schematicItems(IslandSchematicGUI.Schematic.JUNGLE));

        buildCobbleGenerator(world, cx, cy, cz, Material.STONE);
    }

    // ─── Désert ────────────────────────────────────────────────────────────────

    private void generateDesert(World world, int cx, int cy, int cz) {
        int[][] pattern = {
            {-2,-2},{-1,-2},{0,-2},{1,-2},{2,-2},
            {-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},
            {-2,0},{-1,0},{0,0},{1,0},{2,0},
            {-2,1},{-1,1},{0,1},{1,1},{2,1},
            {-1,2},{0,2},{1,2}
        };
        for (int[] pos : pattern) {
            world.getBlockAt(cx+pos[0], cy,   cz+pos[1]).setType(Material.SAND);
            world.getBlockAt(cx+pos[0], cy-1, cz+pos[1]).setType(Material.SANDSTONE);
            world.getBlockAt(cx+pos[0], cy-2, cz+pos[1]).setType(Material.SANDSTONE);
        }
        world.getBlockAt(cx, cy-3, cz).setType(Material.SMOOTH_SANDSTONE);

        // Cactus
        world.getBlockAt(cx-2, cy+1, cz).setType(Material.CACTUS);
        world.getBlockAt(cx-2, cy+2, cz).setType(Material.CACTUS);
        world.getBlockAt(cx+2, cy+1, cz+2).setType(Material.CACTUS);

        // Plante morte
        world.getBlockAt(cx+1, cy+1, cz-1).setType(Material.DEAD_BUSH);
        world.getBlockAt(cx-1, cy+1, cz+1).setType(Material.DEAD_BUSH);

        // Puits
        world.getBlockAt(cx, cy, cz).setType(Material.SANDSTONE);
        world.getBlockAt(cx, cy+1, cz).setType(Material.CHISELED_SANDSTONE);

        Block chestBlock = world.getBlockAt(cx+2, cy+1, cz-2);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) fillStarterChest(chest, schematicItems(IslandSchematicGUI.Schematic.DESERT));

        buildCobbleGenerator(world, cx, cy, cz, Material.SANDSTONE);
    }

    // ─── Igloo ────────────────────────────────────────────────────────────────

    private void generateIgloo(World world, int cx, int cy, int cz) {
        int[][] platform = {
            {-2,-2},{-1,-2},{0,-2},{1,-2},{2,-2},
            {-2,-1},{-1,-1},{0,-1},{1,-1},{2,-1},
            {-2,0},{-1,0},{0,0},{1,0},{2,0},
            {-2,1},{-1,1},{0,1},{1,1},{2,1},
            {-1,2},{0,2},{1,2}
        };
        for (int[] pos : platform) {
            world.getBlockAt(cx+pos[0], cy,   cz+pos[1]).setType(Material.SNOW_BLOCK);
            world.getBlockAt(cx+pos[0], cy-1, cz+pos[1]).setType(Material.PACKED_ICE);
            world.getBlockAt(cx+pos[0], cy-2, cz+pos[1]).setType(Material.BLUE_ICE);
        }
        world.getBlockAt(cx, cy-3, cz).setType(Material.PACKED_ICE);

        // Igloo dôme
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) + Math.abs(dz) <= 3)
                world.getBlockAt(cx+dx, cy+1, cz+dz).setType(Material.SNOW_BLOCK);
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            world.getBlockAt(cx+dx, cy+2, cz+dz).setType(Material.SNOW_BLOCK);
        world.getBlockAt(cx, cy+3, cz).setType(Material.SNOW_BLOCK);

        // Intérieur creux
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            world.getBlockAt(cx+dx, cy+1, cz+dz).setType(Material.AIR);
        world.getBlockAt(cx, cy+2, cz).setType(Material.AIR);

        // Entrée
        world.getBlockAt(cx, cy+1, cz+2).setType(Material.AIR);

        // Sapin
        for (int y = cy+1; y <= cy+3; y++) world.getBlockAt(cx+3, y, cz+3).setType(Material.SPRUCE_LOG);
        placeLeaves(world, cx+3, cy+3, cz+3, 1, Material.SPRUCE_LEAVES);
        placeLeaves(world, cx+3, cy+4, cz+3, 1, Material.SPRUCE_LEAVES);
        world.getBlockAt(cx+3, cy+5, cz+3).setType(Material.SPRUCE_LEAVES);

        Block chestBlock = world.getBlockAt(cx+1, cy+1, cz);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) fillStarterChest(chest, schematicItems(IslandSchematicGUI.Schematic.IGLOO));

        buildCobbleGenerator(world, cx, cy, cz, Material.PACKED_ICE);
    }

    private void buildCobbleGenerator(World world, int cx, int cy, int cz, Material floorMat) {
        // Schéma (vue du dessus, niveau cy) :
        //   [île cx+2][lave cx+3][spot cx+4][eau cx+5][mur cx+6]
        // L'arête de l'île (cx+2) fait office de mur arrière naturel.
        // La lave et l'eau sont séparées d'un seul bloc : elles s'écoulent
        // toutes les deux vers cx+4 et forment de la cobblestone.

        // 1. Plancher sous le canal (cx+3 à cx+6, z-1 à z+1)
        for (int dx = 3; dx <= 6; dx++)
            for (int dz = -1; dz <= 1; dz++)
                world.getBlockAt(cx+dx, cy-1, cz+dz).setType(floorMat);

        // 2. Parois latérales du canal (z-1 et z+1, positions cx+3 à cx+5)
        for (int dx = 3; dx <= 5; dx++) {
            world.getBlockAt(cx+dx, cy, cz-1).setType(floorMat);
            world.getBlockAt(cx+dx, cy, cz+1).setType(floorMat);
        }

        // 3. Mur frontal (cx+6) — contient l'eau côté opposé à l'île
        for (int dz = -1; dz <= 1; dz++)
            world.getBlockAt(cx+6, cy, cz+dz).setType(floorMat);

        // 4. Vider le spot cobblestone au cas où un bloc s'y trouverait
        world.getBlockAt(cx+4, cy, cz).setType(Material.AIR);

        // 5. Fluides en dernier (murs déjà en place)
        world.getBlockAt(cx+3, cy, cz).setType(Material.LAVA);   // source lave
        world.getBlockAt(cx+5, cy, cz).setType(Material.WATER);  // source eau
    }

    private void placeLeaves(World w, int cx, int cy, int cz, int radius, Material mat) {
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++)
                if (Math.abs(dx) + Math.abs(dz) <= radius + 1)
                    w.getBlockAt(cx+dx, cy, cz+dz).setType(mat);
    }

    private void fillStarterChest(Chest chest, List<ItemStack> extras) {
        var inv = chest.getInventory();
        inv.setItem(0,  item(Material.LAVA_BUCKET,   1,  "&cSeau de lave"));
        inv.setItem(1,  item(Material.ICE,            1,  "&bGlaçon → eau"));
        inv.setItem(2,  item(Material.WHEAT_SEEDS,   32,  "&eGraines de blé"));
        inv.setItem(3,  item(Material.BONE_MEAL,     16,  "&fEngrais"));
        inv.setItem(4,  item(Material.BREAD,          8,  "&eNourriture"));
        inv.setItem(5,  item(Material.STONE_PICKAXE,  1,  "&7Pioche de départ"));
        inv.setItem(6,  item(Material.STONE_SWORD,    1,  "&7Épée de départ"));
        inv.setItem(7,  item(Material.STONE_AXE,      1,  "&7Hache de départ"));
        inv.setItem(8,  item(Material.COBBLESTONE,   32,  "&7Cobblestone"));
        inv.setItem(9,  item(Material.TORCH,         16,  "&eTorches"));
        inv.setItem(10, item(Material.DIRT,          32,  "&6Terre"));
        inv.setItem(11, item(Material.CACTUS,         2,  "&aCactus"));
        inv.setItem(12, item(Material.SUGAR_CANE,     4,  "&fCanne à sucre"));
        for (int i = 0; i < extras.size() && i + 13 < 27; i++) {
            inv.setItem(13 + i, extras.get(i));
        }
    }

    private List<ItemStack> schematicItems(IslandSchematicGUI.Schematic s) {
        return switch (s) {
            case CLASSIQUE -> List.of(
                item(Material.OAK_LOG,        8, "&eChêne de départ"),
                item(Material.MELON_SEEDS,    4, "&aGraines de melon"),
                item(Material.PUMPKIN_SEEDS,  4, "&6Graines de citrouille"),
                item(Material.SAND,           8, "&eSable")
            );
            case JUNGLE    -> List.of(
                item(Material.JUNGLE_LOG,     8, "&2Bois de Jungle"),
                item(Material.COCOA_BEANS,    8, "&6Fèves de cacao"),
                item(Material.BAMBOO,        16, "&aGrandir du bambou"),
                item(Material.MELON_SEEDS,    8, "&aGraines de melon")
            );
            case DESERT    -> List.of(
                item(Material.SAND,          32, "&eSable"),
                item(Material.CACTUS,         8, "&aCactus"),
                item(Material.TERRACOTTA,    16, "&6Terracotta"),
                item(Material.SANDSTONE,     16, "&eGrès")
            );
            case IGLOO     -> List.of(
                item(Material.SNOW_BLOCK,    16, "&fBloc de Neige"),
                item(Material.PACKED_ICE,    8,  "&bGlace compacte"),
                item(Material.SPRUCE_LOG,    8,  "&7Bois d'Épicéa"),
                item(Material.SWEET_BERRIES, 8,  "&cBaies sauvages")
            );
        };
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
            cfg.set(p + ".name",                  isl.getName());
            cfg.set(p + ".level",                 isl.getLevel());
            cfg.set(p + ".value",                 isl.getValue());
            cfg.set(p + ".blocks",                isl.getBlocksBroken());
            cfg.set(p + ".locked",                isl.isLocked());
            cfg.set(p + ".pvp",                   isl.isPvpEnabled());
            cfg.set(p + ".warpEnabled",           isl.isWarpEnabled());
            cfg.set(p + ".warpName",              isl.getWarpName());
            cfg.set(p + ".generatorLevel",        isl.getGeneratorLevel());
            cfg.set(p + ".memberSlots",           isl.getMemberSlots());
            cfg.set(p + ".visitorsCanBuild",      isl.isVisitorsCanBuild());
            cfg.set(p + ".visitorsCanBreak",      isl.isVisitorsCanBreak());
            cfg.set(p + ".visitorsCanOpenChests", isl.isVisitorsCanOpenChests());
            cfg.set(p + ".flyUpgrade",            isl.hasFlyUpgrade());
            cfg.set(p + ".keepInventoryUpgrade",  isl.hasKeepInventoryUpgrade());
            cfg.set(p + ".memberSlotsUpgrade",    isl.getMemberSlotsUpgrade());
            cfg.set(p + ".bankBalance",           isl.getBankBalance());
            cfg.set(p + ".schematicType",         isl.getSchematicType());

            List<String> members = new ArrayList<>();
            isl.getMembers().forEach(m -> members.add(m.toString()));
            cfg.set(p + ".members", members);

            List<String> officers = new ArrayList<>();
            isl.getOfficers().forEach(m -> officers.add(m.toString()));
            cfg.set(p + ".officers", officers);

            // Sauvegarder les rôles
            for (Map.Entry<UUID, IslandRole> rEntry : isl.getRoles().entrySet()) {
                cfg.set(p + ".roles." + rEntry.getKey(), rEntry.getValue().name());
            }

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
                isl.setFlyUpgrade(cfg.getBoolean(p + ".flyUpgrade", false));
                isl.setKeepInventoryUpgrade(cfg.getBoolean(p + ".keepInventoryUpgrade", false));
                isl.setMemberSlotsUpgrade(cfg.getInt(p + ".memberSlotsUpgrade", 0));
                isl.setBankBalance(cfg.getLong(p + ".bankBalance", 0));
                isl.setSchematicType(cfg.getString(p + ".schematicType", "CLASSIQUE"));

                Location home = loadLocation(cfg, p + ".home");
                if (home != null) isl.setHome(home);
                for (String m : cfg.getStringList(p + ".members")) {
                    try { UUID mu = UUID.fromString(m); isl.addMember(mu); memberIsland.put(mu, owner); } catch (Exception ignored) {}
                }
                // Charger rôles
                if (cfg.getConfigurationSection(p + ".roles") != null) {
                    for (String ru : cfg.getConfigurationSection(p + ".roles").getKeys(false)) {
                        try {
                            UUID rUuid = UUID.fromString(ru);
                            IslandRole role = IslandRole.valueOf(cfg.getString(p + ".roles." + ru, "MEMBER"));
                            isl.setRole(rUuid, role);
                        } catch (Exception ignored) {}
                    }
                }
                for (String m : cfg.getStringList(p + ".coops"))   { try { UUID mu = UUID.fromString(m); isl.addCoop(mu);   memberIsland.put(mu, owner); } catch (Exception ignored) {} }
                for (String m : cfg.getStringList(p + ".banned"))  { try { UUID mu = UUID.fromString(m); isl.banPlayer(mu); } catch (Exception ignored) {} }
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
