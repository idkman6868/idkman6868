package com.steelballrun.race;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds the course into the world as players come near: a gate at every checkpoint, and at the start line a
 * grandstand and the Race Office where Stephen Steel takes registrations. The course runs east, so gates span
 * north-south.
 */
public final class GateBuilder {
   private static final int NEAR = 96;
   private static final int FLAGS = 2;

   private GateBuilder() {
   }

   static void tick(ServerLevel level, RaceData d) {
      Route route = d.route();
      for (int g = 0; g < route.gates(); g++) {
         if ((d.builtGates & (1 << g)) == 0) {
            double x = route.gateX(g);
            double z = route.gateZ(g);
            if (playerNear(level, x, z) && loaded(level, x, z, g == 0 ? 20 : 8)) {
               build(level, d, g, (int)Math.floor(x), (int)Math.floor(z));
               d.builtGates |= 1 << g;
               d.setDirty();
            }
         }
      }
   }

   private static boolean playerNear(ServerLevel level, double x, double z) {
      for (ServerPlayer p : level.players()) {
         double dx = p.getX() - x;
         double dz = p.getZ() - z;
         if (dx * dx + dz * dz < NEAR * NEAR) {
            return true;
         }
      }
      return false;
   }

   private static boolean loaded(ServerLevel level, double x, double z, int r) {
      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            if (!level.isLoaded(BlockPos.containing(x + sx * r, 64.0, z + sz * r))) {
               return false;
            }
         }
      }
      return true;
   }

   /** Rebuilds a gate now (used by the operator command). */
   public static void rebuild(ServerLevel level, RaceData d, int gate) {
      Route route = d.route();
      build(level, d, gate, (int)Math.floor(route.gateX(gate)), (int)Math.floor(route.gateZ(gate)));
      d.builtGates |= 1 << gate;
      d.setDirty();
   }

   private static void build(ServerLevel level, RaceData d, int gate, int cx, int cz) {
      int y = RaceManager.surface(level, cx + 0.5, cz + 0.5);
      // flatten the track under the gate
      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -6; dz <= 6; dz++) {
            column(level, cx + dx, y, cz + dz, Blocks.COARSE_DIRT, 8);
         }
      }
      boolean finish = gate == Stage.GATES - 1;
      if (gate == 0 || finish) {
         for (int dz = -4; dz <= 4; dz++) {
            Block line = !finish ? Blocks.WHITE_CONCRETE : ((dz & 1) == 0 ? Blocks.WHITE_CONCRETE : Blocks.BLACK_CONCRETE);
            set(level, cx, y - 1, cz + dz, line);
         }
      }
      for (int side = -5; side <= 5; side += 10) {
         for (int h = 0; h < 6; h++) {
            set(level, cx, y + h, cz + side, Blocks.SPRUCE_LOG);
         }
         set(level, cx, y + 7, cz + side, Blocks.LANTERN);
      }
      Block[] bunting = {Blocks.RED_WOOL, Blocks.WHITE_WOOL, Blocks.BLUE_WOOL};
      for (int dz = -5; dz <= 5; dz++) {
         set(level, cx, y + 6, cz + dz, Blocks.SPRUCE_PLANKS);
         if (dz > -5 && dz < 5) {
            set(level, cx, y + 5, cz + dz, bunting[Math.floorMod(dz, 3)]);
         }
      }
      if (gate == 0) {
         buildGrandstand(level, cx, y, cz);
         buildOffice(level, cx, y, cz);
         RaceManager.ensureSteel(level, d, cx + 0.5, y, cz - 7.5);
      }
   }

   private static void buildGrandstand(ServerLevel level, int cx, int y, int cz) {
      for (int dx = -9; dx <= 9; dx++) {
         for (int row = 0; row < 4; row++) {
            int z = cz + 9 + row;
            column(level, cx + dx, y, z, Blocks.COARSE_DIRT, 6);
            for (int h = 0; h <= row; h++) {
               set(level, cx + dx, y + h, z, Blocks.SPRUCE_PLANKS);
            }
            for (int h = row + 1; h <= row + 3; h++) {
               set(level, cx + dx, y + h, z, Blocks.AIR);
            }
         }
      }
      for (int dx = -9; dx <= 9; dx += 6) {
         for (int h = 4; h <= 6; h++) {
            set(level, cx + dx, y + h, cz + 12, Blocks.SPRUCE_LOG);
         }
         set(level, cx + dx, y + 7, cz + 12, dx % 12 == 0 ? Blocks.RED_WOOL : Blocks.BLUE_WOOL);
      }
   }

   private static void buildOffice(ServerLevel level, int cx, int y, int cz) {
      int x0 = cx - 4;
      int x1 = cx + 4;
      int z0 = cz - 15;
      int z1 = cz - 10;
      for (int x = x0 - 1; x <= x1 + 1; x++) {
         for (int z = z0 - 1; z <= z1 + 3; z++) {
            column(level, x, y, z, Blocks.COARSE_DIRT, 8);
         }
      }
      for (int x = x0; x <= x1; x++) {
         for (int z = z0; z <= z1; z++) {
            set(level, x, y - 1, z, Blocks.SPRUCE_PLANKS);
            boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
            for (int h = 0; h < 4; h++) {
               set(level, x, y + h, z, wall ? Blocks.OAK_PLANKS : Blocks.AIR);
            }
            set(level, x, y + 4, z, Blocks.SPRUCE_PLANKS);
         }
      }
      // door facing the track, windows east and west
      set(level, cx, y, z1, Blocks.AIR);
      set(level, cx, y + 1, z1, Blocks.AIR);
      for (int h = 1; h <= 2; h++) {
         set(level, x0, y + h, cz - 13, Blocks.GLASS);
         set(level, x1, y + h, cz - 13, Blocks.GLASS);
      }
      set(level, x0 + 1, y, z0 + 1, Blocks.LANTERN);
      set(level, x1 - 1, y, z0 + 1, Blocks.HAY_BLOCK);
      set(level, x1 + 1, y, z1 + 1, Blocks.HAY_BLOCK);
      set(level, x1 + 1, y + 1, z1 + 1, Blocks.HAY_BLOCK);
      set(level, x0 - 1, y, z1 + 1, Blocks.HAY_BLOCK);
   }

   /** Clears the air above a column and fills gaps below it, so buildings sit flat on uneven ground. */
   private static void column(ServerLevel level, int x, int y, int z, Block ground, int depth) {
      for (int h = 0; h < 9; h++) {
         set(level, x, y + h, z, Blocks.AIR);
      }
      set(level, x, y - 1, z, ground);
      for (int h = 2; h <= depth; h++) {
         BlockPos p = new BlockPos(x, y - h, z);
         BlockState s = level.getBlockState(p);
         if (!s.isAir() && s.getFluidState().isEmpty()) {
            break;
         }
         level.setBlock(p, Blocks.DIRT.defaultBlockState(), FLAGS);
      }
   }

   private static void set(ServerLevel level, int x, int y, int z, Block block) {
      level.setBlock(new BlockPos(x, y, z), block.defaultBlockState(), FLAGS);
   }
}
