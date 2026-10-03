package com.curseddomain.command;

import com.curseddomain.ModMain;
import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueCategory;
import com.curseddomain.technique.TechniqueRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class JjkCommand {
   private static final DynamicCommandExceptionType UNKNOWN_TECHNIQUE = new DynamicCommandExceptionType(
      id -> Component.translatable("commands.cursed_domain.error.unknown_technique", new Object[]{String.valueOf(id)})
   );
   private static final DynamicCommandExceptionType NOT_ENROLLED = new DynamicCommandExceptionType(
      name -> Component.translatable("commands.cursed_domain.error.not_enrolled", new Object[]{name})
   );

   private JjkCommand() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      register(event.getDispatcher());
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                              "jjk"
                           )
                           .requires(source -> source.hasPermission(2)))
                        .then(
                           ((LiteralArgumentBuilder)Commands.literal("info")
                                 .executes(ctx -> info((CommandSourceStack)ctx.getSource(), ((CommandSourceStack)ctx.getSource()).getPlayerOrException())))
                              .then(
                                 Commands.argument("target", EntityArgument.player())
                                    .executes(ctx -> info((CommandSourceStack)ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))
                              )
                        ))
                     .then(
                        ((LiteralArgumentBuilder)Commands.literal("techniques").executes(ctx -> listTechniques((CommandSourceStack)ctx.getSource(), null)))
                           .then(
                              Commands.argument("category", StringArgumentType.word())
                                 .suggests((ctx, b) -> SharedSuggestionProvider.suggest(EnumArgument.names(TechniqueCategory.values()), b))
                                 .executes(
                                    ctx -> listTechniques((CommandSourceStack)ctx.getSource(), EnumArgument.get(ctx, "category", TechniqueCategory.values()))
                                 )
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("set")
                                 .then(
                                    Commands.literal("status")
                                       .then(
                                          targets()
                                             .then(
                                                EnumArgument.argument("status", SorcererStatus.values())
                                                   .executes(ctx -> setStatus(ctx, EnumArgument.get(ctx, "status", SorcererStatus.values())))
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("grade")
                                    .then(
                                       targets()
                                          .then(
                                             EnumArgument.argument("grade", Grade.values())
                                                .executes(ctx -> setGrade(ctx, EnumArgument.get(ctx, "grade", Grade.values())))
                                          )
                                    )
                              ))
                           .then(
                              Commands.literal("technique")
                                 .then(
                                    ((RequiredArgumentBuilder)targets().then(Commands.literal("none").executes(ctx -> setTechnique(ctx, null))))
                                       .then(
                                          Commands.argument("technique", ResourceLocationArgument.id())
                                             .suggests((ctx, b) -> SharedSuggestionProvider.suggestResource(TechniqueRegistry.REGISTRY.keySet(), b))
                                             .executes(ctx -> setTechnique(ctx, resolveTechnique(ResourceLocationArgument.getId(ctx, "technique"))))
                                       )
                                 )
                           ))
                        .then(
                           Commands.literal("energy")
                              .then(
                                 ((RequiredArgumentBuilder)((RequiredArgumentBuilder)targets()
                                          .then(Commands.literal("full").executes(ctx -> setEnergy(ctx, (p, e) -> e.max()))))
                                       .then(Commands.literal("empty").executes(ctx -> setEnergy(ctx, (p, e) -> 0.0F))))
                                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F)).executes(ctx -> {
                                       float amount = FloatArgumentType.getFloat(ctx, "amount");
                                       return setEnergy(ctx, (p, e) -> amount);
                                    }))
                              )
                        )
                  ))
               .then(
                  Commands.literal("reveal")
                     .then(
                        ((RequiredArgumentBuilder)targets().executes(ctx -> reveal(ctx, true)))
                           .then(Commands.literal("hide").executes(ctx -> reveal(ctx, false)))
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)Commands.literal("trait")
                     .then(Commands.literal("add").then(targets().then(EnumArgument.argument("trait", InnateTrait.values()).executes(ctx -> trait(ctx, true))))))
                  .then(
                     Commands.literal("remove").then(targets().then(EnumArgument.argument("trait", InnateTrait.values()).executes(ctx -> trait(ctx, false))))
                  )
            )
      );
   }

   private static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targets() {
      return Commands.argument("targets", EntityArgument.players());
   }

   private static Collection<ServerPlayer> targets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      return EntityArgument.getPlayers(ctx, "targets");
   }

   private static ResourceLocation resolveTechnique(ResourceLocation id) throws CommandSyntaxException {
      if (TechniqueRegistry.get(id).isPresent()) {
         return id;
      } else {
         ResourceLocation modId = ModMain.id(id.getPath());
         if (id.getNamespace().equals("minecraft") && TechniqueRegistry.get(modId).isPresent()) {
            return modId;
         } else {
            throw UNKNOWN_TECHNIQUE.create(id);
         }
      }
   }

   private static int setStatus(CommandContext<CommandSourceStack> ctx, SorcererStatus status) throws CommandSyntaxException {
      Collection<ServerPlayer> players = targets(ctx);
      players.forEach(p -> SorcererManager.setStatus(p, status));
      return feedback(ctx, players, p -> Component.translatable("commands.cursed_domain.set.status", new Object[]{p.getDisplayName(), status.displayName()}));
   }

   private static int setGrade(CommandContext<CommandSourceStack> ctx, Grade grade) throws CommandSyntaxException {
      Collection<ServerPlayer> players = targets(ctx);

      for (ServerPlayer player : players) {
         if (!SorcererManager.setGrade(player, grade)) {
            throw NOT_ENROLLED.create(player.getDisplayName());
         }
      }

      return feedback(ctx, players, p -> Component.translatable("commands.cursed_domain.set.grade", new Object[]{p.getDisplayName(), grade.displayName()}));
   }

   private static int setTechnique(CommandContext<CommandSourceStack> ctx, ResourceLocation id) throws CommandSyntaxException {
      Collection<ServerPlayer> players = targets(ctx);
      players.forEach(p -> SorcererManager.setTechnique(p, id));
      Component name = (Component)(id == null
         ? Component.translatable("commands.cursed_domain.none")
         : TechniqueRegistry.get(id).map(JjkCommand::techniqueLine).orElse(Component.literal(id.toString())));
      return feedback(ctx, players, p -> Component.translatable("commands.cursed_domain.set.technique", new Object[]{p.getDisplayName(), name}));
   }

   private static int setEnergy(CommandContext<CommandSourceStack> ctx, BiFunction<ServerPlayer, CursedEnergyData, Float> value) throws CommandSyntaxException {
      Collection<ServerPlayer> players = targets(ctx);

      for (ServerPlayer player : players) {
         EnergyManager.setCurrent(player, value.apply(player, EnergyManager.get(player)));
      }

      return feedback(ctx, players, p -> {
         CursedEnergyData e = EnergyManager.get(p);
         return Component.translatable("commands.cursed_domain.set.energy", new Object[]{p.getDisplayName(), fmt(e.current()), fmt(e.max())});
      });
   }

   private static int reveal(CommandContext<CommandSourceStack> ctx, boolean revealed) throws CommandSyntaxException {
      Collection<ServerPlayer> players = targets(ctx);
      players.forEach(p -> SorcererManager.setTechniqueRevealed(p, revealed));
      String key = revealed ? "commands.cursed_domain.reveal" : "commands.cursed_domain.hide";
      return feedback(ctx, players, p -> Component.translatable(key, new Object[]{p.getDisplayName()}));
   }

   private static int trait(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
      InnateTrait trait = EnumArgument.get(ctx, "trait", InnateTrait.values());
      Collection<ServerPlayer> players = targets(ctx);
      players.forEach(p -> {
         if (add) {
            SorcererManager.addTrait(p, trait);
         } else {
            SorcererManager.removeTrait(p, trait);
         }
      });
      String key = add ? "commands.cursed_domain.trait.add" : "commands.cursed_domain.trait.remove";
      return feedback(ctx, players, p -> Component.translatable(key, new Object[]{trait.displayName(), p.getDisplayName()}));
   }

   private static int feedback(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, Function<ServerPlayer, Component> message) {
      players.forEach(p -> ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> message.apply(p), true));
      return players.size();
   }

   private static int info(CommandSourceStack source, ServerPlayer player) {
      SorcererData s = SorcererManager.get(player);
      CursedEnergyData e = EnergyManager.get(player);
      source.sendSuccess(
         () -> Component.translatable("commands.cursed_domain.info.header", new Object[]{player.getDisplayName()})
            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}),
         false
      );
      source.sendSuccess(
         () -> Component.translatable("commands.cursed_domain.info.status", new Object[]{s.status().displayName(), s.grade().displayName(), s.gradeXp()}),
         false
      );
      Component technique = s.techniqueId()
         .map(id -> s.technique().map(JjkCommand::techniqueLine).orElse(Component.literal(id + " ?").withStyle(ChatFormatting.RED)))
         .orElse(Component.translatable("commands.cursed_domain.none"));
      Component visibility = Component.translatable(s.techniqueRevealed() ? "commands.cursed_domain.info.revealed" : "commands.cursed_domain.info.hidden")
         .withStyle(ChatFormatting.GRAY);
      source.sendSuccess(() -> Component.translatable("commands.cursed_domain.info.technique", new Object[]{technique, visibility}), false);
      Component traits = (Component)(s.traits().isEmpty()
         ? Component.translatable("commands.cursed_domain.none")
         : ComponentUtils.formatList(s.traits().stream().map(InnateTrait::displayName).toList(), Component.literal(", ")));
      source.sendSuccess(() -> Component.translatable("commands.cursed_domain.info.traits", new Object[]{traits}), false);
      source.sendSuccess(
         () -> Component.translatable(
            "commands.cursed_domain.info.energy",
            new Object[]{fmt(e.current()), fmt(e.max()), fmt(e.regenPerSecond()), fmt(e.output()), Math.round(e.control() * 100.0F), e.nature().displayName()}
         ),
         false
      );
      source.sendSuccess(
         () -> Component.translatable(
            "commands.cursed_domain.info.state",
            new Object[]{
               yesNo(e.meditating()), yesNo(EnergyManager.inCombat(player)), String.format(Locale.ROOT, "%.2f", EnergyManager.effectiveCost(player, 1.0F))
            }
         ),
         false
      );
      return 1;
   }

   private static int listTechniques(CommandSourceStack source, TechniqueCategory only) {
      List<Technique> all = TechniqueRegistry.sorted();
      source.sendSuccess(
         () -> Component.translatable("commands.cursed_domain.techniques.header", new Object[]{all.size()})
            .withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}),
         false
      );

      for (TechniqueCategory category : TechniqueCategory.values()) {
         if (only == null || only == category) {
            List<Component> names = all.stream().filter(t -> t.category() == category).map(JjkCommand::techniqueLine).toList();
            MutableComponent line = Component.empty()
               .append(category.displayName().copy().withStyle(ChatFormatting.YELLOW))
               .append(Component.literal(" (" + names.size() + "): ").withStyle(ChatFormatting.GRAY))
               .append(ComponentUtils.formatList(names, Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY)));
            source.sendSuccess(() -> line, false);
         }
      }

      return all.size();
   }

   private static Component techniqueLine(Technique t) {
      MutableComponent hover = Component.empty()
         .append(t.displayName())
         .append("\n")
         .append(
            Component.translatable(
               "commands.cursed_domain.techniques.hover", new Object[]{t.rarity().displayName(), t.tier().displayName(), t.userName(), t.id().toString()}
            )
         );
      if (t.npcOnly()) {
         hover.append("\n").append(Component.translatable("commands.cursed_domain.techniques.npc_only").withStyle(ChatFormatting.RED));
      }

      if (!t.inRandomRoll()) {
         hover.append("\n").append(Component.translatable("commands.cursed_domain.techniques.not_rolled").withStyle(ChatFormatting.GOLD));
      }

      if (t.modOriginal()) {
         hover.append("\n").append(Component.translatable("commands.cursed_domain.techniques.mod_original").withStyle(ChatFormatting.AQUA));
      }

      if (!t.isImplemented()) {
         hover.append("\n").append(Component.translatable("commands.cursed_domain.techniques.placeholder").withStyle(ChatFormatting.DARK_GRAY));
      }

      return t.displayName().copy().withStyle(style -> style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover)));
   }

   private static Component yesNo(boolean value) {
      return Component.translatable(value ? "commands.cursed_domain.yes" : "commands.cursed_domain.no");
   }

   private static String fmt(float value) {
      return value == Math.rint(value) ? String.valueOf((int)value) : String.format(Locale.ROOT, "%.1f", value);
   }
}
