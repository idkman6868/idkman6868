package com.steelballrun.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

/** Client config (config/steel_ball_run-client.toml). */
public final class SbrClientConfig {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue RACE_HUD;
   public static final BooleanValue RACE_HUD_RIGHT;
   public static final BooleanValue HORSE_HUD;

   private SbrClientConfig() {
   }

   static {
      Builder b = new Builder();
      RACE_HUD = b.comment("Show the race panel (stage, place, next checkpoint, race clock).").define("raceHud", true);
      RACE_HUD_RIGHT = b.comment("Put the race panel in the top-right corner instead of the top-left.").define("raceHudRight", false);
      HORSE_HUD = b.comment("Show your horse's stamina bar while riding.").define("horseHud", true);
      SPEC = b.build();
   }
}
