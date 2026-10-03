package com.curseddomain.entity.cursedspirit;

import com.curseddomain.sorcerer.SorcererManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;

public final class CurseVisibility {
   private static final double REFRESH_RADIUS = 128.0;

   private CurseVisibility() {
   }

   public static boolean canSee(ServerPlayer player) {
      return player.isSpectator() || SorcererManager.get(player).canSeeCurses();
   }

   public static void refresh(ServerPlayer player) {
      ServerChunkCache chunks = player.serverLevel().getChunkSource();

      for (CursedSpirit curse : player.serverLevel().getEntitiesOfClass(CursedSpirit.class, player.getBoundingBox().inflate(128.0))) {
         chunks.removeEntity(curse);
         chunks.addEntity(curse);
      }
   }
}
