package com.curseddomain.item.cursedobject;

import com.curseddomain.sorcerer.AwakeningCause;
import com.curseddomain.sorcerer.AwakeningManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public class CursedObjectItem extends Item {
   public CursedObjectItem(Properties properties) {
      super(properties);
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide && entity instanceof ServerPlayer player && player.tickCount % 10 == 0) {
         AwakeningManager.awaken(player, AwakeningCause.CURSED_OBJECT);
      }
   }

   public boolean isFoil(ItemStack stack) {
      return true;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(
         Component.translatable("item.cursed_domain.cursed_object.desc").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC})
      );
   }
}
