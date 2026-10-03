package com.curseddomain.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "cursed_domain");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
      "main",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.cursed_domain"))
         .icon(() -> new ItemStack((ItemLike)ModItems.RECRUITMENT_LETTER.get()))
         .displayItems((params, output) -> ModItems.ITEMS.getEntries().forEach(item -> output.accept((ItemLike)item.get())))
         .build()
   );

   private ModCreativeTabs() {
   }
}
