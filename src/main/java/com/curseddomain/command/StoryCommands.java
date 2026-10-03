package com.curseddomain.command;

import com.curseddomain.sorcerer.AwakeningCause;
import com.curseddomain.sorcerer.AwakeningManager;
import com.curseddomain.sorcerer.TechniqueRoller;
import com.curseddomain.world.SchoolLocation;
import com.curseddomain.world.SealBreak;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class StoryCommands {
   private StoryCommands() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("jjk")
                           .requires(source -> source.hasPermission(2)))
                        .then(Commands.literal("roll").then(Commands.argument("targets", EntityArgument.players()).executes(StoryCommands::roll))))
                     .then(Commands.literal("awaken").then(Commands.argument("targets", EntityArgument.players()).executes(StoryCommands::awaken))))
                  .then(Commands.literal("locate").then(Commands.literal("school").executes(StoryCommands::locateSchool))))
               .then(
                  Commands.literal("event")
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("seal_break")
                              .executes(ctx -> sealBreak(ctx, BlockPos.containing(((CommandSourceStack)ctx.getSource()).getPosition()).offset(6, 0, 0))))
                           .then(
                              Commands.argument("pos", BlockPosArgument.blockPos())
                                 .executes(ctx -> sealBreak(ctx, BlockPosArgument.getLoadedBlockPos(ctx, "pos")))
                           )
                     )
               )
         );
   }

   private static int roll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");

      for (ServerPlayer player : players) {
         TechniqueRoller.Result result = TechniqueRoller.rollFor(player);
         Component traits = (Component)(result.traits().isEmpty()
            ? Component.translatable("commands.cursed_domain.none")
            : ComponentUtils.formatList(result.traits().stream().map(t -> t.displayName()).toList(), Component.literal(", ")));
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.translatable("commands.cursed_domain.roll", new Object[]{player.getDisplayName(), result.technique().displayName(), traits}),
               true
            );
      }

      return players.size();
   }

   private static int awaken(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
      int count = 0;

      for (ServerPlayer player : players) {
         boolean done = AwakeningManager.awaken(player, AwakeningCause.COMMAND);
         count += done ? 1 : 0;
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.translatable(
                  done ? "commands.cursed_domain.awaken" : "commands.cursed_domain.awaken.already", new Object[]{player.getDisplayName()}
               ),
               true
            );
      }

      return count;
   }

   private static int locateSchool(CommandContext<CommandSourceStack> ctx) {
      BlockPos pos = SchoolLocation.get().orElse(null);
      if (pos == null) {
         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.translatable("commands.cursed_domain.locate.none"));
         return 0;
      } else {
         String tp = "/tp @s " + pos.getX() + " ~ " + pos.getZ();
         Component coords = ComponentUtils.wrapInSquareBrackets(Component.literal(pos.getX() + ", ~, " + pos.getZ()))
            .withStyle(
               style -> style.withColor(ChatFormatting.GREEN)
                  .withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, tp))
                  .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
            );
         int distance = (int)Math.sqrt(
            ((CommandSourceStack)ctx.getSource()).getPosition().distanceToSqr(pos.getX(), ((CommandSourceStack)ctx.getSource()).getPosition().y, pos.getZ())
         );
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(() -> Component.translatable("commands.cursed_domain.locate.school", new Object[]{coords, distance}), false);
         return distance;
      }
   }

   private static int sealBreak(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
      SealBreak.trigger(((CommandSourceStack)ctx.getSource()).getLevel(), pos);
      ((CommandSourceStack)ctx.getSource())
         .sendSuccess(() -> Component.translatable("commands.cursed_domain.event.seal_break", new Object[]{pos.toShortString()}), true);
      return 1;
   }
}
