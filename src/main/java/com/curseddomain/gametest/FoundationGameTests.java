package com.curseddomain.gametest;

import com.curseddomain.config.ServerConfig;
import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.energy.CursedEnergyNature;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.network.payload.SorcererSyncPayload;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueRegistry;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("cursed_domain")
@PrefixGameTestTemplate(false)
public final class FoundationGameTests {
   private FoundationGameTests() {
   }

   @GameTest(
      template = "empty"
   )
   public static void ordinaryPersonHasNoEnergy(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      EnergyManager.recalculate(p);
      h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.NON_SORCERER, "new players start as ordinary people");
      h.assertTrue(EnergyManager.get(p).max() == 0.0F, "ordinary people have no cursed energy");
      h.assertFalse(EnergyManager.tryConsume(p, 1.0F), "ordinary people cannot spend energy");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void awakeningGivesTheUngradedPool(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.AWAKENED);
      float expected = ((Double)ServerConfig.gradeStats(Grade.UNGRADED).maxEnergy().get()).floatValue();
      h.assertTrue(TestSupport.near(EnergyManager.get(p).max(), expected), "awakened max should be the ungraded pool " + expected);
      h.assertTrue(SorcererManager.get(p).grade() == Grade.UNGRADED, "no grade before enrollment");
      h.assertFalse(SorcererManager.setGrade(p, Grade.GRADE_3), "grades need enrollment");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void techniqueStaysSecretUntilEnrollment(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setTechnique(p, ModTechniques.LIMITLESS.getId());
      SorcererManager.addTrait(p, InnateTrait.SIX_EYES);
      SorcererManager.setStatus(p, SorcererStatus.AWAKENED);
      SorcererSyncPayload hidden = SorcererManager.get(p).clientView();
      h.assertTrue(hidden.technique().isEmpty() && hidden.traits().isEmpty(), "client must not learn the technique before the revelation");
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      SorcererSyncPayload shown = SorcererManager.get(p).clientView();
      h.assertTrue(SorcererManager.get(p).grade() == Grade.GRADE_4, "enrollment starts at Grade 4");
      h.assertTrue(shown.technique().equals(Optional.of(ModTechniques.LIMITLESS.getId())), "enrollment reveals the technique");
      h.assertTrue(shown.traits().contains(InnateTrait.SIX_EYES), "enrollment reveals traits");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void spendingRespectsControlAndOutput(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      CursedEnergyData e = EnergyManager.get(p);
      EnergyManager.setCurrent(p, e.max());
      float before = e.current();
      float multiplier = EnergyManager.effectiveCost(p, 1.0F);
      h.assertTrue(multiplier < 1.0F, "control should discount costs");
      h.assertTrue(EnergyManager.tryConsume(p, 20.0F), "a small cost should go through");
      h.assertTrue(TestSupport.near(e.current(), before - 20.0F * multiplier), "spent " + (before - e.current()) + ", expected " + 20.0F * multiplier);
      float tooBig = e.output() / multiplier + 5.0F;
      h.assertFalse(EnergyManager.tryConsume(p, tooBig), "a burst above output must fail");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void sixEyesMakesCostsNearZero(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      float normal = EnergyManager.effectiveCost(p, 100.0F);
      SorcererManager.addTrait(p, InnateTrait.SIX_EYES);
      float sixEyes = EnergyManager.effectiveCost(p, 100.0F);
      float factor = ((Double)ServerConfig.SIX_EYES_COST_MULTIPLIER.get()).floatValue();
      h.assertTrue(TestSupport.near(sixEyes, normal * factor), "six eyes cost " + sixEyes + ", expected " + normal * factor);
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void heavenlyRestrictionTracksTheTechnique(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      SorcererManager.setTechnique(p, ModTechniques.HEAVENLY_RESTRICTION.getId());
      SorcererData s = SorcererManager.get(p);
      h.assertTrue(s.hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL), "the technique grants the trait");
      h.assertTrue(EnergyManager.get(p).max() == 0.0F, "physical restriction has no cursed energy");
      h.assertTrue(s.canSeeCurses(), "physical restriction still sees curses");
      SorcererManager.setTechnique(p, ModTechniques.LIMITLESS.getId());
      h.assertFalse(s.hasTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL), "switching technique removes the granted trait");
      h.assertTrue(EnergyManager.get(p).max() > 0.0F, "energy returns without the restriction");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void energyRestrictionMultipliesThePool(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      float base = EnergyManager.get(p).max();
      SorcererManager.addTrait(p, InnateTrait.HEAVENLY_RESTRICTION_ENERGY);
      float expected = base * ((Double)ServerConfig.HR_ENERGY_POOL_MULTIPLIER.get()).floatValue();
      h.assertTrue(TestSupport.near(EnergyManager.get(p).max(), expected), "pool " + EnergyManager.get(p).max() + ", expected " + expected);
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void natureFollowsTheTechnique(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      SorcererManager.setTechnique(p, ModTechniques.MYTHICAL_BEAST_AMBER.getId());
      h.assertTrue(EnergyManager.get(p).nature() == CursedEnergyNature.ELECTRIC, "Kashimo's technique has electric energy");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void combatTagIsRecorded(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setStatus(p, SorcererStatus.AWAKENED);
      h.assertFalse(EnergyManager.inCombat(p), "not in combat at first");
      EnergyManager.markCombat(p);
      h.assertTrue(EnergyManager.inCombat(p), "damage tags the player");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void dataSurvivesSaveAndLoad(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      SorcererManager.setTechnique(p, ModTechniques.TEN_SHADOWS.getId());
      SorcererManager.setStatus(p, SorcererStatus.STUDENT);
      SorcererManager.setGrade(p, Grade.SEMI_GRADE_1);
      SorcererManager.addTrait(p, InnateTrait.VESSEL);
      SorcererManager.unlock(p, "universal:simple_domain");
      EnergyManager.setCurrent(p, 123.0F);
      RegistryAccess provider = h.getLevel().registryAccess();
      CompoundTag sorcererTag = SorcererManager.get(p).serializeNBT(provider);
      CompoundTag energyTag = EnergyManager.get(p).serializeNBT(provider);
      SorcererData s = new SorcererData();
      s.deserializeNBT(provider, sorcererTag);
      CursedEnergyData e = new CursedEnergyData();
      e.deserializeNBT(provider, energyTag);
      h.assertTrue(s.status() == SorcererStatus.STUDENT && s.grade() == Grade.SEMI_GRADE_1, "status and grade persist");
      h.assertTrue(s.techniqueId().equals(Optional.of(ModTechniques.TEN_SHADOWS.getId())), "technique persists");
      h.assertTrue(s.techniqueRevealed() && s.hasTrait(InnateTrait.VESSEL), "reveal flag and traits persist");
      h.assertTrue(s.isUnlocked("universal:simple_domain"), "unlocks persist");
      h.assertTrue(TestSupport.near(e.current(), 123.0F), "current energy persists");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void catalogueIsComplete(GameTestHelper h) {
      h.assertTrue(TechniqueRegistry.REGISTRY.size() == 61, "expected 61 techniques, found " + TechniqueRegistry.REGISTRY.size());
      h.assertTrue(((Technique)ModTechniques.BODY_SWAP.get()).npcOnly(), "body swap is NPC-only");
      h.assertTrue(((Technique)ModTechniques.SHRINE.get()).requiredTrait().equals(Optional.of(InnateTrait.VESSEL)), "shrine needs a vessel");
      h.assertFalse(((Technique)ModTechniques.PANDA_CORES.get()).inRandomRoll(), "panda cores are a story unlock");

      for (Technique t : TechniqueRegistry.REGISTRY) {
         h.assertTrue(t.id().getNamespace().equals("cursed_domain"), "foreign technique " + t.id());
      }

      h.succeed();
   }
}
