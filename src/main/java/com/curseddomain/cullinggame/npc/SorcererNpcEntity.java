package com.curseddomain.cullinggame.npc;

import com.curseddomain.combat.JjkDamage;
import com.curseddomain.combat.Moves;
import com.curseddomain.cullinggame.CullingGame;
import com.curseddomain.cullinggame.CullingGameData;
import com.curseddomain.cullinggame.CullingRegistries;
import com.curseddomain.cullinggame.CullingRule;
import com.curseddomain.cullinggame.EntityBossBar;
import com.curseddomain.cullinggame.Kogane;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.incarnation.Incarnation;
import com.curseddomain.school.JujutsuHigh;
import com.curseddomain.domain.DomainManager;
import com.curseddomain.domain.DomainStyle;
import com.curseddomain.domain.DomainType;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.entity.cursedspirit.CursedSpirit;
import com.curseddomain.entity.projectile.ProjectileKind;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.shibuya.CurseVariant;
import com.curseddomain.shibuya.RampantCurse;
import com.curseddomain.shibuya.ShibuyaIncident;
import com.curseddomain.shikigami.ShikigamiEntity;
import com.curseddomain.technique.Technique;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.Vfx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A sorcerer who is not a Minecraft player: Culling Game players such as Higuruma or Kashimo, generic awakened and
 * incarnated players, and the scripted bosses Kenjaku and Sukuna.
 */
public class SorcererNpcEntity extends PathfinderMob {
   private static final EntityDataAccessor<Integer> PROFILE = SynchedEntityData.defineId(SorcererNpcEntity.class, EntityDataSerializers.INT);
   private static final Map<UUID, Long> MIYO_LESSONS = new HashMap<>();
   /** Points this player is holding (shown by rule 9, handed over by rule 10). */
   public int points;
   private boolean shibuyaMode;
   private boolean leaving;
   private boolean phaseTwo;
   private int cooldown = 60;
   private int secondaryCooldown = 160;
   private int verdictTimer = -1;
   @Nullable
   private UUID verdictTarget;
   @Nullable
   private UUID lastAttacker;
   @Nullable
   private EntityBossBar bar;
   /** Residents stay near the spot they were placed at. */
   @Nullable
   private Vec3 home;
   /** Cursed corpses only exist for one entrance exam; ones loaded from disk are left over and vanish. */
   private boolean stale;

   public SorcererNpcEntity(EntityType<? extends SorcererNpcEntity> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.FOLLOW_RANGE, 40.0)
         .add(Attributes.ARMOR, 2.0);
   }

   /** Creates and adds an NPC at the given spot. */
   @Nullable
   public static SorcererNpcEntity spawn(ServerLevel level, NpcProfile profile, Vec3 at, int points, boolean shibuya) {
      SorcererNpcEntity npc = (SorcererNpcEntity)((EntityType)CullingRegistries.SORCERER_NPC.get()).create(level);
      if (npc == null) {
         return null;
      } else {
         npc.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360.0F, 0.0F);
         npc.finalizeSpawn(level, level.getCurrentDifficultyAt(npc.blockPosition()), MobSpawnType.EVENT, null);
         npc.setup(profile, points, shibuya);
         level.addFreshEntity(npc);
         Vfx.burst(level, at.add(0.0, 1.0, 0.0), -1, 1.5F, 10);
         return npc;
      }
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PROFILE, 0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, e -> this.huntsPlayers((LivingEntity)e)));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, CursedSpirit.class, 10, true, false, e -> this.profile().hostile()));
   }

   private boolean huntsPlayers(LivingEntity target) {
      return this.profile().hostile() && !this.leaving && target instanceof Player player && !player.isCreative() && !player.isSpectator();
   }

   public NpcProfile profile() {
      return NpcProfile.byIndex((Integer)this.entityData.get(PROFILE));
   }

   public boolean shibuyaMode() {
      return this.shibuyaMode;
   }

   public void setup(NpcProfile profile, int points, boolean shibuya) {
      this.entityData.set(PROFILE, profile.ordinal());
      this.points = points;
      this.shibuyaMode = shibuya;
      this.applyProfile();
      this.setHealth(this.getMaxHealth());
      this.setPersistenceRequired();
      ItemStack held = switch (profile.held()) {
         case IRON_SWORD -> new ItemStack(Items.IRON_SWORD);
         case GOLDEN_SWORD -> new ItemStack(Items.GOLDEN_SWORD);
         case NETHERITE_SWORD -> new ItemStack(Items.NETHERITE_SWORD);
         case STICK -> new ItemStack(Items.STICK);
         default -> ItemStack.EMPTY;
      };
      this.setItemSlot(EquipmentSlot.MAINHAND, held);
      this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
   }

   private void applyProfile() {
      NpcProfile profile = this.profile();
      double health = profile.health();
      if (profile == NpcProfile.SUKUNA && this.level() instanceof ServerLevel level && CullingGame.data(level.getServer()).gojoFreed) {
         health *= 0.6;
      }

      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(profile.damage());
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(profile.speed());
      this.getAttribute(Attributes.ARMOR).setBaseValue(profile.armor());
      Component name = profile.unique() || profile.scripted() ? profile.displayName() : Component.translatable("entity.cursed_domain.sorcerer_npc." + profile.id());
      this.setCustomName(name.copy().withStyle(profile.hostile() ? ChatFormatting.RED : ChatFormatting.AQUA));
      this.setCustomNameVisible(profile.nameVisible());
   }

   public void setHome(Vec3 home) {
      this.home = home;
      this.restrictTo(BlockPos.containing(home), 6);
   }

   public Component npcName() {
      return this.profile().displayName();
   }

   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel level && this.isAlive()) {
         this.serverTick(level);
      }
   }

   private void serverTick(ServerLevel level) {
      NpcProfile profile = this.profile();
      if (this.stale) {
         this.discard();
         return;
      }

      if (this.home != null && this.tickCount % 40 == 0 && this.position().distanceToSqr(this.home) > 144.0) {
         this.teleportTo(this.home.x, this.home.y, this.home.z);
         this.getNavigation().stop();
      }

      if (profile.boss()) {
         if (this.bar == null) {
            this.bar = new EntityBossBar(
               profile.displayName(), profile == NpcProfile.SUKUNA || profile == NpcProfile.KENJAKU ? BossBarColor.RED : BossBarColor.PURPLE
            );
         }

         this.bar.tick(this);
      }

      if (this.cooldown > 0) {
         this.cooldown--;
      }

      if (this.secondaryCooldown > 0) {
         this.secondaryCooldown--;
      }

      if (this.verdictTimer > 0 && --this.verdictTimer == 0) {
         this.verdict(level);
      }

      if (profile.ability() == NpcAbility.JACOBS_LADDER && this.tickCount % 40 == 0) {
         this.jacobsLadder(level);
      }

      LivingEntity target = this.getTarget();
      if (target != null && target.isAlive() && !this.leaving) {
         this.useAbility(level, profile, target);
      }
   }

   // ---------------------------------------------------------------- techniques

   private void useAbility(ServerLevel level, NpcProfile profile, LivingEntity target) {
      double dist = this.distanceTo(target);
      boolean sight = this.hasLineOfSight(target);
      Vec3 eye = this.getEyePosition();
      Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
      Vec3 dir = aim.subtract(eye).normalize();
      switch (profile.ability()) {
         case RANDOM:
            if (this.cooldown == 0 && sight && dist < 18.0) {
               boolean needles = this.random.nextBoolean();
               TechniqueProjectile.spawn(
                  level, this, needles ? ProjectileKind.NAIL : ProjectileKind.ENERGY_BOLT, eye, dir.scale(needles ? 1.6 : 1.1), needles ? 0.3F : 0.6F, needles ? 3.0F : 5.0F
               );
               this.cooldown = 70 + this.random.nextInt(40);
            }
            break;
         case NEEDLES:
            if (this.cooldown == 0 && sight && dist < 20.0) {
               for (int i = -1; i <= 1; i++) {
                  Vec3 spread = dir.add(dir.cross(new Vec3(0.0, 1.0, 0.0)).scale(i * 0.12));
                  TechniqueProjectile.spawn(level, this, ProjectileKind.NAIL, eye, spread.normalize().scale(1.7), 0.3F, 3.0F);
               }

               level.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.HOSTILE, 0.8F, 1.6F);
               this.cooldown = 50;
            }
            break;
         case DEADLY_SENTENCING:
            if (this.cooldown == 0 && dist < 14.0 && this.verdictTimer < 0) {
               this.openCourt(level, target);
               this.cooldown = 500;
            }
            break;
         case CONTRACT_RECREATION:
            if (this.cooldown == 0 && dist < 16.0) {
               this.say(level, "reggie_star.receipt", 20.0);
               Vec3 at = target.position();
               Vfx.burst(level, at.add(0.0, 1.0, 0.0), -1, 3.5F, 12);
               level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2F, 0.6F);
               Moves.areaDamage(this, at, 3.5, 7.0F, true);
               this.cooldown = 120;
            }
            break;
         case EXPLOSIONS:
            if (this.cooldown == 0 && dist < 14.0) {
               for (int i = 0; i < 3; i++) {
                  Vec3 at = target.position().add(this.random.nextGaussian() * 1.5, 0.5, this.random.nextGaussian() * 1.5);
                  Vfx.burst(level, at, -30166, 2.5F, 10);
                  level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y, at.z, 8, 0.6, 0.6, 0.6, 0.02);
                  Moves.areaDamage(this, at, 2.5, 4.0F, true);
               }

               level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5F, 0.6F);
               this.cooldown = 70;
            }
            break;
         case LIGHTNING:
            if (this.cooldown == 0 && sight && dist < 22.0) {
               Vfx.bolt(level, eye, aim, ProjectileKind.ELECTRIC.color(), 0.5F, 8);
               JjkDamage.hurt(target, JjkDamage.TECHNIQUE, this, this.phaseTwo ? 10.0F : 7.0F);
               level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 1.5F, 1.2F);
               this.cooldown = this.phaseTwo ? 25 : 50;
            }
            break;
         case GRANITE_BLAST:
            if (this.cooldown == 0 && sight && dist < 28.0) {
               this.say(level, "ryu_ishigori.granite_blast", 28.0);
               TechniqueProjectile.spawn(level, this, ProjectileKind.ENERGY_BOLT, eye, dir.scale(1.4), 1.8F, 12.0F);
               level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.2F, 0.8F);
               this.cooldown = 90;
            }
            break;
         case SKY_MANIPULATION:
            if (this.cooldown == 0 && dist < 10.0) {
               Vfx.ring(level, this.position().add(0.0, 1.0, 0.0), -7673601, 6.0F, 12);
               Moves.knockback(target, this.position(), 2.2, 1.0);
               JjkDamage.hurt(target, JjkDamage.TECHNIQUE, this, 4.0F);
               level.playSound(null, this.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 1.5F, 0.5F);
               this.cooldown = 80;
            }
            break;
         case SHIKIGAMI_SWARM:
            if (this.cooldown == 0 && sight && dist < 24.0) {
               for (int i = -2; i <= 2; i++) {
                  Vec3 spread = dir.add(dir.cross(new Vec3(0.0, 1.0, 0.0)).scale(i * 0.15)).add(0.0, 0.1, 0.0);
                  TechniqueProjectile.spawn(level, this, ProjectileKind.CROW, eye, spread.normalize().scale(1.2), 0.5F, 4.0F);
               }

               this.cooldown = 70;
            }
            break;
         case SWORDSMAN:
            if (this.cooldown == 0 && dist < 9.0 && dist > 2.5) {
               Vec3 to = target.position().subtract(dir.scale(1.2));
               Moves.teleport(this, to);
               Vfx.slash(level, aim, dir, -1, 2.0F, 6);
               JjkDamage.hurt(target, JjkDamage.TECHNIQUE, this, 9.0F);
               level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5F, 0.7F);
               this.cooldown = 80;
            }
            break;
         case CURSED_SPIRIT_MANIPULATION:
            if (this.cooldown == 0) {
               this.say(level, "kenjaku.summon", 32.0);

               for (int i = 0; i < 2; i++) {
                  Vec3 at = this.position().add(this.random.nextGaussian() * 3.0, 0.0, this.random.nextGaussian() * 3.0);
                  RampantCurse curse = RampantCurse.spawn(level, this.random.nextInt(3) == 0 ? CurseVariant.GRADE_2 : CurseVariant.GRADE_3, at, false);
                  if (curse != null) {
                     curse.setTarget(target);
                  }
               }

               Vfx.darkBurst(level, this.position().add(0.0, 1.0, 0.0), -9819606, 3.0F, 14);
               this.cooldown = 220;
            }

            if (this.secondaryCooldown == 0 && sight && dist < 30.0) {
               this.say(level, "kenjaku.uzumaki", 40.0);
               Vfx.implode(level, eye, -9819606, 3.0F, 20);
               TechniqueProjectile.spawn(level, this, ProjectileKind.SOUL_SPIKE, eye, dir.scale(0.9), 3.0F, 20.0F);
               level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 2.0F, 0.6F);
               this.secondaryCooldown = 320;
            }
            break;
         case SHRINE:
            if (this.cooldown == 0 && sight && dist < 24.0) {
               Vfx.slash(level, aim, new Vec3(this.random.nextGaussian(), this.random.nextGaussian() * 0.3, this.random.nextGaussian()), -3856, 1.6F, 6);
               JjkDamage.hurt(target, JjkDamage.TECHNIQUE, this, 8.0F);
               level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.5F, 0.6F);
               this.cooldown = 40;
            }

            if (this.secondaryCooldown == 0 && sight && dist < 32.0) {
               this.say(level, "sukuna.open", 48.0);
               TechniqueProjectile.spawn(level, this, ProjectileKind.DIVINE_FLAME, eye, dir.scale(1.0), 2.5F, 22.0F);
               level.playSound(null, this.blockPosition(), SoundEvents.BLAZE_BURN, SoundSource.HOSTILE, 2.0F, 0.5F);
               this.secondaryCooldown = 280;
            }
            break;
         default:
      }
   }

   private void openCourt(ServerLevel level, LivingEntity target) {
      Component title = Component.translatable("npc.cursed_domain.hiromi_higuruma.domain").withStyle(ChatFormatting.GOLD);
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (player.distanceToSqr(this.position()) < 576.0) {
            Kogane.title(player, title, Component.translatable("npc.cursed_domain.hiromi_higuruma.domain.sub"), 40);
         }
      }

      Vfx.ring(level, this.position().add(0.0, 0.2, 0.0), DomainStyle.DEADLY_SENTENCING.glow(), 10.0F, 30);
      Vfx.darkBurst(level, this.position().add(0.0, 1.0, 0.0), DomainStyle.DEADLY_SENTENCING.shell(), 8.0F, 20);
      level.playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.5F, 0.6F);
      target.addEffect(new MobEffectInstance(ModEffects.STUNNED, 40, 0, false, false));
      this.verdictTarget = target.getUUID();
      this.verdictTimer = 40;
   }

   private void verdict(ServerLevel level) {
      Entity entity = this.verdictTarget == null ? null : level.getEntity(this.verdictTarget);
      this.verdictTarget = null;
      this.verdictTimer = -1;
      if (entity instanceof LivingEntity target && target.isAlive() && this.distanceTo(target) < 24.0F) {
         float roll = this.random.nextFloat();
         if (roll < 0.5F) {
            this.say(level, "hiromi_higuruma.confiscation", 24.0);
            target.addEffect(new MobEffectInstance(ModEffects.BURNOUT, 600, 0, false, true, true));
            if (target instanceof ServerPlayer player) {
               EnergyManager.setCurrent(player, 0.0F);
               Vfx.screen(player, ScreenFxPayload.Kind.TINT, -1521542, 0.5F, 30);
            }
         } else if (roll < 0.85F) {
            this.say(level, "hiromi_higuruma.death_penalty", 24.0);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2, false, true));
            Vfx.pillar(level, this.position(), -1521542, 4.0F, 20);
         } else {
            this.say(level, "hiromi_higuruma.not_guilty", 24.0);
         }
      }
   }

   private void jacobsLadder(ServerLevel level) {
      List<CursedSpirit> curses = level.getEntitiesOfClass(CursedSpirit.class, this.getBoundingBox().inflate(16.0));

      for (CursedSpirit curse : curses) {
         if (curse.isAlive()) {
            Vfx.pillar(level, curse.position(), -1, 6.0F, 16);
            JjkDamage.hurt(curse, JjkDamage.TECHNIQUE, this, 1000.0F);
            this.say(level, "hana_kurusu.ladder", 20.0);
            break;
         }
      }
   }

   private void enterPhaseTwo(ServerLevel level) {
      this.phaseTwo = true;
      NpcProfile profile = this.profile();
      switch (profile.ability()) {
         case LIGHTNING:
            this.say(level, "hajime_kashimo.amber", 40.0);
            Vfx.pillar(level, this.position(), ProjectileKind.ELECTRIC.color(), 8.0F, 30);
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1, false, true));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1, false, true));
            break;
         case CURSED_SPIRIT_MANIPULATION:
            this.say(level, "kenjaku.antigravity", 40.0);
            Vfx.ring(level, this.position().add(0.0, 0.5, 0.0), -9819606, 10.0F, 20);
            List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0));

            for (LivingEntity e : near) {
               if (e != this && !(e instanceof CursedSpirit)) {
                  Moves.knockback(e, this.position(), 1.2, 1.6);
               }
            }
            break;
         case SHRINE:
            this.castDomain(level, ModTechniques.SHRINE.get(), "sukuna.shrine");
            break;
         default:
      }
   }

   private void castDomain(ServerLevel level, Object technique, String line) {
      if (technique instanceof Technique t) {
         Optional<DomainExpansion> domain = t.domain();
         if (domain.isPresent()) {
            this.say(level, line, 48.0);
            DomainManager.cast(this, domain.get(), DomainType.BARRIERLESS, this.position());
         }
      }
   }

   // ---------------------------------------------------------------- combat hooks

   public boolean doHurtTarget(Entity target) {
      NpcAbility ability = this.profile().ability();
      if (ability == NpcAbility.SUMO || ability == NpcAbility.COMEDIAN) {
         this.swing(InteractionHand.MAIN_HAND);
         Moves.knockback(target, this.position(), ability == NpcAbility.SUMO ? 2.5 : 1.0, 0.5);
         this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 0.8F);
         this.setTarget(null);
         return true;
      } else {
         return super.doHurtTarget(target);
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!(this.level() instanceof ServerLevel level) || this.leaving) {
         return super.hurt(source, amount);
      } else {
         NpcProfile profile = this.profile();
         Entity attacker = source.getEntity();
         ServerPlayer player = playerBehind(attacker);
         if (profile == NpcProfile.MASAMICHI_YAGA || profile == NpcProfile.YOSHINOBU_GAKUGANJI) {
            if (player != null) {
               JujutsuHigh.principalHit(player, this);
            }

            return profile == NpcProfile.MASAMICHI_YAGA && !(Boolean)ServerConfig.YAGA_INVULNERABLE.get() && super.hurt(source, amount);
         } else if (profile.defeat() == NpcProfile.Defeat.INVULNERABLE && attacker != null) {
            if (player != null && this.random.nextInt(3) == 0) {
               this.say(level, profile.id() + ".immune." + (1 + this.random.nextInt(3)), 16.0);
            }

            if (attacker instanceof LivingEntity living && profile.ability() == NpcAbility.SUMO) {
               Moves.knockback(living, this.position(), 2.0, 0.4);
            }

            return false;
         } else if (profile.ability() == NpcAbility.FORESIGHT && attacker != null && this.random.nextFloat() < 0.3F) {
            this.say(level, "charles_bernard.dodge", 16.0);
            Vfx.sparks(level, this.position().add(0.0, 1.0, 0.0), -1, 0.8F, 8);
            return false;
         } else {
            boolean hurt = super.hurt(source, amount);
            if (hurt && this.isAlive()) {
               if (player != null) {
                  this.lastAttacker = player.getUUID();
               }

               if (!this.phaseTwo && this.getHealth() < this.getMaxHealth() * 0.5F) {
                  this.enterPhaseTwo(level);
               }

               if (this.shibuyaMode && profile == NpcProfile.KENJAKU && this.getHealth() < this.getMaxHealth() * 0.45F) {
                  this.leaving = true;
                  ShibuyaIncident.kenjakuEscapes(level, this);
               } else if (this.getHealth() < this.getMaxHealth() * 0.25F) {
                  this.tryYield(level);
               }
            }

            return hurt;
         }
      }
   }

   @Nullable
   private static ServerPlayer playerBehind(@Nullable Entity attacker) {
      if (attacker instanceof ServerPlayer player) {
         return player;
      } else if (attacker instanceof ShikigamiEntity shikigami && shikigami.summoner() instanceof ServerPlayer owner) {
         return owner;
      } else {
         return null;
      }
   }

   private void tryYield(ServerLevel level) {
      NpcProfile profile = this.profile();
      CullingGameData data = CullingGame.data(level.getServer());
      ServerPlayer winner = this.lastAttacker == null ? null : level.getServer().getPlayerList().getPlayer(this.lastAttacker);
      if (profile.defeat() == NpcProfile.Defeat.HIGURUMA) {
         if (!data.has(CullingRule.POINT_TRANSFER) && data.active()) {
            this.say(level, "hiromi_higuruma.yield", 32.0);
            CullingGame.addRule(level.getServer(), CullingRule.POINT_TRANSFER, profile.displayName().getString());
            this.points = Math.max(0, this.points - CullingGameData.ruleCost());
         } else {
            this.say(level, "hiromi_higuruma.yield_transfer", 32.0);
         }

         this.handOver(level, winner);
         this.leave(level);
      } else if (profile.defeat() == NpcProfile.Defeat.YIELDS && data.active() && data.has(CullingRule.POINT_TRANSFER) && winner != null) {
         this.say(level, profile.unique() ? profile.id() + ".yield" : "generic.yield", 32.0);
         this.handOver(level, winner);
         this.leave(level);
      }
   }

   private void handOver(ServerLevel level, @Nullable ServerPlayer winner) {
      if (winner != null && this.points > 0) {
         CullingGame.receiveTransfer(winner, this.points, this.npcName());
         this.points = 0;
      }
   }

   private void leave(ServerLevel level) {
      this.leaving = true;
      CullingGame.onNpcGone(level.getServer(), this, true);
      level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.02);
      level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.8F);
      if (this.bar != null) {
         this.bar.close();
      }

      this.discard();
   }

   /** Kenjaku slipping away from Shibuya. */
   public void vanish(ServerLevel level) {
      this.leaving = true;
      if (this.bar != null) {
         this.bar.close();
      }

      Vfx.darkBurst(level, this.position().add(0.0, 1.0, 0.0), -9819606, 6.0F, 30);
      level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 60, 1.0, 1.5, 1.0, 0.03);
      this.discard();
   }

   public void die(DamageSource source) {
      super.die(source);
      if (this.bar != null) {
         this.bar.close();
      }

      if (this.level() instanceof ServerLevel level) {
         CullingGame.onNpcGone(level.getServer(), this, false);
      }
   }

   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.level() instanceof ServerLevel level && player instanceof ServerPlayer sp && hand == InteractionHand.MAIN_HAND) {
         NpcProfile profile = this.profile();
         if (profile == NpcProfile.HANA_KURUSU) {
            CullingGame.angelInteract(sp, this);
            return InteractionResult.SUCCESS;
         } else if (profile == NpcProfile.FUMIHIKO_TAKABA) {
            this.say(level, "fumihiko_takaba.joke." + (1 + this.random.nextInt(5)), 16.0);
            return InteractionResult.SUCCESS;
         } else if (profile == NpcProfile.ROKUJUSHI_MIYO) {
            this.sumoLesson(level, sp);
            return InteractionResult.SUCCESS;
         } else if (profile == NpcProfile.MASAMICHI_YAGA || profile == NpcProfile.YOSHINOBU_GAKUGANJI) {
            JujutsuHigh.interact(sp, this);
            return InteractionResult.SUCCESS;
         } else if (profile == NpcProfile.KENJAKU_HIDEOUT || profile == NpcProfile.KENJAKU) {
            Incarnation.kenjakuInteract(sp, this);
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   private void sumoLesson(ServerLevel level, ServerPlayer player) {
      long now = level.getGameTime();
      Long last = MIYO_LESSONS.get(player.getUUID());
      if (last != null && now - last < 24000L) {
         Kogane.speech(player, "entity.cursed_domain.sorcerer_npc.rokujushi_miyo", Component.translatable("npc.cursed_domain.rokujushi_miyo.again"), ChatFormatting.AQUA);
      } else {
         MIYO_LESSONS.put(player.getUUID(), now);
         Kogane.speech(player, "entity.cursed_domain.sorcerer_npc.rokujushi_miyo", Component.translatable("npc.cursed_domain.rokujushi_miyo.lesson"), ChatFormatting.AQUA);
         player.addEffect(new MobEffectInstance(ModEffects.ZONE, 3600, 0, false, true, true));
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 0, false, true));
         Vfx.ring(level, player.position().add(0.0, 0.1, 0.0), -1, 3.0F, 20);
      }
   }

   /** Speaks a line to nearby players. */
   public void say(ServerLevel level, String key, double range) {
      Component line = Component.translatable("npc.cursed_domain." + key);
      List<ServerPlayer> players = level.players();

      for (ServerPlayer player : players) {
         if (player.distanceToSqr(this.position()) < range * range) {
            Kogane.speech(player, "entity.cursed_domain.sorcerer_npc." + this.profile().id(), line, this.profile().hostile() ? ChatFormatting.RED : ChatFormatting.AQUA);
         }
      }
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ZOMBIE_VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ZOMBIE_VILLAGER_DEATH;
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putString("profile", this.profile().id());
      tag.putInt("points", this.points);
      tag.putBoolean("shibuya", this.shibuyaMode);
      tag.putBoolean("phase_two", this.phaseTwo);
      if (this.home != null) {
         tag.putDouble("home_x", this.home.x);
         tag.putDouble("home_y", this.home.y);
         tag.putDouble("home_z", this.home.z);
      }
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      NpcProfile profile = NpcProfile.byId(tag.getString("profile"));
      this.entityData.set(PROFILE, (profile == null ? NpcProfile.AWAKENED_PLAYER : profile).ordinal());
      this.points = tag.getInt("points");
      this.shibuyaMode = tag.getBoolean("shibuya");
      this.phaseTwo = tag.getBoolean("phase_two");
      if (tag.contains("home_x")) {
         this.setHome(new Vec3(tag.getDouble("home_x"), tag.getDouble("home_y"), tag.getDouble("home_z")));
      }

      this.stale = profile == NpcProfile.CURSED_CORPSE;
      float health = this.getHealth();
      this.applyProfile();
      this.setHealth(health);
   }
}
