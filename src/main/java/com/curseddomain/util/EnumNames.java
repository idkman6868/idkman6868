package com.curseddomain.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public final class EnumNames {
   private EnumNames() {
   }

   public static String lower(Enum<?> value) {
      return value.name().toLowerCase(Locale.ROOT);
   }

   public static <E extends Enum<E> & StringRepresentable> E byName(E[] values, String name, E fallback) {
      for (E value : values) {
         if (value.getSerializedName().equals(name)) {
            return value;
         }
      }

      return fallback;
   }

   public static <E extends Enum<E> & StringRepresentable> List<String> names(E[] values) {
      return Arrays.stream(values).map(rec$ -> rec$.getSerializedName()).toList();
   }
}
