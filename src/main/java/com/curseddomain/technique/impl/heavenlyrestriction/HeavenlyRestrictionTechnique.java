package com.curseddomain.technique.impl.heavenlyrestriction;

import com.curseddomain.ModMain;
import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.shikigami.ShadowStorage;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HeavenlyRestrictionTechnique extends ImplementedTechnique {
   public static final int STEEL = -1513232;
   private static final Map<UUID, Long> PENDING_SLAM = new HashMap<>();
   private final boolean full;
   private final List<HeavenlyRestrictionTechnique.Modifier> modifiers;

   public HeavenlyRestrictionTechnique(Technique.Properties properties, boolean full) {
      super(properties);
      this.full = full;
      double s = full ? 1.0 : 0.6;
      this.modifiers = List.of(
         new HeavenlyRestrictionTechnique.Modifier(Attributes.ATTACK_DAMAGE, ModMain.id("hr.damage"), 8.0 * s, Operation.ADD_VALUE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.MOVEMENT_SPEED, ModMain.id("hr.speed"), 0.5 * s, Operation.ADD_MULTIPLIED_BASE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.ARMOR, ModMain.id("hr.armor"), 10.0 * s, Operation.ADD_VALUE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.ARMOR_TOUGHNESS, ModMain.id("hr.toughness"), 6.0 * s, Operation.ADD_VALUE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.MAX_HEALTH, ModMain.id("hr.health"), 12.0 * s, Operation.ADD_VALUE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.JUMP_STRENGTH, ModMain.id("hr.jump"), 0.25 * s, Operation.ADD_VALUE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.ATTACK_SPEED, ModMain.id("hr.attack_speed"), 0.4 * s, Operation.ADD_MULTIPLIED_BASE),
         new HeavenlyRestrictionTechnique.Modifier(Attributes.SAFE_FALL_DISTANCE, ModMain.id("hr.fall"), 10.0 * s, Operation.ADD_VALUE)
      );
   }

   public static HeavenlyRestrictionTechnique partial(Technique.Properties properties) {
      return new HeavenlyRestrictionTechnique(properties, false);
   }

   public static HeavenlyRestrictionTechnique full(Technique.Properties properties) {
      return new HeavenlyRestrictionTechnique(properties, true);
   }

   @Override
   protected List<Ability> createAbilities() {
      List<Ability> list = new ArrayList<>(
         List.of(
            Ability.instant(
               "heavenly_dash",
               AbilityStats.builder().cooldownSeconds(3.0F).damage(this.full ? 10.0F : 7.0F).range(this.full ? 12.0F : 9.0F).build(),
               HeavenlyRestrictionTechnique::dash
            ),
            Ability.instant(
               "crushing_leap",
               AbilityStats.builder().cooldownSeconds(8.0F).damage(this.full ? 16.0F : 11.0F).radius(4.0F).build(),
               HeavenlyRestrictionTechnique::leap
            ),
            Ability.instant(
               "keen_senses",
               AbilityStats.builder().cooldownSeconds(20.0F).radius(this.full ? 48.0F : 32.0F).durationSeconds(10.0F).build(),
               HeavenlyRestrictionTechnique::senses
            )
         )
      );
      if (this.full) {
         list.add(
            Ability.instant(
               "inventory_curse",
               AbilityStats.builder().cooldown(10).build(),
               ctx -> {
                  ctx.player
                     .openMenu(
                        new SimpleMenuProvider(
                           (id, inv, p) -> ChestMenu.threeRows(id, inv, ((ShadowStorage)ctx.player.getData(ModAttachments.SHADOW_STORAGE)).container),
                           Component.translatable("container.cursed_domain.inventory_curse")
                        )
                     );
                  ctx.sound(SoundEvents.SLIME_SQUISH, 1.0F, 0.6F);
                  return true;
               }
            )
         );
      }

      return list;
   }

   private boolean restricted(ServerPlayer player) {
      return SorcererManager.get(player).hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL);
   }

   @Override
   public void passiveTick(ServerPlayer player) {
      if (player.tickCount % 20 == 0) {
         boolean apply = this.restricted(player);

         for (HeavenlyRestrictionTechnique.Modifier m : this.modifiers) {
            AttributeInstance instance = player.getAttribute(m.attribute);
            if (instance != null) {
               if (apply) {
                  instance.addOrUpdateTransientModifier(new AttributeModifier(m.id, m.amount, m.op));
               } else {
                  instance.removeModifier(m.id);
               }
            }
         }
      }

      Long started = PENDING_SLAM.get(player.getUUID());
      if (started != null && player.level().getGameTime() - started > 4L && player.onGround()) {
         PENDING_SLAM.remove(player.getUUID());
         slam(player);
      }
   }

   @Override
   public void onRemoved(ServerPlayer player) {
      for (HeavenlyRestrictionTechnique.Modifier m : this.modifiers) {
         AttributeInstance instance = player.getAttribute(m.attribute);
         if (instance != null) {
            instance.removeModifier(m.id);
         }
      }

      if (player.getHealth() > player.getMaxHealth()) {
         player.setHealth(player.getMaxHealth());
      }
   }

   private static boolean dash(AbilityContext ctx) {
      Vec3 from = ctx.player.position();
      Vec3 dir = new Vec3(ctx.look().x, 0.0, ctx.look().z).normalize();
      Vec3 to = Moves.dashTarget(ctx.level, ctx.player, from, dir, ctx.stats.range());
      Set<LivingEntity> hit = new HashSet<>();

      for (double d = 0.0; d < from.distanceTo(to); d += 0.5) {
         hit.addAll(ctx.enemiesAround(from.add(dir.scale(d)).add(0.0, 1.0, 0.0), 1.4));
      }

      Moves.teleport(ctx.player, to);

      for (LivingEntity e : hit) {
         ctx.damage(e, ctx.scaledDamage());
         Vfx.slash(ctx.level, e.position().add(0.0, 1.0, 0.0), dir, -1513232, 1.4F, 8);
      }

      Vfx.beam(ctx.level, from.add(0.0, 1.0, 0.0), to.add(0.0, 1.0, 0.0), -2130706433, 0.3F, 6);
      ctx.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.4F);
      return true;
   }

   private static boolean leap(AbilityContext ctx) {
      Vec3 look = ctx.look();
      ctx.player.setDeltaMovement(look.x * 1.3, 1.1, look.z * 1.3);
      ctx.player.hurtMarked = true;
      PENDING_SLAM.put(ctx.player.getUUID(), ctx.level.getGameTime());
      ctx.sound(SoundEvents.RAVAGER_STEP, 1.0F, 0.8F);
      return true;
   }

   private static void slam(ServerPlayer player) {
      Vec3 at = player.position();
      float damage = 12.0F * AbilityContext.damageScale(player);

      for (LivingEntity e : AbilityContext.enemiesAround(player, at, 4.5)) {
         JjkDamage.hurt(e, JjkDamage.TECHNIQUE, player, damage);
         Moves.knockback(e, at, 1.2, 0.6);
      }

      BlockState ground = player.serverLevel().getBlockState(player.blockPosition().below());
      player.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.2, at.z, 60, 1.5, 0.2, 1.5, 0.2);
      Vfx.ring(player.serverLevel(), at, -1513232, 5.0F, 12);
      Vfx.shake(player.serverLevel(), at, 3.0F, 16.0, 10);
      player.serverLevel().playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 0.7F);
      player.fallDistance = 0.0F;
   }

   private static boolean senses(AbilityContext ctx) {
      for (LivingEntity e : ctx.enemiesAround(ctx.player.position(), ctx.stats.radius())) {
         e.addEffect(new MobEffectInstance(MobEffects.GLOWING, ctx.stats.duration(), 0, false, false));
      }

      Vfx.ring(ctx.level, ctx.player.position(), -1513232, ctx.stats.radius(), 20);
      ctx.sound(SoundEvents.WARDEN_HEARTBEAT, 1.0F, 1.2F);
      return true;
   }

   private record Modifier(Holder<Attribute> attribute, ResourceLocation id, double amount, Operation op) {
   }
}
