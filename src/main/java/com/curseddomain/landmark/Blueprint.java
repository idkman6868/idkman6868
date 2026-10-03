package com.curseddomain.landmark;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A landmark described as a list of box operations in coordinates relative to the site: x/z from the centre,
 * y from ground level (y = -1 is the ground layer, y = 0 the first block above it). The builder clips every
 * operation to one chunk at a time, so a blueprint can be built piece by piece in any order.
 */
public final class Blueprint {
   final List<Blueprint.Op> ops = new ArrayList<>();
   final List<Blueprint.Marker> markers = new ArrayList<>();
   final long seed;
   final int radius;
   final int clearHeight;
   /** Ground layer (y = -1) laid over the whole footprint before the operations run. */
   Blueprint.Painter ground = (x, y, z) -> Palette.GRASS;
   BlockState foundation = Palette.DIRT;
   /** Chance that a wall block above y = 3 is missing (ruins). */
   float decay;

   Blueprint(long seed, int radius, int clearHeight) {
      this.seed = seed;
      this.radius = radius;
      this.clearHeight = clearHeight;
   }

   @FunctionalInterface
   public interface Painter {
      @Nullable
      BlockState at(int x, int y, int z);
   }

   record Op(int x0, int y0, int z0, int x1, int y1, int z1, Blueprint.Painter painter, boolean decays) {
   }

   /** Something to spawn once the chunk holding it is built. */
   record Marker(int x, int y, int z, String what) {
   }

   // ------------------------------------------------------------------------------------------------ noise

   int hash(int x, int y, int z, int salt) {
      long h = this.seed ^ (long)salt * -7046029254386353131L;
      h ^= (long)x * 3129871L;
      h ^= (long)z * 116129781L;
      h ^= (long)y * 2654435761L;
      h = h * h * 42317861L + h * 11L;
      return (int)(h >>> 16 & 2147483647L);
   }

   /** Deterministic pseudo-random value in [0, 1) for a position. */
   float noise(int x, int y, int z, int salt) {
      return (this.hash(x, y, z, salt) & 65535) / 65536.0F;
   }

   // ------------------------------------------------------------------------------------------------ primitives

   void paint(int x0, int y0, int z0, int x1, int y1, int z1, Blueprint.Painter painter) {
      this.ops.add(new Blueprint.Op(Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1), Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1), painter, false));
   }

   void paintDecaying(int x0, int y0, int z0, int x1, int y1, int z1, Blueprint.Painter painter) {
      this.ops.add(new Blueprint.Op(Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1), Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1), painter, true));
   }

   void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
      this.paint(x0, y0, z0, x1, y1, z1, (x, y, z) -> state);
   }

   void marker(int x, int y, int z, String what) {
      this.markers.add(new Blueprint.Marker(x, y, z, what));
   }

   // ------------------------------------------------------------------------------------------------ shapes

   /** Hollow box: walls, floor and ceiling. */
   void shell(int x0, int y0, int z0, int x1, int y1, int z1, BlockState wall, BlockState floor, BlockState ceiling) {
      int ax = Math.min(x0, x1), bx = Math.max(x0, x1), az = Math.min(z0, z1), bz = Math.max(z0, z1), ay = Math.min(y0, y1), by = Math.max(y0, y1);
      this.paintDecaying(ax, ay, az, bx, by, bz, (x, y, z) -> {
         if (y == ay) {
            return floor;
         } else if (y == by) {
            return ceiling;
         } else {
            return x != ax && x != bx && z != az && z != bz ? null : wall;
         }
      });
   }

   /**
    * A modern building: wall shell with window bands, a floor every four blocks with ceiling lights,
    * a door in the middle of the side facing {@code doorSide} (0 = -z, 1 = +z, 2 = -x, 3 = +x) and a parapet.
    */
   void building(int x0, int z0, int x1, int z1, int height, BlockState wall, BlockState window, BlockState trim, int doorSide) {
      int ax = Math.min(x0, x1), bx = Math.max(x0, x1), az = Math.min(z0, z1), bz = Math.max(z0, z1);
      int top = Math.max(4, height);
      int midX = (ax + bx) / 2;
      int midZ = (az + bz) / 2;
      this.paintDecaying(ax, 0, az, bx, top + 1, bz, (x, y, z) -> {
         boolean edgeX = x == ax || x == bx;
         boolean edgeZ = z == az || z == bz;
         boolean edge = edgeX || edgeZ;
         if (y == top + 1) {
            return edge ? trim : null;
         } else if (y == top) {
            return edge ? trim : wall;
         } else if (!edge) {
            if (y % 4 == 0 && y > 0) {
               return (x + z) % 5 == 0 ? Palette.SEA_LANTERN : wall;
            } else {
               return y == 0 ? Palette.SMOOTH_STONE : null;
            }
         } else if (edgeX && edgeZ) {
            return trim;
         } else {
            boolean door = y > 0 && y <= 2 && (doorSide == 0 && z == az && Math.abs(x - midX) <= 1
               || doorSide == 1 && z == bz && Math.abs(x - midX) <= 1
               || doorSide == 2 && x == ax && Math.abs(z - midZ) <= 1
               || doorSide == 3 && x == bx && Math.abs(z - midZ) <= 1);
            if (door) {
               return Palette.AIR;
            } else if (y % 4 == 0) {
               return trim;
            } else {
               int along = edgeZ ? x : z;
               return y % 4 != 1 && Math.floorMod(along, 3) != 0 ? window : wall;
            }
         }
      });
   }

   /** Stepped Japanese roof over [x0..x1]x[z0..z1] starting at height y, with an overhang. */
   void japaneseRoof(int x0, int z0, int x1, int z1, int y, BlockState tiles, BlockState eave) {
      int ax = Math.min(x0, x1) - 2, bx = Math.max(x0, x1) + 2, az = Math.min(z0, z1) - 2, bz = Math.max(z0, z1) + 2;
      int layers = Math.max(2, Math.min(bx - ax, bz - az) / 2);
      // eaves
      this.paint(ax, y, az, bx, y, bz, (x, yy, z) -> x != ax && x != bx && z != az && z != bz ? tiles : eave);

      for (int k = 1; k < layers; k++) {
         int kx0 = ax + k, kx1 = bx - k, kz0 = az + k, kz1 = bz - k;
         if (kx0 > kx1 || kz0 > kz1) {
            break;
         }

         this.fill(kx0, y + k, kz0, kx1, y + k, kz1, tiles);
      }
   }

   /** Traditional hall: stone base, wooden pillars, plaster walls, open front, tiled roof. Floor top is at y = 2. */
   void hall(int x0, int z0, int x1, int z1, int wallHeight, BlockState pillar, BlockState wall, BlockState floor, BlockState tiles, int doorSide) {
      int ax = Math.min(x0, x1), bx = Math.max(x0, x1), az = Math.min(z0, z1), bz = Math.max(z0, z1);
      this.fill(ax, 0, az, bx, 1, bz, Palette.STONE_BRICKS);
      this.fill(ax, 2, az, bx, 2, bz, floor);
      int midX = (ax + bx) / 2;
      int midZ = (az + bz) / 2;
      int top = 2 + wallHeight;
      this.paintDecaying(ax, 3, az, bx, top, bz, (x, y, z) -> {
         boolean edgeX = x == ax || x == bx;
         boolean edgeZ = z == az || z == bz;
         if (!edgeX && !edgeZ) {
            return y == top ? Palette.DARK_OAK_PLANKS : null;
         } else {
            int along = edgeZ ? x - ax : z - az;
            boolean column = edgeX && edgeZ || along % 4 == 0;
            if (column || y == top) {
               return pillar;
            } else {
               boolean door = y <= 5 && (doorSide == 0 && z == az && Math.abs(x - midX) <= 2
                  || doorSide == 1 && z == bz && Math.abs(x - midX) <= 2
                  || doorSide == 2 && x == ax && Math.abs(z - midZ) <= 2
                  || doorSide == 3 && x == bx && Math.abs(z - midZ) <= 2);
               if (door) {
                  return Palette.AIR;
               } else {
                  return y == 5 ? Palette.SPRUCE_PLANKS : wall;
               }
            }
         }
      });
      this.japaneseRoof(ax, az, bx, bz, top + 1, tiles, Palette.DEEPSLATE_TILE_SLAB);
      // front steps
      switch (doorSide) {
         case 0 -> this.fill(midX - 2, 0, az - 1, midX + 2, 0, az - 1, Palette.STONE_BRICKS);
         case 1 -> this.fill(midX - 2, 0, bz + 1, midX + 2, 0, bz + 1, Palette.STONE_BRICKS);
         case 2 -> this.fill(ax - 1, 0, midZ - 2, ax - 1, 0, midZ + 2, Palette.STONE_BRICKS);
         default -> this.fill(bx + 1, 0, midZ - 2, bx + 1, 0, midZ + 2, Palette.STONE_BRICKS);
      }
   }

   /** Multi-tier pagoda centred on (cx, cz). */
   void pagoda(int cx, int cz, int half, int tiers, BlockState wall, BlockState pillar, BlockState tiles) {
      this.fill(cx - half - 1, 0, cz - half - 1, cx + half + 1, 0, cz + half + 1, Palette.STONE_BRICKS);
      int y = 1;

      for (int t = 0; t < tiers; t++) {
         int h = Math.max(1, half - t);
         int body = t == 0 ? 4 : 3;
         int yy = y;
         this.paint(cx - h, yy, cz - h, cx + h, yy + body - 1, cz + h, (x, by, z) -> {
            boolean edge = Math.abs(x - cx) == h || Math.abs(z - cz) == h;
            if (!edge) {
               return null;
            } else {
               return Math.abs(x - cx) == h && Math.abs(z - cz) == h ? pillar : wall;
            }
         });
         int r = h + 2;
         this.paint(cx - r, yy + body, cz - r, cx + r, yy + body, cz + r, (x, by, z) -> Math.abs(x - cx) == r || Math.abs(z - cz) == r ? Palette.DEEPSLATE_TILE_SLAB : tiles);
         this.fill(cx - r + 1, yy + body + 1, cz - r + 1, cx + r - 1, yy + body + 1, cz + r - 1, tiles);
         y = yy + body + 2;
      }

      this.fill(cx, y, cz, cx, y + 5, cz, Palette.IRON);
   }

   /** A torii gate. If {@code acrossX} the gate spans the x axis (you walk through it along z). */
   void torii(int x, int z, boolean acrossX, int width, int height, BlockState body, BlockState top) {
      int hw = width / 2;
      for (int side = -1; side <= 1; side += 2) {
         int px = acrossX ? x + side * hw : x;
         int pz = acrossX ? z : z + side * hw;
         this.fill(px, 0, pz, px, height - 1, pz, body);
      }

      int overhang = hw + 2;
      if (acrossX) {
         this.fill(x - overhang, height, z, x + overhang, height, z, top);
         this.fill(x - hw, height - 2, z, x + hw, height - 2, z, body);
         this.fill(x, height - 1, z, x, height - 1, z, body);
      } else {
         this.fill(x, height, z - overhang, x, height, z + overhang, top);
         this.fill(x, height - 2, z - hw, x, height - 2, z + hw, body);
         this.fill(x, height - 1, z, x, height - 1, z, body);
      }
   }

   void stoneLantern(int x, int z) {
      this.fill(x, 0, z, x, 1, z, Palette.STONE_BRICKS);
      this.fill(x, 2, z, x, 2, z, Palette.CHISELED_STONE_BRICKS);
      this.fill(x, 3, z, x, 3, z, Palette.LANTERN);
   }

   void streetLamp(int x, int z) {
      this.fill(x, 0, z, x, 3, z, Palette.POLISHED_ANDESITE);
      this.fill(x, 4, z, x, 4, z, Palette.SEA_LANTERN);
   }

   void tree(int x, int z, int height, BlockState log, BlockState leaves) {
      this.fill(x, 0, z, x, height - 1, z, log);
      int top = height;
      this.paint(x - 2, top - 2, z - 2, x + 2, top + 1, z + 2, (px, py, pz) -> {
         int dx = Math.abs(px - x);
         int dz = Math.abs(pz - z);
         int dy = py - top;
         if (px == x && pz == z && py < top) {
            return log;
         } else if (dx + dz + Math.max(0, dy) * 2 > 3) {
            return null;
         } else {
            return this.noise(px, py, pz, 7) < 0.85F ? leaves : null;
         }
      });
   }

   /** Vertical cylinder of radius r. */
   void cylinder(int cx, int cz, int r, int y0, int y1, Blueprint.Painter painter) {
      this.paintDecaying(cx - r, y0, cz - r, cx + r, y1, cz + r, (x, y, z) -> {
         int dx = x - cx;
         int dz = z - cz;
         return dx * dx + dz * dz <= r * r + r ? painter.at(x, y, z) : null;
      });
   }
}
