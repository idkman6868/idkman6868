package com.curseddomain.cullinggame;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.CullingGameData.AddedRule;
import com.curseddomain.cullinggame.CullingGameData.PlayerRecord;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /kogane: every player's interface to the Culling Game. Needs no permissions. */
@EventBusSubscriber(modid = ModMain.MODID)
public final class KoganeCommand {
   private KoganeCommand() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("kogane");
      root.executes(ctx -> status(ctx));
      root.then(Commands.literal("rules").executes(ctx -> rules(ctx)));
      root.then(Commands.literal("players").executes(ctx -> players(ctx)));
      root.then(Commands.literal("colonies").executes(ctx -> colonies(ctx)));
      root.then(Commands.literal("leave").executes(ctx -> leave(ctx)));
      RequiredArgumentBuilder<CommandSourceStack, String> rule = Commands.argument("rule", StringArgumentType.word());
      rule.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(purchasable(), builder));
      rule.executes(ctx -> addRule(ctx));
      root.then(Commands.literal("addrule").then(rule));
      RequiredArgumentBuilder<CommandSourceStack, Integer> amount = Commands.argument("amount", IntegerArgumentType.integer(1, 1000000));
      amount.executes(ctx -> transfer(ctx));
      root.then(Commands.literal("transfer").then(Commands.argument("player", EntityArgument.player()).then(amount)));
      event.getDispatcher().register(root);
   }

   static List<String> purchasable() {
      List<String> list = new ArrayList<>();

      for (CullingRule rule : CullingRule.values()) {
         if (rule.purchasable()) {
            list.add(rule.id());
         }
      }

      return list;
   }

   private static CommandSourceStack src(CommandContext<CommandSourceStack> ctx) {
      return (CommandSourceStack)ctx.getSource();
   }

   private static Component line(String key, Object... args) {
      return Component.translatable("cullinggame.cursed_domain.kogane." + key, args);
   }

   private static int status(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      CullingGameData data = CullingGame.data(player.server);
      if (!data.active()) {
         Kogane.say(player, data.phase == CullingGameData.Phase.ENDED ? "over" : "not_running");
         return 0;
      } else {
         PlayerRecord record = data.recordOf(player.getUUID());
         Kogane.say(player, "status.day", CullingGame.day(player.server, data) + 1, CullingRule.originals().size() + data.rules.size());
         if (record == null) {
            Kogane.say(player, "status.none");
         } else if (record.participant) {
            Colony colony = data.colony(record.colony);
            Kogane.say(
               player,
               "status.player",
               record.points,
               colony == null ? Component.translatable("cullinggame.cursed_domain.bar.outside") : colony.displayName(),
               record.rulesAdded
            );
         } else if (record.marked) {
            Kogane.say(player, "status.marked");
         } else {
            Kogane.say(player, "status.none");
         }

         MutableComponent menu = Component.literal("  ");

         for (String sub : new String[]{"rules", "players", "colonies", "addrule", "transfer", "leave"}) {
            String command = "/kogane " + sub + (sub.equals("addrule") || sub.equals("transfer") ? " " : "");
            menu.append(
               ComponentUtils.wrapInSquareBrackets(Component.literal(sub))
                  .withStyle(
                     style -> style.withColor(ChatFormatting.GOLD)
                        .withClickEvent(new ClickEvent(sub.equals("addrule") || sub.equals("transfer") ? Action.SUGGEST_COMMAND : Action.RUN_COMMAND, command))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("cullinggame.cursed_domain.menu." + sub)))
                  )
            );
            menu.append(Component.literal(" "));
         }

         player.sendSystemMessage(menu);
         return 1;
      }
   }

   private static int rules(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      CullingGameData data = CullingGame.data(player.server);
      Kogane.say(player, "rules.header");

      for (CullingRule rule : CullingRule.originals()) {
         player.sendSystemMessage(
            Component.literal(" " + (rule.ordinal() + 1) + ". ").withStyle(ChatFormatting.GOLD).append(rule.text().copy().withStyle(ChatFormatting.GRAY))
         );
      }

      for (int i = 0; i < data.rules.size(); i++) {
         AddedRule added = data.rules.get(i);
         player.sendSystemMessage(
            Component.literal(" " + (9 + i) + ". ")
               .withStyle(ChatFormatting.GOLD)
               .append(added.rule().text().copy().withStyle(ChatFormatting.WHITE))
               .append(Component.translatable("cullinggame.cursed_domain.rules.author", added.author()).withStyle(ChatFormatting.DARK_GRAY))
         );
      }

      return data.rules.size() + 8;
   }

   /** Rule 9. */
   private static int players(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      CullingGameData data = CullingGame.data(player.server);
      if (!data.active()) {
         Kogane.say(player, "not_running");
         return 0;
      } else if (!data.has(CullingRule.PLAYER_INFO)) {
         Kogane.say(player, "no_info_rule");
         return 0;
      } else {
         Kogane.say(player, "players.header");
         int count = 0;

         for (PlayerRecord record : data.participants()) {
            Colony colony = data.colony(record.colony);
            player.sendSystemMessage(
               Component.translatable(
                     "cullinggame.cursed_domain.players.entry",
                     record.name,
                     record.points,
                     record.rulesAdded,
                     colony == null ? Component.translatable("cullinggame.cursed_domain.bar.outside") : colony.displayName()
                  )
                  .withStyle(ChatFormatting.GRAY)
            );
            count++;
         }

         ServerLevel level = player.server.overworld();
         List<SorcererNpcEntity> npcs = level.getEntitiesOfClass(SorcererNpcEntity.class, player.getBoundingBox().inflate(256.0));

         for (SorcererNpcEntity npc : npcs) {
            if (!npc.shibuyaMode()) {
               Colony colony = data.colonyAt(npc.getX(), npc.getZ());
               player.sendSystemMessage(
                  Component.translatable(
                        "cullinggame.cursed_domain.players.entry",
                        npc.npcName(),
                        npc.points,
                        0,
                        colony == null ? Component.translatable("cullinggame.cursed_domain.bar.outside") : colony.displayName()
                     )
                     .withStyle(ChatFormatting.DARK_GRAY)
               );
               count++;
            }
         }

         return count;
      }
   }

   private static int colonies(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      CullingGameData data = CullingGame.data(player.server);
      if (data.colonies().isEmpty()) {
         Kogane.say(player, "not_running");
         return 0;
      } else {
         Kogane.say(player, "colonies.header");

         for (Colony colony : data.colonies()) {
            int distance = (int)colony.distance(player.getX(), player.getZ());
            player.sendSystemMessage(
               Component.translatable("cullinggame.cursed_domain.colonies.entry", colony.displayName(), colony.x(), colony.z(), distance)
                  .withStyle(colony.contains(player.getX(), player.getZ()) ? ChatFormatting.GOLD : ChatFormatting.GRAY)
            );
         }

         return data.colonies().size();
      }
   }

   private static int addRule(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      CullingRule rule = CullingRule.byId(StringArgumentType.getString(ctx, "rule"));
      if (rule == null) {
         Kogane.say(player, "rule_unknown");
         return 0;
      } else {
         String result = CullingGame.tryAddRule(player, rule);
         if (!result.equals("ok")) {
            Kogane.say(player, result, CullingGameData.ruleCost());
            return 0;
         } else {
            return 1;
         }
      }
   }

   private static int transfer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
      int amount = IntegerArgumentType.getInteger(ctx, "amount");
      String result = CullingGame.transfer(player, target, amount);
      if (!result.equals("ok")) {
         Kogane.say(player, result, CullingGameData.ruleCost());
         return 0;
      } else {
         return amount;
      }
   }

   private static int leave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      ServerPlayer player = src(ctx).getPlayerOrException();
      String result = CullingGame.leave(player);
      if (!result.equals("ok")) {
         Kogane.say(player, result, CullingGameData.ruleCost());
         return 0;
      } else {
         return 1;
      }
   }
}
