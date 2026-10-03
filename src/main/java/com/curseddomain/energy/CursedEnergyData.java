package com.curseddomain.energy;

import com.curseddomain.network.payload.CursedEnergySyncPayload;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class CursedEnergyData implements INBTSerializable<CompoundTag> {
   private float current;
   private float trainedMax;
   private float trainedRegen;
   private float trainedOutput;
   private float trainedControl;
   private float max;
   private float regenPerSecond;
   private float output;
   private float control;
   private CursedEnergyNature nature = CursedEnergyNature.STANDARD;
   private long lastCombatTick = -4611686018427387904L;
   private int stillTicks;
   private Vec3 lastPos = Vec3.ZERO;
   private boolean dirty = true;
   private boolean urgent = true;
   private long lastSyncTick = -4611686018427387904L;
   private boolean meditating;
   private boolean inCombat;

   public float current() {
      return this.current;
   }

   public float max() {
      return this.max;
   }

   public float regenPerSecond() {
      return this.regenPerSecond;
   }

   public float output() {
      return this.output;
   }

   public float control() {
      return this.control;
   }

   public CursedEnergyNature nature() {
      return this.nature;
   }

   public float fraction() {
      return this.max <= 0.0F ? 0.0F : this.current / this.max;
   }

   public boolean meditating() {
      return this.meditating;
   }

   public boolean inCombat() {
      return this.inCombat;
   }

   public float trainedMax() {
      return this.trainedMax;
   }

   public float trainedRegen() {
      return this.trainedRegen;
   }

   public float trainedOutput() {
      return this.trainedOutput;
   }

   public float trainedControl() {
      return this.trainedControl;
   }

   void setCurrent(float value) {
      float clamped = Math.max(0.0F, Math.min(this.max, value));
      if (clamped != this.current) {
         this.current = clamped;
         this.dirty = true;
      }
   }

   void setDerived(float max, float regenPerSecond, float output, float control, CursedEnergyNature nature) {
      this.max = max;
      this.regenPerSecond = regenPerSecond;
      this.output = output;
      this.control = control;
      this.nature = nature;
      this.current = Math.min(this.current, max);
      this.markDirty(true);
   }

   void addTraining(float max, float regen, float output, float control) {
      this.trainedMax += max;
      this.trainedRegen += regen;
      this.trainedOutput += output;
      this.trainedControl += control;
   }

   long lastCombatTick() {
      return this.lastCombatTick;
   }

   void setLastCombatTick(long tick) {
      this.lastCombatTick = tick;
      this.stillTicks = 0;
   }

   int stillTicks() {
      return this.stillTicks;
   }

   void trackStillness(boolean still, int ticks, Vec3 pos) {
      this.stillTicks = still && pos.distanceToSqr(this.lastPos) < 1.0E-4 ? this.stillTicks + ticks : 0;
      this.lastPos = pos;
   }

   void setFlags(boolean meditating, boolean inCombat) {
      if (meditating != this.meditating || inCombat != this.inCombat) {
         this.meditating = meditating;
         this.inCombat = inCombat;
         this.dirty = true;
      }
   }

   void markDirty(boolean urgent) {
      this.dirty = true;
      this.urgent |= urgent;
   }

   boolean needsSync(long now, int interval) {
      return this.dirty && (this.urgent || now - this.lastSyncTick >= interval);
   }

   void markSynced(long now) {
      this.dirty = false;
      this.urgent = false;
      this.lastSyncTick = now;
   }

   public void applySync(CursedEnergySyncPayload sync) {
      this.current = sync.current();
      this.max = sync.max();
      this.output = sync.output();
      this.control = sync.control();
      this.nature = sync.nature();
      this.meditating = sync.meditating();
      this.inCombat = sync.inCombat();
   }

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putFloat("current", this.current);
      tag.putFloat("trained_max", this.trainedMax);
      tag.putFloat("trained_regen", this.trainedRegen);
      tag.putFloat("trained_output", this.trainedOutput);
      tag.putFloat("trained_control", this.trainedControl);
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      this.current = tag.getFloat("current");
      this.trainedMax = tag.getFloat("trained_max");
      this.trainedRegen = tag.getFloat("trained_regen");
      this.trainedOutput = tag.getFloat("trained_output");
      this.trainedControl = tag.getFloat("trained_control");
   }
}
