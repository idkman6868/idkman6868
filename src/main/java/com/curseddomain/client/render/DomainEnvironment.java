package com.curseddomain.client.render;

import com.curseddomain.client.vfx.ScreenEffects;
import com.curseddomain.domain.DomainBarrierEntity;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.registry.ModEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeFogColor;
import net.neoforged.neoforge.client.event.ViewportEvent.RenderFog;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "cursed_domain",
   value = {Dist.CLIENT}
)
public final class DomainEnvironment implements Layer {
   private DomainEnvironment() {
   }

   public static DomainEnvironment overlay() {
      return new DomainEnvironment();
   }

   @Nullable
   public static DomainBarrierEntity cameraDomain() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return null;
      } else {
         Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
         DomainBarrierEntity best = null;

         for (DomainBarrierEntity d : mc.level.getEntitiesOfClass(DomainBarrierEntity.class, new AABB(cam, cam).inflate(256.0))) {
            if (d.phase() != DomainInstance.Phase.CASTING && d.contains(cam) && d.domainType() != DomainType.INCOMPLETE) {
               best = d;
            }
         }

         return best;
      }
   }

   @SubscribeEvent
   public static void onFogColor(ComputeFogColor event) {
      DomainBarrierEntity d = cameraDomain();
      if (d != null) {
         int c = d.style().fog();
         float k = d.domainType() == DomainType.BARRIERLESS ? 0.6F : 0.85F;
         event.setRed(Mth.lerp(k, event.getRed(), (c >> 16 & 0xFF) / 255.0F));
         event.setGreen(Mth.lerp(k, event.getGreen(), (c >> 8 & 0xFF) / 255.0F));
         event.setBlue(Mth.lerp(k, event.getBlue(), (c & 0xFF) / 255.0F));
      }
   }

   @SubscribeEvent
   public static void onRenderFog(RenderFog event) {
      DomainBarrierEntity d = cameraDomain();
      if (d != null && d.domainType() == DomainType.BARRIERLESS) {
         event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), d.radius() * 0.5F));
         event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), d.radius() * 2.2F));
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public static void onRenderStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         DomainBarrierEntity d = cameraDomain();
         if (d != null && d.domainType() == DomainType.BARRIERLESS) {
            Minecraft mc = Minecraft.getInstance();
            BufferSource buffers = mc.renderBuffers().bufferSource();
            VertexConsumer shade = buffers.getBuffer(ModRenderTypes.SHADE);
            PoseStack stack = new PoseStack();
            GlowMesh.sphere(stack.last(), shade, 0.0, 0.0, 0.0, 120.0F, GlowMesh.withAlpha(-7732224, 0.78F), 24);
            buffers.endBatch(ModRenderTypes.SHADE);
         }
      }
   }

   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !mc.options.hideGui) {
         DomainBarrierEntity d = cameraDomain();
         if (d != null && d.phase() == DomainInstance.Phase.ACTIVE) {
            Entity owner = mc.level.getEntity(d.ownerId());
            if (owner != mc.player) {
               float time = mc.player.tickCount + delta.getGameTimeDeltaPartialTick(false);
               DomainStyle style = d.style();
               if (style == DomainStyle.UNLIMITED_VOID && mc.player.hasEffect(ModEffects.STUNNED)) {
                  ScreenEffects.whiteoutFrame(g, 0.75F + 0.25F * Mth.sin(time * 0.7F));
               } else if (style == DomainStyle.COFFIN_OF_THE_IRON_MOUNTAIN) {
                  vignette(g, 16738842, 0.45F + 0.1F * Mth.sin(time * 0.3F));
               } else if (style == DomainStyle.CHIMERA_SHADOW_GARDEN || style == DomainStyle.SELF_EMBODIMENT_OF_PERFECTION) {
                  vignette(g, 0, 0.5F);
               }
            }
         }
      }
   }

   private static void vignette(GuiGraphics g, int rgb, float strength) {
      int w = g.guiWidth();
      int h = g.guiHeight();
      int steps = 16;
      int band = Math.max(2, Math.min(w, h) / 40);

      for (int i = 0; i < steps; i++) {
         float edge = 1.0F - (float)i / steps;
         int c = (int)(Mth.clamp(strength * edge * edge, 0.0F, 1.0F) * 255.0F) << 24 | rgb;
         int in = i * band;
         g.fill(in, in, w - in, in + band, c);
         g.fill(in, h - in - band, w - in, h - in, c);
         g.fill(in, in + band, in + band, h - in - band, c);
         g.fill(w - in - band, in + band, w - in, h - in - band, c);
      }
   }
}
