package com.curseddomain.entity.cursedspirit;

import com.curseddomain.registry.ModParticles;
import com.curseddomain.sorcerer.Grade;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class CursedSpirit extends Monster {
   protected CursedSpirit(EntityType<? extends CursedSpirit> type, Level level) {
      super(type, level);
   }

   public abstract Grade curseGrade();

   public boolean broadcastToPlayer(ServerPlayer player) {
      return CurseVisibility.canSee(player) && super.broadcastToPlayer(player);
   }

   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide && this.random.nextInt(4) == 0) {
         this.level()
            .addParticle(
               (ParticleOptions)ModParticles.CURSED_WISP.get(),
               this.getRandomX(0.6),
               this.getRandomY(),
               this.getRandomZ(0.6),
               0.0,
               0.02 + this.random.nextDouble() * 0.02,
               0.0
            );
      }
   }

   protected float getSoundVolume() {
      return 0.45F;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SCULK_CLICKING;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.PHANTOM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PHANTOM_DEATH;
   }
}
