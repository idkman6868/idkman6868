package com.curseddomain.landmark;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;

/** Every landmark placed in this world. */
public final class AtlasData extends SavedData {
   private static final String NAME = "cursed_domain_atlas";
   static final Factory<AtlasData> FACTORY = new Factory<AtlasData>(AtlasData::new, (tag, provider) -> load((CompoundTag)tag, (Provider)provider), null);
   public final Map<String, Site> sites = new LinkedHashMap<>();
   /** Version of the default landmark set already placed; lets later updates add new landmarks to old worlds. */
   public int defaultsVersion;

   public static AtlasData get(MinecraftServer server) {
      return (AtlasData)server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
   }

   static AtlasData load(CompoundTag tag, Provider provider) {
      AtlasData data = new AtlasData();
      data.defaultsVersion = tag.getInt("defaults");
      ListTag list = tag.getList("sites", 10);

      for (int i = 0; i < list.size(); i++) {
         Site site = Site.read(list.getCompound(i));
         if (site != null) {
            data.sites.put(site.id, site);
         }
      }

      return data;
   }

   public CompoundTag save(CompoundTag tag, Provider provider) {
      tag.putInt("defaults", this.defaultsVersion);
      ListTag list = new ListTag();

      for (Site site : this.sites.values()) {
         list.add(site.write());
      }

      tag.put("sites", list);
      return tag;
   }
}
