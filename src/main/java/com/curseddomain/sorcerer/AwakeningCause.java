package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.util.StringRepresentable;

public enum AwakeningCause implements StringRepresentable {
   CURSE_ATTACKS,
   NEAR_DEATH,
   CURSED_OBJECT,
   SEAL_BREAK,
   COMMAND;

   public String subtitleKey() {
      return ModMain.key("awakening", this.getSerializedName());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
