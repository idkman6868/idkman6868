package com.curseddomain.landmark;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** One placed landmark: where it is, its ground level, and which chunks of it are already built. */
public final class Site {
   public static final int UNSET = Integer.MIN_VALUE;
   public final String id;
   public final LandmarkType type;
   public final int x;
   public final int z;
   public final int radius;
   public final long seed;
   /** Display name key; null means the landmark type's name. */
   @Nullable
   public final String nameKey;
   public int baseY = UNSET;
   public final Set<Long> built = new HashSet<>();
   @Nullable
   public UUID resident;

   public Site(String id, LandmarkType type, int x, int z, int radius, long seed, @Nullable String nameKey) {
      this.id = id;
      this.type = type;
      this.x = x;
      this.z = z;
      this.radius = radius;
      this.seed = seed;
      this.nameKey = nameKey;
   }

   public Component displayName() {
      return this.nameKey != null ? Component.translatable(this.nameKey) : this.type.displayName();
   }

   public int minChunkX() {
      return (this.x - this.radius) >> 4;
   }

   public int maxChunkX() {
      return (this.x + this.radius) >> 4;
   }

   public int minChunkZ() {
      return (this.z - this.radius) >> 4;
   }

   public int maxChunkZ() {
      return (this.z + this.radius) >> 4;
   }

   public int chunkCount() {
      return (this.maxChunkX() - this.minChunkX() + 1) * (this.maxChunkZ() - this.minChunkZ() + 1);
   }

   public boolean complete() {
      return this.built.size() >= this.chunkCount();
   }

   public boolean overlaps(int ox, int oz, int oradius, int margin) {
      return Math.abs(ox - this.x) < oradius + this.radius + margin && Math.abs(oz - this.z) < oradius + this.radius + margin;
   }

   public static long chunkKey(int cx, int cz) {
      return (long)cx & 4294967295L | ((long)cz & 4294967295L) << 32;
   }

   CompoundTag write() {
      CompoundTag tag = new CompoundTag();
      tag.putString("id", this.id);
      tag.putString("type", this.type.id());
      tag.putInt("x", this.x);
      tag.putInt("z", this.z);
      tag.putInt("radius", this.radius);
      tag.putLong("seed", this.seed);
      if (this.nameKey != null) {
         tag.putString("name", this.nameKey);
      }

      tag.putInt("base_y", this.baseY);
      long[] chunks = new long[this.built.size()];
      int i = 0;

      for (long key : this.built) {
         chunks[i++] = key;
      }

      tag.putLongArray("built", chunks);
      if (this.resident != null) {
         tag.putString("resident", this.resident.toString());
      }

      return tag;
   }

   @Nullable
   static Site read(CompoundTag tag) {
      LandmarkType type = LandmarkType.byId(tag.getString("type"));
      if (type == null) {
         return null;
      } else {
         Site site = new Site(
            tag.getString("id"), type, tag.getInt("x"), tag.getInt("z"), tag.getInt("radius"), tag.getLong("seed"), tag.contains("name") ? tag.getString("name") : null
         );
         site.baseY = tag.contains("base_y") ? tag.getInt("base_y") : UNSET;

         for (long key : tag.getLongArray("built")) {
            site.built.add(key);
         }

         if (tag.contains("resident")) {
            try {
               site.resident = UUID.fromString(tag.getString("resident"));
            } catch (IllegalArgumentException ignored) {
            }
         }

         return site;
      }
   }
}
