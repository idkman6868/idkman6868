package com.steelballrun.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.steelballrun.SteelBallRun;
import com.steelballrun.horse.HorseCare;
import com.steelballrun.horse.HorseFactory;
import com.steelballrun.npc.RivalManager;
import com.steelballrun.race.Background;
import com.steelballrun.race.GateBuilder;
import com.steelballrun.race.RaceData;
import com.steelballrun.race.RaceManager;
import com.steelballrun.race.RaceRules;
import com.steelballrun.race.Route;
import com.steelballrun.race.Stage;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** {@code /sbr}: standings, route, rules, registering, horse info, and operator controls for the race. */
@EventBusSubscriber(modid = SteelBallRun.MODID)
public final class SbrCommands {
   private SbrCommands() {
   }

   @SubscribeEvent
   public static void register(RegisterCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      dispatcher.register(
         Commands.literal("sbr")
            .then(Commands.literal("standings").executes(c -> standings(c)))
            .then(Commands.literal("route").executes(c -> route(c)))
            .then(Commands.literal("rules").executes(c -> rules(c)))
            .then(Commands.literal("register")
               .then(Commands.argument("background", StringArgumentType.word())
                  .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Background.ALL).map(Background::id), b))
                  .executes(c -> register(c, StringArgumentType.getString(c, "background")))))
            .then(Commands.literal("withdraw").executes(c -> withdraw(c)))
            .then(Commands.literal("horse")
               .executes(c -> horse(c))
               .then(Commands.literal("summon").requires(s -> s.hasPermission(2)).executes(c -> summonHorse(c))))
            .then(Commands.literal("steel").requires(s -> s.hasPermission(2)).executes(c -> steel(c)))
            .then(Commands.literal("race").requires(s -> s.hasPermission(2))
               .then(Commands.literal("status").executes(c -> status(c)))
               .then(Commands.literal("start").executes(c -> start(c, false))
                  .then(Commands.literal("now").executes(c -> start(c, true))))
               .then(Commands.literal("finish").executes(c -> finish(c)))
               .then(Commands.literal("reset").executes(c -> reset(c)))
               .then(Commands.literal("gate")
                  .then(Commands.argument("gate", IntegerArgumentType.integer(0, Stage.GATES - 1))
                     .executes(c -> gate(c, IntegerArgumentType.getInteger(c, "gate"), false))
                     .then(Commands.literal("rebuild").executes(c -> gate(c, IntegerArgumentType.getInteger(c, "gate"), true))))))
      );
   }

   private static RaceData data(CommandContext<CommandSourceStack> c) {
      return RaceData.get(c.getSource().getServer());
   }

   private static int standings(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
      RaceManager.showStandings(c.getSource().getPlayerOrException(), data(c));
      return 1;
   }

   private static int route(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
      RaceManager.showRoute(c.getSource().getPlayerOrException());
      return 1;
   }

   private static int rules(CommandContext<CommandSourceStack> c) {
      c.getSource().sendSuccess(() -> Component.translatable("rule.steel_ball_run.header").withStyle(ChatFormatting.GOLD), false);
      for (String rule : RaceRules.activeRules()) {
         c.getSource().sendSuccess(() -> Component.literal(" - ").append(Component.translatable("rule.steel_ball_run." + rule + ".desc")), false);
      }
      return 1;
   }

   private static int register(CommandContext<CommandSourceStack> c, String id) throws CommandSyntaxException {
      Background background = Background.byId(id);
      if (background == null) {
         c.getSource().sendFailure(Component.translatable("message.steel_ball_run.unknown_background", id));
         return 0;
      }
      Component error = RaceManager.register(c.getSource().getPlayerOrException(), background);
      if (error != null) {
         c.getSource().sendFailure(error);
         return 0;
      }
      return 1;
   }

   private static int withdraw(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
      Component error = RaceManager.withdraw(c.getSource().getPlayerOrException());
      if (error != null) {
         c.getSource().sendFailure(error);
         return 0;
      }
      return 1;
   }

   private static int horse(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
      ServerPlayer player = c.getSource().getPlayerOrException();
      if (player.getVehicle() instanceof AbstractHorse horse) {
         HorseCare.describe(player, horse);
         return 1;
      }
      c.getSource().sendFailure(Component.translatable("message.steel_ball_run.not_riding"));
      return 0;
   }

   private static int summonHorse(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
      ServerPlayer player = c.getSource().getPlayerOrException();
      HorseFactory.spawnStarter(player.serverLevel(), player, Background.JOCKEY);
      return 1;
   }

   private static int steel(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
      ServerPlayer player = c.getSource().getPlayerOrException();
      RaceData d = data(c);
      d.steel = null;
      RaceManager.ensureSteel(player.serverLevel(), d, player.getX(), player.getY(), player.getZ());
      return 1;
   }

   private static int status(CommandContext<CommandSourceStack> c) {
      RaceData d = data(c);
      Route r = d.route();
      long players = d.entrants.values().stream().filter(e -> !e.isRival()).count();
      c.getSource().sendSuccess(() -> Component.translatable("message.steel_ball_run.status", d.phase.name(), (int)r.gateX(0), (int)r.gateZ(0),
         (int)r.length(), d.entrants.size(), players), false);
      return 1;
   }

   private static int start(CommandContext<CommandSourceStack> c, boolean now) {
      RaceData d = data(c);
      if (d.phase != RaceData.Phase.REGISTRATION && d.phase != RaceData.Phase.COUNTDOWN) {
         c.getSource().sendFailure(Component.translatable("message.steel_ball_run.cannot_start", d.phase.name()));
         return 0;
      }
      ServerLevel level = c.getSource().getServer().overworld();
      if (now) {
         RaceManager.startRace(c.getSource().getServer(), level, d, level.getGameTime());
      } else if (d.phase == RaceData.Phase.REGISTRATION) {
         RaceManager.startCountdown(c.getSource().getServer(), d);
      }
      return 1;
   }

   private static int finish(CommandContext<CommandSourceStack> c) {
      RaceData d = data(c);
      if (d.phase != RaceData.Phase.RUNNING) {
         c.getSource().sendFailure(Component.translatable("message.steel_ball_run.not_running"));
         return 0;
      }
      RaceManager.finishRace(c.getSource().getServer(), d, c.getSource().getServer().overworld().getGameTime());
      return 1;
   }

   private static int reset(CommandContext<CommandSourceStack> c) {
      RaceData d = data(c);
      RivalManager.clearBodies(c.getSource().getServer().overworld());
      d.reset();
      c.getSource().sendSuccess(() -> Component.translatable("message.steel_ball_run.reset"), true);
      return 1;
   }

   private static int gate(CommandContext<CommandSourceStack> c, int gate, boolean rebuild) throws CommandSyntaxException {
      ServerPlayer player = c.getSource().getPlayerOrException();
      RaceData d = data(c);
      ServerLevel level = c.getSource().getServer().overworld();
      Route r = d.route();
      if (rebuild) {
         GateBuilder.rebuild(level, d, gate);
      } else {
         double x = r.gateX(gate) - 6.0;
         double z = r.gateZ(gate);
         player.teleportTo(level, x, RaceManager.surface(level, x, z), z, -90.0F, 0.0F);
      }
      return 1;
   }
}
