package com.steelballrun.race;

import java.util.ArrayList;
import java.util.List;

/** Stage points by finishing place, parsed from the comma-separated config table. */
public final class Points {
   public static final String DEFAULT_TABLE = "100,70,50,40,30,25,20,15,10,8,6,5,4,3,2,1";
   private final int[] table;

   private Points(int[] table) {
      this.table = table;
   }

   /** Invalid entries are skipped; an empty or broken table falls back to the default. */
   public static Points parse(String text) {
      List<Integer> values = new ArrayList<>();
      if (text != null) {
         for (String part : text.split(",")) {
            try {
               int v = Integer.parseInt(part.trim());
               if (v >= 0) {
                  values.add(v);
               }
            } catch (NumberFormatException ignored) {
            }
         }
      }
      if (values.isEmpty() && !DEFAULT_TABLE.equals(text)) {
         return parse(DEFAULT_TABLE);
      }
      int[] table = new int[values.size()];
      for (int i = 0; i < table.length; i++) {
         table[i] = values.get(i);
      }
      return new Points(table);
   }

   /** Points for finishing a stage in {@code place} (1 = first). Places past the end of the table score nothing. */
   public int forPlace(int place) {
      return place >= 1 && place <= this.table.length ? this.table[place - 1] : 0;
   }
}
