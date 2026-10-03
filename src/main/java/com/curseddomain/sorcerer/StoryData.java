package com.curseddomain.sorcerer;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class StoryData implements INBTSerializable<CompoundTag> {
   public int curseHits;
   public boolean letterRead;
   public float hintOffsetDegrees;
   public long hintRolledAt = -4611686018427387904L;
   public long scoutMessageAt = -1L;
   public long interviewCooldownUntil;

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("curse_hits", this.curseHits);
      tag.putBoolean("letter_read", this.letterRead);
      tag.putFloat("hint_offset", this.hintOffsetDegrees);
      tag.putLong("hint_rolled_at", this.hintRolledAt);
      tag.putLong("scout_at", this.scoutMessageAt);
      tag.putLong("interview_cooldown", this.interviewCooldownUntil);
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      this.curseHits = tag.getInt("curse_hits");
      this.letterRead = tag.getBoolean("letter_read");
      this.hintOffsetDegrees = tag.getFloat("hint_offset");
      this.hintRolledAt = tag.contains("hint_rolled_at") ? tag.getLong("hint_rolled_at") : -4611686018427387904L;
      this.scoutMessageAt = tag.contains("scout_at") ? tag.getLong("scout_at") : -1L;
      this.interviewCooldownUntil = tag.getLong("interview_cooldown");
   }
}
