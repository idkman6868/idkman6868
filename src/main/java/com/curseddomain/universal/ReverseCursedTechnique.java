package com.curseddomain.universal;

import com.curseddomain.energy.EnergyManager;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.vfx.Vfx;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class ReverseCursedTechnique extends Ability {
   public static final int POSITIVE = -462640;
   private static final int TRIAL_TICKS = 200;
   private static final Map<UUID, Integer> TRIAL = new HashMap<>();

   public ReverseCursedTechnique() {
      super("reverse_cursed_technique", AbilityKind.CHANNEL, AbilityStats.builder().cost(6.0F).upkeep(14.0F).cooldown(20).build());
   }

   @Override
   public boolean activeTick(AbilityContext ctx, int ticks) {
      if (ticks % 4 != 0) {
         return true;
      } else {
         LivingEntity patient = ctx.player;
         if (SorcererManager.get(ctx.player).isUnlocked("rct_output")) {
            LivingEntity target = ctx.target(5.0);
            if (target != null && ctx.isAlly(target) && target.getHealth() < target.getMaxHealth()) {
               patient = target;
            }
         }

         if (patient.getHealth() < patient.getMaxHealth()) {
            patient.heal(1.0F);
            patient.removeEffect(MobEffects.POISON);
            patient.removeEffect(MobEffects.WITHER);
            Vfx.particles(ctx.level, patient.position().add(0.0, 1.0, 0.0), -462640, 0.7F, 3, 0.35, 0.02);
            if (ticks % 20 == 0) {
               ctx.sound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 1.6F);
            }
         }

         return true;
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(Post event) {
      if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 5 == 0) {
         SorcererData data = SorcererManager.get(player);
         if (!data.isUnlocked("rct") && data.hasCursedEnergy()) {
            if (player.isAlive() && player.getHealth() <= 2.0F && EnergyManager.inCombat(player)) {
               int ticks = TRIAL.merge(player.getUUID(), 5, Integer::sum);
               if (ticks >= 200) {
                  TRIAL.remove(player.getUUID());
                  SorcererManager.unlock(player, "rct");
                  player.heal(6.0F);
                  Vfx.burst(player.serverLevel(), player.position().add(0.0, 1.0, 0.0), -462640, 2.5F, 20);
                  player.sendSystemMessage(
                     Component.translatable("combat.cursed_domain.rct_awakened").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD})
                  );
               }
            } else {
               TRIAL.remove(player.getUUID());
            }
         } else {
            TRIAL.remove(player.getUUID());
         }
      }
   }
}
