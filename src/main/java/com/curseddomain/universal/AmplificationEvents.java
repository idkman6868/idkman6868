package com.curseddomain.universal;

import com.curseddomain.registry.ModEffects;
import com.curseddomain.technique.ability.AbilityManager;
import com.curseddomain.vfx.Vfx;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class AmplificationEvents {
   private AmplificationEvents() {
   }

   @SubscribeEvent
   public static void onHit(LivingIncomingDamageEvent event) {
      if (event.getSource().getEntity() instanceof LivingEntity attacker
         && event.getSource().getDirectEntity() == attacker
         && attacker.hasEffect(ModEffects.DOMAIN_AMPLIFICATION)) {
         if (event.getEntity() instanceof ServerPlayer target) {
            AbilityManager.stopAll(target);
            Vfx.burst(target.serverLevel(), target.position().add(0.0, 1.0, 0.0), -8758560, 1.2F, 8);
         }
      }
   }
}
