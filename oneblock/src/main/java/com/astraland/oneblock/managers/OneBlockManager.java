package com.astraland.oneblock.managers;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import com.astraland.oneblock.models.UpgradeType;
import org.bukkit.*;
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
    private final File dataFile;

    public OneBlockManager(OneBlock plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "islands.yml");
        load();
    }

    public boolean hasIsland(UUID uuid) { return islands.containsKey(uuid) || memberIsland.containsKey(uuid); }
    public OneBlockIsland getIsland(UUID uuid) {
        if (islands.containsKey(uuid)) return islands.get(uuid);
        UUID owner = memberIsland.get(uuid);
        return owner == null ? null : islands.get(owner);
    }
    public OneBlockIsland getIslandByOwner(UUID owner) { return islands.get(owner); }
    public Collection<OneBlockIsland> getAllIslands() { return islands.values(); }

    public OneBlockIsland createIsland(UUID owner) {
        String worldName = plugin.getConfig().getString("oneblock.world", "world_oneblock");
        World world = Bukkit.getWorld(worldName);
        if (world == null) { plugin.getLogger().warning("Monde '" + worldName + "' introuvable !"); return null; }
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
            List<Material> blocks = phase.getBlocks();
            int idx = blocks.size() - 1 - plugin.getRandom().nextInt(Math.min(4, blocks.size()));
            mat = blocks.get(Math.max(0, idx));
        } else {
            mat = phase.getRandomBlock();
        }
        // Délai d'1 tick OBLIGATOIRE : sinon Bukkit casse le nouveau bloc
        // juste après que l'événement BlockBreakEvent le positionne
        final Material finalMat = mat;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (loc.getWorld() != null) {
                loc.getBlock().setType(finalMat);
                // Joue un son pour confirmer la régénération
                loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
            }
        }, 1L);
    }

    public void deleteIsland(UUID owner) {
        OneBlockIsland island = islands.remove(owner);
        if (island == null) return;
        memberIsland.remove(owner);
        island.getMembers().forEach(memberIsland::remove);
        island.getCoOwners().forEach(memberIsland::remove);
        saveAll();
    }

    public List<OneBlockIsland> getTop(int limit) {
        List<OneBlockIsland> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> Long.compare(b.getBlocksBroken(), a.getBlocksBroken()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<OneBlockIsland> getTopByWorth(int limit) {
        List<OneBlockIsland> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> Long.compare(b.getIslandWorth(), a.getIslandWorth()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    public List<OneBlockIsland> getTopByLevel(int limit) {
        List<OneBlockIsland> list = new ArrayList<>(islands.values());
        list.sort((a, b) -> Long.compare(b.getIslandLevel(), a.getIslandLevel()));
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
        if (island == null || island.isMember(targetUuid)) return false;
        island.invite(targetUuid);
        pendingInvites.put(targetUuid, ownerUuid);
        Bukkit.getScheduler().runTaskLater(plugin, () -> { island.removeInvite(targetUuid); pendingInvites.remove(targetUuid); }, 20L * 60);
        return true;
    }

    public UUID getPendingInviteFrom(UUID uuid) { return pendingInvites.get(uuid); }

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
        if (island == null || !island.isMember(memberUuid) || island.isOwner(memberUuid)) return false;
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

    public boolean setCoOwner(UUID ownerUuid, UUID targetUuid) {
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null || !island.isMember(targetUuid) || island.isOwner(targetUuid)) return false;
        island.addCoOwner(targetUuid);
        saveAll();
        return true;
    }

    public boolean removeCoOwner(UUID ownerUuid, UUID targetUuid) {
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null || !island.isCoOwner(targetUuid)) return false;
        island.removeCoOwner(targetUuid);
        island.addMember(targetUuid);
        saveAll();
        return true;
    }

    public boolean prestigeIsland(UUID ownerUuid) {
        OneBlockIsland island = islands.get(ownerUuid);
        if (island == null) return false;
        if (island.getCurrentPhase() != Phase.END) return false;
        if (island.getBlocksBroken() < 5000) return false;
        if (island.getPrestige() >= com.astraland.oneblock.gui.PrestigeGUI.MAX_PRESTIGE) return false;
        Location blockLoc = island.getBlockLocation();
        island.doPrestige();
        if (blockLoc != null && blockLoc.getWorld() != null) blockLoc.getBlock().setType(Material.GRASS_BLOCK);
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
            cfg.set(path + ".motd", isl.getMotd());
            cfg.set(path + ".bank", isl.getBankBalance());
            cfg.set(path + ".prestige", isl.getPrestige());
            cfg.set(path + ".worth", isl.getIslandWorth());

            List<String> members = new ArrayList<>();
            isl.getMembers().forEach(m -> members.add(m.toString()));
            cfg.set(path + ".members", members);

            List<String> coOwners = new ArrayList<>();
            isl.getCoOwners().forEach(m -> coOwners.add(m.toString()));
            cfg.set(path + ".co-owners", coOwners);

            if (isl.getBlockLocation() != null) saveLocation(cfg, path + ".blockloc", isl.getBlockLocation());
            if (isl.getHome() != null) saveLocation(cfg, path + ".home", isl.getHome());

            isl.getUpgrades().forEach((k, v) -> cfg.set(path + ".upgrades." + k, v));
            isl.getChallengeProgressMap().forEach((k, v) -> cfg.set(path + ".challenge-progress." + k, v));
            cfg.set(path + ".completed-challenges", new ArrayList<>(isl.getCompletedChallenges()));
            isl.getCollections().forEach((k, v) -> cfg.set(path + ".collections." + k, v));
            isl.getClaimedMilestones().forEach((k, v) -> cfg.set(path + ".claimed-milestones." + k, v));
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
                isl.setMotd(cfg.getString(path + ".motd", ""));
                isl.setBankBalance(cfg.getLong(path + ".bank", 0));
                isl.setPrestige(cfg.getInt(path + ".prestige", 0));
                isl.setIslandWorth(cfg.getLong(path + ".worth", 0));

                for (String m : cfg.getStringList(path + ".members")) {
                    try { UUID mu = UUID.fromString(m); isl.addMember(mu); memberIsland.put(mu, owner); } catch (Exception ignored) {}
                }
                for (String m : cfg.getStringList(path + ".co-owners")) {
                    try { UUID mu = UUID.fromString(m); isl.addCoOwner(mu); memberIsland.put(mu, owner); } catch (Exception ignored) {}
                }
                if (cfg.getConfigurationSection(path + ".upgrades") != null)
                    cfg.getConfigurationSection(path + ".upgrades").getKeys(false).forEach(k -> isl.getUpgrades().put(k, cfg.getInt(path + ".upgrades." + k, 0)));
                if (cfg.getConfigurationSection(path + ".challenge-progress") != null)
                    cfg.getConfigurationSection(path + ".challenge-progress").getKeys(false).forEach(k -> isl.getChallengeProgressMap().put(k, cfg.getLong(path + ".challenge-progress." + k, 0)));
                isl.getCompletedChallenges().addAll(cfg.getStringList(path + ".completed-challenges"));
                if (cfg.getConfigurationSection(path + ".collections") != null)
                    cfg.getConfigurationSection(path + ".collections").getKeys(false).forEach(k -> isl.getCollections().put(k, cfg.getLong(path + ".collections." + k, 0)));
                if (cfg.getConfigurationSection(path + ".claimed-milestones") != null)
                    cfg.getConfigurationSection(path + ".claimed-milestones").getKeys(false).forEach(k -> isl.getClaimedMilestones().put(k, cfg.getInt(path + ".claimed-milestones." + k, -1)));

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
