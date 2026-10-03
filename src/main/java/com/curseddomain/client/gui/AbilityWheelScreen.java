package com.curseddomain.client.gui;

import com.curseddomain.client.input.AbilityInput;
import com.curseddomain.client.input.ModKeys;
import com.curseddomain.network.payload.AbilityInputPayloads;
import com.curseddomain.network.payload.AbilitySyncPayload;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityManager;
import com.curseddomain.technique.ability.AbilityState;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

public class AbilityWheelScreen extends Screen {
   private static final int RADIUS = 72;
   public static boolean debugHold;
   private int hovered = -1;

   public AbilityWheelScreen() {
      super(Component.translatable("gui.cursed_domain.ability_wheel"));
   }

   private List<Ability> abilities() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player == null ? List.of() : AbilityManager.abilities(mc.player);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void tick() {
      Minecraft mc = Minecraft.getInstance();
      boolean held = debugHold || InputConstants.isKeyDown(mc.getWindow().getWindow(), ModKeys.WHEEL.getKey().getValue());
      if (!held) {
         this.castHovered();
         this.onClose();
      }
   }

   private void castHovered() {
      List<Ability> list = this.abilities();
      if (this.hovered >= 0 && this.hovered < list.size()) {
         AbilityInput.send(this.hovered, true);
         if (list.get(this.hovered).kind() == AbilityKind.CHARGE || list.get(this.hovered).kind() == AbilityKind.CHANNEL) {
            AbilityInput.send(this.hovered, false);
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode >= 49 && keyCode <= 52 && this.hovered >= 0) {
         PacketDistributor.sendToServer(new AbilityInputPayloads.AssignSlot(keyCode - 49, this.hovered), new CustomPacketPayload[0]);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fillGradient(0, 0, this.width, this.height, 1610612736, -1877997544);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
      List<Ability> list = this.abilities();
      Minecraft mc = Minecraft.getInstance();
      AbilityState state = (AbilityState)mc.player.getData(ModAttachments.ABILITY_STATE);
      AbilitySyncPayload view = state.clientView();
      int cx = this.width / 2;
      int cy = this.height / 2;
      int n = list.size();
      double dx = mouseX - cx;
      double dy = mouseY - cy;
      this.hovered = -1;
      if (n > 0 && dx * dx + dy * dy > 256.0) {
         double angle = Math.atan2(dy, dx) + (Math.PI / 2);
         if (angle < 0.0) {
            angle += Math.PI * 2;
         }

         this.hovered = (int)Math.floor(angle / (Math.PI * 2) * n + 0.5) % n;
      }

      for (int i = 0; i < n; i++) {
         Ability ability = list.get(i);
         double a = i * Math.PI * 2.0 / n - (Math.PI / 2);
         int x = cx + (int)(Math.cos(a) * 72.0) - 12;
         int y = cy + (int)(Math.sin(a) * 72.0) - 12;
         boolean hot = i == this.hovered;
         int border = ability.isMaximum() ? -1523648 : (hot ? -4658945 : -11912602);
         g.fill(x - 2, y - 2, x + 26, y + 26, border);
         g.fill(x - 1, y - 1, x + 25, y + 25, hot ? -14016440 : -15462882);
         g.pose().pushPose();
         g.pose().translate(x, y, 0.0F);
         g.pose().scale(1.5F, 1.5F, 1.0F);
         g.blit(ability.icon(), 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
         g.pose().popPose();
         if (view != null && i < view.entries().size() && view.entries().get(i).cooldownRemaining() > 0) {
            g.fill(x - 1, y - 1, x + 25, y + 25, -1879048192);
         }

         for (int s = 0; s < 4; s++) {
            if (state.slot(s) == i) {
               g.drawString(this.font, String.valueOf(s + 1), x + 20, y - 6, -1523648, true);
            }
         }
      }

      if (this.hovered >= 0 && this.hovered < n) {
         Ability ability = list.get(this.hovered);
         g.drawCenteredString(this.font, ability.displayName().copy().withStyle(ChatFormatting.BOLD), cx, cy - 24, -1);
         int y = cy - 12;

         for (FormattedCharSequence line : this.font.split(ability.description(), 150)) {
            g.drawCenteredString(this.font, line, cx, y, -3620648);
            y += 9;
         }

         if (view != null && this.hovered < view.entries().size()) {
            AbilitySyncPayload.Entry e = view.entries().get(this.hovered);
            Component cost = Component.translatable(
               "gui.cursed_domain.ability_cost", new Object[]{Math.round(e.cost()), String.format("%.1f", ability.stats().cooldown() / 20.0F)}
            );
            g.drawCenteredString(this.font, cost, cx, y + 2, -7352065);
         }

         if (ability.isMaximum()) {
            g.drawCenteredString(this.font, Component.translatable("gui.cursed_domain.maximum").withStyle(ChatFormatting.GOLD), cx, y + 12, -1523648);
         }
      } else {
         g.drawCenteredString(this.font, Component.translatable("gui.cursed_domain.ability_wheel.hint"), cx, cy - 4, -7701848);
      }

      g.drawCenteredString(this.font, Component.translatable("gui.cursed_domain.ability_wheel.assign"), cx, cy + 72 + 26, -9807224);
      g.fill(cx - 1, cy - 1, cx + 1, cy + 1, -1);
   }
}
