package com.curseddomain.sorcerer;

import com.curseddomain.config.ServerConfig;
import com.curseddomain.entity.cursedspirit.CursedSpirit;
import com.curseddomain.world.SealBreak;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class AwakeningEvents {
   private AwakeningEvents() {
   }

   @SubscribeEvent
   public static void onDamage(Post event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() instanceof CursedSpirit) {
         if (SorcererManager.get(player).status() == SorcererStatus.NON_SORCERER && !(event.getNewDamage() <= 0.0F)) {
            StoryData story = AwakeningManager.story(player);
            story.curseHits++;
            int dread = (Integer)ServerConfig.DREAD_SECONDS.get() * 20;
            if (dread > 0) {
               player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, dread + 40, 0, false, false));
            }

            if (player.isAlive() && player.getHealth() <= (Double)ServerConfig.NEAR_DEATH_HEALTH.get()) {
               AwakeningManager.awaken(player, AwakeningCause.NEAR_DEATH);
            } else if (story.curseHits >= (Integer)ServerConfig.CURSE_HITS_TO_AWAKEN.get()) {
               AwakeningManager.awaken(player, AwakeningCause.CURSE_ATTACKS);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
         StoryData story = AwakeningManager.story(player);
         long now = player.serverLevel().getGameTime();
         if (story.scoutMessageAt >= 0L && now >= story.scoutMessageAt) {
            story.scoutMessageAt = -1L;
            AwakeningManager.sendScoutMessage(player);
         }

         if (player.tickCount % 1200 == 0
            && player.serverLevel().isNight()
            && player.serverLevel().dimensionType().natural()
            && player.getRandom().nextDouble() < (Double)ServerConfig.SEAL_BREAK_CHANCE_PER_MINUTE.get()) {
            SealBreak.nearPlayer(player);
         }
      }
   }
}
