package com.curseddomain.technique.ability;

import com.curseddomain.network.payload.AbilitySyncPayload;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public final class AbilityState implements INBTSerializable<CompoundTag> {
   final Map<ResourceLocation, Long> cooldownUntil = new HashMap<>();
   final Map<ResourceLocation, Integer> cooldownTotal = new HashMap<>();
   final Map<ResourceLocation, Long> activeSince = new LinkedHashMap<>();
   @Nullable
   ResourceLocation charging;
   long chargeStart;
   int[] slots = new int[]{0, 1, 2, 3};
   boolean dirty = true;
   @Nullable
   private AbilitySyncPayload clientView;
   private long clientSyncTick;

   public int slot(int index) {
      return this.slots[index];
   }

   @Nullable
   public AbilitySyncPayload clientView() {
      return this.clientView;
   }

   public long clientSyncTick() {
      return this.clientSyncTick;
   }

   public void applySync(AbilitySyncPayload payload, long clientTick) {
      this.clientView = payload;
      this.clientSyncTick = clientTick;
      this.slots = payload.slots().stream().mapToInt(Integer::intValue).toArray();
   }

   public boolean isActive(ResourceLocation ability) {
      return this.activeSince.containsKey(ability);
   }

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putIntArray("slots", this.slots);
      CompoundTag cooldowns = new CompoundTag();
      long now = AbilityManager.serverTick();
      this.cooldownUntil.forEach((id, until) -> {
         if (until > now) {
            cooldowns.putLong(id.toString(), until - now);
         }
      });
      tag.put("cooldowns", cooldowns);
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      int[] saved = tag.getIntArray("slots");
      this.slots = saved.length == 4 ? saved : new int[]{0, 1, 2, 3};
      this.cooldownUntil.clear();
      this.cooldownTotal.clear();
      CompoundTag cooldowns = tag.getCompound("cooldowns");
      long now = AbilityManager.serverTick();

      for (String key : cooldowns.getAllKeys()) {
         ResourceLocation id = ResourceLocation.tryParse(key);
         if (id != null) {
            long remaining = cooldowns.getLong(key);
            this.cooldownUntil.put(id, now + remaining);
            this.cooldownTotal.put(id, (int)remaining);
         }
      }

      this.dirty = true;
   }
}
