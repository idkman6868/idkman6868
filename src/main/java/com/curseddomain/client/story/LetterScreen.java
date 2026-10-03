package com.curseddomain.client.story;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class LetterScreen extends Screen {
   private static final int PAPER_WIDTH = 236;
   private static final int PAPER = -923956;
   private static final int PAPER_EDGE = -3558262;
   private static final int INK = -13949920;
   private static final int SEAL = -6415589;
   private static final int HEADER = 30;
   private final boolean firstRead;
   private final List<FormattedCharSequence> lines = new ArrayList<>();
   private int paperX;
   private int paperY;
   private int paperHeight;
   private int visibleLines;
   private int scroll;

   public LetterScreen(boolean firstRead) {
      super(Component.translatable("letter.cursed_domain.title"));
      this.firstRead = firstRead;
   }

   protected void init() {
      this.lines.clear();
      int textWidth = 208;

      for (String paragraph : Component.translatable("letter.cursed_domain.body").getString().split("\n")) {
         this.lines.addAll(this.font.split(Component.literal(paragraph), textWidth));
         this.lines.add(FormattedCharSequence.EMPTY);
      }

      this.lines.addAll(this.font.split(Component.translatable("letter.cursed_domain.signature"), textWidth));
      if (this.firstRead) {
         this.lines.add(FormattedCharSequence.EMPTY);
         this.lines.addAll(this.font.split(Component.translatable("letter.cursed_domain.applicant"), textWidth));
      }

      int lineHeight = 9 + 1;
      int maxPaper = this.height - 40;
      int wanted = 30 + this.lines.size() * lineHeight + 12;
      this.paperHeight = Math.min(wanted, maxPaper);
      this.visibleLines = Math.max(1, (this.paperHeight - 30 - 12) / lineHeight);
      this.scroll = Mth.clamp(this.scroll, 0, Math.max(0, this.lines.size() - this.visibleLines));
      this.paperX = (this.width - 236) / 2;
      this.paperY = Math.max(6, (this.height - this.paperHeight - 28) / 2);
      this.addRenderableWidget(
         Button.builder(Component.translatable("letter.cursed_domain.close"), b -> this.onClose())
            .bounds(this.width / 2 - 50, this.paperY + this.paperHeight + 6, 100, 20)
            .build()
      );
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      this.scroll = Mth.clamp(this.scroll - (int)Math.signum(scrollY) * 2, 0, Math.max(0, this.lines.size() - this.visibleLines));
      return true;
   }

   public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(g, mouseX, mouseY, partialTick);
      g.fill(this.paperX - 2, this.paperY - 2, this.paperX + 236 + 2, this.paperY + this.paperHeight + 2, -3558262);
      g.fill(this.paperX, this.paperY, this.paperX + 236, this.paperY + this.paperHeight, -923956);
      int sx = this.paperX + 236 - 12;
      int sy = this.paperY - 8;
      g.fill(sx + 3, sy, sx + 17, sy + 20, -6415589);
      g.fill(sx, sy + 3, sx + 20, sy + 17, -6415589);
      g.fill(sx + 7, sy + 6, sx + 13, sy + 14, -3585478);
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      super.render(g, mouseX, mouseY, partialTick);
      int x = this.paperX + 14;
      g.drawString(this.font, this.title, x, this.paperY + 10, -6415589, false);
      int y = this.paperY + 30;
      int end = Math.min(this.lines.size(), this.scroll + this.visibleLines);

      for (int i = this.scroll; i < end; i++) {
         g.drawString(this.font, this.lines.get(i), x, y, -13949920, false);
         y += 9 + 1;
      }

      if (end < this.lines.size()) {
         g.drawCenteredString(this.font, Component.literal("▼"), this.paperX + 118, this.paperY + this.paperHeight - 10, -7706048);
      }

      if (this.scroll > 0) {
         g.drawCenteredString(this.font, Component.literal("▲"), this.paperX + 118, this.paperY + 30 - 10, -7706048);
      }
   }
}
