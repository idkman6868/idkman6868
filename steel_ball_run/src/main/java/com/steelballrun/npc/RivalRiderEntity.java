package com.steelballrun.npc;

import com.steelballrun.race.Entrant;
import com.steelballrun.race.RaceData;
import com.steelballrun.race.RaceManager;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The body of a rival racer while a player is near. Steers its horse (or its own feet, for Sandman) along the course.
 * Bodies are never kept across restarts: one loaded from disk removes itself, and simulation carries on.
 */
public class RivalRiderEntity extends PathfinderMob {
   private static final EntityDataAccessor<Integer> SKIN = SynchedEntityData.defineId(RivalRiderEntity.class, EntityDataSerializers.INT);
   @Nullable
   private UUID entrant;
   private boolean stale;

   public RivalRiderEntity(EntityType<? extends RivalRiderEntity> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 48.0);
   }

   void setup(Entrant e, Rivals.Profile profile) {
      this.entrant = e.id;
      this.entityData.set(SKIN, Rivals.skinIndex(profile));
      this.setCustomName(Component.literal(profile.name() + " #" + e.number));
      this.setCustomNameVisible(true);
   }

   public int skin() {
      return this.entityData.get(SKIN);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SKIN, 0);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel level && this.tickCount % 10 == 0) {
         if (this.stale || this.entrant == null) {
            this.removeWithMount();
            return;
         }
         RaceData d = RaceData.get(level.getServer());
         Entrant e = d.entrant(this.entrant);
         if (e == null) {
            this.removeWithMount();
         } else if (d.phase == RaceData.Phase.RUNNING && e.racing()) {
            double[] target = RivalManager.point(d.route(), e, 20.0);
            BlockPos column = BlockPos.containing(target[0], 64.0, target[1]);
            double y = level.isLoaded(column) ? RaceManager.surface(level, target[0], target[1]) : this.getY();
            Mob mover = this.getVehicle() instanceof Mob vehicle ? vehicle : this;
            mover.getNavigation().moveTo(target[0], y, target[1], Math.max(0.3, Math.min(1.4, RivalManager.paceNow(level, e))));
         }
      }
   }

   private void removeWithMount() {
      Entity vehicle = this.getVehicle();
      if (vehicle != null) {
         vehicle.discard();
      }
      this.discard();
   }

   @Override
   public void die(DamageSource source) {
      super.die(source);
      if (this.level() instanceof ServerLevel level && this.entrant != null) {
         RivalManager.onKilled(level.getServer(), this.entrant);
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.stale = true;
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }
}
