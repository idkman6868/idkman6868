package com.steelballrun.race;

import com.steelballrun.SteelBallRun;
import com.steelballrun.config.SbrConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

/**
 * The race rules. Shortcuts that can be stopped before they happen (ender pearls, chorus fruit, portals) are simply
 * refused. Ones that can only be noticed afterwards (gliding, boats, minecarts) are stopped and cost points.
 */
@EventBusSubscriber(modid = SteelBallRun.MODID)
public final class RaceRules {
   private static final Map<UUID, Long> LAST_PENALTY = new HashMap<>();

   private RaceRules() {
   }

   static void clearSession() {
      LAST_PENALTY.clear();
   }

   static void check(ServerPlayer player, RaceData d, Entrant e, long now) {
      if (SbrConfig.BAN_ELYTRA.get() && player.isFallFlying()) {
         player.stopFallFlying();
         penalize(player, d, e, "elytra", now);
      }
      Entity vehicle = player.getVehicle();
      if (vehicle instanceof Boat && SbrConfig.BAN_BOATS.get()) {
         player.stopRiding();
         penalize(player, d, e, "boat", now);
      } else if (vehicle instanceof AbstractMinecart && SbrConfig.BAN_MINECARTS.get()) {
         player.stopRiding();
         penalize(player, d, e, "minecart", now);
      }
   }

   private static void penalize(ServerPlayer player, RaceData d, Entrant e, String rule, long now) {
      Long last = LAST_PENALTY.get(e.id);
      if (last != null && now - last < 200L) {
         return;
      }
      LAST_PENALTY.put(e.id, now);
      int penalty = SbrConfig.PENALTY.get();
      e.points -= penalty;
      e.penalties += penalty;
      d.setDirty();
      player.sendSystemMessage(Component.translatable("rule.steel_ball_run.penalty", Component.translatable("rule.steel_ball_run." + rule), penalty)
         .withStyle(ChatFormatting.RED));
   }

   private static boolean racing(ServerPlayer player) {
      RaceData d = RaceData.get(player.server);
      if (d.phase != RaceData.Phase.RUNNING) {
         return false;
      }
      Entrant e = d.entrant(player.getUUID());
      return e != null && e.racing();
   }

   private static void refuse(ServerPlayer player, String rule) {
      player.displayClientMessage(Component.translatable("rule.steel_ball_run.refused", Component.translatable("rule.steel_ball_run." + rule))
         .withStyle(ChatFormatting.RED), true);
   }

   @SubscribeEvent
   public static void onPearl(EntityTeleportEvent.EnderPearl event) {
      ServerPlayer player = event.getPlayer();
      if (SbrConfig.BAN_PEARLS.get() && racing(player)) {
         event.setCanceled(true);
         refuse(player, "ender_pearl");
      }
   }

   @SubscribeEvent
   public static void onChorus(EntityTeleportEvent.ChorusFruit event) {
      if (event.getEntityLiving() instanceof ServerPlayer player && SbrConfig.BAN_CHORUS.get() && racing(player)) {
         event.setCanceled(true);
         refuse(player, "chorus_fruit");
      }
   }

   @SubscribeEvent
   public static void onTravel(EntityTravelToDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getDimension() != Level.OVERWORLD && SbrConfig.BAN_DIMENSIONS.get() && racing(player)) {
         event.setCanceled(true);
         refuse(player, "dimension");
      }
   }

   public static String[] activeRules() {
      java.util.List<String> rules = new java.util.ArrayList<>();
      rules.add("checkpoints");
      if (SbrConfig.BAN_ELYTRA.get()) rules.add("elytra");
      if (SbrConfig.BAN_PEARLS.get()) rules.add("ender_pearl");
      if (SbrConfig.BAN_CHORUS.get()) rules.add("chorus_fruit");
      if (SbrConfig.BAN_BOATS.get()) rules.add("boat");
      if (SbrConfig.BAN_MINECARTS.get()) rules.add("minecart");
      if (SbrConfig.BAN_DIMENSIONS.get()) rules.add("dimension");
      return rules.toArray(new String[0]);
   }
}
