package com.steelballrun.registry;

import com.steelballrun.SteelBallRun;
import com.steelballrun.item.HorseBrushItem;
import com.steelballrun.item.MapOfAmericaItem;
import com.steelballrun.item.TooltipItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SbrItems {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SteelBallRun.MODID);
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SteelBallRun.MODID);

   public static final DeferredItem<MapOfAmericaItem> MAP_OF_AMERICA = ITEMS.register(
      "map_of_america", () -> new MapOfAmericaItem(new Item.Properties().stacksTo(1))
   );
   public static final DeferredItem<HorseBrushItem> HORSE_BRUSH = ITEMS.register("horse_brush", () -> new HorseBrushItem(new Item.Properties().stacksTo(1)));
   public static final DeferredItem<TooltipItem> RACE_NUMBER = ITEMS.register("race_number", () -> new TooltipItem(new Item.Properties().stacksTo(1)));
   public static final DeferredItem<TooltipItem> DOLLAR = ITEMS.register("dollar", () -> new TooltipItem(new Item.Properties()));
   public static final DeferredItem<TooltipItem> PRIZE_CHEQUE = ITEMS.register(
      "prize_cheque", () -> new TooltipItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
   );
   public static final DeferredItem<TooltipItem> TROPHY = ITEMS.register("trophy", () -> new TooltipItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register(
      "main",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.steel_ball_run"))
         .icon(() -> new ItemStack(MAP_OF_AMERICA.get()))
         .displayItems((params, out) -> {
            out.accept(MAP_OF_AMERICA.get());
            out.accept(HORSE_BRUSH.get());
            out.accept(RACE_NUMBER.get());
            out.accept(DOLLAR.get());
            out.accept(PRIZE_CHEQUE.get());
            out.accept(TROPHY.get());
         })
         .build()
   );

   private SbrItems() {
   }
}
