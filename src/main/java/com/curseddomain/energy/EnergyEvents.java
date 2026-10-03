package com.curseddomain.energy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class EnergyEvents {
   private EnergyEvents() {
   }

   @SubscribeEvent
   public static void onPlayerTick(Post event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         EnergyManager.tick(player);
      }
   }

   @SubscribeEvent
   public static void onDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
      if (!(event.getNewDamage() <= 0.0F)) {
         if (event.getEntity() instanceof ServerPlayer victim) {
            EnergyManager.markCombat(victim);
         }

         if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != event.getEntity()) {
            EnergyManager.markCombat(attacker);
         }
      }
   }
}
