package com.curseddomain.cullinggame;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.CullingGameData.PlayerRecord;
import com.curseddomain.cullinggame.npc.NpcProfile;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.shibuya.CurseVariant;
import com.curseddomain.shibuya.RampantCurse;
import com.curseddomain.shibuya.ShibuyaData;
import com.curseddomain.shibuya.ShibuyaIncident;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator commands for the Shibuya Incident and the Culling Game, under /jjk. */
@EventBusSubscriber(modid = ModMain.MODID)
public final class CullingCommands {
   private CullingCommands() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> jjk = Commands.literal("jjk");
      jjk.requires(source -> ((CommandSourceStack)source).hasPermission(2));
      LiteralArgumentBuilder<CommandSourceStack> shibuyaEvent = Commands.literal("shibuya");
      shibuyaEvent.executes(ctx -> shibuya(ctx, BlockPos.containing(src(ctx).getPosition())));
      shibuyaEvent.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ctx -> shibuya(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"))));
      jjk.then(Commands.literal("event").then(shibuyaEvent));
      LiteralArgumentBuilder<CommandSourceStack> shibuya = Commands.literal("shibuya");
      shibuya.then(Commands.literal("status").executes(ctx -> shibuyaStatus(ctx)));
      shibuya.then(Commands.literal("stop").executes(ctx -> shibuyaStop(ctx)));
      shibuya.then(Commands.literal("skip").executes(ctx -> shibuyaSkip(ctx)));
      jjk.then(shibuya);
      LiteralArgumentBuilder<CommandSourceStack> game = Commands.literal("cullinggame");
      LiteralArgumentBuilder<CommandSourceStack> start = Commands.literal("start");
      start.executes(ctx -> start(ctx, null));
      start.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ctx -> start(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos"))));
      game.then(start);
      game.then(Commands.literal("stop").executes(ctx -> stop(ctx)));
      game.then(Commands.literal("reset").executes(ctx -> reset(ctx)));
      game.then(Commands.literal("status").executes(ctx -> status(ctx)));
      game.then(Commands.literal("endgame").executes(ctx -> endgame(ctx)));
      RequiredArgumentBuilder<CommandSourceStack, Integer> day = Commands.argument("day", IntegerArgumentType.integer(0, 100000));
      day.executes(ctx -> setDay(ctx));
      game.then(Commands.literal("day").then(day));
      RequiredArgumentBuilder<CommandSourceStack, Integer> points = Commands.argument("amount", IntegerArgumentType.integer(-100000, 100000));
      points.executes(ctx -> points(ctx));
      game.then(Commands.literal("points").then(Commands.argument("targets", EntityArgument.players()).then(points)));
      RequiredArgumentBuilder<CommandSourceStack, String> rule = Commands.argument("rule", StringArgumentType.word());
      rule.suggests((ctx, b) -> SharedSuggestionProvider.suggest(ruleIds(), b));
      rule.executes(ctx -> rule(ctx));
      game.then(Commands.literal("rule").then(rule));
      RequiredArgumentBuilder<CommandSourceStack, String> npc = Commands.argument("profile", StringArgumentType.word());
      npc.suggests((ctx, b) -> SharedSuggestionProvider.suggest(profileIds(), b));
      npc.executes(ctx -> spawnNpc(ctx));
      game.then(Commands.literal("summon").then(npc));
      RequiredArgumentBuilder<CommandSourceStack, String> curse = Commands.argument("variant", StringArgumentType.word());
      curse.suggests((ctx, b) -> SharedSuggestionProvider.suggest(variantIds(), b));
      curse.executes(ctx -> spawnCurse(ctx));
      game.then(Commands.literal("curse").then(curse));
      jjk.then(game);
      LiteralArgumentBuilder<CommandSourceStack> locate = Commands.literal("locate");
      locate.then(Commands.literal("shibuya").executes(ctx -> locateShibuya(ctx)));
      RequiredArgumentBuilder<CommandSourceStack, String> colony = Commands.argument("colony", StringArgumentType.word());
      colony.suggests((ctx, b) -> SharedSuggestionProvider.suggest(colonyIds(ctx), b));
      colony.executes(ctx -> locateColony(ctx));
      locate.then(Commands.literal("colony").then(colony));
      jjk.then(locate);
      event.getDispatcher().register(jjk);
   }

   private static CommandSourceStack src(CommandContext<CommandSourceStack> ctx) {
      return (CommandSourceStack)ctx.getSource();
   }

   private static MinecraftServer server(CommandContext<CommandSourceStack> ctx) {
      return src(ctx).getServer();
   }

   private static void ok(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
      Component msg = Component.translatable("commands.cursed_domain.culling." + key, args);
      src(ctx).sendSuccess(() -> msg, true);
   }

   private static int fail(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
      src(ctx).sendFailure(Component.translatable("commands.cursed_domain.culling." + key, args));
      return 0;
   }

   private static List<String> ruleIds() {
      List<String> ids = new ArrayList<>();

      for (CullingRule rule : CullingRule.values()) {
         if (!rule.original()) {
            ids.add(rule.id());
         }
      }

      return ids;
   }

   private static List<String> profileIds() {
      List<String> ids = new ArrayList<>();

      for (NpcProfile profile : NpcProfile.values()) {
         ids.add(profile.id());
      }

      return ids;
   }

   private static List<String> variantIds() {
      List<String> ids = new ArrayList<>();

      for (CurseVariant variant : CurseVariant.values()) {
         ids.add(variant.id());
      }

      return ids;
   }

   private static List<String> colonyIds(CommandContext<CommandSourceStack> ctx) {
      List<String> ids = new ArrayList<>();

      for (Colony colony : CullingGame.data(server(ctx)).colonies()) {
         ids.add(colony.key());
      }

      return ids;
   }

   // ------------------------------------------------------------------------------------------------ shibuya

   private static int shibuya(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
      if (!ShibuyaIncident.begin(server(ctx), pos)) {
         return fail(ctx, "shibuya.running");
      } else {
         ok(ctx, "shibuya.started", pos.getX(), pos.getZ());
         return 1;
      }
   }

   private static int shibuyaStatus(CommandContext<CommandSourceStack> ctx) {
      ShibuyaData data = ShibuyaIncident.data(server(ctx));
      BlockPos center = data.center == null ? ShibuyaIncident.defaultCenter(server(ctx)) : data.center;
      ok(ctx, "shibuya.status", data.state.getSerializedName(), data.stage.getSerializedName(), data.wave + 1, data.tracked.size(), center.getX(), center.getZ());
      return 1;
   }

   private static int shibuyaStop(CommandContext<CommandSourceStack> ctx) {
      ShibuyaIncident.cancel(server(ctx));
      ok(ctx, "shibuya.stopped");
      return 1;
   }

   private static int shibuyaSkip(CommandContext<CommandSourceStack> ctx) {
      ShibuyaIncident.skipStage(server(ctx));
      ok(ctx, "shibuya.skipped");
      return 1;
   }

   // ------------------------------------------------------------------------------------------------ culling game

   private static int start(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
      if (!CullingGame.start(server(ctx), pos)) {
         return fail(ctx, "running");
      } else {
         BlockPos anchor = CullingGame.data(server(ctx)).anchor;
         ok(ctx, "started", anchor.getX(), anchor.getZ());
         return 1;
      }
   }

   private static int stop(CommandContext<CommandSourceStack> ctx) {
      if (!CullingGame.data(server(ctx)).active()) {
         return fail(ctx, "not_running");
      } else {
         CullingGame.stop(server(ctx), "stopped");
         ok(ctx, "stopped");
         return 1;
      }
   }

   private static int reset(CommandContext<CommandSourceStack> ctx) {
      CullingGame.stop(server(ctx), "reset");
      CullingGame.data(server(ctx)).reset();
      ok(ctx, "reset");
      return 1;
   }

   private static int status(CommandContext<CommandSourceStack> ctx) {
      MinecraftServer server = server(ctx);
      CullingGameData data = CullingGame.data(server);
      ok(
         ctx,
         "status",
         data.phase.getSerializedName(),
         CullingGame.day(server, data),
         data.participants().size(),
         CullingRule.originals().size() + data.rules.size(),
         data.endgameStage,
         data.gojoFreed ? "yes" : "no"
      );
      return 1;
   }

   private static int endgame(CommandContext<CommandSourceStack> ctx) {
      CullingGameData data = CullingGame.data(server(ctx));
      if (!data.active()) {
         return fail(ctx, "not_running");
      } else if (data.endgameStage != 0) {
         return fail(ctx, "endgame.already");
      } else {
         CullingGame.beginEndgame(server(ctx), data);
         ok(ctx, "endgame");
         return 1;
      }
   }

   private static int setDay(CommandContext<CommandSourceStack> ctx) {
      MinecraftServer server = server(ctx);
      CullingGameData data = CullingGame.data(server);
      if (!data.active()) {
         return fail(ctx, "not_running");
      } else {
         int day = IntegerArgumentType.getInteger(ctx, "day");
         data.startTick = CullingGame.now(server) - (long)day * CullingGame.dayTicks();
         data.setDirty();
         ok(ctx, "day", day);
         return day;
      }
   }

   private static int points(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
      int amount = IntegerArgumentType.getInteger(ctx, "amount");
      CullingGameData data = CullingGame.data(server(ctx));
      int changed = 0;

      for (ServerPlayer player : players) {
         PlayerRecord record = data.recordOf(player.getUUID());
         if (record != null && record.participant) {
            record.points = Math.max(0, record.points + amount);
            record.lastScoreChange = CullingGame.now(server(ctx));
            data.setDirty();
            ok(ctx, "points", player.getDisplayName(), record.points);
            changed++;
         } else {
            fail(ctx, "not_player", player.getDisplayName());
         }
      }

      return changed;
   }

   private static int rule(CommandContext<CommandSourceStack> ctx) {
      CullingRule rule = CullingRule.byId(StringArgumentType.getString(ctx, "rule"));
      CullingGameData data = CullingGame.data(server(ctx));
      if (rule == null || rule.original()) {
         return fail(ctx, "rule.unknown");
      } else if (!data.active()) {
         return fail(ctx, "not_running");
      } else if (!CullingGame.addRule(server(ctx), rule, src(ctx).getTextName())) {
         return fail(ctx, "rule.exists");
      } else {
         return 1;
      }
   }

   private static int spawnNpc(CommandContext<CommandSourceStack> ctx) {
      NpcProfile profile = NpcProfile.byId(StringArgumentType.getString(ctx, "profile"));
      if (profile == null) {
         return fail(ctx, "summon.unknown");
      } else {
         Vec3 at = src(ctx).getPosition();
         SorcererNpcEntity npc = SorcererNpcEntity.spawn(src(ctx).getLevel(), profile, at, profile.points(), false);
         if (npc != null && profile.unique()) {
            CullingGameData data = CullingGame.data(server(ctx));
            data.npc(profile.id()).spawned = true;
            data.setDirty();
         }

         ok(ctx, "summoned", profile.displayName());
         return npc == null ? 0 : 1;
      }
   }

   private static int spawnCurse(CommandContext<CommandSourceStack> ctx) {
      CurseVariant variant = CurseVariant.byId(StringArgumentType.getString(ctx, "variant"));
      if (variant == null) {
         return fail(ctx, "summon.unknown");
      } else {
         RampantCurse curse = RampantCurse.spawn(src(ctx).getLevel(), variant, src(ctx).getPosition(), false);
         ok(ctx, "summoned", variant.displayName());
         return curse == null ? 0 : 1;
      }
   }

   private static int locateShibuya(CommandContext<CommandSourceStack> ctx) {
      BlockPos pos = ShibuyaIncident.anchor(server(ctx));
      ok(ctx, "locate.shibuya", pos.getX(), pos.getZ());
      return 1;
   }

   private static int locateColony(CommandContext<CommandSourceStack> ctx) {
      CullingGameData data = CullingGame.data(server(ctx));
      Colony colony = data.colony(StringArgumentType.getString(ctx, "colony"));
      if (colony == null) {
         return fail(ctx, "locate.unknown");
      } else {
         ok(ctx, "locate.colony", colony.displayName(), colony.x(), colony.z(), colony.radius());
         return 1;
      }
   }
}
