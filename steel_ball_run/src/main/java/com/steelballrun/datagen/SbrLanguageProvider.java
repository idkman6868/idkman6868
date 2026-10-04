package com.steelballrun.datagen;

import com.steelballrun.SteelBallRun;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** {@code ./gradlew runData} writes en_us.json from {@link SbrLang}. */
public final class SbrLanguageProvider extends LanguageProvider {
   public SbrLanguageProvider(PackOutput output) {
      super(output, SteelBallRun.MODID, "en_us");
   }

   @Override
   protected void addTranslations() {
      SbrLang.addAll(this::add);
   }

   /** Registered on the mod bus by {@link SteelBallRun}. */
   public static void gather(GatherDataEvent event) {
      DataGenerator generator = event.getGenerator();
      generator.addProvider(event.includeClient(), new SbrLanguageProvider(generator.getPackOutput()));
   }
}
