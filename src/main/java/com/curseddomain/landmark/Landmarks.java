package com.curseddomain.landmark;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.Colony;
import com.curseddomain.cullinggame.CullingConfig;
import com.curseddomain.cullinggame.CullingGameData;
import com.curseddomain.cullinggame.npc.NpcProfile;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.shibuya.ShibuyaIncident;
import com.curseddomain.world.SchoolLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import org.jetbrains.annotations.Nullable;

/**
 * Places the mod's landmarks (Jujutsu High in Tokyo and Kyoto, Shibuya, Tokyo, Kyoto, cities, Kenjaku's hideout) and
 * builds them chunk by chunk as players come near. No world generation is involved, so this works in existing worlds.
 */
@EventBusSubscriber(modid = ModMain.MODID)
public final class Landmarks {
   /** Bump when new default landmarks are added, so old worlds get them too. */
   private static final int DEFAULTS_VERSION = 1;
   private static final int BUILD_RANGE = 176;
   private static final Map<String, Blueprint> BLUEPRINTS = new HashMap<>();

   private Landmarks() {
   }

   public static AtlasData data(MinecraftServer server) {
      return AtlasData.get(server);
   }

   // ------------------------------------------------------------------------------------------------ placement

   private static void placeDefaults(MinecraftServer server, AtlasData data) {
      long seed = server.overworld().getSeed();
      RandomSource random = RandomSource.create(seed ^ 6022019L);
      BlockPos school = SchoolLocation.get().orElse(SchoolLocation.compute(seed));
      add(data, new Site("tokyo_high", LandmarkType.TOKYO_HIGH, school.getX(), school.getZ(), LandmarkType.TOKYO_HIGH.radius(), seed ^ 11L, null));
      BlockPos shibuya = ShibuyaIncident.defaultCenter(server);
      Site shibuyaSite = nudge(data, "shibuya", LandmarkType.SHIBUYA, shibuya.getX(), shibuya.getZ(), 0, 0, null, random, seed ^ 12L);
      double a = random.nextDouble() * Math.PI * 2.0;
      nudge(data, "tokyo", LandmarkType.TOKYO, shibuyaSite.x + (int)(Math.cos(a) * 240.0), shibuyaSite.z + (int)(Math.sin(a) * 240.0), shibuyaSite.x, shibuyaSite.z, null, random, seed ^ 13L);
      a = random.nextDouble() * Math.PI * 2.0;
      double d = 1400.0 + random.nextDouble() * 600.0;
      Site kyoto = nudge(data, "kyoto", LandmarkType.KYOTO, (int)(Math.cos(a) * d), (int)(Math.sin(a) * d), 0, 0, null, random, seed ^ 14L);
      a = random.nextDouble() * Math.PI * 2.0;
      nudge(data, "kyoto_high", LandmarkType.KYOTO_HIGH, kyoto.x + (int)(Math.cos(a) * 200.0), kyoto.z + (int)(Math.sin(a) * 200.0), kyoto.x, kyoto.z, null, random, seed ^ 15L);
      a = random.nextDouble() * Math.PI * 2.0;
      d = 500.0 + random.nextDouble() * 400.0;
      nudge(data, "kenjaku_hideout", LandmarkType.KENJAKU_HIDEOUT, (int)(Math.cos(a) * d), (int)(Math.sin(a) * d), 0, 0, null, random, seed ^ 16L);

      for (String city : new String[]{"sendai", "osaka", "yokohama"}) {
         a = random.nextDouble() * Math.PI * 2.0;
         d = 900.0 + random.nextDouble() * 1700.0;
         nudge(data, city, LandmarkType.CITY, (int)(Math.cos(a) * d), (int)(Math.sin(a) * d), 0, 0, "landmark.cursed_domain.name." + city, random, seed ^ city.hashCode());
      }
   }

   /** Adds a site, rotating it around (ox, oz) until it no longer overlaps another landmark. */
   private static Site nudge(AtlasData data, String id, LandmarkType type, int x, int z, int ox, int oz, @Nullable String name, RandomSource random, long seed) {
      double dx = x - ox;
      double dz = z - oz;
      double dist = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
      double angle = Math.atan2(dz, dx);
      int px = x;
      int pz = z;

      for (int tries = 0; tries < 24 && overlapsAny(data, px, pz, type.radius()); tries++) {
         angle += 0.55;
         if (tries % 8 == 7) {
            dist += type.radius() * 2;
         }

         px = ox + (int)(Math.cos(angle) * dist);
         pz = oz + (int)(Math.sin(angle) * dist);
      }

      Site site = new Site(id, type, px, pz, type.radius(), seed, name);
      add(data, site);
      return site;
   }

   private static boolean overlapsAny(AtlasData data, int x, int z, int radius) {
      for (Site site : data.sites.values()) {
         if (site.overlaps(x, z, radius, 24)) {
            return true;
         }
      }

      return false;
   }

   private static void add(AtlasData data, Site site) {
      if (!data.sites.containsKey(site.id)) {
         data.sites.put(site.id, site);
         data.setDirty();
         ModMain.LOGGER.info("[landmarks] {} ({}) at {}, {}", site.id, site.type.id(), site.x, site.z);
      }
   }

   /** Called when the Culling Game starts: the named colonies become ruined cities. */
   public static void addColonyCities(MinecraftServer server, CullingGameData game) {
      AtlasData data = data(server);

      for (Colony colony : game.colonies()) {
         if (colony.named()) {
            String id = "colony_" + colony.key();
            int radius = Math.min(LandmarkType.RUINED_CITY.radius(), colony.radius() - 16);
            if (!data.sites.containsKey(id) && !overlapsAny(data, colony.x(), colony.z(), radius)) {
               add(data, new Site(id, LandmarkType.RUINED_CITY, colony.x(), colony.z(), radius, server.overworld().getSeed() ^ id.hashCode(), "colony.cursed_domain." + colony.key()));
            }
         }
      }
   }

   /** Admin: a new landmark at a position. */
   public static Site create(MinecraftServer server, LandmarkType type, int x, int z) {
      AtlasData data = data(server);
      int n = 1;

      while (data.sites.containsKey(type.id() + "_" + n)) {
         n++;
      }

      Site site = new Site(type.id() + "_" + n, type, x, z, type.radius(), server.overworld().getSeed() ^ ((long)x * 31L + z), null);
      add(data, site);
      return site;
   }

   public static boolean remove(MinecraftServer server, String id) {
      AtlasData data = data(server);
      BLUEPRINTS.remove(id);
      boolean removed = data.sites.remove(id) != null;
      data.setDirty();
      return removed;
   }

   @Nullable
   public static Site site(MinecraftServer server, String id) {
      return data(server).sites.get(id);
   }

   // ------------------------------------------------------------------------------------------------ building

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = event.getServer();
      if (CullingConfig.b(CullingConfig.LANDMARKS)) {
         AtlasData data = data(server);
         if (data.defaultsVersion < DEFAULTS_VERSION) {
            placeDefaults(server, data);
            data.defaultsVersion = DEFAULTS_VERSION;
            data.setDirty();
         }

         ServerLevel level = server.overworld();
         List<ServerPlayer> players = level.players();
         if (!players.isEmpty()) {
            int budget = CullingConfig.i(CullingConfig.LANDMARK_CHUNKS_PER_TICK);

            for (Site site : data.sites.values()) {
               if (budget <= 0) {
                  break;
               }

               if (!site.complete() && near(site, players, BUILD_RANGE)) {
                  budget -= buildSome(level, data, site, players, budget);
               }
            }

            if (server.getTickCount() % 100 == 0) {
               for (Site site : data.sites.values()) {
                  watchResident(level, data, site, players);
               }
            }
         }
      }
   }

   private static boolean near(Site site, List<ServerPlayer> players, int range) {
      for (ServerPlayer player : players) {
         if (Math.abs(player.getX() - site.x) < site.radius + range && Math.abs(player.getZ() - site.z) < site.radius + range) {
            return true;
         }
      }

      return false;
   }

   /** Builds up to {@code budget} loaded chunks of the site, nearest to a player first. Returns how many it built. */
   private static int buildSome(ServerLevel level, AtlasData data, Site site, List<ServerPlayer> players, int budget) {
      if (site.baseY == Site.UNSET) {
         site.baseY = groundLevel(level, site);
         data.setDirty();
      }

      Blueprint bp = BLUEPRINTS.computeIfAbsent(site.id, k -> Blueprints.of(site));
      int done = 0;

      while (done < budget) {
         int bestX = 0;
         int bestZ = 0;
         double best = Double.MAX_VALUE;

         for (int cx = site.minChunkX(); cx <= site.maxChunkX(); cx++) {
            for (int cz = site.minChunkZ(); cz <= site.maxChunkZ(); cz++) {
               if (!site.built.contains(Site.chunkKey(cx, cz)) && level.getChunkSource().hasChunk(cx, cz)) {
                  for (ServerPlayer player : players) {
                     double dx = player.getX() - (cx * 16 + 8);
                     double dz = player.getZ() - (cz * 16 + 8);
                     double dist = dx * dx + dz * dz;
                     if (dist < best) {
                        best = dist;
                        bestX = cx;
                        bestZ = cz;
                     }
                  }
               }
            }
         }

         if (best == Double.MAX_VALUE) {
            break;
         }

         buildChunk(level, site, bp, bestX, bestZ);
         site.built.add(Site.chunkKey(bestX, bestZ));
         data.setDirty();
         done++;
      }

      if (site.complete()) {
         BLUEPRINTS.remove(site.id);
         ModMain.LOGGER.info("[landmarks] finished building {}", site.id);
      }

      return Math.max(done, 1);
   }

   /** Ground level of a site: the median surface height of five sample points, kept in a sane range. */
   private static int groundLevel(ServerLevel level, Site site) {
      int h = site.radius / 2;
      int[] samples = new int[]{
         surface(level, site.x, site.z), surface(level, site.x - h, site.z - h), surface(level, site.x + h, site.z - h), surface(level, site.x - h, site.z + h), surface(level, site.x + h, site.z + h)
      };
      Arrays.sort(samples);
      return Math.max(level.getSeaLevel(), Math.min(level.getMaxBuildHeight() - site.type.clearHeight() - 8, samples[2]));
   }

   private static int surface(ServerLevel level, int x, int z) {
      return level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY();
   }

   private static void buildChunk(ServerLevel level, Site site, Blueprint bp, int cx, int cz) {
      int x0 = Math.max(cx * 16, site.x - site.radius);
      int x1 = Math.min(cx * 16 + 15, site.x + site.radius);
      int z0 = Math.max(cz * 16, site.z - site.radius);
      int z1 = Math.min(cz * 16 + 15, site.z + site.radius);
      if (x0 <= x1 && z0 <= z1) {
         int base = site.baseY;
         int minY = level.getMinBuildHeight();
         int maxY = level.getMaxBuildHeight() - 1;
         MutableBlockPos pos = new MutableBlockPos();

         // terrain: foundation, ground layer, cleared air above
         for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
               int rx = x - site.x;
               int rz = z - site.z;

               for (int y = base - 6; y <= base - 2; y++) {
                  if (y >= minY) {
                     pos.set(x, y, z);
                     BlockState here = level.getBlockState(pos);
                     if (here.isAir() || !here.getFluidState().isEmpty()) {
                        set(level, pos, bp.foundation);
                     }
                  }
               }

               BlockState ground = bp.ground.at(rx, -1, rz);
               pos.set(x, base - 1, z);
               set(level, pos, ground == null ? Palette.GRASS : ground);

               for (int y = base; y <= Math.min(maxY, base + bp.clearHeight); y++) {
                  pos.set(x, y, z);
                  if (!level.getBlockState(pos).isAir()) {
                     set(level, pos, Palette.AIR);
                  }
               }
            }
         }

         for (Blueprint.Op op : bp.ops) {
            int ax = Math.max(x0, site.x + op.x0());
            int bx = Math.min(x1, site.x + op.x1());
            int az = Math.max(z0, site.z + op.z0());
            int bz = Math.min(z1, site.z + op.z1());
            if (ax <= bx && az <= bz) {
               for (int ry = op.y0(); ry <= op.y1(); ry++) {
                  int y = base + ry;
                  if (y >= minY && y <= maxY) {
                     for (int x = ax; x <= bx; x++) {
                        for (int z = az; z <= bz; z++) {
                           int rx = x - site.x;
                           int rz = z - site.z;
                           BlockState state = op.painter().at(rx, ry, rz);
                           if (state != null) {
                              if (op.decays() && bp.decay > 0.0F && ry >= 3 && state != Palette.AIR && bp.noise(rx, ry, rz, 99) < bp.decay * Math.min(1.0F, ry / 12.0F + 0.3F)) {
                                 state = bp.noise(rx, ry, rz, 98) < 0.3F ? Palette.CRACKED_STONE_BRICKS : Palette.AIR;
                              }

                              pos.set(x, y, z);
                              set(level, pos, state);
                           }
                        }
                     }
                  }
               }
            }
         }

         for (Blueprint.Marker marker : bp.markers) {
            int mx = site.x + marker.x();
            int mz = site.z + marker.z();
            if (mx >= x0 && mx <= x1 && mz >= z0 && mz <= z1) {
               spawnResident(level, site, marker);
            }
         }
      }
   }

   private static void set(ServerLevel level, MutableBlockPos pos, BlockState state) {
      if (level.getBlockState(pos) != state) {
         level.setBlock(pos, state, 2);
      }
   }

   // ------------------------------------------------------------------------------------------------ residents

   @Nullable
   private static Blueprint.Marker residentMarker(Site site) {
      Blueprint bp = BLUEPRINTS.computeIfAbsent(site.id, k -> Blueprints.of(site));
      return bp.markers.isEmpty() ? null : bp.markers.get(0);
   }

   private static void spawnResident(ServerLevel level, Site site, Blueprint.Marker marker) {
      NpcProfile profile = residentProfile(marker.what());
      Entity existing = site.resident == null ? null : level.getEntity(site.resident);
      if (profile != null && (existing == null || !existing.isAlive())) {
         Vec3 at = new Vec3(site.x + marker.x() + 0.5, site.baseY + marker.y(), site.z + marker.z() + 0.5);
         SorcererNpcEntity npc = SorcererNpcEntity.spawn(level, profile, at, 0, false);
         if (npc != null) {
            npc.setHome(at);
            site.resident = npc.getUUID();
            data(level.getServer()).setDirty();
         }
      }
   }

   @Nullable
   private static NpcProfile residentProfile(String what) {
      return switch (what) {
         case "masamichi_yaga" -> NpcProfile.MASAMICHI_YAGA;
         case "yoshinobu_gakuganji" -> NpcProfile.YOSHINOBU_GAKUGANJI;
         case "kenjaku" -> NpcProfile.KENJAKU_HIDEOUT;
         default -> null;
      };
   }

   /** Brings a resident back if their building is loaded and they are gone. */
   private static void watchResident(ServerLevel level, AtlasData data, Site site, List<ServerPlayer> players) {
      if (site.baseY != Site.UNSET && site.resident != null && near(site, players, 0)) {
         Blueprint.Marker marker = residentMarker(site);
         if (marker != null) {
            int mx = site.x + marker.x();
            int mz = site.z + marker.z();
            if (site.built.contains(Site.chunkKey(mx >> 4, mz >> 4)) && level.getChunkSource().hasChunk(mx >> 4, mz >> 4)) {
               Entity resident = level.getEntity(site.resident);
               if (resident == null || !resident.isAlive()) {
                  spawnResident(level, site, marker);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onStopped(ServerStoppedEvent event) {
      BLUEPRINTS.clear();
   }
}
