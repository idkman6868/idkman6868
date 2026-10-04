package com.steelballrun.npc;

/**
 * The other riders in the race. The named ones are the manga's contenders; the rest of the field are original
 * characters. {@code speed} is the rival's average pace in blocks per second over a whole stage (rests included).
 */
public final class Rivals {
   public record Profile(String id, String name, double speed, boolean onFoot, String horse, String skin) {
      public boolean canon() {
         return !this.skin.startsWith("generic");
      }
   }

   public static final Profile[] ALL = new Profile[]{
      new Profile("gyro_zeppeli", "Gyro Zeppeli", 6.3, false, "Valkyrie", "gyro_zeppeli"),
      new Profile("johnny_joestar", "Johnny Joestar", 6.1, false, "Slow Dancer", "johnny_joestar"),
      new Profile("diego_brando", "Diego Brando", 6.5, false, "Silver Bullet", "diego_brando"),
      new Profile("sandman", "Sandman", 6.4, true, "", "sandman"),
      new Profile("pocoloco", "Pocoloco", 6.0, false, "", "pocoloco"),
      new Profile("hot_pants", "Hot Pants", 5.9, false, "", "hot_pants"),
      new Profile("mountain_tim", "Mountain Tim", 5.8, false, "", "mountain_tim"),
      new Profile("ezra_holt", "Ezra Holt", 5.6, false, "", "generic_0"),
      new Profile("silas_crane", "Silas Crane", 5.4, false, "", "generic_1"),
      new Profile("ingrid_larsen", "Ingrid Larsen", 5.5, false, "", "generic_2"),
      new Profile("lorenzo_vega", "Lorenzo Vega", 5.3, false, "", "generic_3"),
      new Profile("amos_whitfield", "Amos Whitfield", 5.2, false, "", "generic_0"),
      new Profile("beatrice_hale", "Beatrice Hale", 5.5, false, "", "generic_2"),
      new Profile("cyrus_bell", "Cyrus Bell", 5.0, false, "", "generic_1"),
      new Profile("wyatt_kincaid", "Wyatt Kincaid", 5.4, false, "", "generic_3"),
      new Profile("thaddeus_rowe", "Thaddeus Rowe", 4.8, false, "", "generic_0"),
      new Profile("henrietta_cole", "Henrietta Cole", 5.1, false, "", "generic_2"),
      new Profile("clement_dupree", "Clement Dupree", 4.9, false, "", "generic_1"),
      new Profile("otis_marsh", "Otis Marsh", 4.6, false, "", "generic_3"),
      new Profile("august_brandt", "August Brandt", 5.0, false, "", "generic_0"),
      new Profile("rufus_tate", "Rufus Tate", 4.5, false, "", "generic_1"),
      new Profile("jebediah_moss", "Jebediah Moss", 4.4, false, "", "generic_3"),
      new Profile("ignatius_ford", "Ignatius Ford", 4.7, false, "", "generic_0"),
      new Profile("elijah_stone", "Elijah Stone", 4.3, false, "", "generic_2")
   };

   public static final String[] SKINS = {
      "gyro_zeppeli", "johnny_joestar", "diego_brando", "sandman", "pocoloco", "hot_pants", "mountain_tim",
      "generic_0", "generic_1", "generic_2", "generic_3"
   };

   private Rivals() {
   }

   public static Profile get(int index) {
      return ALL[Math.max(0, Math.min(ALL.length - 1, index))];
   }

   public static int skinIndex(Profile p) {
      for (int i = 0; i < SKINS.length; i++) {
         if (SKINS[i].equals(p.skin())) {
            return i;
         }
      }
      return 0;
   }
}
