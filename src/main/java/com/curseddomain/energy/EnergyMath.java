package com.curseddomain.energy;

public final class EnergyMath {
   private static final float EPSILON = 1.0E-4F;

   private EnergyMath() {
   }

   public static float costMultiplier(float control, float maxReduction, boolean sixEyes, float sixEyesMultiplier) {
      float c = clamp01(control);
      float multiplier = 1.0F - c * clamp01(maxReduction);
      if (sixEyes) {
         multiplier *= clamp01(sixEyesMultiplier);
      }

      return Math.max(0.0F, multiplier);
   }

   public static float regenAmount(
      float regenPerSecond, float seconds, boolean inCombat, boolean meditating, float outOfCombatMultiplier, float meditationMultiplier
   ) {
      float rate = regenPerSecond;
      if (!inCombat) {
         rate = regenPerSecond * outOfCombatMultiplier;
      }

      if (meditating) {
         rate *= meditationMultiplier;
      }

      return Math.max(0.0F, rate * seconds);
   }

   public static boolean withinOutput(float cost, float output) {
      return cost <= output + 1.0E-4F;
   }

   public static boolean affordable(float cost, float current, float output) {
      return withinOutput(cost, output) && cost <= current + 1.0E-4F;
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }
}
