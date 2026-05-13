package com.astraland.oneblock.managers;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OneBlockManager {

    private final OneBlock plugin;
    private final Map<UUID, OneBlockIsland> islands = new HashMap<>();
    private final Map<UUID, UUID> memberIsland = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();
    private File dataFile;

    public OneBlockManager(OneBlock plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        load();
    }

    public boolean hasIsland(UUID uuid) {
        return islands.containsKey(uuid) || memberIsland.containsKey(uuid);
    }

    public OneBlockIsland getIsland(UUID uuid) {
        if (islands.containsKey(uuid)) return islands.get(uuid);
        UUID owner = memberIsland.get(uuid);
        return owner == null ? null : islands.get(owner);
    }

    public OneBlockIsland getIslandByOwner(UUID owner) {
        return islands.get(owner);
    }

    public Collection<OneBlockIsland> getAllIslands() {
        return islands.values();
    }

    public OneBlockIsland createIsland(UUID owner) {
        String worldName = plugin.getConfig().getString("oneblock.world", "world_oneblock");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Monde oneblock '" + worldName + "' introuvable !");
            return null;
        }
        int dist = plugin.getConfig().getInt("oneblock.spawn-distance", 300);
        int x = islands.size() * dist;
        Location blockLoc = new Location(world, x, 65, 0);
        world.getBlockAt(blockLoc).setType(Material.GRASS_BLOCK);

        OneBlockIsland island = new OneBlockIsland(owner, blockLoc);
        islands.put(owner, island);
        memberIsland.put(owner, owner);
        saveAll();
        return island;
    }

    public void regenerateBlock(OneBlockIsland island) {
        Location loc = island.getBlockLocation();
        if (loc == null || loc.getWorld() == null) return;
        island.incrementBlocks();
        Phase phase = island.getCurrentPhase();

        int genLevel = island.getUpgradeLevel(UpgradeType.GENERATOR);
        Material mat;
        if (genLevel >= 2 && plugin.getRandom().nextInt(100) < 15) {
            mat = getBoostedBlock(phase);
        } else {
            mat = phase.getRandomBlock();
        }
        loc.getBlock().setType(mat);
    }

    private Material getBoostedBlock(Phase phase) {
        List<Material> blocks = phase.getBlocks();
        int idx = blocks.size() - 1 - plugin.getRandom().nextInt(Math.min(4, blocks.size()));
        return blocks.get(Math.max(0, idx));
    }

    public void deleteIsland(UUID owner) {
        OneBlockIsland island = islands.remove(owner);
        if (island == null) return;
        memberIsland.remove(owner);
        for (UUID m : island.getMembers()) memberIsland.remove(m);
        saveAll();
    }

    public List<OneBlockIsland> getTop(int limit) {
        List<OneBlockIsland> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> Long.compare(b.getBlocksBroken(), a.getBlocksBroken()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<OneBlockIsland> getPublicWarps() {
        return islands.values().stream()
            .filter(i -> i.isWarpEnabled() && i.isVisitorsAllowed())
            .sorted((a, b) -> Long.compare(b.getBlocksBroken(), a.getBlocksBroken()))
            .collect(Collectors.toList());
    }

    public boolean invitePlayer(UUID ownerUuid, UUID targetUuid) {
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null) return false;
        if (island.isMember(targetUuid)) return false;
        island.invite(targetUuid);
        pendingInvites.put(targetUuid, ownerUuid);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            island.removeInvite(targetUuid);
            pendingInvites.remove(targetUuid);
        }, 20L * 60);
        return true;
    }

    public UUID getPendingInviteFrom(UUID targetUuid) {
        return pendingInvites.get(targetUuid);
    }

    public boolean acceptInvite(UUID targetUuid) {
        UUID ownerUuid = pendingInvites.remove(targetUuid);
        if (ownerUuid == null) return false;
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null) return false;
        island.addMember(targetUuid);
        memberIsland.put(targetUuid, ownerUuid);
        island.addChallengeProgress(com.astraland.oneblock.models.IslandChallenge.ChallengeType.MEMBERS_INVITED, 1);
        saveAll();
        return true;
    }

    public boolean declineInvite(UUID targetUuid) {
        UUID ownerUuid = pendingInvites.remove(targetUuid);
        if (ownerUuid == null) return false;
        OneBlockIsland island = islands.get(ownerUuid);
        if (island != null) island.removeInvite(targetUuid);
        return true;
    }

    public boolean kickMember(UUID ownerUuid, UUID memberUuid) {
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null) return false;
        if (!island.isMember(memberUuid) || island.isOwner(memberUuid)) return false;
        island.removeMember(memberUuid);
        memberIsland.remove(memberUuid);
        saveAll();
        return true;
    }

    public boolean leaveIsland(UUID memberUuid) {
        UUID ownerUuid = memberIsland.get(memberUuid);
        if (ownerUuid == null || ownerUuid.equals(memberUuid)) return false;
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null) return false;
        island.removeMember(memberUuid);
        memberIsland.remove(memberUuid);
        saveAll();
        return true;
    }

    public String getOwnerName(OneBlockIsland island) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(island.getOwner());
        return op.getName() != null ? op.getName() : "Inconnu";
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, OneBlockIsland> e : islands.entrySet()) {
            OneBlockIsland isl = e.getValue();
            String path = "islands." + e.getKey();
            cfg.set(path + ".blocks", isl.getBlocksBroken());
            cfg.set(path + ".phase", isl.getCurrentPhase().name());
            cfg.set(path + ".pvp", isl.isPvpEnabled());
            cfg.set(path + ".visitors", isl.isVisitorsAllowed());
            cfg.set(path + ".warp", isl.isWarpEnabled());
            cfg.set(path + ".warp-name", isl.getWarpName());
            List<String> members = new ArrayList<>();
            isl.getMembers().forEach(m -> members.add(m.toString()));
            cfg.set(path + ".members", members);
            if (isl.getBlockLocation() != null) saveLocation(cfg, path + ".blockloc", isl.getBlockLocation());
            if (isl.getHome() != null) saveLocation(cfg, path + ".home", isl.getHome());
            for (Map.Entry<String, Integer> upg : isl.getUpgrades().entrySet()) {
                cfg.set(path + ".upgrades." + upg.getKey(), upg.getValue());
            }
            for (Map.Entry<String, Long> cp : isl.getChallengeProgressMap().entrySet()) {
                cfg.set(path + ".challenge-progress." + cp.getKey(), cp.getValue());
            }
            cfg.set(path + ".completed-challenges", new ArrayList<>(isl.getCompletedChallenges()));
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
                String path = "islands." + uuidStr;
                Location blockLoc = loadLocation(cfg, path + ".blockloc");
                if (blockLoc == null) continue;
                OneBlockIsland isl = new OneBlockIsland(owner, blockLoc);
                isl.setBlocksBroken(cfg.getLong(path + ".blocks", 0));
                Location home = loadLocation(cfg, path + ".home");
                if (home != null) isl.setHome(home);
                isl.setPvpEnabled(cfg.getBoolean(path + ".pvp", false));
                isl.setVisitorsAllowed(cfg.getBoolean(path + ".visitors", true));
                isl.setWarpEnabled(cfg.getBoolean(path + ".warp", false));
                isl.setWarpName(cfg.getString(path + ".warp-name", ""));
                for (String m : cfg.getStringList(path + ".members")) {
                    try {
                        UUID mu = UUID.fromString(m);
                        isl.addMember(mu);
                        memberIsland.put(mu, owner);
                    } catch (Exception ignored) {}
                }
                if (cfg.getConfigurationSection(path + ".upgrades") != null) {
                    for (String key : cfg.getConfigurationSection(path + ".upgrades").getKeys(false)) {
                        isl.getUpgrades().put(key, cfg.getInt(path + ".upgrades." + key, 0));
                    }
                }
                if (cfg.getConfigurationSection(path + ".challenge-progress") != null) {
                    for (String key : cfg.getConfigurationSection(path + ".challenge-progress").getKeys(false)) {
                        isl.getChallengeProgressMap().put(key, cfg.getLong(path + ".challenge-progress." + key, 0));
                    }
                }
                isl.getCompletedChallenges().addAll(cfg.getStringList(path + ".completed-challenges"));
                islands.put(owner, isl);
                memberIsland.put(owner, owner);
            } catch (Exception ignored) {}
        }
    }

    private void saveLocation(FileConfiguration cfg, String path, Location loc) {
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
    }

    private Location loadLocation(FileConfiguration cfg, String path) {
        String w = cfg.getString(path + ".world");
        if (w == null) return null;
        World world = Bukkit.getWorld(w);
        if (world == null) return null;
        return new Location(world, cfg.getDouble(path + ".x"), cfg.getDouble(path + ".y"), cfg.getDouble(path + ".z"));
    }
}
