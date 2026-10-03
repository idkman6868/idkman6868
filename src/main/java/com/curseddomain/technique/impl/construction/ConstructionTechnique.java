package com.curseddomain.technique.impl.construction;

import com.curseddomain.block.ConstructBlock;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.entity.projectile.ProjectileBehavior;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.item.ConstructedBladeItem;
import com.curseddomain.registry.ModBlocks;
import com.curseddomain.registry.ModItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructionTechnique extends ImplementedTechnique {
   public static final int CONSTRUCT = -3612417;

   public ConstructionTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.CONSTRUCTED_BULLET, new ConstructionTechnique.BulletBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant(
            "construct_blade", AbilityStats.builder().cost(25.0F).cooldownSeconds(20.0F).durationSeconds(60.0F).build(), ConstructionTechnique::blade
         ),
         Ability.instant("construct_wall", AbilityStats.builder().cost(20.0F).cooldownSeconds(8.0F).range(6.0F).build(), ConstructionTechnique::wall),
         Ability.instant("super_bullet", AbilityStats.builder().cost(40.0F).cooldown(24000).damage(34.0F).range(4.0F).build(), ConstructionTechnique::bullet),
         Ability.instant("final_construction", AbilityStats.builder().cost(300.0F).cooldown(240000).build(), ConstructionTechnique::finalConstruction)
            .maximum()
      );
   }

   private static void constructFx(AbilityContext ctx, Vec3 at) {
      Vfx.burst(ctx.level, at, -3612417, 1.0F, 10);
      Vfx.sparks(ctx.level, at, -3612417, 1.2F, 16);
      ctx.sound(at, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
   }

   private static boolean blade(AbilityContext ctx) {
      ItemStack blade = ConstructedBladeItem.temporary(
         new ItemStack((ItemLike)ModItems.CONSTRUCTED_BLADE.get()), ctx.level.getGameTime() + ctx.stats.duration()
      );
      if (!ctx.player.getInventory().add(blade)) {
         ctx.player.drop(blade, false);
      }

      constructFx(ctx, ctx.eye().add(ctx.look()));
      return true;
   }

   private static boolean wall(AbilityContext ctx) {
      Vec3 at = ctx.targetPoint(ctx.stats.range());
      Vec3 look = ctx.look();
      Vec3 side = new Vec3(-look.z, 0.0, look.x).normalize();
      BlockPos base = BlockPos.containing(at.subtract(look.scale(0.5)));
      int placed = 0;

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = 0; dy < 3; dy++) {
            BlockPos pos = BlockPos.containing(Vec3.atCenterOf(base).add(side.scale(dx))).above(dy);
            if (ctx.level.getBlockState(pos).canBeReplaced()) {
               ctx.level.setBlockAndUpdate(pos, ((ConstructBlock)ModBlocks.CONSTRUCT.get()).defaultBlockState());
               placed++;
            }
         }
      }

      if (placed == 0) {
         return false;
      } else {
         constructFx(ctx, Vec3.atCenterOf(base).add(0.0, 1.0, 0.0));
         return true;
      }
   }

   private static boolean bullet(AbilityContext ctx) {
      TechniqueProjectile p = TechniqueProjectile.spawn(
         ctx.level, ctx.player, ProjectileKind.CONSTRUCTED_BULLET, ctx.eye().add(ctx.look()), ctx.look().scale(ctx.stats.range()), 0.25F, ctx.scaledDamage()
      );
      p.pierce = true;
      p.maxAge = 40;
      ctx.sound((SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), 0.7F, 1.8F);
      Vfx.burst(ctx.level, ctx.eye().add(ctx.look().scale(1.5)), -8064, 0.8F, 6);
      return true;
   }

   private static boolean finalConstruction(AbilityContext ctx) {
      ItemStack tool = new ItemStack((ItemLike)ModItems.CONSTRUCTED_CURSED_TOOL.get());
      if (!ctx.player.getInventory().add(tool)) {
         ctx.player.drop(tool, false);
      }

      EnergyManager.addTraining(ctx.player, -60.0F, 0.0F, 0.0F, 0.0F);
      constructFx(ctx, ctx.player.position().add(0.0, 1.0, 0.0));
      Vfx.pillar(ctx.level, ctx.player.position(), -3612417, 6.0F, 30);
      ctx.player.displayClientMessage(Component.translatable("ability.cursed_domain.construction.final").withStyle(ChatFormatting.AQUA), false);
      return true;
   }

   static final class BulletBehavior implements ProjectileBehavior {
      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         p.damageTarget(hit.getEntity(), p.damage);
         Vfx.burst((ServerLevel)p.level(), hit.getLocation(), -8064, 1.0F, 8);
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         Vfx.burst((ServerLevel)p.level(), hit.getLocation(), -8064, 0.8F, 8);
         p.discard();
      }
   }
}
