package com.curseddomain.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class ClientConfig {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue SHOW_ENERGY_BAR;
   public static final BooleanValue SHOW_ENERGY_NUMBERS;
   public static final EnumValue<HudAnchor> ENERGY_BAR_ANCHOR;
   public static final IntValue ENERGY_BAR_OFFSET_X;
   public static final IntValue ENERGY_BAR_OFFSET_Y;

   private ClientConfig() {
   }

   static {
      Builder b = new Builder();
      b.comment("Heads-up display.").push("hud");
      SHOW_ENERGY_BAR = b.comment("Show the cursed energy bar (only once you have awakened).").define("showEnergyBar", true);
      SHOW_ENERGY_NUMBERS = b.comment("Show current / max numbers next to the bar.").define("showEnergyNumbers", true);
      ENERGY_BAR_ANCHOR = b.comment("HOTBAR = a status row above the food / air bars. The corners show a larger bar with a label and your grade.")
         .defineEnum("energyBarAnchor", HudAnchor.HOTBAR);
      ENERGY_BAR_OFFSET_X = b.comment("Horizontal distance from the anchor corner, in GUI pixels (corner anchors only).")
         .defineInRange("energyBarOffsetX", 6, 0, 2000);
      ENERGY_BAR_OFFSET_Y = b.comment("Vertical distance from the anchor corner, in GUI pixels (corner anchors only).")
         .defineInRange("energyBarOffsetY", 6, 0, 2000);
      b.pop();
      SPEC = b.build();
   }
}
