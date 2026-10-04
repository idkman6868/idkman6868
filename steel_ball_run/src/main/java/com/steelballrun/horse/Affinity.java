package com.steelballrun.horse;

import java.util.Locale;

/** The kind of ground a horse rides best on. In its terrain it drains 25% less stamina. */
public enum Affinity {
   DESERT,
   PLAINS,
   MOUNTAIN,
   SNOW;

   public static final Affinity[] ALL = values();

   public String langKey() {
      return "affinity.steel_ball_run." + this.name().toLowerCase(Locale.ROOT);
   }

   /** Terrain from biome temperature and height: hot is desert, freezing is snow, high ground is mountain. */
   public static Affinity of(float temperature, int y) {
      if (temperature <= 0.15F) {
         return SNOW;
      } else if (y >= 110) {
         return MOUNTAIN;
      } else {
         return temperature >= 1.5F ? DESERT : PLAINS;
      }
   }
}
