package com.curseddomain.technique.impl.cursedspeech;

import com.curseddomain.combat.Grades;
import com.curseddomain.combat.Moves;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import com.curseddomain.vfx.VfxKind;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class CursedSpeechTechnique extends ImplementedTechnique {
   public static final int VOICE = -1517392;
   private static final int MAX_STRAIN = 4;

   public CursedSpeechTechnique(Technique.Properties properties) {
      super(properties);
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         word("stop", 10.0F, 6.0F, 0.0F, 1, (ctx, t, p) -> stun(t, (int)(50.0F * p))),
         word(
            "dont_move", 8.0F, 5.0F, 0.0F, 1, (ctx, t, p) -> t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int)(90.0F * p), 9, false, true))
         ),
         word("blast_away", 12.0F, 6.0F, 6.0F, 1, (ctx, t, p) -> {
            ctx.damage(t, ctx.scaledDamage() * p);
            Moves.knockback(t, ctx.player.position(), 3.2 * p, 0.8 * p);
         }),
         word("crush", 18.0F, 10.0F, 12.0F, 2, (ctx, t, p) -> {
            ctx.damage(t, ctx.scaledDamage() * p);
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, true));
            Vfx.ring(ctx.level, t.position(), -1517392, 1.5F, 10);
         }),
         word("sleep", 14.0F, 12.0F, 0.0F, 1, (ctx, t, p) -> {
            stun(t, (int)(100.0F * p));
            t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int)(100.0F * p), 0, false, true));
         }),
         word("run", 10.0F, 8.0F, 0.0F, 1, (ctx, t, p) -> {
            Moves.knockback(t, ctx.player.position(), 1.6 * p, 0.2);
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int)(80.0F * p), 1, false, true));
            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int)(80.0F * p), 1, false, true));
            if (t instanceof Mob mob) {
               mob.setTarget(null);
               Vec3 away = t.position().add(t.position().subtract(ctx.player.position()).normalize().scale(12.0));
               mob.getNavigation().moveTo(away.x, away.y, away.z, 1.6);
            }
         }),
         word("explode", 40.0F, 25.0F, 22.0F, 3, (ctx, t, p) -> {
            ctx.damage(t, ctx.scaledDamage() * p);
            Vfx.burst(ctx.level, t.position().add(0.0, 1.0, 0.0), -42454, 2.5F, 12);
            ctx.sound(t.position(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.2F);
         }).maximum()
      );
   }

   private static Ability word(String name, float cost, float cooldownSeconds, float damage, int strain, CursedSpeechTechnique.Command command) {
      return Ability.instant(
         name, AbilityStats.builder().cost(cost).cooldownSeconds(cooldownSeconds).damage(damage).range(14.0F).build(), ctx -> speak(ctx, name, strain, command)
      );
   }

   private static void stun(LivingEntity target, int ticks) {
      target.addEffect(new MobEffectInstance(ModEffects.STUNNED, Math.max(10, ticks), 0, false, true));
      if (target instanceof Mob mob) {
         mob.getNavigation().stop();
      }
   }

   private static boolean speak(AbilityContext ctx, String word, int strain, CursedSpeechTechnique.Command command) {
      MobEffectInstance hoarse = ctx.player.getEffect(ModEffects.HOARSE);
      int current = hoarse == null ? -1 : hoarse.getAmplifier();
      if (current + 1 >= 4) {
         ctx.player.displayClientMessage(Component.translatable("ability.cursed_domain.cursed_speech.throat").withStyle(ChatFormatting.RED), true);
         return false;
      } else {
         if (current >= 0) {
            ctx.player.hurt(ctx.player.damageSources().magic(), (current + 1) * 1.5F);
         }

         List<LivingEntity> targets = new ArrayList<>();
         Vec3 eye = ctx.eye();
         Vec3 look = ctx.look();

         for (LivingEntity e : ctx.enemiesAround(eye, ctx.stats.range())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye).normalize();
            if (to.dot(look) > 0.55 && ctx.player.hasLineOfSight(e)) {
               targets.add(e);
            }
         }

         int backlash = 0;

         for (LivingEntity target : targets) {
            int gap = Grades.gap(ctx.player, target);
            float power = gap >= 2 ? 0.3F : (gap == 1 ? 0.6F : 1.0F);
            backlash = Math.max(backlash, gap >= 2 ? 1 : 0);
            command.apply(ctx, target, power);
         }

         int amplifier = Math.min(3, current + strain + backlash);
         ctx.player.addEffect(new MobEffectInstance(ModEffects.HOARSE, 600, amplifier, false, false, true));
         Vfx.send(ctx.level, VfxKind.SOUND_WAVE, eye.add(look.scale(0.4)), look, -1517392, ctx.stats.range(), 16);
         ctx.player
            .displayClientMessage(
               Component.translatable("ability.cursed_domain.cursed_speech.say." + word)
                  .withStyle(new ChatFormatting[]{ChatFormatting.BOLD, ChatFormatting.GOLD}),
               true
            );
         ctx.sound(SoundEvents.EVOKER_CAST_SPELL, 1.2F, 0.6F);
         return true;
      }
   }

   @FunctionalInterface
   interface Command {
      void apply(AbilityContext var1, LivingEntity var2, float var3);
   }
}
