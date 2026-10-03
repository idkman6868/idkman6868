package com.curseddomain.command;

import com.curseddomain.util.EnumNames;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

final class EnumArgument {
   private static final DynamicCommandExceptionType INVALID = new DynamicCommandExceptionType(
      value -> Component.translatable("commands.cursed_domain.error.invalid_value", new Object[]{String.valueOf(value)})
   );

   private EnumArgument() {
   }

   static <E extends Enum<E> & StringRepresentable> RequiredArgumentBuilder<CommandSourceStack, String> argument(String name, E[] values) {
      return Commands.argument(name, StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(names(values), builder));
   }

   static <E extends Enum<E> & StringRepresentable> E get(CommandContext<CommandSourceStack> ctx, String name, E[] values) throws CommandSyntaxException {
      String raw = StringArgumentType.getString(ctx, name);
      E value = EnumNames.byName(values, raw, null);
      if (value == null) {
         throw INVALID.create(raw);
      } else {
         return value;
      }
   }

   static <E extends Enum<E> & StringRepresentable> List<String> names(E[] values) {
      return EnumNames.names(values);
   }
}
