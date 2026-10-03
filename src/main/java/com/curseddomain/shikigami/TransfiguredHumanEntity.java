package com.curseddomain.shikigami;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class TransfiguredHumanEntity extends ShikigamiEntity {
   public TransfiguredHumanEntity(EntityType<? extends TransfiguredHumanEntity> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 18.0)
         .add(Attributes.ATTACK_DAMAGE, 4.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FOLLOW_RANGE, 24.0);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ZOMBIE_VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_VILLAGER_DEATH;
   }

   public float getVoicePitch() {
      return 0.6F;
   }
}
