package com.curseddomain.landmark;

import com.curseddomain.ModMain;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /atlas for everyone, /jjk landmark for operators. */
@EventBusSubscriber(modid = ModMain.MODID)
public final class LandmarkCommands {
   private LandmarkCommands() {
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> atlas = Commands.literal("atlas");
      atlas.executes(ctx -> atlas(ctx));
      event.getDispatcher().register(atlas);
      LiteralArgumentBuilder<CommandSourceStack> jjk = Commands.literal("jjk");
      jjk.requires(source -> ((CommandSourceStack)source).hasPermission(2));
      LiteralArgumentBuilder<CommandSourceStack> landmark = Commands.literal("landmark");
      landmark.then(Commands.literal("list").executes(ctx -> atlas(ctx)));
      RequiredArgumentBuilder<CommandSourceStack, String> type = Commands.argument("type", StringArgumentType.word());
      type.suggests((ctx, b) -> SharedSuggestionProvider.suggest(typeIds(), b));
      type.executes(ctx -> build(ctx));
      landmark.then(Commands.literal("build").then(type));
      RequiredArgumentBuilder<CommandSourceStack, String> id = Commands.argument("id", StringArgumentType.word());
      id.suggests((ctx, b) -> SharedSuggestionProvider.suggest(siteIds(ctx), b));
      id.executes(ctx -> remove(ctx));
      landmark.then(Commands.literal("remove").then(id));
      RequiredArgumentBuilder<CommandSourceStack, String> rebuild = Commands.argument("id", StringArgumentType.word());
      rebuild.suggests((ctx, b) -> SharedSuggestionProvider.suggest(siteIds(ctx), b));
      rebuild.executes(ctx -> rebuild(ctx));
      landmark.then(Commands.literal("rebuild").then(rebuild));
      jjk.then(landmark);
      event.getDispatcher().register(jjk);
   }

   private static CommandSourceStack src(CommandContext<CommandSourceStack> ctx) {
      return (CommandSourceStack)ctx.getSource();
   }

   private static List<String> typeIds() {
      List<String> ids = new ArrayList<>();

      for (LandmarkType type : LandmarkType.values()) {
         ids.add(type.id());
      }

      return ids;
   }

   private static List<String> siteIds(CommandContext<CommandSourceStack> ctx) {
      return new ArrayList<>(Landmarks.data(src(ctx).getServer()).sites.keySet());
   }

   private static int atlas(CommandContext<CommandSourceStack> ctx) {
      AtlasData data = Landmarks.data(src(ctx).getServer());
      Vec3 here = src(ctx).getPosition();
      src(ctx).sendSuccess(() -> Component.translatable("landmark.cursed_domain.atlas.header").withStyle(ChatFormatting.GOLD), false);

      for (Site site : data.sites.values()) {
         int distance = (int)Math.sqrt((here.x - site.x) * (here.x - site.x) + (here.z - site.z) * (here.z - site.z));
         int percent = site.built.size() * 100 / Math.max(1, site.chunkCount());
         Component line = Component.translatable("landmark.cursed_domain.atlas.entry", site.displayName(), site.x, site.z, distance, percent)
            .withStyle(ChatFormatting.GRAY);
         src(ctx).sendSuccess(() -> line, false);
      }

      return data.sites.size();
   }

   private static int build(CommandContext<CommandSourceStack> ctx) {
      LandmarkType type = LandmarkType.byId(StringArgumentType.getString(ctx, "type"));
      if (type == null) {
         src(ctx).sendFailure(Component.translatable("landmark.cursed_domain.unknown"));
         return 0;
      } else {
         BlockPos pos = BlockPos.containing(src(ctx).getPosition());
         Site site = Landmarks.create(src(ctx).getServer(), type, pos.getX(), pos.getZ());
         src(ctx).sendSuccess(() -> Component.translatable("landmark.cursed_domain.created", site.displayName(), site.id), true);
         return 1;
      }
   }

   private static int remove(CommandContext<CommandSourceStack> ctx) {
      String id = StringArgumentType.getString(ctx, "id");
      if (!Landmarks.remove(src(ctx).getServer(), id)) {
         src(ctx).sendFailure(Component.translatable("landmark.cursed_domain.unknown"));
         return 0;
      } else {
         src(ctx).sendSuccess(() -> Component.translatable("landmark.cursed_domain.removed", id), true);
         return 1;
      }
   }

   /** Forgets which chunks were built, so the landmark is laid down again (e.g. after it was griefed). */
   private static int rebuild(CommandContext<CommandSourceStack> ctx) {
      String id = StringArgumentType.getString(ctx, "id");
      Site site = Landmarks.site(src(ctx).getServer(), id);
      if (site == null) {
         src(ctx).sendFailure(Component.translatable("landmark.cursed_domain.unknown"));
         return 0;
      } else {
         site.built.clear();
         Landmarks.data(src(ctx).getServer()).setDirty();
         src(ctx).sendSuccess(() -> Component.translatable("landmark.cursed_domain.rebuilding", id), true);
         return 1;
      }
   }
}
