package com.curseddomain.technique.impl.limitless;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
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
import com.curseddomain.vfx.Vfx;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class LimitlessTechnique extends ImplementedTechnique {
   public static final int BLUE = -13739009;
   public static final int RED = -54742;
   public static final int PURPLE = -6274817;

   public LimitlessTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.BLUE, new LimitlessTechnique.BlueBehavior());
      ProjectileBehavior.register(ProjectileKind.RED, new LimitlessTechnique.RedBehavior());
      ProjectileBehavior.register(ProjectileKind.HOLLOW_PURPLE, new LimitlessTechnique.PurpleBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         new LimitlessTechnique.InfinityAbility(),
         Ability.instant(
            "lapse_blue",
            AbilityStats.builder().cost(18.0F).cooldownSeconds(6.0F).damage(2.5F).range(24.0F).radius(7.0F).durationSeconds(3.0F).build(),
            ctx -> blue(ctx, false)
         ),
         Ability.charge(
               "reversal_red",
               AbilityStats.builder().cost(30.0F).cooldownSeconds(8.0F).damage(16.0F).range(2.4F).radius(6.0F).charge(30).build(),
               LimitlessTechnique::red
            )
            .onCharge((ctx, t) -> {
               if (t % 4 == 0) {
                  Vfx.particles(ctx.level, ctx.eye().add(ctx.look()), -54742, 1.0F, 3, 0.15, 0.02);
               }
            })
            .requires("rct"),
         Ability.instant("blue_step", AbilityStats.builder().cost(8.0F).cooldownSeconds(2.0F).range(14.0F).build(), LimitlessTechnique::blueStep),
         Ability.instant(
               "maximum_blue",
               AbilityStats.builder().cost(70.0F).cooldownSeconds(40.0F).damage(4.0F).range(30.0F).radius(12.0F).durationSeconds(6.0F).build(),
               ctx -> blue(ctx, true)
            )
            .maximum(),
         Ability.charge(
               "hollow_purple",
               AbilityStats.builder().cost(140.0F).cooldownSeconds(60.0F).damage(48.0F).range(1.4F).radius(2.6F).charge(50).durationSeconds(4.0F).build(),
               LimitlessTechnique::hollowPurple
            )
            .onCharge(LimitlessTechnique::purpleWindup)
            .requires("rct")
            .maximum()
            .minGrade(Grade.GRADE_1)
      );
   }

   @Override
   protected DomainExpansion createDomain() {
      return new UnlimitedVoid();
   }

   private static boolean blue(AbilityContext ctx, boolean maximum) {
      Vec3 at = ctx.targetPoint(ctx.stats.range());
      TechniqueProjectile orb = TechniqueProjectile.spawn(ctx.level, ctx.player, ProjectileKind.BLUE, at, Vec3.ZERO, maximum ? 2.4F : 1.2F, ctx.scaledDamage());
      orb.radius = ctx.stats.radius();
      orb.maxAge = ctx.stats.duration();
      orb.anchor = at;
      orb.setNoGravity(true);
      ctx.sound(at, SoundEvents.BEACON_ACTIVATE, 1.5F, 1.8F);
      Vfx.implode(ctx.level, at, -13739009, ctx.stats.radius(), 14);
      return true;
   }

   private static void red(AbilityContext ctx, float charge) {
      Vec3 eye = ctx.eye();
      Vec3 dir = ctx.look();
      TechniqueProjectile orb = TechniqueProjectile.spawn(
         ctx.level,
         ctx.player,
         ProjectileKind.RED,
         eye.add(dir.scale(1.2)),
         dir.scale(ctx.stats.range()),
         0.6F + charge * 0.5F,
         ctx.scaledDamage() * (0.5F + 0.5F * charge)
      );
      orb.radius = ctx.stats.radius() * (0.6F + 0.4F * charge);
      orb.maxAge = 40;
      orb.setPower(charge);
      ctx.sound(SoundEvents.FIRECHARGE_USE, 1.4F, 0.6F);
      Vfx.burst(ctx.level, eye.add(dir), -54742, 1.2F, 8);
   }

   private static boolean blueStep(AbilityContext ctx) {
      Vec3 from = ctx.player.position();
      Vec3 to = Moves.dashTarget(ctx.level, ctx.player, from, ctx.look(), ctx.stats.range());
      if (to.distanceTo(from) < 1.5) {
         return false;
      } else {
         Vfx.implode(ctx.level, from.add(0.0, 1.0, 0.0), -13739009, 1.5F, 8);
         Moves.teleport(ctx.player, to);
         Vfx.burst(ctx.level, to.add(0.0, 1.0, 0.0), -13739009, 1.5F, 8);
         ctx.sound(SoundEvents.ENDERMAN_TELEPORT, 0.8F, 1.6F);
         return true;
      }
   }

   private static void hollowPurple(AbilityContext ctx, float charge) {
      if (charge < 0.6F) {
         Vfx.burst(ctx.level, ctx.eye().add(ctx.look()), -6274817, 1.0F, 8);
      } else {
         Vec3 dir = ctx.look();
         TechniqueProjectile orb = TechniqueProjectile.spawn(
            ctx.level,
            ctx.player,
            ProjectileKind.HOLLOW_PURPLE,
            ctx.eye().add(dir.scale(4.5)),
            dir.scale(ctx.stats.range()),
            ctx.stats.radius(),
            ctx.scaledDamage() * charge
         );
         orb.pierce = true;
         orb.radius = ctx.stats.radius();
         orb.maxAge = ctx.stats.duration();
         orb.setPower(charge);
         ctx.sound(SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 0.6F);
         Vfx.shake(ctx.level, ctx.player.position(), 4.0F, 40.0, 20);
      }
   }

   public static void purpleWindup(AbilityContext ctx, int ticks) {
      if (ticks % 3 == 0) {
         float t = Math.min(1.0F, (float)ticks / ctx.stats.chargeTicks());
         Vec3 eye = ctx.eye();
         Vec3 dir = ctx.look();
         Vec3 side = new Vec3(-dir.z, 0.0, dir.x).normalize().scale(2.2 * (1.0F - t) + 0.25);
         Vec3 front = eye.add(dir.scale(3.5)).subtract(0.0, 0.3, 0.0);
         Vfx.burst(ctx.level, front.add(side), -13739009, 0.35F, 4);
         Vfx.burst(ctx.level, front.subtract(side), -54742, 0.35F, 4);
         if (t >= 1.0F) {
            Vfx.burst(ctx.level, front, -6274817, 0.6F, 4);
         }
      }
   }

   static final class BlueBehavior implements ProjectileBehavior {
      @Override
      public void tick(TechniqueProjectile p) {
         p.setDeltaMovement(Vec3.ZERO);
         ServerLevel level = (ServerLevel)p.level();
         Vec3 center = p.position();
         AABB box = p.getBoundingBox().inflate(p.radius);
         Entity owner = p.getOwner();

         for (Entity e : level.getEntitiesOfClass(Entity.class, box, ex -> ex != owner && !(ex instanceof TechniqueProjectile))) {
            if (!(e.distanceToSqr(center) > p.radius * p.radius) && (owner == null || !AbilityContext.isAlly(owner, e))) {
               Moves.pull(e, center, 0.55);
               if (e instanceof LivingEntity living && p.age % 10 == 0) {
                  JjkDamage.hurt(living, JjkDamage.TECHNIQUE, owner, p, p.damage, true);
               } else if (e instanceof ItemEntity item) {
                  item.setPickUpDelay(10);
               }
            }
         }

         if (p.age % 8 == 0) {
            Vfx.implode(level, center, -13739009, p.radius * 0.6F, 10);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.6F, 1.8F);
         }

         if (p.size() > 2.0F && p.age % 4 == 0) {
            Moves.breakSphere(level, center, 2.2, 3.0F, 12);
         }
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
      }

      @Override
      public void expire(TechniqueProjectile p) {
         Vfx.burst((ServerLevel)p.level(), p.position(), -13739009, p.radius * 0.5F, 10);
         p.discard();
      }
   }

   static final class InfinityAbility extends Ability {
      InfinityAbility() {
         super("infinity", AbilityKind.TOGGLE, AbilityStats.builder().cost(5.0F).upkeep(4.0F).cooldownSeconds(2.0F).radius(3.0F).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         ctx.sound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.6F);
         Vfx.burst(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -6301441, 1.6F, 10);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         AABB box = ctx.player.getBoundingBox().inflate(ctx.stats.radius());

         for (Projectile p : ctx.level.getEntitiesOfClass(Projectile.class, box, px -> px.getOwner() != ctx.player)) {
            Vec3 v = p.getDeltaMovement();
            if (v.lengthSqr() > 1.0E-4) {
               p.setDeltaMovement(v.scale(0.15));
               p.hurtMarked = true;
               if (ticks % 4 == 0) {
                  Vfx.particles(ctx.level, p.position(), -6301441, 0.6F, 2, 0.1, 0.01);
               }
            }
         }

         return true;
      }

      @Override
      public void deactivate(AbilityContext ctx) {
         ctx.sound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.8F);
      }
   }

   static final class PurpleBehavior implements ProjectileBehavior {
      @Override
      public void tick(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         Moves.breakSphere(level, p.position(), p.radius, 50.0F, 120);
         if (p.getOwner() instanceof LivingEntity caster) {
            for (LivingEntity e : AbilityContext.enemiesAround(caster, p.position(), p.radius + 1.0F)) {
               if (p.alreadyHit.add(e.getUUID())) {
                  JjkDamage.hurt(e, JjkDamage.TECHNIQUE, caster, p, p.damage, true);
                  Vfx.burst(level, e.position().add(0.0, 1.0, 0.0), -6274817, 2.0F, 10);
               }
            }
         }

         if (p.age % 3 == 0) {
            Vfx.ring(level, p.position(), -6274817, p.radius * 1.8F, 8);
         }
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
      }

      @Override
      public void expire(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         Vfx.burst(level, p.position(), -6274817, p.radius * 2.5F, 20);
         p.discard();
      }
   }

   static final class RedBehavior implements ProjectileBehavior {
      private static void detonate(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         Vec3 at = p.position();
         if (p.getOwner() instanceof LivingEntity caster) {
            for (LivingEntity e : AbilityContext.enemiesAround(caster, at, p.radius)) {
               JjkDamage.hurt(e, JjkDamage.TECHNIQUE, caster, p, p.damage, false);
               Moves.knockback(e, at, 2.4 * (0.6 + p.power()), 0.6);
            }
         }

         Moves.breakSphere(level, at, 1.5 + p.power() * 1.5, 2.5F, 40);
         Vfx.burst(level, at, -54742, p.radius, 14);
         Vfx.ring(level, at, -54742, p.radius * 1.6F, 16);
         Vfx.shake(level, at, 3.0F, 24.0, 10);
         level.playSound(null, at.x, at.y, at.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 1.3F);
         p.discard();
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         detonate(p);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         detonate(p);
      }

      @Override
      public void expire(TechniqueProjectile p) {
         detonate(p);
      }
   }
}
