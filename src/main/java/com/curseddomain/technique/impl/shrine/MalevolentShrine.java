package com.curseddomain.technique.impl.shrine;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.Vfx;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MalevolentShrine extends DomainExpansion {
   public MalevolentShrine() {
      super(
         "malevolent_shrine",
         DomainStyle.MALEVOLENT_SHRINE,
         DomainType.BARRIERLESS,
         AbilityStats.builder().cost(280.0F).upkeep(20.0F).radius(28.0F).damage(4.0F).durationSeconds(24.0F).build()
      );
   }

   @Override
   public void onOpen(DomainInstance d) {
      d.level.playSound(null, d.center.x, d.center.y, d.center.z, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.5F, 0.5F);
      Vfx.shake(d.level, d.center, 5.0F, d.radius * 2.0F, 30);
   }

   @Override
   public void onTick(DomainInstance d) {
      if (d.activeTicks % 3 == 0) {
         Vec3 at = d.center
            .add((d.level.random.nextDouble() - 0.5) * d.radius * 1.6, d.level.random.nextDouble() * 4.0, (d.level.random.nextDouble() - 0.5) * d.radius * 1.6);
         if (at.distanceTo(d.center) < d.radius) {
            Moves.domainBreak(d.level, at, 1.4, 4.0F, 6);
         }
      }

      if (d.activeTicks % 20 == 0) {
         d.level.playSound(null, d.center.x, d.center.y, d.center.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2.0F, 0.5F);
      }
   }

   @Override
   public void sureHit(DomainInstance d, LivingEntity target) {
      float dismantle = d.domain.stats().damage();
      float cleave = target.getMaxHealth() * 0.04F;
      JjkDamage.hurt(target, JjkDamage.SURE_HIT, d.owner, null, dismantle + cleave, true);
      Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
      Vec3 dir = new Vec3(d.level.random.nextGaussian(), d.level.random.nextGaussian() * 0.4, d.level.random.nextGaussian());
      Vfx.slash(d.level, at, dir, -3856, target.getBbHeight() * 0.8F + 0.6F, 6);
   }

   @Override
   public float refinement() {
      return 3.0F;
   }
}
