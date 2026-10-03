package com.curseddomain.landmark;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Kinds of landmark the mod builds into the world. */
public enum LandmarkType {
   TOKYO_HIGH(52, 34),
   KYOTO_HIGH(52, 40),
   SHIBUYA(76, 56),
   TOKYO(84, 112),
   KYOTO(80, 40),
   CITY(64, 48),
   RUINED_CITY(72, 48),
   KENJAKU_HIDEOUT(18, 16);

   private final int radius;
   private final int clearHeight;

   LandmarkType(int radius, int clearHeight) {
      this.radius = radius;
      this.clearHeight = clearHeight;
   }

   /** Half the side of the square footprint. */
   public int radius() {
      return this.radius;
   }

   /** How far above ground level the footprint is cleared before building. */
   public int clearHeight() {
      return this.clearHeight;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public Component displayName() {
      return Component.translatable("landmark.cursed_domain.type." + this.id());
   }

   public static LandmarkType byId(String id) {
      for (LandmarkType type : values()) {
         if (type.id().equals(id)) {
            return type;
         }
      }

      return null;
   }
}
