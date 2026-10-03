package com.curseddomain.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public final class CommonConfig {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue DEBUG_LOGGING;

   private CommonConfig() {
   }

   static {
      Builder b = new Builder();
      b.comment("Diagnostics.").push("debug");
      DEBUG_LOGGING = b.comment("Log sorcerer data changes and energy syncs (secret technique rolls included) to the server log.")
         .define("debugLogging", false);
      b.pop();
      SPEC = b.build();
   }
}
