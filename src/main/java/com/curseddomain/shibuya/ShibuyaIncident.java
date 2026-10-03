package com.curseddomain.shibuya;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.CullingConfig;
import com.curseddomain.cullinggame.CullingGame;
import com.curseddomain.cullinggame.CullingGameData;
import com.curseddomain.cullinggame.CullingRegistries;
import com.curseddomain.cullinggame.Kogane;
import com.curseddomain.cullinggame.npc.NpcProfile;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.Vfx;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * October 31st. A curtain falls over Shibuya, curses pour in, the disaster curses and Mahito make their move,
 * Satoru Gojo is sealed in the Prison Realm, and Kenjaku walks away having released countless curses into Tokyo.
 */
public final class ShibuyaIncident {
   private static final long SALT = 31102018L;
   private static final String KENJAKU = "entity.cursed_domain.sorcerer_npc.kenjaku";
   private static final String NARRATOR = "shibuya.cursed_domain.narrator";
   private static final Set<UUID> INSIDE = new HashSet<>();

   private ShibuyaIncident() {
   }

   /** Where Shibuya is in this world, decided by the seed (the same way Jujutsu High is placed). */
   public static BlockPos defaultCenter(MinecraftServer server) {
      RandomSource random = RandomSource.create(server.overworld().getSeed() ^ SALT);
      double angle = random.nextDouble() * Math.PI * 2.0;
      double distance = 400.0 + random.nextDouble() * 400.0;
      int x = (int)(Math.cos(angle) * distance);
      int z = (int)(Math.sin(angle) * distance);
      return new BlockPos((x >> 4 << 4) + 8, 0, (z >> 4 << 4) + 8);
   }

   /** Anchor of the colony line: wherever the incident actually happened. */
   public static BlockPos anchor(MinecraftServer server) {
      ShibuyaData data = ShibuyaData.get(server);
      return data.center != null ? new BlockPos(data.center.getX(), 0, data.center.getZ()) : defaultCenter(server);
   }

   public static ShibuyaData data(MinecraftServer server) {
      return ShibuyaData.get(server);
   }

   // ------------------------------------------------------------------------------------------------ control

   public static boolean begin(MinecraftServer server, BlockPos center) {
      ShibuyaData data = data(server);
      if (data.running()) {
         return false;
      } else {
         ServerLevel level = server.overworld();
         data.state = ShibuyaData.State.PENDING;
         Vec3 top = CullingGame.surface(level, center.getX(), center.getZ());
         data.center = new BlockPos(center.getX(), (int)top.y, center.getZ());
         data.radius = CullingConfig.i(CullingConfig.SHIBUYA_CURTAIN_RADIUS);
         data.stage = ShibuyaData.Stage.VEIL;
         data.wave = 0;
         data.pendingSince = level.getGameTime();
         data.lastOccupied = level.getGameTime();
         data.stageStart = level.getGameTime();
         data.cullingStartAt = -1L;
         data.tracked.clear();
         data.participants.clear();
         data.setDirty();
         INSIDE.clear();
         List<ServerPlayer> players = server.getPlayerList().getPlayers();

         for (ServerPlayer player : players) {
            Kogane.title(
               player,
               Component.translatable("shibuya.cursed_domain.title").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}),
               Component.translatable("shibuya.cursed_domain.subtitle"),
               100
            );
            narrate(player, "begin.1");
            narrate(player, "begin.2");
            player.sendSystemMessage(
               Component.translatable("shibuya.cursed_domain.location", data.center.getX(), data.center.getZ()).withStyle(ChatFormatting.LIGHT_PURPLE)
            );
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 0.5F);
         }

         ModMain.LOGGER.info("[shibuya] curtain fell at {}", data.center);
         return true;
      }
   }

   /** Admin stop: lifts the curtain and removes the incident's curses. */
   public static void cancel(MinecraftServer server) {
      ShibuyaData data = data(server);
      discardTracked(server.overworld(), data);
      data.state = ShibuyaData.State.NONE;
      data.cullingStartAt = -1L;
      data.setDirty();
      INSIDE.clear();
   }

   /** Admin skip: finishes the current stage. */
   public static void skipStage(MinecraftServer server) {
      ShibuyaData data = data(server);
      if (data.state == ShibuyaData.State.ACTIVE) {
         discardTracked(server.overworld(), data);
         if (data.stage == ShibuyaData.Stage.PRISON_REALM) {
            end(server, data, true);
         } else {
            nextStage(server.overworld(), data);
         }
      }
   }

   // ------------------------------------------------------------------------------------------------ tick

   public static void tick(MinecraftServer server, int t) {
      ShibuyaData data = data(server);
      ServerLevel level = server.overworld();
      long now = level.getGameTime();
      if (data.state == ShibuyaData.State.ENDED && data.cullingStartAt >= 0L && now >= data.cullingStartAt) {
         data.cullingStartAt = -1L;
         data.setDirty();
         CullingGame.start(server, anchor(server));
      }

      if (t % 20 == 0 && data.state == ShibuyaData.State.NONE) {
         int day = CullingConfig.i(CullingConfig.SHIBUYA_AUTO_START_DAY);
         CullingGameData game = CullingGame.data(server);
         if (day > 0 && game.phase == CullingGameData.Phase.INACTIVE && now >= (long)(day - 1) * 24000L && level.isNight()) {
            begin(server, defaultCenter(server));
         }
      }

      if (data.running() && data.center != null) {
         if (t % 5 == 0) {
            enforceCurtain(level, data);
         }

         if (t % 10 == 0) {
            List<ServerPlayer> players = level.players();

            for (ServerPlayer player : players) {
               double dx = player.getX() - data.center.getX();
               double dz = player.getZ() - data.center.getZ();
               if (Math.abs(Math.sqrt(dx * dx + dz * dz) - data.radius) < 24.0) {
                  CullingGame.curtain(level, data.center.getX(), data.center.getZ(), data.radius, player);
               }
            }
         }

         if (t % 20 == 0) {
            progress(server, level, data, now);
         }
      }
   }

   /** The curtain keeps non-sorcerers in. Sorcerers come and go freely. */
   private static void enforceCurtain(ServerLevel level, ShibuyaData data) {
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (!player.isSpectator() && player.isAlive()) {
            boolean inside = data.inside(player.getX(), player.getZ());
            UUID id = player.getUUID();
            if (inside) {
               INSIDE.add(id);
            } else if (INSIDE.remove(id) && SorcererManager.get(player).status() == SorcererStatus.NON_SORCERER && !player.isCreative()) {
               CullingGame.place(player, data.center.getX(), data.center.getZ(), data.radius - 3.0);
               INSIDE.add(id);
               player.displayClientMessage(Component.translatable("shibuya.cursed_domain.curtain.sealed").withStyle(ChatFormatting.DARK_PURPLE), true);
            }
         }
      }
   }

   private static void progress(MinecraftServer server, ServerLevel level, ShibuyaData data, long now) {
      List<ServerPlayer> inside = new ArrayList<>();
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (!player.isSpectator() && data.inside(player.getX(), player.getZ())) {
            inside.add(player);
         }
      }

      long abandon = CullingConfig.i(CullingConfig.SHIBUYA_ABANDON_DAYS) * 24000L;
      if (inside.isEmpty()) {
         long since = data.state == ShibuyaData.State.PENDING ? data.pendingSince : data.lastOccupied;
         if (now - since > abandon) {
            abandoned(server, data);
         }
      } else {
         data.lastOccupied = now;

         for (ServerPlayer player : inside) {
            if (data.participants.add(player.getUUID())) {
               data.setDirty();
            }
         }

         if (data.state == ShibuyaData.State.PENDING) {
            data.state = ShibuyaData.State.ACTIVE;
            data.stage = ShibuyaData.Stage.VEIL;
            data.wave = 0;
            data.stageStart = now;
            data.setDirty();

            for (ServerPlayer player : players) {
               narrate(player, "active");
            }

            spawnWave(level, data, inside);
         } else {
            boolean cleared = aliveTracked(level, data) == 0;
            long elapsed = now - data.stageStart;
            switch (data.stage) {
               case VEIL:
                  if (cleared || elapsed > 2400L) {
                     if (data.wave < 2) {
                        data.wave++;
                        data.stageStart = now;
                        data.setDirty();
                        spawnWave(level, data, inside);
                     } else {
                        nextStage(level, data);
                     }
                  }
                  break;
               case DISASTER_CURSES:
               case MAHITO:
                  if (cleared || elapsed > 12000L) {
                     nextStage(level, data);
                  }
                  break;
               case PRISON_REALM:
                  if (cleared) {
                     end(server, data, true);
                  }
            }
         }
      }
   }

   private static void spawnWave(ServerLevel level, ShibuyaData data, List<ServerPlayer> inside) {
      int count = 4 + data.wave * 2 + Math.min(inside.size(), 4) * 2;

      for (int i = 0; i < count; i++) {
         ServerPlayer near = inside.get(level.random.nextInt(inside.size()));
         Vec3 at = CullingGame.around(level, near, 10.0, 20.0);
         float roll = level.random.nextFloat();
         CurseVariant variant;
         if (data.wave == 2 && i == 0) {
            variant = CurseVariant.GRADE_1;
         } else if (roll < 0.15F + data.wave * 0.1F) {
            variant = CurseVariant.GRADE_2;
         } else {
            variant = CurseVariant.GRADE_3;
         }

         RampantCurse curse = RampantCurse.spawn(level, variant, at, true);
         if (curse != null) {
            curse.setTarget(near);
            data.tracked.add(curse.getUUID());
         }
      }

      data.setDirty();
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (data.inside(player.getX(), player.getZ())) {
            player.displayClientMessage(Component.translatable("shibuya.cursed_domain.wave", data.wave + 1).withStyle(ChatFormatting.DARK_PURPLE), true);
         }
      }
   }

   private static void nextStage(ServerLevel level, ShibuyaData data) {
      data.tracked.clear();
      data.stageStart = level.getGameTime();
      List<ServerPlayer> inside = new ArrayList<>();
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (data.inside(player.getX(), player.getZ()) && !player.isSpectator()) {
            inside.add(player);
         }
      }

      ServerPlayer focus = inside.isEmpty() ? null : inside.get(level.random.nextInt(inside.size()));
      Vec3 base = focus != null ? CullingGame.around(level, focus, 12.0, 18.0) : CullingGame.surface(level, data.center.getX(), data.center.getZ());
      switch (data.stage) {
         case VEIL:
            data.stage = ShibuyaData.Stage.DISASTER_CURSES;

            for (ServerPlayer player : players) {
               narrate(player, "disasters");
            }

            CurseVariant[] disasters = new CurseVariant[]{CurseVariant.JOGO, CurseVariant.HANAMI, CurseVariant.DAGON};

            for (int i = 0; i < disasters.length; i++) {
               Vec3 at = focus != null ? CullingGame.around(level, focus, 12.0, 20.0) : base.add(i * 4.0, 0.0, 0.0);
               RampantCurse curse = RampantCurse.spawn(level, disasters[i], at, true);
               if (curse != null) {
                  if (focus != null) {
                     curse.setTarget(focus);
                  }

                  data.tracked.add(curse.getUUID());
               }
            }
            break;
         case DISASTER_CURSES:
            data.stage = ShibuyaData.Stage.MAHITO;

            for (ServerPlayer player : players) {
               narrate(player, "mahito");
            }

            RampantCurse mahito = RampantCurse.spawn(level, CurseVariant.MAHITO, base, true);
            if (mahito != null) {
               if (focus != null) {
                  mahito.setTarget(focus);
               }

               data.tracked.add(mahito.getUUID());
            }
            break;
         case MAHITO:
            beginPrisonRealm(level, data, base, focus);
            break;
         default:
      }

      data.setDirty();
   }

   private static void beginPrisonRealm(ServerLevel level, ShibuyaData data, Vec3 at, @Nullable LivingEntity focus) {
      data.stage = ShibuyaData.Stage.PRISON_REALM;
      data.tracked.clear();
      data.stageStart = level.getGameTime();
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         Kogane.title(
            player,
            Component.translatable("shibuya.cursed_domain.prison_realm.title").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}),
            Component.translatable("shibuya.cursed_domain.prison_realm.subtitle"),
            90
         );
         narrate(player, "prison_realm");
      }

      SorcererNpcEntity kenjaku = SorcererNpcEntity.spawn(level, NpcProfile.KENJAKU, at, 0, true);
      if (kenjaku != null) {
         if (focus != null) {
            kenjaku.setTarget(focus);
         }

         data.tracked.add(kenjaku.getUUID());

         for (ServerPlayer player : players) {
            if (data.inside(player.getX(), player.getZ())) {
               Kogane.speech(player, KENJAKU, Component.translatable("shibuya.cursed_domain.kenjaku.arrive"), ChatFormatting.DARK_RED);
            }
         }
      }

      data.setDirty();
   }

   // ------------------------------------------------------------------------------------------------ hooks

   /** Mahito falls low: Kenjaku appears and takes him with Cursed Spirit Manipulation. */
   public static void mahitoAbsorbed(ServerLevel level, RampantCurse mahito) {
      ShibuyaData data = data(level.getServer());
      Vec3 at = mahito.position();
      Vfx.implode(level, at.add(0.0, 1.0, 0.0), -9819606, 4.0F, 30);
      level.playSound(null, mahito.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 2.0F, 0.6F);
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (player.distanceToSqr(at) < 4096.0) {
            Kogane.speech(player, KENJAKU, Component.translatable("shibuya.cursed_domain.kenjaku.absorb"), ChatFormatting.DARK_RED);
         }
      }

      mahito.closeBar();
      mahito.discard();
      if (data.state == ShibuyaData.State.ACTIVE && data.stage == ShibuyaData.Stage.MAHITO) {
         LivingEntity focus = mahito.getTarget();
         beginPrisonRealm(level, data, at, focus);
      }
   }

   /** Kenjaku has had his fun: he drops the Prison Realm's front gate, lets loose his curses and leaves. */
   public static void kenjakuEscapes(ServerLevel level, SorcererNpcEntity kenjaku) {
      MinecraftServer server = level.getServer();
      ShibuyaData data = data(server);
      Vec3 at = kenjaku.position();
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (player.distanceToSqr(at) < 9216.0) {
            Kogane.speech(player, KENJAKU, Component.translatable("shibuya.cursed_domain.kenjaku.escape"), ChatFormatting.DARK_RED);
         }
      }

      kenjaku.spawnAtLocation(new ItemStack((ItemLike)CullingRegistries.PRISON_REALM.get()));

      for (int i = 0; i < 10; i++) {
         Vec3 spot = at.add(level.random.nextGaussian() * 6.0, 0.0, level.random.nextGaussian() * 6.0);
         RampantCurse.spawn(level, level.random.nextInt(3) == 0 ? CurseVariant.GRADE_2 : CurseVariant.GRADE_3, CullingGame.surface(level, spot.x, spot.z), false);
      }

      kenjaku.vanish(level);
      if (data.state == ShibuyaData.State.ACTIVE) {
         end(server, data, true);
      }
   }

   private static void abandoned(MinecraftServer server, ShibuyaData data) {
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         narrate(player, "abandoned");
      }

      discardTracked(server.overworld(), data);
      end(server, data, false);
   }

   private static void end(MinecraftServer server, ShibuyaData data, boolean fought) {
      data.state = ShibuyaData.State.ENDED;
      data.tracked.clear();
      INSIDE.clear();
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         Kogane.title(
            player,
            Component.translatable("shibuya.cursed_domain.end.title").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}),
            Component.translatable("shibuya.cursed_domain.end.subtitle"),
            100
         );
         narrate(player, "end");
         Vfx.screen(player, ScreenFxPayload.Kind.TINT, -9819606, 0.4F, 60);
         if (fought && data.participants.contains(player.getUUID())) {
            SorcererManager.addGradeXp(player, 150);
            player.sendSystemMessage(Component.translatable("shibuya.cursed_domain.survived").withStyle(ChatFormatting.GRAY));
         }
      }

      if (CullingConfig.b(CullingConfig.SHIBUYA_STARTS_CULLING_GAME) && !CullingGame.data(server).active()) {
         data.cullingStartAt = server.overworld().getGameTime() + CullingConfig.i(CullingConfig.CULLING_START_DELAY_SECONDS) * 20L;
      }

      data.setDirty();
      ModMain.LOGGER.info("[shibuya] incident over (fought: {})", fought);
   }

   // ------------------------------------------------------------------------------------------------ helpers

   private static int aliveTracked(ServerLevel level, ShibuyaData data) {
      int alive = 0;

      for (UUID id : data.tracked) {
         Entity entity = level.getEntity(id);
         if (entity != null && entity.isAlive()) {
            alive++;
         }
      }

      return alive;
   }

   private static void discardTracked(ServerLevel level, ShibuyaData data) {
      for (UUID id : data.tracked) {
         Entity entity = level.getEntity(id);
         if (entity instanceof RampantCurse curse) {
            curse.closeBar();
            curse.discard();
         } else if (entity instanceof SorcererNpcEntity npc) {
            npc.vanish(level);
         } else if (entity != null) {
            entity.discard();
         }
      }

      data.tracked.clear();
      data.setDirty();
   }

   private static void narrate(ServerPlayer player, String key) {
      player.sendSystemMessage(
         Component.translatable(NARRATOR)
            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD})
            .append(Component.literal(" "))
            .append(Component.translatable("shibuya.cursed_domain." + key).withStyle(new ChatFormatting[]{ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC}))
      );
   }

   public static void forget(UUID player) {
      INSIDE.remove(player);
   }
}
