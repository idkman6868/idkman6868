package com.curseddomain.gametest;

import com.curseddomain.config.ServerConfig;
import com.curseddomain.entity.cursedspirit.CurseVisibility;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import com.curseddomain.item.LetterHints;
import com.curseddomain.item.RecruitmentLetterItem;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModItems;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.sorcerer.AwakeningCause;
import com.curseddomain.sorcerer.AwakeningManager;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.sorcerer.TechniqueRoller;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueCategory;
import com.curseddomain.world.SchoolLocation;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("cursed_domain")
@PrefixGameTestTemplate(false)
public final class StoryGameTests {
   private StoryGameTests() {
   }

   @GameTest(
      template = "empty"
   )
   public static void rollIsSecretAndRespectsTheRules(GameTestHelper h) {
      RandomSource random = RandomSource.create(42L);

      for (int i = 0; i < 500; i++) {
         TechniqueRoller.Result r = TechniqueRoller.roll(random);
         Technique t = r.technique();
         h.assertFalse(t.npcOnly(), "rolled an NPC-only technique " + t.id());
         h.assertFalse(t.category() == TechniqueCategory.CURSES_AND_CURSE_USERS, "rolled a curse technique " + t.id());
         h.assertTrue(t.inRandomRoll(), "rolled a story-only technique " + t.id());
         if (t == ModTechniques.LIMITLESS.get()) {
            h.assertTrue(r.traits().contains(InnateTrait.SIX_EYES), "Limitless must come with Six Eyes");
         }

         h.assertFalse(
            r.traits().contains(InnateTrait.HEAVENLY_RESTRICTION_ENERGY) && r.traits().contains(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL),
            "both restrictions at once"
         );
      }

      ServerPlayer p = TestSupport.player(h);
      TechniqueRoller.rollFor(p);
      SorcererData data = SorcererManager.get(p);
      h.assertTrue(data.rolled() && data.technique().isPresent(), "the roll stores a technique");
      h.assertTrue(data.clientView().technique().isEmpty(), "the client must not learn the roll");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void cursesAreInvisibleToOrdinaryPeople(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      Grade4Curse curse = (Grade4Curse)h.spawn((EntityType)ModEntities.GRADE_4_CURSE.get(), 4, 1, 4);
      h.assertFalse(curse.broadcastToPlayer(p), "an ordinary person must not receive the curse");
      SorcererManager.setStatus(p, SorcererStatus.AWAKENED);
      h.assertTrue(curse.broadcastToPlayer(p), "an awakened player sees curses");
      SorcererManager.setStatus(p, SorcererStatus.NON_SORCERER);
      SorcererManager.addTrait(p, InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL);
      h.assertTrue(CurseVisibility.canSee(p), "physical restriction sees curses unaided");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void repeatedCurseHitsAwaken(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      Grade4Curse curse = (Grade4Curse)h.spawn((EntityType)ModEntities.GRADE_4_CURSE.get(), 4, 1, 4);
      int hits = (Integer)ServerConfig.CURSE_HITS_TO_AWAKEN.get();

      for (int i = 0; i < hits; i++) {
         p.invulnerableTime = 0;
         p.setHealth(20.0F);
         p.hurt(h.getLevel().damageSources().mobAttack(curse), 1.0F);
         if (i < hits - 1) {
            h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.NON_SORCERER, "awakened too early at hit " + (i + 1));
         }
      }

      h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.AWAKENED, "five unseen hits awaken");
      h.assertTrue(hasItem(p, ((RecruitmentLetterItem)ModItems.RECRUITMENT_LETTER.get()).getDefaultInstance()), "awakening delivers the letter");
      h.assertTrue(SorcererManager.get(p).isUnlocked("universal:reinforcement"), "reinforcement unlocked");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void nearDeathAwakens(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      Grade4Curse curse = (Grade4Curse)h.spawn((EntityType)ModEntities.GRADE_4_CURSE.get(), 4, 1, 4);
      p.setHealth(4.5F);
      p.hurt(h.getLevel().damageSources().mobAttack(curse), 2.0F);
      h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.AWAKENED, "a near-death curse attack awakens");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void carryingACursedObjectAwakens(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      p.getInventory().add(new ItemStack((ItemLike)ModItems.CURSED_OBJECT.get()));
      ItemStack relic = p.getInventory().getItem(0);
      p.tickCount = 10;
      relic.getItem().inventoryTick(relic, h.getLevel(), p, 0, false);
      h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.AWAKENED, "carrying a cursed object awakens");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void readingTheLetterMakesAnApplicant(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      ItemStack letter = new ItemStack((ItemLike)ModItems.RECRUITMENT_LETTER.get());
      p.setItemInHand(InteractionHand.MAIN_HAND, letter);
      letter.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
      h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.NON_SORCERER, "ordinary people cannot read it");
      AwakeningManager.awaken(p, AwakeningCause.COMMAND);
      letter.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
      h.assertTrue(SorcererManager.get(p).status() == SorcererStatus.APPLICANT, "reading the letter makes an applicant");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void schoolSitsInTheConfiguredBand(GameTestHelper h) {
      BlockPos school = SchoolLocation.compute(h.getLevel().getSeed());
      double distance = Math.sqrt((double)school.getX() * school.getX() + (double)school.getZ() * school.getZ());
      int min = (Integer)ServerConfig.JUJUTSU_HIGH_MIN_DISTANCE.get();
      int max = (Integer)ServerConfig.JUJUTSU_HIGH_MAX_DISTANCE.get();
      h.assertTrue(distance >= min - 24 && distance <= max + 24, "school at " + (int)distance + " blocks, band " + min + "-" + max);
      h.assertTrue(school.equals(SchoolLocation.compute(h.getLevel().getSeed())), "placement is deterministic");
      h.assertTrue(Math.abs(LetterHints.yawTowards(0.0, 0.0, 0.0, 10.0)) < 0.01, "yaw 0 is south");
      h.assertTrue(Math.abs(LetterHints.yawTowards(0.0, 0.0, -10.0, 0.0) - 90.0F) < 0.01, "yaw 90 is west");
      h.succeed();
   }

   @GameTest(
      template = "empty"
   )
   public static void energyRestrictionMakesTheBodyFrail(GameTestHelper h) {
      ServerPlayer p = TestSupport.player(h);
      float before = p.getMaxHealth();
      SorcererManager.applyRoll(p, ModTechniques.RATIO.getId(), EnumSet.of(InnateTrait.HEAVENLY_RESTRICTION_ENERGY));
      float penalty = ((Double)ServerConfig.HR_ENERGY_HEALTH_PENALTY.get()).floatValue();
      h.assertTrue(Math.abs(p.getMaxHealth() - (before - penalty)) < 0.01, "max health " + p.getMaxHealth());
      SorcererManager.applyRoll(p, ModTechniques.RATIO.getId(), EnumSet.noneOf(InnateTrait.class));
      h.assertTrue(Math.abs(p.getMaxHealth() - before) < 0.01, "penalty removed with the trait");
      h.succeed();
   }

   private static boolean hasItem(Player player, ItemStack stack) {
      return player.getInventory().items.stream().anyMatch(s -> ItemStack.isSameItem(s, stack));
   }
}
