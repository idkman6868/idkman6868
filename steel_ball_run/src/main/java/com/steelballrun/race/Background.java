package com.steelballrun.race;

import java.util.Locale;

/** What kind of rider you entered as. Small perks only; none of them replace progression. */
public enum Background {
   /** Horse stamina drains 15% slower and starts with a better horse. */
   JOCKEY,
   /** Starts with a horse you already have a bond with. Spin arrives in 0.2.0. */
   ZEPPELI_APPRENTICE,
   /** Starts with leads and a little money; good with horses that aren't theirs. */
   COWBOY,
   /** Runs the race on foot, Sandman style: much faster unmounted, no starter horse. */
   NATIVE_RUNNER,
   /** A random horse that might be great or terrible, and more money. */
   WANDERER;

   public static final Background[] ALL = values();

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public static Background byId(String id) {
      for (Background b : ALL) {
         if (b.id().equals(id)) {
            return b;
         }
      }
      return null;
   }

   public String langKey() {
      return "background.steel_ball_run." + this.id();
   }

   public String descKey() {
      return "background.steel_ball_run." + this.id() + ".desc";
   }
}
