package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum Grade implements StringRepresentable {
   UNGRADED,
   GRADE_4,
   GRADE_3,
   GRADE_2,
   SEMI_GRADE_1,
   GRADE_1,
   SPECIAL_GRADE;

   public boolean atLeast(Grade other) {
      return this.ordinal() >= other.ordinal();
   }

   public Grade next() {
      Grade[] values = values();
      return this == SPECIAL_GRADE ? this : values[this.ordinal() + 1];
   }

   public String translationKey() {
      return ModMain.key("grade", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
