package com.curseddomain.client.hud;

import com.curseddomain.client.input.ModKeys;
import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.network.payload.AbilitySyncPayload;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityManager;
import com.curseddomain.technique.ability.AbilityState;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class AbilityBarHud implements Layer {
   private static final int SLOT = 18;
   private static final int GAP = 2;

   public void render(GuiGraphics g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null && !mc.options.hideGui && !player.isSpectator()) {
         SorcererData sorcerer = (SorcererData)player.getData(ModAttachments.SORCERER);
         AbilityState state = (AbilityState)player.getData(ModAttachments.ABILITY_STATE);
         AbilitySyncPayload view = state.clientView();
         List<Ability> abilities = AbilityManager.abilities(player);
         Technique technique = sorcerer.technique().orElse(null);
         if (view != null && !abilities.isEmpty() && !view.entries().isEmpty() && sorcerer.status().awakened()) {
            CursedEnergyData energy = (CursedEnergyData)player.getData(ModAttachments.CURSED_ENERGY);
            float elapsed = (float)(player.level().getGameTime() - state.clientSyncTick()) + delta.getGameTimeDeltaPartialTick(false);
            int x0 = g.guiWidth() / 2 - 91;
            int y = g.guiHeight() - mc.gui.leftHeight - 18 - 2;
            Font font = mc.font;

            for (int i = 0; i < 4; i++) {
               int index = state.slot(i);
               int x = x0 + i * 20;
               g.fill(x, y, x + 18, y + 18, -1341125608);
               if (index >= 0 && index < abilities.size() && index < view.entries().size()) {
                  Ability ability = abilities.get(index);
                  AbilitySyncPayload.Entry entry = view.entries().get(index);
                  g.blit(ability.icon(), x + 1, y + 1, 0.0F, 0.0F, 16, 16, 16, 16);
                  float remaining = Math.max(0.0F, entry.cooldownRemaining() - elapsed);
                  boolean affordable = entry.cost() <= energy.current() + 0.01F;
                  if (!affordable) {
                     g.fill(x + 1, y + 1, x + 17, y + 17, 1890586640);
                  }

                  if (remaining > 0.0F) {
                     float frac = Mth.clamp(remaining / Math.max(1, entry.cooldownTotal()), 0.0F, 1.0F);
                     int h = Math.round(16.0F * frac);
                     g.fill(x + 1, y + 1 + (16 - h), x + 17, y + 17, -1610612736);
                     perimeter(g, x, y, 1.0F - frac, -7352065);
                     String secs = remaining >= 20.0F ? String.valueOf((int)Math.ceil(remaining / 20.0F)) : String.format("%.1f", remaining / 20.0F);
                     g.drawCenteredString(font, secs, x + 9, y + 5, -1);
                  } else {
                     outline(g, x, y, ability.isMaximum() ? -1523648 : -11912602);
                  }

                  if (entry.active()) {
                     float pulse = 0.5F + 0.5F * Mth.sin((player.tickCount + delta.getGameTimeDeltaPartialTick(false)) * 0.3F);
                     int a = (int)(120.0F + pulse * 135.0F);
                     outline(g, x - 1, y - 1, 20, a << 24 | 8380671);
                     outline(g, x, y, a << 24 | 11596031);
                  }

                  if (view.chargingIndex() == index) {
                     float charge = Mth.clamp((view.chargeElapsed() + elapsed) / view.chargeMax(), 0.0F, 1.0F);
                     g.fill(x, y - 4, x + 18, y - 1, -1073741824);
                     g.fill(x, y - 4, x + Math.round(18.0F * charge), y - 1, charge >= 1.0F ? -8064 : -8408321);
                  }

                  String key = ModKeys.SLOTS[i].getTranslatedKeyMessage().getString();
                  if (key.length() > 2) {
                     key = key.substring(0, 1);
                  }

                  g.pose().pushPose();
                  g.pose().translate(0.0F, 0.0F, 200.0F);
                  g.drawString(font, key, x + 18 - font.width(key), y + 18 - 8, -2041616, true);
                  g.pose().popPose();
               } else {
                  outline(g, x, y, -14015946);
               }
            }

            mc.gui.leftHeight += 24;
            if (technique != null && AbilityManager.techniqueAvailable(player)) {
               technique.domain().ifPresent(domain -> {
                  int xx = x0 + 80 + 2;
                  if (xx + 40 < g.guiWidth() / 2) {
                     g.drawString(font, Component.literal("[" + ModKeys.DOMAIN.getTranslatedKeyMessage().getString() + "]"), xx, y + 5, -7704400, true);
                  }
               });
            }
         }
      }
   }

   private static void outline(GuiGraphics g, int x, int y, int color) {
      outline(g, x, y, 18, color);
   }

   private static void outline(GuiGraphics g, int x, int y, int size, int color) {
      g.fill(x, y, x + size, y + 1, color);
      g.fill(x, y + size - 1, x + size, y + size, color);
      g.fill(x, y, x + 1, y + size, color);
      g.fill(x + size - 1, y, x + size, y + size, color);
   }

   private static void perimeter(GuiGraphics g, int x, int y, float progress, int color) {
      int len = 68;
      int lit = Math.round(len * progress);

      for (int i = 0; i < lit; i++) {
         int side = i / 17;
         int off = i % 17;
         int px;
         int py;
         switch (side) {
            case 0:
               px = x + off;
               py = y;
               break;
            case 1:
               px = x + 18 - 1;
               py = y + off;
               break;
            case 2:
               px = x + 18 - 1 - off;
               py = y + 18 - 1;
               break;
            default:
               px = x;
               py = y + 18 - 1 - off;
         }

         g.fill(px, py, px + 1, py + 1, color);
      }
   }
}
