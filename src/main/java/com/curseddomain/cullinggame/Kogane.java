package com.curseddomain.cullinggame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;

/**
 * Kogane, the shikigami that serves as every player's interface to the game. Speaks in chat, shows a status bar,
 * and makes the announcements.
 */
public final class Kogane {
   private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();

   private Kogane() {
   }

   public static MutableComponent prefix() {
      return Component.translatable("cullinggame.cursed_domain.kogane.name").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
   }

   public static void say(ServerPlayer player, Component line) {
      player.sendSystemMessage(prefix().append(Component.literal(" ")).append(line.copy().withStyle(ChatFormatting.YELLOW)));
   }

   public static void say(ServerPlayer player, String key, Object... args) {
      say(player, Component.translatable("cullinggame.cursed_domain.kogane." + key, args));
   }

   public static void chime(ServerPlayer player) {
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.6F);
   }

   /** Kogane announcement to everyone on the server. */
   public static void announce(MinecraftServer server, Component line) {
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         say(player, line);
         chime(player);
      }
   }

   public static void title(ServerPlayer player, Component title, Component subtitle, int stay) {
      player.connection.send(new ClientboundSetTitlesAnimationPacket(10, stay, 20));
      player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
      player.connection.send(new ClientboundSetTitleTextPacket(title));
   }

   public static void titleAll(MinecraftServer server, Component title, Component subtitle, int stay) {
      List<ServerPlayer> players = server.getPlayerList().getPlayers();

      for (ServerPlayer player : players) {
         title(player, title, subtitle, stay);
      }
   }

   /** A speaker line, e.g. "[Kenjaku] ...". */
   public static void speech(ServerPlayer player, String speakerKey, Component line, ChatFormatting color) {
      player.sendSystemMessage(
         Component.translatable(speakerKey)
            .withStyle(new ChatFormatting[]{color, ChatFormatting.BOLD})
            .append(Component.literal(" "))
            .append(line.copy().withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}))
      );
   }

   public static void updateBar(ServerPlayer player, Component text, float progress, boolean urgent) {
      ServerBossEvent bar = BARS.get(player.getUUID());
      if (bar == null) {
         bar = new ServerBossEvent(text, BossBarColor.YELLOW, BossBarOverlay.NOTCHED_10);
         BARS.put(player.getUUID(), bar);
         bar.addPlayer(player);
      }

      bar.setName(text);
      bar.setProgress(Math.max(0.0F, Math.min(1.0F, progress)));
      bar.setColor(urgent ? BossBarColor.RED : BossBarColor.YELLOW);
   }

   public static void removeBar(ServerPlayer player) {
      ServerBossEvent bar = BARS.remove(player.getUUID());
      if (bar != null) {
         bar.removeAllPlayers();
      }
   }

   /** Re-attaches a player's bar after they log back in (the old connection is gone). */
   public static void forget(UUID player) {
      ServerBossEvent bar = BARS.remove(player);
      if (bar != null) {
         bar.removeAllPlayers();
      }
   }

   public static void clear() {
      for (ServerBossEvent bar : BARS.values()) {
         bar.removeAllPlayers();
      }

      BARS.clear();
   }
}
