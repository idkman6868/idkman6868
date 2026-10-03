package com.curseddomain.cullinggame;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;

/** The Prison Realm, with Satoru Gojo sealed inside. Only the Angel's technique can open it. */
public class PrisonRealmItem extends Item {
   public PrisonRealmItem(Properties properties) {
      super(properties);
   }

   public boolean isFoil(ItemStack stack) {
      return true;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("item.cursed_domain.prison_realm.desc").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC}));
      tooltip.add(Component.translatable("item.cursed_domain.prison_realm.hint").withStyle(ChatFormatting.GRAY));
   }
}
