package com.curseddomain.cullinggame;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/** Server config for the Shibuya Incident and the Culling Game (cursed_domain-culling-server.toml). */
public final class CullingConfig {
   public static final ModConfigSpec SPEC;
   public static final IntValue DAY_TICKS;
   public static final IntValue DECLARE_DAYS;
   public static final IntValue STAGNATION_DAYS;
   public static final BooleanValue REMOVAL_STRIPS_TECHNIQUE;
   public static final BooleanValue DEATH_RESETS_POINTS;
   public static final IntValue RULE_COST;
   public static final IntValue COLONY_RADIUS;
   public static final IntValue COLONY_SPACING;
   public static final BooleanValue MARK_ON_LOGIN;
   public static final IntValue KASHIMO_RULE_DAY;
   public static final IntValue NO_NEW_PLAYERS_DAY;
   public static final IntValue ENDGAME_DAY;
   public static final IntValue MERGER_DAY;
   public static final IntValue GENERIC_NPCS_PER_COLONY;
   public static final IntValue COLONY_CURSE_CAP;
   public static final BooleanValue CURSE_SURGE;
   public static final DoubleValue CURSE_SURGE_CHANCE;
   public static final BooleanValue SHIBUYA_STARTS_CULLING_GAME;
   public static final IntValue CULLING_START_DELAY_SECONDS;
   public static final IntValue SHIBUYA_AUTO_START_DAY;
   public static final IntValue SHIBUYA_CURTAIN_RADIUS;
   public static final IntValue SHIBUYA_ABANDON_DAYS;
   public static final BooleanValue LANDMARKS;
   public static final IntValue LANDMARK_CHUNKS_PER_TICK;

   private CullingConfig() {
   }

   static {
      Builder b = new Builder();
      b.push("culling_game");
      DAY_TICKS = b.comment("Length of one Culling Game day in ticks. 24000 = one Minecraft day.").defineInRange("dayLengthTicks", 24000, 1200, 2400000);
      DECLARE_DAYS = b.comment("Rule 1: days a marked player has to declare participation at a colony.").defineInRange("declareDays", 19, 1, 365);
      STAGNATION_DAYS = b.comment("Rule 8: days a player's score may stay the same before cursed technique removal.").defineInRange("stagnationDays", 19, 1, 365);
      REMOVAL_STRIPS_TECHNIQUE = b.comment("Cursed technique removal always kills the player. If true it also deletes their innate technique.")
         .define("removalStripsTechnique", false);
      DEATH_RESETS_POINTS = b.comment("A player who dies while in the game loses every point they were holding.").define("deathResetsPoints", true);
      RULE_COST = b.comment("Rule 6: points a player spends to add a rule.").defineInRange("ruleCost", 100, 1, 100000);
      COLONY_RADIUS = b.comment("Radius of each colony barrier, in blocks.").defineInRange("colonyRadius", 112, 32, 1024);
      COLONY_SPACING = b.comment("Distance between neighbouring colonies along the barrier line.").defineInRange("colonySpacing", 512, 96, 20000);
      MARK_ON_LOGIN = b.comment("Non-sorcerers who join while the game runs are marked by Kenjaku and awakened.").define("markOnLogin", true);
      KASHIMO_RULE_DAY = b.comment("Day on which Hajime Kashimo adds rule 9 himself (0 = never).").defineInRange("kashimoRuleDay", 2, 0, 365);
      NO_NEW_PLAYERS_DAY = b.comment("Day on which Kenjaku adds the rule that no new players may join (0 = never).").defineInRange("noNewPlayersDay", 18, 0, 365);
      ENDGAME_DAY = b.comment("Day on which Kenjaku adds the end condition and starts culling at the Lake Gosho Colony (0 = never).")
         .defineInRange("endgameDay", 24, 0, 365);
      MERGER_DAY = b.comment("Day on which the Great Merger happens if Sukuna has not been defeated (0 = never).").defineInRange("mergerDay", 34, 0, 365);
      GENERIC_NPCS_PER_COLONY = b.comment("Unnamed awakened/incarnated players kept around a player inside a colony.").defineInRange("genericPlayersNearby", 3, 0, 20);
      COLONY_CURSE_CAP = b.comment("Cursed spirits kept around a player inside a colony.").defineInRange("colonyCurseCap", 8, 0, 40);
      CURSE_SURGE = b.comment("While the game runs, the curses Kenjaku released keep appearing near players at night.").define("curseSurge", true);
      CURSE_SURGE_CHANCE = b.comment("Chance per player per minute of a curse surge spawn at night.").defineInRange("curseSurgeChance", 0.5, 0.0, 1.0);
      b.pop();
      b.push("shibuya_incident");
      SHIBUYA_STARTS_CULLING_GAME = b.comment("When the Shibuya Incident ends, Kenjaku starts the Culling Game.").define("startsCullingGame", true);
      CULLING_START_DELAY_SECONDS = b.comment("Seconds between the end of the incident and Kenjaku's announcement.").defineInRange("cullingStartDelaySeconds", 60, 0, 86400);
      SHIBUYA_AUTO_START_DAY = b.comment("World day on which the curtain falls over Shibuya on its own, at nightfall (0 = only by command).")
         .defineInRange("autoStartDay", 31, 0, 100000);
      SHIBUYA_CURTAIN_RADIUS = b.comment("Radius of the curtain over Shibuya.").defineInRange("curtainRadius", 64, 24, 256);
      SHIBUYA_ABANDON_DAYS = b.comment("If no sorcerer enters the curtain within this many days, Kenjaku's plan succeeds without them.")
         .defineInRange("abandonDays", 2, 1, 100);
      b.pop();
      b.push("landmarks");
      LANDMARKS = b.comment("Build Jujutsu High (Tokyo and Kyoto), Shibuya, Tokyo, Kyoto, the cities and Kenjaku's hideout as players come near.")
         .define("enabled", true);
      LANDMARK_CHUNKS_PER_TICK = b.comment("Chunks of landmark built per server tick. Raise for faster building, lower if the server lags.")
         .defineInRange("chunksPerTick", 1, 1, 16);
      b.pop();
      SPEC = b.build();
   }

   public static int i(IntValue value) {
      return (Integer)value.get();
   }

   public static boolean b(BooleanValue value) {
      return (Boolean)value.get();
   }

   public static double d(DoubleValue value) {
      return (Double)value.get();
   }
}
