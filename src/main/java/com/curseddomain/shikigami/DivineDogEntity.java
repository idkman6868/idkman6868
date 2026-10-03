package com.curseddomain.shikigami;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class DivineDogEntity extends ShikigamiEntity {
   public static final int WHITE = 0;
   public static final int BLACK = 1;
   public static final int TOTALITY = 2;
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(DivineDogEntity.class, EntityDataSerializers.INT);

   public DivineDogEntity(EntityType<? extends DivineDogEntity> type, Level level) {
      super(type, level);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 24.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.MOVEMENT_SPEED, 0.36)
         .add(Attributes.FOLLOW_RANGE, 32.0)
         .add(Attributes.STEP_HEIGHT, 1.1);
   }

   @Override
   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
   }

   public int variant() {
      return (Integer)this.entityData.get(VARIANT);
   }

   public void setVariant(int variant) {
      this.entityData.set(VARIANT, variant);
      if (variant == 2) {
         this.setAttribute(Attributes.MAX_HEALTH, 60.0);
         this.setAttribute(Attributes.ATTACK_DAMAGE, 12.0);
         this.setAttribute(Attributes.SCALE, 1.8);
         this.setAttribute(Attributes.MOVEMENT_SPEED, 0.4);
         this.setHealth(this.getMaxHealth());
      }
   }

   private void setAttribute(Holder<Attribute> attribute, double value) {
      AttributeInstance instance = this.getAttribute(attribute);
      if (instance != null) {
         instance.setBaseValue(value);
      }
   }

   public void die(DamageSource source) {
      super.die(source);
      if (!this.level().isClientSide && !this.isRitual()) {
         TenShadowsShikigami.onDogDied(this);
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.getTarget() != null ? SoundEvents.WOLF_GROWL : SoundEvents.WOLF_PANT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.WOLF_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WOLF_DEATH;
   }
}
