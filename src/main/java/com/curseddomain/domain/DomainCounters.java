package com.curseddomain.domain;

import com.curseddomain.energy.EnergyManager;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.vfx.Vfx;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class DomainCounters {
   private static final float FALLING_BLOSSOM_COST = 6.0F;

   private DomainCounters() {
   }

   public static boolean protectedFrom(LivingEntity target, DomainInstance domain) {
      if (!target.hasEffect(ModEffects.SIMPLE_DOMAIN)
         && !target.hasEffect(ModEffects.HOLLOW_WICKER_BASKET)
         && !target.hasEffect(ModEffects.DOMAIN_AMPLIFICATION)) {
         if (target instanceof Player player && SorcererManager.get(player).hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL)) {
            return true;
         } else if (target.hasEffect(ModEffects.FALLING_BLOSSOM_EMOTION) && target instanceof ServerPlayer player && EnergyManager.tryConsume(player, 6.0F)) {
            Vfx.burst(player.serverLevel(), player.position().add(0.0, 1.0, 0.0), -1007424, 1.6F, 8);
            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }
}
