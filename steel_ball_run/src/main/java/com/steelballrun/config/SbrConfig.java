package com.steelballrun.config;

import com.steelballrun.horse.Stamina;
import com.steelballrun.race.Points;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/** Server config (serverconfig/steel_ball_run-server.toml). */
public final class SbrConfig {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue AUTO_START;
   public static final IntValue START_DAY;
   public static final IntValue COUNTDOWN_SECONDS;
   public static final DoubleValue ROUTE_SCALE;
   public static final IntValue START_OFFSET;
   public static final IntValue GATE_RADIUS;
   public static final ConfigValue<String> POINTS_TABLE;
   public static final IntValue FINISH_GRACE_SECONDS;
   public static final IntValue REGISTRATION_RADIUS;
   public static final BooleanValue BUILD_GATES;
   public static final IntValue RIVAL_COUNT;
   public static final DoubleValue RIVAL_SPEED;
   public static final DoubleValue RIVAL_NIGHT_FACTOR;
   public static final IntValue RIVALS_LOADED;
   public static final IntValue RIVAL_RANGE;
   public static final BooleanValue BAN_ELYTRA;
   public static final BooleanValue BAN_PEARLS;
   public static final BooleanValue BAN_CHORUS;
   public static final BooleanValue BAN_BOATS;
   public static final BooleanValue BAN_MINECARTS;
   public static final BooleanValue BAN_DIMENSIONS;
   public static final IntValue PENALTY;
   public static final BooleanValue STARTER_HORSES;
   public static final DoubleValue HORSE_STAMINA;
   public static final DoubleValue DRAIN;
   public static final DoubleValue TROT_REGEN;
   public static final DoubleValue REST_REGEN;
   public static final DoubleValue EXHAUSTED_REGEN;
   public static final DoubleValue RECOVER_FRACTION;
   public static final DoubleValue EXHAUSTED_SLOWDOWN;
   public static final DoubleValue RUNNER_SPEED;

   private static String pointsText;
   private static Points points;

   private SbrConfig() {
   }

   public static Points points() {
      String text = POINTS_TABLE.get();
      if (points == null || !text.equals(pointsText)) {
         pointsText = text;
         points = Points.parse(text);
      }
      return points;
   }

   public static Stamina.Tuning tuning() {
      return new Stamina.Tuning(DRAIN.get(), TROT_REGEN.get(), REST_REGEN.get(), EXHAUSTED_REGEN.get(), RECOVER_FRACTION.get());
   }

   static {
      Builder b = new Builder();
      b.push("race");
      AUTO_START = b.comment("Start the race on its own on world day startDay, once at least one rider has registered.").define("autoStart", true);
      START_DAY = b.comment("World day on which the race starts (counting from day 0).").defineInRange("startDay", 3, 0, 10000);
      COUNTDOWN_SECONDS = b.comment("Length of the countdown before the starting gun.").defineInRange("countdownSeconds", 10, 3, 120);
      ROUTE_SCALE = b.comment("Multiplies every stage length. 1.0 = about 12,000 blocks from San Diego to New York.")
         .defineInRange("routeScale", 1.0, 0.02, 20.0);
      START_OFFSET = b.comment("How far east of world spawn the start line is laid out, in blocks.").defineInRange("startOffset", 40, 0, 100000);
      GATE_RADIUS = b.comment("How close to a checkpoint gate you must ride to pass it.").defineInRange("gateRadius", 14, 4, 128);
      POINTS_TABLE = b.comment("Stage points by place: 1st, 2nd, 3rd, ... Places past the end score nothing.").define("pointsTable", Points.DEFAULT_TABLE);
      FINISH_GRACE_SECONDS = b.comment("Once every registered player has finished or retired, the race ends after this many seconds.")
         .defineInRange("finishGraceSeconds", 180, 0, 36000);
      REGISTRATION_RADIUS = b.comment("Players must be this close to Stephen Steel or the start line to register (operators can register anywhere).")
         .defineInRange("registrationRadius", 16, 1, 100000);
      BUILD_GATES = b.comment("Build the start line, grandstand, Race Office and the checkpoint gates into the world as players come near.")
         .define("buildGates", true);
      b.pop();
      b.push("rivals");
      RIVAL_COUNT = b.comment("Rival riders in the field. The first seven are the named contenders.").defineInRange("count", 24, 0, 24);
      RIVAL_SPEED = b.comment("Multiplies every rival's pace.").defineInRange("speedMultiplier", 1.0, 0.1, 5.0);
      RIVAL_NIGHT_FACTOR = b.comment("Rivals ride this much slower at night, when most riders rest.").defineInRange("nightFactor", 0.55, 0.0, 1.0);
      RIVALS_LOADED = b.comment("At most this many rivals are ridden in the world at once; the others are simulated.")
         .defineInRange("maxInWorld", 6, 0, 24);
      RIVAL_RANGE = b.comment("Rivals appear in the world when a player is this close to them.").defineInRange("appearRange", 112, 32, 256);
      b.pop();
      b.push("rules");
      BAN_ELYTRA = b.comment("Registered riders may not glide with elytra during the race.").define("banElytra", true);
      BAN_PEARLS = b.comment("Registered riders may not throw ender pearls during the race.").define("banEnderPearls", true);
      BAN_CHORUS = b.comment("Registered riders may not teleport with chorus fruit during the race.").define("banChorusFruit", true);
      BAN_BOATS = b.comment("Registered riders may not use boats during the race.").define("banBoats", true);
      BAN_MINECARTS = b.comment("Registered riders may not ride minecarts during the race.").define("banMinecarts", true);
      BAN_DIMENSIONS = b.comment("Registered riders may not cut through the Nether or the End during the race.").define("banOtherDimensions", true);
      PENALTY = b.comment("Points a race official takes away for each rule broken.").defineInRange("penaltyPoints", 25, 0, 10000);
      b.pop();
      b.push("horses");
      STARTER_HORSES = b.comment("Registering gives you a tamed horse (except Native Runners).").define("starterHorses", true);
      HORSE_STAMINA = b.comment("Average maximum stamina of a horse before bond.").defineInRange("baseStamina", 100.0, 10.0, 10000.0);
      DRAIN = b.comment("Stamina lost per second at a full gallop.").defineInRange("gallopDrain", 1.6, 0.0, 100.0);
      TROT_REGEN = b.comment("Stamina recovered per second while moving slowly.").defineInRange("trotRegen", 1.0, 0.0, 100.0);
      REST_REGEN = b.comment("Stamina recovered per second while standing still or unridden.").defineInRange("restRegen", 3.0, 0.0, 100.0);
      EXHAUSTED_REGEN = b.comment("Stamina recovered per second by an exhausted horse.").defineInRange("exhaustedRegen", 0.5, 0.0, 100.0);
      RECOVER_FRACTION = b.comment("An exhausted horse recovers once it is back to this fraction of its maximum.").defineInRange("recoverFraction", 0.5, 0.05, 1.0);
      EXHAUSTED_SLOWDOWN = b.comment("Speed lost by an exhausted horse (0.6 = 60% slower).").defineInRange("exhaustedSlowdown", 0.6, 0.0, 0.95);
      RUNNER_SPEED = b.comment("Extra on-foot speed for Native Runners while the race is on (0.35 = 35% faster). Running costs hunger instead of stamina.")
         .defineInRange("runnerSpeed", 0.35, 0.0, 3.0);
      b.pop();
      SPEC = b.build();
   }
}
