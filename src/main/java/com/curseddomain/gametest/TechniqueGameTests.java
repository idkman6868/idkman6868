package com.curseddomain.gametest;

import com.curseddomain.domain.DomainInstance;
import com.curseddomain.domain.DomainManager;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.shikigami.DivineDogEntity;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityManager;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

@GameTestHolder("cursed_domain")
@PrefixGameTestTemplate(false)
public final class TechniqueGameTests {
   private TechniqueGameTests() {
   }

   static ServerPlayer sorcerer(GameTestHelper h, ResourceLocation technique) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setTechnique(p, technique);
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      SorcererManager.setGrade(p, Grade.SPECIAL_GRADE);
      EnergyManager.setCurrent(p, EnergyManager.get(p).max());
      return p;
   }

   static int index(Technique technique, String name) {
      List<Ability> list = technique.abilities();

      for (int i = 0; i < list.size(); i++) {
         if (list.get(i).name().equals(name)) {
            return i;
         }
      }

      throw new IllegalArgumentException(name);
   }

   @GameTest(
      template = "empty"
   )
   public static void castingSpendsEnergyAndStartsCooldown(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.STRAW_DOLL.getId());
      int nails = index((Technique)ModTechniques.STRAW_DOLL.get(), "hammer_nails");
      float before = EnergyManager.get(p).current();
      AbilityManager.input(p, nails, true);
      float after = EnergyManager.get(p).current();
      h.assertTrue(after < before, "casting spends energy");
      AbilityManager.input(p, nails, true);
      h.assertTrue(Math.abs(EnergyManager.get(p).current() - after) < 0.001, "the cooldown blocks a second cast");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void unenrolledCannotCast(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setTechnique(p, ModTechniques.STRAW_DOLL.getId());
      SorcererManager.setStatus(p, SorcererStatus.AWAKENED);
      EnergyManager.setCurrent(p, EnergyManager.get(p).max());
      h.assertTrue(
         AbilityManager.abilities(p).stream().noneMatch(a -> a.technique() == ModTechniques.STRAW_DOLL.get()),
         "the technique's abilities stay locked until enrollment"
      );
      h.assertTrue(AbilityManager.abilities(p).stream().anyMatch(a -> a.name().equals("black_flash")), "universal Black Flash focus is available once awakened");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void reversalRedNeedsRct(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.LIMITLESS.getId());
      int red = index((Technique)ModTechniques.LIMITLESS.get(), "reversal_red");
      float before = EnergyManager.get(p).current();
      AbilityManager.input(p, red, true);
      AbilityManager.input(p, red, false);
      h.assertTrue(Math.abs(EnergyManager.get(p).current() - before) < 0.001, "Red without RCT must fail");
      SorcererManager.unlock(p, "rct");
      AbilityManager.input(p, red, true);
      AbilityManager.input(p, red, false);
      h.assertTrue(EnergyManager.get(p).current() < before, "Red works once RCT is learned");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void infinityStopsHits(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.LIMITLESS.getId());
      Zombie zombie = (Zombie)h.spawn(EntityType.ZOMBIE, 3, 1, 3);
      AbilityManager.input(p, index((Technique)ModTechniques.LIMITLESS.get(), "infinity"), true);
      float health = p.getHealth();
      p.hurt(h.getLevel().damageSources().mobAttack(zombie), 5.0F);
      h.assertTrue(p.getHealth() == health, "Infinity stops the blow");
      AbilityManager.input(p, index((Technique)ModTechniques.LIMITLESS.get(), "infinity"), true);
      p.invulnerableTime = 0;
      p.hurt(h.getLevel().damageSources().mobAttack(zombie), 5.0F);
      h.assertTrue(p.getHealth() < health, "without Infinity the blow lands");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void runningDryExhausts(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.STRAW_DOLL.getId());
      int nails = index((Technique)ModTechniques.STRAW_DOLL.get(), "hammer_nails");
      float cost = EnergyManager.effectiveCost(p, ((Technique)ModTechniques.STRAW_DOLL.get()).abilities().get(nails).stats().cost());
      EnergyManager.setCurrent(p, cost + 0.2F);
      AbilityManager.input(p, nails, true);
      h.assertTrue(p.hasEffect(ModEffects.EXHAUSTED), "spending the last drop exhausts");
      AbilityManager.resetCooldowns(p);
      EnergyManager.setCurrent(p, 100.0F);
      float before = EnergyManager.get(p).current();
      AbilityManager.input(p, nails, true);
      h.assertTrue(Math.abs(EnergyManager.get(p).current() - before) < 0.001, "an exhausted sorcerer cannot cast");
      h.succeed();
   }

   @GameTest(
      template = "empty",
      timeoutTicks = 60,
      batch = "blue"
   )
   public static void blueDragsThingsIn(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.LIMITLESS.getId());
      p.setYRot(0.0F);
      p.setXRot(0.0F);
      Zombie zombie = (Zombie)h.spawn(EntityType.ZOMBIE, 2, 1, 9);
      Vec3 start = zombie.position();
      AbilityManager.input(p, index((Technique)ModTechniques.LIMITLESS.get(), "lapse_blue"), true);
      h.runAfterDelay(30L, () -> {
         h.assertTrue(zombie.position().distanceTo(start) > 0.5 || !zombie.isAlive(), "Blue should move the zombie");
         h.succeed();
      });
   }

   @GameTest(
      template = "empty",
      timeoutTicks = 200,
      batch = "domain_lifecycle"
   )
   public static void domainExpandsTrapsAndEnds(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.LIMITLESS.getId());
      SorcererManager.unlock(p, "domain");
      Zombie zombie = (Zombie)h.spawn(EntityType.ZOMBIE, 5, 1, 5);
      h.assertTrue(DomainManager.tryCast(p), "cast should start");
      h.runAfterDelay(80L, () -> {
         DomainInstance d = DomainManager.of(p).orElse(null);
         h.assertTrue(d != null && d.live(), "domain active after hand signs + forming");
         h.assertTrue(d.trapped.contains(zombie.getUUID()), "the zombie is trapped inside");
         h.assertTrue(zombie.hasEffect(ModEffects.STUNNED), "Unlimited Void's sure-hit stuns");
         h.assertTrue(d.barrier != null && d.barrier.isAlive(), "the barrier entity exists");
         DomainManager.end(d, "dismissed");
         h.runAfterDelay(40L, () -> {
            h.assertTrue(DomainManager.of(p).isEmpty(), "the domain is gone after collapsing");
            h.assertTrue(p.hasEffect(ModEffects.BURNOUT), "the caster is burnt out");
            h.succeed();
         });
      });
   }

   @GameTest(
      template = "empty",
      timeoutTicks = 200,
      batch = "domain_counter"
   )
   public static void simpleDomainBlocksTheSureHit(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.LIMITLESS.getId());
      SorcererManager.unlock(p, "domain");
      Zombie zombie = (Zombie)h.spawn(EntityType.ZOMBIE, 5, 1, 5);
      zombie.addEffect(new MobEffectInstance(ModEffects.SIMPLE_DOMAIN, 400));
      DomainManager.tryCast(p);
      h.runAfterDelay(80L, () -> {
         h.assertFalse(zombie.hasEffect(ModEffects.STUNNED), "Simple Domain must cancel the sure-hit");
         DomainManager.of(p).ifPresent(d -> DomainManager.end(d, "dismissed"));
         h.succeed();
      });
   }

   @GameTest(
      template = "empty",
      timeoutTicks = 300,
      batch = "domain_clash"
   )
   public static void overlappingDomainsClash(GameTestHelper h) {
      ServerPlayer gojo = sorcerer(h, ModTechniques.LIMITLESS.getId());
      ServerPlayer jogo = sorcerer(h, ModTechniques.DISASTER_FLAMES.getId());
      SorcererManager.unlock(gojo, "domain");
      SorcererManager.unlock(jogo, "domain");
      DomainManager.tryCast(gojo);
      h.runAfterDelay(70L, () -> DomainManager.tryCast(jogo));
      h.runAfterDelay(140L, () -> {
         DomainInstance a = DomainManager.of(gojo).orElse(null);
         DomainInstance b = DomainManager.of(jogo).orElse(null);
         h.assertTrue(a != null && b != null && a.clash != null && a.clash == b.clash, "the two domains should be clashing");
      });
      h.runAfterDelay(260L, () -> {
         boolean gojoAlive = DomainManager.of(gojo).map(DomainInstance::live).orElse(false);
         boolean jogoAlive = DomainManager.of(jogo).map(DomainInstance::live).orElse(false);
         h.assertTrue(gojoAlive ^ jogoAlive, "exactly one domain survives the clash");
         DomainManager.all().forEach(d -> DomainManager.end(d, "dismissed"));
         h.succeed();
      });
   }

   @GameTest(
      template = "empty"
   )
   public static void divineDogsComeAndGo(GameTestHelper h) {
      ServerPlayer p = sorcerer(h, ModTechniques.TEN_SHADOWS.getId());
      int dogs = index((Technique)ModTechniques.TEN_SHADOWS.get(), "divine_dogs");
      AbilityManager.input(p, dogs, true);
      List<DivineDogEntity> summoned = h.getLevel().getEntitiesOfClass(DivineDogEntity.class, p.getBoundingBox().inflate(8.0));
      h.assertTrue(summoned.size() == 2, "two dogs, found " + summoned.size());
      h.assertTrue(summoned.stream().allMatch(d -> p.getUUID().equals(d.getOwnerUUID())), "the dogs belong to the summoner");
      AbilityManager.resetCooldowns(p);
      AbilityManager.input(p, dogs, true);
      h.assertTrue(summoned.stream().allMatch(Entity::isRemoved), "using it again recalls them");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void everyTierATechniqueIsBuilt(GameTestHelper h) {
      for (DeferredHolder<Technique, Technique> holder : List.of(
         ModTechniques.LIMITLESS,
         ModTechniques.TEN_SHADOWS,
         ModTechniques.STRAW_DOLL,
         ModTechniques.CURSED_SPEECH,
         ModTechniques.BOOGIE_WOOGIE,
         ModTechniques.BLOOD_MANIPULATION,
         ModTechniques.CONSTRUCTION,
         ModTechniques.RATIO,
         ModTechniques.HEAVENLY_RESTRICTION,
         ModTechniques.HEAVENLY_RESTRICTION_FULL,
         ModTechniques.SHRINE,
         ModTechniques.IDLE_TRANSFIGURATION,
         ModTechniques.DISASTER_FLAMES,
         ModTechniques.NEW_SHADOW_STYLE
      )) {
         Technique t = (Technique)holder.get();
         h.assertTrue(t.isImplemented(), t.id() + " is still a placeholder");
         h.assertTrue(t.abilities().size() >= 3, t.id() + " has too few abilities");
      }

      for (DeferredHolder<Technique, Technique> holder : List.of(
         ModTechniques.LIMITLESS, ModTechniques.TEN_SHADOWS, ModTechniques.SHRINE, ModTechniques.IDLE_TRANSFIGURATION, ModTechniques.DISASTER_FLAMES
      )) {
         h.assertTrue(((Technique)holder.get()).domain().isPresent(), holder.getId() + " needs its domain");
      }

      h.assertTrue(ModEntities.DIVINE_DOG.get() != null, "registries");
      h.succeed();
   }
}
