package com.curseddomain.shikigami;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class ShadowStorage implements INBTSerializable<CompoundTag> {
   public final SimpleContainer container = new SimpleContainer(27);

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.put("items", this.container.createTag(provider));
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      this.container.clearContent();
      this.container.fromTag(tag.getList("items", 10), provider);
   }
}
