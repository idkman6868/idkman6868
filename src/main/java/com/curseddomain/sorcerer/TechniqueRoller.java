package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.config.CommonConfig;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueCategory;
import com.curseddomain.technique.TechniqueRarity;
import com.curseddomain.technique.TechniqueRegistry;
import com.curseddomain.util.WeightedPicker;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class TechniqueRoller {
   private TechniqueRoller() {
   }

   public static TechniqueRoller.Result roll(RandomSource random) {
      EnumSet<InnateTrait> traits = EnumSet.noneOf(InnateTrait.class);
      rollTrait(random, traits, InnateTrait.SIX_EYES, (Double)ServerConfig.SIX_EYES_CHANCE.get());
      rollTrait(random, traits, InnateTrait.VESSEL, (Double)ServerConfig.VESSEL_CHANCE.get());
      rollTrait(random, traits, InnateTrait.HEAVENLY_RESTRICTION_ENERGY, (Double)ServerConfig.HR_ENERGY_CHANCE.get());
      rollTrait(random, traits, InnateTrait.DEATH_PAINTING, (Double)ServerConfig.DEATH_PAINTING_CHANCE.get());
      List<Technique> pool = candidates(traits);
      List<TechniqueRarity> tiers = Arrays.stream(TechniqueRarity.values()).filter(r -> pool.stream().anyMatch(t -> t.rarity() == r)).toList();
      TechniqueRarity tier = WeightedPicker.pick(tiers, r -> ((Integer)ServerConfig.RARITY_WEIGHTS.get(r).get()).intValue(), random.nextDouble());
      if (tier == null) {
         tier = tiers.isEmpty() ? TechniqueRarity.COMMON : tiers.getFirst();
      }

      TechniqueRarity chosenTier = tier;
      List<Technique> inTier = pool.stream().filter(t -> t.rarity() == chosenTier).toList();
      Technique technique = inTier.isEmpty() ? (Technique)ModTechniques.NEW_SHADOW_STYLE.get() : inTier.get(random.nextInt(inTier.size()));
      if (technique == ModTechniques.LIMITLESS.get() && (Boolean)ServerConfig.SIX_EYES_WITH_LIMITLESS.get()) {
         traits.add(InnateTrait.SIX_EYES);
      }

      technique.grantedTrait().ifPresent(traits::add);
      if (traits.contains(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL)) {
         traits.remove(InnateTrait.HEAVENLY_RESTRICTION_ENERGY);
      }

      return new TechniqueRoller.Result(technique, traits);
   }

   public static List<Technique> candidates(Set<InnateTrait> traits) {
      boolean npcOnlyAllowed = (Boolean)ServerConfig.ALLOW_NPC_ONLY_TECHNIQUES.get();
      boolean cursesAllowed = (Boolean)ServerConfig.CURSE_TECHNIQUES_IN_ROLL.get();
      return TechniqueRegistry.REGISTRY
         .stream()
         .filter(Technique::inRandomRoll)
         .filter(t -> npcOnlyAllowed || !t.npcOnly())
         .filter(t -> cursesAllowed || t.category() != TechniqueCategory.CURSES_AND_CURSE_USERS)
         .filter(t -> t.requiredTrait().map(traits::contains).orElse(true))
         .toList();
   }

   public static TechniqueRoller.Result rollFor(ServerPlayer player) {
      TechniqueRoller.Result result = roll(player.getRandom());
      SorcererManager.applyRoll(player, result.technique().id(), result.traits());
      if ((Boolean)CommonConfig.DEBUG_LOGGING.get()) {
         ModMain.LOGGER.info("[roll] {} -> {} {}", new Object[]{player.getScoreboardName(), result.technique().id(), result.traits()});
      }

      if (!(Boolean)ServerConfig.REQUIRE_JUJUTSU_HIGH_ENROLLMENT.get() && !SorcererManager.get(player).status().enrolled()) {
         SorcererManager.setStatus(player, SorcererStatus.STUDENT);
      }

      return result;
   }

   private static void rollTrait(RandomSource random, Set<InnateTrait> traits, InnateTrait trait, double chance) {
      if (random.nextDouble() < chance) {
         traits.add(trait);
      }
   }

   public record Result(Technique technique, Set<InnateTrait> traits) {
   }
}
