package com.curseddomain.client.culling;

import com.curseddomain.client.input.ModKeys;
import com.curseddomain.network.payload.AbilityInputPayloads;
import com.curseddomain.network.payload.AbilitySyncPayload;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityManager;
import com.curseddomain.technique.ability.AbilityState;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The technique menu: every ability you have, and the five quick slots. Click an ability, then click a slot
 * (or press 1-5) to put it there.
 */
public class TechniqueMenuScreen extends Screen {
   private static final int SLOTS = 5;
   private static final int CELL = 26;
   private static final int COLUMNS = 6;
   private int selected = -1;
   private int hovered = -1;
   private int hoveredSlot = -1;

   public TechniqueMenuScreen() {
      super(Component.translatable("gui.cursed_domain.technique_menu"));
   }

   public boolean isPauseScreen() {
      return false;
   }

   private List<Ability> abilities() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player == null ? List.of() : AbilityManager.abilities(mc.player);
   }

   private int gridX() {
      return this.width / 2 - 160;
   }

   private int gridY() {
      return this.height / 2 - 70;
   }

   private int slotX(int slot) {
      return this.width / 2 - (SLOTS * 30) / 2 + slot * 30;
   }

   private int slotY() {
      return this.height / 2 + 78;
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player != null) {
         SorcererData sorcerer = (SorcererData)player.getData(ModAttachments.SORCERER);
         AbilityState state = (AbilityState)player.getData(ModAttachments.ABILITY_STATE);
         AbilitySyncPayload view = state.clientView();
         List<Ability> list = this.abilities();
         int cx = this.width / 2;
         Technique technique = sorcerer.technique().orElse(null);
         Component title = technique != null && sorcerer.techniqueRevealed()
            ? technique.displayName().copy().withStyle(new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD})
            : Component.translatable("gui.cursed_domain.technique_menu.sealed").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.BOLD});
         g.drawCenteredString(this.font, title, cx, this.gridY() - 34, -1);
         g.drawCenteredString(
            this.font,
            Component.translatable("gui.cursed_domain.technique_menu.status", sorcerer.status().displayName(), sorcerer.grade().displayName()),
            cx,
            this.gridY() - 22,
            -6250336
         );
         if (technique != null && !sorcerer.techniqueRevealed()) {
            g.drawCenteredString(this.font, Component.translatable("gui.cursed_domain.technique_menu.enroll"), cx, this.gridY() - 10, -2039584);
         }

         // ability grid
         this.hovered = -1;

         for (int i = 0; i < list.size(); i++) {
            Ability ability = list.get(i);
            int x = this.gridX() + (i % COLUMNS) * CELL;
            int y = this.gridY() + (i / COLUMNS) * CELL;
            boolean hot = mouseX >= x && mouseX < x + 22 && mouseY >= y && mouseY < y + 22;
            if (hot) {
               this.hovered = i;
            }

            int border = i == this.selected ? -1523648 : (hot ? -4658945 : (ability.isMaximum() ? -6728704 : -11912602));
            g.fill(x - 1, y - 1, x + 23, y + 23, border);
            g.fill(x, y, x + 22, y + 22, hot ? -14016440 : -15462882);
            g.blit(ability.icon(), x + 3, y + 3, 0.0F, 0.0F, 16, 16, 16, 16);
            if (!sorcerer.grade().atLeast(ability.minGrade())) {
               g.fill(x, y, x + 22, y + 22, -1342177280);
            }

            for (int s = 0; s < SLOTS; s++) {
               if (state.slot(s) == i) {
                  g.drawString(this.font, String.valueOf(s + 1), x + 16, y + 1, -1523648, true);
               }
            }
         }

         if (list.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("gui.cursed_domain.technique_menu.empty"), cx, this.gridY() + 20, -6250336);
         }

         // details of the hovered (or selected) ability
         int shown = this.hovered >= 0 ? this.hovered : this.selected;
         int px = this.gridX() + COLUMNS * CELL + 14;
         int py = this.gridY();
         if (shown >= 0 && shown < list.size()) {
            Ability ability = list.get(shown);
            g.drawString(this.font, ability.displayName().copy().withStyle(ChatFormatting.BOLD), px, py, -1, true);
            int y = py + 12;

            for (FormattedCharSequence line : this.font.split(ability.description(), 150)) {
               g.drawString(this.font, line, px, y, -3620648, false);
               y += 10;
            }

            if (view != null && shown < view.entries().size()) {
               AbilitySyncPayload.Entry entry = view.entries().get(shown);
               g.drawString(
                  this.font,
                  Component.translatable("gui.cursed_domain.ability_cost", Math.round(entry.cost()), String.format("%.1f", ability.stats().cooldown() / 20.0F)),
                  px,
                  y + 4,
                  -7352065,
                  false
               );
               y += 14;
            }

            if (!sorcerer.grade().atLeast(ability.minGrade())) {
               g.drawString(this.font, Component.translatable("gui.cursed_domain.technique_menu.needs_grade", ability.minGrade().displayName()), px, y + 4, -40864, false);
            } else if (ability.isMaximum()) {
               g.drawString(this.font, Component.translatable("gui.cursed_domain.maximum").withStyle(ChatFormatting.GOLD), px, y + 4, -1523648, false);
            }
         } else {
            int y = py;

            for (FormattedCharSequence line : this.font.split(Component.translatable("gui.cursed_domain.technique_menu.help"), 150)) {
               g.drawString(this.font, line, px, y, -6250336, false);
               y += 10;
            }
         }

         // the five quick slots
         this.hoveredSlot = -1;
         g.drawCenteredString(this.font, Component.translatable("gui.cursed_domain.technique_menu.slots"), cx, this.slotY() - 12, -6250336);

         for (int s = 0; s < SLOTS; s++) {
            int x = this.slotX(s);
            int y = this.slotY();
            boolean hot = mouseX >= x && mouseX < x + 26 && mouseY >= y && mouseY < y + 26;
            if (hot) {
               this.hoveredSlot = s;
            }

            g.fill(x - 1, y - 1, x + 27, y + 27, hot && this.selected >= 0 ? -1523648 : -11912602);
            g.fill(x, y, x + 26, y + 26, -15462882);
            int index = state.slot(s);
            if (index >= 0 && index < list.size()) {
               g.blit(list.get(index).icon(), x + 5, y + 5, 0.0F, 0.0F, 16, 16, 16, 16);
            }

            String key = ModKeys.SLOTS[s].getTranslatedKeyMessage().getString();
            if (key.length() > 2) {
               key = key.substring(0, 1);
            }

            g.drawString(this.font, key, x + 26 - this.font.width(key) - 1, y + 26 - 9, -2041616, true);
         }
      }
   }

   private void assign(int slot, int index) {
      if (slot >= 0 && slot < SLOTS && index >= 0 && index < this.abilities().size()) {
         PacketDistributor.sendToServer(new AbilityInputPayloads.AssignSlot(slot, index), new CustomPacketPayload[0]);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.hovered >= 0) {
         this.selected = this.hovered == this.selected ? -1 : this.hovered;
         return true;
      } else if (this.hoveredSlot >= 0 && this.selected >= 0) {
         this.assign(this.hoveredSlot, this.selected);
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      int ability = this.hovered >= 0 ? this.hovered : this.selected;
      if (keyCode >= 49 && keyCode <= 53 && ability >= 0) {
         this.assign(keyCode - 49, ability);
         return true;
      } else if (CullingClient.TECHNIQUE_MENU.matches(keyCode, scanCode)) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
