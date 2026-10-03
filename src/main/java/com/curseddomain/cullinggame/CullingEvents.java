package com.curseddomain.cullinggame;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.CullingGameData.PlayerRecord;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.entity.cursedspirit.CursedSpirit;
import com.curseddomain.shibuya.ShibuyaIncident;
import com.curseddomain.shikigami.ShikigamiEntity;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.SorcererManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import org.jetbrains.annotations.Nullable;

/** Scoring (rules 4 and 5) and session bookkeeping for the Culling Game. */
@EventBusSubscriber(modid = ModMain.MODID)
public final class CullingEvents {
   private CullingEvents() {
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      LivingEntity victim = event.getEntity();
      if (!victim.level().isClientSide && victim.level().getServer() != null) {
         CullingGameData data = CullingGame.data(victim.level().getServer());
         if (data.active()) {
            ServerPlayer killer = killer(event.getSource().getEntity());
            int value = 0;
            if (victim instanceof ServerPlayer player) {
               PlayerRecord record = data.recordOf(player.getUUID());
               if (record != null && record.participant) {
                  value = SorcererManager.get(player).status().awakened() ? 5 : 1;
                  if (CullingConfig.b(CullingConfig.DEATH_RESETS_POINTS) && record.points > 0) {
                     Kogane.say(player, "points_lost", record.points);
                     record.points = 0;
                     data.setDirty();
                  }
               }
            } else if (victim instanceof SorcererNpcEntity npc) {
               value = npc.profile().sorcerer() ? 5 : 1;
               if (event.getSource().getEntity() instanceof SorcererNpcEntity other && other != npc) {
                  other.points += value;
               }
            } else if (victim instanceof CursedSpirit curse && data.colonyAt(victim.getX(), victim.getZ()) != null) {
               value = curse.curseGrade() == Grade.GRADE_4 ? 1 : 5;
            }

            if (killer != null && killer != victim && value > 0) {
               CullingGame.givePoints(killer, value, victim.getDisplayName());
            }
         }
      }
   }

   /** Whoever is responsible for a kill: the player, or the player whose shikigami did it. */
   @Nullable
   private static ServerPlayer killer(@Nullable Entity entity) {
      if (entity instanceof ServerPlayer player) {
         return player;
      } else if (entity instanceof ShikigamiEntity shikigami && shikigami.summoner() instanceof ServerPlayer owner) {
         return owner;
      } else {
         return null;
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CullingGame.forgetPosition(player.getUUID());
         Kogane.forget(player.getUUID());
         CullingGameData data = CullingGame.data(player.server);
         if (data.active()) {
            CullingGame.considerMark(player, data);
            PlayerRecord record = data.recordOf(player.getUUID());
            if (record != null && (record.participant || record.marked)) {
               Kogane.say(player, "welcome_back", record.points);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CullingGame.forgetPosition(player.getUUID());
         Kogane.forget(player.getUUID());
         ShibuyaIncident.forget(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CullingGame.forgetPosition(player.getUUID());
         ShibuyaIncident.forget(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         CullingGame.forgetPosition(player.getUUID());
         ShibuyaIncident.forget(player.getUUID());
      }
   }
}
