package com.curseddomain.client.vfx;

import com.curseddomain.vfx.ScreenFxPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent.ComputeCameraAngles;

@EventBusSubscriber(
   modid = "cursed_domain",
   value = {Dist.CLIENT}
)
public final class ScreenEffects implements Layer {
   private static final List<ScreenEffects.Active> ACTIVE = new ArrayList<>();
   private static final Random RANDOM = new Random();

   public static void add(ScreenFxPayload fx) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && ACTIVE.size() < 32) {
         ACTIVE.add(new ScreenEffects.Active(fx, mc.level.getGameTime()));
      }
   }

   public static void whiteoutFrame(GuiGraphics g, float intensity) {
      int w = g.guiWidth();
      int h = g.guiHeight();
      g.fill(0, 0, w, h, (int)(Mth.clamp(intensity, 0.0F, 1.0F) * 220.0F) << 24 | 16054527);

      for (int i = 0; i < (int)(60.0F * intensity); i++) {
         int x = RANDOM.nextInt(Math.max(1, w));
         int y = RANDOM.nextInt(Math.max(1, h));
         int len = 4 + RANDOM.nextInt(40);
         int shade = RANDOM.nextBoolean() ? -1 : -6309633;
         g.fill(x, y, x + len, y + 1, shade);
      }
   }

   private static float progress(ScreenEffects.Active a, float partial) {
      Minecraft mc = Minecraft.getInstance();
      return Mth.clamp(((float)(mc.level.getGameTime() - a.start) + partial) / Math.max(1.0F, (float)a.fx.duration()), 0.0F, 1.0F);
   }

   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && !ACTIVE.isEmpty()) {
         float partial = delta.getGameTimeDeltaPartialTick(false);
         ACTIVE.removeIf(ax -> mc.level.getGameTime() - ax.start > ax.fx.duration());

         for (ScreenEffects.Active a : ACTIVE) {
            float t = progress(a, partial);
            float strength = a.fx.intensity() * (1.0F - t);
            int rgb = a.fx.color() & 16777215;
            switch (a.fx.kind()) {
               case FLASH:
                  g.fill(0, 0, g.guiWidth(), g.guiHeight(), (int)(Mth.clamp(strength, 0.0F, 1.0F) * 255.0F) << 24 | rgb);
                  break;
               case TINT:
                  float k = t < 0.15F ? t / 0.15F : 1.0F - (t - 0.15F) / 0.85F;
                  g.fill(0, 0, g.guiWidth(), g.guiHeight(), (int)(Mth.clamp(k * a.fx.intensity(), 0.0F, 1.0F) * 160.0F) << 24 | rgb);
                  break;
               case WHITEOUT:
                  whiteoutFrame(g, a.fx.intensity() * (t > 0.8F ? (1.0F - t) / 0.2F : 1.0F));
            }
         }
      }
   }

   @SubscribeEvent
   public static void onCameraAngles(ComputeCameraAngles event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         float shake = 0.0F;

         for (ScreenEffects.Active a : ACTIVE) {
            if (a.fx.kind() == ScreenFxPayload.Kind.SHAKE) {
               shake += a.fx.intensity() * (1.0F - progress(a, (float)event.getPartialTick()));
            }
         }

         if (shake > 0.01F) {
            shake = Math.min(shake, 6.0F);
            event.setPitch(event.getPitch() + (RANDOM.nextFloat() - 0.5F) * shake);
            event.setYaw(event.getYaw() + (RANDOM.nextFloat() - 0.5F) * shake);
            event.setRoll(event.getRoll() + (RANDOM.nextFloat() - 0.5F) * shake * 0.6F);
         }
      }
   }

   private record Active(ScreenFxPayload fx, long start) {
   }
}
