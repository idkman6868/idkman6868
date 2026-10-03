package com.curseddomain.technique.impl.limitless;

import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.Vfx;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class UnlimitedVoid extends DomainExpansion {
   public UnlimitedVoid() {
      super(
         "unlimited_void",
         DomainStyle.UNLIMITED_VOID,
         DomainType.CLOSED,
         AbilityStats.builder().cost(220.0F).upkeep(14.0F).radius(18.0F).durationSeconds(30.0F).build()
      );
   }

   @Override
   public void onOpen(DomainInstance d) {
      d.level.playSound(null, d.center.x, d.center.y, d.center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 2.0F, 1.4F);
   }

   @Override
   public void sureHit(DomainInstance d, LivingEntity target) {
      target.addEffect(new MobEffectInstance(ModEffects.STUNNED, 30, 0, false, false, true));
      target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, false));
      if (target instanceof Mob mob) {
         mob.setTarget(null);
         mob.getNavigation().stop();
      }

      if (target instanceof ServerPlayer player && d.activeTicks % 40 == 0) {
         Vfx.screen(player, ScreenFxPayload.Kind.FLASH, 16777215, 0.6F, 10);
      }
   }

   @Override
   public float refinement() {
      return 1.6F;
   }
}
