package com.curseddomain.cullinggame;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * The ten colonies, laid out in a line like the barriers running down the archipelago (Hokkaido is left out).
 * Tokyo No. 1 sits on the anchor, which is where the Shibuya Incident happened.
 */
public final class Colonies {
   public static final String TOKYO_1 = "tokyo_1";
   public static final String TOKYO_2 = "tokyo_2";
   public static final String SENDAI = "sendai";
   public static final String SAKURAJIMA = "sakurajima";
   public static final String LAKE_GOSHO = "lake_gosho";
   private static final String[] KEYS = new String[]{
      SAKURAJIMA, "colony_6", "colony_7", LAKE_GOSHO, "colony_8", "colony_9", TOKYO_2, TOKYO_1, "colony_10", SENDAI
   };
   private static final int ANCHOR_INDEX = 7;

   private Colonies() {
   }

   public static List<Colony> layout(BlockPos anchor, double angle, int spacing, int radius) {
      List<Colony> list = new ArrayList<>();
      double dx = Math.cos(angle);
      double dz = Math.sin(angle);

      for (int i = 0; i < KEYS.length; i++) {
         double along = (double)(i - ANCHOR_INDEX) * spacing;
         int x = (int)Math.round(anchor.getX() + dx * along);
         int z = (int)Math.round(anchor.getZ() + dz * along);
         boolean named = !KEYS[i].startsWith("colony_");
         int r = KEYS[i].startsWith("tokyo") ? radius + radius / 8 : radius;
         list.add(new Colony(i, KEYS[i], x, z, r, named));
      }

      return list;
   }

   public static boolean isKey(String key) {
      for (String k : KEYS) {
         if (k.equals(key)) {
            return true;
         }
      }

      return false;
   }
}
