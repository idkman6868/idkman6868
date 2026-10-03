package com.curseddomain.entity.projectile;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public interface ProjectileBehavior {
   Map<ProjectileKind, ProjectileBehavior> REGISTRY = new EnumMap<>(ProjectileKind.class);
   ProjectileBehavior DEFAULT = new ProjectileBehavior() {};

   static void register(ProjectileKind kind, ProjectileBehavior behavior) {
      REGISTRY.put(kind, behavior);
   }

   static ProjectileBehavior of(ProjectileKind kind) {
      return REGISTRY.getOrDefault(kind, DEFAULT);
   }

   default void tick(TechniqueProjectile projectile) {
   }

   default void hitEntity(TechniqueProjectile projectile, EntityHitResult hit) {
      projectile.damageTarget(hit.getEntity(), projectile.damage);
      if (!projectile.pierce) {
         projectile.discard();
      }
   }

   default void hitBlock(TechniqueProjectile projectile, BlockHitResult hit) {
      projectile.discard();
   }

   default void expire(TechniqueProjectile projectile) {
      projectile.discard();
   }
}
