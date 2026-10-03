package com.curseddomain.vfx;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Vfx {
   public static final double RANGE = 96.0;

   private Vfx() {
   }

   public static void send(ServerLevel level, VfxKind kind, Vec3 a, Vec3 b, int color, float size, int duration) {
      PacketDistributor.sendToPlayersNear(
         level, null, a.x, a.y, a.z, 96.0, new VfxPayload(kind, a, b, color, size, duration, level.random.nextInt()), new CustomPacketPayload[0]
      );
   }

   public static void burst(ServerLevel level, Vec3 at, int color, float radius, int duration) {
      send(level, VfxKind.BURST, at, at, color, radius, duration);
   }

   public static void darkBurst(ServerLevel level, Vec3 at, int color, float radius, int duration) {
      send(level, VfxKind.DARK_BURST, at, at, color, radius, duration);
   }

   public static void implode(ServerLevel level, Vec3 at, int color, float radius, int duration) {
      send(level, VfxKind.IMPLODE, at, at, color, radius, duration);
   }

   public static void ring(ServerLevel level, Vec3 at, int color, float radius, int duration) {
      send(level, VfxKind.RING, at, at, color, radius, duration);
   }

   public static void beam(ServerLevel level, Vec3 from, Vec3 to, int color, float width, int duration) {
      send(level, VfxKind.BEAM, from, to, color, width, duration);
   }

   public static void slash(ServerLevel level, Vec3 at, Vec3 direction, int color, float radius, int duration) {
      send(level, VfxKind.SLASH, at, direction, color, radius, duration);
   }

   public static void crossSlash(ServerLevel level, Vec3 at, Vec3 direction, int color, float radius, int duration) {
      send(level, VfxKind.CROSS_SLASH, at, direction, color, radius, duration);
   }

   public static void pillar(ServerLevel level, Vec3 at, int color, float height, int duration) {
      send(level, VfxKind.PILLAR, at, at, color, height, duration);
   }

   public static void bolt(ServerLevel level, Vec3 from, Vec3 to, int color, float width, int duration) {
      send(level, VfxKind.BOLT, from, to, color, width, duration);
   }

   public static void sparks(ServerLevel level, Vec3 at, int color, float radius, int count) {
      send(level, VfxKind.SPARKS, at, at, color, radius, count);
   }

   public static void particles(ServerLevel level, Vec3 at, int color, float size, int count, double spread, double speed) {
      level.sendParticles(new EnergyParticleOptions(color, size, 0.0F), at.x, at.y, at.z, count, spread, spread, spread, speed);
   }

   public static void screen(ServerPlayer player, ScreenFxPayload.Kind kind, int color, float intensity, int duration) {
      PacketDistributor.sendToPlayer(player, new ScreenFxPayload(kind, color, intensity, duration), new CustomPacketPayload[0]);
   }

   public static void shake(ServerLevel level, Vec3 at, float intensity, double radius, int duration) {
      for (ServerPlayer player : level.players()) {
         double d = Math.sqrt(player.distanceToSqr(at));
         if (d < radius) {
            screen(player, ScreenFxPayload.Kind.SHAKE, 0, (float)(intensity * (1.0 - d / radius)), duration);
         }
      }
   }
}
