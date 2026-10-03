package com.curseddomain.domain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DomainBarrierEntity extends Entity {
   private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> CLASHING = SynchedEntityData.defineId(DomainBarrierEntity.class, EntityDataSerializers.BOOLEAN);
   public int phaseStartAge;
   private int lastPhase = -1;

   public DomainBarrierEntity(EntityType<? extends DomainBarrierEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
      this.setNoGravity(true);
      this.setInvulnerable(true);
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(RADIUS, 1.0F);
      builder.define(STYLE, 0);
      builder.define(TYPE, 0);
      builder.define(PHASE, 0);
      builder.define(OWNER, -1);
      builder.define(CLASHING, false);
   }

   public void setup(float radius, DomainStyle style, DomainType type, int ownerId) {
      this.entityData.set(RADIUS, radius);
      this.entityData.set(STYLE, style.ordinal());
      this.entityData.set(TYPE, type.ordinal());
      this.entityData.set(OWNER, ownerId);
   }

   public float radius() {
      return (Float)this.entityData.get(RADIUS);
   }

   public DomainStyle style() {
      DomainStyle[] values = DomainStyle.values();
      int i = (Integer)this.entityData.get(STYLE);
      return i >= 0 && i < values.length ? values[i] : DomainStyle.GENERIC;
   }

   public DomainType domainType() {
      DomainType[] values = DomainType.values();
      int i = (Integer)this.entityData.get(TYPE);
      return i >= 0 && i < values.length ? values[i] : DomainType.CLOSED;
   }

   public DomainInstance.Phase phase() {
      DomainInstance.Phase[] values = DomainInstance.Phase.values();
      int i = (Integer)this.entityData.get(PHASE);
      return i >= 0 && i < values.length ? values[i] : DomainInstance.Phase.ACTIVE;
   }

   public void setPhase(DomainInstance.Phase phase) {
      this.entityData.set(PHASE, phase.ordinal());
   }

   public int ownerId() {
      return (Integer)this.entityData.get(OWNER);
   }

   public boolean clashing() {
      return (Boolean)this.entityData.get(CLASHING);
   }

   public void setClashing(boolean clashing) {
      this.entityData.set(CLASHING, clashing);
   }

   public boolean contains(Vec3 point) {
      return point.distanceToSqr(this.position()) < this.radius() * this.radius();
   }

   public void tick() {
      super.tick();
      int phase = (Integer)this.entityData.get(PHASE);
      if (phase != this.lastPhase) {
         this.lastPhase = phase;
         this.phaseStartAge = this.tickCount;
      }
   }

   public AABB getBoundingBoxForCulling() {
      return new AABB(this.position(), this.position()).inflate(this.radius() + 2.0F);
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      double r = this.radius() + 128.0F;
      return distance < r * r;
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }
}
