package com.curseddomain.landmark;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Block states used by the landmark blueprints. */
final class Palette {
   static final BlockState AIR = s(Blocks.AIR);
   static final BlockState STONE = s(Blocks.STONE);
   static final BlockState DIRT = s(Blocks.DIRT);
   static final BlockState GRASS = s(Blocks.GRASS_BLOCK);
   static final BlockState GRAVEL = s(Blocks.GRAVEL);
   static final BlockState SAND = s(Blocks.SAND);
   static final BlockState COARSE_DIRT = s(Blocks.COARSE_DIRT);
   static final BlockState MOSS = s(Blocks.MOSS_BLOCK);
   static final BlockState WATER = s(Blocks.WATER);
   static final BlockState COBBLE = s(Blocks.COBBLESTONE);
   static final BlockState MOSSY_COBBLE = s(Blocks.MOSSY_COBBLESTONE);
   static final BlockState STONE_BRICKS = s(Blocks.STONE_BRICKS);
   static final BlockState MOSSY_STONE_BRICKS = s(Blocks.MOSSY_STONE_BRICKS);
   static final BlockState CRACKED_STONE_BRICKS = s(Blocks.CRACKED_STONE_BRICKS);
   static final BlockState CHISELED_STONE_BRICKS = s(Blocks.CHISELED_STONE_BRICKS);
   static final BlockState SMOOTH_STONE = s(Blocks.SMOOTH_STONE);
   static final BlockState SMOOTH_STONE_SLAB = s(Blocks.SMOOTH_STONE_SLAB);
   static final BlockState POLISHED_ANDESITE = s(Blocks.POLISHED_ANDESITE);
   static final BlockState ANDESITE = s(Blocks.ANDESITE);
   static final BlockState GRAY_CONCRETE = s(Blocks.GRAY_CONCRETE);
   static final BlockState LIGHT_GRAY_CONCRETE = s(Blocks.LIGHT_GRAY_CONCRETE);
   static final BlockState WHITE_CONCRETE = s(Blocks.WHITE_CONCRETE);
   static final BlockState BLACK_CONCRETE = s(Blocks.BLACK_CONCRETE);
   static final BlockState RED_CONCRETE = s(Blocks.RED_CONCRETE);
   static final BlockState ORANGE_CONCRETE = s(Blocks.ORANGE_CONCRETE);
   static final BlockState CYAN_CONCRETE = s(Blocks.CYAN_CONCRETE);
   static final BlockState BLUE_CONCRETE = s(Blocks.BLUE_CONCRETE);
   static final BlockState YELLOW_CONCRETE = s(Blocks.YELLOW_CONCRETE);
   static final BlockState PINK_CONCRETE = s(Blocks.PINK_CONCRETE);
   static final BlockState LIME_CONCRETE = s(Blocks.LIME_CONCRETE);
   static final BlockState WHITE_TERRACOTTA = s(Blocks.WHITE_TERRACOTTA);
   static final BlockState LIGHT_GRAY_TERRACOTTA = s(Blocks.LIGHT_GRAY_TERRACOTTA);
   static final BlockState BROWN_TERRACOTTA = s(Blocks.BROWN_TERRACOTTA);
   static final BlockState TERRACOTTA = s(Blocks.TERRACOTTA);
   static final BlockState CYAN_TERRACOTTA = s(Blocks.CYAN_TERRACOTTA);
   static final BlockState BRICKS = s(Blocks.BRICKS);
   static final BlockState GLASS = s(Blocks.GLASS);
   static final BlockState TINTED_GLASS = s(Blocks.TINTED_GLASS);
   static final BlockState QUARTZ = s(Blocks.SMOOTH_QUARTZ);
   static final BlockState IRON = s(Blocks.IRON_BLOCK);
   static final BlockState DARK_OAK_PLANKS = s(Blocks.DARK_OAK_PLANKS);
   static final BlockState DARK_OAK_LOG = s(Blocks.DARK_OAK_LOG);
   static final BlockState SPRUCE_PLANKS = s(Blocks.SPRUCE_PLANKS);
   static final BlockState SPRUCE_LOG = s(Blocks.SPRUCE_LOG);
   static final BlockState OAK_PLANKS = s(Blocks.OAK_PLANKS);
   static final BlockState CHERRY_LOG = s(Blocks.CHERRY_LOG);
   static final BlockState DEEPSLATE_TILES = s(Blocks.DEEPSLATE_TILES);
   static final BlockState DEEPSLATE_TILE_SLAB = s(Blocks.DEEPSLATE_TILE_SLAB);
   static final BlockState POLISHED_DEEPSLATE = s(Blocks.POLISHED_DEEPSLATE);
   static final BlockState POLISHED_BLACKSTONE = s(Blocks.POLISHED_BLACKSTONE);
   static final BlockState SEA_LANTERN = s(Blocks.SEA_LANTERN);
   static final BlockState GLOWSTONE = s(Blocks.GLOWSTONE);
   static final BlockState LANTERN = s(Blocks.LANTERN);
   static final BlockState COBWEB = s(Blocks.COBWEB);
   static final BlockState HAY = s(Blocks.HAY_BLOCK);
   static final BlockState CHERRY_LEAVES = leaves(Blocks.CHERRY_LEAVES);
   static final BlockState OAK_LEAVES = leaves(Blocks.OAK_LEAVES);
   static final BlockState SPRUCE_LEAVES = leaves(Blocks.SPRUCE_LEAVES);

   private Palette() {
   }

   private static BlockState s(Block block) {
      return block.defaultBlockState();
   }

   /** Leaves placed without a log nearby must be persistent or they decay. */
   private static BlockState leaves(Block block) {
      return (BlockState)block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, Boolean.TRUE);
   }
}
