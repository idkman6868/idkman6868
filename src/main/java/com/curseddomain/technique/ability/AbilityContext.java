package com.curseddomain.technique.ability;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.config.CombatConfig;
import com.curseddomain.domain.DomainManager;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.SorcererManager;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

public final class AbilityContext {
   public final ServerPlayer player;
   public final ServerLevel level;
   public final Ability ability;
   public final AbilityStats stats;

   public AbilityContext(ServerPlayer player, Ability ability) {
      this.player = player;
      this.level = player.serverLevel();
      this.ability = ability;
      this.stats = ability.stats();
   }

   public Vec3 eye() {
      return this.player.getEyePosition();
   }

   public Vec3 look() {
      return this.player.getLookAngle();
   }

   public float damageScale() {
      return damageScale(this.player);
   }

   public static float damageScale(ServerPlayer player) {
      Grade grade = SorcererManager.get(player).grade();
      int steps = Math.max(0, grade.ordinal() - Grade.GRADE_4.ordinal());
      return (1.0F + steps * ((Double)CombatConfig.GRADE_DAMAGE_BONUS.get()).floatValue()) * DomainManager.damageMultiplier(player);
   }

   public float scaledDamage() {
      return this.stats.damage() * this.damageScale();
   }

   public HitResult raycast(double range) {
      Vec3 from = this.eye();
      Vec3 to = from.add(this.look().scale(range));
      BlockHitResult block = this.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this.player));
      Vec3 end = block.getType() == Type.MISS ? to : block.getLocation();
      AABB box = this.player.getBoundingBox().expandTowards(end.subtract(from)).inflate(1.0);
      EntityHitResult entity = ProjectileUtil.getEntityHitResult(
         this.level, this.player, from, end, box, e -> e instanceof LivingEntity && e.isPickable() && !this.isAlly(e), 0.3F
      );
      return (HitResult)(entity != null ? entity : block);
   }

   @Nullable
   public LivingEntity target(double range) {
      if (this.raycast(range) instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity living) {
         return living;
      } else {
         Vec3 eye = this.eye();
         Vec3 look = this.look();
         LivingEntity best = null;
         double bestScore = 0.97;

         for (LivingEntity candidate : this.enemiesAround(eye, range)) {
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(eye);
            double dot = to.normalize().dot(look);
            if (dot > bestScore && this.player.hasLineOfSight(candidate)) {
               bestScore = dot;
               best = candidate;
            }
         }

         return best;
      }
   }

   public Vec3 targetPoint(double range) {
      HitResult hit = this.raycast(range);
      return hit.getType() == Type.MISS ? this.eye().add(this.look().scale(range)) : hit.getLocation();
   }

   public List<LivingEntity> enemiesAround(Vec3 center, double radius) {
      return enemiesAround(this.player, center, radius);
   }

   public static List<LivingEntity> enemiesAround(Entity caster, Vec3 center, double radius) {
      AABB box = new AABB(center, center).inflate(radius);
      return caster.level()
         .getEntitiesOfClass(
            LivingEntity.class, box, e -> e != caster && e.isAlive() && !e.isSpectator() && !isAlly(caster, e) && e.distanceToSqr(center) <= radius * radius
         );
   }

   public boolean isAlly(Entity entity) {
      return isAlly(this.player, entity);
   }

   public static boolean isAlly(Entity caster, Entity entity) {
      if (entity == caster) {
         return true;
      } else {
         return entity instanceof OwnableEntity owned && caster.getUUID().equals(owned.getOwnerUUID()) ? true : caster.isAlliedTo(entity);
      }
   }

   public boolean damage(LivingEntity target, float amount) {
      return JjkDamage.hurt(target, JjkDamage.TECHNIQUE, this.player, amount);
   }

   public boolean damage(LivingEntity target, float amount, ResourceKey<DamageType> type, boolean ignoreInvulnerability) {
      return JjkDamage.hurt(target, type, this.player, null, amount, ignoreInvulnerability);
   }

   public void sound(SoundEvent sound, float volume, float pitch) {
      this.level.playSound(null, this.player.getX(), this.player.getY(), this.player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
   }

   public void sound(Vec3 at, SoundEvent sound, float volume, float pitch) {
      this.level.playSound(null, at.x, at.y, at.z, sound, SoundSource.PLAYERS, volume, pitch);
   }
}
