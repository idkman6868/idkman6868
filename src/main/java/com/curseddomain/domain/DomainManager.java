package com.curseddomain.domain;

import com.curseddomain.combat.CombatRecord;
import com.curseddomain.config.CombatConfig;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityManager;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.Vfx;
import com.curseddomain.vfx.VfxKind;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent.Detonate;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class DomainManager {
   private static final Map<UUID, DomainInstance> ACTIVE = new LinkedHashMap<>();
   private static final List<DomainClash> CLASHES = new ArrayList<>();

   private DomainManager() {
   }

   public static Optional<DomainInstance> of(Entity owner) {
      return Optional.ofNullable(ACTIVE.get(owner.getUUID()));
   }

   public static List<DomainInstance> all() {
      return List.copyOf(ACTIVE.values());
   }

   public static List<DomainInstance> containing(Entity entity) {
      List<DomainInstance> list = new ArrayList<>();

      for (DomainInstance d : ACTIVE.values()) {
         if (d.level == entity.level() && d.contains(entity)) {
            list.add(d);
         }
      }

      return list;
   }

   public static void input(ServerPlayer player) {
      DomainInstance own = ACTIVE.get(player.getUUID());
      if (own != null) {
         if (own.clash != null) {
            own.clashPresses++;
         } else if (own.live()) {
            end(own, "dismissed");
         }
      } else {
         tryCast(player);
      }
   }

   public static boolean tryCast(ServerPlayer player) {
      Technique technique = SorcererManager.get(player).technique().orElse(null);
      DomainExpansion domain = technique == null ? null : technique.domain().orElse(null);
      String failure = null;
      DomainType type = null;
      if (!AbilityManager.techniqueUsable(player)) {
         failure = "locked";
      } else if (domain == null) {
         failure = "no_domain";
      } else if (player.hasEffect(ModEffects.BURNOUT)) {
         failure = "burnout";
      } else if (player.hasEffect(ModEffects.DOMAIN_AMPLIFICATION)) {
         failure = "amplified";
      } else if (!player.hasEffect(ModEffects.EXHAUSTED) && !player.hasEffect(ModEffects.STUNNED)) {
         type = allowedType(player, domain);
         if (type == null) {
            failure = "not_learned";
         } else {
            float cost = EnergyManager.effectiveCost(player, costFor(domain, type));
            if (cost > EnergyManager.get(player).current()) {
               failure = "energy";
            }
         }
      } else {
         failure = "exhausted";
      }

      if (failure != null) {
         player.displayClientMessage(Component.translatable("domain.cursed_domain.fail." + failure).withStyle(ChatFormatting.RED), true);
         return false;
      } else {
         cast(player, domain, type, player.position());
         return true;
      }
   }

   @Nullable
   private static DomainType allowedType(ServerPlayer player, DomainExpansion domain) {
      SorcererData data = SorcererManager.get(player);
      if (!(Boolean)CombatConfig.DOMAIN_REQUIRES_UNLOCK.get() || data.isUnlocked("domain")) {
         return domain.nativeType();
      } else {
         return data.isUnlocked("domain_incomplete") ? DomainType.INCOMPLETE : null;
      }
   }

   private static float costFor(DomainExpansion domain, DomainType type) {
      float cost = domain.stats().cost();
      return type == DomainType.INCOMPLETE ? cost * 0.5F : cost;
   }

   public static DomainInstance cast(LivingEntity owner, DomainExpansion domain, DomainType type, Vec3 center) {
      DomainInstance existing = ACTIVE.remove(owner.getUUID());
      if (existing != null) {
         discard(existing);
      }

      AbilityStats stats = domain.stats();
      float radius = type == DomainType.INCOMPLETE ? stats.radius() * 0.6F : stats.radius();
      DomainInstance instance = new DomainInstance(owner, domain, type, center, radius);
      instance.barrierHealth = ((Double)CombatConfig.DOMAIN_BARRIER_HEALTH.get()).floatValue();
      ACTIVE.put(owner.getUUID(), instance);
      ServerLevel level = instance.level;
      level.playSound(null, owner.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.5F, 0.6F);
      if (owner instanceof ServerPlayer player) {
         player.displayClientMessage(
            Component.translatable("domain.cursed_domain.casting", new Object[]{domain.displayName()})
               .withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD}),
            true
         );
      }

      return instance;
   }

   @SubscribeEvent
   public static void onServerTick(Post event) {
      if (!ACTIVE.isEmpty()) {
         for (DomainInstance d : new ArrayList<>(ACTIVE.values())) {
            tick(d);
         }

         for (DomainClash clash : new ArrayList<>(CLASHES)) {
            tickClash(clash);
         }
      }
   }

   private static void tick(DomainInstance d) {
      d.phaseTicks++;
      if (d.owner.isAlive() && !d.owner.isRemoved() && d.owner.level() == d.level) {
         switch (d.phase) {
            case CASTING:
               tickCasting(d);
               break;
            case FORMING:
               tickForming(d);
               break;
            case ACTIVE:
               tickActive(d);
               break;
            case COLLAPSING:
               if (d.phaseTicks >= 24) {
                  discard(d);
               }
         }
      } else {
         end(d, "owner_gone");
      }
   }

   private static void tickCasting(DomainInstance d) {
      d.owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 3, false, false));
      if (d.phaseTicks % 4 == 1) {
         Vfx.send(d.level, VfxKind.HAND_SIGN, d.owner.position().add(0.0, 1.2, 0.0), Vec3.ZERO, d.domain.style().glow(), 0.9F, 6);
      }

      if (d.castDamageTaken > 6.0F) {
         if (d.owner instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("domain.cursed_domain.interrupted").withStyle(ChatFormatting.RED), true);
         }

         discard(d);
      } else if (d.phaseTicks >= (Integer)CombatConfig.DOMAIN_CAST_TICKS.get()) {
         if (d.owner instanceof ServerPlayer player && !EnergyManager.tryConsume(player, costFor(d.domain, d.type))) {
            player.displayClientMessage(Component.translatable("domain.cursed_domain.fail.energy").withStyle(ChatFormatting.RED), true);
            discard(d);
         } else {
            if (d.owner instanceof ServerPlayer player) {
               AbilityManager.stopAll(player);
            }

            DomainBarrierEntity barrier = new DomainBarrierEntity((EntityType<? extends DomainBarrierEntity>)ModEntities.DOMAIN_BARRIER.get(), d.level);
            barrier.setPos(d.center);
            barrier.setup(d.radius, d.domain.style(), d.type, d.owner.getId());
            d.level.addFreshEntity(barrier);
            d.barrier = barrier;
            d.setPhase(DomainInstance.Phase.FORMING);
            announce(d);
            d.level.playSound(null, d.owner.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 2.0F, 0.5F);
            Vfx.darkBurst(d.level, d.center.add(0.0, 1.0, 0.0), d.domain.style().shell(), d.radius, 24);
         }
      }
   }

   private static void announce(DomainInstance d) {
      Component line = Component.translatable("domain.cursed_domain.expansion", new Object[]{d.owner.getDisplayName(), d.domain.displayName()})
         .withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD});

      for (ServerPlayer player : d.level.players()) {
         if (player.distanceToSqr(d.center) < (d.radius + 48.0F) * (d.radius + 48.0F)) {
            player.displayClientMessage(line, false);
         }
      }
   }

   private static void tickForming(DomainInstance d) {
      if (d.phaseTicks >= 24) {
         if (d.hasBarrier()) {
            AABB box = new AABB(d.center, d.center).inflate(d.radius);

            for (LivingEntity e : d.level.getEntitiesOfClass(LivingEntity.class, box, d::contains)) {
               if (!(e instanceof Player p && SorcererManager.get(p).hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL) && e != d.owner)) {
                  d.trapped.add(e.getUUID());
               }
            }
         }

         d.setPhase(DomainInstance.Phase.ACTIVE);
         if (d.owner instanceof ServerPlayer player) {
            ((CombatRecord)player.getData(ModAttachments.COMBAT_RECORD)).domainsExpanded++;
         }

         d.domain.onOpen(d);
         startClashIfOverlapping(d);
      }
   }

   private static void tickActive(DomainInstance d) {
      d.activeTicks++;
      int max = d.domain.stats().duration() > 0
         ? Math.min(d.domain.stats().duration(), (Integer)CombatConfig.DOMAIN_MAX_SECONDS.get() * 20)
         : (Integer)CombatConfig.DOMAIN_MAX_SECONDS.get() * 20;
      if (d.activeTicks > max) {
         end(d, "expired");
      } else {
         if (d.owner instanceof ServerPlayer player && d.activeTicks % 20 == 0) {
            float upkeep = d.domain.stats().upkeep() * (d.type == DomainType.INCOMPLETE ? 0.5F : 1.0F);
            if (upkeep > 0.0F && !EnergyManager.tryConsume(player, upkeep)) {
               end(d, "energy");
               return;
            }
         }

         if (d.hasBarrier()) {
            enforceBarrier(d);
            if (d.barrierHealth <= 0.0F) {
               Vfx.darkBurst(d.level, d.center, d.domain.style().glow(), d.radius * 1.1F, 20);
               d.level.playSound(null, d.owner.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 3.0F, 0.4F);
               end(d, "shattered");
               return;
            }
         }

         d.domain.onTick(d);
         if (d.clash == null && d.type != DomainType.INCOMPLETE && d.activeTicks % (Integer)CombatConfig.DOMAIN_SURE_HIT_INTERVAL.get() == 0) {
            for (LivingEntity target : sureHitTargets(d)) {
               if (!DomainCounters.protectedFrom(target, d)) {
                  d.domain.sureHit(d, target);
               }
            }
         }
      }
   }

   public static List<LivingEntity> sureHitTargets(DomainInstance d) {
      AABB box = new AABB(d.center, d.center).inflate(d.radius);
      return d.level
         .getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && d.contains(e) && !AbilityContext.isAlly(d.owner, e) && !e.isSpectator() && (!d.hasBarrier() || d.trapped.contains(e.getUUID()))
         );
   }

   private static void enforceBarrier(DomainInstance d) {
      double r = d.radius;
      AABB box = new AABB(d.center, d.center).inflate(r + 4.0);

      for (Entity e : d.level.getEntitiesOfClass(Entity.class, box, ex -> !(ex instanceof DomainBarrierEntity))) {
         Vec3 offset = e.position().subtract(d.center);
         double dist = offset.length();
         Vec3 dir = dist < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : offset.scale(1.0 / dist);
         boolean trapped = d.trapped.contains(e.getUUID());
         if (e instanceof Projectile projectile) {
            Vec3 motion = projectile.getDeltaMovement();
            boolean inward = motion.dot(dir) < 0.0;
            boolean fromOutside = projectile.getOwner() == null || !d.trapped.contains(projectile.getOwner().getUUID());
            if (fromOutside && inward && dist < r + 0.8 && dist > r - 1.5) {
               d.barrierHealth = d.barrierHealth - (4.0F + (float)motion.length() * 2.0F);
               Vfx.burst(d.level, e.position(), d.domain.style().glow(), 0.8F, 6);
               projectile.discard();
            }
         } else if (e instanceof LivingEntity) {
            if (trapped && dist > r - 0.8) {
               Vec3 back = d.center.add(dir.scale(r - 1.5));
               push(e, back, dir.scale(-0.4));
            } else if (!trapped && dist < r && !(e instanceof Player p && SorcererManager.get(p).hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL))) {
               Vec3 out = d.center.add(dir.scale(r + 1.0));
               push(e, out, dir.scale(0.4));
            }
         }
      }
   }

   private static void push(Entity e, Vec3 to, Vec3 velocity) {
      if (e instanceof ServerPlayer player) {
         player.teleportTo(to.x, Math.max(to.y, player.getY()), to.z);
      } else {
         e.setPos(to.x, Math.max(to.y, e.getY()), to.z);
      }

      e.setDeltaMovement(velocity);
      e.hurtMarked = true;
   }

   private static void startClashIfOverlapping(DomainInstance d) {
      if (d.type != DomainType.INCOMPLETE) {
         for (DomainInstance other : ACTIVE.values()) {
            if (other != d
               && other.level == d.level
               && other.clash == null
               && other.live()
               && other.type != DomainType.INCOMPLETE
               && other.center.distanceTo(d.center) < other.radius + d.radius) {
               DomainClash clash = new DomainClash(d, other, (Integer)CombatConfig.CLASH_SECONDS.get() * 20);
               d.clash = clash;
               other.clash = clash;
               CLASHES.add(clash);
               setClashing(d, true);
               setClashing(other, true);
               Vfx.darkBurst(d.level, d.center.add(other.center).scale(0.5), -1, 6.0F, 20);

               for (DomainInstance side : List.of(d, other)) {
                  if (side.owner instanceof ServerPlayer player) {
                     player.displayClientMessage(
                        Component.translatable("domain.cursed_domain.clash").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}), true
                     );
                  }
               }

               return;
            }
         }
      }
   }

   private static void setClashing(DomainInstance d, boolean clashing) {
      if (d.barrier != null) {
         d.barrier.setClashing(clashing);
      }
   }

   private static void tickClash(DomainClash clash) {
      clash.ticks++;

      for (DomainInstance side : List.of(clash.a, clash.b)) {
         if (!(side.owner instanceof Player) && side.owner.getRandom().nextInt(6) == 0) {
            side.clashPresses++;
         }

         if (side.owner instanceof ServerPlayer player && clash.ticks % 4 == 0) {
            DomainPayloads.sendClash(player, clash.share(side), clash.duration - clash.ticks);
         }
      }

      if (clash.ticks % 6 == 0) {
         Vec3 mid = clash.a.center.add(clash.b.center).scale(0.5).add(0.0, 2.0, 0.0);
         Vfx.bolt(clash.a.level, clash.a.center.add(0.0, 2.0, 0.0), mid, clash.a.domain.style().glow(), 0.2F, 6);
         Vfx.bolt(clash.a.level, clash.b.center.add(0.0, 2.0, 0.0), mid, clash.b.domain.style().glow(), 0.2F, 6);
      }

      boolean over = clash.ticks >= clash.duration || !ACTIVE.containsValue(clash.a) || !ACTIVE.containsValue(clash.b);
      if (over) {
         CLASHES.remove(clash);
         DomainInstance loser = clash.loser();
         DomainInstance winner = clash.other(loser);
         clash.a.clash = null;
         clash.b.clash = null;
         setClashing(clash.a, false);
         setClashing(clash.b, false);
         if (ACTIVE.containsValue(winner) && winner.owner instanceof ServerPlayer player) {
            DomainPayloads.sendClash(player, -1.0F, 0);
            player.displayClientMessage(Component.translatable("domain.cursed_domain.clash_won").withStyle(ChatFormatting.GOLD), true);
            ((CombatRecord)player.getData(ModAttachments.COMBAT_RECORD)).clashesWon++;
         }

         if (ACTIVE.containsValue(loser)) {
            if (loser.owner instanceof ServerPlayer player) {
               DomainPayloads.sendClash(player, -1.0F, 0);
            }

            end(loser, "clash_lost");
         }
      }
   }

   public static void end(DomainInstance d, String reason) {
      if (d.phase != DomainInstance.Phase.COLLAPSING) {
         d.endReason = reason;
         if (d.phase == DomainInstance.Phase.CASTING) {
            discard(d);
         } else {
            d.domain.onClose(d);
            d.setPhase(DomainInstance.Phase.COLLAPSING);
            d.level.playSound(null, d.owner.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 0.5F);
            int burnout = (Integer)CombatConfig.BURNOUT_SECONDS.get() * 20;
            if (burnout > 0) {
               d.owner.addEffect(new MobEffectInstance(ModEffects.BURNOUT, burnout, 0, false, false, true));
            }

            if (d.owner instanceof ServerPlayer player) {
               player.displayClientMessage(Component.translatable("domain.cursed_domain.ended." + reason).withStyle(ChatFormatting.GRAY), true);
            }
         }
      }
   }

   private static void discard(DomainInstance d) {
      ACTIVE.remove(d.owner.getUUID(), d);
      if (d.clash != null) {
         CLASHES.remove(d.clash);
         d.clash.other(d).clash = null;
         setClashing(d.clash.other(d), false);
      }

      if (d.barrier != null) {
         d.barrier.discard();
      }
   }

   public static float costMultiplier(Player player) {
      DomainInstance d = ACTIVE.get(player.getUUID());
      return d != null && d.live() && d.contains(player) ? 0.6F : 1.0F;
   }

   public static float damageMultiplier(Player player) {
      DomainInstance d = ACTIVE.get(player.getUUID());
      return d != null && d.live() && d.contains(player) ? 1.25F : 1.0F;
   }

   @SubscribeEvent
   public static void onDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
      DomainInstance d = ACTIVE.get(event.getEntity().getUUID());
      if (d != null && d.phase == DomainInstance.Phase.CASTING) {
         d.castDamageTaken = d.castDamageTaken + event.getNewDamage();
      }
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      DomainInstance d = ACTIVE.get(event.getEntity().getUUID());
      if (d != null) {
         end(d, "owner_gone");
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      DomainInstance d = ACTIVE.get(event.getEntity().getUUID());
      if (d != null) {
         discard(d);
      }
   }

   @SubscribeEvent
   public static void onExplosion(Detonate event) {
      Vec3 at = event.getExplosion().center();

      for (DomainInstance d : ACTIVE.values()) {
         if (d.hasBarrier() && d.live() && d.level == event.getLevel()) {
            double dist = at.distanceTo(d.center);
            if (dist > d.radius - 1.0F && dist < d.radius + 4.0F) {
               d.barrierHealth = d.barrierHealth - event.getExplosion().radius() * 8.0F;
            }
         }
      }
   }

   @SubscribeEvent
   public static void onStopping(ServerStoppingEvent event) {
      ACTIVE.clear();
      CLASHES.clear();
   }
}
