package com.steelballrun.item;

import net.minecraft.world.item.Item;

/** Right-click a tamed horse to brush it (handled in {@link com.steelballrun.horse.HorseCare}). */
public class HorseBrushItem extends TooltipItem {
   public HorseBrushItem(Item.Properties properties) {
      super(properties);
   }
}
