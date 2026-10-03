package com.curseddomain.client.vfx;

import com.curseddomain.client.render.GlowMesh;
import com.curseddomain.client.render.ModRenderTypes;
import com.curseddomain.vfx.EnergyParticleOptions;
import com.curseddomain.vfx.VfxKind;
import com.curseddomain.vfx.VfxPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@EventBusSubscriber(
   modid = "cursed_domain",
   value = {Dist.CLIENT}
)
public final class VfxManager {
   private static final List<VfxManager.Active> ACTIVE = new ArrayList<>();
   private static final int MAX_ACTIVE = 400;

   private VfxManager() {
   }

   public static void add(VfxPayload fx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         if (fx.kind() == VfxKind.SPARKS) {
            spawnSparks(fx);
         } else {
            if (ACTIVE.size() < 400) {
               ACTIVE.add(new VfxManager.Active(fx, mc.level.getGameTime()));
            }
         }
      }
   }

   private static void spawnSparks(VfxPayload fx) {
      Minecraft mc = Minecraft.getInstance();
      Random random = new Random(fx.seed());
      int count = Math.min(200, fx.duration());

      for (int i = 0; i < count; i++) {
         Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
         double speed = fx.size() * (0.05 + random.nextDouble() * 0.12);
         ClientLevel var10000 = mc.level;
         EnergyParticleOptions var10001 = new EnergyParticleOptions(fx.color(), 0.6F + random.nextFloat() * 0.6F, 0.0F);
         double var10005 = d.x * speed;
         double var10006 = d.y * speed;
         var10000.addParticle(var10001, fx.a().x, fx.a().y, fx.a().z, var10005, var10006, d.z * speed);
      }
   }

   @SubscribeEvent
   public static void onTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         ACTIVE.clear();
      } else {
         long now = mc.level.getGameTime();
         ACTIVE.removeIf(a -> now - a.start > a.fx.duration() || now < a.start);
      }
   }

   @SubscribeEvent
   public static void onRender(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES && !ACTIVE.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            Vec3 cam = event.getCamera().getPosition();
            float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
            long now = mc.level.getGameTime();
            PoseStack stack = new PoseStack();
            Pose pose = stack.last();
            BufferSource buffers = mc.renderBuffers().bufferSource();
            VertexConsumer bodies = buffers.getBuffer(ModRenderTypes.SHADE);

            for (VfxManager.Active active : ACTIVE) {
               if (!isDark(active.fx.kind())) {
                  float t = Mth.clamp(((float)(now - active.start) + partial) / Math.max(1.0F, (float)active.fx.duration()), 0.0F, 1.0F);
                  drawBody(pose, bodies, active.fx, t, cam);
               }
            }

            buffers.endBatch(ModRenderTypes.SHADE);
            VertexConsumer glow = buffers.getBuffer(ModRenderTypes.GLOW);

            for (VfxManager.Active activex : ACTIVE) {
               if (!isDark(activex.fx.kind())) {
                  float t = Mth.clamp(((float)(now - activex.start) + partial) / Math.max(1.0F, (float)activex.fx.duration()), 0.0F, 1.0F);
                  draw(pose, glow, activex.fx, t, cam, (float)now + partial);
               }
            }

            buffers.endBatch(ModRenderTypes.GLOW);
            VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);

            for (VfxManager.Active activexx : ACTIVE) {
               if (isDark(activexx.fx.kind())) {
                  float t = Mth.clamp(((float)(now - activexx.start) + partial) / Math.max(1.0F, (float)activexx.fx.duration()), 0.0F, 1.0F);
                  drawDark(pose, shade, activexx.fx, t, cam, (float)now + partial);
               }
            }

            buffers.endBatch(ModRenderTypes.SHADE);
         }
      }
   }

   private static boolean isDark(VfxKind kind) {
      return kind == VfxKind.DARK_BURST || kind == VfxKind.BLACK_SPARKS;
   }

   private static float cameraFade(Vec3 a, float r) {
      if (r <= 0.01F) {
         return 1.0F;
      } else {
         double d = a.length();
         return Mth.clamp((float)((d - r * 0.25) / (r * 0.9)), 0.0F, 1.0F);
      }
   }

   private static void drawBody(Pose pose, VertexConsumer vc, VfxPayload fx, float t, Vec3 cam) {
      Vec3 a = fx.a().subtract(cam);
      float fade = (1.0F - t) * (fx.kind() != VfxKind.BURST && fx.kind() != VfxKind.IMPLODE ? 1.0F : cameraFade(a, fx.size()));
      int color = fx.color();
      switch (fx.kind()) {
         case BURST:
            GlowMesh.body(pose, vc, a.x, a.y, a.z, fx.size() * easeOut(t) * 0.85F, color, 0.45F * fade * fade);
            break;
         case IMPLODE:
            GlowMesh.body(pose, vc, a.x, a.y, a.z, fx.size() * (1.0F - easeOut(t)), color, 0.25F + 0.25F * t);
            break;
         case BEAM:
            GlowMesh.beam(
               pose,
               vc,
               clearOfCamera(a, fx.b().subtract(cam)),
               fx.b().subtract(cam),
               fx.size() * 0.8F * (1.0F - t * 0.6F),
               GlowMesh.withAlpha(color, 0.55F * fade),
               10
            );
            break;
         case PILLAR:
            GlowMesh.beam(
               pose,
               vc,
               a,
               a.add(0.0, fx.size() * easeOut(Math.min(1.0F, t * 3.0F)), 0.0),
               Math.max(0.2F, fx.size() * 0.1F),
               GlowMesh.withAlpha(color, 0.5F * fade),
               10
            );
            break;
         case RING:
            GlowMesh.ring(pose, vc, a.x, a.y + 0.04, a.z, fx.size() * easeOut(t), 0.5F + fx.size() * 0.12F, GlowMesh.withAlpha(color, 0.5F * fade));
      }
   }

   private static float easeOut(float t) {
      return 1.0F - (1.0F - t) * (1.0F - t);
   }

   static Vec3 clearOfCamera(Vec3 start, Vec3 end) {
      double distance = start.length();
      if (distance >= 1.2) {
         return start;
      } else {
         Vec3 dir = end.subtract(start);
         double length = dir.length();
         return length < 1.0E-4 ? start : start.add(dir.scale(Math.min(length * 0.8, 1.2 - distance) / length));
      }
   }

   private static void draw(Pose pose, VertexConsumer vc, VfxPayload fx, float t, Vec3 cam, float time) {
      Vec3 a = fx.a().subtract(cam);
      if (fx.kind() == VfxKind.BEAM || fx.kind() == VfxKind.BOLT) {
         a = clearOfCamera(a, fx.b().subtract(cam));
      }

      Vec3 b = fx.b();
      int color = fx.color();
      float fade = (1.0F - t) * (fx.kind() != VfxKind.BURST && fx.kind() != VfxKind.IMPLODE ? 1.0F : cameraFade(a, fx.size()));
      switch (fx.kind()) {
         case BURST: {
            float r = fx.size() * easeOut(t);
            GlowMesh.sphere(pose, vc, a.x, a.y, a.z, r * 0.6F, GlowMesh.withAlpha(-1, fade * 0.5F), 14);
            GlowMesh.sphere(pose, vc, a.x, a.y, a.z, r, GlowMesh.withAlpha(color, fade * 0.45F), 18);
            GlowMesh.ring(pose, vc, a.x, a.y, a.z, r * 1.3F, r * 0.25F, GlowMesh.withAlpha(color, fade * 0.8F));
            break;
         }
         case IMPLODE: {
            float r = fx.size() * (1.0F - easeOut(t));
            GlowMesh.sphere(pose, vc, a.x, a.y, a.z, r, GlowMesh.withAlpha(color, (0.15F + t * 0.45F) * cameraFade(a, fx.size())), 18);
            GlowMesh.sphere(pose, vc, a.x, a.y, a.z, Math.max(0.1F, fx.size() * 0.15F), GlowMesh.withAlpha(-1, t * cameraFade(a, fx.size())), 10);
            break;
         }
         case BEAM: {
            Vec3 end = b.subtract(cam);
            GlowMesh.energyBeam(pose, vc, a, end, fx.size() * (1.0F - t * 0.6F), color, fade);
            break;
         }
         case PILLAR: {
            float r = Math.max(0.2F, fx.size() * 0.12F) * (1.0F - t * 0.5F);
            Vec3 top = a.add(0.0, fx.size() * easeOut(Math.min(1.0F, t * 3.0F)), 0.0);
            GlowMesh.energyBeam(pose, vc, a, top, r, color, fade);
            GlowMesh.ring(pose, vc, a.x, a.y + 0.05, a.z, r * 3.0F * easeOut(t), r * 1.5F, GlowMesh.withAlpha(color, fade));
            break;
         }
         case RING:
            GlowMesh.ring(pose, vc, a.x, a.y + 0.05, a.z, fx.size() * easeOut(t), 0.6F + fx.size() * 0.15F, GlowMesh.withAlpha(color, fade));
            break;
         case SLASH:
            slash(pose, vc, a, b, fx.size(), color, t, fx.seed(), 0.0F);
            break;
         case CROSS_SLASH:
            slash(pose, vc, a, b, fx.size(), color, t, fx.seed(), (float) (Math.PI / 4));
            slash(pose, vc, a, b, fx.size(), color, Mth.clamp(t * 1.3F - 0.15F, 0.0F, 1.0F), fx.seed() + 7, (float) (-Math.PI / 4));
            break;
         case BOLT:
            long seed = fx.seed() + (long)(time / 2.0F);
            GlowMesh.bolt(pose, vc, a, b.subtract(cam), fx.size(), GlowMesh.withAlpha(color, fade), seed);
            GlowMesh.bolt(pose, vc, a, b.subtract(cam), fx.size() * 0.35F, GlowMesh.withAlpha(-1, fade), seed);
            break;
         case SOUND_WAVE: {
            Vec3 dir = b.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : b.normalize();
            Vec3 u = GlowMesh.perpendicular(dir);
            Vec3 w = dir.cross(u).normalize();

            for (int i = 0; i < 3; i++) {
               float k = Mth.clamp(t * 1.4F - i * 0.15F, 0.0F, 1.0F);
               if (!(k <= 0.0F)) {
                  Vec3 c = a.add(dir.scale(fx.size() * k));
                  GlowMesh.ring(pose, vc, c, u, w, 0.4F + k * fx.size() * 0.35F, 0.25F, GlowMesh.withAlpha(color, (1.0F - k) * 0.9F));
               }
            }
            break;
         }
         case SWAP: {
            Vec3 end = b.subtract(cam);
            float r = 1.2F * easeOut(t);
            GlowMesh.sphere(pose, vc, a.x, a.y, a.z, r, GlowMesh.withAlpha(color, fade * 0.6F), 12);
            GlowMesh.sphere(pose, vc, end.x, end.y, end.z, r, GlowMesh.withAlpha(color, fade * 0.6F), 12);
            GlowMesh.beam(pose, vc, a, end, 0.05F, GlowMesh.withAlpha(color, fade * 0.5F), 6);
            break;
         }
         case HAND_SIGN:
            float spin = time * 0.15F;

            for (int i = 0; i < 6; i++) {
               float ang = spin + i * (float) (Math.PI * 2) / 6.0F;
               Vec3 p = a.add(Mth.cos(ang) * fx.size(), 0.2 * Mth.sin(time * 0.2F + i), Mth.sin(ang) * fx.size());
               GlowMesh.sphere(pose, vc, p.x, p.y, p.z, 0.12F, GlowMesh.withAlpha(color, 0.8F * fade + 0.2F), 8);
            }

            GlowMesh.ring(pose, vc, a.x, a.y - 0.9, a.z, fx.size() * 1.2F, 0.25F, GlowMesh.withAlpha(color, 0.7F));
            break;
         case RATIO_LINE: {
            Vec3 dir = b.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : b.normalize();
            Vec3 u = GlowMesh.perpendicular(dir);
            Vec3 diag = u.add(new Vec3(0.0, 1.0, 0.0)).normalize().scale(fx.size());
            GlowMesh.beam(pose, vc, a.subtract(diag.scale(0.7)), a.add(diag.scale(0.3)), 0.04F, GlowMesh.withAlpha(color, fade), 6);
            GlowMesh.sphere(pose, vc, a.x, a.y, a.z, 0.12F, GlowMesh.withAlpha(-1, fade), 8);
         }
      }
   }

   private static void slash(Pose pose, VertexConsumer vc, Vec3 a, Vec3 direction, float radius, int color, float t, int seed, float extraRoll) {
      Vec3 dir = direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : direction.normalize();
      Vec3 side = GlowMesh.perpendicular(dir);
      Vec3 up = dir.cross(side).normalize();
      float roll = (new Random(seed).nextFloat() - 0.5F) * 1.2F + extraRoll;
      Vec3 u = side.scale(Mth.cos(roll)).add(up.scale(Mth.sin(roll)));
      Vec3 center = a.subtract(dir.scale(radius * 0.6));
      float reveal = Mth.clamp(t * 3.5F, 0.0F, 1.0F);
      float fade = t < 0.3F ? 1.0F : 1.0F - (t - 0.3F) / 0.7F;
      GlowMesh.arc(pose, vc, center, u, dir, radius, -1.0995574F, (float) (Math.PI * 7.0 / 10.0), radius * 0.18F, GlowMesh.withAlpha(color, fade), reveal);
      GlowMesh.arc(pose, vc, center, u, dir, radius, -1.0995574F, (float) (Math.PI * 7.0 / 10.0), radius * 0.06F, GlowMesh.withAlpha(-1, fade), reveal);
   }

   private static void drawDark(Pose pose, VertexConsumer vc, VfxPayload fx, float t, Vec3 cam, float time) {
      Vec3 a = fx.a().subtract(cam);
      float fade = 1.0F - t;
      if (fx.kind() == VfxKind.DARK_BURST) {
         fade *= cameraFade(a, fx.size());
         float r = fx.size() * easeOut(t);
         GlowMesh.sphere(pose, vc, a.x, a.y, a.z, r, GlowMesh.withAlpha(-16449016, fade * 0.7F), 20);
         GlowMesh.sphere(pose, vc, a.x, a.y, a.z, r * 0.92F, GlowMesh.withAlpha(fx.color(), fade * 0.4F), 18);
      } else {
         Random random = new Random(fx.seed() + (long)(time / 2.0F));

         for (int i = 0; i < 7; i++) {
            Vec3 d = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.6, random.nextGaussian())
               .normalize()
               .scale(fx.size() * (0.6 + random.nextDouble() * 0.8));
            GlowMesh.bolt(pose, vc, a, a.add(d), 0.12F, GlowMesh.withAlpha(-16777216, fade), random.nextLong());
            GlowMesh.bolt(pose, vc, a, a.add(d), 0.05F, GlowMesh.withAlpha(fx.color(), fade), random.nextLong());
         }
      }
   }

   private record Active(VfxPayload fx, long start) {
   }
}
