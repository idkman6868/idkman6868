package com.curseddomain.shikigami;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.vfx.Vfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NueEntity extends ShikigamiEntity {
   private int diveCooldown;
   private int diving;

   public NueEntity(EntityType<? extends NueEntity> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 22.0)
         .add(Attributes.ATTACK_DAMAGE, 7.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FLYING_SPEED, 0.6)
         .add(Attributes.FOLLOW_RANGE, 40.0);
   }

   @Override
   protected void registerGoals() {
      this.targetSelector.addGoal(1, new ShikigamiGoals.DefendOwner(this));
      this.targetSelector.addGoal(2, new ShikigamiGoals.AssistOwner(this));
      this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(4, new ShikigamiGoals.RitualTarget(this));
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.level().isClientSide && !this.isRemoved()) {
         LivingEntity target = this.getTarget();
         LivingEntity summoner = this.summoner();
         Vec3 goal;
         if (target != null && target.isAlive()) {
            if (this.diveCooldown > 0) {
               this.diveCooldown--;
               goal = target.position().add(0.0, 5.0, 0.0);
            } else {
               this.diving = 1;
               goal = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);
               if (this.distanceTo(target) < 2.2) {
                  this.shock(target);
               }
            }
         } else if (summoner != null) {
            goal = summoner.position().add(Math.cos(this.tickCount * 0.05) * 3.0, 3.5, Math.sin(this.tickCount * 0.05) * 3.0);
         } else {
            goal = this.position();
         }

         Vec3 to = goal.subtract(this.position());
         double speed = this.diving > 0 ? 0.9 : 0.45;
         Vec3 wanted = to.lengthSqr() > 0.01 ? to.normalize().scale(Math.min(speed, to.length() * 0.3)) : Vec3.ZERO;
         this.setDeltaMovement(this.getDeltaMovement().scale(0.7).add(wanted.scale(0.3)));
         if (wanted.lengthSqr() > 0.001) {
            this.setYRot((float)(Math.atan2(-wanted.x, wanted.z) * 180.0 / Math.PI));
            this.yBodyRot = this.getYRot();
         }
      }
   }

   private void shock(LivingEntity target) {
      JjkDamage.hurt(
         target,
         JjkDamage.TECHNIQUE,
         (Entity)(this.summoner() == null ? this : this.summoner()),
         this,
         (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE),
         false
      );
      if (this.level() instanceof ServerLevel server) {
         Vfx.bolt(server, this.position().add(0.0, 0.5, 0.0), target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0), -6297345, 0.25F, 8);
         Vfx.sparks(server, target.position().add(0.0, 1.0, 0.0), -6297345, 2.0F, 20);
         server.playSound(null, this.blockPosition(), (SoundEvent)SoundEvents.TRIDENT_THUNDER.value(), this.getSoundSource(), 0.6F, 1.6F);
      }

      this.diving = 0;
      this.diveCooldown = 50;
   }

   public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.PHANTOM_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.PHANTOM_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.PHANTOM_DEATH;
   }

   public void die(DamageSource source) {
      super.die(source);
      if (!this.level().isClientSide && this.isRitual()) {
         TenShadowsShikigami.onRitualWon(this, source, "shikigami:nue");
      }
   }
}
