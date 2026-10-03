package com.curseddomain.universal;

import com.curseddomain.ModMain;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueCategory;
import com.curseddomain.technique.TechniqueRarity;
import com.curseddomain.technique.TechniqueTier;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityKind;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.newshadowstyle.NewShadowStyleTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class UniversalTechniques extends Technique {
   public static final UniversalTechniques INSTANCE = new UniversalTechniques();
   public static final ResourceLocation ID = ModMain.id("universal");

   private UniversalTechniques() {
      super(Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.COMMON, TechniqueTier.A).notInRandomRoll());
   }

   @Override
   public ResourceLocation id() {
      return ID;
   }

   @Override
   public boolean isImplemented() {
      return true;
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         new BlackFlash.Focus(),
         new UniversalTechniques.Reinforcement().requires("universal:reinforcement"),
         new ReverseCursedTechnique().requires("rct"),
         new NewShadowStyleTechnique.SimpleDomain().requires("universal:simple_domain"),
         new UniversalTechniques.HollowWickerBasket().requires("universal:hollow_wicker_basket"),
         new UniversalTechniques.FallingBlossom().requires("universal:falling_blossom_emotion"),
         new UniversalTechniques.DomainAmplification().requires("universal:domain_amplification").minGrade(Grade.GRADE_1)
      );
   }

   public static boolean isUniversal(Ability ability) {
      return ability.technique() == INSTANCE;
   }

   static final class DomainAmplification extends Ability {
      DomainAmplification() {
         super("domain_amplification", AbilityKind.TOGGLE, AbilityStats.builder().cost(20.0F).upkeep(8.0F).cooldownSeconds(5.0F).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         Vfx.burst(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -8758560, 1.4F, 10);
         ctx.sound(SoundEvents.ILLUSIONER_PREPARE_MIRROR, 1.0F, 0.8F);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         ctx.player.addEffect(new MobEffectInstance(ModEffects.DOMAIN_AMPLIFICATION, 25, 0, false, false, true));
         if (ticks % 8 == 0) {
            Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -8758560, 0.7F, 3, 0.3, 0.01);
         }

         return true;
      }

      @Override
      public void deactivate(AbilityContext ctx) {
         ctx.player.removeEffect(ModEffects.DOMAIN_AMPLIFICATION);
      }
   }

   static final class FallingBlossom extends Ability {
      FallingBlossom() {
         super("falling_blossom_emotion", AbilityKind.TOGGLE, AbilityStats.builder().cost(8.0F).upkeep(2.0F).cooldownSeconds(3.0F).build());
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         ctx.player.addEffect(new MobEffectInstance(ModEffects.FALLING_BLOSSOM_EMOTION, 25, 0, false, false, true));
         if (ticks % 20 == 0) {
            Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -1007424, 0.6F, 3, 0.4, 0.01);
         }

         return true;
      }

      @Override
      public void deactivate(AbilityContext ctx) {
         ctx.player.removeEffect(ModEffects.FALLING_BLOSSOM_EMOTION);
      }
   }

   static final class HollowWickerBasket extends Ability {
      HollowWickerBasket() {
         super("hollow_wicker_basket", AbilityKind.CHANNEL, AbilityStats.builder().cost(10.0F).upkeep(5.0F).cooldownSeconds(3.0F).build());
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         ctx.player.addEffect(new MobEffectInstance(ModEffects.HOLLOW_WICKER_BASKET, 5, 0, false, false, true));
         if (ticks % 6 == 0) {
            Vfx.ring(ctx.level, ctx.player.position(), -2572144, 1.6F, 8);
            Vfx.ring(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -2572144, 1.3F, 8);
         }

         return true;
      }
   }

   static final class Reinforcement extends Ability {
      Reinforcement() {
         super("reinforcement", AbilityKind.TOGGLE, AbilityStats.builder().cost(4.0F).upkeep(2.0F).cooldown(10).build());
      }

      @Override
      public boolean activate(AbilityContext ctx) {
         ctx.sound(SoundEvents.BEACON_POWER_SELECT, 0.6F, 1.6F);
         return true;
      }

      @Override
      public boolean activeTick(AbilityContext ctx, int ticks) {
         ctx.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 0, false, false, true));
         ctx.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25, 0, false, false, true));
         if (ticks % 10 == 0) {
            Vfx.particles(ctx.level, ctx.player.position().add(0.0, 1.0, 0.0), -8408321, 0.6F, 2, 0.35, 0.01);
         }

         return true;
      }
   }
}
