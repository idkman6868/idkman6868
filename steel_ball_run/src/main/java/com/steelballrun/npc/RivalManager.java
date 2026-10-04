package com.steelballrun.npc;

import com.steelballrun.config.SbrConfig;
import com.steelballrun.race.Entrant;
import com.steelballrun.race.RaceData;
import com.steelballrun.race.RaceManager;
import com.steelballrun.race.Route;
import com.steelballrun.registry.SbrEntities;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import org.jetbrains.annotations.Nullable;

/**
 * Rival riders. Every rival always has a position on the course, advanced by simulation. When a player comes near,
 * the rival is given a body (a rider on a horse) at that spot and rides for real; when players leave, the body is
 * removed and simulation takes over again. Only a few bodies exist at once.
 */
public final class RivalManager {
   public static final String HORSE_TAG = "steel_ball_run.rival_horse";
   /** Entrant id to the rider entity standing in for them. */
   private static final Map<UUID, UUID> BODIES = new HashMap<>();
   /** Entrant id to {progress, game time} when they last made progress, to catch riders stuck on terrain. */
   private static final Map<UUID, double[]> STUCK = new HashMap<>();

   private RivalManager() {
   }

   /** How much faster than its plain stats a rival is going right now: its stage pace, the config and the night. */
   public static double paceNow(ServerLevel level, Entrant e) {
      double night = level.isNight() ? SbrConfig.RIVAL_NIGHT_FACTOR.get() : 1.0;
      return e.pace * SbrConfig.RIVAL_SPEED.get() * night;
   }

   /** Rivals spread over the width of the road instead of riding in a single file. */
   public static double lane(Entrant e) {
      return ((e.rival % 7) - 3) * 2.0;
   }

   /** A point on the course {@code ahead} blocks in front of a rival, in their lane: {x, z}. */
   public static double[] point(Route route, Entrant e, double ahead) {
      double[] p = route.pointAt(e.progress + ahead);
      double[] dir = route.directionAt(e.progress + ahead);
      double lane = lane(e);
      return new double[]{p[0] - dir[1] * lane, p[1] + dir[0] * lane};
   }

   public static void simulate(ServerLevel level, RaceData d, long now, double seconds) {
      Route route = d.route();
      MinecraftServer server = level.getServer();
      for (Entrant e : d.entrants.values()) {
         if (e.isRival() && e.racing()) {
            Entity body = body(level, e);
            if (body != null) {
               e.progress = Math.max(e.progress - 4.0, route.progressOf(body.getX(), body.getZ()));
            } else {
               e.progress += Rivals.get(e.rival).speed() * paceNow(level, e) * seconds;
            }
            while (e.racing() && e.progress >= route.distanceTo(e.nextGate)) {
               RaceManager.arrive(server, d, e, e.nextGate, now);
            }
         }
      }
      d.setDirty();
   }

   @Nullable
   private static Entity body(ServerLevel level, Entrant e) {
      UUID id = BODIES.get(e.id);
      if (id == null) {
         return null;
      }
      Entity entity = level.getEntity(id);
      if (entity == null || !entity.isAlive()) {
         BODIES.remove(e.id);
         return null;
      }
      return entity;
   }

   /** Gives bodies to rivals players are near and takes them away from the rest. */
   public static void manageBodies(ServerLevel level, RaceData d, long now) {
      int max = SbrConfig.RIVALS_LOADED.get();
      double range = SbrConfig.RIVAL_RANGE.get();
      boolean allowed = d.phase != RaceData.Phase.FINISHED;
      int live = 0;
      for (Iterator<Map.Entry<UUID, UUID>> it = BODIES.entrySet().iterator(); it.hasNext(); ) {
         Map.Entry<UUID, UUID> entry = it.next();
         Entity entity = level.getEntity(entry.getValue());
         if (entity == null || !entity.isAlive()) {
            it.remove();
         } else {
            live++;
         }
      }
      Route route = d.route();
      for (Entrant e : d.entrants.values()) {
         if (!e.isRival()) {
            continue;
         }
         Entity body = body(level, e);
         boolean wanted = allowed && !e.retired && (!e.finished() || d.phase != RaceData.Phase.RUNNING);
         if (body != null) {
            if (!wanted || nearestPlayerSq(level, body.getX(), body.getZ()) > (range + 48.0) * (range + 48.0)) {
               remove(body);
               BODIES.remove(e.id);
               live--;
            } else if (d.phase == RaceData.Phase.RUNNING && stuck(e, now)) {
               e.progress += 20.0;
               remove(body);
               BODIES.remove(e.id);
               live--;
            }
         } else if (wanted && live < max) {
            double[] at = point(route, e, 0.0);
            if (nearestPlayerSq(level, at[0], at[1]) <= range * range && level.isLoaded(BlockPos.containing(at[0], 64.0, at[1]))) {
               if (spawn(level, e, at[0], at[1])) {
                  live++;
               }
            }
         }
      }
   }

   private static boolean stuck(Entrant e, long now) {
      double[] s = STUCK.get(e.id);
      if (s == null || e.progress - s[0] > 6.0) {
         STUCK.put(e.id, new double[]{e.progress, now});
         return false;
      }
      return now - (long)s[1] > 300L;
   }

   private static double nearestPlayerSq(ServerLevel level, double x, double z) {
      double best = Double.MAX_VALUE;
      for (ServerPlayer p : level.players()) {
         if (!p.isSpectator()) {
            double dx = p.getX() - x;
            double dz = p.getZ() - z;
            best = Math.min(best, dx * dx + dz * dz);
         }
      }
      return best;
   }

   private static boolean spawn(ServerLevel level, Entrant e, double x, double z) {
      Rivals.Profile profile = Rivals.get(e.rival);
      RivalRiderEntity rider = SbrEntities.RIVAL_RIDER.get().create(level);
      if (rider == null) {
         return false;
      }
      int y = RaceManager.surface(level, x, z);
      rider.moveTo(x, y, z, -90.0F, 0.0F);
      rider.finalizeSpawn(level, level.getCurrentDifficultyAt(rider.blockPosition()), MobSpawnType.EVENT, null);
      rider.setup(e, profile);
      if (profile.onFoot()) {
         rider.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(profile.speed() * 1.2 / 43.17);
         level.addFreshEntity(rider);
      } else {
         Horse horse = (Horse)EntityType.HORSE.create(level);
         if (horse == null) {
            return false;
         }
         horse.moveTo(x, y, z, -90.0F, 0.0F);
         horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), MobSpawnType.EVENT, null);
         horse.setTamed(true);
         horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(profile.speed() * 1.25 / 43.17);
         horse.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(48.0);
         horse.addTag(HORSE_TAG);
         if (!profile.horse().isEmpty()) {
            horse.setCustomName(Component.literal(profile.horse()).withStyle(ChatFormatting.GRAY));
         }
         level.addFreshEntity(horse);
         level.addFreshEntity(rider);
         rider.startRiding(horse, true);
      }
      BODIES.put(e.id, rider.getUUID());
      STUCK.put(e.id, new double[]{e.progress, level.getGameTime()});
      return true;
   }

   private static void remove(Entity body) {
      Entity vehicle = body.getVehicle();
      if (vehicle != null) {
         vehicle.discard();
      }
      body.discard();
   }

   /** Removes every rival body, e.g. when the race is reset. */
   public static void clearBodies(ServerLevel level) {
      for (UUID id : BODIES.values()) {
         Entity entity = level.getEntity(id);
         if (entity != null) {
            remove(entity);
         }
      }
      BODIES.clear();
      STUCK.clear();
   }

   public static void clearSession() {
      BODIES.clear();
      STUCK.clear();
   }

   /** A rider was killed: they are out of the race. */
   static void onKilled(MinecraftServer server, UUID entrant) {
      RaceData d = RaceData.get(server);
      Entrant e = d.entrant(entrant);
      BODIES.remove(entrant);
      if (e != null && !e.retired && !e.finished()) {
         e.retired = true;
         d.setDirty();
         RaceManager.broadcast(server, Component.translatable("message.steel_ball_run.rival_retired", e.name).withStyle(ChatFormatting.GRAY));
      }
   }
}
