package com.curseddomain.technique.impl.tenshadows;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.shikigami.DivineDogEntity;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.Vfx;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ChimeraShadowGarden extends DomainExpansion {
   public ChimeraShadowGarden() {
      super(
         "chimera_shadow_garden",
         DomainStyle.CHIMERA_SHADOW_GARDEN,
         DomainType.CLOSED,
         AbilityStats.builder().cost(180.0F).upkeep(10.0F).radius(16.0F).damage(3.0F).durationSeconds(30.0F).build()
      );
   }

   @Override
   public void onOpen(DomainInstance d) {
      if (d.owner instanceof ServerPlayer player) {
         for (int i = 0; i < 2; i++) {
            Vec3 at = d.center.add(i == 0 ? -3 : 3, 0.0, 2.0);
            DivineDogEntity dog = TenShadowsTechnique.summon(player, (EntityType<DivineDogEntity>)ModEntities.DIVINE_DOG.get(), at);
            if (dog != null) {
               dog.setVariant(1);
               dog.lifetime = d.domain.stats().duration();
            }
         }
      }
   }

   @Override
   public void onTick(DomainInstance d) {
      if (d.contains(d.owner)) {
         d.owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 1, false, false));
      }

      if (d.activeTicks % 10 == 0) {
         Vec3 at = d.center.add((d.level.random.nextDouble() - 0.5) * d.radius * 1.6, 0.0, (d.level.random.nextDouble() - 0.5) * d.radius * 1.6);
         Vfx.ring(d.level, at, -15724520, 1.5F + d.level.random.nextFloat() * 2.0F, 20);
      }
   }

   @Override
   public void sureHit(DomainInstance d, LivingEntity target) {
      JjkDamage.hurt(target, JjkDamage.SURE_HIT, d.owner, null, d.domain.stats().damage(), true);
      target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false));
      target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
      Vfx.pillar(d.level, target.position(), -15066566, 1.6F, 8);
   }
}
