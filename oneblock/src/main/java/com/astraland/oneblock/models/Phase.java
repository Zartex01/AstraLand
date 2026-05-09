package com.astraland.oneblock.models;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum Phase {

    PLAINES(0, "Plaines", "&a", Arrays.asList(
        Material.GRASS_BLOCK, Material.DIRT, Material.OAK_LOG, Material.OAK_LEAVES,
        Material.STONE, Material.GRAVEL, Material.SAND, Material.OAK_SAPLING,
        Material.WHEAT_SEEDS, Material.POPPY, Material.DANDELION, Material.COBBLESTONE
    )),
    FORET(100, "Forêt", "&2", Arrays.asList(
        Material.OAK_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.OAK_LEAVES,
        Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES, Material.MUSHROOM_STEM, Material.MOSSY_COBBLESTONE,
        Material.PODZOL, Material.COARSE_DIRT, Material.FERN, Material.BAMBOO
    )),
    DESERT(500, "Désert", "&e", Arrays.asList(
        Material.SAND, Material.SANDSTONE, Material.RED_SAND, Material.CACTUS,
        Material.DEAD_BUSH, Material.CHISELED_SANDSTONE, Material.GOLD_ORE, Material.IRON_ORE,
        Material.CLAY, Material.TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.BONE_BLOCK
    )),
    NEIGE(1000, "Neige", "&b", Arrays.asList(
        Material.SNOW_BLOCK, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE,
        Material.SPRUCE_LOG, Material.SPRUCE_LEAVES, Material.POWDER_SNOW,
        Material.COBBLESTONE, Material.IRON_ORE, Material.COAL_ORE, Material.EMERALD_ORE
    )),
    ENFER(2000, "Enfer", "&c", Arrays.asList(
        Material.NETHERRACK, Material.NETHER_BRICKS, Material.SOUL_SAND, Material.SOUL_SOIL,
        Material.GLOWSTONE, Material.MAGMA_BLOCK, Material.NETHER_QUARTZ_ORE, Material.CRIMSON_STEM,
        Material.WARPED_STEM, Material.BASALT, Material.BLACKSTONE, Material.ANCIENT_DEBRIS
    )),
    END(5000, "End", "&5", Arrays.asList(
        Material.END_STONE, Material.PURPUR_BLOCK, Material.PURPUR_PILLAR, Material.END_STONE_BRICKS,
        Material.CHORUS_PLANT, Material.OBSIDIAN, Material.DRAGON_EGG, Material.SHULKER_BOX,
        Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.GOLD_BLOCK, Material.IRON_BLOCK
    ));

    private final long blocksRequired;
    private final String displayName;
    private final String color;
    private final List<Material> blocks;
    private static final Random RANDOM = new Random();

    Phase(long blocksRequired, String displayName, String color, List<Material> blocks) {
        this.blocksRequired = blocksRequired;
        this.displayName = displayName;
        this.color = color;
        this.blocks = blocks;
    }

    public Material getRandomBlock() {
        return blocks.get(RANDOM.nextInt(blocks.size()));
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
}
