package com.steelballrun.horse;

/**
 * Horse stamina rules. Pure maths so it can be tested outside the game.
 *
 * <p>Horses ridden by players have one speed: full or nothing. So galloping (more than 70% of the horse's top speed)
 * drains stamina, anything slower recovers it, and standing still recovers it fastest. A horse that hits zero is
 * <em>exhausted</em>: it is slowed down and recovers slowly until it is back to {@code recoverFraction} of its maximum.
 * Pulling up to rest before that happens is the better strategy.
 */
public final class Stamina {
   public static final double GALLOP = 0.7;
   public static final double WALK = 0.3;

   /** All per-second rates. */
   public record Tuning(double drain, double trotRegen, double restRegen, double exhaustedRegen, double recoverFraction) {
      public static final Tuning DEFAULT = new Tuning(1.6, 1.0, 3.0, 0.5, 0.5);
   }

   /** Result of one update. */
   public record State(double stamina, boolean exhausted) {
   }

   private Stamina() {
   }

   /**
    * @param ratio     current speed divided by the horse's top speed
    * @param drainMult multiplier on drain (terrain affinity, jockey, bond)
    * @param regenMult multiplier on recovery (endurance)
    */
   public static State step(double stamina, double max, boolean exhausted, double ratio, double seconds, double drainMult, double regenMult, Tuning t) {
      double s = stamina;
      if (exhausted) {
         s += t.exhaustedRegen() * regenMult * seconds * (ratio < WALK ? 2.0 : 1.0);
      } else if (ratio > GALLOP) {
         s -= t.drain() * drainMult * seconds * Math.min(1.5, ratio);
      } else if (ratio > WALK) {
         s += t.trotRegen() * regenMult * seconds;
      } else {
         s += t.restRegen() * regenMult * seconds;
      }
      s = Math.max(0.0, Math.min(max, s));
      boolean ex = exhausted;
      if (!ex && s <= 0.0) {
         ex = true;
      } else if (ex && s >= max * t.recoverFraction()) {
         ex = false;
      }
      return new State(s, ex);
   }

   /** Recovery while nobody rides the horse, applied in one go when someone mounts it again. */
   public static double restWhileUnridden(double stamina, double max, double seconds, double regenMult, Tuning t) {
      return Math.min(max, stamina + Math.max(0.0, seconds) * t.restRegen() * regenMult);
   }

   /** Bond (0-100) raises maximum stamina by up to 50%. */
   public static double maxFor(double base, int bond) {
      return base * (1.0 + Math.max(0, Math.min(100, bond)) / 200.0);
   }

   /** Bond (0-100) cuts drain by up to 25%. */
   public static double bondDrain(int bond) {
      return 1.0 - Math.max(0, Math.min(100, bond)) / 400.0;
   }
}
