package com.steelballrun.client;

import com.steelballrun.config.SbrClientConfig;
import com.steelballrun.race.Stage;
import com.steelballrun.network.RaceStatusPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** The race panel in the corner of the screen. */
public final class RaceHud implements LayeredDraw.Layer {
   private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
   private static final int REGISTRATION = 0;
   private static final int COUNTDOWN = 1;
   private static final int RUNNING = 2;

   @Override
   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      RaceStatusPayload s = ClientRace.status;
      if (player == null || s == null || mc.options.hideGui || !SbrClientConfig.RACE_HUD.get()) {
         return;
      }
      List<Component> lines = lines(s, player);
      if (lines.isEmpty()) {
         return;
      }
      Font font = mc.font;
      int width = 0;
      for (Component line : lines) {
         width = Math.max(width, font.width(line));
      }
      int x = SbrClientConfig.RACE_HUD_RIGHT.get() ? g.guiWidth() - width - 6 : 6;
      int y = 6;
      g.fill(x - 3, y - 3, x + width + 3, y + lines.size() * 10 + 1, 0x88000000);
      for (Component line : lines) {
         g.drawString(font, line, x, y, 0xFFFFFFFF, true);
         y += 10;
      }
   }

   private static List<Component> lines(RaceStatusPayload s, LocalPlayer player) {
      List<Component> out = new ArrayList<>();
      Component title = Component.translatable("hud.steel_ball_run.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
      if (!s.registered()) {
         if (s.phase() == REGISTRATION) {
            out.add(title);
            out.add(Component.translatable("hud.steel_ball_run.registration", s.startDay()).withStyle(ChatFormatting.WHITE));
            out.add(Component.translatable("hud.steel_ball_run.unregistered").withStyle(ChatFormatting.GRAY));
            out.add(direction(s, player, Component.translatable("gate.steel_ball_run.start")));
         } else if (s.phase() == COUNTDOWN || s.phase() == RUNNING) {
            out.add(title);
            out.add(Component.translatable("hud.steel_ball_run.spectating", s.field()).withStyle(ChatFormatting.GRAY));
         }
         return out;
      }
      out.add(title);
      if (s.phase() == REGISTRATION) {
         out.add(Component.translatable("hud.steel_ball_run.registration", s.startDay()).withStyle(ChatFormatting.WHITE));
         out.add(direction(s, player, Component.translatable("gate.steel_ball_run.start")));
      } else if (s.phase() == COUNTDOWN) {
         out.add(Component.translatable("hud.steel_ball_run.countdown", s.seconds()).withStyle(ChatFormatting.YELLOW));
         out.add(direction(s, player, Component.translatable("gate.steel_ball_run.start")));
      } else if (s.phase() == RUNNING) {
         if (s.finishPlace() > 0) {
            out.add(Component.translatable("hud.steel_ball_run.finished", ordinal(s.finishPlace()), s.points()).withStyle(ChatFormatting.GREEN));
            out.add(Component.translatable("hud.steel_ball_run.overall", ordinal(s.overall()), s.points()).withStyle(ChatFormatting.WHITE));
         } else {
            if (s.nextGate() == 0) {
               out.add(Component.translatable("hud.steel_ball_run.to_start").withStyle(ChatFormatting.YELLOW));
            } else {
               Stage stage = Stage.forNextGate(s.nextGate());
               out.add(Component.translatable("hud.steel_ball_run.stage", s.nextGate(), Component.translatable(stage.langKey())).withStyle(ChatFormatting.YELLOW));
            }
            out.add(direction(s, player, null));
            out.add(Component.translatable("hud.steel_ball_run.place", s.place(), s.field()).withStyle(ChatFormatting.WHITE));
            out.add(Component.translatable("hud.steel_ball_run.overall", ordinal(s.overall()), s.points()).withStyle(ChatFormatting.WHITE));
         }
         out.add(Component.translatable("hud.steel_ball_run.time", clock(s.seconds())).withStyle(ChatFormatting.GRAY));
      } else {
         out.add(Component.translatable("hud.steel_ball_run.overall", ordinal(s.overall()), s.points()).withStyle(ChatFormatting.WHITE));
         out.add(Component.translatable("hud.steel_ball_run.over").withStyle(ChatFormatting.GRAY));
      }
      return out;
   }

   /** "Checkpoint 3: 412 blocks ↗", the arrow relative to where the player is looking. */
   private static Component direction(RaceStatusPayload s, LocalPlayer player, Component name) {
      double dx = s.gateX() + 0.5 - player.getX();
      double dz = s.gateZ() + 0.5 - player.getZ();
      int dist = (int)Math.sqrt(dx * dx + dz * dz);
      float target = (float)Math.toDegrees(Math.atan2(-dx, dz));
      float rel = Mth.wrapDegrees(target - player.getYRot());
      String arrow = ARROWS[Math.floorMod(Math.round(rel / 45.0F), 8)];
      Object label = name != null ? name : Component.literal(String.valueOf(s.nextGate()));
      return Component.translatable("hud.steel_ball_run.next", label, dist, arrow).withStyle(ChatFormatting.AQUA);
   }

   private static String clock(int seconds) {
      int h = seconds / 3600;
      int m = seconds / 60 % 60;
      int sec = seconds % 60;
      return String.format(java.util.Locale.ROOT, "%d:%02d:%02d", h, m, sec);
   }

   private static String ordinal(int n) {
      int mod100 = n % 100;
      int mod10 = n % 10;
      return n + (mod100 >= 11 && mod100 <= 13 ? "th" : mod10 == 1 ? "st" : mod10 == 2 ? "nd" : mod10 == 3 ? "rd" : "th");
   }
}
