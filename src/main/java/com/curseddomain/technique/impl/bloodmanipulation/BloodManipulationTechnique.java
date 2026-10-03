package com.curseddomain.technique.impl.bloodmanipulation;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.entity.projectile.ProjectileBehavior;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.util.Scheduler;
import com.curseddomain.vfx.EnergyParticleOptions;
import com.curseddomain.vfx.Vfx;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BloodManipulationTechnique extends ImplementedTechnique {
   public static final int BLOOD = -4190182;

   public BloodManipulationTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.BLOOD_DISC, new BloodManipulationTechnique.DiscBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.charge(
               "piercing_blood",
               AbilityStats.builder().cost(20.0F).cooldownSeconds(5.0F).damage(22.0F).range(32.0F).charge(30).build(),
               BloodManipulationTechnique::piercingBlood
            )
            .onCharge(BloodManipulationTechnique::convergence),
         Ability.instant(
            "slicing_exorcism", AbilityStats.builder().cost(12.0F).cooldown(40).damage(6.0F).range(1.3F).build(), BloodManipulationTechnique::slicingExorcism
         ),
         new BloodManipulationTechnique.FlowingRedScale(),
         Ability.instant(
            "crimson_binding",
            AbilityStats.builder().cost(15.0F).cooldownSeconds(8.0F).range(16.0F).durationSeconds(2.5F).build(),
            BloodManipulationTechnique::crimsonBinding
         ),
         Ability.instant(
               "supernova",
               AbilityStats.builder().cost(60.0F).cooldownSeconds(30.0F).damage(12.0F).radius(10.0F).build(),
               BloodManipulationTechnique::supernova
            )
            .maximum()
      );
   }

   static boolean bleed(ServerPlayer player, float hp) {
      if (SorcererManager.get(player).hasTrait(InnateTrait.DEATH_PAINTING)) {
         return true;
      } else if (player.getHealth() <= hp + 1.0F) {
         player.displayClientMessage(Component.translatable("ability.cursed_domain.blood.too_weak").withStyle(ChatFormatting.RED), true);
         return false;
      } else {
         player.setHealth(player.getHealth() - hp);
         return true;
      }
   }

   private static void convergence(AbilityContext ctx, int ticks) {
      if (ticks % 2 == 0) {
         Vec3 hands = ctx.eye().add(ctx.look().scale(0.8)).subtract(0.0, 0.3, 0.0);
         ctx.level.sendParticles(new EnergyParticleOptions(-4190182, 0.9F, 0.0F), hands.x, hands.y, hands.z, 3, 0.25, 0.25, 0.25, 0.0);
      }
   }

   private static void piercingBlood(AbilityContext ctx, float charge) {
      if (bleed(ctx.player, 2.0F)) {
         Vec3 from = ctx.eye().add(ctx.look().scale(0.6)).subtract(0.0, 0.2, 0.0);
         Vec3 dir = ctx.look();
         double range = ctx.stats.range() * (0.5 + 0.5 * charge);
         Vec3 to = from.add(dir.scale(range));
         BlockHitResult clip = ctx.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, ctx.player));
         to = clip.getLocation();
         float damage = ctx.scaledDamage() * (0.35F + 0.65F * charge);
         Set<LivingEntity> hit = new HashSet<>();

         for (double d = 0.0; d < from.distanceTo(to); d += 0.5) {
            Vec3 p = from.add(dir.scale(d));

            for (LivingEntity e : ctx.enemiesAround(p, 0.9)) {
               if (hit.add(e)) {
                  ctx.damage(e, damage, JjkDamage.TECHNIQUE, true);
               }
            }
         }

         Vfx.beam(ctx.level, from, to, -4190182, 0.18F + charge * 0.15F, 10);
         Vfx.burst(ctx.level, to, -4190182, 1.0F, 8);
         ctx.sound((SoundEvent)SoundEvents.TRIDENT_RIPTIDE_1.value(), 1.0F, 1.4F);
      }
   }

   private static boolean slicingExorcism(AbilityContext ctx) {
      if (!bleed(ctx.player, 1.0F)) {
         return false;
      } else {
         Vec3 dir = ctx.look();
         Vec3 right = new Vec3(-dir.z, 0.0, dir.x).normalize();

         for (int i = -1; i <= 1; i += 2) {
            TechniqueProjectile disc = TechniqueProjectile.spawn(
               ctx.level,
               ctx.player,
               ProjectileKind.BLOOD_DISC,
               ctx.eye().add(right.scale(i * 0.8)).subtract(0.0, 0.3, 0.0),
               dir.add(right.scale(i * 0.12)).normalize().scale(ctx.stats.range()),
               0.7F,
               ctx.scaledDamage()
            );
            disc.pierce = true;
            disc.maxAge = 30;
         }

         ctx.sound((SoundEvent)SoundEvents.TRIDENT_THROW.value(), 1.0F, 1.3F);
         return true;
      }
   }

   private static boolean crimsonBinding(AbilityContext ctx) {
      LivingEntity target = ctx.target(ctx.stats.range());
      if (target != null && bleed(ctx.player, 1.0F)) {
         target.addEffect(new MobEffectInstance(ModEffects.STUNNED, ctx.stats.duration(), 0, false, true));
         target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ctx.stats.duration() + 40, 2, false, true));
         if (target instanceof Mob mob) {
            mob.getNavigation().stop();
         }

         Vec3 c = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0);

         for (int i = 0; i < 3; i++) {
            Vfx.ring(ctx.level, c.add(0.0, (i - 1) * 0.5, 0.0), -4190182, target.getBbWidth() + 0.3F, ctx.stats.duration());
         }

         Vfx.beam(ctx.level, ctx.eye().add(ctx.look().scale(0.8)).subtract(0.0, 0.4, 0.0), c, -4190182, 0.08F, 10);
         ctx.sound(SoundEvents.CHAIN_PLACE, 1.0F, 0.6F);
         return true;
      } else {
         return false;
      }
   }

   private static boolean supernova(AbilityContext ctx) {
      if (!bleed(ctx.player, 4.0F)) {
         return false;
      } else {
         float damage = ctx.scaledDamage();
         float radius = ctx.stats.radius();
         Vec3 center = ctx.player.position().add(0.0, 1.0, 0.0);
         ctx.sound(SoundEvents.BEACON_POWER_SELECT, 1.5F, 0.5F);
         Scheduler.repeat(ctx.level, 8, 3, 8, ix -> {
            double ax = ix * Math.PI / 4.0;
            Vec3 at = center.add(Math.cos(ax) * radius * 0.5, ix % 2 * 1.2, Math.sin(ax) * radius * 0.5);

            for (LivingEntity e : AbilityContext.enemiesAround(ctx.player, at, radius * 0.45)) {
               JjkDamage.hurt(e, JjkDamage.TECHNIQUE, ctx.player, null, damage, true);
            }

            Vfx.burst(ctx.level, at, -4190182, radius * 0.4F, 10);
            Vfx.sparks(ctx.level, at, -40864, 2.0F, 14);
            ctx.level.playSound(null, at.x, at.y, at.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 1.4F);
         });

         for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            Vfx.burst(ctx.level, center.add(Math.cos(a) * radius * 0.5, i % 2 * 1.2, Math.sin(a) * radius * 0.5), -4190182, 0.5F, 10);
         }

         return true;
      }
   }

   static final class DiscBehavior implements ProjectileBehavior {
      @Override
      public void tick(TechniqueProjectile p) {
         if (p.getOwner() instanceof LivingEntity owner) {
            LivingEntity var10 = null;
            double bestD = 36.0;

            for (LivingEntity e : AbilityContext.enemiesAround(owner, p.position(), 6.0)) {
               double d = e.distanceToSqr(p);
               if (d < bestD && !p.alreadyHit.contains(e.getUUID())) {
                  bestD = d;
                  var10 = e;
               }
            }

            if (var10 != null) {
               Vec3 to = var10.position().add(0.0, var10.getBbHeight() / 2.0F, 0.0).subtract(p.position()).normalize();
               Vec3 v = p.getDeltaMovement();
               p.setDeltaMovement(v.add(to.scale(v.length() * 0.18)).normalize().scale(v.length()));
            }
         }
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         p.damageTarget(hit.getEntity(), p.damage);
         Vfx.particles((ServerLevel)p.level(), hit.getLocation(), -4190182, 0.8F, 6, 0.2, 0.05);
      }
   }

   static final class FlowingRedScale extends Ability {
      FlowingRedScale() {
         super("flowing_red_scale", AbilityKind.TOGGLE, AbilityStats.builder().cost(10.0F).upkeep(3.0F).cooldownSeconds(4.0F).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         ctx.sound(SoundEvents.HONEY_DRINK, 1.0F, 0.6F);
         Vfx.burst(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -4190182, 1.4F, 10);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         ctx.player.addEffect(new MobEffectInstance(ModEffects.FLOWING_RED_SCALE, 25, 0, false, false, true));
         if (ticks % 40 == 39 && !BloodManipulationTechnique.bleed(ctx.player, 1.0F)) {
            return false;
         } else {
            if (ticks % 5 == 0) {
               Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -4190182, 0.7F, 2, 0.3, 0.01);
            }

            return true;
         }
      }

      @Override
      public void deactivate(AbilityContext ctx) {
         ctx.player.removeEffect(ModEffects.FLOWING_RED_SCALE);
      }
   }
}
