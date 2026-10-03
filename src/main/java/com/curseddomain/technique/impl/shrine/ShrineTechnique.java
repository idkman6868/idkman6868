package com.curseddomain.technique.impl.shrine;

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
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ShrineTechnique extends ImplementedTechnique {
   public static final int SLASH = -3856;
   public static final int FLAME = -34278;

   public ShrineTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.DISMANTLE, new ShrineTechnique.DismantleBehavior());
      ProjectileBehavior.register(ProjectileKind.WORLD_SLASH, new ShrineTechnique.DismantleBehavior());
      ProjectileBehavior.register(ProjectileKind.DIVINE_FLAME, new ShrineTechnique.FlameBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant(
            "dismantle",
            AbilityStats.builder().cost(10.0F).cooldown(14).damage(11.0F).range(3.0F).build(),
            ctx -> slashProjectile(ctx, ProjectileKind.DISMANTLE, 1.4F, 20)
         ),
         Ability.instant("cleave", AbilityStats.builder().cost(18.0F).cooldownSeconds(3.0F).damage(6.0F).range(4.5F).build(), ShrineTechnique::cleave),
         Ability.charge(
               "divine_flame",
               AbilityStats.builder().cost(80.0F).cooldownSeconds(30.0F).damage(32.0F).range(2.6F).radius(6.0F).charge(40).build(),
               ShrineTechnique::divineFlame
            )
            .onCharge(ShrineTechnique::kindle)
            .minGrade(Grade.GRADE_2),
         Ability.instant(
               "world_cutting_slash",
               AbilityStats.builder().cost(150.0F).cooldownSeconds(90.0F).damage(60.0F).range(3.5F).build(),
               ctx -> slashProjectile(ctx, ProjectileKind.WORLD_SLASH, 6.0F, 30)
            )
            .requires("world_slash")
            .maximum()
      );
   }

   @Override
   protected DomainExpansion createDomain() {
      return new MalevolentShrine();
   }

   private static boolean slashProjectile(AbilityContext ctx, ProjectileKind kind, float size, int life) {
      TechniqueProjectile p = TechniqueProjectile.spawn(
         ctx.level, ctx.player, kind, ctx.eye().add(ctx.look().scale(1.2)), ctx.look().scale(ctx.stats.range()), size, ctx.scaledDamage()
      );
      p.pierce = true;
      p.maxAge = life;
      p.radius = size * 0.6F;
      if (kind == ProjectileKind.WORLD_SLASH) {
         p.damageType = JjkDamage.SOUL;
         Vfx.shake(ctx.level, ctx.player.position(), 3.0F, 30.0, 12);
      }

      ctx.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, kind == ProjectileKind.WORLD_SLASH ? 0.5F : 1.6F);
      return true;
   }

   private static boolean cleave(AbilityContext ctx) {
      LivingEntity target = ctx.target(ctx.stats.range());
      if (target == null) {
         return false;
      } else {
         float damage = ctx.scaledDamage() + target.getMaxHealth() * 0.15F;
         ctx.damage(target, damage, JjkDamage.TECHNIQUE, true);
         Vfx.crossSlash(ctx.level, target.position().add(0.0, target.getBbHeight() * 0.6, 0.0), ctx.look(), -3856, target.getBbHeight() * 0.9F, 10);
         Vfx.sparks(ctx.level, target.position().add(0.0, 1.0, 0.0), -5238768, 1.5F, 18);
         ctx.sound(target.position(), SoundEvents.PLAYER_ATTACK_STRONG, 1.2F, 0.6F);
         return true;
      }
   }

   private static void kindle(AbilityContext ctx, int ticks) {
      if (ticks % 3 == 0) {
         Vec3 hand = ctx.eye().add(ctx.look().scale(0.9)).subtract(0.0, 0.3, 0.0);
         Vfx.particles(ctx.level, hand, -34278, 1.0F, 3, 0.15, 0.03);
         if (ticks % 12 == 0) {
            ctx.sound(SoundEvents.BLAZE_BURN, 0.6F, 0.6F);
         }
      }
   }

   private static void divineFlame(AbilityContext ctx, float charge) {
      if (!(charge < 0.4F)) {
         TechniqueProjectile p = TechniqueProjectile.spawn(
            ctx.level,
            ctx.player,
            ProjectileKind.DIVINE_FLAME,
            ctx.eye().add(ctx.look().scale(1.5)),
            ctx.look().scale(ctx.stats.range()),
            0.5F + charge * 0.3F,
            ctx.scaledDamage() * charge
         );
         p.radius = ctx.stats.radius() * charge;
         p.maxAge = 60;
         p.setPower(charge);
         ctx.sound(SoundEvents.FIRECHARGE_USE, 1.5F, 0.5F);
         ctx.player
            .displayClientMessage(
               Component.translatable("ability.cursed_domain.shrine.open").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}), true
            );
      }
   }

   static final class DismantleBehavior implements ProjectileBehavior {
      @Override
      public void tick(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         BlockPos pos = p.blockPosition();
         BlockState state = level.getBlockState(pos);
         boolean world = p.kind() == ProjectileKind.WORLD_SLASH;
         if (world) {
            Moves.breakSphere(level, p.position(), p.radius, 50.0F, 60);
            if (p.getOwner() instanceof LivingEntity owner) {
               for (LivingEntity e : AbilityContext.enemiesAround(owner, p.position(), p.radius + 1.0F)) {
                  if (p.alreadyHit.add(e.getUUID())) {
                     JjkDamage.hurt(e, JjkDamage.SOUL, owner, p, p.damage, true);
                  }
               }
            }
         } else if (state.is(BlockTags.LEAVES) || state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.FLOWERS)) {
            Moves.breakSphere(level, Vec3.atCenterOf(pos), 0.6, 0.5F, 1);
         }
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         p.damageTarget(hit.getEntity(), p.damage);
         Vfx.slash((ServerLevel)p.level(), hit.getLocation(), p.getDeltaMovement(), -3856, 1.6F, 8);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         if (p.kind() != ProjectileKind.WORLD_SLASH) {
            Vfx.slash((ServerLevel)p.level(), hit.getLocation(), p.getDeltaMovement(), -3856, 1.2F, 6);
            p.discard();
         }
      }
   }

   static final class FlameBehavior implements ProjectileBehavior {
      private static void erupt(TechniqueProjectile p) {
         ServerLevel level = (ServerLevel)p.level();
         Vec3 at = p.position();
         if (p.getOwner() instanceof LivingEntity owner) {
            for (LivingEntity e : AbilityContext.enemiesAround(owner, at, p.radius)) {
               JjkDamage.hurt(e, JjkDamage.TECHNIQUE, owner, p, p.damage, false);
               e.igniteForSeconds(8.0F);
            }
         }

         if ((Boolean)CombatConfig.TECHNIQUES_BREAK_BLOCKS.get()) {
            BlockPos c = BlockPos.containing(at);

            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 2, 3))) {
               if (level.random.nextFloat() < 0.25F && level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid()) {
                  level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
               }
            }
         }

         Vfx.burst(level, at, -34278, p.radius, 18);
         Vfx.pillar(level, at, -46576, p.radius * 2.5F, 24);
         Vfx.ring(level, at, -34278, p.radius * 1.6F, 16);
         Vfx.shake(level, at, 4.0F, 40.0, 14);
         level.playSound(null, at.x, at.y, at.z, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0F, 0.6F);
         p.discard();
      }

      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         erupt(p);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         erupt(p);
      }

      @Override
      public void expire(TechniqueProjectile p) {
         erupt(p);
      }
   }
}
