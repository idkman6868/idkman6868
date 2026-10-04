package com.steelballrun.horse;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Racehorse data attached to a horse: its hidden stats (rolled the first time anyone rides it), its stamina and its
 * bond with its rider.
 */
public final class HorseData implements INBTSerializable<CompoundTag> {
   public static final String[] BREEDS = {"appaloosa", "quarter_horse", "thoroughbred", "mustang", "arabian", "morgan"};
   private static final Affinity[] BREED_AFFINITY = {Affinity.PLAINS, Affinity.PLAINS, Affinity.PLAINS, Affinity.DESERT, Affinity.DESERT, Affinity.MOUNTAIN};

   public boolean rolled;
   /** Maximum stamina before bond, as a multiple of the configured base (0.8-1.2). */
   public double staminaStat = 1.0;
   /** Recovery speed multiplier (0.75-1.3). */
   public double endurance = 1.0;
   public int breed;
   public Affinity affinity = Affinity.PLAINS;
   /** 0-100. Rises with riding, feeding and brushing. */
   public int bond;
   public double stamina = -1.0;
   public boolean exhausted;
   /** Game time of the last stamina update, for recovery while nobody rides the horse. */
   public long lastUpdate;
   /** Blocks ridden since the last bond point from riding. */
   public double ridden;
   public long feedDay = -1L;
   public int feedsToday;
   public long brushDay = -1L;

   public void roll(RandomSource random) {
      this.rolled = true;
      this.staminaStat = 0.8 + random.nextDouble() * 0.4;
      this.endurance = 0.75 + random.nextDouble() * 0.55;
      this.breed = random.nextInt(BREEDS.length);
      this.affinity = random.nextInt(4) == 0 ? Affinity.ALL[random.nextInt(Affinity.ALL.length)] : BREED_AFFINITY[this.breed];
   }

   public String breedKey() {
      return "breed.steel_ball_run." + BREEDS[Math.max(0, Math.min(BREEDS.length - 1, this.breed))];
   }

   public double max(double base) {
      return Stamina.maxFor(base * this.staminaStat, this.bond);
   }

   public void addBond(int amount) {
      this.bond = Math.max(0, Math.min(100, this.bond + amount));
   }

   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("rolled", this.rolled);
      tag.putDouble("staminaStat", this.staminaStat);
      tag.putDouble("endurance", this.endurance);
      tag.putInt("breed", this.breed);
      tag.putInt("affinity", this.affinity.ordinal());
      tag.putInt("bond", this.bond);
      tag.putDouble("stamina", this.stamina);
      tag.putBoolean("exhausted", this.exhausted);
      tag.putLong("lastUpdate", this.lastUpdate);
      tag.putDouble("ridden", this.ridden);
      tag.putLong("feedDay", this.feedDay);
      tag.putInt("feedsToday", this.feedsToday);
      tag.putLong("brushDay", this.brushDay);
      return tag;
   }

   @Override
   public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
      this.rolled = tag.getBoolean("rolled");
      this.staminaStat = tag.contains("staminaStat") ? tag.getDouble("staminaStat") : 1.0;
      this.endurance = tag.contains("endurance") ? tag.getDouble("endurance") : 1.0;
      this.breed = tag.getInt("breed");
      int a = tag.getInt("affinity");
      this.affinity = a >= 0 && a < Affinity.ALL.length ? Affinity.ALL[a] : Affinity.PLAINS;
      this.bond = tag.getInt("bond");
      this.stamina = tag.contains("stamina") ? tag.getDouble("stamina") : -1.0;
      this.exhausted = tag.getBoolean("exhausted");
      this.lastUpdate = tag.getLong("lastUpdate");
      this.ridden = tag.getDouble("ridden");
      this.feedDay = tag.contains("feedDay") ? tag.getLong("feedDay") : -1L;
      this.feedsToday = tag.getInt("feedsToday");
      this.brushDay = tag.contains("brushDay") ? tag.getLong("brushDay") : -1L;
   }
}
