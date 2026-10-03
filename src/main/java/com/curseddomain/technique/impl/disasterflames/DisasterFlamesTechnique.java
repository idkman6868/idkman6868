package com.curseddomain.technique.impl.disasterflames;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.config.CombatConfig;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.entity.projectile.ProjectileBehavior;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.util.Scheduler;
import com.curseddomain.vfx.Vfx;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DisasterFlamesTechnique extends ImplementedTechnique {
   public static final int FIRE = -34278;
   public static final int MAGMA = -46576;

   public DisasterFlamesTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.EMBER_INSECT, new DisasterFlamesTechnique.EmberBehavior());
      ProjectileBehavior.register(ProjectileKind.METEOR, new DisasterFlamesTechnique.MeteorBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant(
            "ember_insects", AbilityStats.builder().cost(14.0F).cooldownSeconds(3.0F).damage(4.0F).range(0.9F).build(), DisasterFlamesTechnique::embers
         ),
         new DisasterFlamesTechnique.FlameBlast(),
         Ability.instant(
            "eruption",
            AbilityStats.builder().cost(30.0F).cooldownSeconds(8.0F).damage(20.0F).range(28.0F).radius(3.5F).build(),
            DisasterFlamesTechnique::eruption
         ),
         Ability.charge(
               "maximum_meteor",
               AbilityStats.builder().cost(150.0F).cooldownSeconds(60.0F).damage(45.0F).range(40.0F).radius(8.0F).charge(40).build(),
               DisasterFlamesTechnique::meteor
            )
            .maximum()
            .minGrade(Grade.GRADE_1)
      );
   }

   @Override
   protected DomainExpansion createDomain() {
      return new CoffinOfTheIronMountain();
   }

   private static boolean embers(AbilityContext ctx) {
      for (int i = 0; i < 6; i++) {
         Vec3 spread = new Vec3(ctx.level.random.nextGaussian() * 0.25, 0.25 + ctx.level.random.nextFloat() * 0.25, ctx.level.random.nextGaussian() * 0.25);
         TechniqueProjectile p = TechniqueProjectile.spawn(
            ctx.level,
            ctx.player,
            ProjectileKind.EMBER_INSECT,
            ctx.eye().add(ctx.look()),
            ctx.look().add(spread).normalize().scale(ctx.stats.range()),
            0.35F,
            ctx.scaledDamage()
         );
         p.maxAge = 80;
      }

      ctx.sound(SoundEvents.BEE_LOOP_AGGRESSIVE, 1.0F, 1.6F);
      return true;
   }

   private static boolean eruption(AbilityContext ctx) {
      Vec3 aim = ctx.targetPoint(ctx.stats.range());
      BlockPos top = ctx.level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(aim));
      Vec3 at = aim.y > top.getY() - 2 && aim.y < top.getY() + 3 ? aim : Vec3.atBottomCenterOf(top);
      float damage = ctx.scaledDamage();
      float radius = ctx.stats.radius();
      Vfx.ring(ctx.level, at, -46576, radius, 20);
      ctx.level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 20, radius * 0.5, 0.1, radius * 0.5, 0.0);
      ctx.sound(at, SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.2F, 0.4F);
      Scheduler.later(ctx.level, 18, () -> {
         for (LivingEntity e : AbilityContext.enemiesAround(ctx.player, at, radius)) {
            JjkDamage.hurt(e, JjkDamage.TECHNIQUE, ctx.player, damage);
            e.igniteForSeconds(6.0F);
            e.setDeltaMovement(e.getDeltaMovement().add(0.0, 1.1, 0.0));
            e.hurtMarked = true;
         }

         Vfx.pillar(ctx.level, at, -46576, 10.0F, 26);
         Vfx.burst(ctx.level, at.add(0.0, 1.0, 0.0), -34278, radius, 16);
         ctx.level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 1.0, at.z, 40, 0.8, 1.5, 0.8, 0.4);
         ctx.level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 2.0, at.z, 30, 1.0, 2.0, 1.0, 0.05);
         Vfx.shake(ctx.level, at, 3.0F, 24.0, 12);
         ctx.level.playSound(null, at.x, at.y, at.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.5F);
      });
      return true;
   }

   private static void meteor(AbilityContext ctx, float charge) {
      if (!(charge < 0.8F)) {
         Vec3 aim = ctx.targetPoint(ctx.stats.range());
         Vec3 start = aim.add(-ctx.look().x * 12.0, 34.0, -ctx.look().z * 12.0);
         Vec3 velocity = aim.subtract(start).normalize().scale(1.2);
         TechniqueProjectile p = TechniqueProjectile.spawn(ctx.level, ctx.player, ProjectileKind.METEOR, start, velocity, 3.2F, ctx.scaledDamage());
         p.radius = ctx.stats.radius();
         p.maxAge = 120;
         Vfx.ring(ctx.level, aim, -46576, ctx.stats.radius(), 40);
         ctx.sound(SoundEvents.ENDER_DRAGON_GROWL, 1.5F, 0.6F);
      }
   }

   static final class EmberBehavior implements ProjectileBehavior {
      @Override
      public void tick(TechniqueProjectile p) {
         if (p.age >= 6 && p.getOwner() instanceof LivingEntity owner) {
            LivingEntity var10 = null;
            double bestD = 400.0;

            for (LivingEntity e : AbilityContext.enemiesAround(owner, p.position(), 20.0)) {
               double d = e.distanceToSqr(p);
               if (d < bestD) {
                  bestD = d;
                  var10 = e;
               }
            }

            if (var10 != null) {
               Vec3 to = var10.position().add(0.0, var10.getBbHeight() / 2.0F, 0.0).subtract(p.position()).normalize();
               Vec3 v = p.getDeltaMovement();
               p.setDeltaMovement(v.scale(0.85).add(to.scale(0.18)).normalize().scale(Math.max(0.6, v.length())));
            }
         }
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         p.damageTarget(hit.getEntity(), p.damage);
         hit.getEntity().igniteForSeconds(4.0F);
         Vfx.burst((ServerLevel)p.level(), hit.getLocation(), -34278, 1.2F, 8);
         p.discard();
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         Vfx.burst((ServerLevel)p.level(), hit.getLocation(), -34278, 0.8F, 6);
         p.discard();
      }
   }

   static final class FlameBlast extends Ability {
      FlameBlast() {
         super("flame_blast", AbilityKind.CHANNEL, AbilityStats.builder().cost(6.0F).upkeep(12.0F).cooldownSeconds(3.0F).damage(2.5F).range(9.0F).build());
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         Vec3 eye = ctx.eye().subtract(0.0, 0.3, 0.0);
         Vec3 look = ctx.look();

         for (int i = 0; i < 6; i++) {
            Vec3 v = look.add(ctx.level.random.nextGaussian() * 0.12, ctx.level.random.nextGaussian() * 0.12, ctx.level.random.nextGaussian() * 0.12)
               .scale(0.7 + ctx.level.random.nextFloat() * 0.4);
            ctx.level.sendParticles(ParticleTypes.FLAME, eye.x + look.x, eye.y + look.y, eye.z + look.z, 0, v.x, v.y, v.z, 1.0);
         }

         if (ticks % 2 == 0) {
            Vfx.particles(ctx.level, eye.add(look.scale(2.0)), -34278, 1.2F, 3, 0.4, 0.05);
         }

         if (ticks % 5 == 0) {
            for (LivingEntity e : ctx.enemiesAround(eye, ctx.stats.range())) {
               Vec3 to = e.getBoundingBox().getCenter().subtract(eye).normalize();
               if (to.dot(look) > 0.8) {
                  ctx.damage(e, ctx.scaledDamage(), JjkDamage.TECHNIQUE, true);
                  e.igniteForSeconds(3.0F);
               }
            }

            if (ticks % 20 == 0) {
               ctx.sound(SoundEvents.FIRECHARGE_USE, 0.8F, 0.7F);
            }
         }

         return true;
      }
   }

   static final class MeteorBehavior implements ProjectileBehavior {
      private static void impact(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         Vec3 at = p.position();
         if (p.getOwner() instanceof LivingEntity owner) {
            for (LivingEntity e : AbilityContext.enemiesAround(owner, at, p.radius)) {
               JjkDamage.hurt(e, JjkDamage.TECHNIQUE, owner, p, p.damage, false);
               e.igniteForSeconds(10.0F);
               Moves.knockback(e, at, 2.0, 0.8);
            }
         }

         Moves.breakSphere(level, at, p.radius * 0.55, 6.0F, 400);
         if ((Boolean)CombatConfig.TECHNIQUES_BREAK_BLOCKS.get()) {
            BlockPos c = BlockPos.containing(at);

            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-6, -3, -6), c.offset(6, 3, 6))) {
               if (level.random.nextFloat() < 0.08F && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid()) {
                  level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
               }
            }
         }

         Vfx.burst(level, at, -34278, p.radius * 1.4F, 22);
         Vfx.ring(level, at, -46576, p.radius * 2.0F, 20);
         Vfx.pillar(level, at, -46576, 16.0F, 30);
         Vfx.shake(level, at, 6.0F, 60.0, 24);
         level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y, at.z, 3, 2.0, 1.0, 2.0, 0.0);
         level.playSound(null, at.x, at.y, at.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 4.0F, 0.4F);
         p.discard();
      }

      @Override
      public void tick(TechniqueProjectile p) {
         if (p.age % 2 == 0) {
            ((ServerLevel)p.level()).sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 2.0, p.getZ(), 4, 1.5, 1.5, 1.5, 0.02);
         }
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         impact(p);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         impact(p);
      }

      @Override
      public void expire(TechniqueProjectile p) {
         impact(p);
      }
   }
}
