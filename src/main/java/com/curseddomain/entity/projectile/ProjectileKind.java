package com.curseddomain.entity.projectile;

public enum ProjectileKind {
   BLUE(-13739009),
   RED(-54742),
   HOLLOW_PURPLE(-6274817),
   DIVINE_FLAME(-34278),
   DISMANTLE(-986881),
   WORLD_SLASH(-1),
   NAIL(-4671288),
   BLOOD_DISC(-4190182),
   BLOOD_SPEAR(-4190182),
   EMBER_INSECT(-30166),
   CONSTRUCTED_BULLET(-2561793),
   METEOR(-42470),
   ENERGY_BOLT(-6301441),
   ICE_SHARD(-4656897),
   WATER_FISH(-12932888),
   CROW(-14671832),
   ELECTRIC(-6297345),
   PLANT_SPEAR(-10843606),
   SOUL_SPIKE(-7710032),
   GUITAR_WAVE(-10166),
   MARKER_STONE(-5723992);

   private final int color;

   private ProjectileKind(int color) {
      this.color = color;
   }

   public int color() {
      return this.color;
   }
}
