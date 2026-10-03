package com.curseddomain.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class ConstructedBladeItem extends SwordItem {
   private static final String EXPIRES = "construct_expires";

   public ConstructedBladeItem(Tier tier, Properties properties) {
      super(tier, properties);
   }

   public static ItemStack temporary(ItemStack stack, long expiresAt) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("construct_expires", expiresAt);
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return stack;
   }

   private static long expiry(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? -1L : data.copyTag().getLong("construct_expires");
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      long expires = expiry(stack);
      if (!level.isClientSide && expires > 0L && level.getGameTime() > expires) {
         stack.setCount(0);
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(
         Component.translatable(expiry(stack) > 0L ? "item.cursed_domain.constructed.temporary" : "item.cursed_domain.constructed.permanent")
            .withStyle(new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.ITALIC})
      );
   }
}
