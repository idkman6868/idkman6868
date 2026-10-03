package com.curseddomain.shibuya;

import com.curseddomain.util.EnumNames;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import org.jetbrains.annotations.Nullable;

/** Saved state of the Shibuya Incident. */
public final class ShibuyaData extends SavedData {
   private static final String NAME = "cursed_domain_shibuya";
   static final Factory<ShibuyaData> FACTORY = new Factory<ShibuyaData>(ShibuyaData::new, (tag, provider) -> load((CompoundTag)tag, (Provider)provider), null);
   public ShibuyaData.State state = ShibuyaData.State.NONE;
   @Nullable
   public BlockPos center;
   public int radius = 64;
   public ShibuyaData.Stage stage = ShibuyaData.Stage.VEIL;
   public int wave;
   public long stageStart;
   public long pendingSince;
   public long lastOccupied;
   public long cullingStartAt = -1L;
   public final List<UUID> tracked = new ArrayList<>();
   public final Set<UUID> participants = new LinkedHashSet<>();

   public static ShibuyaData get(MinecraftServer server) {
      return (ShibuyaData)server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
   }

   public boolean running() {
      return this.state == ShibuyaData.State.PENDING || this.state == ShibuyaData.State.ACTIVE;
   }

   public boolean inside(double x, double z) {
      if (this.center == null) {
         return false;
      } else {
         double dx = x - this.center.getX();
         double dz = z - this.center.getZ();
         return dx * dx + dz * dz < (double)this.radius * this.radius;
      }
   }

   static ShibuyaData load(CompoundTag tag, Provider provider) {
      ShibuyaData data = new ShibuyaData();
      data.state = EnumNames.byName(ShibuyaData.State.values(), tag.getString("state"), ShibuyaData.State.NONE);
      if (tag.contains("x")) {
         data.center = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
      }

      data.radius = tag.contains("radius") ? tag.getInt("radius") : 64;
      data.stage = EnumNames.byName(ShibuyaData.Stage.values(), tag.getString("stage"), ShibuyaData.Stage.VEIL);
      data.wave = tag.getInt("wave");
      data.stageStart = tag.getLong("stage_start");
      data.pendingSince = tag.getLong("pending_since");
      data.lastOccupied = tag.getLong("last_occupied");
      data.cullingStartAt = tag.contains("culling_at") ? tag.getLong("culling_at") : -1L;
      readIds(tag.getList("tracked", 8), data.tracked);
      List<UUID> people = new ArrayList<>();
      readIds(tag.getList("participants", 8), people);
      data.participants.addAll(people);
      return data;
   }

   private static void readIds(ListTag list, List<UUID> into) {
      for (int i = 0; i < list.size(); i++) {
         try {
            into.add(UUID.fromString(list.getString(i)));
         } catch (IllegalArgumentException ignored) {
         }
      }
   }

   private static ListTag writeIds(Iterable<UUID> ids) {
      ListTag list = new ListTag();

      for (UUID id : ids) {
         list.add(StringTag.valueOf(id.toString()));
      }

      return list;
   }

   public CompoundTag save(CompoundTag tag, Provider provider) {
      tag.putString("state", this.state.getSerializedName());
      if (this.center != null) {
         tag.putInt("x", this.center.getX());
         tag.putInt("y", this.center.getY());
         tag.putInt("z", this.center.getZ());
      }

      tag.putInt("radius", this.radius);
      tag.putString("stage", this.stage.getSerializedName());
      tag.putInt("wave", this.wave);
      tag.putLong("stage_start", this.stageStart);
      tag.putLong("pending_since", this.pendingSince);
      tag.putLong("last_occupied", this.lastOccupied);
      tag.putLong("culling_at", this.cullingStartAt);
      tag.put("tracked", writeIds(this.tracked));
      tag.put("participants", writeIds(this.participants));
      return tag;
   }

   public static enum State implements net.minecraft.util.StringRepresentable {
      NONE,
      PENDING,
      ACTIVE,
      ENDED;

      public String getSerializedName() {
         return EnumNames.lower(this);
      }
   }

   public static enum Stage implements net.minecraft.util.StringRepresentable {
      VEIL,
      DISASTER_CURSES,
      MAHITO,
      PRISON_REALM;

      public String getSerializedName() {
         return EnumNames.lower(this);
      }
   }
}
