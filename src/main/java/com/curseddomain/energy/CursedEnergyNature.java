package com.curseddomain.energy;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum CursedEnergyNature implements StringRepresentable {
   STANDARD(-8408321, -14202935),
   ELECTRIC(-2662, -13649680),
   ROUGH(-6950212, -14907818);

   private final int hudTop;
   private final int hudBottom;

   private CursedEnergyNature(int hudTop, int hudBottom) {
      this.hudTop = hudTop;
      this.hudBottom = hudBottom;
   }

   public int hudTop() {
      return this.hudTop;
   }

   public int hudBottom() {
      return this.hudBottom;
   }

   public String translationKey() {
      return ModMain.key("energy_nature", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
