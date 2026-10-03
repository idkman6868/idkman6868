package com.curseddomain.entity.projectile;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.vfx.EnergyParticleOptions;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class TechniqueProjectile extends Projectile {
   private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(TechniqueProjectile.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(TechniqueProjectile.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> POWER = SynchedEntityData.defineId(TechniqueProjectile.class, EntityDataSerializers.FLOAT);
   public float damage;
   public float radius;
   public int maxAge = 100;
   public boolean pierce;
   public float gravity;
   public float drag = 1.0F;
   public ResourceKey<DamageType> damageType = JjkDamage.TECHNIQUE;
   public int age;
   public final Set<UUID> alreadyHit = new HashSet<>();
   public Vec3 anchor = Vec3.ZERO;

   public TechniqueProjectile(EntityType<? extends TechniqueProjectile> type, Level level) {
      super(type, level);
      this.noPhysics = false;
   }

   public static TechniqueProjectile spawn(ServerLevel level, LivingEntity owner, ProjectileKind kind, Vec3 pos, Vec3 velocity, float size, float damage) {
      TechniqueProjectile p = new TechniqueProjectile((EntityType<? extends TechniqueProjectile>)ModEntities.TECHNIQUE_PROJECTILE.get(), level);
      p.setOwner(owner);
      p.setKind(kind);
      p.setSize(size);
      p.damage = damage;
      p.setPos(pos);
      p.setDeltaMovement(velocity);
      p.alignToVelocity();
      level.addFreshEntity(p);
      return p;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(KIND, 0);
      builder.define(SIZE, 0.5F);
      builder.define(POWER, 1.0F);
   }

   public ProjectileKind kind() {
      ProjectileKind[] kinds = ProjectileKind.values();
      int i = (Integer)this.entityData.get(KIND);
      return i >= 0 && i < kinds.length ? kinds[i] : ProjectileKind.ENERGY_BOLT;
   }

   public void setKind(ProjectileKind kind) {
      this.entityData.set(KIND, kind.ordinal());
   }

   public float size() {
      return (Float)this.entityData.get(SIZE);
   }

   public void setSize(float size) {
      this.entityData.set(SIZE, size);
   }

   public float power() {
      return (Float)this.entityData.get(POWER);
   }

   public void setPower(float power) {
      this.entityData.set(POWER, power);
   }

   public void alignToVelocity() {
      Vec3 v = this.getDeltaMovement();
      if (v.lengthSqr() > 1.0E-6) {
         double h = v.horizontalDistance();
         this.setYRot((float)(Math.atan2(v.x, v.z) * 180.0 / Math.PI));
         this.setXRot((float)(Math.atan2(v.y, h) * 180.0 / Math.PI));
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }
   }

   public void tick() {
      super.tick();
      this.age++;
      Vec3 velocity = this.getDeltaMovement();
      if (!this.level().isClientSide) {
         ProjectileBehavior behavior = ProjectileBehavior.of(this.kind());
         if (this.age > this.maxAge) {
            behavior.expire(this);
            return;
         }

         HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
         if (hit.getType() != Type.MISS) {
            this.onHit(hit);
            if (this.isRemoved()) {
               return;
            }
         }

         behavior.tick(this);
         if (this.isRemoved()) {
            return;
         }

         velocity = this.getDeltaMovement();
      } else {
         this.trail();
      }

      this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
      this.setDeltaMovement(velocity.scale(this.drag).add(0.0, -this.gravity, 0.0));
      this.alignToVelocity();
   }

   private void trail() {
      ProjectileKind kind = this.kind();
      int color = kind.color();
      float size = this.size();
      if (kind != ProjectileKind.NAIL && kind != ProjectileKind.CROW) {
         int count = size > 1.5F ? 2 : 1;

         for (int i = 0; i < count; i++) {
            this.level()
               .addParticle(
                  new EnergyParticleOptions(color, 0.6F + size * 0.6F, 0.0F),
                  this.getRandomX(size),
                  this.getY() + this.random.nextGaussian() * size * 0.5,
                  this.getRandomZ(size),
                  this.random.nextGaussian() * 0.02,
                  this.random.nextGaussian() * 0.02,
                  this.random.nextGaussian() * 0.02
               );
         }
      }
   }

   protected boolean canHitEntity(Entity target) {
      if (super.canHitEntity(target) && !(target instanceof TechniqueProjectile)) {
         Entity owner = this.getOwner();
         return owner != null && AbilityContext.isAlly(owner, target) ? false : !this.alreadyHit.contains(target.getUUID());
      } else {
         return false;
      }
   }

   protected void onHitEntity(EntityHitResult result) {
      this.alreadyHit.add(result.getEntity().getUUID());
      ProjectileBehavior.of(this.kind()).hitEntity(this, result);
   }

   protected void onHitBlock(BlockHitResult result) {
      ProjectileBehavior.of(this.kind()).hitBlock(this, result);
   }

   public boolean damageTarget(Entity target, float amount) {
      return target instanceof LivingEntity living ? JjkDamage.hurt(living, this.damageType, this.getOwner(), this, amount, false) : false;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 25600.0;
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
   }
}
