package com.curseddomain.client.hud;

import com.curseddomain.client.input.ModKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class ClashHud implements Layer {
   private static float share = -1.0F;
   private static float shown = 0.5F;
   private static int remaining;

   public static void update(float newShare, int remainingTicks) {
      share = newShare;
      remaining = remainingTicks;
      if (newShare < 0.0F) {
         shown = 0.5F;
      }
   }

   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      if (!(share < 0.0F) && mc.player != null && !mc.options.hideGui) {
         shown = Mth.lerp(0.2F, shown, share);
         int w = 200;
         int x = (g.guiWidth() - w) / 2;
         int y = 30;
         g.fill(x - 2, y - 2, x + w + 2, y + 10, -16777216);
         int split = x + Math.round(w * shown);
         g.fillGradient(x, y, split, y + 8, -7352065, -14202935);
         g.fillGradient(split, y, x + w, y + 8, -38306, -7730678);
         g.fill(split - 1, y - 3, split + 1, y + 11, -1);
         float pulse = 0.5F + 0.5F * Mth.sin(mc.player.tickCount * 0.6F);
         Component title = Component.translatable("hud.cursed_domain.clash", new Object[]{ModKeys.DOMAIN.getTranslatedKeyMessage()})
            .withStyle(ChatFormatting.BOLD);
         g.drawCenteredString(mc.font, title, g.guiWidth() / 2, y - 14, (int)(155.0F + pulse * 100.0F) << 24 | 16767050);
         g.drawCenteredString(mc.font, String.format("%.1fs", remaining / 20.0F), g.guiWidth() / 2, y + 13, -2039584);
      }
   }
}
