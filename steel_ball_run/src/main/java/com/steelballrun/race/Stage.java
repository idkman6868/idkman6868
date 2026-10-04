package com.steelballrun.race;

/**
 * The nine stages of the race, in order. Each stage ends at a checkpoint gate; gate 0 is the start line at San Diego
 * Beach and gate {@code n} is the end of stage {@code n}. Lengths are in blocks at {@code routeScale = 1.0}.
 */
public enum Stage {
   SAN_DIEGO_BEACH("san_diego_beach", 300),
   ARIZONA_DESERT("arizona_desert", 1800),
   MONUMENT_VALLEY("monument_valley", 1500),
   ROCKY_MOUNTAINS("rocky_mountains", 1700),
   GREAT_PLAINS("great_plains", 1700),
   MISSISSIPPI_RIVER("mississippi_river", 1400),
   LAKE_MICHIGAN("lake_michigan", 1300),
   PHILADELPHIA("philadelphia", 1200),
   NEW_YORK("new_york", 1100);

   public static final Stage[] ALL = values();
   /** Gates are the start line plus one at the end of every stage. */
   public static final int GATES = ALL.length + 1;

   private final String id;
   private final int length;

   Stage(String id, int length) {
      this.id = id;
      this.length = length;
   }

   public String id() {
      return this.id;
   }

   public int length() {
      return this.length;
   }

   /** The stage that a racer whose next gate is {@code gate} is riding (gate 0 means they are still at the start). */
   public static Stage forNextGate(int gate) {
      return ALL[Math.max(0, Math.min(ALL.length - 1, gate - 1))];
   }

   public String langKey() {
      return "stage.steel_ball_run." + this.id;
   }
}
