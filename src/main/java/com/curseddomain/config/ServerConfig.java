package com.curseddomain.config;

import com.curseddomain.sorcerer.Grade;
import com.curseddomain.technique.TechniqueRarity;
import com.curseddomain.world.SchoolSpawnMode;
import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class ServerConfig {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue REQUIRE_JUJUTSU_HIGH_ENROLLMENT;
   public static final BooleanValue YAGA_INVULNERABLE;
   public static final BooleanValue ALLOW_CURSE_USER_PATH;
   public static final IntValue CURSE_HITS_TO_AWAKEN;
   public static final DoubleValue NEAR_DEATH_HEALTH;
   public static final IntValue DREAD_SECONDS;
   public static final DoubleValue LETTER_HINT_REFRESH_MINUTES;
   public static final DoubleValue LETTER_HINT_JITTER_DEGREES;
   public static final DoubleValue LETTER_COOLDOWN_SECONDS;
   public static final DoubleValue SEAL_BREAK_CHANCE_PER_MINUTE;
   public static final IntValue SEAL_BREAK_RADIUS;
   public static final DoubleValue SIX_EYES_CHANCE;
   public static final BooleanValue SIX_EYES_WITH_LIMITLESS;
   public static final DoubleValue VESSEL_CHANCE;
   public static final DoubleValue HR_ENERGY_CHANCE;
   public static final DoubleValue DEATH_PAINTING_CHANCE;
   public static final DoubleValue HR_ENERGY_HEALTH_PENALTY;
   public static final EnumValue<SchoolSpawnMode> JUJUTSU_HIGH_SPAWN_MODE;
   public static final IntValue JUJUTSU_HIGH_MIN_DISTANCE;
   public static final IntValue JUJUTSU_HIGH_MAX_DISTANCE;
   public static final IntValue JUJUTSU_HIGH_FIXED_X;
   public static final IntValue JUJUTSU_HIGH_FIXED_Z;
   public static final BooleanValue ALLOW_NPC_ONLY_TECHNIQUES;
   public static final BooleanValue CURSE_TECHNIQUES_IN_ROLL;
   public static final BooleanValue TECHNIQUE_REROLL_ALLOWED;
   public static final Map<TechniqueRarity, IntValue> RARITY_WEIGHTS = new EnumMap<>(TechniqueRarity.class);
   public static final BooleanValue DOMAINS_BREAK_BLOCKS;
   public static final BooleanValue ALLOW_SUKUNA_TAKEOVER;
   public static final DoubleValue PVP_TECHNIQUE_DAMAGE_MULTIPLIER;
   public static final IntValue MAX_SPECIAL_GRADE_PLAYERS;
   public static final DoubleValue TECHNIQUE_DAMAGE_MULTIPLIER;
   public static final DoubleValue TECHNIQUE_COST_MULTIPLIER;
   public static final DoubleValue TECHNIQUE_COOLDOWN_MULTIPLIER;
   public static final DoubleValue CONTROL_MAX_COST_REDUCTION;
   public static final DoubleValue SIX_EYES_COST_MULTIPLIER;
   public static final IntValue COMBAT_TAG_SECONDS;
   public static final DoubleValue OUT_OF_COMBAT_REGEN_MULTIPLIER;
   public static final DoubleValue MEDITATION_REGEN_MULTIPLIER;
   public static final DoubleValue MEDITATION_DELAY_SECONDS;
   public static final DoubleValue RESPAWN_ENERGY_FRACTION;
   public static final IntValue SYNC_INTERVAL_TICKS;
   public static final DoubleValue HR_ENERGY_POOL_MULTIPLIER;
   public static final DoubleValue HR_ENERGY_REGEN_MULTIPLIER;
   public static final DoubleValue HR_ENERGY_OUTPUT_MULTIPLIER;
   private static final Map<Grade, ServerConfig.GradeStats> GRADE_STATS = new EnumMap<>(Grade.class);

   private ServerConfig() {
   }

   public static ServerConfig.GradeStats gradeStats(Grade grade) {
      return GRADE_STATS.get(grade);
   }

   private static double[] gradeDefaults(Grade grade) {
      return switch (grade) {
         case UNGRADED -> new double[]{40.0, 1.0, 10.0, 0.0};
         case GRADE_4 -> new double[]{100.0, 2.0, 25.0, 0.1};
         case GRADE_3 -> new double[]{160.0, 2.8, 40.0, 0.2};
         case GRADE_2 -> new double[]{250.0, 3.8, 60.0, 0.3};
         case SEMI_GRADE_1 -> new double[]{350.0, 5.0, 85.0, 0.4};
         case GRADE_1 -> new double[]{500.0, 6.5, 120.0, 0.5};
         case SPECIAL_GRADE -> new double[]{800.0, 9.0, 200.0, 0.65};
      };
   }

   static {
      Builder b = new Builder();
      b.comment("The story start: finding Jujutsu High and Principal Yaga's test.").push("story");
      REQUIRE_JUJUTSU_HIGH_ENROLLMENT = b.comment(
            "Players start as ordinary people and must enroll at Jujutsu High. False = skip the story and start as a Grade 4 student."
         )
         .define("requireJujutsuHighEnrollment", true);
      YAGA_INVULNERABLE = b.comment("Principal Yaga cannot be hurt.").define("yagaInvulnerable", true);
      ALLOW_CURSE_USER_PATH = b.comment("Players may become curse users (attack Yaga or accept the hidden recruiter's offer).")
         .define("allowCurseUserPath", true);
      CURSE_HITS_TO_AWAKEN = b.comment("Hits from curses an ordinary person takes before awakening.").defineInRange("curseHitsToAwaken", 5, 1, 100);
      NEAR_DEATH_HEALTH = b.comment("A curse attack that leaves an ordinary person at or below this health awakens them.")
         .defineInRange("nearDeathHealth", 3.0, 0.0, 20.0);
      DREAD_SECONDS = b.comment("Nausea after an unseen curse hits an ordinary person.").defineInRange("dreadSeconds", 4, 0, 60);
      LETTER_HINT_REFRESH_MINUTES = b.comment("Minutes before the Recruitment Letter's direction hint changes its (deliberately vague) angle.")
         .defineInRange("letterHintRefreshMinutes", 5.0, 0.0, 120.0);
      LETTER_HINT_JITTER_DEGREES = b.comment("Maximum error, in degrees, of the letter's direction hint.")
         .defineInRange("letterHintJitterDegrees", 30.0, 0.0, 90.0);
      LETTER_COOLDOWN_SECONDS = b.comment("Cooldown between uses of the Recruitment Letter.").defineInRange("letterCooldownSeconds", 10.0, 0.0, 600.0);
      SEAL_BREAK_CHANCE_PER_MINUTE = b.comment("Chance per player per minute, at night, that a sealed finger's seal breaks nearby (rare world event).")
         .defineInRange("sealBreakChancePerMinute", 0.002, 0.0, 1.0);
      SEAL_BREAK_RADIUS = b.comment("Ordinary people within this many blocks of a breaking seal awaken.").defineInRange("sealBreakRadius", 24, 1, 256);
      b.pop();
      b.comment("Innate traits rolled in secret on first join.").push("traits");
      SIX_EYES_CHANCE = b.comment("Chance of Six Eyes (independent of the technique).").defineInRange("sixEyesChance", 0.01, 0.0, 1.0);
      SIX_EYES_WITH_LIMITLESS = b.comment("Rolling Limitless always comes with Six Eyes.").define("sixEyesWithLimitless", true);
      VESSEL_CHANCE = b.comment("Chance of being a Vessel.").defineInRange("vesselChance", 0.005, 0.0, 1.0);
      HR_ENERGY_CHANCE = b.comment("Chance of Heavenly Restriction (cursed energy).").defineInRange("heavenlyRestrictionEnergyChance", 0.03, 0.0, 1.0);
      DEATH_PAINTING_CHANCE = b.comment("Chance of being a Death Painting incarnation.").defineInRange("deathPaintingChance", 0.02, 0.0, 1.0);
      HR_ENERGY_HEALTH_PENALTY = b.comment("Max health lost by Heavenly Restriction (cursed energy) users.")
         .defineInRange("heavenlyRestrictionEnergyHealthPenalty", 6.0, 0.0, 19.0);
      b.pop();
      b.comment("Placement of Tokyo Jujutsu High.").push("world");
      JUJUTSU_HIGH_SPAWN_MODE = b.comment("NEAR_SPAWN = between min and max distance from world spawn, RANDOM = anywhere, FIXED_COORDS = at fixedX / fixedZ.")
         .defineEnum("jujutsuHighSpawnMode", SchoolSpawnMode.NEAR_SPAWN);
      JUJUTSU_HIGH_MIN_DISTANCE = b.comment("Minimum distance in blocks from world spawn (NEAR_SPAWN).")
         .defineInRange("jujutsuHighMinDistance", 800, 0, 100000);
      JUJUTSU_HIGH_MAX_DISTANCE = b.comment("Maximum distance in blocks from world spawn (NEAR_SPAWN).")
         .defineInRange("jujutsuHighMaxDistance", 2000, 0, 100000);
      JUJUTSU_HIGH_FIXED_X = b.comment("X coordinate for FIXED_COORDS.").defineInRange("jujutsuHighFixedX", 0, -30000000, 30000000);
      JUJUTSU_HIGH_FIXED_Z = b.comment("Z coordinate for FIXED_COORDS.").defineInRange("jujutsuHighFixedZ", 0, -30000000, 30000000);
      b.pop();
      b.comment("Innate technique rolls.").push("techniques");
      ALLOW_NPC_ONLY_TECHNIQUES = b.comment("Let players roll and use story/boss-only techniques (Body Swap, Cockroach Swarm, Smallpox Deity...).")
         .define("allowNpcOnlyTechniques", false);
      CURSE_TECHNIQUES_IN_ROLL = b.comment("Include cursed-spirit and curse-user techniques in the normal first-join roll.")
         .define("curseTechniquesInRoll", false);
      TECHNIQUE_REROLL_ALLOWED = b.comment("Players may reroll their innate technique (once, through a story item).").define("techniqueRerollAllowed", false);
      b.comment("Relative roll weight of each rarity.").push("rarityWeights");

      for (TechniqueRarity rarity : TechniqueRarity.values()) {
         RARITY_WEIGHTS.put(rarity, b.defineInRange(rarity.getSerializedName(), rarity.defaultWeight(), 0, 10000));
      }

      b.pop();
      b.pop();
      b.comment("Combat and destruction rules.").push("combat");
      DOMAINS_BREAK_BLOCKS = b.comment("Domain expansions and the biggest techniques may destroy terrain.").define("domainsBreakBlocks", true);
      ALLOW_SUKUNA_TAKEOVER = b.comment("A Vessel who eats too many fingers can lose control to Sukuna.").define("allowSukunaTakeover", true);
      PVP_TECHNIQUE_DAMAGE_MULTIPLIER = b.comment("Multiplier on technique damage dealt by players to players.")
         .defineInRange("pvpTechniqueDamageMultiplier", 0.75, 0.0, 10.0);
      MAX_SPECIAL_GRADE_PLAYERS = b.comment("How many players can hold Special Grade at once. 0 = unlimited.")
         .defineInRange("maxSpecialGradePlayers", 0, 0, 1000);
      TECHNIQUE_DAMAGE_MULTIPLIER = b.comment("Multiplier on all technique damage.").defineInRange("techniqueDamageMultiplier", 1.0, 0.0, 100.0);
      TECHNIQUE_COST_MULTIPLIER = b.comment("Multiplier on all technique energy costs.").defineInRange("techniqueCostMultiplier", 1.0, 0.0, 100.0);
      TECHNIQUE_COOLDOWN_MULTIPLIER = b.comment("Multiplier on all technique cooldowns.").defineInRange("techniqueCooldownMultiplier", 1.0, 0.0, 100.0);
      b.pop();
      CombatConfig.define(b);
      b.comment("Cursed energy: regeneration, costs and per-grade stats.").push("energy");
      CONTROL_MAX_COST_REDUCTION = b.comment("Cost reduction at control 1.0. Cost multiplier = 1 - control * this.")
         .defineInRange("controlMaxCostReduction", 0.5, 0.0, 1.0);
      SIX_EYES_COST_MULTIPLIER = b.comment("Extra cost multiplier for Six Eyes users (near-zero costs).").defineInRange("sixEyesCostMultiplier", 0.1, 0.0, 1.0);
      COMBAT_TAG_SECONDS = b.comment("Seconds after dealing or taking damage during which a player counts as in combat.")
         .defineInRange("combatTagSeconds", 6, 0, 600);
      OUT_OF_COMBAT_REGEN_MULTIPLIER = b.comment("Regeneration multiplier while out of combat.").defineInRange("outOfCombatRegenMultiplier", 1.5, 0.0, 100.0);
      MEDITATION_REGEN_MULTIPLIER = b.comment("Extra regeneration multiplier while meditating (crouching still, out of combat).")
         .defineInRange("meditationRegenMultiplier", 3.0, 0.0, 100.0);
      MEDITATION_DELAY_SECONDS = b.comment("Seconds of crouching still before meditation starts.").defineInRange("meditationDelaySeconds", 3.0, 0.0, 60.0);
      RESPAWN_ENERGY_FRACTION = b.comment("Fraction of max cursed energy a player respawns with.").defineInRange("respawnEnergyFraction", 1.0, 0.0, 1.0);
      SYNC_INTERVAL_TICKS = b.comment("Minimum ticks between energy HUD syncs while regenerating. Spending energy always syncs on the next tick.")
         .defineInRange("syncIntervalTicks", 5, 1, 100);
      HR_ENERGY_POOL_MULTIPLIER = b.comment("Max cursed energy multiplier for Heavenly Restriction (energy) users.")
         .defineInRange("heavenlyRestrictionEnergyPoolMultiplier", 3.0, 1.0, 100.0);
      HR_ENERGY_REGEN_MULTIPLIER = b.comment("Regeneration multiplier for Heavenly Restriction (energy) users.")
         .defineInRange("heavenlyRestrictionEnergyRegenMultiplier", 1.5, 1.0, 100.0);
      HR_ENERGY_OUTPUT_MULTIPLIER = b.comment("Output multiplier for Heavenly Restriction (energy) users.")
         .defineInRange("heavenlyRestrictionEnergyOutputMultiplier", 1.5, 1.0, 100.0);
      b.comment("Base stats per grade. 'ungraded' is an awakened player who has not enrolled yet.").push("grades");

      for (Grade grade : Grade.values()) {
         double[] d = gradeDefaults(grade);
         b.push(grade.getSerializedName());
         GRADE_STATS.put(
            grade,
            new ServerConfig.GradeStats(
               b.comment("Maximum cursed energy.").defineInRange("maxEnergy", d[0], 0.0, 1000000.0),
               b.comment("Cursed energy regenerated per second in combat (before multipliers).").defineInRange("regenPerSecond", d[1], 0.0, 10000.0),
               b.comment("Most cursed energy that can be spent in one burst.").defineInRange("output", d[2], 0.0, 1000000.0),
               b.comment("Control (0-1). Lowers every cost.").defineInRange("control", d[3], 0.0, 1.0)
            )
         );
         b.pop();
      }

      b.pop();
      b.pop();
      SPEC = b.build();
   }

   public record GradeStats(DoubleValue maxEnergy, DoubleValue regenPerSecond, DoubleValue output, DoubleValue control) {
   }
}
