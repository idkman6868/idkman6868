package com.curseddomain.client.hud;

import com.curseddomain.config.ClientConfig;
import com.curseddomain.config.HudAnchor;
import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.sorcerer.SorcererData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public final class CursedEnergyHud implements Layer {
   private static final int ROW_WIDTH = 81;
   private static final int CORNER_WIDTH = 90;
   private static final int BAR_HEIGHT = 5;
   private static final int TEXT_COLOR = -1646347;
   private static final int MEDITATING_COLOR = -6297345;
   private static final int LOW_COLOR = -38306;
   private float shown = -1.0F;

   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && !mc.options.hideGui && !player.isSpectator() && (Boolean)ClientConfig.SHOW_ENERGY_BAR.get()) {
         SorcererData sorcerer = (SorcererData)player.getData(ModAttachments.SORCERER);
         CursedEnergyData energy = (CursedEnergyData)player.getData(ModAttachments.CURSED_ENERGY);
         if (sorcerer.status().awakened() && !(energy.max() <= 0.0F)) {
            float target = Mth.clamp(energy.current() / energy.max(), 0.0F, 1.0F);
            float step = 1.0F - (float)Math.pow(0.75, delta.getRealtimeDeltaTicks());
            this.shown = this.shown < 0.0F ? target : Mth.lerp(step, this.shown, target);
            float time = player.tickCount + delta.getGameTimeDeltaPartialTick(false);
            boolean low = target < 0.2F;
            float pulse = low ? 0.5F + 0.5F * Mth.sin(time * 0.35F) : 0.0F;
            Component numbers = ClientConfig.SHOW_ENERGY_NUMBERS.get()
               ? Component.literal(Math.round(energy.current()) + " / " + Math.round(energy.max()))
               : null;
            int numberColor = energy.meditating() ? -6297345 : (low ? lerp(pulse, -1646347, -38306) : -1646347);
            if (ClientConfig.ENERGY_BAR_ANCHOR.get() == HudAnchor.HOTBAR) {
               this.renderRow(mc, g, energy, numbers, numberColor, low, pulse, time);
            } else {
               this.renderCorner(mc, g, sorcerer, energy, numbers, numberColor, low, pulse, time);
            }
         } else {
            this.shown = -1.0F;
         }
      }
   }

   private void renderRow(Minecraft mc, GuiGraphics g, CursedEnergyData energy, Component numbers, int numberColor, boolean low, float pulse, float time) {
      Gui gui = mc.gui;
      Font font = mc.font;
      int right = g.guiWidth() / 2 + 91;
      int barX = right - 81;
      int barY = g.guiHeight() - gui.rightHeight + 3;
      int used = 10;
      if (numbers != null) {
         g.drawString(font, numbers, right - font.width(numbers), barY - 9 - 1, numberColor, true);
         used += 10;
      }

      this.bar(g, energy, barX, barY, 81, low, pulse, time);
      gui.rightHeight += used;
   }

   private void renderCorner(
      Minecraft mc, GuiGraphics g, SorcererData sorcerer, CursedEnergyData energy, Component numbers, int numberColor, boolean low, float pulse, float time
   ) {
      Font font = mc.font;
      Component label = Component.translatable(energy.meditating() ? "hud.cursed_domain.meditating" : "hud.cursed_domain.energy");
      Component grade = sorcerer.status().enrolled() ? sorcerer.grade().displayName().copy().withStyle(ChatFormatting.GRAY) : null;
      int numbersWidth = numbers == null ? 0 : font.width(numbers) + 4;
      int labelWidth = 1 + font.width(label) + (grade == null ? 0 : 6 + font.width(grade));
      int width = Math.max(92 + numbersWidth, labelWidth);
      int height = 9 + 2 + 5 + 2;
      HudAnchor anchor = (HudAnchor)ClientConfig.ENERGY_BAR_ANCHOR.get();
      int x = anchor.right() ? g.guiWidth() - width - (Integer)ClientConfig.ENERGY_BAR_OFFSET_X.get() : (Integer)ClientConfig.ENERGY_BAR_OFFSET_X.get();
      int y = anchor.bottom() ? g.guiHeight() - height - (Integer)ClientConfig.ENERGY_BAR_OFFSET_Y.get() : (Integer)ClientConfig.ENERGY_BAR_OFFSET_Y.get();
      g.drawString(font, label, x + 1, y, energy.meditating() ? -6297345 : -1646347, true);
      if (grade != null) {
         g.drawString(font, grade, x + 1 + font.width(label) + 6, y, -5592406, true);
      }

      int barX = x + 1;
      int barY = y + 9 + 2;
      this.bar(g, energy, barX, barY, 90, low, pulse, time);
      if (numbers != null) {
         g.drawString(font, numbers, barX + 90 + 4, barY + (5 - 9) / 2 + 1, numberColor, true);
      }
   }

   private void bar(GuiGraphics g, CursedEnergyData energy, int x, int y, int width, boolean low, float pulse, float time) {
      int frame = low ? lerp(pulse, -14675944, -5230550) : -15462372;
      g.fill(x - 1, y - 1, x + width + 1, y + 5 + 1, frame);
      g.fill(x, y, x + width, y + 5, -1073741824);
      int fill = Math.round(width * this.shown);
      if (fill > 0) {
         g.fillGradient(x, y, x + fill, y + 5, energy.nature().hudTop(), energy.nature().hudBottom());
         if (energy.meditating()) {
            int shimmerX = x + Math.round(fill * (time % 40.0F / 40.0F));
            g.fill(shimmerX, y, Math.min(x + fill, shimmerX + 3), y + 5, -2130706433);
         }
      }
   }

   private static int lerp(float t, int from, int to) {
      return ARGB32.lerp(t, from, to);
   }
}
