package com.curseddomain.technique.impl.disasterflames;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.Vfx;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class CoffinOfTheIronMountain extends DomainExpansion {
   public CoffinOfTheIronMountain() {
      super(
         "coffin_of_the_iron_mountain",
         DomainStyle.COFFIN_OF_THE_IRON_MOUNTAIN,
         DomainType.CLOSED,
         AbilityStats.builder().cost(200.0F).upkeep(12.0F).radius(16.0F).damage(5.0F).durationSeconds(25.0F).build()
      );
   }

   @Override
   public void onTick(DomainInstance d) {
      if (d.activeTicks % 4 == 0) {
         Vec3 at = d.center
            .add((d.level.random.nextDouble() - 0.5) * d.radius * 1.6, d.level.random.nextDouble() * 6.0, (d.level.random.nextDouble() - 0.5) * d.radius * 1.6);
         if (at.distanceTo(d.center) < d.radius) {
            d.level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 2, 0.3, 0.3, 0.3, 0.0);
            d.level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 4, 0.5, 0.5, 0.5, 0.02);
         }
      }
   }

   @Override
   public void sureHit(DomainInstance d, LivingEntity target) {
      JjkDamage.hurt(target, JjkDamage.SURE_HIT, d.owner, null, d.domain.stats().damage(), true);
      target.igniteForSeconds(4.0F);
      target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, false));
      Vfx.particles(d.level, target.position().add(0.0, 1.0, 0.0), -34278, 1.0F, 4, 0.4, 0.05);
   }

   @Override
   public float refinement() {
      return 1.2F;
   }
}
