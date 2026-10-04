package com.steelballrun.race;

import com.steelballrun.SteelBallRun;
import com.steelballrun.config.SbrConfig;
import com.steelballrun.horse.HorseFactory;
import com.steelballrun.network.RaceStatusPayload;
import com.steelballrun.npc.RivalManager;
import com.steelballrun.npc.Rivals;
import com.steelballrun.npc.StephenSteelEntity;
import com.steelballrun.registry.SbrEntities;
import com.steelballrun.registry.SbrItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/** Runs the race: registration, the starting gun, checkpoints, points, the finish and the race panel. */
@EventBusSubscriber(modid = SteelBallRun.MODID)
public final class RaceManager {
   private static final ResourceLocation RUNNER_SPEED = SteelBallRun.id("native_runner");
   private static final Map<UUID, Long> MISSED_WARNING = new HashMap<>();

   private RaceManager() {
   }

   /** Forget per-session state so a second world opened in the same game starts clean. */
   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      MISSED_WARNING.clear();
      RaceRules.clearSession();
      RivalManager.clearSession();
      com.steelballrun.horse.HorseCare.clearSession();
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent.Post event) {
      MinecraftServer server = event.getServer();
      ServerLevel level = server.overworld();
      RaceData d = RaceData.get(server);
      int tick = server.getTickCount();
      if (!d.laidOut) {
         BlockPos spawn = level.getSharedSpawnPos();
         d.layOut(spawn.getX() + SbrConfig.START_OFFSET.get() + 0.5, spawn.getZ() + 0.5, SbrConfig.ROUTE_SCALE.get());
         SteelBallRun.LOGGER.info("Steel Ball Run course laid out from {} {}, {} blocks long", (int)d.originX, (int)d.originZ, (int)d.route().length());
      }
      if (!d.rivalsSeeded) {
         seedRivals(d, level);
      }

      long now = level.getGameTime();
      if (tick % 20 == 0) {
         switch (d.phase) {
            case REGISTRATION -> {
               if (SbrConfig.AUTO_START.get() && level.getDayTime() / 24000L >= SbrConfig.START_DAY.get() && hasPlayerEntrant(d)) {
                  startCountdown(server, d);
               }
            }
            case COUNTDOWN -> countdownStep(server, level, d, now);
            case RUNNING -> {
               RivalManager.simulate(level, d, now, 1.0);
               checkFinish(server, d, now);
            }
            case FINISHED -> {
            }
         }
      }
      if (tick % 10 == 5) {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            playerTick(level, d, player, now);
         }
         if (SbrConfig.BUILD_GATES.get()) {
            GateBuilder.tick(level, d);
         }
      }
      if (tick % 40 == 7) {
         RivalManager.manageBodies(level, d, now);
      }
      if (tick % 20 == 10) {
         syncAll(server, level, d, now);
      }
   }

   private static boolean hasPlayerEntrant(RaceData d) {
      for (Entrant e : d.entrants.values()) {
         if (!e.isRival()) {
            return true;
         }
      }
      return false;
   }

   static void seedRivals(RaceData d, ServerLevel level) {
      d.rivalsSeeded = true;
      int count = Math.min(SbrConfig.RIVAL_COUNT.get(), Rivals.ALL.length);
      for (int i = 0; i < count; i++) {
         Rivals.Profile p = Rivals.ALL[i];
         UUID id = UUID.nameUUIDFromBytes(("steel_ball_run:rival:" + p.id()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
         if (!d.entrants.containsKey(id)) {
            Entrant e = new Entrant(id, p.name(), d.nextNumber++, i, null);
            e.progress = -4.0 - (i % 6) * 3.0;
            e.pace = rollPace(level, p);
            d.entrants.put(id, e);
         }
      }
      d.setDirty();
   }

   public static double rollPace(Level level, Rivals.Profile p) {
      double r = level.getRandom().nextDouble();
      return "pocoloco".equals(p.id()) ? 0.85 + r * 0.4 : 0.9 + r * 0.2;
   }

   // ---------------------------------------------------------------------------------------------- registration

   /** Right-clicking Stephen Steel. */
   public static void talkToSteel(ServerPlayer player) {
      RaceData d = RaceData.get(player.server);
      Entrant e = d.entrant(player.getUUID());
      switch (d.phase) {
         case REGISTRATION -> {
            if (e != null) {
               player.sendSystemMessage(steel(Component.translatable("steel.steel_ball_run.registered", e.number, SbrConfig.START_DAY.get())));
            } else {
               player.sendSystemMessage(steel(Component.translatable("steel.steel_ball_run.welcome")));
               player.sendSystemMessage(steel(Component.translatable("steel.steel_ball_run.choose")));
               for (Background b : Background.ALL) {
                  player.sendSystemMessage(backgroundButton(b));
               }
            }
         }
         case COUNTDOWN, RUNNING -> player.sendSystemMessage(steel(Component.translatable(e != null ? "steel.steel_ball_run.ride" : "steel.steel_ball_run.closed")));
         case FINISHED -> {
            player.sendSystemMessage(steel(Component.translatable("steel.steel_ball_run.over")));
            showStandings(player, d);
         }
      }
   }

   private static MutableComponent steel(Component line) {
      return Component.translatable("steel.steel_ball_run.says", line).withStyle(ChatFormatting.GOLD);
   }

   private static Component backgroundButton(Background b) {
      Component hover = Component.translatable(b.descKey());
      return Component.literal("  ")
         .append(
            Component.literal("[")
               .append(Component.translatable(b.langKey()))
               .append("]")
               .withStyle(s -> s.withColor(ChatFormatting.AQUA)
                  .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sbr register " + b.id()))
                  .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)))
         )
         .append(Component.literal(" ").append(Component.translatable(b.descKey())).withStyle(ChatFormatting.GRAY));
   }

   /** Returns null on success, otherwise the reason it failed. */
   @Nullable
   public static Component register(ServerPlayer player, Background background) {
      RaceData d = RaceData.get(player.server);
      if (d.phase != RaceData.Phase.REGISTRATION) {
         return Component.translatable("message.steel_ball_run.registration_closed");
      } else if (d.entrant(player.getUUID()) != null) {
         return Component.translatable("message.steel_ball_run.already_registered");
      } else if (!player.hasPermissions(2) && !nearRegistration(player, d)) {
         return Component.translatable("message.steel_ball_run.too_far_to_register");
      } else {
         Entrant e = new Entrant(player.getUUID(), player.getScoreboardName(), d.nextNumber++, -1, background);
         e.progress = d.route().progressOf(player.getX(), player.getZ());
         d.entrants.put(e.id, e);
         d.setDirty();
         give(player, new ItemStack(SbrItems.RACE_NUMBER.get()));
         give(player, new ItemStack(SbrItems.MAP_OF_AMERICA.get()));
         give(player, new ItemStack(SbrItems.HORSE_BRUSH.get()));
         give(player, new ItemStack(Items.WHEAT, 16));
         give(player, new ItemStack(SbrItems.DOLLAR.get(), background == Background.WANDERER ? 40 : 10));
         if (background == Background.COWBOY) {
            give(player, new ItemStack(Items.LEAD, 2));
            give(player, new ItemStack(SbrItems.DOLLAR.get(), 10));
         }
         if (background != Background.NATIVE_RUNNER && SbrConfig.STARTER_HORSES.get()) {
            give(player, new ItemStack(Items.SADDLE));
            HorseFactory.spawnStarter(player.serverLevel(), player, background);
         }
         player.sendSystemMessage(steel(Component.translatable("steel.steel_ball_run.accepted", e.number, Component.translatable(background.langKey()))));
         player.sendSystemMessage(Component.translatable("message.steel_ball_run.after_register", SbrConfig.START_DAY.get()).withStyle(ChatFormatting.GRAY));
         player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
         return null;
      }
   }

   private static boolean nearRegistration(ServerPlayer player, RaceData d) {
      double r = SbrConfig.REGISTRATION_RADIUS.get();
      double dx = player.getX() - d.route().gateX(0);
      double dz = player.getZ() - d.route().gateZ(0);
      if (dx * dx + dz * dz <= (r + 24.0) * (r + 24.0)) {
         return true;
      } else {
         return !player.serverLevel().getEntitiesOfClass(StephenSteelEntity.class, player.getBoundingBox().inflate(r), x -> true).isEmpty();
      }
   }

   public static void give(ServerPlayer player, ItemStack stack) {
      if (!player.getInventory().add(stack)) {
         player.drop(stack, false);
      }
   }

   // ---------------------------------------------------------------------------------------------- start

   public static void startCountdown(MinecraftServer server, RaceData d) {
      d.phase = RaceData.Phase.COUNTDOWN;
      d.countdown = SbrConfig.COUNTDOWN_SECONDS.get();
      d.setDirty();
      broadcast(server, Component.translatable("message.steel_ball_run.countdown_start", (int)d.route().gateX(0), (int)d.route().gateZ(0))
         .withStyle(ChatFormatting.GOLD));
   }

   private static void countdownStep(MinecraftServer server, ServerLevel level, RaceData d, long now) {
      if (d.countdown > 0) {
         for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            title(p, Component.literal(String.valueOf(d.countdown)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
               Component.translatable("title.steel_ball_run.countdown"), 0, 25, 0);
            p.playNotifySound((SoundEvent)SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.MASTER, 1.0F, d.countdown <= 3 ? 1.4F : 1.0F);
         }
         d.countdown--;
         d.setDirty();
      } else {
         startRace(server, level, d, now);
      }
   }

   public static void startRace(MinecraftServer server, ServerLevel level, RaceData d, long now) {
      d.phase = RaceData.Phase.RUNNING;
      d.startTick = now;
      d.playersDoneTick = -1L;
      d.setDirty();
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
         title(p, Component.translatable("title.steel_ball_run.go").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            Component.translatable("title.steel_ball_run.go_sub"), 0, 50, 15);
         p.playNotifySound((SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.MASTER, 0.8F, 1.6F);
      }
      broadcast(server, Component.translatable("message.steel_ball_run.started").withStyle(ChatFormatting.GOLD));
   }

   // ---------------------------------------------------------------------------------------------- the race

   private static void playerTick(ServerLevel overworld, RaceData d, ServerPlayer player, long now) {
      Entrant e = d.entrant(player.getUUID());
      boolean running = d.phase == RaceData.Phase.RUNNING && e != null && e.racing();
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         boolean runner = running && e.background == Background.NATIVE_RUNNER && !player.isPassenger();
         if (runner) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(RUNNER_SPEED, SbrConfig.RUNNER_SPEED.get(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
         } else {
            speed.removeModifier(RUNNER_SPEED);
         }
      }
      if (running && player.level() == overworld && !player.isSpectator()) {
         RaceRules.check(player, d, e, now);
         Route route = d.route();
         e.progress = route.progressOf(player.getX(), player.getZ());
         int gate = e.nextGate;
         double dx = player.getX() - route.gateX(gate);
         double dz = player.getZ() - route.gateZ(gate);
         double r = SbrConfig.GATE_RADIUS.get();
         if (dx * dx + dz * dz <= r * r) {
            arrive(overworld.getServer(), d, e, gate, now);
         } else if (e.progress > route.distanceTo(gate) + r * 3.0 + 40.0) {
            Long last = MISSED_WARNING.get(e.id);
            if (last == null || now - last > 200L) {
               MISSED_WARNING.put(e.id, now);
               player.displayClientMessage(Component.translatable("message.steel_ball_run.missed_gate", gate).withStyle(ChatFormatting.RED), true);
            }
         }
      }
   }

   /** A racer rides through {@code gate}. Hands out the place and points and tells everyone who needs to know. */
   public static void arrive(MinecraftServer server, RaceData d, Entrant e, int gate, long now) {
      d.arrivals[gate]++;
      int place = d.arrivals[gate];
      e.places[gate] = place;
      e.nextGate = gate + 1;
      int pts = gate == 0 ? 0 : SbrConfig.points().forPlace(place);
      e.points += pts;
      if (e.isRival()) {
         e.pace = rollPace(server.overworld(), Rivals.get(e.rival));
      }
      boolean last = gate == Stage.GATES - 1;
      if (last) {
         e.finishTick = now;
      }
      d.setDirty();

      ServerPlayer player = e.isRival() ? null : server.getPlayerList().getPlayer(e.id);
      if (gate == 0) {
         if (player != null) {
            player.displayClientMessage(Component.translatable("message.steel_ball_run.crossed_start").withStyle(ChatFormatting.GOLD), true);
         }
         return;
      }
      Stage stage = Stage.ALL[gate - 1];
      if (player != null) {
         Component head = last
            ? Component.translatable("title.steel_ball_run.finished")
            : Component.translatable("title.steel_ball_run.stage_done", gate, Component.translatable(stage.langKey()));
         title(player, head.copy().withStyle(ChatFormatting.GOLD), Component.translatable("title.steel_ball_run.place_points", ordinal(place), pts), 5, 70, 20);
         player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.0F);
      }
      if (place == 1) {
         broadcast(server, Component.translatable(last ? "message.steel_ball_run.race_leader_finished" : "message.steel_ball_run.stage_winner",
            e.name, gate, Component.translatable(stage.langKey())).withStyle(ChatFormatting.YELLOW));
      } else if (player != null) {
         broadcast(server, Component.translatable(last ? "message.steel_ball_run.player_finished" : "message.steel_ball_run.player_stage",
            e.name, ordinal(place), gate, Component.translatable(stage.langKey())).withStyle(ChatFormatting.GRAY));
      }
   }

   /**
    * The race ends once every rider is home. Players who are done start a grace period for the rivals still on the
    * road; players who have logged off mid-race don't hold the race open once all the rivals are home.
    */
   private static void checkFinish(MinecraftServer server, RaceData d, long now) {
      boolean anyPlayer = false;
      boolean playersDone = true;
      boolean onlineRacing = false;
      boolean rivalsDone = true;
      for (Entrant e : d.entrants.values()) {
         if (e.isRival()) {
            rivalsDone &= !e.racing();
         } else {
            anyPlayer = true;
            playersDone &= !e.racing();
            onlineRacing |= e.racing() && server.getPlayerList().getPlayer(e.id) != null;
         }
      }
      if (playersDone && d.playersDoneTick < 0L) {
         d.playersDoneTick = now;
         d.setDirty();
         if (anyPlayer && !rivalsDone) {
            broadcast(server, Component.translatable("message.steel_ball_run.closing", SbrConfig.FINISH_GRACE_SECONDS.get()).withStyle(ChatFormatting.GOLD));
         }
      }
      boolean graceOver = d.playersDoneTick >= 0L && now - d.playersDoneTick >= SbrConfig.FINISH_GRACE_SECONDS.get() * 20L;
      if (playersDone && (rivalsDone || anyPlayer && graceOver) || !playersDone && rivalsDone && !onlineRacing) {
         finishRace(server, d, now);
      }
   }

   /** A player gives up. */
   @Nullable
   public static Component withdraw(ServerPlayer player) {
      RaceData d = RaceData.get(player.server);
      Entrant e = d.entrant(player.getUUID());
      if (e == null || !e.racing()) {
         return Component.translatable("message.steel_ball_run.not_racing");
      }
      if (d.phase == RaceData.Phase.REGISTRATION) {
         d.entrants.remove(e.id);
      } else {
         e.retired = true;
         broadcast(player.server, Component.translatable("message.steel_ball_run.rival_retired", e.name).withStyle(ChatFormatting.GRAY));
      }
      d.setDirty();
      player.sendSystemMessage(Component.translatable("message.steel_ball_run.withdrawn"));
      return null;
   }

   public static void finishRace(MinecraftServer server, RaceData d, long now) {
      d.phase = RaceData.Phase.FINISHED;
      d.endTick = now;
      d.setDirty();
      List<Entrant> overall = Standings.sorted(d.entrants.values(), Standings.OVERALL);
      broadcast(server, Component.translatable("message.steel_ball_run.race_over").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
      for (int i = 0; i < Math.min(3, overall.size()); i++) {
         Entrant e = overall.get(i);
         broadcast(server, Component.translatable("message.steel_ball_run.podium", ordinal(i + 1), e.name, e.points).withStyle(ChatFormatting.YELLOW));
      }
      for (int i = 0; i < overall.size(); i++) {
         Entrant e = overall.get(i);
         ServerPlayer p = e.isRival() ? null : server.getPlayerList().getPlayer(e.id);
         if (p != null) {
            int place = i + 1;
            p.sendSystemMessage(Component.translatable("message.steel_ball_run.your_result", ordinal(place), e.points).withStyle(ChatFormatting.GOLD));
            if (!e.retired) {
               if (place == 1) {
                  give(p, new ItemStack(SbrItems.PRIZE_CHEQUE.get()));
                  give(p, new ItemStack(SbrItems.TROPHY.get()));
                  title(p, Component.translatable("title.steel_ball_run.winner").withStyle(ChatFormatting.GOLD), Component.translatable("title.steel_ball_run.winner_sub"), 10, 100, 30);
               }
               give(p, new ItemStack(SbrItems.DOLLAR.get(), Math.max(4, 64 - (place - 1) * 6)));
            }
         }
      }
   }

   // ---------------------------------------------------------------------------------------------- info

   public static void showStandings(ServerPlayer player, RaceData d) {
      List<Entrant> overall = Standings.sorted(d.entrants.values(), Standings.OVERALL);
      player.sendSystemMessage(Component.translatable("message.steel_ball_run.standings_header").withStyle(ChatFormatting.GOLD));
      Entrant me = d.entrant(player.getUUID());
      for (int i = 0; i < overall.size(); i++) {
         Entrant e = overall.get(i);
         if (i < 10 || e == me) {
            player.sendSystemMessage(standingLine(i + 1, e, e == me));
         }
      }
   }

   private static Component standingLine(int place, Entrant e, boolean me) {
      Component where;
      if (e.retired) {
         where = Component.translatable("standings.steel_ball_run.retired");
      } else if (e.finished()) {
         where = Component.translatable("standings.steel_ball_run.finished");
      } else if (e.nextGate == 0) {
         where = Component.translatable("standings.steel_ball_run.at_start");
      } else {
         where = Component.translatable(Stage.forNextGate(e.nextGate).langKey());
      }
      return Component.translatable("standings.steel_ball_run.line", place, e.number, e.name, e.points, where)
         .withStyle(me ? ChatFormatting.AQUA : e.isRival() && Rivals.get(e.rival).canon() ? ChatFormatting.WHITE : ChatFormatting.GRAY);
   }

   public static void showRoute(ServerPlayer player) {
      RaceData d = RaceData.get(player.server);
      Route route = d.route();
      Entrant e = d.entrant(player.getUUID());
      player.sendSystemMessage(Component.translatable("message.steel_ball_run.route_header", (int)(route.length() * 0.5)).withStyle(ChatFormatting.GOLD));
      for (int g = 0; g < route.gates(); g++) {
         int dist = (int)Math.sqrt(sq(player.getX() - route.gateX(g)) + sq(player.getZ() - route.gateZ(g)));
         Component name = g == 0 ? Component.translatable("gate.steel_ball_run.start") : Component.translatable(Stage.ALL[g - 1].langKey());
         ChatFormatting colour = e == null ? ChatFormatting.WHITE : g < e.nextGate ? ChatFormatting.DARK_GREEN : g == e.nextGate ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
         player.sendSystemMessage(Component.translatable("message.steel_ball_run.route_line", g, name, (int)route.gateX(g), (int)route.gateZ(g), dist)
            .withStyle(colour));
      }
   }

   private static double sq(double v) {
      return v * v;
   }

   // ---------------------------------------------------------------------------------------------- HUD sync

   private static void syncAll(MinecraftServer server, ServerLevel level, RaceData d, long now) {
      List<Entrant> road = Standings.sorted(d.entrants.values(), Standings.ON_ROAD);
      List<Entrant> overall = Standings.sorted(d.entrants.values(), Standings.OVERALL);
      int field = 0;
      for (Entrant e : road) {
         if (!e.retired) {
            field++;
         }
      }
      int seconds = switch (d.phase) {
         case RUNNING -> (int)((now - d.startTick) / 20L);
         case FINISHED -> (int)((d.endTick - d.startTick) / 20L);
         default -> d.countdown;
      };
      Route route = d.route();
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
         Entrant e = d.entrant(p.getUUID());
         RaceStatusPayload payload;
         if (e == null) {
            payload = new RaceStatusPayload(d.phase.ordinal(), false, 0, (int)route.gateX(0), (int)route.gateZ(0), 0, field, 0, 0, seconds,
               SbrConfig.START_DAY.get(), 0);
         } else {
            int gate = Math.min(e.nextGate, Stage.GATES - 1);
            payload = new RaceStatusPayload(d.phase.ordinal(), true, e.nextGate, (int)route.gateX(gate), (int)route.gateZ(gate), road.indexOf(e) + 1, field,
               overall.indexOf(e) + 1, e.points, seconds, SbrConfig.START_DAY.get(), e.finished() ? e.places[Stage.GATES - 1] : 0);
         }
         PacketDistributor.sendToPlayer(p, payload);
      }
   }

   // ---------------------------------------------------------------------------------------------- helpers

   public static void broadcast(MinecraftServer server, Component message) {
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
         p.sendSystemMessage(message);
      }
   }

   public static void title(ServerPlayer p, Component title, Component subtitle, int in, int stay, int out) {
      p.connection.send(new ClientboundSetTitlesAnimationPacket(in, stay, out));
      p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
      p.connection.send(new ClientboundSetTitleTextPacket(title));
   }

   public static Component ordinal(int n) {
      int mod100 = n % 100;
      int mod10 = n % 10;
      String suffix = mod100 >= 11 && mod100 <= 13 ? "th" : mod10 == 1 ? "st" : mod10 == 2 ? "nd" : mod10 == 3 ? "rd" : "th";
      return Component.literal(n + suffix);
   }

   /** Puts Stephen Steel at the Race Office if he isn't there already. */
   public static void ensureSteel(ServerLevel level, RaceData d, double x, double y, double z) {
      if (d.steel != null && level.getEntity(d.steel) != null) {
         return;
      }
      StephenSteelEntity steel = SbrEntities.STEPHEN_STEEL.get().create(level);
      if (steel != null) {
         steel.moveTo(x, y, z, -90.0F, 0.0F);
         steel.finalizeSpawn(level, level.getCurrentDifficultyAt(steel.blockPosition()), MobSpawnType.EVENT, null);
         level.addFreshEntity(steel);
         d.steel = steel.getUUID();
         d.setDirty();
      }
   }

   /** Surface height at a column, for placing things on the course. */
   public static int surface(ServerLevel level, double x, double z) {
      return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, 0.0, z)).getY();
   }
}
