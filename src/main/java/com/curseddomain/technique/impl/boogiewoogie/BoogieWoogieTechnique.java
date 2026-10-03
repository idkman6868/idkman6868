package com.curseddomain.technique.impl.boogiewoogie;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.entity.projectile.ProjectileBehavior;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.util.Scheduler;
import com.curseddomain.vfx.Vfx;
import com.curseddomain.vfx.VfxKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BoogieWoogieTechnique extends ImplementedTechnique {
   public static final int CLAP = -999328;
   private static final Map<UUID, Vec3> MARKERS = new HashMap<>();

   public BoogieWoogieTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.MARKER_STONE, new BoogieWoogieTechnique.MarkerBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant("clap_swap", AbilityStats.builder().cost(6.0F).cooldown(20).range(30.0F).build(), BoogieWoogieTechnique::clapSwap),
         Ability.instant("swap_others", AbilityStats.builder().cost(8.0F).cooldown(30).range(30.0F).radius(12.0F).build(), BoogieWoogieTechnique::swapOthers),
         Ability.instant("feint", AbilityStats.builder().cost(3.0F).cooldown(30).radius(8.0F).build(), BoogieWoogieTechnique::feint),
         Ability.instant("mark_stone", AbilityStats.builder().cost(2.0F).cooldown(20).range(1.6F).build(), BoogieWoogieTechnique::markStone),
         Ability.instant(
               "swap_chain", AbilityStats.builder().cost(35.0F).cooldownSeconds(15.0F).damage(4.0F).radius(15.0F).build(), BoogieWoogieTechnique::swapChain
            )
            .maximum()
      );
   }

   static void clapFx(AbilityContext ctx) {
      ctx.sound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.4F, 1.6F);
      ctx.sound((SoundEvent)SoundEvents.NOTE_BLOCK_HAT.value(), 1.2F, 0.7F);
      Vfx.burst(ctx.level, ctx.eye().add(ctx.look().scale(0.6)).subtract(0.0, 0.4, 0.0), -999328, 0.6F, 5);
   }

   static void swap(ServerLevel level, Entity a, Entity b) {
      Vec3 pa = a.position();
      Vec3 pb = b.position();
      Moves.teleport(a, pb);
      Moves.teleport(b, pa);
      Vfx.send(level, VfxKind.SWAP, pa.add(0.0, 1.0, 0.0), pb.add(0.0, 1.0, 0.0), -999328, 1.0F, 8);
      level.playSound(null, pb.x, pb.y, pb.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.8F);
   }

   private static boolean clapSwap(AbilityContext ctx) {
      LivingEntity target = ctx.target(ctx.stats.range());
      clapFx(ctx);
      if (target != null) {
         swap(ctx.level, ctx.player, target);
         return true;
      } else {
         Vec3 marker = MARKERS.remove(ctx.player.getUUID());
         if (marker != null && marker.distanceTo(ctx.player.position()) <= ctx.stats.range() * 2.0F) {
            Vec3 from = ctx.player.position();
            Moves.teleport(ctx.player, marker);
            Vfx.send(ctx.level, VfxKind.SWAP, from.add(0.0, 1.0, 0.0), marker.add(0.0, 1.0, 0.0), -999328, 1.0F, 8);
            return true;
         } else {
            return true;
         }
      }
   }

   private static boolean swapOthers(AbilityContext ctx) {
      LivingEntity target = ctx.target(ctx.stats.range());
      if (target == null) {
         return false;
      } else {
         LivingEntity other = null;
         double best = Double.MAX_VALUE;

         for (LivingEntity e : ctx.level
            .getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(ctx.stats.radius()), ex -> ex != target && ex != ctx.player && ex.isAlive())) {
            double d = e.distanceToSqr(target);
            if (d < best) {
               best = d;
               other = e;
            }
         }

         clapFx(ctx);
         if (other == null) {
            return true;
         } else {
            swap(ctx.level, target, other);
            return true;
         }
      }
   }

   private static boolean feint(AbilityContext ctx) {
      clapFx(ctx);

      for (LivingEntity e : ctx.enemiesAround(ctx.player.position(), ctx.stats.radius())) {
         e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, true));
         e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, true));
         if (e instanceof Mob mob) {
            mob.setTarget(null);
         }
      }

      Vfx.ring(ctx.level, ctx.player.position(), -999328, ctx.stats.radius(), 10);
      return true;
   }

   private static boolean markStone(AbilityContext ctx) {
      TechniqueProjectile stone = TechniqueProjectile.spawn(
         ctx.level, ctx.player, ProjectileKind.MARKER_STONE, ctx.eye().add(ctx.look()), ctx.look().scale(ctx.stats.range()), 0.2F, 0.0F
      );
      stone.gravity = 0.05F;
      stone.maxAge = 80;
      ctx.sound(SoundEvents.SNOWBALL_THROW, 0.8F, 0.6F);
      return true;
   }

   private static boolean swapChain(AbilityContext ctx) {
      List<LivingEntity> enemies = ctx.enemiesAround(ctx.player.position(), ctx.stats.radius());
      if (enemies.isEmpty()) {
         return false;
      } else {
         float damage = ctx.scaledDamage();
         Scheduler.repeat(ctx.level, 0, 3, 6, i -> {
            if (ctx.player.isAlive()) {
               LivingEntity e = enemies.get(ctx.level.random.nextInt(enemies.size()));
               if (e.isAlive()) {
                  clapFx(ctx);
                  swap(ctx.level, ctx.player, e);
                  ctx.damage(e, damage, JjkDamage.TECHNIQUE, true);
                  e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true));
               }
            }
         });
         return true;
      }
   }

   static final class MarkerBehavior implements ProjectileBehavior {
      private static void mark(TechniqueProjectile p) {
         if (p.getOwner() != null) {
            BoogieWoogieTechnique.MARKERS.put(p.getOwner().getUUID(), p.position());
            Vfx.ring((ServerLevel)p.level(), p.position(), -999328, 0.8F, 30);
         }

         p.discard();
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         mark(p);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         mark(p);
      }

      @Override
      public void expire(TechniqueProjectile p) {
         mark(p);
      }
   }
}
