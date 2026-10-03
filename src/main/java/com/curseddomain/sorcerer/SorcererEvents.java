package com.curseddomain.sorcerer;

import com.curseddomain.energy.EnergyManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class SorcererEvents {
   private SorcererEvents() {
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SorcererManager.get(player).sightChanged();
         if (!SorcererManager.get(player).rolled()) {
            TechniqueRoller.rollFor(player);
         }

         EnergyManager.recalculate(player);
         TraitEffects.apply(player);
         SorcererManager.syncAll(player);
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SorcererManager.get(player).sightChanged();
         TraitEffects.apply(player);
         SorcererManager.sync(player);
         EnergyManager.onRespawn(player, !event.isEndConquered());
      }
   }

   @SubscribeEvent
   public static void onChangeDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         SorcererManager.syncAll(player);
      }
   }
}
