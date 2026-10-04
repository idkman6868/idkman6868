package com.steelballrun.client;

import com.steelballrun.config.SbrClientConfig;
import com.steelballrun.network.HorseStatusPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Your horse's stamina bar, above the hotbar while you ride. */
public final class HorseHud implements LayeredDraw.Layer {
   private static final int WIDTH = 120;
   private float shown = -1.0F;

   @Override
   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      HorseStatusPayload h = ClientRace.horse;
      if (player == null || h == null || mc.options.hideGui || !player.isPassenger() || !SbrClientConfig.HORSE_HUD.get() || h.max() <= 0.0F) {
         this.shown = -1.0F;
         return;
      }
      float target = Mth.clamp(h.stamina() / h.max(), 0.0F, 1.0F);
      float step = 1.0F - (float)Math.pow(0.8, delta.getRealtimeDeltaTicks());
      this.shown = this.shown < 0.0F ? target : Mth.lerp(step, this.shown, target);
      Font font = mc.font;
      int x = g.guiWidth() / 2 - WIDTH / 2;
      int y = g.guiHeight() - 72;
      float time = player.tickCount + delta.getGameTimeDeltaPartialTick(false);
      int colour;
      if (h.exhausted()) {
         colour = Mth.sin(time * 0.4F) > 0.0F ? 0xFFD03030 : 0xFF801818;
      } else if (target < 0.25F) {
         colour = 0xFFE07020;
      } else if (target < 0.5F) {
         colour = 0xFFE0C030;
      } else {
         colour = 0xFF50C050;
      }
      g.fill(x - 1, y - 1, x + WIDTH + 1, y + 6, 0xAA000000);
      g.fill(x, y, x + Math.round(WIDTH * this.shown), y + 5, colour);
      Component label = h.exhausted()
         ? Component.translatable("hud.steel_ball_run.exhausted")
         : Component.translatable("hud.steel_ball_run.stamina").append(" " + Math.round(h.stamina()) + "/" + Math.round(h.max()));
      Component bond = Component.translatable("hud.steel_ball_run.bond", h.bond());
      g.drawString(font, label, x, y - 10, h.exhausted() ? 0xFFFF6060 : 0xFFE8E8E8, true);
      g.drawString(font, bond, x + WIDTH - font.width(bond), y - 10, 0xFFE8C880, true);
   }
}
