package com.curseddomain.shikigami;

import com.curseddomain.registry.ModParticles;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.vfx.Vfx;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class ShikigamiEntity extends PathfinderMob implements OwnableEntity {
   private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(ShikigamiEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Boolean> RITUAL = SynchedEntityData.defineId(ShikigamiEntity.class, EntityDataSerializers.BOOLEAN);
   public int lifetime = -1;

   protected ShikigamiEntity(EntityType<? extends ShikigamiEntity> type, Level level) {
      super(type, level);
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(OWNER, Optional.empty());
      builder.define(RITUAL, false);
   }

   public void setOwner(LivingEntity owner) {
      this.entityData.set(OWNER, Optional.of(owner.getUUID()));
   }

   public void setRitual(boolean ritual) {
      this.entityData.set(RITUAL, ritual);
   }

   public boolean isRitual() {
      return (Boolean)this.entityData.get(RITUAL);
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.isRitual() ? null : (UUID)((Optional)this.entityData.get(OWNER)).orElse(null);
   }

   @Nullable
   public UUID summonerUUID() {
      return (UUID)((Optional)this.entityData.get(OWNER)).orElse(null);
   }

   @Nullable
   public LivingEntity summoner() {
      UUID id = this.summonerUUID();
      if (id != null && this.level() instanceof ServerLevel server) {
         return server.getEntity(id) instanceof LivingEntity living ? living : null;
      } else {
         return null;
      }
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true));
      this.goalSelector.addGoal(4, new ShikigamiGoals.FollowOwner(this, 1.2, 10.0F, 3.0F));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new ShikigamiGoals.DefendOwner(this));
      this.targetSelector.addGoal(2, new ShikigamiGoals.AssistOwner(this));
      this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(4, new ShikigamiGoals.RitualTarget(this));
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.lifetime > 0 && --this.lifetime == 0) {
            this.dismiss();
         } else {
            LivingEntity summoner = this.summoner();
            if (summoner != null && summoner.isAlive()) {
               if (!this.isRitual() && this.distanceToSqr(summoner) > 2304.0) {
                  Vec3 near = summoner.position().add(this.random.nextGaussian() * 2.0, 0.0, this.random.nextGaussian() * 2.0);
                  this.teleportTo(near.x, summoner.getY(), near.z);
               }

               if (this.getTarget() != null && !this.isRitual() && AbilityContext.isAlly(summoner, this.getTarget())) {
                  this.setTarget(null);
               }
            } else {
               if (this.tickCount > 20) {
                  this.dismiss();
               }
            }
         }
      }
   }

   public void dismiss() {
      if (this.level() instanceof ServerLevel server) {
         server.sendParticles(
            (SimpleParticleType)ModParticles.CURSED_WISP.get(), this.getX(), this.getY() + this.getBbHeight() / 2.0F, this.getZ(), 20, 0.4, 0.5, 0.4, 0.02
         );
         Vfx.ring(server, this.position(), -15724520, this.getBbWidth() * 1.5F, 10);
      }

      this.discard();
   }

   public boolean isAlliedTo(Entity other) {
      UUID owner = this.getOwnerUUID();
      if (owner != null) {
         if (other.getUUID().equals(owner)) {
            return true;
         }

         if (other instanceof ShikigamiEntity s && owner.equals(s.getOwnerUUID())) {
            return true;
         }
      }

      return super.isAlliedTo(other);
   }

   public boolean hurt(DamageSource source, float amount) {
      return source.getEntity() != null && this.isAlliedTo(source.getEntity()) ? false : super.hurt(source, amount);
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
   }
}
