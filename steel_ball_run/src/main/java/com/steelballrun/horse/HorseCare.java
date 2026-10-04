package com.steelballrun.horse;

import com.steelballrun.SteelBallRun;
import com.steelballrun.config.SbrConfig;
import com.steelballrun.network.HorseStatusPayload;
import com.steelballrun.race.Background;
import com.steelballrun.race.Entrant;
import com.steelballrun.race.RaceData;
import com.steelballrun.registry.SbrAttachments;
import com.steelballrun.registry.SbrItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Stamina while riding, and caring for horses: feeding (wheat, sugar, carrots, apples, hay bales) and brushing.
 * Golden carrots and golden apples are left to vanilla so horse breeding still works.
 */
@EventBusSubscriber(modid = SteelBallRun.MODID)
public final class HorseCare {
   /** How fast a horse goes per point of its movement-speed attribute, in blocks per second. */
   public static final double BLOCKS_PER_SECOND = 43.17;
   private static final ResourceLocation EXHAUSTED = SteelBallRun.id("exhausted");
   private static final int INTERVAL = 10;
   private static final Map<UUID, Ride> RIDES = new HashMap<>();

   private static final class Ride {
      final int horse;
      double x;
      double z;
      long tick;

      Ride(int horse, double x, double z, long tick) {
         this.horse = horse;
         this.x = x;
         this.z = z;
         this.tick = tick;
      }
   }

   private HorseCare() {
   }

   public static void clearSession() {
      RIDES.clear();
   }

   // ---------------------------------------------------------------------------------------------- riding

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % INTERVAL != 0) {
         return;
      }
      ServerLevel level = player.serverLevel();
      long now = level.getGameTime();
      if (player.getVehicle() instanceof AbstractHorse horse && horse.isTamed()) {
         HorseData h = horse.getData(SbrAttachments.HORSE);
         double max = prepare(horse, h, now);
         Ride ride = RIDES.get(player.getUUID());
         if (ride == null || ride.horse != horse.getId()) {
            RIDES.put(player.getUUID(), new Ride(horse.getId(), horse.getX(), horse.getZ(), now));
            send(player, horse, h, max, false);
            return;
         }
         double seconds = Math.max(0.05, (now - ride.tick) / 20.0);
         double dist = Math.sqrt(sq(horse.getX() - ride.x) + sq(horse.getZ() - ride.z));
         ride.x = horse.getX();
         ride.z = horse.getZ();
         ride.tick = now;
         AttributeInstance speedAttr = horse.getAttribute(Attributes.MOVEMENT_SPEED);
         double top = speedAttr == null ? 0.0 : speedAttr.getBaseValue() * BLOCKS_PER_SECOND;
         // a teleport or a chunk-border hiccup isn't riding
         if (dist > Math.max(20.0, top * seconds * 3.0)) {
            dist = 0.0;
         }
         double ratio = top <= 0.0 ? 0.0 : dist / seconds / top;
         BlockPos pos = horse.blockPosition();
         Affinity terrain = Affinity.of(level.getBiome(pos).value().getBaseTemperature(), pos.getY());
         double drainMult = (terrain == h.affinity ? 0.75 : 1.0) * Stamina.bondDrain(h.bond) * (isJockey(player) ? 0.85 : 1.0);
         Stamina.State s = Stamina.step(h.stamina, max, h.exhausted, ratio, seconds, drainMult, h.endurance, SbrConfig.tuning());
         boolean tired = s.exhausted() && !h.exhausted;
         boolean recovered = !s.exhausted() && h.exhausted;
         h.stamina = s.stamina();
         h.exhausted = s.exhausted();
         h.lastUpdate = now;
         h.ridden += dist;
         while (h.ridden >= 300.0) {
            h.ridden -= 300.0;
            h.addBond(1);
         }
         applySlowdown(horse, h.exhausted);
         if (tired) {
            player.displayClientMessage(Component.translatable("message.steel_ball_run.horse_exhausted").withStyle(ChatFormatting.RED), true);
            level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_BREATHE, SoundSource.NEUTRAL, 1.5F, 0.7F);
         } else if (recovered) {
            player.displayClientMessage(Component.translatable("message.steel_ball_run.horse_recovered").withStyle(ChatFormatting.GREEN), true);
         } else if (ratio > Stamina.GALLOP && h.stamina < max * 0.2 && player.tickCount % 40 == 0) {
            level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_BREATHE, SoundSource.NEUTRAL, 1.0F, 0.9F);
         }
         send(player, horse, h, max, ratio > Stamina.GALLOP);
      } else if (RIDES.remove(player.getUUID()) != null) {
         PacketDistributor.sendToPlayer(player, HorseStatusPayload.NONE);
      }
   }

   /**
    * Rolls a horse's stats the first time it matters and catches up on the rest it got while nobody rode it.
    * Returns its maximum stamina.
    */
   static double prepare(AbstractHorse horse, HorseData h, long now) {
      if (!h.rolled) {
         h.roll(horse.getRandom());
      }
      double max = h.max(SbrConfig.HORSE_STAMINA.get());
      if (h.stamina < 0.0) {
         h.stamina = max;
      } else if (h.lastUpdate > 0L && now > h.lastUpdate + INTERVAL * 2) {
         h.stamina = Stamina.restWhileUnridden(h.stamina, max, (now - h.lastUpdate) / 20.0, h.endurance, SbrConfig.tuning());
         if (h.exhausted && h.stamina >= max * SbrConfig.RECOVER_FRACTION.get()) {
            h.exhausted = false;
            applySlowdown(horse, false);
         }
      }
      h.stamina = Math.min(h.stamina, max);
      h.lastUpdate = now;
      return max;
   }

   private static void applySlowdown(AbstractHorse horse, boolean exhausted) {
      AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         if (exhausted) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(EXHAUSTED, -SbrConfig.EXHAUSTED_SLOWDOWN.get(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
         } else {
            speed.removeModifier(EXHAUSTED);
         }
      }
   }

   private static boolean isJockey(ServerPlayer player) {
      Entrant e = RaceData.get(player.server).entrant(player.getUUID());
      return e != null && e.background == Background.JOCKEY;
   }

   private static void send(ServerPlayer player, AbstractHorse horse, HorseData h, double max, boolean galloping) {
      PacketDistributor.sendToPlayer(
         player, new HorseStatusPayload(horse.getId(), (float)h.stamina, (float)max, h.bond, h.exhausted, galloping, h.breed, h.affinity.ordinal())
      );
   }

   private static double sq(double v) {
      return v * v;
   }

   // ---------------------------------------------------------------------------------------------- care

   /** Stamina a food gives back, or 0 if it isn't horse food we handle. */
   public static int foodValue(ItemStack stack) {
      if (stack.is(Items.HAY_BLOCK)) {
         return 45;
      } else if (stack.is(Items.APPLE)) {
         return 14;
      } else if (stack.is(Items.CARROT)) {
         return 12;
      } else if (stack.is(Items.SUGAR)) {
         return 8;
      } else {
         return stack.is(Items.WHEAT) ? 6 : 0;
      }
   }

   @SubscribeEvent
   public static void onInteract(PlayerInteractEvent.EntityInteract event) {
      if (!(event.getTarget() instanceof AbstractHorse horse) || !horse.isTamed()) {
         return;
      }
      ItemStack stack = event.getItemStack();
      boolean brush = stack.is(SbrItems.HORSE_BRUSH.get());
      int food = foodValue(stack);
      if (!brush && food <= 0) {
         return;
      }
      event.setCanceled(true);
      event.setCancellationResult(InteractionResult.SUCCESS);
      Player player = event.getEntity();
      if (!(player instanceof ServerPlayer sp)) {
         return;
      }
      ServerLevel level = sp.serverLevel();
      long now = level.getGameTime();
      long day = level.getDayTime() / 24000L;
      HorseData h = horse.getData(SbrAttachments.HORSE);
      double max = prepare(horse, h, now);
      if (brush) {
         if (h.brushDay == day) {
            sp.displayClientMessage(Component.translatable("message.steel_ball_run.already_brushed"), true);
            return;
         }
         h.brushDay = day;
         h.addBond(3);
         h.stamina = Math.min(max, h.stamina + 5.0);
         level.sendParticles(ParticleTypes.HAPPY_VILLAGER, horse.getX(), horse.getY() + 1.2, horse.getZ(), 8, 0.5, 0.4, 0.5, 0.0);
         level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_SADDLE, SoundSource.NEUTRAL, 0.6F, 1.4F);
      } else {
         h.stamina = Math.min(max, h.stamina + food);
         horse.heal(food / 4.0F);
         if (h.feedDay != day) {
            h.feedDay = day;
            h.feedsToday = 0;
         }
         if (h.feedsToday < 3) {
            h.feedsToday++;
            h.addBond(1);
         }
         if (!sp.isCreative()) {
            stack.shrink(1);
         }
         level.sendParticles(ParticleTypes.HAPPY_VILLAGER, horse.getX(), horse.getY() + 1.4, horse.getZ(), 4, 0.3, 0.3, 0.3, 0.0);
         level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.HORSE_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);
      }
      max = h.max(SbrConfig.HORSE_STAMINA.get());
      if (h.exhausted && h.stamina >= max * SbrConfig.RECOVER_FRACTION.get()) {
         h.exhausted = false;
         applySlowdown(horse, false);
      }
      sp.displayClientMessage(Component.translatable("message.steel_ball_run.horse_care", (int)h.stamina, (int)max, h.bond), true);
      if (sp.getVehicle() == horse) {
         send(sp, horse, h, max, false);
      }
   }

   /** Describes a horse in chat: breed, terrain, stamina, bond and speed. */
   public static void describe(ServerPlayer player, AbstractHorse horse) {
      HorseData h = horse.getData(SbrAttachments.HORSE);
      double max = prepare(horse, h, player.serverLevel().getGameTime());
      AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
      double bps = speed == null ? 0.0 : speed.getBaseValue() * BLOCKS_PER_SECOND;
      player.sendSystemMessage(Component.translatable("message.steel_ball_run.horse_info", Component.translatable(h.breedKey()),
         Component.translatable(h.affinity.langKey()), (int)h.stamina, (int)max, h.bond, String.format(java.util.Locale.ROOT, "%.1f", bps),
         String.format(java.util.Locale.ROOT, "%.2f", h.endurance)).withStyle(ChatFormatting.GOLD));
   }

   /** Rival horses that come back from disk belong to a rider who is gone; remove them. */
   @SubscribeEvent
   public static void onJoin(EntityJoinLevelEvent event) {
      Entity entity = event.getEntity();
      if (!event.getLevel().isClientSide && event.loadedFromDisk() && entity.getTags().contains(com.steelballrun.npc.RivalManager.HORSE_TAG)) {
         event.setCanceled(true);
      }
   }
}
