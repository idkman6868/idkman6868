package com.curseddomain.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class DataGenerators {
   private DataGenerators() {
   }

   @SubscribeEvent
   public static void gather(GatherDataEvent event) {
      DataGenerator generator = event.getGenerator();
      PackOutput output = generator.getPackOutput();
      generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
      generator.addProvider(event.includeClient(), new ModItemModelProvider(output, event.getExistingFileHelper()));
      generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, event.getExistingFileHelper()));
   }
}
