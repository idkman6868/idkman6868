package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class TraitEffects {
   private static final ResourceLocation FRAIL_BODY = ModMain.id("heavenly_restriction_frailty");

   private TraitEffects() {
   }

   public static void apply(ServerPlayer player) {
      SorcererData data = SorcererManager.get(player);
      AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
      if (maxHealth != null) {
         if (data.hasTrait(InnateTrait.HEAVENLY_RESTRICTION_ENERGY)) {
            maxHealth.addOrUpdateTransientModifier(new AttributeModifier(FRAIL_BODY, -(Double)ServerConfig.HR_ENERGY_HEALTH_PENALTY.get(), Operation.ADD_VALUE));
         } else {
            maxHealth.removeModifier(FRAIL_BODY);
         }

         if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
         }
      }
   }

   @SubscribeEvent
   public static void onEffectApplicable(Applicable event) {
      if (event.getEntity() instanceof Player player
         && event.getEffectInstance().getEffect().equals(MobEffects.POISON)
         && SorcererManager.get(player).hasTrait(InnateTrait.DEATH_PAINTING)) {
         event.setResult(Result.DO_NOT_APPLY);
      }
   }
}
