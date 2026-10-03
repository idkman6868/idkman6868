package com.curseddomain.technique.impl.idletransfiguration;

import com.curseddomain.ModMain;
import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.entity.projectile.ProjectileBehavior;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.shikigami.TransfiguredHumanEntity;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class IdleTransfigurationTechnique extends ImplementedTechnique {
   public static final int SOUL = -7710032;

   public IdleTransfigurationTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.SOUL_SPIKE, new IdleTransfigurationTechnique.IsomerBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant(
            "soul_touch",
            AbilityStats.builder().cost(15.0F).cooldownSeconds(2.0F).damage(9.0F).range(4.0F).durationSeconds(30.0F).build(),
            IdleTransfigurationTechnique::soulTouch
         ),
         Ability.instant(
            "body_repel", AbilityStats.builder().cost(15.0F).cooldownSeconds(5.0F).damage(8.0F).radius(4.5F).build(), IdleTransfigurationTechnique::bodyRepel
         ),
         Ability.instant(
            "transfigured_humans",
            AbilityStats.builder().cost(30.0F).cooldownSeconds(30.0F).durationSeconds(45.0F).build(),
            IdleTransfigurationTechnique::summonHumans
         ),
         new IdleTransfigurationTechnique.BladeMorph(),
         Ability.instant(
               "soul_isomer",
               AbilityStats.builder().cost(70.0F).cooldownSeconds(30.0F).damage(26.0F).range(1.4F).radius(5.0F).build(),
               IdleTransfigurationTechnique::isomer
            )
            .maximum()
      );
   }

   @Override
   protected DomainExpansion createDomain() {
      return new SelfEmbodimentOfPerfection();
   }

   public static void touchSoul(ServerLevel level, LivingEntity caster, LivingEntity target, float damage, int duration) {
      JjkDamage.hurt(target, JjkDamage.SOUL, caster, null, damage, true);
      MobEffectInstance wound = target.getEffect(ModEffects.SOUL_DAMAGE);
      int amp = wound == null ? 0 : Math.min(9, wound.getAmplifier() + 1);
      target.addEffect(new MobEffectInstance(ModEffects.SOUL_DAMAGE, duration, amp, false, true, true));
      Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
      Vfx.burst(level, at, -7710032, 1.2F, 10);
      Vfx.sparks(level, at, -3628824, 1.2F, 14);
      level.playSound(null, target.blockPosition(), (SoundEvent)SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.5F, 0.7F);
   }

   private static boolean soulTouch(AbilityContext ctx) {
      LivingEntity target = ctx.target(ctx.stats.range());
      if (target == null) {
         return false;
      } else {
         touchSoul(ctx.level, ctx.player, target, ctx.scaledDamage(), ctx.stats.duration());
         ctx.player.swing(InteractionHand.MAIN_HAND, true);
         return true;
      }
   }

   private static boolean bodyRepel(AbilityContext ctx) {
      Vec3 at = ctx.player.position().add(0.0, 1.0, 0.0);

      for (LivingEntity e : ctx.enemiesAround(at, ctx.stats.radius())) {
         ctx.damage(e, ctx.scaledDamage());
         Moves.knockback(e, at, 1.5, 0.5);
      }

      for (int i = 0; i < 10; i++) {
         double a = i * Math.PI / 5.0;
         Vec3 dir = new Vec3(Math.cos(a), 0.15, Math.sin(a));
         Vfx.beam(ctx.level, at, at.add(dir.scale(ctx.stats.radius())), -7710032, 0.18F, 8);
      }

      Vfx.burst(ctx.level, at, -7710032, 1.6F, 8);
      ctx.sound(SoundEvents.PUFFER_FISH_BLOW_UP, 1.2F, 0.6F);
      return true;
   }

   private static boolean summonHumans(AbilityContext ctx) {
      for (int i = 0; i < 3; i++) {
         double a = i * Math.PI * 2.0 / 3.0;
         Vec3 at = ctx.player.position().add(Math.cos(a) * 2.0, 0.0, Math.sin(a) * 2.0);
         TransfiguredHumanEntity human = (TransfiguredHumanEntity)((EntityType)ModEntities.TRANSFIGURED_HUMAN.get()).create(ctx.level);
         if (human != null) {
            human.moveTo(at.x, at.y, at.z, ctx.player.getYRot(), 0.0F);
            human.setOwner(ctx.player);
            human.lifetime = ctx.stats.duration();
            human.finalizeSpawn(ctx.level, ctx.level.getCurrentDifficultyAt(human.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            ctx.level.addFreshEntity(human);
            Vfx.burst(ctx.level, at.add(0.0, 1.0, 0.0), -7710032, 1.4F, 10);
         }
      }

      ctx.sound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0F, 0.6F);
      return true;
   }

   private static boolean isomer(AbilityContext ctx) {
      TechniqueProjectile p = TechniqueProjectile.spawn(
         ctx.level, ctx.player, ProjectileKind.SOUL_SPIKE, ctx.eye().add(ctx.look().scale(1.5)), ctx.look().scale(ctx.stats.range()), 1.1F, ctx.scaledDamage()
      );
      p.radius = ctx.stats.radius();
      p.maxAge = 50;
      p.gravity = 0.015F;
      ctx.sound(SoundEvents.ZOMBIE_VILLAGER_CURE, 1.0F, 0.5F);
      return true;
   }

   static final class BladeMorph extends Ability {
      private static final ResourceLocation DAMAGE = ModMain.id("blade_morph.damage");
      private static final ResourceLocation REACH = ModMain.id("blade_morph.reach");

      BladeMorph() {
         super("blade_morph", AbilityKind.TOGGLE, AbilityStats.builder().cost(12.0F).upkeep(3.0F).cooldownSeconds(3.0F).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         set(ctx, true);
         ctx.sound(SoundEvents.SLIME_SQUISH, 1.2F, 0.5F);
         Vfx.burst(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -7710032, 1.2F, 8);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         if (ticks % 30 == 0) {
            Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -7710032, 0.6F, 3, 0.3, 0.01);
         }

         return true;
      }

      @Override
      public void deactivate(AbilityContext ctx) {
         set(ctx, false);
      }

      private static void set(AbilityContext ctx, boolean on) {
         AttributeInstance damage = ctx.player.getAttribute(Attributes.ATTACK_DAMAGE);
         AttributeInstance reach = ctx.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
         if (on) {
            if (damage != null) {
               damage.addOrUpdateTransientModifier(new AttributeModifier(DAMAGE, 6.0, Operation.ADD_VALUE));
            }

            if (reach != null) {
               reach.addOrUpdateTransientModifier(new AttributeModifier(REACH, 1.5, Operation.ADD_VALUE));
            }
         } else {
            if (damage != null) {
               damage.removeModifier(DAMAGE);
            }

            if (reach != null) {
               reach.removeModifier(REACH);
            }
         }
      }
   }

   static final class IsomerBehavior implements ProjectileBehavior {
      private static void burst(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         if (p.getOwner() instanceof LivingEntity owner) {
            for (LivingEntity e : AbilityContext.enemiesAround(owner, p.position(), p.radius)) {
               IdleTransfigurationTechnique.touchSoul(level, owner, e, p.damage, 600);
            }
         }

         Vfx.burst(level, p.position(), -7710032, p.radius, 16);
         Vfx.ring(level, p.position(), -3628824, p.radius * 1.4F, 14);
         level.playSound(null, p.blockPosition(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.6F);
         p.discard();
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         burst(p);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         burst(p);
      }

      @Override
      public void expire(TechniqueProjectile p) {
         burst(p);
      }
   }
}
