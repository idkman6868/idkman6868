package com.curseddomain.technique.impl.ratio;

import com.curseddomain.combat.Moves;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import com.curseddomain.vfx.VfxKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public class RatioTechnique extends ImplementedTechnique {
   public static final int GOLD = -2047904;
   private static final float CRIT = 1.8F;
   private static final Map<UUID, Long> FOCUS = new HashMap<>();

   public RatioTechnique(Technique.Properties properties) {
      super(properties);
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant("ratio_strike", AbilityStats.builder().cost(8.0F).cooldownSeconds(3.0F).durationSeconds(3.0F).build(), ctx -> {
            FOCUS.put(ctx.player.getUUID(), ctx.level.getGameTime() + ctx.stats.duration());
            ctx.sound((SoundEvent)SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 0.7F);
            Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.2, 0.0), -2047904, 0.8F, 8, 0.4, 0.02);
            return true;
         }),
         Ability.instant(
            "collapse", AbilityStats.builder().cost(25.0F).cooldownSeconds(10.0F).damage(12.0F).range(5.0F).radius(4.0F).build(), RatioTechnique::collapse
         ),
         new RatioTechnique.Overtime()
      );
   }

   private static boolean isRatioUser(ServerPlayer player) {
      return SorcererManager.get(player).technique().filter(t -> t instanceof RatioTechnique).isPresent();
   }

   @SubscribeEvent
   public static void onHit(LivingIncomingDamageEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer player && event.getSource().getDirectEntity() == player && isRatioUser(player)) {
         LivingEntity target = event.getEntity();
         Long focus = FOCUS.get(player.getUUID());
         boolean focused = focus != null && focus >= player.level().getGameTime();
         if (!target.hasEffect(ModEffects.RATIO_MARK) && !focused) {
            target.addEffect(new MobEffectInstance(ModEffects.RATIO_MARK, 100, 0, false, true));
            Vfx.send(
               player.serverLevel(),
               VfxKind.RATIO_LINE,
               target.position().add(0.0, target.getBbHeight() * 0.7, 0.0),
               player.getLookAngle(),
               -2132754336,
               target.getBbHeight() * 0.6F,
               20
            );
         } else {
            event.setAmount(event.getAmount() * (focused ? 2.2F : 1.8F));
            target.removeEffect(ModEffects.RATIO_MARK);
            FOCUS.remove(player.getUUID());
            Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.7, 0.0);
            Vfx.send(player.serverLevel(), VfxKind.RATIO_LINE, at, player.getLookAngle(), -2047904, target.getBbHeight() * 0.8F, 14);
            Vfx.sparks(player.serverLevel(), at, -2047904, 1.5F, 14);
            player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2F, 0.7F);
         }
      }
   }

   private static boolean collapse(AbilityContext ctx) {
      Vec3 at = ctx.targetPoint(ctx.stats.range());
      BlockPos ground = BlockPos.containing(at).below();
      BlockState state = ctx.level.getBlockState(ground);
      if (!state.isAir()) {
         ctx.level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, at.y + 0.5, at.z, 80, 1.5, 0.6, 1.5, 0.3);
      }

      for (LivingEntity e : ctx.enemiesAround(at, ctx.stats.radius())) {
         ctx.damage(e, ctx.scaledDamage());
         Moves.knockback(e, at, 0.6, 0.7);
      }

      Moves.breakSphere(ctx.level, at, 2.5, 3.5F, 40);
      Vfx.ring(ctx.level, at, -2047904, ctx.stats.radius() + 1.0F, 12);
      Vfx.send(ctx.level, VfxKind.RATIO_LINE, at.add(0.0, 0.6, 0.0), ctx.look(), -2047904, 2.5F, 12);
      Vfx.shake(ctx.level, at, 2.5F, 16.0, 10);
      ctx.sound(at, (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.8F);
      return true;
   }

   static final class Overtime extends Ability {
      Overtime() {
         super("overtime", AbilityKind.TOGGLE, AbilityStats.builder().cost(5.0F).cooldownSeconds(10.0F).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         ctx.sound((SoundEvent)SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 0.6F);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         if (ctx.level.isNight()) {
            ctx.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 1, false, false, true));
            ctx.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 0, false, false, true));
            if (ticks % 20 == 0) {
               Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -2047904, 0.6F, 3, 0.3, 0.01);
            }
         } else {
            ctx.player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25, 0, false, false, true));
         }

         return true;
      }
   }
}
