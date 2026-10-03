package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum SorcererStatus implements StringRepresentable {
   NON_SORCERER,
   AWAKENED,
   APPLICANT,
   ENTRANCE_MISSION,
   STUDENT,
   CURSE_USER;

   public boolean awakened() {
      return this != NON_SORCERER;
   }

   public boolean enrolled() {
      return this == STUDENT || this == CURSE_USER;
   }

   public String translationKey() {
      return ModMain.key("status", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
