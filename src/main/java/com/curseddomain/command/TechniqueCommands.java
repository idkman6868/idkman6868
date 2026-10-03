package com.curseddomain.command;

import com.curseddomain.domain.DomainManager;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.ability.AbilityManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class TechniqueCommands {
   private static final List<String> KNOWN_FLAGS = List.of(
      "rct",
      "domain",
      "domain_incomplete",
      "world_slash",
      "shikigami:nue",
      "shikigami:great_serpent",
      "shikigami:toad",
      "shikigami:max_elephant",
      "shikigami:rabbit_escape",
      "shikigami:round_deer",
      "shikigami:piercing_ox",
      "shikigami:tiger_funeral",
      "universal:reinforcement",
      "universal:simple_domain",
      "universal:curtain",
      "universal:hollow_wicker_basket",
      "universal:falling_blossom_emotion",
      "universal:domain_amplification",
      "rct_output"
   );

   private TechniqueCommands() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      event.getDispatcher()
         .register(
            (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "jjk"
                              )
                              .requires(source -> source.hasPermission(2)))
                           .then(
                              Commands.literal("unlock")
                                 .then(
                                    Commands.argument("targets", EntityArgument.players())
                                       .then(
                                          Commands.argument("flag", StringArgumentType.greedyString())
                                             .suggests((c, b) -> SharedSuggestionProvider.suggest(KNOWN_FLAGS, b))
                                             .executes(ctx -> unlock(ctx, true))
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("lock")
                              .then(
                                 Commands.argument("targets", EntityArgument.players())
                                    .then(
                                       Commands.argument("flag", StringArgumentType.greedyString())
                                          .suggests((c, b) -> SharedSuggestionProvider.suggest(KNOWN_FLAGS, b))
                                          .executes(ctx -> unlock(ctx, false))
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("cooldowns").then(Commands.argument("targets", EntityArgument.players()).executes(TechniqueCommands::resetCooldowns))
                     ))
                  .then(Commands.literal("cleanse").then(Commands.argument("targets", EntityArgument.players()).executes(TechniqueCommands::cleanse))))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("domain")
                        .then(Commands.literal("cast").then(Commands.argument("targets", EntityArgument.players()).executes(ctx -> domain(ctx, true)))))
                     .then(Commands.literal("end").then(Commands.argument("targets", EntityArgument.players()).executes(ctx -> domain(ctx, false))))
               )
         );
   }

   private static int unlock(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
      String flag = StringArgumentType.getString(ctx, "flag").trim();
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");

      for (ServerPlayer player : players) {
         if (add) {
            SorcererManager.unlock(player, flag);
         } else {
            SorcererManager.lock(player, flag);
         }

         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.translatable(add ? "commands.cursed_domain.unlock" : "commands.cursed_domain.lock", new Object[]{flag, player.getDisplayName()}),
               true
            );
      }

      return players.size();
   }

   private static int resetCooldowns(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");

      for (ServerPlayer player : players) {
         AbilityManager.resetCooldowns(player);
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(() -> Component.translatable("commands.cursed_domain.cooldowns", new Object[]{player.getDisplayName()}), true);
      }

      return players.size();
   }

   private static int cleanse(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");

      for (ServerPlayer player : players) {
         for (MobEffectInstance effect : List.copyOf(player.getActiveEffects())) {
            player.removeEffect(effect.getEffect());
         }

         AbilityManager.resetCooldowns(player);
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(() -> Component.translatable("commands.cursed_domain.cleanse", new Object[]{player.getDisplayName()}), true);
      }

      return players.size();
   }

   private static int domain(CommandContext<CommandSourceStack> ctx, boolean cast) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");

      for (ServerPlayer player : players) {
         if (cast) {
            DomainManager.tryCast(player);
         } else {
            DomainManager.of(player).ifPresent(d -> DomainManager.end(d, "dismissed"));
         }
      }

      return players.size();
   }
}
