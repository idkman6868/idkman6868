package com.curseddomain.energy;

import com.curseddomain.ModMain;
import com.curseddomain.config.CommonConfig;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.domain.DomainManager;
import com.curseddomain.network.payload.CursedEnergySyncPayload;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.Technique;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EnergyManager {
   static final int WORK_INTERVAL = 5;

   private EnergyManager() {
   }

   public static CursedEnergyData get(Player player) {
      return (CursedEnergyData)player.getData(ModAttachments.CURSED_ENERGY);
   }

   public static void recalculate(ServerPlayer player) {
      SorcererData sorcerer = SorcererManager.get(player);
      CursedEnergyData energy = get(player);
      if (!sorcerer.hasCursedEnergy()) {
         energy.setDerived(0.0F, 0.0F, 0.0F, 0.0F, CursedEnergyNature.STANDARD);
      } else {
         ServerConfig.GradeStats base = ServerConfig.gradeStats(sorcerer.grade());
         float max = ((Double)base.maxEnergy().get()).floatValue() + energy.trainedMax();
         float regen = ((Double)base.regenPerSecond().get()).floatValue() + energy.trainedRegen();
         float output = ((Double)base.output().get()).floatValue() + energy.trainedOutput();
         float control = ((Double)base.control().get()).floatValue() + energy.trainedControl();
         if (sorcerer.hasTrait(InnateTrait.HEAVENLY_RESTRICTION_ENERGY)) {
            max *= ((Double)ServerConfig.HR_ENERGY_POOL_MULTIPLIER.get()).floatValue();
            regen *= ((Double)ServerConfig.HR_ENERGY_REGEN_MULTIPLIER.get()).floatValue();
            output *= ((Double)ServerConfig.HR_ENERGY_OUTPUT_MULTIPLIER.get()).floatValue();
         }

         CursedEnergyNature nature = sorcerer.technique().map(Technique::nature).orElse(CursedEnergyNature.STANDARD);
         energy.setDerived(max, regen, output, Math.max(0.0F, Math.min(1.0F, control)), nature);
      }
   }

   public static float effectiveCost(ServerPlayer player, float baseCost) {
      SorcererData sorcerer = SorcererManager.get(player);
      CursedEnergyData energy = get(player);
      return baseCost
         * DomainManager.costMultiplier(player)
         * EnergyMath.costMultiplier(
            energy.control(),
            ((Double)ServerConfig.CONTROL_MAX_COST_REDUCTION.get()).floatValue(),
            sorcerer.hasTrait(InnateTrait.SIX_EYES),
            ((Double)ServerConfig.SIX_EYES_COST_MULTIPLIER.get()).floatValue()
         );
   }

   public static boolean canAfford(ServerPlayer player, float baseCost) {
      CursedEnergyData energy = get(player);
      return SorcererManager.get(player).hasCursedEnergy() && EnergyMath.affordable(effectiveCost(player, baseCost), energy.current(), energy.output());
   }

   public static boolean tryConsume(ServerPlayer player, float baseCost) {
      if (!canAfford(player, baseCost)) {
         return false;
      } else {
         CursedEnergyData energy = get(player);
         energy.setCurrent(energy.current() - effectiveCost(player, baseCost));
         energy.markDirty(true);
         return true;
      }
   }

   public static void setCurrent(ServerPlayer player, float value) {
      CursedEnergyData energy = get(player);
      energy.setCurrent(value);
      energy.markDirty(true);
   }

   public static void restore(ServerPlayer player, float amount) {
      CursedEnergyData energy = get(player);
      setCurrent(player, energy.current() + amount);
   }

   public static void addTraining(ServerPlayer player, float max, float regen, float output, float control) {
      get(player).addTraining(max, regen, output, control);
      recalculate(player);
   }

   public static void markCombat(ServerPlayer player) {
      get(player).setLastCombatTick(now(player));
   }

   public static boolean inCombat(ServerPlayer player) {
      return now(player) - get(player).lastCombatTick() < ((Integer)ServerConfig.COMBAT_TAG_SECONDS.get()).intValue() * 20L;
   }

   public static void tick(ServerPlayer player) {
      CursedEnergyData energy = get(player);
      if (player.tickCount % 5 == 0) {
         work(player, energy);
      }

      long now = now(player);
      if (energy.needsSync(now, (Integer)ServerConfig.SYNC_INTERVAL_TICKS.get())) {
         sync(player);
      }
   }

   private static void work(ServerPlayer player, CursedEnergyData energy) {
      if (!SorcererManager.get(player).hasCursedEnergy()) {
         energy.setFlags(false, false);
      } else {
         boolean inCombat = inCombat(player);
         boolean still = player.isCrouching() && player.onGround() && !player.isPassenger();
         energy.trackStillness(still, 5, player.position());
         int meditationDelay = (int)Math.round((Double)ServerConfig.MEDITATION_DELAY_SECONDS.get() * 20.0);
         boolean meditating = !inCombat && still && energy.stillTicks() >= meditationDelay;
         energy.setFlags(meditating, inCombat);
         if (energy.current() < energy.max()) {
            float amount = EnergyMath.regenAmount(
               energy.regenPerSecond(),
               0.25F,
               inCombat,
               meditating,
               ((Double)ServerConfig.OUT_OF_COMBAT_REGEN_MULTIPLIER.get()).floatValue(),
               ((Double)ServerConfig.MEDITATION_REGEN_MULTIPLIER.get()).floatValue()
            );
            energy.setCurrent(energy.current() + amount);
         }
      }
   }

   public static void onRespawn(ServerPlayer player, boolean fromDeath) {
      recalculate(player);
      if (fromDeath) {
         CursedEnergyData energy = get(player);
         energy.setCurrent(energy.max() * ((Double)ServerConfig.RESPAWN_ENERGY_FRACTION.get()).floatValue());
      }

      sync(player);
   }

   public static void sync(ServerPlayer player) {
      CursedEnergyData energy = get(player);
      SorcererData sorcerer = SorcererManager.get(player);
      CursedEnergyNature visibleNature = sorcerer.techniqueRevealed() ? energy.nature() : CursedEnergyNature.STANDARD;
      PacketDistributor.sendToPlayer(
         player,
         CursedEnergySyncPayload.of(energy.current(), energy.max(), energy.output(), energy.control(), visibleNature, energy.meditating(), energy.inCombat()),
         new CustomPacketPayload[0]
      );
      energy.markSynced(now(player));
      if ((Boolean)CommonConfig.DEBUG_LOGGING.get()) {
         ModMain.LOGGER.debug("[energy] {} {}/{}", new Object[]{player.getScoreboardName(), energy.current(), energy.max()});
      }
   }

   private static long now(ServerPlayer player) {
      return player.server.getTickCount();
   }
}
