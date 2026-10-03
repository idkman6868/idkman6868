package com.curseddomain.sorcerer;

import com.curseddomain.network.payload.SorcererSyncPayload;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueRegistry;
import com.curseddomain.util.EnumNames;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public final class SorcererData implements INBTSerializable<CompoundTag> {
   private SorcererStatus status = SorcererStatus.NON_SORCERER;
   private Grade grade = Grade.UNGRADED;
   private int gradeXp;
   @Nullable
   private ResourceLocation technique;
   private boolean techniqueRevealed;
   private boolean rolled;
   private final EnumSet<InnateTrait> traits = EnumSet.noneOf(InnateTrait.class);
   private final Set<String> unlocks = new LinkedHashSet<>();
   @Nullable
   private Boolean lastSight;

   public SorcererStatus status() {
      return this.status;
   }

   public Grade grade() {
      return this.grade;
   }

   public int gradeXp() {
      return this.gradeXp;
   }

   public Optional<ResourceLocation> techniqueId() {
      return Optional.ofNullable(this.technique);
   }

   public Optional<Technique> technique() {
      return this.techniqueId().flatMap(TechniqueRegistry::get);
   }

   public boolean techniqueRevealed() {
      return this.techniqueRevealed;
   }

   public boolean rolled() {
      return this.rolled;
   }

   public boolean hasTrait(InnateTrait trait) {
      return this.traits.contains(trait);
   }

   public Set<InnateTrait> traits() {
      return Collections.unmodifiableSet(this.traits);
   }

   public boolean isUnlocked(String flag) {
      return this.unlocks.contains(flag);
   }

   public Set<String> unlocks() {
      return Collections.unmodifiableSet(this.unlocks);
   }

   public boolean hasCursedEnergy() {
      return this.status.awakened() && !this.hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL);
   }

   public boolean canSeeCurses() {
      return this.status.awakened() || this.hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL);
   }

   public boolean sightChanged() {
      boolean now = this.canSeeCurses();
      boolean changed = this.lastSight != null && this.lastSight != now;
      this.lastSight = now;
      return changed;
   }

   void setStatus(SorcererStatus status) {
      this.status = status;
   }

   void setGrade(Grade grade) {
      this.grade = grade;
   }

   void setGradeXp(int gradeXp) {
      this.gradeXp = Math.max(0, gradeXp);
   }

   void setTechnique(@Nullable ResourceLocation technique) {
      this.technique = technique;
   }

   void setTechniqueRevealed(boolean techniqueRevealed) {
      this.techniqueRevealed = techniqueRevealed;
   }

   void setRolled(boolean rolled) {
      this.rolled = rolled;
   }

   boolean addTrait(InnateTrait trait) {
      return this.traits.add(trait);
   }

   boolean removeTrait(InnateTrait trait) {
      return this.traits.remove(trait);
   }

   boolean unlock(String flag) {
      return this.unlocks.add(flag);
   }

   boolean lock(String flag) {
      return this.unlocks.remove(flag);
   }

   public SorcererSyncPayload clientView() {
      return new SorcererSyncPayload(
         this.status,
         this.grade,
         this.gradeXp,
         this.techniqueRevealed ? this.techniqueId() : Optional.empty(),
         this.techniqueRevealed ? List.copyOf(this.traits) : List.of(),
         List.copyOf(this.unlocks)
      );
   }

   public void applyClientView(SorcererSyncPayload view) {
      this.status = view.status();
      this.grade = view.grade();
      this.gradeXp = view.gradeXp();
      this.technique = view.technique().orElse(null);
      this.techniqueRevealed = this.technique != null;
      this.traits.clear();
      this.traits.addAll(view.traits());
      this.unlocks.clear();
      this.unlocks.addAll(view.unlocks());
   }

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putString("status", this.status.getSerializedName());
      tag.putString("grade", this.grade.getSerializedName());
      tag.putInt("grade_xp", this.gradeXp);
      if (this.technique != null) {
         tag.putString("technique", this.technique.toString());
      }

      tag.putBoolean("revealed", this.techniqueRevealed);
      tag.putBoolean("rolled", this.rolled);
      ListTag traitList = new ListTag();
      this.traits.forEach(t -> traitList.add(StringTag.valueOf(t.getSerializedName())));
      tag.put("traits", traitList);
      ListTag unlockList = new ListTag();
      this.unlocks.forEach(u -> unlockList.add(StringTag.valueOf(u)));
      tag.put("unlocks", unlockList);
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      this.status = EnumNames.byName(SorcererStatus.values(), tag.getString("status"), SorcererStatus.NON_SORCERER);
      this.grade = EnumNames.byName(Grade.values(), tag.getString("grade"), Grade.UNGRADED);
      this.gradeXp = tag.getInt("grade_xp");
      this.technique = tag.contains("technique") ? ResourceLocation.tryParse(tag.getString("technique")) : null;
      this.techniqueRevealed = tag.getBoolean("revealed");
      this.rolled = tag.getBoolean("rolled");
      this.traits.clear();
      ListTag traitList = tag.getList("traits", 8);

      for (int i = 0; i < traitList.size(); i++) {
         InnateTrait trait = EnumNames.byName(InnateTrait.values(), traitList.getString(i), null);
         if (trait != null) {
            this.traits.add(trait);
         }
      }

      this.unlocks.clear();
      ListTag unlockList = tag.getList("unlocks", 8);

      for (int ix = 0; ix < unlockList.size(); ix++) {
         this.unlocks.add(unlockList.getString(ix));
      }
   }
}
