package com.curseddomain.combat;

import com.curseddomain.ModMain;
import com.curseddomain.config.ServerConfig;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class JjkDamage {
   public static final ResourceKey<DamageType> TECHNIQUE = key("technique");
   public static final ResourceKey<DamageType> SURE_HIT = key("sure_hit");
   public static final ResourceKey<DamageType> SOUL = key("soul");
   public static final ResourceKey<DamageType> BLACK_FLASH = key("black_flash");
   public static final ResourceKey<DamageType> DOMAIN = key("domain");

   private JjkDamage() {
   }

   private static ResourceKey<DamageType> key(String name) {
      return ResourceKey.create(Registries.DAMAGE_TYPE, ModMain.id(name));
   }

   public static DamageSource source(Entity anchor, ResourceKey<DamageType> type, @Nullable Entity attacker, @Nullable Entity direct) {
      Reference<DamageType> holder = anchor.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
      return new DamageSource(holder, direct != null ? direct : attacker, attacker);
   }

   public static boolean hurt(LivingEntity target, ResourceKey<DamageType> type, @Nullable Entity attacker, float amount) {
      return hurt(target, type, attacker, null, amount, false);
   }

   public static boolean hurt(
      LivingEntity target, ResourceKey<DamageType> type, @Nullable Entity attacker, @Nullable Entity direct, float amount, boolean ignoreInvulnerability
   ) {
      if (target != attacker && !(amount <= 0.0F) && target.isAlive()) {
         float scaled = amount * ((Double)ServerConfig.TECHNIQUE_DAMAGE_MULTIPLIER.get()).floatValue();
         if (target instanceof Player && attacker instanceof Player) {
            scaled *= ((Double)ServerConfig.PVP_TECHNIQUE_DAMAGE_MULTIPLIER.get()).floatValue();
         }

         if (ignoreInvulnerability) {
            target.invulnerableTime = 0;
         }

         return target.hurt(source(target, type, attacker, direct), scaled);
      } else {
         return false;
      }
   }
}
