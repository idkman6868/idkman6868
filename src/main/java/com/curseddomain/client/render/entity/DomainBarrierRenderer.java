package com.curseddomain.client.render.entity;

import com.curseddomain.ModMain;
import com.curseddomain.client.render.GlowMesh;
import com.curseddomain.client.render.ModRenderTypes;
import com.curseddomain.domain.DomainBarrierEntity;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DomainBarrierRenderer extends EntityRenderer<DomainBarrierEntity> {
   private static final Map<DomainStyle, ResourceLocation> TEXTURES = new EnumMap<>(DomainStyle.class);

   public DomainBarrierRenderer(Context context) {
      super(context);
   }

   static ResourceLocation interior(DomainStyle style) {
      return TEXTURES.computeIfAbsent(style, s -> ModMain.id("textures/domain/" + s.textureName() + ".png"));
   }

   public boolean shouldRender(DomainBarrierEntity entity, Frustum frustum, double camX, double camY, double camZ) {
      return true;
   }

   public void render(DomainBarrierEntity entity, float yaw, float partialTick, PoseStack stack, MultiBufferSource buffers, int light) {
      float r = entity.radius();
      DomainStyle style = entity.style();
      float age = entity.tickCount - entity.phaseStartAge + partialTick;
      float time = entity.tickCount + partialTick;
      Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      boolean inside = cam.distanceTo(entity.position()) < r - 0.2;
      Pose pose = stack.last();
      DomainInstance.Phase phase = entity.phase();
      DomainType type = entity.domainType();
      if (type == DomainType.INCOMPLETE) {
         VertexConsumer glow = buffers.getBuffer(ModRenderTypes.GLOW);
         float pulse = 0.5F + 0.5F * Mth.sin(time * 0.15F);
         GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, r, GlowMesh.withAlpha(style.glow(), 0.05F + pulse * 0.04F), 32);
         GlowMesh.ring(pose, glow, 0.0, 0.05, 0.0, r, 0.6F, GlowMesh.withAlpha(style.glow(), 0.5F));
      } else if (type == DomainType.BARRIERLESS) {
         renderBarrierless(entity, pose, stack, buffers, style, r, time, age, phase);
      } else {
         float formed = switch (phase) {
            case CASTING -> 0.0F;
            case FORMING -> Mth.clamp(age / 24.0F, 0.0F, 1.0F);
            case ACTIVE -> 1.0F;
            case COLLAPSING -> 1.0F - Mth.clamp(age / 24.0F, 0.0F, 1.0F);
         };
         if (!(formed <= 0.001F)) {
            if (inside && phase == DomainInstance.Phase.ACTIVE) {
               renderInterior(stack, buffers, style, r, time);
            } else if (inside) {
               VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);
               GlowMesh.sphere(pose, shade, 0.0, 0.0, 0.0, r, GlowMesh.withAlpha(-16645884, formed), 40);
               if (formed > 0.6F) {
                  renderInterior(stack, buffers, style, r, time);
               }
            } else {
               VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);
               GlowMesh.sphere(pose, shade, 0.0, 0.0, 0.0, r * Math.min(1.0F, formed * 1.05F), GlowMesh.withAlpha(-16449016, 0.93F * formed), 40);
               VertexConsumer glow = buffers.getBuffer(ModRenderTypes.GLOW);
               float rim = 0.12F + 0.06F * Mth.sin(time * 0.2F);
               GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, r * formed + 0.05F, GlowMesh.withAlpha(style.shell(), rim * formed), 40);
               GlowMesh.ring(pose, glow, 0.0, 0.05, 0.0, r * formed + 0.3F, 1.2F, GlowMesh.withAlpha(style.glow(), 0.7F * formed));
            }

            if (entity.clashing() || phase == DomainInstance.Phase.COLLAPSING) {
               VertexConsumer glow = buffers.getBuffer(ModRenderTypes.GLOW);
               long seed = (long)(time / 2.0F) * 31L + entity.getId();
               Random random = new Random(seed);

               for (int i = 0; i < 6; i++) {
                  Vec3 a = randomOnSphere(random).scale(r);
                  Vec3 b = a.add(randomOnSphere(random).scale(r * 0.25));
                  GlowMesh.bolt(pose, glow, a, b.normalize().scale(r), 0.15F, GlowMesh.withAlpha(style.glow(), 0.9F), random.nextLong());
               }
            }
         }
      }
   }

   private static Vec3 randomOnSphere(Random random) {
      return new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
   }

   private static void renderInterior(PoseStack stack, MultiBufferSource buffers, DomainStyle style, float r, float time) {
      VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(interior(style)));
      Pose pose = stack.last();
      int segments = 48;
      int rings = 24;
      float scroll = time * 6.0E-4F;

      for (int i = 0; i < rings; i++) {
         float v0 = (float)i / rings;
         float v1 = (float)(i + 1) / rings;
         float t0 = (float) Math.PI * v0 - (float) (Math.PI / 2);
         float t1 = (float) Math.PI * v1 - (float) (Math.PI / 2);

         for (int j = 0; j < segments; j++) {
            float u0 = (float)j / segments;
            float u1 = (float)(j + 1) / segments;
            float p0 = (float) (Math.PI * 2) * u0;
            float p1 = (float) (Math.PI * 2) * u1;
            vertex(vc, pose, r, t0, p0, u0 + scroll, 1.0F - v0);
            vertex(vc, pose, r, t0, p1, u1 + scroll, 1.0F - v0);
            vertex(vc, pose, r, t1, p1, u1 + scroll, 1.0F - v1);
            vertex(vc, pose, r, t1, p0, u0 + scroll, 1.0F - v1);
         }
      }
   }

   private static void vertex(VertexConsumer vc, Pose pose, float r, float theta, float phi, float u, float v) {
      float x = Mth.cos(theta) * Mth.cos(phi);
      float y = Mth.sin(theta);
      float z = Mth.cos(theta) * Mth.sin(phi);
      vc.addVertex(pose, x * r, y * r, z * r)
         .setColor(255, 255, 255, 255)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, -x, -y, -z);
   }

   private static void renderBarrierless(
      DomainBarrierEntity entity,
      Pose pose,
      PoseStack stack,
      MultiBufferSource buffers,
      DomainStyle style,
      float r,
      float time,
      float age,
      DomainInstance.Phase phase
   ) {
      float spread = phase == DomainInstance.Phase.FORMING
         ? Mth.clamp(age / 24.0F, 0.0F, 1.0F)
         : (phase == DomainInstance.Phase.COLLAPSING ? 1.0F - Mth.clamp(age / 24.0F, 0.0F, 1.0F) : 1.0F);
      VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);

      for (int k = 0; k < 4; k++) {
         float rr = r * spread * (1.0F - k * 0.2F);
         GlowMesh.ring(pose, shade, 0.0, 0.06 + k * 0.01, 0.0, rr, rr * 0.2F, GlowMesh.withAlpha(-12976128, 0.35F * spread));
      }

      if (style == DomainStyle.MALEVOLENT_SHRINE) {
         ShrineModel.render(stack, buffers, spread, time);
      }

      VertexConsumer glow = buffers.getBuffer(ModRenderTypes.GLOW);
      GlowMesh.ring(pose, glow, 0.0, 0.08, 0.0, r * spread, 0.8F, GlowMesh.withAlpha(style.glow(), 0.6F * spread));
      Random random = new Random((long)(time / 3.0F) * 7L + entity.getId());

      for (int i = 0; i < 10.0F * spread; i++) {
         Vec3 c = new Vec3((random.nextDouble() - 0.5) * r * 1.6, random.nextDouble() * 6.0 + 0.5, (random.nextDouble() - 0.5) * r * 1.6);
         Vec3 dir = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.5, random.nextGaussian()).normalize();
         Vec3 u = GlowMesh.perpendicular(dir);
         GlowMesh.arc(pose, glow, c, u, dir, 1.6F + random.nextFloat() * 2.0F, -0.9F, 1.8F, 0.18F, GlowMesh.withAlpha(-5912, 0.8F), 1.0F);
      }
   }

   public ResourceLocation getTextureLocation(DomainBarrierEntity entity) {
      return interior(entity.style());
   }
}
