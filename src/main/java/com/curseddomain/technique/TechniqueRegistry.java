package com.curseddomain.technique;

import com.curseddomain.ModMain;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class TechniqueRegistry {
   public static final ResourceKey<Registry<Technique>> KEY = ResourceKey.createRegistryKey(ModMain.id("technique"));
   public static final Registry<Technique> REGISTRY = new RegistryBuilder(KEY).sync(true).create();

   private TechniqueRegistry() {
   }

   public static void onNewRegistry(NewRegistryEvent event) {
      event.register(REGISTRY);
   }

   public static Optional<Technique> get(ResourceLocation id) {
      return REGISTRY.getOptional(id);
   }

   public static List<Technique> sorted() {
      return REGISTRY.stream()
         .sorted(Comparator.comparing(Technique::category).thenComparing(Technique::rarity).thenComparing(t -> t.id().toString()))
         .toList();
   }
}
