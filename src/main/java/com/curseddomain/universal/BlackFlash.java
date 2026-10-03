package com.curseddomain.universal;

import com.curseddomain.combat.CombatRecord;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.Vfx;
import com.curseddomain.vfx.VfxKind;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
public final class BlackFlash {
   public static final float MULTIPLIER = 2.5F;
   private static final int LATE_WINDOW = 12;
   private static final Map<UUID, Long> FOCUS = new HashMap<>();

   private BlackFlash() {
   }

   public static int window(ServerPlayer player) {
      int window = 2;
      if (player.hasEffect(ModEffects.ZONE)) {
         window += 2;
      }

      if (SorcererManager.get(player).technique().filter(t -> t == ModTechniques.DIVERGENT_FIST.get()).isPresent()) {
         window++;
      }

      return window;
   }

   @SubscribeEvent
   public static void onHit(LivingIncomingDamageEvent event) {
      if (event.getSource().getEntity() instanceof ServerPlayer player && event.getSource().getDirectEntity() == player) {
         Long focus = FOCUS.get(player.getUUID());
         if (focus != null) {
            long delay = player.level().getGameTime() - focus;
            if (delay <= 12L && delay >= 0L) {
               FOCUS.remove(player.getUUID());
               LivingEntity target = event.getEntity();
               if (delay <= window(player)) {
                  event.setAmount(event.getAmount() * 2.5F);
                  trigger(player, target);
               } else {
                  event.setAmount(event.getAmount() + 3.0F);
                  Vfx.particles(player.serverLevel(), target.position().add(0.0, 1.0, 0.0), -8408321, 0.8F, 6, 0.2, 0.05);
               }
            }
         }
      }
   }

   public static void trigger(ServerPlayer player, LivingEntity target) {
      Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
      Vfx.send(player.serverLevel(), VfxKind.BLACK_SPARKS, at, Vec3.ZERO, -2093014, 2.6F, 14);
      Vfx.burst(player.serverLevel(), at, -2093014, 1.6F, 8);
      Vfx.shake(player.serverLevel(), at, 3.5F, 20.0, 10);
      Vfx.screen(player, ScreenFxPayload.Kind.FLASH, 2097160, 0.55F, 6);
      player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.0F, 1.4F);
      player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.4F, 0.5F);
      player.addEffect(new MobEffectInstance(ModEffects.ZONE, 600, 0, false, false, true));
      CombatRecord record = (CombatRecord)player.getData(ModAttachments.COMBAT_RECORD);
      record.blackFlashes++;
      if (record.blackFlashes == 1) {
         player.displayClientMessage(
            Component.translatable("combat.cursed_domain.first_black_flash").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}),
            false
         );
      } else {
         player.displayClientMessage(
            Component.translatable("combat.cursed_domain.black_flash").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}), true
         );
      }
   }

   static final class Focus extends Ability {
      Focus() {
         super("black_flash", AbilityKind.INSTANT, AbilityStats.builder().cost(5.0F).cooldown(8).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         BlackFlash.FOCUS.put(ctx.player.getUUID(), ctx.level.getGameTime());
         Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.1, 0.0).add(ctx.look().scale(0.6)), -14024696, 0.7F, 4, 0.15, 0.02);
         return true;
      }
   }
}
