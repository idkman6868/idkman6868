package com.curseddomain.technique.impl.newshadowstyle;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public class NewShadowStyleTechnique extends ImplementedTechnique {
   public static final int FIELD = -7352065;
   private static final Map<UUID, Long> COUNTER = new HashMap<>();

   public NewShadowStyleTechnique(Technique.Properties properties) {
      super(properties);
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         new NewShadowStyleTechnique.SimpleDomain(),
         Ability.instant(
            "quick_draw", AbilityStats.builder().cost(8.0F).cooldownSeconds(3.0F).damage(10.0F).range(7.0F).build(), NewShadowStyleTechnique::quickDraw
         ),
         Ability.instant("iai_counter", AbilityStats.builder().cost(6.0F).cooldownSeconds(4.0F).damage(12.0F).duration(20).build(), ctx -> {
            COUNTER.put(ctx.player.getUUID(), ctx.level.getGameTime() + ctx.stats.duration());
            ctx.sound((SoundEvent)SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.5F);
            Vfx.ring(ctx.level, ctx.player.position(), -7352065, 1.2F, ctx.stats.duration());
            return true;
         })
      );
   }

   private static boolean quickDraw(AbilityContext ctx) {
      Vec3 from = ctx.player.position();
      Vec3 dir = new Vec3(ctx.look().x, 0.0, ctx.look().z).normalize();
      Vec3 to = Moves.dashTarget(ctx.level, ctx.player, from, dir, ctx.stats.range());
      Set<LivingEntity> hit = new HashSet<>();

      for (double d = 0.0; d < from.distanceTo(to); d += 0.5) {
         hit.addAll(ctx.enemiesAround(from.add(dir.scale(d)).add(0.0, 1.0, 0.0), 1.5));
      }

      Moves.teleport(ctx.player, to);

      for (LivingEntity e : hit) {
         ctx.damage(e, ctx.scaledDamage());
      }

      Vfx.slash(ctx.level, from.add(dir.scale(from.distanceTo(to) / 2.0)).add(0.0, 1.0, 0.0), dir, -1510145, 2.2F, 10);
      ctx.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.2F, 1.6F);
      return true;
   }

   @SubscribeEvent
   public static void onIncoming(LivingIncomingDamageEvent event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() instanceof LivingEntity attacker) {
         Long until = COUNTER.get(player.getUUID());
         if (until != null && until >= player.level().getGameTime() && event.getSource().getDirectEntity() == attacker) {
            COUNTER.remove(player.getUUID());
            event.setCanceled(true);
            JjkDamage.hurt(attacker, JjkDamage.TECHNIQUE, player, 12.0F * AbilityContext.damageScale(player));
            Vfx.crossSlash(
               player.serverLevel(),
               attacker.position().add(0.0, attacker.getBbHeight() * 0.6, 0.0),
               attacker.position().subtract(player.position()),
               -1510145,
               1.6F,
               10
            );
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 1.9F);
         }
      }
   }

   public static final class SimpleDomain extends Ability {
      private static final float RADIUS = 2.6F;
      private final Map<UUID, Long> lastCut = new HashMap<>();

      public SimpleDomain() {
         super("simple_domain", AbilityKind.TOGGLE, AbilityStats.builder().cost(8.0F).upkeep(3.0F).cooldownSeconds(2.0F).damage(8.0F).radius(2.6F).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         ctx.sound(SoundEvents.AMETHYST_BLOCK_PLACE, 1.0F, 0.8F);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         ctx.player.addEffect(new MobEffectInstance(ModEffects.SIMPLE_DOMAIN, 25, 0, false, false, true));
         if (ticks % 10 == 0) {
            Vfx.ring(ctx.level, ctx.player.position(), -7352065, ctx.stats.radius(), 12);
         }

         long now = ctx.level.getGameTime();

         for (LivingEntity e : ctx.enemiesAround(ctx.player.position().add(0.0, 1.0, 0.0), ctx.stats.radius())) {
            Long last = this.lastCut.get(e.getUUID());
            if (last == null || now - last > 20L) {
               this.lastCut.put(e.getUUID(), now);
               ctx.damage(e, ctx.scaledDamage(), JjkDamage.TECHNIQUE, true);
               Vfx.slash(ctx.level, e.position().add(0.0, e.getBbHeight() * 0.6, 0.0), e.position().subtract(ctx.player.position()), -1510145, 1.4F, 6);
               ctx.level.playSound(null, e.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.8F);
            }
         }

         return true;
      }

      @Override
      public void deactivate(AbilityContext ctx) {
         ctx.player.removeEffect(ModEffects.SIMPLE_DOMAIN);
         this.lastCut.clear();
      }
   }
}
