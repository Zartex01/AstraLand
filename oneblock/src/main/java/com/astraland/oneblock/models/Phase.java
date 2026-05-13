package com.astraland.oneblock.models;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum Phase {

    PLAINES(0, "Plaines", "&a",
        Arrays.asList(
            Material.GRASS_BLOCK, Material.DIRT, Material.OAK_LOG, Material.OAK_LEAVES,
            Material.STONE, Material.GRAVEL, Material.SAND, Material.OAK_SAPLING,
            Material.WHEAT_SEEDS, Material.POPPY, Material.DANDELION, Material.COBBLESTONE,
            Material.GRASS_BLOCK, Material.DIRT, Material.STONE, Material.COBBLESTONE
        ),
        Arrays.asList(EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN,
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER),
        Arrays.asList(
            new LootEntry(Material.WHEAT, 1, 8),
            new LootEntry(Material.CARROT, 1, 4),
            new LootEntry(Material.OAK_SAPLING, 1, 3),
            new LootEntry(Material.BONE, 1, 6),
            new LootEntry(Material.APPLE, 1, 3),
            new LootEntry(Material.STRING, 1, 4)
        )
    ),
    FORET(100, "Forêt", "&2",
        Arrays.asList(
            Material.OAK_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.OAK_LEAVES,
            Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.MUSHROOM_STEM, Material.MOSSY_COBBLESTONE,
            Material.PODZOL, Material.COARSE_DIRT, Material.FERN, Material.BAMBOO,
            Material.OAK_LOG, Material.BIRCH_LOG, Material.OAK_LOG, Material.JUNGLE_LOG
        ),
        Arrays.asList(EntityType.WOLF, EntityType.FOX, EntityType.BEE, EntityType.SPIDER,
            EntityType.CREEPER, EntityType.ZOMBIE, EntityType.SKELETON),
        Arrays.asList(
            new LootEntry(Material.OAK_LOG, 4, 16),
            new LootEntry(Material.MUSHROOM_STEW, 1, 2),
            new LootEntry(Material.COCOA_BEANS, 2, 6),
            new LootEntry(Material.BONE_MEAL, 4, 12),
            new LootEntry(Material.JUNGLE_SAPLING, 1, 3),
            new LootEntry(Material.LEATHER, 1, 4)
        )
    ),
    DESERT(500, "Désert", "&e",
        Arrays.asList(
            Material.SAND, Material.SANDSTONE, Material.RED_SAND, Material.CACTUS,
            Material.DEAD_BUSH, Material.CHISELED_SANDSTONE, Material.GOLD_ORE, Material.IRON_ORE,
            Material.CLAY, Material.TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.BONE_BLOCK,
            Material.SAND, Material.SANDSTONE, Material.GOLD_ORE, Material.IRON_ORE
        ),
        Arrays.asList(EntityType.HUSK, EntityType.WITCH, EntityType.CAVE_SPIDER,
            EntityType.PILLAGER, EntityType.SKELETON),
        Arrays.asList(
            new LootEntry(Material.GOLD_NUGGET, 4, 16),
            new LootEntry(Material.IRON_INGOT, 2, 6),
            new LootEntry(Material.CACTUS, 4, 12),
            new LootEntry(Material.SAND, 8, 32),
            new LootEntry(Material.ROTTEN_FLESH, 2, 6),
            new LootEntry(Material.GOLD_INGOT, 1, 3)
        )
    ),
    NEIGE(1000, "Neige", "&b",
        Arrays.asList(
            Material.SNOW_BLOCK, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE,
            Material.SPRUCE_LOG, Material.SPRUCE_LEAVES, Material.POWDER_SNOW,
            Material.COBBLESTONE, Material.IRON_ORE, Material.COAL_ORE, Material.EMERALD_ORE,
            Material.SNOW_BLOCK, Material.ICE, Material.IRON_ORE, Material.EMERALD_ORE
        ),
        Arrays.asList(EntityType.STRAY, EntityType.POLAR_BEAR, EntityType.GOAT,
            EntityType.PHANTOM, EntityType.ZOMBIE, EntityType.WITHER_SKELETON),
        Arrays.asList(
            new LootEntry(Material.IRON_INGOT, 4, 12),
            new LootEntry(Material.EMERALD, 1, 3),
            new LootEntry(Material.PACKED_ICE, 4, 16),
            new LootEntry(Material.SPRUCE_LOG, 4, 12),
            new LootEntry(Material.COAL, 4, 12),
            new LootEntry(Material.LEATHER_CHESTPLATE, 1, 1)
        )
    ),
    ENFER(2000, "Enfer", "&c",
        Arrays.asList(
            Material.NETHERRACK, Material.NETHER_BRICKS, Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.GLOWSTONE, Material.MAGMA_BLOCK, Material.NETHER_QUARTZ_ORE, Material.CRIMSON_STEM,
            Material.WARPED_STEM, Material.BASALT, Material.BLACKSTONE, Material.ANCIENT_DEBRIS,
            Material.NETHERRACK, Material.NETHER_QUARTZ_ORE, Material.GLOWSTONE, Material.ANCIENT_DEBRIS
        ),
        Arrays.asList(EntityType.BLAZE, EntityType.ZOMBIFIED_PIGLIN, EntityType.MAGMA_CUBE,
            EntityType.WITHER_SKELETON, EntityType.HOGLIN, EntityType.PIGLIN),
        Arrays.asList(
            new LootEntry(Material.QUARTZ, 4, 16),
            new LootEntry(Material.GOLD_INGOT, 4, 12),
            new LootEntry(Material.BLAZE_ROD, 1, 4),
            new LootEntry(Material.GLOWSTONE_DUST, 4, 16),
            new LootEntry(Material.NETHERITE_SCRAP, 1, 2),
            new LootEntry(Material.NETHER_WART, 4, 12)
        )
    ),
    END(5000, "End", "&5",
        Arrays.asList(
            Material.END_STONE, Material.PURPUR_BLOCK, Material.PURPUR_PILLAR, Material.END_STONE_BRICKS,
            Material.CHORUS_PLANT, Material.OBSIDIAN, Material.DRAGON_EGG, Material.SHULKER_BOX,
            Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.GOLD_BLOCK, Material.IRON_BLOCK,
            Material.END_STONE, Material.PURPUR_BLOCK, Material.DIAMOND_ORE, Material.OBSIDIAN
        ),
        Arrays.asList(EntityType.ENDERMAN, EntityType.SHULKER, EntityType.PHANTOM,
            EntityType.WITHER_SKELETON, EntityType.BLAZE),
        Arrays.asList(
            new LootEntry(Material.DIAMOND, 1, 4),
            new LootEntry(Material.EMERALD, 4, 12),
            new LootEntry(Material.OBSIDIAN, 4, 16),
            new LootEntry(Material.ENDER_PEARL, 2, 6),
            new LootEntry(Material.CHORUS_FRUIT, 4, 12),
            new LootEntry(Material.SHULKER_SHELL, 1, 2)
        )
    );

    public static class LootEntry {
        public final Material material;
        public final int minAmount;
        public final int maxAmount;

        public LootEntry(Material material, int minAmount, int maxAmount) {
            this.material = material;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    private final long blocksRequired;
    private final String displayName;
    private final String color;
    private final List<Material> blocks;
    private final List<EntityType> mobs;
    private final List<LootEntry> chestLoot;
    private static final Random RANDOM = new Random();

    Phase(long blocksRequired, String displayName, String color,
          List<Material> blocks, List<EntityType> mobs, List<LootEntry> chestLoot) {
        this.blocksRequired = blocksRequired;
        this.displayName = displayName;
        this.color = color;
        this.blocks = blocks;
        this.mobs = mobs;
        this.chestLoot = chestLoot;
    }

    public Material getRandomBlock() {
        return blocks.get(RANDOM.nextInt(blocks.size()));
    }

    public EntityType getRandomMob() {
        return mobs.get(RANDOM.nextInt(mobs.size()));
    }

    public LootEntry getRandomLoot() {
        return chestLoot.get(RANDOM.nextInt(chestLoot.size()));
    }

    public static Phase getPhase(long blocksBroken) {
        Phase current = PLAINES;
        for (Phase p : values()) {
            if (blocksBroken >= p.blocksRequired) current = p;
        }
        return current;
    }

    public long getBlocksRequired() { return blocksRequired; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public List<Material> getBlocks() { return blocks; }
    public List<EntityType> getMobs() { return mobs; }
    public List<LootEntry> getChestLoot() { return chestLoot; }
}
