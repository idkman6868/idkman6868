package com.curseddomain.technique;

import com.curseddomain.ModMain;
import com.curseddomain.util.EnumNames;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum TechniqueRarity implements StringRepresentable {
   COMMON(40, ChatFormatting.WHITE),
   UNCOMMON(25, ChatFormatting.GREEN),
   RARE(18, ChatFormatting.AQUA),
   EPIC(10, ChatFormatting.LIGHT_PURPLE),
   LEGENDARY(5, ChatFormatting.GOLD),
   MYTHIC(2, ChatFormatting.RED);

   private final int defaultWeight;
   private final ChatFormatting color;

   private TechniqueRarity(int defaultWeight, ChatFormatting color) {
      this.defaultWeight = defaultWeight;
      this.color = color;
   }

   public int defaultWeight() {
      return this.defaultWeight;
   }

   public ChatFormatting color() {
      return this.color;
   }

   public String translationKey() {
      return ModMain.key("rarity", this.getSerializedName());
   }

   public Component displayName() {
      return Component.translatable(this.translationKey()).withStyle(this.color);
   }

   public String getSerializedName() {
      return EnumNames.lower(this);
   }
}
