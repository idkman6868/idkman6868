package com.curseddomain.combat;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class CombatRecord implements INBTSerializable<CompoundTag> {
   public int blackFlashes;
   public int cursesExorcised;
   public int domainsExpanded;
   public int clashesWon;

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("black_flashes", this.blackFlashes);
      tag.putInt("curses_exorcised", this.cursesExorcised);
      tag.putInt("domains", this.domainsExpanded);
      tag.putInt("clashes_won", this.clashesWon);
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      this.blackFlashes = tag.getInt("black_flashes");
      this.cursesExorcised = tag.getInt("curses_exorcised");
      this.domainsExpanded = tag.getInt("domains");
      this.clashesWon = tag.getInt("clashes_won");
   }
}
