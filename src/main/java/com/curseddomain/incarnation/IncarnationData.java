package com.curseddomain.incarnation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

/** Players who are currently twenty fingers, waiting to wake up in a new body. Saved so a restart can't strand them. */
public final class IncarnationData extends SavedData {
   private static final String NAME = "cursed_domain_incarnations";
   static final Factory<IncarnationData> FACTORY = new Factory<IncarnationData>(
      IncarnationData::new, (tag, provider) -> load((CompoundTag)tag, (Provider)provider), null
   );
   final Map<UUID, IncarnationData.Pending> pending = new LinkedHashMap<>();

   public static IncarnationData get(MinecraftServer server) {
      return (IncarnationData)server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
   }

   record Pending(long wakeAt, String previousMode) {
   }

   static IncarnationData load(CompoundTag tag, Provider provider) {
      IncarnationData data = new IncarnationData();
      CompoundTag list = tag.getCompound("pending");

      for (String key : list.getAllKeys()) {
         try {
            CompoundTag p = list.getCompound(key);
            data.pending.put(UUID.fromString(key), new IncarnationData.Pending(p.getLong("wake"), p.getString("mode")));
         } catch (IllegalArgumentException ignored) {
         }
      }

      return data;
   }

   public CompoundTag save(CompoundTag tag, Provider provider) {
      CompoundTag list = new CompoundTag();

      for (Map.Entry<UUID, IncarnationData.Pending> entry : this.pending.entrySet()) {
         CompoundTag p = new CompoundTag();
         p.putLong("wake", entry.getValue().wakeAt());
         p.putString("mode", entry.getValue().previousMode());
         list.put(entry.getKey().toString(), p);
      }

      tag.put("pending", list);
      return tag;
   }
}
