package com.curseddomain.combat;

import com.curseddomain.config.CombatConfig;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.technique.ability.AbilityContext;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class Moves {
   private Moves() {
   }

   public static void knockback(Entity target, Vec3 from, double strength, double lift) {
      Vec3 d = target.position().subtract(from);
      Vec3 h = new Vec3(d.x, 0.0, d.z);
      h = h.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : h.normalize();
      target.setDeltaMovement(target.getDeltaMovement().add(h.x * strength, lift, h.z * strength));
      target.hurtMarked = true;
   }

   public static void pull(Entity target, Vec3 to, double strength) {
      Vec3 d = to.subtract(target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0));
      double len = d.length();
      if (len < 0.3) {
         target.setDeltaMovement(target.getDeltaMovement().scale(0.5));
      } else {
         Vec3 v = d.scale(Math.min(strength, len * 0.25) / len);
         target.setDeltaMovement(target.getDeltaMovement().scale(0.6).add(v));
      }

      target.hurtMarked = true;
      target.fallDistance = 0.0F;
   }

   public static List<LivingEntity> areaDamage(LivingEntity caster, Vec3 center, double radius, float damage, boolean knock) {
      List<LivingEntity> hit = AbilityContext.enemiesAround(caster, center, radius);

      for (LivingEntity e : hit) {
         float falloff = (float)(1.0 - 0.5 * e.distanceTo(caster) / Math.max(radius * 2.0, 1.0));
         JjkDamage.hurt(e, JjkDamage.TECHNIQUE, caster, damage * Math.max(0.5F, falloff));
         if (knock) {
            knockback(e, center, 0.8 + radius * 0.08, 0.35);
         }
      }

      return hit;
   }

   public static Vec3 dashTarget(ServerLevel level, Entity entity, Vec3 start, Vec3 dir, double distance) {
      Vec3 end = start.add(dir.scale(distance));
      BlockHitResult hit = level.clip(new ClipContext(start.add(0.0, 0.5, 0.0), end.add(0.0, 0.5, 0.0), Block.COLLIDER, Fluid.NONE, entity));
      return hit.getType() == Type.MISS ? end : hit.getLocation().subtract(dir.scale(0.8)).subtract(0.0, 0.5, 0.0);
   }

   public static void teleport(Entity entity, Vec3 to) {
      if (entity instanceof ServerPlayer player) {
         player.teleportTo(to.x, to.y, to.z);
      } else {
         entity.teleportTo(to.x, to.y, to.z);
      }

      entity.fallDistance = 0.0F;
   }

   public static int breakSphere(ServerLevel level, Vec3 center, double radius, float maxHardness, int limit) {
      return !CombatConfig.TECHNIQUES_BREAK_BLOCKS.get() ? 0 : carve(level, center, radius, maxHardness, limit);
   }

   public static int domainBreak(ServerLevel level, Vec3 center, double radius, float maxHardness, int limit) {
      return !ServerConfig.DOMAINS_BREAK_BLOCKS.get() ? 0 : carve(level, center, radius, maxHardness, limit);
   }

   private static int carve(ServerLevel level, Vec3 center, double radius, float maxHardness, int limit) {
      int broken = 0;
      int r = (int)Math.ceil(radius);
      BlockPos c = BlockPos.containing(center);

      for (BlockPos pos : BlockPos.betweenClosed(c.offset(-r, -r, -r), c.offset(r, r, r))) {
         if (broken >= limit) {
            break;
         }

         if (!(pos.distToCenterSqr(center) > radius * radius)) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.is(BlockTags.WITHER_IMMUNE) && level.getBlockEntity(pos) == null) {
               float hardness = state.getDestroySpeed(level, pos);
               if (!(hardness < 0.0F) && !(hardness > maxHardness)) {
                  level.destroyBlock(pos, false);
                  broken++;
               }
            }
         }
      }

      return broken;
   }
}
