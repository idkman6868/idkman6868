package com.curseddomain.registry;

import com.curseddomain.ModMain;
import com.curseddomain.effect.ModMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
   public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "cursed_domain");
   public static final DeferredHolder<MobEffect, MobEffect> STUNNED = EFFECTS.register(
      "stunned",
      () -> new ModMobEffect(MobEffectCategory.HARMFUL, 13158655)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, ModMain.id("effect.stunned.speed"), -1.0, Operation.ADD_MULTIPLIED_TOTAL)
         .addAttributeModifier(Attributes.JUMP_STRENGTH, ModMain.id("effect.stunned.jump"), -1.0, Operation.ADD_MULTIPLIED_TOTAL)
         .addAttributeModifier(Attributes.ATTACK_SPEED, ModMain.id("effect.stunned.attack"), -0.9, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final DeferredHolder<MobEffect, MobEffect> EXHAUSTED = EFFECTS.register(
      "exhausted",
      () -> new ModMobEffect(MobEffectCategory.HARMFUL, 5917290)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, ModMain.id("effect.exhausted.speed"), -0.25, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final DeferredHolder<MobEffect, MobEffect> BURNOUT = EFFECTS.register("burnout", () -> new ModMobEffect(MobEffectCategory.HARMFUL, 10369562));
   public static final DeferredHolder<MobEffect, MobEffect> HOARSE = EFFECTS.register("hoarse", () -> new ModMobEffect(MobEffectCategory.HARMFUL, 11045482));
   public static final DeferredHolder<MobEffect, MobEffect> RATIO_MARK = EFFECTS.register(
      "ratio_mark", () -> new ModMobEffect(MobEffectCategory.HARMFUL, 14729312)
   );
   public static final DeferredHolder<MobEffect, MobEffect> FLOWING_RED_SCALE = EFFECTS.register(
      "flowing_red_scale",
      () -> new ModMobEffect(MobEffectCategory.BENEFICIAL, 11538458)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, ModMain.id("effect.red_scale.speed"), 0.35, Operation.ADD_MULTIPLIED_TOTAL)
         .addAttributeModifier(Attributes.ATTACK_DAMAGE, ModMain.id("effect.red_scale.damage"), 3.0, Operation.ADD_VALUE)
         .addAttributeModifier(Attributes.ATTACK_SPEED, ModMain.id("effect.red_scale.attack"), 0.3, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final DeferredHolder<MobEffect, MobEffect> SOUL_DAMAGE = EFFECTS.register(
      "soul_damage",
      () -> new ModMobEffect(MobEffectCategory.HARMFUL, 6962316)
         .addAttributeModifier(Attributes.MAX_HEALTH, ModMain.id("effect.soul_damage.health"), -4.0, Operation.ADD_VALUE)
   );
   public static final DeferredHolder<MobEffect, MobEffect> SIMPLE_DOMAIN = EFFECTS.register(
      "simple_domain",
      () -> new ModMobEffect(MobEffectCategory.NEUTRAL, 9425151)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, ModMain.id("effect.simple_domain.speed"), -0.6, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final DeferredHolder<MobEffect, MobEffect> HOLLOW_WICKER_BASKET = EFFECTS.register(
      "hollow_wicker_basket",
      () -> new ModMobEffect(MobEffectCategory.BENEFICIAL, 14205072)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, ModMain.id("effect.hwb.speed"), -1.0, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final DeferredHolder<MobEffect, MobEffect> FALLING_BLOSSOM_EMOTION = EFFECTS.register(
      "falling_blossom_emotion", () -> new ModMobEffect(MobEffectCategory.BENEFICIAL, 15769792)
   );
   public static final DeferredHolder<MobEffect, MobEffect> DOMAIN_AMPLIFICATION = EFFECTS.register(
      "domain_amplification", () -> new ModMobEffect(MobEffectCategory.BENEFICIAL, 8018656)
   );
   public static final DeferredHolder<MobEffect, MobEffect> ZONE = EFFECTS.register(
      "zone",
      () -> new ModMobEffect(MobEffectCategory.BENEFICIAL, 11538464)
         .addAttributeModifier(Attributes.ATTACK_SPEED, ModMain.id("effect.zone.attack"), 0.25, Operation.ADD_MULTIPLIED_TOTAL)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, ModMain.id("effect.zone.speed"), 0.15, Operation.ADD_MULTIPLIED_TOTAL)
   );

   private ModEffects() {
   }
}
