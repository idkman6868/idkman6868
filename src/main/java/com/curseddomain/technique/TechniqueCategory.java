package com.curseddomain.technique;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum TechniqueCategory implements StringRepresentable {
   TOKYO_HIGH,
   KYOTO_HIGH,
   CLANS_AND_ADULTS,
   CULLING_GAME,
   CURSES_AND_CURSE_USERS;

   public String translationKey() {
      return ModMain.key("technique_category", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
