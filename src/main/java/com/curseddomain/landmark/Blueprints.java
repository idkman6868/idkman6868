package com.curseddomain.landmark;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

/** The designs of every landmark type. Everything is derived from the site seed, so rebuilding gives the same result. */
final class Blueprints {
   /** Grid cell of the modern cities: 7 blocks of road + 19 of lot. */
   private static final int CELL = 26;
   private static final int ROAD = 7;

   private Blueprints() {
   }

   static Blueprint of(Site site) {
      Blueprint bp = new Blueprint(site.seed, site.radius, site.type.clearHeight());
      switch (site.type) {
         case TOKYO_HIGH -> school(bp, false);
         case KYOTO_HIGH -> school(bp, true);
         case SHIBUYA -> shibuya(bp);
         case TOKYO -> tokyo(bp);
         case KYOTO -> kyoto(bp);
         case CITY -> city(bp, false, List.of());
         case RUINED_CITY -> city(bp, true, List.of());
         case KENJAKU_HIDEOUT -> hideout(bp);
      }

      return bp;
   }

   // ------------------------------------------------------------------------------------------------ schools

   /** Tokyo Prefectural Jujutsu High, or its Kyoto sister school. */
   private static void school(Blueprint bp, boolean kyoto) {
      int r = bp.radius - 8;
      BlockState pillar = kyoto ? Palette.RED_CONCRETE : Palette.DARK_OAK_LOG;
      BlockState wall = Palette.WHITE_TERRACOTTA;
      bp.ground = (x, y, z) -> bp.noise(x, 0, z, 1) < 0.08F ? Palette.MOSS : Palette.GRASS;
      // compound wall with a tiled cap and a gate on the +z side
      bp.paint(-r, 0, -r, r, 3, r, (x, y, z) -> {
         boolean edge = Math.abs(x) == r || Math.abs(z) == r;
         if (!edge || z == r && Math.abs(x) <= 4) {
            return null;
         } else {
            return y == 3 ? Palette.DEEPSLATE_TILE_SLAB : (y == 0 ? Palette.STONE_BRICKS : wall);
         }
      });
      // approach: gravel path, torii, stone lanterns
      bp.fill(-2, -1, -12, 2, -1, bp.radius, Palette.GRAVEL);
      bp.torii(0, r + 4, true, 9, 9, Palette.RED_CONCRETE, Palette.BLACK_CONCRETE);
      bp.torii(0, r - 12, true, 7, 7, Palette.RED_CONCRETE, Palette.BLACK_CONCRETE);
      bp.torii(0, r - 22, true, 7, 7, Palette.RED_CONCRETE, Palette.BLACK_CONCRETE);

      for (int z = -8; z < r; z += 8) {
         bp.stoneLantern(-5, z);
         bp.stoneLantern(5, z);
      }

      // main hall with the principal inside
      bp.fill(-6, -1, -14, 6, -1, -10, Palette.GRAVEL);
      bp.hall(-14, -34, 14, -14, 7, pillar, wall, Palette.DARK_OAK_PLANKS, Palette.DEEPSLATE_TILES, 1);
      bp.marker(0, 3, -26, kyoto ? "yoshinobu_gakuganji" : "masamichi_yaga");
      // dormitory
      bp.building(-38, -6, -22, 12, 8, Palette.SPRUCE_PLANKS, Palette.GLASS, Palette.DARK_OAK_LOG, 3);
      bp.japaneseRoof(-38, -6, -22, 12, 10, Palette.DEEPSLATE_TILES, Palette.DEEPSLATE_TILE_SLAB);
      // training hall
      bp.hall(20, -6, 36, 10, 5, pillar, wall, Palette.OAK_PLANKS, Palette.DEEPSLATE_TILES, 2);
      // training ground
      bp.fill(18, -1, 16, 38, -1, 34, Palette.COARSE_DIRT);
      bp.fill(18, 0, 16, 18, 0, 34, Palette.SPRUCE_LOG);
      // pond
      bp.paint(-38, -2, 20, -24, -1, 32, (x, y, z) -> {
         boolean rim = x == -38 || x == -24 || z == 20 || z == 32;
         return rim ? (y == -1 ? Palette.STONE_BRICKS : null) : Palette.WATER;
      });

      // trees
      int[][] trees = kyoto ? new int[][]{{-16, 20}, {14, 26}, {-30, -26}, {30, -26}, {-10, 36}, {10, 36}} : new int[][]{{-16, 18}, {14, 24}, {-30, -28}, {30, -28}, {-12, 36}, {12, 36}, {0, -40}};

      for (int[] t : trees) {
         bp.tree(t[0], t[1], 5, Palette.CHERRY_LOG, Palette.CHERRY_LEAVES);
      }

      if (kyoto) {
         bp.pagoda(28, -28, 4, 3, Palette.WHITE_TERRACOTTA, Palette.RED_CONCRETE, Palette.DEEPSLATE_TILES);
      } else {
         bp.tree(-40, -40, 7, Palette.SPRUCE_LOG, Palette.SPRUCE_LEAVES);
         bp.tree(40, -40, 7, Palette.SPRUCE_LOG, Palette.SPRUCE_LEAVES);
      }
   }

   // ------------------------------------------------------------------------------------------------ modern city

   /** A rectangle (relative coords) that the city grid leaves empty for a set piece. */
   record Zone(int x0, int z0, int x1, int z1) {
      boolean overlaps(int ax, int az, int bx, int bz) {
         return ax <= this.x1 && bx >= this.x0 && az <= this.z1 && bz >= this.z0;
      }
   }

   private static int cellOffset(int v) {
      return Math.floorMod(v + 3, CELL);
   }

   static void city(Blueprint bp, boolean ruined, List<Zone> zones) {
      int r = bp.radius;
      bp.foundation = Palette.STONE;
      bp.decay = ruined ? 0.22F : 0.0F;
      bp.ground = (x, y, z) -> {
         int ox = cellOffset(x);
         int oz = cellOffset(z);
         boolean roadX = ox < ROAD;
         boolean roadZ = oz < ROAD;
         if (!roadX && !roadZ) {
            return Palette.SMOOTH_STONE;
         } else {
            boolean sidewalk = roadX && (ox == 0 || ox == ROAD - 1) && !roadZ || roadZ && (oz == 0 || oz == ROAD - 1) && !roadX;
            if (sidewalk) {
               return Palette.SMOOTH_STONE;
            } else if (ruined && bp.noise(x, 0, z, 3) < 0.07F) {
               return Palette.COBBLE;
            } else if (roadX && ox == 3 && !roadZ && Math.floorMod(z, 6) < 3 || roadZ && oz == 3 && !roadX && Math.floorMod(x, 6) < 3) {
               return Palette.WHITE_CONCRETE;
            } else {
               return Palette.GRAY_CONCRETE;
            }
         }
      };

      // street lamps on the sidewalks
      for (int x = -r + 2; x <= r - 2; x++) {
         for (int z = -r + 2; z <= r - 2; z++) {
            int ox = cellOffset(x);
            int oz = cellOffset(z);
            if ((ox == 0 && oz == 12 || oz == 0 && ox == 12) && !inZones(zones, x, z, x, z)) {
               if (!ruined || bp.noise(x, 0, z, 9) < 0.5F) {
                  bp.streetLamp(x, z);
               }
            }
         }
      }

      // lots
      int first = -r - CELL;

      for (int cx = first; cx <= r; cx += CELL) {
         for (int cz = first; cz <= r; cz += CELL) {
            int lx0 = cx - Math.floorMod(cx + 3, CELL) + ROAD;
            int lz0 = cz - Math.floorMod(cz + 3, CELL) + ROAD;
            int lx1 = lx0 + CELL - ROAD - 1;
            int lz1 = lz0 + CELL - ROAD - 1;
            if (lx0 >= -r && lz0 >= -r && lx1 <= r && lz1 <= r && !inZones(zones, lx0, lz0, lx1, lz1)) {
               lot(bp, lx0 + 1, lz0 + 1, lx1 - 1, lz1 - 1, ruined);
            }
         }
      }

      if (ruined) {
         // rubble piles in the streets
         for (int i = 0; i < 30; i++) {
            int x = (int)(bp.noise(i, 1, 0, 11) * 2 * r) - r;
            int z = (int)(bp.noise(i, 2, 0, 11) * 2 * r) - r;
            bp.paint(x - 1, 0, z - 1, x + 1, 1, z + 1, (px, py, pz) -> bp.noise(px, py, pz, 12) < 0.6F - py * 0.3F ? Palette.COBBLE : null);
         }
      }
   }

   private static boolean inZones(List<Zone> zones, int ax, int az, int bx, int bz) {
      for (Zone zone : zones) {
         if (zone.overlaps(ax, az, bx, bz)) {
            return true;
         }
      }

      return false;
   }

   /** Fills one city lot with buildings or a small park. */
   private static void lot(Blueprint bp, int x0, int z0, int x1, int z1, boolean ruined) {
      float pick = bp.noise(x0, 0, z0, 21);
      float h = bp.noise(x0, 1, z0, 22);
      if (pick < 0.1F) {
         bp.fill(x0, -1, z0, x1, -1, z1, Palette.GRASS);
         bp.tree((x0 + x1) / 2, (z0 + z1) / 2, 5, Palette.CHERRY_LOG, Palette.CHERRY_LEAVES);
         bp.tree(x0 + 3, z0 + 3, 4, Palette.SPRUCE_LOG, Palette.OAK_LEAVES);
         bp.tree(x1 - 3, z1 - 3, 4, Palette.SPRUCE_LOG, Palette.OAK_LEAVES);
      } else if (pick < 0.35F) {
         // glass tower
         int height = 24 + (int)(h * 28);
         if (ruined) {
            height = (int)(height * (0.4F + 0.5F * bp.noise(x0, 2, z0, 23)));
         }

         BlockState glass = pick < 0.22F ? Palette.TINTED_GLASS : Palette.GLASS;
         bp.building(x0 + 1, z0 + 1, x1 - 1, z1 - 1, height, Palette.LIGHT_GRAY_CONCRETE, glass, Palette.GRAY_CONCRETE, side(bp, x0, z0));
      } else if (pick < 0.6F) {
         // two apartment blocks
         int mid = (x0 + x1) / 2;
         BlockState[] walls = new BlockState[]{Palette.BRICKS, Palette.WHITE_TERRACOTTA, Palette.LIGHT_GRAY_TERRACOTTA, Palette.CYAN_TERRACOTTA};
         int height = 8 + (int)(h * 12);
         if (ruined) {
            height = Math.max(4, height - (int)(bp.noise(x0, 3, z0, 24) * 8));
         }

         bp.building(x0, z0, mid - 1, z1, height, walls[(int)(h * 4) % 4], Palette.GLASS, Palette.STONE_BRICKS, 2);
         bp.building(mid + 1, z0, x1, z1, height + 4, walls[(int)(h * 7) % 4], Palette.GLASS, Palette.STONE_BRICKS, 3);
      } else if (pick < 0.85F) {
         // offices
         int height = 12 + (int)(h * 16);
         if (ruined) {
            height = Math.max(4, height - (int)(bp.noise(x0, 4, z0, 25) * 10));
         }

         bp.building(x0, z0, x1, z1, height, Palette.WHITE_CONCRETE, Palette.GLASS, Palette.LIGHT_GRAY_CONCRETE, side(bp, x0, z0));
      } else {
         // row of colourful shops
         BlockState[] fronts = new BlockState[]{Palette.YELLOW_CONCRETE, Palette.PINK_CONCRETE, Palette.CYAN_CONCRETE, Palette.LIME_CONCRETE, Palette.ORANGE_CONCRETE};
         int w = (x1 - x0 + 1) / 3;

         for (int i = 0; i < 3; i++) {
            int sx0 = x0 + i * w;
            int sx1 = i == 2 ? x1 : sx0 + w - 1;
            bp.building(sx0, z0, sx1, z1, 5 + (int)(bp.noise(sx0, 5, z0, 26) * 4), fronts[(int)(bp.noise(sx0, 6, z0, 27) * 5) % 5], Palette.GLASS, Palette.WHITE_CONCRETE, 0);
         }
      }
   }

   private static int side(Blueprint bp, int x, int z) {
      return (int)(bp.noise(x, 9, z, 28) * 4) % 4;
   }

   // ------------------------------------------------------------------------------------------------ Shibuya

   private static void shibuya(Blueprint bp) {
      Zone crossing = new Zone(-18, -18, 18, 18);
      Zone station = new Zone(-40, -46, 40, -19);
      Zone tower109 = new Zone(19, 19, 38, 38);
      Zone screens = new Zone(-38, 19, -19, 38);
      city(bp, false, List.of(crossing, station, tower109, screens));
      Blueprint.Painter cityGround = bp.ground;
      // the scramble crossing: four zebra crossings and the two diagonals
      bp.ground = (x, y, z) -> {
         if (Math.abs(x) <= 16 && Math.abs(z) <= 16) {
            int ax = Math.abs(x);
            int az = Math.abs(z);
            boolean zebra = ax >= 10 && ax <= 14 && az <= 8 && Math.floorMod(z, 2) == 0 || az >= 10 && az <= 14 && ax <= 8 && Math.floorMod(x, 2) == 0;
            boolean diagonal = Math.abs(ax - az) <= 1 && ax < 10 && Math.floorMod(x + z, 2) == 0;
            if (ax >= 15 || az >= 15) {
               return Palette.SMOOTH_STONE;
            } else {
               return zebra || diagonal ? Palette.WHITE_CONCRETE : Palette.GRAY_CONCRETE;
            }
         } else {
            return cityGround.at(x, y, z);
         }
      };

      // Shibuya Station along the north side
      bp.building(-38, -44, 38, -24, 12, Palette.LIGHT_GRAY_CONCRETE, Palette.GLASS, Palette.WHITE_CONCRETE, 1);
      bp.fill(-38, 9, -24, 38, 9, -24, Palette.LIME_CONCRETE);
      bp.fill(-6, 1, -24, 6, 4, -24, Palette.AIR);
      bp.fill(-38, 5, -22, 38, 5, -20, Palette.SMOOTH_STONE_SLAB);
      // Hachiko, waiting in front of the station
      bp.fill(-12, 0, -19, -10, 0, -18, Palette.STONE_BRICKS);
      bp.fill(-12, 1, -19, -10, 1, -19, Palette.BROWN_TERRACOTTA);
      bp.fill(-12, 2, -19, -12, 2, -19, Palette.BROWN_TERRACOTTA);
      bp.fill(-12, 1, -18, -12, 1, -18, Palette.BROWN_TERRACOTTA);
      bp.fill(-10, 1, -18, -10, 1, -18, Palette.BROWN_TERRACOTTA);
      // the 109 building: a white cylinder with a glass seam
      bp.cylinder(28, 28, 8, 0, 40, (x, y, z) -> {
         int dx = x - 28;
         int dz = z - 28;
         boolean outer = dx * dx + dz * dz >= 7 * 7 - 2;
         if (y == 40) {
            return Palette.QUARTZ;
         } else if (!outer) {
            return y % 4 == 0 ? Palette.QUARTZ : null;
         } else if (Math.abs(dx + dz) <= 1 && dx < 0) {
            return Palette.GLASS;
         } else {
            return y % 4 == 2 ? Palette.GLASS : Palette.QUARTZ;
         }
      });
      bp.fill(26, 36, 20, 30, 38, 20, Palette.SEA_LANTERN);
      // the giant screens of the crossing
      bp.building(-36, 21, -21, 36, 30, Palette.BLACK_CONCRETE, Palette.TINTED_GLASS, Palette.GRAY_CONCRETE, 0);
      bp.fill(-34, 12, 20, -23, 22, 20, Palette.SEA_LANTERN);
      bp.fill(-33, 13, 20, -24, 21, 20, Palette.CYAN_CONCRETE);
   }

   // ------------------------------------------------------------------------------------------------ Tokyo

   private static void tokyo(Blueprint bp) {
      Zone tower = new Zone(-20, -20, 20, 20);
      city(bp, false, List.of(tower));
      bp.fill(-18, -1, -18, 18, -1, 18, Palette.GRASS);
      tokyoTower(bp, 0, 0);
   }

   /** Tokyo Tower: a red-and-white lattice tower with two observation decks. */
   private static void tokyoTower(Blueprint bp, int cx, int cz) {
      int height = 100;
      bp.paint(cx - 12, 0, cz - 12, cx + 12, height, cz + 12, (x, y, z) -> {
         int w = Math.max(1, 11 - y * 10 / 72);
         int dx = Math.abs(x - cx);
         int dz = Math.abs(z - cz);
         if (y > 72) {
            return dx == 0 && dz == 0 ? (y >= height - 2 ? Palette.IRON : ((y / 6) % 2 == 0 ? Palette.ORANGE_CONCRETE : Palette.WHITE_CONCRETE)) : null;
         } else if (dx > w || dz > w) {
            return null;
         } else {
            boolean ring = dx == w || dz == w;
            BlockState band = (y / 8) % 2 == 0 ? Palette.ORANGE_CONCRETE : Palette.WHITE_CONCRETE;
            boolean deck = y == 30 || y == 58;
            boolean deckWall = (y > 30 && y <= 33 || y > 58 && y <= 60) && ring;
            if (deck) {
               return dx == w && dz == w ? band : Palette.SMOOTH_STONE;
            } else if (deckWall) {
               return dx == w && dz == w ? band : Palette.GLASS;
            } else if (!ring) {
               return null;
            } else {
               boolean corner = dx == w && dz == w || dx >= w - 1 && dz >= w - 1;
               boolean brace = y % 6 == 0 || Math.floorMod((dx == w ? z - cz : x - cx) + y, 6) == 0;
               if (corner || brace && y > 4) {
                  return band;
               } else {
                  return null;
               }
            }
         }
      });
      bp.fill(cx - 1, 31, cz - 1, cx + 1, 31, cz + 1, Palette.SEA_LANTERN);
   }

   // ------------------------------------------------------------------------------------------------ Kyoto

   private static void kyoto(Blueprint bp) {
      int r = bp.radius;
      int cell = 20;
      bp.ground = (x, y, z) -> {
         int ox = Math.floorMod(x + 2, cell);
         int oz = Math.floorMod(z + 2, cell);
         if (ox < 4 || oz < 4) {
            return Palette.GRAVEL;
         } else {
            return bp.noise(x, 0, z, 31) < 0.1F ? Palette.MOSS : Palette.GRASS;
         }
      };
      List<Zone> zones = List.of(new Zone(-14, -14, 14, 14), new Zone(-r, 36, -10, 52));

      for (int gx = -r - cell; gx <= r; gx += cell) {
         for (int gz = -r - cell; gz <= r; gz += cell) {
            int x0 = gx - Math.floorMod(gx + 2, cell) + 4;
            int z0 = gz - Math.floorMod(gz + 2, cell) + 4;
            int x1 = x0 + cell - 5;
            int z1 = z0 + cell - 5;
            if (x0 >= -r && z0 >= -r && x1 <= r && z1 <= r && !inZones(zones, x0, z0, x1, z1)) {
               // two machiya town houses per block
               int mid = (z0 + z1) / 2;
               machiya(bp, x0 + 1, z0 + 1, x1 - 1, mid - 1, 0);
               machiya(bp, x0 + 1, mid + 1, x1 - 1, z1 - 1, 1);
            }
         }
      }

      // the five-storey pagoda in the middle
      bp.fill(-13, -1, -13, 13, -1, 13, Palette.GRAVEL);
      bp.pagoda(0, 0, 6, 5, Palette.WHITE_TERRACOTTA, Palette.DARK_OAK_LOG, Palette.DEEPSLATE_TILES);

      for (int[] t : new int[][]{{-11, -11}, {11, -11}, {-11, 11}, {11, 11}}) {
         bp.tree(t[0], t[1], 5, Palette.CHERRY_LOG, Palette.CHERRY_LEAVES);
      }

      // Fushimi Inari: a tunnel of torii gates
      bp.fill(-r + 2, -1, 42, -12, -1, 46, Palette.GRAVEL);

      for (int x = -r + 4; x <= -14; x += 2) {
         bp.torii(x, 44, false, 5, 5, Palette.RED_CONCRETE, Palette.BLACK_CONCRETE);
      }
   }

   /** A small wooden town house with a tiled roof; door faces -z (0) or +z (1). */
   private static void machiya(Blueprint bp, int x0, int z0, int x1, int z1, int door) {
      int height = 5 + (int)(bp.noise(x0, 0, z0, 33) * 3);
      int midX = (x0 + x1) / 2;
      bp.paint(x0, 0, z0, x1, height, z1, (x, y, z) -> {
         boolean edgeX = x == x0 || x == x1;
         boolean edgeZ = z == z0 || z == z1;
         if (!edgeX && !edgeZ) {
            return y == 0 ? Palette.OAK_PLANKS : (y == height ? Palette.SPRUCE_PLANKS : null);
         } else if (edgeX && edgeZ) {
            return Palette.DARK_OAK_LOG;
         } else if (y <= 2 && Math.abs(x - midX) <= 0 && (door == 0 && z == z0 || door == 1 && z == z1)) {
            return y == 0 ? Palette.OAK_PLANKS : Palette.AIR;
         } else if (y <= 2) {
            return (x + z) % 2 == 0 ? Palette.DARK_OAK_PLANKS : Palette.SPRUCE_PLANKS;
         } else if (y == 3) {
            return Palette.DARK_OAK_LOG;
         } else {
            return edgeZ && Math.floorMod(x, 3) == 1 ? Palette.GLASS : Palette.WHITE_TERRACOTTA;
         }
      });
      bp.japaneseRoof(x0, z0, x1, z1, height + 1, Palette.DEEPSLATE_TILES, Palette.DEEPSLATE_TILE_SLAB);
   }

   // ------------------------------------------------------------------------------------------------ Kenjaku's hideout

   private static void hideout(Blueprint bp) {
      bp.foundation = Palette.STONE;
      bp.decay = 0.18F;
      bp.ground = (x, y, z) -> bp.noise(x, 0, z, 41) < 0.4F ? Palette.MOSSY_COBBLE : Palette.COARSE_DIRT;
      bp.fill(-7, 0, -7, 7, 0, 7, Palette.MOSSY_STONE_BRICKS);
      bp.paintDecaying(-7, 1, -7, 7, 7, 7, (x, y, z) -> {
         boolean edge = Math.abs(x) == 7 || Math.abs(z) == 7;
         if (!edge) {
            return null;
         } else if (Math.abs(x) == 7 && Math.abs(z) == 7 || Math.floorMod(x, 4) == 3 && Math.abs(z) == 7 || Math.floorMod(z, 4) == 3 && Math.abs(x) == 7) {
            return Palette.DARK_OAK_LOG;
         } else if (z == 7 && Math.abs(x) <= 1 && y <= 3) {
            return Palette.AIR;
         } else {
            float n = bp.noise(x, y, z, 42);
            return n < 0.3F ? Palette.CRACKED_STONE_BRICKS : (n < 0.6F ? Palette.MOSSY_STONE_BRICKS : Palette.STONE_BRICKS);
         }
      });
      bp.paintDecaying(-9, 8, -9, 9, 8, 9, (x, y, z) -> bp.noise(x, y, z, 43) < 0.7F ? Palette.DEEPSLATE_TILES : null);
      bp.paint(-6, 1, -6, 6, 6, 6, (x, y, z) -> bp.noise(x, y, z, 44) < 0.04F ? Palette.COBWEB : null);
      bp.fill(-1, 1, -5, 1, 1, -5, Palette.POLISHED_BLACKSTONE);
      bp.fill(0, 2, -5, 0, 2, -5, Palette.LANTERN);
      // a broken torii in front
      bp.fill(-3, 0, 12, -3, 5, 12, Palette.RED_CONCRETE);
      bp.fill(3, 0, 12, 3, 2, 12, Palette.RED_CONCRETE);
      bp.fill(-5, 6, 12, 0, 6, 12, Palette.BLACK_CONCRETE);
      bp.marker(0, 1, -2, "kenjaku");
   }

   static List<String> residents() {
      List<String> list = new ArrayList<>();
      list.add("masamichi_yaga");
      list.add("yoshinobu_gakuganji");
      list.add("kenjaku");
      return list;
   }
}
