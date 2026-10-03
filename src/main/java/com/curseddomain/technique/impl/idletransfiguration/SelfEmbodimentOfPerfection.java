package com.curseddomain.technique.impl.idletransfiguration;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.Vfx;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class SelfEmbodimentOfPerfection extends DomainExpansion {
   private static final int TRANSFIGURE_AT = 4;

   public SelfEmbodimentOfPerfection() {
      super(
         "self_embodiment_of_perfection",
         DomainStyle.SELF_EMBODIMENT_OF_PERFECTION,
         DomainType.CLOSED,
         AbilityStats.builder().cost(200.0F).upkeep(12.0F).radius(14.0F).damage(5.0F).durationSeconds(25.0F).build()
      );
   }

   @Override
   public void sureHit(DomainInstance d, LivingEntity target) {
      if (d.activeTicks % 20 == 0) {
         MobEffectInstance wound = target.getEffect(ModEffects.SOUL_DAMAGE);
         if (wound != null && wound.getAmplifier() + 1 >= 4) {
            JjkDamage.hurt(target, JjkDamage.SOUL, d.owner, null, 30.0F, true);
            Vfx.burst(d.level, target.position().add(0.0, 1.0, 0.0), -1519361, 2.5F, 16);
            target.removeEffect(ModEffects.SOUL_DAMAGE);
         } else {
            IdleTransfigurationTechnique.touchSoul(d.level, d.owner, target, d.domain.stats().damage(), 400);
         }
      }
   }

   @Override
   public float refinement() {
      return 1.3F;
   }
}
