package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum InnateTrait implements StringRepresentable {
   SIX_EYES,
   HEAVENLY_RESTRICTION_PHYSICAL,
   HEAVENLY_RESTRICTION_ENERGY,
   VESSEL,
   DEATH_PAINTING,
   INCARNATED_SORCERER;

   public String translationKey() {
      return ModMain.key("trait", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
