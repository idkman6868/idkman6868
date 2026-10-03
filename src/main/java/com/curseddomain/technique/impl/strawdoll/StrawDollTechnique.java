package com.curseddomain.technique.impl.strawdoll;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.entity.projectile.ProjectileBehavior;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class StrawDollTechnique extends ImplementedTechnique {
   public static final int NAIL_GLOW = -9787137;
   private static final Map<UUID, List<StrawDollTechnique.Embedded>> NAILS = new HashMap<>();

   public StrawDollTechnique(Technique.Properties properties) {
      super(properties);
      ProjectileBehavior.register(ProjectileKind.NAIL, new StrawDollTechnique.NailBehavior());
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant("hammer_nails", AbilityStats.builder().cost(6.0F).cooldown(16).damage(4.0F).range(2.2F).build(), StrawDollTechnique::hammer),
         Ability.instant(
            "hairpin", AbilityStats.builder().cost(12.0F).cooldownSeconds(5.0F).damage(6.0F).radius(2.5F).range(48.0F).build(), StrawDollTechnique::hairpin
         ),
         Ability.instant(
            "resonance", AbilityStats.builder().cost(20.0F).cooldownSeconds(10.0F).damage(10.0F).range(64.0F).build(), ctx -> resonance(ctx, false)
         ),
         Ability.instant(
               "resonance_burst", AbilityStats.builder().cost(35.0F).cooldownSeconds(25.0F).damage(18.0F).range(64.0F).build(), ctx -> resonance(ctx, true)
            )
            .maximum()
      );
   }

   private static List<StrawDollTechnique.Embedded> nails(Player player) {
      long now = player.level().getGameTime();
      List<StrawDollTechnique.Embedded> list = NAILS.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
      list.removeIf(n -> n.expires < now);
      return list;
   }

   private static boolean hammer(AbilityContext ctx) {
      Vec3 dir = ctx.look();
      Vec3 right = new Vec3(-dir.z, 0.0, dir.x).normalize();

      for (int i = -1; i <= 1; i++) {
         Vec3 v = dir.add(right.scale(i * 0.06)).normalize().scale(ctx.stats.range());
         TechniqueProjectile nail = TechniqueProjectile.spawn(
            ctx.level, ctx.player, ProjectileKind.NAIL, ctx.eye().add(dir.scale(0.8)).add(right.scale(i * 0.25)), v, 0.3F, ctx.scaledDamage()
         );
         nail.maxAge = 40;
         nail.gravity = 0.01F;
      }

      ctx.sound(SoundEvents.ANVIL_PLACE, 0.5F, 1.8F);
      Vfx.particles(ctx.level, ctx.eye().add(dir), -9787137, 0.8F, 6, 0.1, 0.05);
      return true;
   }

   private static boolean hairpin(AbilityContext ctx) {
      List<StrawDollTechnique.Embedded> list = nails(ctx.player);
      if (list.isEmpty()) {
         return false;
      } else {
         for (StrawDollTechnique.Embedded nail : list) {
            Vec3 at = nail.pos;
            if (nail.host != null && ctx.level.getEntity(nail.host) instanceof LivingEntity host && host.isAlive()) {
               at = host.position().add(0.0, host.getBbHeight() * 0.6, 0.0);
               ctx.damage(host, ctx.scaledDamage(), JjkDamage.TECHNIQUE, true);
            }

            if (!(at.distanceTo(ctx.player.position()) > ctx.stats.range())) {
               for (LivingEntity e : ctx.enemiesAround(at, ctx.stats.radius())) {
                  if (!e.getUUID().equals(nail.host)) {
                     ctx.damage(e, ctx.scaledDamage() * 0.5F);
                  }
               }

               Vfx.burst(ctx.level, at, -9787137, ctx.stats.radius(), 10);
               Vfx.sparks(ctx.level, at, -3612417, 1.5F, 12);
               ctx.sound(at, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), 0.6F, 1.6F);
            }
         }

         list.clear();
         ctx.sound((SoundEvent)SoundEvents.NOTE_BLOCK_HAT.value(), 1.0F, 1.2F);
         return true;
      }
   }

   private static boolean resonance(AbilityContext ctx, boolean burst) {
      List<StrawDollTechnique.Embedded> list = nails(ctx.player);
      List<LivingEntity> hosts = new ArrayList<>();

      for (StrawDollTechnique.Embedded nail : list) {
         if (nail.host != null
            && ctx.level.getEntity(nail.host) instanceof LivingEntity host
            && host.isAlive()
            && host.distanceTo(ctx.player) <= ctx.stats.range()
            && !hosts.contains(host)) {
            hosts.add(host);
         }
      }

      if (hosts.isEmpty()) {
         return false;
      } else {
         if (burst) {
            ctx.player.hurt(ctx.player.damageSources().magic(), 4.0F);
         }

         for (LivingEntity host : hosts) {
            float damage = ctx.scaledDamage() * (host.getType().getCategory().isFriendly() ? 1.0F : 1.2F);
            JjkDamage.hurt(host, JjkDamage.SOUL, ctx.player, null, damage, true);
            Vfx.burst(ctx.level, host.position().add(0.0, host.getBbHeight() / 2.0F, 0.0), -50630, 1.8F, 12);
            Vfx.beam(
               ctx.level, host.position().add(0.0, host.getBbHeight() + 0.5, 0.0), host.position().add(0.0, host.getBbHeight() + 3.0F, 0.0), -50630, 0.15F, 10
            );
         }

         Vfx.ring(ctx.level, ctx.player.position(), -50630, 2.5F, 12);
         ctx.sound(SoundEvents.ANVIL_LAND, 0.8F, 0.7F);
         list.removeIf(n -> n.host != null);
         return true;
      }
   }

   public static int embeddedCount(Entity caster) {
      return caster instanceof Player p ? nails(p).size() : 0;
   }

   private record Embedded(ServerLevel level, Vec3 pos, UUID host, long expires) {
   }

   static final class NailBehavior implements ProjectileBehavior {
      @Override
      public void hitEntity(TechniqueProjectile p, EntityHitResult hit) {
         p.damageTarget(hit.getEntity(), p.damage);
         if (p.getOwner() instanceof Player owner) {
            StrawDollTechnique.nails(owner)
               .add(new StrawDollTechnique.Embedded((ServerLevel)p.level(), hit.getLocation(), hit.getEntity().getUUID(), p.level().getGameTime() + 1200L));
         }

         p.level().playSound(null, p.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 0.7F, 1.5F);
         p.discard();
      }

      @Override
      public void hitBlock(TechniqueProjectile p, BlockHitResult hit) {
         if (p.getOwner() instanceof Player owner) {
            StrawDollTechnique.nails(owner)
               .add(new StrawDollTechnique.Embedded((ServerLevel)p.level(), hit.getLocation(), null, p.level().getGameTime() + 1200L));
         }

         p.level().playSound(null, p.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.7F, 1.4F);
         p.discard();
      }
   }
}
