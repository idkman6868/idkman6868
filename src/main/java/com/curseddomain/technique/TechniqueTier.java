package com.curseddomain.technique;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum TechniqueTier implements StringRepresentable {
   A,
   B,
   C;

   public String translationKey() {
      return ModMain.key("technique_tier", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
