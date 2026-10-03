package com.curseddomain.client.render.entity;

import com.curseddomain.client.render.GlowMesh;
import com.curseddomain.client.render.ModRenderTypes;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class TechniqueProjectileRenderer extends EntityRenderer<TechniqueProjectile> {
   public TechniqueProjectileRenderer(Context context) {
      super(context);
   }

   public void render(TechniqueProjectile entity, float yaw, float partialTick, PoseStack stack, MultiBufferSource buffers, int light) {
      ProjectileKind kind = entity.kind();
      float size = entity.size();
      float time = entity.tickCount + partialTick;
      Vec3 vel = entity.getDeltaMovement();
      Vec3 dir = vel.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : vel.normalize();
      stack.pushPose();
      stack.translate(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
      Pose pose = stack.last();
      int color = kind.color();
      if (kind == ProjectileKind.METEOR) {
         renderMeteor(stack, buffers, size, time);
      }

      VertexConsumer body = buffers.getBuffer(ModRenderTypes.SHADE);
      switch (kind) {
         case BLUE:
            GlowMesh.body(pose, body, 0.0, 0.0, 0.0, size * 0.62F, -16115062, 0.9F);
            break;
         case RED:
            GlowMesh.body(pose, body, 0.0, 0.0, 0.0, size * 0.8F, -4192248, 0.85F);
            break;
         case HOLLOW_PURPLE:
            GlowMesh.body(pose, body, 0.0, 0.0, 0.0, size * 0.85F, -9826112, 0.85F);
            break;
         case DIVINE_FLAME:
         case EMBER_INSECT:
         case METEOR:
            GlowMesh.body(pose, body, 0.0, 0.0, 0.0, size * 0.6F, -3126776, 0.7F);
            break;
         case BLOOD_DISC:
         case BLOOD_SPEAR:
            GlowMesh.body(pose, body, 0.0, 0.0, 0.0, size * 0.4F, -8781816, 0.8F);
         case DISMANTLE:
         case WORLD_SLASH:
         case NAIL:
         case CROW:
            break;
         default:
            GlowMesh.body(pose, body, 0.0, 0.0, 0.0, size * 0.6F, color, 0.6F);
      }

      VertexConsumer glow = buffers.getBuffer(ModRenderTypes.GLOW);
      switch (kind) {
         case BLUE:
            GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, size * 0.3F, GlowMesh.withAlpha(-1, 0.9F), 12);
            GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, size * 0.7F, GlowMesh.withAlpha(-15716097, 0.45F), 14);

            for (int i = 0; i < 3; i++) {
               float ax = time * 0.3F + i * (float) (Math.PI * 2) / 3.0F;
               Vec3 ux = new Vec3(Mth.cos(ax), 0.4 * i - 0.4, Mth.sin(ax)).normalize();
               Vec3 wx = GlowMesh.perpendicular(ux);
               GlowMesh.ring(pose, glow, Vec3.ZERO, ux, wx, size * (1.3F + 0.2F * Mth.sin(time * 0.4F + i)), size * 0.25F, GlowMesh.withAlpha(color, 0.6F));
            }
            break;
         case RED:
            GlowMesh.orb(pose, glow, 0.0, 0.0, 0.0, size, color, 1.0F);

            for (int i = 0; i < 6; i++) {
               float ax = time * 0.5F + i;
               Vec3 spike = new Vec3(Mth.cos(ax * 1.3F), Mth.sin(ax * 0.7F), Mth.sin(ax * 1.1F)).normalize().scale(size * 1.8);
               GlowMesh.bolt(pose, glow, Vec3.ZERO, spike, 0.08F, GlowMesh.withAlpha(color, 0.8F), (long)(time / 2.0F) + i * 31L);
            }
            break;
         case HOLLOW_PURPLE: {
            GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, size * 0.4F, GlowMesh.withAlpha(-1, 0.9F), 12);
            GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, size, GlowMesh.withAlpha(color, 0.3F), 16);
            Vec3 u = GlowMesh.perpendicular(dir);
            Vec3 w = dir.cross(u).normalize();
            float spin = time * 0.4F;
            Vec3 ru = u.scale(Mth.cos(spin)).add(w.scale(Mth.sin(spin)));
            GlowMesh.ring(pose, glow, Vec3.ZERO, ru, dir, size * 1.5F, size * 0.2F, GlowMesh.withAlpha(-53200, 0.7F));
            GlowMesh.ring(pose, glow, Vec3.ZERO, dir, ru, size * 1.5F, size * 0.2F, GlowMesh.withAlpha(-13610753, 0.7F));
            break;
         }
         case DIVINE_FLAME:
            Vec3 tail = dir.scale(-size * 5.0F);
            GlowMesh.energyBeam(pose, glow, tail, dir.scale(size), size * 0.5F, -38384, 1.0F);
            GlowMesh.energyBeam(pose, glow, tail.scale(1.5), Vec3.ZERO, size * 0.9F, -50688, 0.4F);
            GlowMesh.orb(pose, glow, 0.0, 0.0, 0.0, size * 0.8F, -20416, 1.0F);
            break;
         case EMBER_INSECT: {
            GlowMesh.orb(pose, glow, 0.0, 0.0, 0.0, size * 0.6F, color, 1.0F);
            float flap = Mth.sin(time * 1.8F) * 0.6F;
            Vec3 side = GlowMesh.perpendicular(dir).scale(size * 1.1);
            GlowMesh.beam(pose, glow, Vec3.ZERO, side.add(0.0, flap * size, 0.0), size * 0.12F, GlowMesh.withAlpha(-12192, 0.7F), 4);
            GlowMesh.beam(pose, glow, Vec3.ZERO, side.scale(-1.0).add(0.0, flap * size, 0.0), size * 0.12F, GlowMesh.withAlpha(-12192, 0.7F), 4);
            break;
         }
         case METEOR:
            GlowMesh.sphere(pose, glow, 0.0, 0.0, 0.0, size * 1.25F, GlowMesh.withAlpha(color, 0.35F), 16);
            GlowMesh.energyBeam(pose, glow, dir.scale(-size * 4.0F), Vec3.ZERO, size * 0.9F, -34272, 0.5F);
            break;
         case BLOOD_DISC: {
            Vec3 u = GlowMesh.perpendicular(dir);
            Vec3 w = dir.cross(u).normalize();
            float spin = time * 0.9F;
            Vec3 a = u.scale(Mth.cos(spin)).add(dir.scale(Mth.sin(spin) * 0.2));
            GlowMesh.ring(pose, glow, Vec3.ZERO, a, w, size, size * 0.9F, GlowMesh.withAlpha(color, 0.9F));
            GlowMesh.ring(pose, glow, Vec3.ZERO, a, w, size * 1.05F, size * 0.12F, GlowMesh.withAlpha(-40864, 0.9F));
            break;
         }
         case BLOOD_SPEAR:
         case PLANT_SPEAR:
         case ICE_SHARD:
         case SOUL_SPIKE:
            GlowMesh.energyBeam(pose, glow, dir.scale(-size * 2.5), dir.scale(size * 0.8), size * 0.25F, color, 1.0F);
            break;
         case DISMANTLE:
         case WORLD_SLASH: {
            Vec3 u = GlowMesh.perpendicular(dir);
            float roll = entity.getId() % 7 * 0.45F;
            Vec3 side = u.scale(Mth.cos(roll)).add(dir.cross(u).normalize().scale(Mth.sin(roll)));
            float alpha = kind == ProjectileKind.WORLD_SLASH ? 1.0F : 0.85F;
            GlowMesh.arc(
               pose,
               glow,
               dir.scale(-size * 0.6),
               side,
               dir,
               size,
               (float) (-Math.PI * 2.0 / 5.0),
               (float) (Math.PI * 4.0 / 5.0),
               size * 0.12F,
               GlowMesh.withAlpha(color, alpha),
               1.0F
            );
            GlowMesh.arc(
               pose,
               glow,
               dir.scale(-size * 0.6),
               side,
               dir,
               size,
               (float) (-Math.PI * 2.0 / 5.0),
               (float) (Math.PI * 4.0 / 5.0),
               size * 0.04F,
               GlowMesh.withAlpha(-1, 1.0F),
               1.0F
            );
            break;
         }
         case NAIL: {
            VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);
            GlowMesh.beam(pose, shade, dir.scale(-0.45), dir.scale(0.25), 0.035F, -6645080, 6);
            GlowMesh.beam(pose, shade, dir.scale(-0.5), dir.scale(-0.42), 0.08F, -9803144, 6);
            glow = buffers.getBuffer(ModRenderTypes.GLOW);
            GlowMesh.beam(pose, glow, dir.scale(-0.45), dir.scale(0.25), 0.06F, GlowMesh.withAlpha(-9787137, 0.35F), 6);
            break;
         }
         case CROW: {
            VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);
            GlowMesh.sphere(pose, shade, 0.0, 0.0, 0.0, size * 0.4F, -15724524, 8);
            float flap = Mth.sin(time * 1.2F) * 0.5F;
            Vec3 side = GlowMesh.perpendicular(dir).scale(size);
            GlowMesh.beam(pose, shade, Vec3.ZERO, side.add(0.0, flap, 0.0), size * 0.15F, -15198178, 4);
            GlowMesh.beam(pose, shade, Vec3.ZERO, side.scale(-1.0).add(0.0, flap, 0.0), size * 0.15F, -15198178, 4);
            break;
         }
         case ELECTRIC:
            GlowMesh.orb(pose, glow, 0.0, 0.0, 0.0, size * 0.6F, color, 1.0F);

            for (int i = 0; i < 4; i++) {
               Vec3 end = new Vec3(Math.sin(time + i * 2.1), Math.cos(time * 1.3 + i), Math.sin(time * 0.7 + i * 1.7)).scale(size * 1.6);
               GlowMesh.bolt(pose, glow, Vec3.ZERO, end, 0.06F, color, (long)time * 13L + i);
            }
            break;
         default:
            GlowMesh.orb(pose, glow, 0.0, 0.0, 0.0, size, color, 1.0F);
      }

      stack.popPose();
      super.render(entity, yaw, partialTick, stack, buffers, light);
   }

   private static void renderMeteor(PoseStack stack, MultiBufferSource buffers, float size, float time) {
      BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();
      stack.pushPose();
      stack.mulPose(Axis.YP.rotationDegrees(time * 4.0F));
      stack.mulPose(Axis.XP.rotationDegrees(time * 3.0F));
      float s = size * 1.6F;
      stack.scale(s, s, s);
      stack.translate(-0.5, -0.5, -0.5);
      blocks.renderSingleBlock(Blocks.MAGMA_BLOCK.defaultBlockState(), stack, buffers, 15728880, OverlayTexture.NO_OVERLAY);
      stack.popPose();
   }

   public ResourceLocation getTextureLocation(TechniqueProjectile entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
