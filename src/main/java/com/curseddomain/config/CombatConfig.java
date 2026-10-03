package com.curseddomain.config;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class CombatConfig {
   public static BooleanValue TECHNIQUES_NEED_ENROLLMENT;
   public static BooleanValue TECHNIQUES_BREAK_BLOCKS;
   public static IntValue EXHAUSTION_SECONDS;
   public static DoubleValue GRADE_DAMAGE_BONUS;
   public static BooleanValue DOMAIN_REQUIRES_UNLOCK;
   public static IntValue DOMAIN_CAST_TICKS;
   public static IntValue BURNOUT_SECONDS;
   public static IntValue CLASH_SECONDS;
   public static DoubleValue DOMAIN_DURATION_PER_ENERGY;
   public static IntValue DOMAIN_MAX_SECONDS;
   public static IntValue DOMAIN_SURE_HIT_INTERVAL;
   public static DoubleValue DOMAIN_BARRIER_HEALTH;

   private CombatConfig() {
   }

   static void define(Builder b) {
      b.comment("Rules shared by every innate technique. Per-ability numbers: datapack data/<ns>/ability_stats/<technique>/<ability>.json")
         .push("techniqueRules");
      TECHNIQUES_NEED_ENROLLMENT = b.comment("Innate techniques unlock at enrollment (Jujutsu High student or curse user). False = as soon as you awaken.")
         .define("techniquesNeedEnrollment", true);
      TECHNIQUES_BREAK_BLOCKS = b.comment("Destructive techniques (Hollow Purple, Collapse, eruptions...) may break terrain.")
         .define("techniquesBreakBlocks", true);
      EXHAUSTION_SECONDS = b.comment("Running out of cursed energy exhausts you: no techniques and slowness for this long.")
         .defineInRange("exhaustionSeconds", 10, 0, 600);
      GRADE_DAMAGE_BONUS = b.comment("Extra technique damage per grade above Grade 4 (0.12 = +12% per grade).")
         .defineInRange("gradeDamageBonus", 0.12, 0.0, 5.0);
      b.pop();
      b.comment("Domain expansion.").push("domains");
      DOMAIN_REQUIRES_UNLOCK = b.comment(
            "A domain needs the 'domain' unlock (Grade 1 trial); without it players get the incomplete domain only if they have 'domain_incomplete'."
         )
         .define("domainRequiresUnlock", true);
      DOMAIN_CAST_TICKS = b.comment("Hand-sign wind-up before the barrier closes (interrupted by taking heavy damage).").defineInRange("castTicks", 30, 0, 200);
      BURNOUT_SECONDS = b.comment("After a domain ends, the innate technique is burnt out for this long.").defineInRange("burnoutSeconds", 40, 0, 600);
      CLASH_SECONDS = b.comment("Length of a domain clash before the weaker domain collapses.").defineInRange("clashSeconds", 5, 1, 60);
      DOMAIN_DURATION_PER_ENERGY = b.comment("Seconds of domain per point of cursed energy left after casting.")
         .defineInRange("durationPerEnergy", 0.06, 0.0, 10.0);
      DOMAIN_MAX_SECONDS = b.comment("Hard cap on a domain's duration.").defineInRange("maxSeconds", 40, 5, 600);
      DOMAIN_SURE_HIT_INTERVAL = b.comment("Ticks between sure-hit applications.").defineInRange("sureHitIntervalTicks", 10, 1, 100);
      DOMAIN_BARRIER_HEALTH = b.comment("Damage a closed barrier takes from outside before it shatters.").defineInRange("barrierHealth", 120.0, 1.0, 100000.0);
      b.pop();
   }
}
