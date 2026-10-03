package com.curseddomain.util;

import java.util.List;
import java.util.function.ToDoubleFunction;

public final class WeightedPicker {
   private WeightedPicker() {
   }

   public static <T> T pick(List<T> entries, ToDoubleFunction<T> weight, double roll) {
      double total = 0.0;

      for (T entry : entries) {
         total += Math.max(0.0, weight.applyAsDouble(entry));
      }

      if (total <= 0.0) {
         return null;
      } else {
         double target = roll * total;
         T last = null;

         for (T entry : entries) {
            double w = Math.max(0.0, weight.applyAsDouble(entry));
            if (!(w <= 0.0)) {
               last = entry;
               if (target < w) {
                  return entry;
               }

               target -= w;
            }
         }

         return last;
      }
   }
}
