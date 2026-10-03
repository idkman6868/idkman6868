package com.curseddomain.technique.ability;

import com.curseddomain.ModMain;
import com.curseddomain.config.ServerConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class AbilityStatsLoader extends SimpleJsonResourceReloadListener {
   private static volatile Map<ResourceLocation, JsonObject> overrides = Map.of();

   private AbilityStatsLoader() {
      super(new Gson(), "ability_stats");
   }

   @SubscribeEvent
   public static void register(AddReloadListenerEvent event) {
      event.addListener(new AbilityStatsLoader());
   }

   protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
      Map<ResourceLocation, JsonObject> loaded = new HashMap<>();
      files.forEach((id, json) -> {
         if (json.isJsonObject()) {
            loaded.put(id, json.getAsJsonObject());
         } else {
            ModMain.LOGGER.error("ability_stats {} is not a JSON object", id);
         }
      });
      overrides = Map.copyOf(loaded);
      ModMain.LOGGER.info("Loaded {} ability stat overrides", loaded.size());
   }

   public static AbilityStats statsFor(Ability ability) {
      return statsFor(ability.id(), ability.defaults());
   }

   public static AbilityStats statsFor(ResourceLocation id, AbilityStats defaults) {
      AbilityStats stats = defaults;
      JsonObject json = overrides.get(id);
      if (json != null) {
         stats = defaults.override(json);
      }

      return stats.scaled(
         ((Double)ServerConfig.TECHNIQUE_COST_MULTIPLIER.get()).floatValue(), ((Double)ServerConfig.TECHNIQUE_COOLDOWN_MULTIPLIER.get()).floatValue()
      );
   }
}
