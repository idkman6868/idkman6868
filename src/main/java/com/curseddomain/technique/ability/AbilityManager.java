package com.curseddomain.technique.ability;

import com.curseddomain.config.CombatConfig;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.network.payload.AbilitySyncPayload;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.universal.UniversalTechniques;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class AbilityManager {
   private AbilityManager() {
   }

   public static AbilityState state(Player player) {
      return (AbilityState)player.getData(ModAttachments.ABILITY_STATE);
   }

   public static long serverTick() {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      return server == null ? 0L : server.getTickCount();
   }

   public static List<Ability> abilities(Player player) {
      SorcererData data = SorcererManager.get(player);
      List<Ability> list = new ArrayList<>();
      if (techniqueAvailable(player)) {
         data.technique().ifPresent(t -> list.addAll(t.abilities()));
      }

      if (data.hasCursedEnergy()) {
         for (Ability ability : UniversalTechniques.INSTANCE.abilities()) {
            if (ability.requiredUnlock() == null || data.isUnlocked(ability.requiredUnlock())) {
               list.add(ability);
            }
         }
      }

      return list;
   }

   public static boolean techniqueAvailable(Player player) {
      SorcererData data = SorcererManager.get(player);
      boolean statusOk = CombatConfig.TECHNIQUES_NEED_ENROLLMENT.get() ? data.status().enrolled() : data.status().awakened();
      return statusOk && data.technique().isPresent();
   }

   @Nullable
   private static Ability byId(Player player, ResourceLocation id) {
      for (Ability ability : abilities(player)) {
         if (ability.id().equals(id)) {
            return ability;
         }
      }

      return null;
   }

   public static void input(ServerPlayer player, int index, boolean pressed) {
      List<Ability> list = abilities(player);
      if (index >= 0 && index < list.size() && player.isAlive()) {
         Ability ability = list.get(index);
         if (pressed) {
            press(player, ability);
         } else {
            release(player, ability);
         }
      }
   }

   public static void assignSlot(ServerPlayer player, int slot, int index) {
      if (slot >= 0 && slot <= 4 && index >= 0 && index < abilities(player).size()) {
         AbilityState state = state(player);
         state.slots[slot] = index;
         state.dirty = true;
      }
   }

   private static void press(ServerPlayer player, Ability ability) {
      AbilityState state = state(player);
      boolean active = state.activeSince.containsKey(ability.id());
      if (active && ability.kind() == AbilityKind.TOGGLE) {
         stop(player, ability, true);
      } else {
         AbilityContext ctx = new AbilityContext(player, ability);
         if (check(player, ability, ctx.stats, true)) {
            switch (ability.kind()) {
               case INSTANT:
                  if (ability.activate(ctx)) {
                     spend(player, ctx.stats.cost());
                     startCooldown(state, ability, ctx.stats.cooldown());
                  }
                  break;
               case CHARGE:
                  state.charging = ability.id();
                  state.chargeStart = serverTick();
                  break;
               case TOGGLE:
               case CHANNEL:
                  if (ability.activate(ctx)) {
                     spend(player, ctx.stats.cost());
                     state.activeSince.put(ability.id(), serverTick());
                  }
            }

            state.dirty = true;
         }
      }
   }

   private static void release(ServerPlayer player, Ability ability) {
      AbilityState state = state(player);
      if (ability.kind() == AbilityKind.CHARGE && ability.id().equals(state.charging)) {
         state.charging = null;
         state.dirty = true;
         AbilityContext ctx = new AbilityContext(player, ability);
         if (!check(player, ability, ctx.stats, true)) {
            return;
         }

         long held = serverTick() - state.chargeStart;
         float charge = Math.min(1.0F, (float)held / Math.max(1, ctx.stats.chargeTicks()));
         spend(player, ctx.stats.cost());
         ability.release(ctx, charge);
         startCooldown(state, ability, ctx.stats.cooldown());
      } else if (ability.kind() == AbilityKind.CHANNEL && state.activeSince.containsKey(ability.id())) {
         stop(player, ability, true);
      }
   }

   public static boolean techniqueUsable(ServerPlayer player) {
      SorcererData data = SorcererManager.get(player);
      boolean statusOk = CombatConfig.TECHNIQUES_NEED_ENROLLMENT.get() ? data.status().enrolled() : data.status().awakened();
      return statusOk && data.technique().isPresent();
   }

   private static boolean check(ServerPlayer player, Ability ability, AbilityStats stats, boolean notify) {
      String failure = failure(player, ability, stats);
      if (failure != null && notify) {
         player.displayClientMessage(
            Component.translatable("ability.cursed_domain.fail." + failure, new Object[]{ability.displayName()}).withStyle(ChatFormatting.RED), true
         );
      }

      return failure == null;
   }

   @Nullable
   private static String failure(ServerPlayer player, Ability ability, AbilityStats stats) {
      boolean universal = UniversalTechniques.isUniversal(ability);
      if (universal ? SorcererManager.get(player).hasCursedEnergy() || !(stats.cost() > 0.0F) : techniqueUsable(player)) {
         if (!universal && player.hasEffect(ModEffects.DOMAIN_AMPLIFICATION)) {
            return "amplified";
         } else if (player.hasEffect(ModEffects.STUNNED)) {
            return "stunned";
         } else if (player.hasEffect(ModEffects.EXHAUSTED)) {
            return "exhausted";
         } else if (!universal && player.hasEffect(ModEffects.BURNOUT)) {
            return "burnout";
         } else {
            SorcererData data = SorcererManager.get(player);
            if (!data.grade().atLeast(ability.minGrade())) {
               return "grade";
            } else if (ability.requiredUnlock() != null && !data.isUnlocked(ability.requiredUnlock())) {
               return "unlock." + ability.requiredUnlock();
            } else {
               Long until = state(player).cooldownUntil.get(ability.id());
               if (until != null && until > serverTick()) {
                  return "cooldown";
               } else {
                  if (stats.cost() > 0.0F) {
                     float cost = EnergyManager.effectiveCost(player, stats.cost());
                     if (cost > EnergyManager.get(player).output() + 1.0E-4F) {
                        return "output";
                     }

                     if (cost > EnergyManager.get(player).current() + 1.0E-4F) {
                        return "energy";
                     }
                  }

                  return null;
               }
            }
         }
      } else {
         return "locked";
      }
   }

   private static void spend(ServerPlayer player, float cost) {
      if (cost > 0.0F) {
         EnergyManager.tryConsume(player, cost);
         checkExhaustion(player);
      }
   }

   private static void checkExhaustion(ServerPlayer player) {
      if (EnergyManager.get(player).current() < 1.0F && EnergyManager.get(player).max() > 0.0F) {
         int ticks = (Integer)CombatConfig.EXHAUSTION_SECONDS.get() * 20;
         if (ticks > 0) {
            player.addEffect(new MobEffectInstance(ModEffects.EXHAUSTED, ticks, 0, false, false, true));
            player.displayClientMessage(Component.translatable("ability.cursed_domain.exhausted").withStyle(ChatFormatting.DARK_RED), true);
         }

         stopAll(player);
      }
   }

   public static void startCooldown(AbilityState state, Ability ability, int ticks) {
      if (ticks > 0) {
         state.cooldownUntil.put(ability.id(), serverTick() + ticks);
         state.cooldownTotal.put(ability.id(), ticks);
      }

      state.dirty = true;
   }

   public static void tick(ServerPlayer player) {
      AbilityState state = state(player);
      SorcererManager.get(player).technique().ifPresent(t -> t.passiveTick(player));
      if (!state.activeSince.isEmpty()) {
         for (Entry<ResourceLocation, Long> entry : new ArrayList<>(state.activeSince.entrySet())) {
            Ability ability = byId(player, entry.getKey());
            if (ability == null) {
               state.activeSince.remove(entry.getKey());
               state.dirty = true;
            } else {
               AbilityContext ctx = new AbilityContext(player, ability);
               float upkeep = ctx.stats.upkeep() / 20.0F;
               boolean paid = upkeep <= 0.0F || EnergyManager.tryConsume(player, upkeep);
               int ticks = (int)(serverTick() - entry.getValue());
               boolean gate = UniversalTechniques.isUniversal(ability) || techniqueUsable(player);
               if (!paid || !gate || player.hasEffect(ModEffects.STUNNED) || !ability.activeTick(ctx, ticks)) {
                  stop(player, ability, true);
                  if (!paid) {
                     checkExhaustion(player);
                  }
               }
            }
         }
      }

      if (state.charging != null) {
         Ability ability = byId(player, state.charging);
         if (ability != null && (UniversalTechniques.isUniversal(ability) || techniqueUsable(player)) && !player.hasEffect(ModEffects.STUNNED)) {
            ability.chargeTick(new AbilityContext(player, ability), (int)(serverTick() - state.chargeStart));
         } else {
            state.charging = null;
            state.dirty = true;
         }
      }

      if (state.dirty) {
         sync(player);
      }
   }

   public static void stop(ServerPlayer player, Ability ability, boolean cooldown) {
      AbilityState state = state(player);
      if (state.activeSince.remove(ability.id()) != null) {
         AbilityContext ctx = new AbilityContext(player, ability);
         ability.deactivate(ctx);
         if (cooldown) {
            startCooldown(state, ability, ctx.stats.cooldown());
         }

         state.dirty = true;
      }
   }

   public static void stopAll(ServerPlayer player) {
      AbilityState state = state(player);

      for (ResourceLocation id : new ArrayList<>(state.activeSince.keySet())) {
         Ability ability = byId(player, id);
         if (ability != null) {
            stop(player, ability, true);
         } else {
            state.activeSince.remove(id);
         }
      }

      state.charging = null;
      state.dirty = true;
   }

   public static void resetCooldowns(ServerPlayer player) {
      AbilityState state = state(player);
      state.cooldownUntil.clear();
      state.cooldownTotal.clear();
      state.dirty = true;
   }

   public static boolean isActive(Player player, Ability ability) {
      return state(player).activeSince.containsKey(ability.id());
   }

   public static void markDirty(Player player) {
      state(player).dirty = true;
   }

   public static void sync(ServerPlayer player) {
      AbilityState state = state(player);
      state.dirty = false;
      long now = serverTick();
      List<Ability> list = abilities(player);
      List<AbilitySyncPayload.Entry> entries = new ArrayList<>(list.size());
      int chargingIndex = -1;
      int chargeMax = 1;

      for (int i = 0; i < list.size(); i++) {
         Ability ability = list.get(i);
         Long until = state.cooldownUntil.get(ability.id());
         int remaining = until == null ? 0 : (int)Math.max(0L, until - now);
         AbilityStats stats = ability.stats();
         entries.add(
            new AbilitySyncPayload.Entry(
               remaining,
               state.cooldownTotal.getOrDefault(ability.id(), 1),
               state.activeSince.containsKey(ability.id()),
               EnergyManager.effectiveCost(player, stats.cost())
            )
         );
         if (ability.id().equals(state.charging)) {
            chargingIndex = i;
            chargeMax = Math.max(1, stats.chargeTicks());
         }
      }

      int elapsed = chargingIndex < 0 ? 0 : (int)(now - state.chargeStart);
      List<Integer> slots = List.of(state.slots[0], state.slots[1], state.slots[2], state.slots[3], state.slots[4]);
      PacketDistributor.sendToPlayer(player, new AbilitySyncPayload(entries, slots, chargingIndex, elapsed, chargeMax), new CustomPacketPayload[0]);
   }

   @SubscribeEvent
   public static void onTick(Post event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         tick(player);
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         stopAll(player);
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         stopAll(player);
      }
   }

   @SubscribeEvent
   public static void onDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         stopAll(player);
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      markDirty(event.getEntity());
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      markDirty(event.getEntity());
   }
}
