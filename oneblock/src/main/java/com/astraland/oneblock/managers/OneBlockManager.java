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

    // Islands where the current OneBlock is a reward chest (not a normal block)
    private final Set<UUID> pendingRewardChest = new HashSet<>();

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

        // Kit de démarrage + messages tutoriel
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            org.bukkit.entity.Player online = Bukkit.getPlayer(owner);
            if (online != null) giveStarterKit(online);
        }, 5L);

        return island;
    }

    private void giveStarterKit(org.bukkit.entity.Player player) {
        String c = "§";
        org.bukkit.inventory.ItemStack pick = new org.bukkit.inventory.ItemStack(Material.WOODEN_PICKAXE);
        org.bukkit.inventory.meta.ItemMeta pm = pick.getItemMeta();
        pm.setDisplayName(c + "e" + c + "l⛏ Pioche de l'Aube");
        pm.setLore(java.util.Arrays.asList(c + "7Casse tes premiers blocs !"));
        pick.setItemMeta(pm);

        org.bukkit.inventory.ItemStack sword = new org.bukkit.inventory.ItemStack(Material.WOODEN_SWORD);
        org.bukkit.inventory.meta.ItemMeta sm = sword.getItemMeta();
        sm.setDisplayName(c + "e" + c + "l⚔ Épée de l'Aube");
        sm.setLore(java.util.Arrays.asList(c + "7Défends-toi contre les mobs !"));
        sword.setItemMeta(sm);

        player.getInventory().addItem(
            pick,
            sword,
            new org.bukkit.inventory.ItemStack(Material.BREAD, 10),
            new org.bukkit.inventory.ItemStack(Material.OAK_LOG, 8),
            new org.bukkit.inventory.ItemStack(Material.TORCH, 16)
        );
        player.sendTitle(c + "6" + c + "l✦ Bienvenue sur OneBlock ✦",
            c + "7Casse le bloc d'herbe pour commencer !", 10, 80, 20);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&8&m─────────────────────────────"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&6&l✦ Kit de départ reçu !"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Les &e14 premiers blocs &7sont doux — cassables à la main."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Tous les &e15 blocs &7: un &6✦ Coffre de Récompense &7apparaît !"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Tape &e/ob &7pour ouvrir le menu de ton île."));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&8&m─────────────────────────────"));
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.5f);
    }

    public void regenerateBlock(OneBlockIsland island) {
        Location loc = island.getBlockLocation();
        if (loc == null || loc.getWorld() == null) return;
        island.incrementBlocks();
        long blocks = island.getBlocksBroken();
        Phase phase = island.getCurrentPhase();

        // ── Tous les 15 blocs : Coffre de Récompense ──────────────────────
        if (blocks % 15 == 0) {
            pendingRewardChest.add(island.getOwner());
            List<org.bukkit.inventory.ItemStack> items = getRewardItems(blocks);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (loc.getWorld() == null) return;
                loc.getBlock().setType(Material.TRAPPED_CHEST);
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.getBlock().getState();
                for (int i = 0; i < items.size() && i < chest.getInventory().getSize(); i++)
                    chest.getInventory().setItem(i, items.get(i));
                chest.update();
                loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_CHEST_OPEN, 1f, 0.8f);
                loc.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, loc.clone().add(0.5, 1, 0.5), 30, 0.5, 0.5, 0.5, 0.2);
            }, 1L);
            return;
        }

        // ── Premiers 14 blocs : uniquement des blocs cassables à mains nues ──
        int genLevel = island.getUpgradeLevel(UpgradeType.GENERATOR);
        Material mat;
        if (blocks < 15) {
            mat = getSoftBlock(phase);
        } else if (genLevel >= 2 && plugin.getRandom().nextInt(100) < 15) {
            List<Material> blockList = phase.getBlocks();
            int idx = blockList.size() - 1 - plugin.getRandom().nextInt(Math.min(4, blockList.size()));
            mat = blockList.get(Math.max(0, idx));
        } else {
            mat = phase.getRandomBlock();
        }

        // Délai d'1 tick OBLIGATOIRE : sinon Bukkit casse le nouveau bloc immédiatement
        final Material finalMat = mat;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (loc.getWorld() != null) {
                loc.getBlock().setType(finalMat);
                loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);
            }
        }, 1L);
    }

    // ── Blocs cassables sans outil ────────────────────────────────────────
    private Material getSoftBlock(Phase phase) {
        List<Material> soft = new ArrayList<>();
        for (Material m : phase.getBlocks()) if (isSoftMaterial(m)) soft.add(m);
        if (soft.isEmpty()) return Material.GRASS_BLOCK;
        return soft.get(plugin.getRandom().nextInt(soft.size()));
    }

    private boolean isSoftMaterial(Material m) {
        return switch (m) {
            case GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, ROOTED_DIRT,
                 SAND, RED_SAND, GRAVEL,
                 OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 OAK_LEAVES, BIRCH_LEAVES, SPRUCE_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES,
                 OAK_SAPLING, BIRCH_SAPLING, SPRUCE_SAPLING, JUNGLE_SAPLING, ACACIA_SAPLING, DARK_OAK_SAPLING,
                 DANDELION, POPPY, FERN, LARGE_FERN, DEAD_BUSH, BAMBOO,
                 MUSHROOM_STEM, BROWN_MUSHROOM, RED_MUSHROOM,
                 SOUL_SAND, SOUL_SOIL, NETHERRACK,
                 SNOW_BLOCK, POWDER_SNOW, ICE,
                 CACTUS, VINE, MELON, PUMPKIN -> true;
            default -> false;
        };
    }

    // ── Contenu des coffres de récompense ─────────────────────────────────
    private List<org.bukkit.inventory.ItemStack> getRewardItems(long blocksBroken) {
        List<org.bukkit.inventory.ItemStack> items = new ArrayList<>();
        int chestIndex = (int)(blocksBroken / 15);

        if (chestIndex == 1) {
            // Coffre 1 (bloc 15) — Démarrage
            items.add(named(Material.WOODEN_PICKAXE, "&e&l⛏ Pioche de l'Aube", "&7Continue à casser !"));
            items.add(named(Material.WOODEN_SWORD, "&e&l⚔ Épée de l'Aube", "&7Protège-toi !"));
            items.add(new org.bukkit.inventory.ItemStack(Material.BREAD, 10));
            items.add(new org.bukkit.inventory.ItemStack(Material.OAK_LOG, 16));
            items.add(new org.bukkit.inventory.ItemStack(Material.TORCH, 16));
        } else if (chestIndex == 2) {
            // Coffre 2 (bloc 30) — Progression
            items.add(named(Material.STONE_PICKAXE, "&7&l⛏ Pioche Solide", "&7Pour les minerais !"));
            items.add(named(Material.STONE_SWORD, "&7&l⚔ Épée Solide", "&7Plus efficace !"));
            items.add(new org.bukkit.inventory.ItemStack(Material.BREAD, 16));
            items.add(new org.bukkit.inventory.ItemStack(Material.IRON_NUGGET, 9));
            items.add(new org.bukkit.inventory.ItemStack(Material.SHIELD));
        } else if (chestIndex == 3) {
            // Coffre 3 (bloc 45) — Montée en puissance
            items.add(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 4));
            items.add(new org.bukkit.inventory.ItemStack(Material.COOKED_BEEF, 16));
            items.add(new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE, 4));
            items.add(new org.bukkit.inventory.ItemStack(Material.BONE_MEAL, 16));
            items.add(new org.bukkit.inventory.ItemStack(Material.CRAFTING_TABLE));
        } else if (chestIndex == 4) {
            // Coffre 4 (bloc 60) — Aventurier
            items.add(named(Material.IRON_PICKAXE, "&b&l⛏ Pioche Aventurier", "&7Pour les minerais rares !"));
            items.add(new org.bukkit.inventory.ItemStack(Material.IRON_INGOT, 8));
            items.add(new org.bukkit.inventory.ItemStack(Material.COOKED_BEEF, 16));
            items.add(enchanted(Material.IRON_PICKAXE, org.bukkit.enchantments.Enchantment.EFFICIENCY, 2));
        } else if (chestIndex == 5) {
            // Coffre 5 (bloc 75) — Expert
            items.add(named(Material.IRON_SWORD, "&b&l⚔ Épée Expert", "&7+Force I enchantée !"));
            items.add(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 2));
            items.add(new org.bukkit.inventory.ItemStack(Material.COOKED_PORKCHOP, 16));
            items.add(new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE, 8));
            items.add(new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 1));
        } else if (chestIndex == 6) {
            // Coffre 6 (bloc 90) — Confirmé
            items.add(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 4));
            items.add(new org.bukkit.inventory.ItemStack(Material.EMERALD, 2));
            items.add(new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE, 16));
            items.add(enchanted(Material.IRON_CHESTPLATE, org.bukkit.enchantments.Enchantment.PROTECTION, 2));
            items.add(new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 2));
        } else if (chestIndex <= 10) {
            // Coffres 7-10 (blocs 105-150) — Escalade progressive
            items.add(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 3 + plugin.getRandom().nextInt(4)));
            items.add(new org.bukkit.inventory.ItemStack(Material.EMERALD, 2 + plugin.getRandom().nextInt(3)));
            items.add(new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE, 16));
            items.add(new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 2 + plugin.getRandom().nextInt(2)));
            if (plugin.getRandom().nextBoolean())
                items.add(enchanted(Material.DIAMOND_PICKAXE, org.bukkit.enchantments.Enchantment.EFFICIENCY, 3));
        } else {
            // Coffres 11+ (blocs 165+) — Endgame
            items.add(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 5 + plugin.getRandom().nextInt(5)));
            items.add(new org.bukkit.inventory.ItemStack(Material.EMERALD, 4 + plugin.getRandom().nextInt(4)));
            items.add(new org.bukkit.inventory.ItemStack(Material.EXPERIENCE_BOTTLE, 32));
            items.add(new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 4));
            items.add(new org.bukkit.inventory.ItemStack(Material.NETHERITE_SCRAP, plugin.getRandom().nextInt(2) + 1));
            if (plugin.getRandom().nextInt(3) == 0)
                items.add(new org.bukkit.inventory.ItemStack(Material.NETHER_STAR));
        }
        return items;
    }

    private org.bukkit.inventory.ItemStack named(Material mat, String name, String lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(java.util.Arrays.asList(ChatColor.translateAlternateColorCodes('&', lore)));
        item.setItemMeta(meta);
        return item;
    }

    private org.bukkit.inventory.ItemStack enchanted(Material mat, org.bukkit.enchantments.Enchantment ench, int level) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat);
        item.addUnsafeEnchantment(ench, level);
        return item;
    }

    public boolean isRewardChest(UUID islandOwner) { return pendingRewardChest.contains(islandOwner); }
    public void claimRewardChest(UUID islandOwner) { pendingRewardChest.remove(islandOwner); }

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
