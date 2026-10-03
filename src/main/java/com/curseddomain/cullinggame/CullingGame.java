package com.curseddomain.cullinggame;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.CullingGameData.NpcState;
import com.curseddomain.cullinggame.CullingGameData.PlayerRecord;
import com.curseddomain.cullinggame.npc.NpcProfile;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.entity.cursedspirit.CursedSpirit;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModParticles;
import com.curseddomain.shibuya.CurseVariant;
import com.curseddomain.shibuya.RampantCurse;
import com.curseddomain.shibuya.ShibuyaIncident;
import com.curseddomain.sorcerer.AwakeningCause;
import com.curseddomain.sorcerer.AwakeningManager;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.combat.JjkDamage;
import com.curseddomain.util.Scheduler;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.Vfx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import org.jetbrains.annotations.Nullable;

/**
 * The Culling Game itself: Kenjaku's marks, the colony barriers, rules 1 to 15, points and the endgame.
 * Everything runs on the server; players see it through chat, titles, particles and Kogane's status bar.
 */
@EventBusSubscriber(modid = ModMain.MODID)
public final class CullingGame {
   private static final Map<UUID, Integer> LAST_COLONY = new HashMap<>();
   private static final String KENJAKU = "entity.cursed_domain.sorcerer_npc.kenjaku";

   private CullingGame() {
   }

   public static CullingGameData data(MinecraftServer server) {
      return CullingGameData.get(server);
   }

   public static long now(MinecraftServer server) {
      return server.overworld().getGameTime();
   }

   public static long dayTicks() {
      return CullingConfig.i(CullingConfig.DAY_TICKS);
   }

   public static int day(MinecraftServer server, CullingGameData data) {
      return (int)Math.max(0L, (now(server) - data.startTick) / dayTicks());
   }

   // ------------------------------------------------------------------------------------------------ lifecycle

   /** Kenjaku's announcement. Returns false if a game is already running. */
   public static boolean start(MinecraftServer server, @Nullable BlockPos anchor) {
      CullingGameData data = data(server);
      if (data.active()) {
         return false;
      } else {
         data.reset();
         data.phase = CullingGameData.Phase.ACTIVE;
         data.startTick = now(server);
         data.anchor = anchor != null ? anchor : ShibuyaIncident.anchor(server);
         data.angle = RandomSource.create(server.overworld().getSeed() ^ 7140213L).nextDouble() * Math.PI * 2.0;
         data.relayout();
         data.setDirty();
         LAST_COLONY.clear();
         List<ServerPlayer> players = server.getPlayerList().getPlayers();

         for (ServerPlayer player : players) {
            for (int i = 1; i <= 4; i++) {
               Kogane.speech(player, KENJAKU, Component.translatable("cullinggame.cursed_domain.start." + i), ChatFormatting.DARK_RED);
            }

            Kogane.title(
               player,
               Component.translatable("cullinggame.cursed_domain.start.title").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}),
               Component.translatable("cullinggame.cursed_domain.start.subtitle"),
               80
            );
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 0.6F);
            considerMark(player, data);
         }

         List<ServerPlayer> inWorld = server.overworld().players();

         for (ServerPlayer player : inWorld) {
            Colony colony = data.colonyAt(player.getX(), player.getZ());
            if (colony != null && !player.isCreative() && !player.isSpectator()) {
               // rule 3: anyone already inside a barrier gets one chance to leave safely
               pushOut(player, colony);
               Kogane.say(player, "civilian_exit", colony.displayName());
            }
         }

         ModMain.LOGGER.info("[culling game] started, anchor {}", data.anchor);
         return true;
      }
   }

   public static void stop(MinecraftServer server, String outcome) {
      CullingGameData data = data(server);
      data.phase = CullingGameData.Phase.ENDED;
      data.outcome = outcome;
      data.setDirty();
      Kogane.clear();
      LAST_COLONY.clear();
      ModMain.LOGGER.info("[culling game] ended: {}", outcome);
   }

   /** Called for every player when the game starts and when they log in while it runs. */
   static void considerMark(ServerPlayer player, CullingGameData data) {
      if (data.active() && data.recordOf(player.getUUID()) == null) {
         SorcererData sorcerer = SorcererManager.get(player);
         long now = now(player.server);
         if (sorcerer.hasTrait(InnateTrait.VESSEL)) {
            PlayerRecord record = data.record(player.getUUID(), player.getScoreboardName());
            record.participant = true;
            record.lastScoreChange = now;
            data.setDirty();
            Kogane.say(player, "vessel");
            Kogane.chime(player);
         } else if (sorcerer.status() == SorcererStatus.NON_SORCERER
            && CullingConfig.b(CullingConfig.MARK_ON_LOGIN)
            && !data.has(CullingRule.NO_NEW_PLAYERS)) {
            PlayerRecord record = data.record(player.getUUID(), player.getScoreboardName());
            record.marked = true;
            record.markedAt = now;
            data.setDirty();
            Kogane.speech(player, KENJAKU, Component.translatable("cullinggame.cursed_domain.mark"), ChatFormatting.DARK_RED);
            AwakeningManager.awaken(player, AwakeningCause.SEAL_BREAK);
            Vfx.screen(player, ScreenFxPayload.Kind.TINT, -9819606, 0.6F, 40);
            Kogane.say(player, "marked", CullingConfig.i(CullingConfig.DECLARE_DAYS));
            Kogane.chime(player);
         }
      }
   }

   // ------------------------------------------------------------------------------------------------ rules & points

   public static boolean addRule(MinecraftServer server, CullingRule rule, String author) {
      CullingGameData data = data(server);
      if (data.has(rule)) {
         return false;
      } else {
         data.rules.add(new CullingGameData.AddedRule(rule, author, now(server)));
         data.setDirty();
         int number = data.number(rule);
         Kogane.announce(server, Component.translatable("cullinggame.cursed_domain.kogane.rule_added", number, author, rule.text()));
         Kogane.titleAll(
            server,
            Component.translatable("cullinggame.cursed_domain.rule_title", number).withStyle(ChatFormatting.GOLD),
            rule.text(),
            70
         );
         ModMain.LOGGER.info("[culling game] rule {} ({}) added by {}", number, rule.id(), author);
         return true;
      }
   }

   public static void givePoints(ServerPlayer player, int amount, Component victim) {
      CullingGameData data = data(player.server);
      PlayerRecord record = data.recordOf(player.getUUID());
      if (data.active() && record != null && record.participant && amount > 0) {
         record.points += amount;
         record.lastScoreChange = now(player.server);
         record.warned = Integer.MAX_VALUE;
         data.setDirty();
         Kogane.say(player, "points", player.getDisplayName(), amount, victim, record.points);
         Kogane.chime(player);
      }
   }

   /** Points handed over by an NPC who yielded (rule 10). */
   public static void receiveTransfer(ServerPlayer player, int amount, Component from) {
      CullingGameData data = data(player.server);
      PlayerRecord record = data.recordOf(player.getUUID());
      if (record != null && record.participant && amount > 0) {
         record.points += amount;
         record.lastScoreChange = now(player.server);
         record.warned = Integer.MAX_VALUE;
         data.setDirty();
         Kogane.say(player, "transfer_received", amount, from, record.points);
         Kogane.chime(player);
      }
   }

   /** Rule 6: spend points to add a rule. Returns a Kogane line key describing the result. */
   public static String tryAddRule(ServerPlayer player, CullingRule rule) {
      CullingGameData data = data(player.server);
      PlayerRecord record = data.recordOf(player.getUUID());
      int cost = CullingGameData.ruleCost();
      if (!data.active()) {
         return "not_running";
      } else if (record == null || !record.participant) {
         return "not_player";
      } else if (!rule.purchasable()) {
         return "rule_refused";
      } else if (data.has(rule)) {
         return "rule_exists";
      } else if (record.points < cost) {
         return "not_enough";
      } else {
         record.points -= cost;
         record.rulesAdded++;
         record.lastScoreChange = now(player.server);
         record.warned = Integer.MAX_VALUE;
         data.setDirty();
         Kogane.say(player, "negotiated");
         addRule(player.server, rule, player.getScoreboardName());
         return "ok";
      }
   }

   /** Rule 10. */
   public static String transfer(ServerPlayer from, ServerPlayer to, int amount) {
      CullingGameData data = data(from.server);
      PlayerRecord a = data.recordOf(from.getUUID());
      PlayerRecord b = data.recordOf(to.getUUID());
      if (!data.active()) {
         return "not_running";
      } else if (!data.has(CullingRule.POINT_TRANSFER)) {
         return "no_transfer_rule";
      } else if (a == null || !a.participant) {
         return "not_player";
      } else if (b == null || !b.participant || from == to) {
         return "bad_target";
      } else if (amount <= 0 || a.points < amount) {
         return "not_enough";
      } else {
         long now = now(from.server);
         a.points -= amount;
         b.points += amount;
         a.lastScoreChange = now;
         b.lastScoreChange = now;
         a.warned = Integer.MAX_VALUE;
         b.warned = Integer.MAX_VALUE;
         data.setDirty();
         Kogane.say(from, "transfer_sent", amount, to.getDisplayName(), a.points);
         Kogane.say(to, "transfer_received", amount, from.getDisplayName(), b.points);
         Kogane.chime(to);
         return "ok";
      }
   }

   /** Rule 11: pay to leave, and a substitute is invited into the colony. */
   public static String leave(ServerPlayer player) {
      CullingGameData data = data(player.server);
      PlayerRecord record = data.recordOf(player.getUUID());
      int cost = CullingGameData.ruleCost();
      if (!data.active()) {
         return "not_running";
      } else if (!data.has(CullingRule.LEAVE_BY_SUBSTITUTE)) {
         return "no_leave_rule";
      } else if (record == null || !record.participant) {
         return "not_player";
      } else if (record.points < cost) {
         return "not_enough";
      } else {
         Colony colony = data.colony(record.colony);
         record.points -= cost;
         record.participant = false;
         record.colony = -1;
         data.setDirty();
         LAST_COLONY.remove(player.getUUID());
         Kogane.removeBar(player);
         Kogane.say(player, "left");
         if (colony != null) {
            if (colony.contains(player.getX(), player.getZ())) {
               pushOut(player, colony);
            }

            ServerLevel level = player.server.overworld();
            Vec3 spot = ninePoints(level, colony, level.random.nextInt(9));
            SorcererNpcEntity.spawn(level, NpcProfile.AWAKENED_PLAYER, spot, 0, false);
         }

         return "ok";
      }
   }

   /** Rules 2 and 8. Players without an innate technique are not at risk. */
   static void techniqueRemoval(ServerPlayer player, String rule) {
      SorcererData sorcerer = SorcererManager.get(player);
      if (sorcerer.technique().isEmpty()) {
         Kogane.say(player, "removal_spared");
      } else {
         Kogane.say(player, "removal." + rule);
         Kogane.title(
            player,
            Component.translatable("cullinggame.cursed_domain.removal.title").withStyle(ChatFormatting.DARK_RED),
            Component.translatable("cullinggame.cursed_domain.removal.subtitle"),
            60
         );
         Vfx.screen(player, ScreenFxPayload.Kind.WHITEOUT, -1, 1.0F, 30);
         if (CullingConfig.b(CullingConfig.REMOVAL_STRIPS_TECHNIQUE)) {
            SorcererManager.setTechnique(player, null);
         }

         if (!player.isCreative() && !player.isSpectator()) {
            JjkDamage.hurt(player, JjkDamage.SOUL, null, 100000.0F);
         }
      }
   }

   // ------------------------------------------------------------------------------------------------ npc hooks

   public static void onNpcGone(MinecraftServer server, SorcererNpcEntity npc, boolean yielded) {
      CullingGameData data = data(server);
      NpcProfile profile = npc.profile();
      if (profile.unique() && !npc.shibuyaMode()) {
         NpcState state = data.npc(profile.id());
         state.defeated = true;
         data.setDirty();
      }

      if (data.active() && !npc.shibuyaMode()) {
         if (profile == NpcProfile.KENJAKU && data.endgameStage == 1) {
            kenjakuFailsafe(server, data, npc);
         } else if (profile == NpcProfile.SUKUNA && data.endgameStage == 2) {
            finish(server, data, true);
         }
      }
   }

   /** The Angel. Holding the Prison Realm lets her Jacob's Ladder open it and free Satoru Gojo. */
   public static void angelInteract(ServerPlayer player, SorcererNpcEntity angel) {
      CullingGameData data = data(player.server);
      ItemStack held = player.getMainHandItem();
      String speaker = "entity.cursed_domain.sorcerer_npc.hana_kurusu";
      if (held.is((Item)CullingRegistries.PRISON_REALM.get())) {
         if (data.gojoFreed) {
            Kogane.speech(player, speaker, Component.translatable("npc.cursed_domain.hana_kurusu.already"), ChatFormatting.AQUA);
         } else {
            held.shrink(1);
            data.gojoFreed = true;
            data.setDirty();
            ServerLevel level = player.serverLevel();
            Kogane.speech(player, speaker, Component.translatable("npc.cursed_domain.hana_kurusu.unseal"), ChatFormatting.AQUA);
            Vfx.pillar(level, angel.position(), -1, 12.0F, 40);
            Vfx.burst(level, angel.position().add(0.0, 1.5, 0.0), -4663041, 6.0F, 30);
            level.playSound(null, angel.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 1.2F);
            Kogane.titleAll(
               player.server,
               Component.translatable("cullinggame.cursed_domain.gojo.title").withStyle(new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}),
               Component.translatable("cullinggame.cursed_domain.gojo.subtitle"),
               80
            );
            SorcererManager.addGradeXp(player, 300);
         }
      } else if (SorcererManager.get(player).hasTrait(InnateTrait.VESSEL)) {
         Kogane.speech(player, speaker, Component.translatable("npc.cursed_domain.hana_kurusu.vessel"), ChatFormatting.AQUA);
      } else {
         Kogane.speech(player, speaker, Component.translatable("npc.cursed_domain.hana_kurusu.greet"), ChatFormatting.AQUA);
      }
   }

   // ------------------------------------------------------------------------------------------------ endgame

   static void beginEndgame(MinecraftServer server, CullingGameData data) {
      data.endgameStage = 1;
      data.setDirty();
      addRule(server, CullingRule.END_CONDITION, Component.translatable(KENJAKU).getString());
      Colony gosho = data.colony(Colonies.LAKE_GOSHO);
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         Kogane.speech(player, KENJAKU, Component.translatable("cullinggame.cursed_domain.endgame"), ChatFormatting.DARK_RED);
         if (gosho != null) {
            Kogane.say(player, "endgame_location", gosho.displayName(), gosho.x(), gosho.z());
         }
      }
   }

   private static void kenjakuFailsafe(MinecraftServer server, CullingGameData data, SorcererNpcEntity kenjaku) {
      data.endgameStage = 2;
      data.setDirty();
      addRule(server, CullingRule.MERGER_AUTHORITY, Component.translatable("cullinggame.cursed_domain.failsafe").getString());
      ServerLevel level = (ServerLevel)kenjaku.level();
      Vec3 at = kenjaku.position();
      Vfx.darkBurst(level, at.add(0.0, 1.0, 0.0), -9819606, 10.0F, 40);
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         Kogane.speech(player, KENJAKU, Component.translatable("cullinggame.cursed_domain.failsafe.line"), ChatFormatting.DARK_RED);
      }

      Scheduler.later(level, 100, () -> {
         NpcState state = data.npc(NpcProfile.SUKUNA.id());
         if (!state.spawned && data.endgameStage == 2) {
            state.spawned = true;
            data.setDirty();
            spawnSukuna(level, at);
         }
      });
   }

   private static void spawnSukuna(ServerLevel level, Vec3 at) {
      SorcererNpcEntity sukuna = SorcererNpcEntity.spawn(level, NpcProfile.SUKUNA, at, 0, false);
      if (sukuna != null) {
         Vfx.pillar(level, at, -50646, 10.0F, 40);
         Vfx.shake(level, at, 4.0F, 64.0, 30);
         level.playSound(null, sukuna.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0F, 0.6F);
         List<ServerPlayer> players = level.players();

         for (ServerPlayer player : players) {
            if (player.distanceToSqr(at) < 16384.0) {
               Kogane.title(
                  player,
                  Component.translatable("cullinggame.cursed_domain.sukuna.title").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}),
                  Component.translatable("cullinggame.cursed_domain.sukuna.subtitle"),
                  70
               );
            }
         }
      }
   }

   static void finish(MinecraftServer server, CullingGameData data, boolean averted) {
      data.endgameStage = 3;
      List<ServerPlayer> players = server.getPlayerList().getPlayers();
      String key = averted ? "averted" : "merger";

      for (ServerPlayer player : players) {
         Kogane.title(
            player,
            Component.translatable("cullinggame.cursed_domain.end." + key + ".title").withStyle(averted ? ChatFormatting.AQUA : ChatFormatting.DARK_RED),
            Component.translatable("cullinggame.cursed_domain.end." + key + ".subtitle"),
            100
         );
         player.sendSystemMessage(Component.translatable("cullinggame.cursed_domain.end." + key).withStyle(ChatFormatting.GRAY));
         if (averted) {
            PlayerRecord record = data.recordOf(player.getUUID());
            if (record != null && record.participant) {
               SorcererManager.addGradeXp(player, 500);
            }
         } else {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0, false, false));
            Vfx.screen(player, ScreenFxPayload.Kind.TINT, -9819606, 0.8F, 120);
         }
      }

      stop(server, key);
   }

   // ------------------------------------------------------------------------------------------------ tick

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = event.getServer();
      int t = server.getTickCount();
      long now = now(server);
      if (t % 20 == 0) {
         EntityBossBar.sweep(now);
      }

      ShibuyaIncident.tick(server, t);
      CullingGameData data = data(server);
      if (data.active()) {
         if (t % 5 == 0) {
            enforceBarriers(server, data);
         }

         if (t % 10 == 0) {
            drawBarriers(server.overworld(), data);
         }

         if (t % 20 == 0) {
            everySecond(server, data, now, t);
         }
      }
   }

   private static void everySecond(MinecraftServer server, CullingGameData data, long now, int t) {
      int day = day(server, data);
      int kashimo = CullingConfig.i(CullingConfig.KASHIMO_RULE_DAY);
      int noNew = CullingConfig.i(CullingConfig.NO_NEW_PLAYERS_DAY);
      int endgame = CullingConfig.i(CullingConfig.ENDGAME_DAY);
      int merger = CullingConfig.i(CullingConfig.MERGER_DAY);
      if (kashimo > 0 && day >= kashimo && !data.has(CullingRule.PLAYER_INFO)) {
         addRule(server, CullingRule.PLAYER_INFO, NpcProfile.HAJIME_KASHIMO.displayName().getString());
      }

      if (noNew > 0 && day >= noNew && !data.has(CullingRule.NO_NEW_PLAYERS)) {
         addRule(server, CullingRule.NO_NEW_PLAYERS, Component.translatable(KENJAKU).getString());
      }

      if (endgame > 0 && day >= endgame && data.endgameStage == 0) {
         beginEndgame(server, data);
      }

      if (merger > 0 && day >= merger && data.endgameStage < 3) {
         finish(server, data, false);
         return;
      }

      long dayTicks = dayTicks();
      long declare = CullingConfig.i(CullingConfig.DECLARE_DAYS) * dayTicks;
      long stagnation = CullingConfig.i(CullingConfig.STAGNATION_DAYS) * dayTicks;
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         PlayerRecord record = data.recordOf(player.getUUID());
         if (record == null) {
            Kogane.removeBar(player);
         } else if (record.participant) {
            long left = stagnation - (now - record.lastScoreChange);
            int daysLeft = (int)Math.ceil((double)left / dayTicks);
            warn(player, record, daysLeft, "warn_stagnation");
            if (left <= 0L) {
               record.lastScoreChange = now;
               record.warned = Integer.MAX_VALUE;
               data.setDirty();
               techniqueRemoval(player, "rule8");
            }

            Colony colony = data.colony(record.colony);
            Component where = colony == null ? Component.translatable("cullinggame.cursed_domain.bar.outside") : colony.displayName();
            Kogane.updateBar(
               player, Component.translatable("cullinggame.cursed_domain.bar.player", record.points, where, Math.max(0, daysLeft)), (float)left / stagnation, daysLeft <= 3
            );
         } else if (record.marked) {
            long left = declare - (now - record.markedAt);
            int daysLeft = (int)Math.ceil((double)left / dayTicks);
            warn(player, record, daysLeft, "warn_declare");
            if (left <= 0L) {
               record.marked = false;
               data.setDirty();
               techniqueRemoval(player, "rule2");
               Kogane.removeBar(player);
            } else {
               Kogane.updateBar(player, Component.translatable("cullinggame.cursed_domain.bar.marked", daysLeft), (float)left / declare, true);
            }
         } else {
            Kogane.removeBar(player);
         }
      }

      if (t % 200 == 0) {
         ServerLevel overworld = server.overworld();
         List<ServerPlayer> inWorld = overworld.players();

         for (ServerPlayer player : inWorld) {
            if (!player.isSpectator()) {
               Colony colony = data.colonyAt(player.getX(), player.getZ());
               PlayerRecord record = data.recordOf(player.getUUID());
               if (colony != null && record != null && record.participant) {
                  populate(overworld, data, colony, player);
               } else if (colony == null && t % 400 == 0) {
                  surge(overworld, player);
               }
            }
         }
      }
   }

   private static void warn(ServerPlayer player, PlayerRecord record, int daysLeft, String key) {
      if ((daysLeft == 5 || daysLeft == 3 || daysLeft == 1) && daysLeft < record.warned) {
         record.warned = daysLeft;
         Kogane.say(player, key, daysLeft);
         Kogane.chime(player);
      }
   }

   // ------------------------------------------------------------------------------------------------ barriers

   private static void enforceBarriers(MinecraftServer server, CullingGameData data) {
      ServerLevel overworld = server.overworld();
      List<ServerPlayer> players = overworld.players();

      for (ServerPlayer player : players) {
         if (!player.isSpectator() && player.isAlive()) {
            UUID id = player.getUUID();
            Colony colony = data.colonyAt(player.getX(), player.getZ());
            PlayerRecord record = data.recordOf(id);
            boolean participant = record != null && record.participant;
            Integer previous = LAST_COLONY.get(id);
            if (colony != null) {
               if (!participant) {
                  if (player.isCreative()) {
                     continue;
                  }

                  if (data.has(CullingRule.NO_NEW_PLAYERS)) {
                     pushOut(player, colony);
                     player.displayClientMessage(Component.translatable("cullinggame.cursed_domain.barrier.closed").withStyle(ChatFormatting.RED), true);
                     continue;
                  }

                  join(player, colony, data);
                  LAST_COLONY.put(id, colony.index());
                  continue;
               }

               if (record.colony != colony.index()) {
                  record.colony = colony.index();
                  data.setDirty();
               }

               LAST_COLONY.put(id, colony.index());
            } else if (previous != null) {
               Colony left = data.colony(previous);
               if (participant && left != null && !data.has(CullingRule.FREE_BORDERS) && !player.isCreative()) {
                  pushIn(player, left);
                  player.displayClientMessage(Component.translatable("cullinggame.cursed_domain.barrier.sealed").withStyle(ChatFormatting.RED), true);
               } else {
                  LAST_COLONY.remove(id);
               }
            }
         }
      }
   }

   /** Rule 3, and the barrier rules: entering makes you a player and sends you to one of nine fixed points. */
   private static void join(ServerPlayer player, Colony colony, CullingGameData data) {
      PlayerRecord record = data.record(player.getUUID(), player.getScoreboardName());
      record.participant = true;
      record.marked = false;
      record.colony = colony.index();
      record.lastScoreChange = now(player.server);
      record.warned = Integer.MAX_VALUE;
      data.setDirty();
      ServerLevel level = player.serverLevel();
      int point = level.random.nextInt(9);
      Vec3 to = ninePoints(level, colony, point);
      player.teleportTo(to.x, to.y, to.z);
      level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.6F);
      Kogane.title(
         player, colony.displayName().copy().withStyle(ChatFormatting.GOLD), Component.translatable("cullinggame.cursed_domain.joined.subtitle"), 60
      );
      Kogane.say(player, "joined", colony.displayName(), point + 1);
      Kogane.chime(player);
      if (SorcererManager.get(player).status() == SorcererStatus.NON_SORCERER && AwakeningManager.awaken(player, AwakeningCause.SEAL_BREAK)) {
         Kogane.say(player, "secondary_awakening");
      }
   }

   /** The nine pre-established arrival points of a colony: its centre and eight around it. */
   public static Vec3 ninePoints(ServerLevel level, Colony colony, int point) {
      double x = colony.x();
      double z = colony.z();
      if (point > 0) {
         double angle = (point - 1) * Math.PI / 4.0 + colony.index() * 0.3;
         x += Math.cos(angle) * colony.radius() * 0.55;
         z += Math.sin(angle) * colony.radius() * 0.55;
      }

      return surface(level, x, z);
   }

   public static Vec3 surface(ServerLevel level, double x, double z) {
      BlockPos top = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(new Vec3(x, 0.0, z)));
      return new Vec3(Math.floor(x) + 0.5, top.getY(), Math.floor(z) + 0.5);
   }

   private static void pushIn(ServerPlayer player, Colony colony) {
      place(player, colony.x(), colony.z(), colony.radius() - 3.0);
   }

   private static void pushOut(ServerPlayer player, Colony colony) {
      place(player, colony.x(), colony.z(), colony.radius() + 3.0);
   }

   /** Moves a player onto the circle of the given radius, keeping their bearing from the centre. */
   public static void place(ServerPlayer player, double cx, double cz, double radius) {
      double dx = player.getX() - cx;
      double dz = player.getZ() - cz;
      double len = Math.sqrt(dx * dx + dz * dz);
      if (len < 0.001) {
         dx = 1.0;
         len = 1.0;
      }

      Vec3 to = surface(player.serverLevel(), cx + dx / len * radius, cz + dz / len * radius);
      player.teleportTo(to.x, Math.max(to.y, player.getY()), to.z);
      player.fallDistance = 0.0F;
   }

   private static void drawBarriers(ServerLevel level, CullingGameData data) {
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         for (Colony colony : data.colonies()) {
            double dist = colony.distance(player.getX(), player.getZ());
            if (Math.abs(dist - colony.radius()) < 24.0) {
               curtain(level, colony.x(), colony.z(), colony.radius(), player);
            }
         }
      }
   }

   /** A wall of dark particles along the arc of a barrier nearest to the player. */
   public static void curtain(ServerLevel level, double cx, double cz, double radius, ServerPlayer player) {
      double bearing = Math.atan2(player.getZ() - cz, player.getX() - cx);
      double step = 2.2 / radius;

      for (int k = -6; k <= 6; k++) {
         double a = bearing + k * step;
         double x = cx + Math.cos(a) * radius;
         double z = cz + Math.sin(a) * radius;

         for (int h = 0; h < 4; h++) {
            double y = player.getY() - 2.0 + h * 3.0 + level.random.nextDouble();
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.15, 0.6, 0.15, 0.0);
         }

         if (level.random.nextInt(4) == 0) {
            level.sendParticles((SimpleParticleType)ModParticles.CURSED_WISP.get(), x, player.getY() + 2.0, z, 1, 0.2, 2.0, 0.2, 0.0);
         }
      }
   }

   // ------------------------------------------------------------------------------------------------ population

   private static void populate(ServerLevel level, CullingGameData data, Colony colony, ServerPlayer player) {
      RandomSource random = level.random;
      for (NpcProfile profile : NpcProfile.values()) {
         if (profile.unique() && !profile.scripted() && colony.key().equals(profile.colony())) {
            NpcState state = data.npc(profile.id());
            if (!state.spawned && !state.defeated) {
               state.spawned = true;
               data.setDirty();
               SorcererNpcEntity.spawn(level, profile, around(level, player, 18.0, 34.0), profile.points(), false);
               return;
            }
         }
      }

      if (colony.key().equals(Colonies.LAKE_GOSHO)) {
         NpcState kenjaku = data.npc(NpcProfile.KENJAKU.id());
         if (data.endgameStage == 1 && !kenjaku.spawned) {
            kenjaku.spawned = true;
            data.setDirty();
            SorcererNpcEntity.spawn(level, NpcProfile.KENJAKU, around(level, player, 16.0, 24.0), 0, false);
            return;
         }

         NpcState sukuna = data.npc(NpcProfile.SUKUNA.id());
         if (data.endgameStage == 2 && !sukuna.spawned) {
            sukuna.spawned = true;
            data.setDirty();
            spawnSukuna(level, around(level, player, 16.0, 24.0));
            return;
         }
      }

      CurseVariant special = colony.key().equals(Colonies.SENDAI) ? CurseVariant.KUROURUSHI : colony.key().equals(Colonies.SAKURAJIMA) ? CurseVariant.NAOYA : null;
      if (special != null) {
         NpcState state = data.npc("curse_" + special.id());
         if (!state.spawned) {
            state.spawned = true;
            data.setDirty();
            RampantCurse.spawn(level, special, around(level, player, 18.0, 30.0), false);
            return;
         }
      }

      int npcs = level.getEntitiesOfClass(SorcererNpcEntity.class, player.getBoundingBox().inflate(64.0)).size();
      if (npcs < CullingConfig.i(CullingConfig.GENERIC_NPCS_PER_COLONY) && random.nextFloat() < 0.35F) {
         NpcProfile generic = random.nextInt(3) == 0 ? NpcProfile.INCARNATED_SORCERER : NpcProfile.AWAKENED_PLAYER;
         SorcererNpcEntity.spawn(level, generic, around(level, player, 20.0, 36.0), random.nextInt(8) * 5, false);
      }

      int curses = level.getEntitiesOfClass(CursedSpirit.class, player.getBoundingBox().inflate(48.0)).size();
      if (curses < CullingConfig.i(CullingConfig.COLONY_CURSE_CAP) && random.nextFloat() < 0.6F) {
         int n = 1 + random.nextInt(2);

         for (int i = 0; i < n; i++) {
            float roll = random.nextFloat();
            Vec3 at = around(level, player, 14.0, 28.0);
            if (roll < 0.06F) {
               RampantCurse.spawn(level, CurseVariant.GRADE_2, at, false);
            } else if (roll < 0.3F) {
               RampantCurse.spawn(level, CurseVariant.GRADE_3, at, false);
            } else {
               spawnGrade4(level, at);
            }
         }
      }
   }

   /** The curses Kenjaku let loose keep turning up at night, everywhere. */
   private static void surge(ServerLevel level, ServerPlayer player) {
      if (CullingConfig.b(CullingConfig.CURSE_SURGE) && level.isNight() && level.random.nextDouble() < CullingConfig.d(CullingConfig.CURSE_SURGE_CHANCE) / 3.0) {
         int curses = level.getEntitiesOfClass(CursedSpirit.class, player.getBoundingBox().inflate(48.0)).size();
         if (curses < 6) {
            int n = 1 + level.random.nextInt(2);

            for (int i = 0; i < n; i++) {
               Vec3 at = around(level, player, 16.0, 28.0);
               if (level.random.nextInt(5) == 0) {
                  RampantCurse.spawn(level, CurseVariant.GRADE_3, at, false);
               } else {
                  spawnGrade4(level, at);
               }
            }
         }
      }
   }

   public static void spawnGrade4(ServerLevel level, Vec3 at) {
      Grade4Curse curse = (Grade4Curse)((EntityType)ModEntities.GRADE_4_CURSE.get()).create(level);
      if (curse != null) {
         curse.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360.0F, 0.0F);
         curse.finalizeSpawn(level, level.getCurrentDifficultyAt(curse.blockPosition()), MobSpawnType.EVENT, null);
         level.addFreshEntity(curse);
      }
   }

   public static Vec3 around(ServerLevel level, ServerPlayer player, double min, double max) {
      double angle = level.random.nextDouble() * Math.PI * 2.0;
      double dist = min + level.random.nextDouble() * (max - min);
      return surface(level, player.getX() + Math.cos(angle) * dist, player.getZ() + Math.sin(angle) * dist);
   }

   // ------------------------------------------------------------------------------------------------ session

   /** Player respawned, changed dimension or logged out: they are no longer "just inside" a colony. */
   public static void forgetPosition(UUID player) {
      LAST_COLONY.remove(player);
   }

   @SubscribeEvent
   public static void onStopped(ServerStoppedEvent event) {
      LAST_COLONY.clear();
      Kogane.clear();
      EntityBossBar.clearAll();
   }
}
