package com.steelballrun.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** An item whose tooltip is the lang entry {@code <item key>.tooltip}. */
public class TooltipItem extends Item {
   public TooltipItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
   }
}
