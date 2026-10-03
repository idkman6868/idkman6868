package com.curseddomain.world;

import com.curseddomain.config.ServerConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class SchoolLocation {
   private static final String DATA_NAME = "cursed_domain_school";
   private static final long SALT = 20922926024119115L;
   @Nullable
   private static volatile BlockPos current;

   private SchoolLocation() {
   }

   public static Optional<BlockPos> get() {
      return Optional.ofNullable(current);
   }

   public static BlockPos compute(long seed) {
      RandomSource random = RandomSource.create(seed ^ 20922926024119115L);
      int x;
      int z;
      switch ((SchoolSpawnMode)ServerConfig.JUJUTSU_HIGH_SPAWN_MODE.get()) {
         case FIXED_COORDS:
            x = (Integer)ServerConfig.JUJUTSU_HIGH_FIXED_X.get();
            z = (Integer)ServerConfig.JUJUTSU_HIGH_FIXED_Z.get();
            break;
         case RANDOM: {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int max = Math.max((Integer)ServerConfig.JUJUTSU_HIGH_MAX_DISTANCE.get(), 1);
            double distance = max + random.nextDouble() * max * 2.0;
            x = (int)(Math.cos(angle) * distance);
            z = (int)(Math.sin(angle) * distance);
            break;
         }
         default: {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int min = (Integer)ServerConfig.JUJUTSU_HIGH_MIN_DISTANCE.get();
            int max = Math.max(min, (Integer)ServerConfig.JUJUTSU_HIGH_MAX_DISTANCE.get());
            double distance = min + random.nextDouble() * (max - min);
            x = (int)(Math.cos(angle) * distance);
            z = (int)(Math.sin(angle) * distance);
         }
      }

      return new BlockPos((x >> 4 << 4) + 8, 0, (z >> 4 << 4) + 8);
   }

   @SubscribeEvent
   public static void onAboutToStart(ServerAboutToStartEvent event) {
      current = compute(event.getServer().getWorldData().worldGenOptions().seed());
   }

   @SubscribeEvent
   public static void onStarted(ServerStartedEvent event) {
      MinecraftServer server = event.getServer();
      SchoolLocation.Pinned pinned = (SchoolLocation.Pinned)server.overworld()
         .getDataStorage()
         .computeIfAbsent(SchoolLocation.Pinned.FACTORY, "cursed_domain_school");
      if (pinned.pos == null) {
         pinned.pos = current;
         pinned.setDirty();
      } else {
         current = pinned.pos;
      }
   }

   @SubscribeEvent
   public static void onStopped(ServerStoppedEvent event) {
      current = null;
   }

   public static void set(MinecraftServer server, BlockPos pos) {
      current = pos;
      SchoolLocation.Pinned pinned = (SchoolLocation.Pinned)server.overworld()
         .getDataStorage()
         .computeIfAbsent(SchoolLocation.Pinned.FACTORY, "cursed_domain_school");
      pinned.pos = pos;
      pinned.setDirty();
   }

   private static final class Pinned extends SavedData {
      static final Factory<SchoolLocation.Pinned> FACTORY = new Factory(SchoolLocation.Pinned::new, SchoolLocation.Pinned::load, null);
      @Nullable
      BlockPos pos;

      static SchoolLocation.Pinned load(CompoundTag tag, Provider provider) {
         SchoolLocation.Pinned data = new SchoolLocation.Pinned();
         if (tag.contains("x")) {
            data.pos = new BlockPos(tag.getInt("x"), 0, tag.getInt("z"));
         }

         return data;
      }

      public CompoundTag save(CompoundTag tag, Provider provider) {
         if (this.pos != null) {
            tag.putInt("x", this.pos.getX());
            tag.putInt("z", this.pos.getZ());
         }

         return tag;
      }
   }
}
