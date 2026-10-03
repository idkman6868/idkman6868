package com.curseddomain.client.story;

import com.curseddomain.sorcerer.AwakeningCause;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public final class AwakeningOverlay implements Layer {
   private static final int DURATION = 140;
   private static long startTick = -1L;

   public static void start(AwakeningCause cause) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         startTick = mc.level.getGameTime();
         mc.gui.setTimes(10, 70, 30);
         mc.gui.setTitle(Component.translatable("awakening.cursed_domain.title").withStyle(ChatFormatting.DARK_PURPLE));
         mc.gui.setSubtitle(Component.translatable(cause.subtitleKey()).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ELDER_GUARDIAN_CURSE, 0.7F, 0.8F));
         mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_ACTIVATE, 0.6F, 1.4F));
      }
   }

   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      if (startTick >= 0L && mc.level != null) {
         float t = (float)(mc.level.getGameTime() - startTick) + delta.getGameTimeDeltaPartialTick(false);
         if (!(t > 140.0F) && !(t < 0.0F)) {
            int w = g.guiWidth();
            int h = g.guiHeight();
            if (t < 12.0F) {
               float flash = 1.0F - t / 12.0F;
               g.fill(0, 0, w, h, color(flash * 0.95F, 255, 255, 255));
            }

            float strength = t < 20.0F ? t / 20.0F : 1.0F - Mth.clamp((t - 40.0F) / 100.0F, 0.0F, 1.0F);
            float pulse = 0.85F + 0.15F * Mth.sin(t * 0.25F);
            int steps = 24;

            for (int i = 0; i < steps; i++) {
               float edge = 1.0F - (float)i / steps;
               float alpha = strength * pulse * edge * edge * 0.85F;
               int c = color(alpha, 40, 8, 60);
               int inset = i * Math.max(1, Math.min(w, h) / (steps * 4));
               int band = Math.max(1, Math.min(w, h) / (steps * 4));
               g.fill(inset, inset, w - inset, inset + band, c);
               g.fill(inset, h - inset - band, w - inset, h - inset, c);
               g.fill(inset, inset + band, inset + band, h - inset - band, c);
               g.fill(w - inset - band, inset + band, w - inset, h - inset - band, c);
            }
         } else {
            startTick = -1L;
         }
      }
   }

   private static int color(float alpha, int r, int g, int b) {
      return (int)(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24 | r << 16 | g << 8 | b;
   }
}
