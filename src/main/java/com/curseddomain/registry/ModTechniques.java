package com.curseddomain.registry;

import com.curseddomain.energy.CursedEnergyNature;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.technique.PlaceholderTechnique;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueCategory;
import com.curseddomain.technique.TechniqueRarity;
import com.curseddomain.technique.TechniqueRegistry;
import com.curseddomain.technique.TechniqueTier;
import com.curseddomain.technique.impl.bloodmanipulation.BloodManipulationTechnique;
import com.curseddomain.technique.impl.boogiewoogie.BoogieWoogieTechnique;
import com.curseddomain.technique.impl.construction.ConstructionTechnique;
import com.curseddomain.technique.impl.cursedspeech.CursedSpeechTechnique;
import com.curseddomain.technique.impl.disasterflames.DisasterFlamesTechnique;
import com.curseddomain.technique.impl.heavenlyrestriction.HeavenlyRestrictionTechnique;
import com.curseddomain.technique.impl.idletransfiguration.IdleTransfigurationTechnique;
import com.curseddomain.technique.impl.limitless.LimitlessTechnique;
import com.curseddomain.technique.impl.newshadowstyle.NewShadowStyleTechnique;
import com.curseddomain.technique.impl.ratio.RatioTechnique;
import com.curseddomain.technique.impl.shrine.ShrineTechnique;
import com.curseddomain.technique.impl.strawdoll.StrawDollTechnique;
import com.curseddomain.technique.impl.tenshadows.TenShadowsTechnique;
import java.util.function.Function;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModTechniques {
   public static final DeferredRegister<Technique> TECHNIQUES = DeferredRegister.create(TechniqueRegistry.KEY, "cursed_domain");
   public static final DeferredHolder<Technique, Technique> LIMITLESS = impl(
      "limitless", LimitlessTechnique::new, Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.MYTHIC, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> TEN_SHADOWS = impl(
      "ten_shadows", TenShadowsTechnique::new, Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.LEGENDARY, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> STRAW_DOLL = impl(
      "straw_doll", StrawDollTechnique::new, Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.RARE, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> CURSED_SPEECH = impl(
      "cursed_speech", CursedSpeechTechnique::new, Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.RARE, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> PUPPET_MANIPULATION = entry(
      "puppet_manipulation", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> PANDA_CORES = entry(
      "panda_cores", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.RARE, TechniqueTier.C).notInRandomRoll()
   );
   public static final DeferredHolder<Technique, Technique> RCT_OUTPUT = entry(
      "rct_output", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> BARRIER_TECHNIQUES = entry(
      "barrier_techniques", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> INJURY_STABILIZATION = entry(
      "injury_stabilization", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.COMMON, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> DIVERGENT_FIST = entry(
      "divergent_fist", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.LEGENDARY, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> COPY = entry(
      "copy", Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.MYTHIC, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> HEAVENLY_RESTRICTION = impl(
      "heavenly_restriction",
      HeavenlyRestrictionTechnique::partial,
      Technique.Properties.of(TechniqueCategory.TOKYO_HIGH, TechniqueRarity.RARE, TechniqueTier.A).grantsTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL)
   );
   public static final DeferredHolder<Technique, Technique> BOOGIE_WOOGIE = impl(
      "boogie_woogie", BoogieWoogieTechnique::new, Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> BLOOD_MANIPULATION = impl(
      "blood_manipulation", BloodManipulationTechnique::new, Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.EPIC, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> CONSTRUCTION = impl(
      "construction", ConstructionTechnique::new, Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> REMOTE_PUPPETRY = entry(
      "remote_puppetry", Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> BROOM_FLIGHT = entry(
      "broom_flight", Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> SOLO_FORBIDDEN_AREA = entry(
      "solo_forbidden_area", Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> GUITAR_TECHNIQUE = entry(
      "guitar_technique", Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.UNCOMMON, TechniqueTier.C).modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> NEW_SHADOW_STYLE = impl(
      "new_shadow_style", NewShadowStyleTechnique::new, Technique.Properties.of(TechniqueCategory.KYOTO_HIGH, TechniqueRarity.COMMON, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> RATIO = impl(
      "ratio", RatioTechnique::new, Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.RARE, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> PROJECTION_SORCERY = entry(
      "projection_sorcery", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.EPIC, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> BLACK_BIRD_MANIPULATION = entry(
      "black_bird_manipulation", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> TELEPORTATION = entry(
      "teleportation", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.RARE, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> AUSPICIOUS_BEASTS = entry(
      "auspicious_beasts", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> STAR_RAGE = entry(
      "star_rage", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.LEGENDARY, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> FLAME_SWORD = entry(
      "flame_sword", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.UNCOMMON, TechniqueTier.C).modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> GIANT_FIST = entry(
      "giant_fist", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.UNCOMMON, TechniqueTier.C).modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> EVIL_EYE = entry(
      "evil_eye", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.UNCOMMON, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> HEAVENLY_RESTRICTION_FULL = impl(
      "heavenly_restriction_full",
      HeavenlyRestrictionTechnique::full,
      Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.EPIC, TechniqueTier.A).grantsTrait(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL)
   );
   public static final DeferredHolder<Technique, Technique> ICE_FORMATION = entry(
      "ice_formation", Technique.Properties.of(TechniqueCategory.CLANS_AND_ADULTS, TechniqueRarity.EPIC, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> IDLE_DEATH_GAMBLE = entry(
      "idle_death_gamble", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.LEGENDARY, TechniqueTier.B).nature(CursedEnergyNature.ROUGH)
   );
   public static final DeferredHolder<Technique, Technique> DEADLY_SENTENCING = entry(
      "deadly_sentencing", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.EPIC, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> MYTHICAL_BEAST_AMBER = entry(
      "mythical_beast_amber",
      Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.LEGENDARY, TechniqueTier.B).nature(CursedEnergyNature.ELECTRIC)
   );
   public static final DeferredHolder<Technique, Technique> SKY_MANIPULATION = entry(
      "sky_manipulation", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> GRANITE_BLAST = entry(
      "granite_blast", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> LOVE_RENDEZVOUS = entry(
      "love_rendezvous", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> COMEDIAN = entry(
      "comedian", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.MYTHIC, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> JACOBS_LADDER = entry(
      "jacobs_ladder", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.LEGENDARY, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> CONTRACT_REPOSSESSION = entry(
      "contract_repossession", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> SHIKIGAMI_TERRITORY = entry(
      "shikigami_territory", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.C).modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> EXPLOSIVE_FRAGMENTS = entry(
      "explosive_fragments", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.UNCOMMON, TechniqueTier.C).modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> SUMO = entry(
      "sumo", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.UNCOMMON, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> G_WARSTAFF = entry(
      "g_warstaff", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> HEART_CATCH = entry(
      "heart_catch", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.UNCOMMON, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> BLACK_ROPE_DANCE = entry(
      "black_rope_dance", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.C).modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> MIRACLES = entry(
      "miracles", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.UNCOMMON, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> SEANCE = entry(
      "seance", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.RARE, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> TRUE_SPHERE_CONSTRUCTION = entry(
      "true_sphere_construction", Technique.Properties.of(TechniqueCategory.CULLING_GAME, TechniqueRarity.LEGENDARY, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> SHRINE = impl(
      "shrine",
      ShrineTechnique::new,
      Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.MYTHIC, TechniqueTier.A).requiresTrait(InnateTrait.VESSEL)
   );
   public static final DeferredHolder<Technique, Technique> IDLE_TRANSFIGURATION = impl(
      "idle_transfiguration",
      IdleTransfigurationTechnique::new,
      Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.EPIC, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> DISASTER_FLAMES = impl(
      "disaster_flames", DisasterFlamesTechnique::new, Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.EPIC, TechniqueTier.A)
   );
   public static final DeferredHolder<Technique, Technique> DISASTER_PLANTS = entry(
      "disaster_plants", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.EPIC, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> DISASTER_TIDES = entry(
      "disaster_tides", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.EPIC, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> CURSED_SPIRIT_MANIPULATION = entry(
      "cursed_spirit_manipulation", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.LEGENDARY, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> BODY_SWAP = entry(
      "body_swap", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.MYTHIC, TechniqueTier.C).npcOnly()
   );
   public static final DeferredHolder<Technique, Technique> ANTI_GRAVITY_SYSTEM = entry(
      "anti_gravity_system", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.RARE, TechniqueTier.C)
   );
   public static final DeferredHolder<Technique, Technique> ROT_TECHNIQUE = entry(
      "rot_technique", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.RARE, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> MOON_DREGS = entry(
      "moon_dregs", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.UNCOMMON, TechniqueTier.B)
   );
   public static final DeferredHolder<Technique, Technique> COCKROACH_SWARM = entry(
      "cockroach_swarm", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.EPIC, TechniqueTier.C).npcOnly().modOriginal()
   );
   public static final DeferredHolder<Technique, Technique> SMALLPOX_DEITY = entry(
      "smallpox_deity", Technique.Properties.of(TechniqueCategory.CURSES_AND_CURSE_USERS, TechniqueRarity.EPIC, TechniqueTier.C).npcOnly().modOriginal()
   );

   private ModTechniques() {
   }

   private static DeferredHolder<Technique, Technique> impl(String name, Function<Technique.Properties, Technique> factory, Technique.Properties properties) {
      return TECHNIQUES.register(name, () -> factory.apply(properties));
   }

   private static DeferredHolder<Technique, Technique> entry(String name, Technique.Properties properties) {
      return TECHNIQUES.register(name, () -> new PlaceholderTechnique(properties));
   }
}
