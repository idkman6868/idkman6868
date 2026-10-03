package com.curseddomain.config;

public enum HudAnchor {
   HOTBAR,
   TOP_LEFT,
   TOP_RIGHT,
   BOTTOM_LEFT,
   BOTTOM_RIGHT;

   public boolean right() {
      return this == TOP_RIGHT || this == BOTTOM_RIGHT;
   }

   public boolean bottom() {
      return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
   }
}
