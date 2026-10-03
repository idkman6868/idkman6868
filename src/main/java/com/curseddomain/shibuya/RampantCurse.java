package com.curseddomain.shibuya;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.cullinggame.CullingRegistries;
import com.curseddomain.cullinggame.EntityBossBar;
import com.curseddomain.cullinggame.Kogane;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.domain.DomainManager;
import com.curseddomain.domain.DomainType;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.shikigami.TransfiguredHumanEntity;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.technique.Technique;
import com.curseddomain.vfx.Vfx;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** A cursed spirit stronger than the Grade 4 rabble: graded curses and the special grades of the Shibuya Incident. */
public class RampantCurse extends Grade4Curse {
   private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(RampantCurse.class, EntityDataSerializers.INT);
   private boolean shibuyaMode;
   private boolean phaseTwo;
   private boolean absorbed;
   private int cooldown = 60;
   private int secondaryCooldown = 200;
   @Nullable
   private EntityBossBar bar;

   public RampantCurse(EntityType<? extends RampantCurse> type, Level level) {
      super(type, level);
      this.xpReward = 10;
   }

   @Nullable
   public static RampantCurse spawn(ServerLevel level, CurseVariant variant, Vec3 at, boolean shibuya) {
      RampantCurse curse = (RampantCurse)((EntityType)CullingRegistries.RAMPANT_CURSE.get()).create(level);
      if (curse == null) {
         return null;
      } else {
         curse.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360.0F, 0.0F);
         curse.finalizeSpawn(level, level.getCurrentDifficultyAt(curse.blockPosition()), MobSpawnType.EVENT, null);
         curse.setup(variant, shibuya);
         level.addFreshEntity(curse);
         return curse;
      }
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(VARIANT, 0);
   }

   public CurseVariant variant() {
      return CurseVariant.byIndex((Integer)this.entityData.get(VARIANT));
   }

   public void setup(CurseVariant variant, boolean shibuya) {
      this.entityData.set(VARIANT, variant.ordinal());
      this.shibuyaMode = shibuya;
      this.applyVariant();
      this.setHealth(this.getMaxHealth());
      if (variant.boss() || shibuya) {
         this.setPersistenceRequired();
      }
   }

   private void applyVariant() {
      CurseVariant variant = this.variant();
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(variant.health());
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(variant.damage());
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variant.speed());
      this.getAttribute(Attributes.SCALE).setBaseValue(variant.scale());
      this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(variant.boss() ? 48.0 : 32.0);
      if (variant.boss()) {
         this.setCustomName(variant.displayName().copy().withStyle(ChatFormatting.DARK_PURPLE));
         this.setCustomNameVisible(true);
      }
   }

   public Grade curseGrade() {
      return this.variant().grade();
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel level && this.isAlive()) {
         CurseVariant variant = this.variant();
         if (variant.boss()) {
            if (this.bar == null) {
               this.bar = new EntityBossBar(variant.displayName(), BossBarColor.PURPLE);
            }

            this.bar.tick(this);
         }

         if (this.cooldown > 0) {
            this.cooldown--;
         }

         if (this.secondaryCooldown > 0) {
            this.secondaryCooldown--;
         }

         if (variant == CurseVariant.HANAMI && this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
         }

         LivingEntity target = this.getTarget();
         if (target != null && target.isAlive()) {
            this.useAbility(level, variant, target);
         }
      }
   }

   private void useAbility(ServerLevel level, CurseVariant variant, LivingEntity target) {
      double dist = this.distanceTo(target);
      boolean sight = this.hasLineOfSight(target);
      Vec3 eye = this.getEyePosition();
      Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
      Vec3 dir = aim.subtract(eye).normalize();
      switch (variant) {
         case JOGO:
            if (this.cooldown == 0 && sight && dist < 24.0) {
               for (int i = 0; i < 4; i++) {
                  Vec3 spread = dir.add(this.random.nextGaussian() * 0.12, this.random.nextGaussian() * 0.06, this.random.nextGaussian() * 0.12);
                  TechniqueProjectile.spawn(level, this, ProjectileKind.EMBER_INSECT, eye, spread.normalize().scale(0.9), 0.5F, 4.0F);
               }

               this.cooldown = 70;
            }

            if (this.secondaryCooldown == 0 && sight && dist < 30.0) {
               this.say(level, "jogo.meteor");
               TechniqueProjectile.spawn(level, this, ProjectileKind.METEOR, target.position().add(0.0, 14.0, 0.0), new Vec3(0.0, -0.9, 0.0), 2.5F, 16.0F);
               this.secondaryCooldown = 260;
            }
            break;
         case HANAMI:
            if (this.cooldown == 0 && sight && dist < 22.0) {
               for (int i = -1; i <= 1; i++) {
                  Vec3 spread = dir.add(dir.cross(new Vec3(0.0, 1.0, 0.0)).scale(i * 0.15));
                  TechniqueProjectile.spawn(level, this, ProjectileKind.PLANT_SPEAR, eye, spread.normalize().scale(1.3), 0.6F, 5.0F);
               }

               this.cooldown = 70;
            }

            if (this.secondaryCooldown == 0 && dist < 10.0) {
               this.say(level, "hanami.roots");
               target.addEffect(new MobEffectInstance(ModEffects.STUNNED, 40, 0, false, true));
               Vfx.ring(level, target.position().add(0.0, 0.1, 0.0), -10843606, 2.5F, 20);
               this.secondaryCooldown = 200;
            }
            break;
         case DAGON:
            if (this.cooldown == 0 && sight && dist < 26.0) {
               for (int i = 0; i < 5; i++) {
                  Vec3 spread = dir.add(this.random.nextGaussian() * 0.15, this.random.nextGaussian() * 0.08, this.random.nextGaussian() * 0.15);
                  TechniqueProjectile.spawn(level, this, ProjectileKind.WATER_FISH, eye, spread.normalize().scale(1.1), 0.5F, 3.5F);
               }

               level.playSound(null, this.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.HOSTILE, 1.2F, 0.6F);
               this.cooldown = 60;
            }
            break;
         case MAHITO:
            if (this.secondaryCooldown == 0) {
               this.say(level, "mahito.transfigure");

               for (int i = 0; i < 2; i++) {
                  TransfiguredHumanEntity human = (TransfiguredHumanEntity)((EntityType)ModEntities.TRANSFIGURED_HUMAN.get()).create(level);
                  if (human != null) {
                     human.moveTo(this.getX() + this.random.nextGaussian() * 2.0, this.getY(), this.getZ() + this.random.nextGaussian() * 2.0, 0.0F, 0.0F);
                     human.setOwner(this);
                     human.lifetime = 600;
                     human.setTarget(target);
                     level.addFreshEntity(human);
                  }
               }

               this.secondaryCooldown = 240;
            }
            break;
         case KUROURUSHI:
            if (this.secondaryCooldown == 0 && dist < 20.0) {
               this.say(level, "kurourushi.swarm");

               for (int i = 0; i < 4; i++) {
                  Entity bug = EntityType.SILVERFISH.create(level);
                  if (bug != null) {
                     bug.moveTo(this.getX() + this.random.nextGaussian(), this.getY(), this.getZ() + this.random.nextGaussian(), 0.0F, 0.0F);
                     level.addFreshEntity(bug);
                  }
               }

               this.secondaryCooldown = 220;
            }
            break;
         case NAOYA:
            if (this.cooldown == 0 && dist < 16.0 && dist > 3.0) {
               this.say(level, "naoya.projection");
               Moves.teleport(this, target.position().subtract(dir.scale(1.5)));
               Vfx.bolt(level, eye, this.getEyePosition(), -1, 0.3F, 6);
               target.addEffect(new MobEffectInstance(ModEffects.STUNNED, 24, 0, false, true));
               JjkDamage.hurt(target, JjkDamage.TECHNIQUE, this, 7.0F);
               this.cooldown = 70;
            }
            break;
         default:
      }
   }

   public boolean doHurtTarget(Entity target) {
      boolean hit = super.doHurtTarget(target);
      if (hit && target instanceof LivingEntity living) {
         switch (this.variant()) {
            case JOGO -> living.igniteForSeconds(4.0F);
            case MAHITO -> {
               living.addEffect(new MobEffectInstance(ModEffects.SOUL_DAMAGE, 400, 0, false, true));
               if (this.level() instanceof ServerLevel l) {
                  Vfx.sparks(l, living.position().add(0.0, 1.0, 0.0), -7710032, 0.6F, 6);
               }
            }
            case DAGON -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
            default -> {
            }
         }
      }

      return hit;
   }

   public boolean hurt(DamageSource source, float amount) {
      boolean hurt = super.hurt(source, amount);
      if (hurt && this.isAlive() && this.level() instanceof ServerLevel level) {
         CurseVariant variant = this.variant();
         if (!this.phaseTwo && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.phaseTwo = true;
            if (variant == CurseVariant.JOGO) {
               this.castDomain(level, ModTechniques.DISASTER_FLAMES.get(), "jogo.domain");
            } else if (variant == CurseVariant.MAHITO) {
               this.castDomain(level, ModTechniques.IDLE_TRANSFIGURATION.get(), "mahito.domain");
            }
         }

         if (variant == CurseVariant.MAHITO && this.shibuyaMode && !this.absorbed && this.getHealth() < this.getMaxHealth() * 0.2F) {
            this.absorbed = true;
            if (this.bar != null) {
               this.bar.close();
            }

            ShibuyaIncident.mahitoAbsorbed(level, this);
         }
      }

      return hurt;
   }

   private void castDomain(ServerLevel level, Object technique, String line) {
      if (technique instanceof Technique t) {
         Optional<DomainExpansion> domain = t.domain();
         if (domain.isPresent()) {
            this.say(level, line);
            DomainManager.cast(this, domain.get(), DomainType.CLOSED, this.position());
         }
      }
   }

   public void die(DamageSource source) {
      super.die(source);
      if (this.bar != null) {
         this.bar.close();
      }
   }

   public void closeBar() {
      if (this.bar != null) {
         this.bar.close();
      }
   }

   private void say(ServerLevel level, String key) {
      Component line = Component.translatable("npc.cursed_domain." + key);
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (player.distanceToSqr(this.position()) < 1600.0) {
            Kogane.speech(player, "entity.cursed_domain.rampant_curse." + this.variant().id(), line, ChatFormatting.DARK_PURPLE);
         }
      }
   }

   public boolean removeWhenFarAway(double distance) {
      return !this.variant().boss() && !this.shibuyaMode && super.removeWhenFarAway(distance);
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("variant", this.variant().id());
      tag.putBoolean("shibuya", this.shibuyaMode);
      tag.putBoolean("phase_two", this.phaseTwo);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      CurseVariant variant = CurseVariant.byId(tag.getString("variant"));
      this.entityData.set(VARIANT, (variant == null ? CurseVariant.GRADE_3 : variant).ordinal());
      this.shibuyaMode = tag.getBoolean("shibuya");
      this.phaseTwo = tag.getBoolean("phase_two");
      float health = this.getHealth();
      this.applyVariant();
      this.setHealth(health);
   }
}
