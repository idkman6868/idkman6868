package com.steelballrun.item;

import com.steelballrun.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Use it to see the course: every checkpoint, how far away it is and which way to ride. */
public class MapOfAmericaItem extends TooltipItem {
   public MapOfAmericaItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide && player instanceof ServerPlayer sp) {
         RaceManager.showRoute(sp);
      }
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
   }
}
