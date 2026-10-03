package com.curseddomain.technique.impl.limitless;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityManager;
import com.curseddomain.vfx.Vfx;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class InfinityEvents {
   private InfinityEvents() {
   }

   public static boolean infinityActive(ServerPlayer player) {
      return SorcererManager.get(player).technique().filter(t -> t instanceof LimitlessTechnique).map(t -> {
         Ability infinity = t.abilities().getFirst();
         return AbilityManager.isActive(player, infinity);
      }).orElse(false);
   }

   @SubscribeEvent
   public static void onIncomingDamage(LivingIncomingDamageEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && infinityActive(player)) {
         DamageSource source = event.getSource();
         if (source.getEntity() != null || source.getDirectEntity() != null) {
            if (!source.is(JjkDamage.SURE_HIT) && !source.is(JjkDamage.DOMAIN)) {
               if (!(source.getDirectEntity() instanceof TechniqueProjectile p && p.kind() == ProjectileKind.WORLD_SLASH)) {
                  if (!(source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(ModEffects.DOMAIN_AMPLIFICATION))) {
                     float cost = 1.0F + event.getAmount() * 0.25F;
                     if (EnergyManager.tryConsume(player, cost)) {
                        event.setCanceled(true);
                        Vfx.particles(player.serverLevel(), player.position().add(0.0, 1.0, 0.0), -6301441, 0.8F, 6, 0.4, 0.01);
                     }
                  }
               }
            }
         }
      }
   }
}
