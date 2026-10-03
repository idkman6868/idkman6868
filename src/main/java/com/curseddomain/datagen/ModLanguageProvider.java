package com.curseddomain.datagen;

import com.curseddomain.config.ClientConfig;
import com.curseddomain.config.CommonConfig;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.cullinggame.CullingConfig;
import com.curseddomain.datagen.lang.CullingLang;
import com.curseddomain.datagen.lang.StoryLang;
import com.curseddomain.datagen.lang.TechniqueLang;
import com.curseddomain.energy.CursedEnergyNature;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueCategory;
import com.curseddomain.technique.TechniqueRarity;
import com.curseddomain.technique.TechniqueTier;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModLanguageProvider extends LanguageProvider {
   private final Set<String> added = new HashSet<>();
   private final Set<String> techniquesNamed = new HashSet<>();

   public ModLanguageProvider(PackOutput output) {
      super(output, "cursed_domain", "en_us");
   }

   protected void addTranslations() {
      this.techniques();
      this.enums();
      this.hud();
      this.commands();
      this.config();
      StoryLang.addTo(this);
      TechniqueLang.addTo(this);
      CullingLang.addTo(this);
   }

   private void techniques() {
      this.technique(ModTechniques.LIMITLESS, "Limitless", "Satoru Gojo");
      this.technique(ModTechniques.TEN_SHADOWS, "Ten Shadows Technique", "Megumi Fushiguro");
      this.technique(ModTechniques.STRAW_DOLL, "Straw Doll Technique", "Nobara Kugisaki");
      this.technique(ModTechniques.CURSED_SPEECH, "Cursed Speech", "Toge Inumaki");
      this.technique(ModTechniques.PUPPET_MANIPULATION, "Puppet Manipulation", "Masamichi Yaga");
      this.technique(ModTechniques.PANDA_CORES, "Panda Cores", "Panda");
      this.technique(ModTechniques.RCT_OUTPUT, "Reverse Cursed Technique (Output)", "Shoko Ieiri");
      this.technique(ModTechniques.BARRIER_TECHNIQUES, "Barrier Techniques", "Kiyotaka Ijichi, Tengen");
      this.technique(ModTechniques.INJURY_STABILIZATION, "Injury Stabilization", "Arata Nitta");
      this.technique(ModTechniques.DIVERGENT_FIST, "Divergent Fist", "Yuji Itadori");
      this.technique(ModTechniques.COPY, "Copy", "Yuta Okkotsu");
      this.technique(ModTechniques.HEAVENLY_RESTRICTION, "Heavenly Restriction", "Maki Zenin");
      this.technique(ModTechniques.BOOGIE_WOOGIE, "Boogie Woogie", "Aoi Todo");
      this.technique(ModTechniques.BLOOD_MANIPULATION, "Blood Manipulation", "Noritoshi Kamo, Choso");
      this.technique(ModTechniques.CONSTRUCTION, "Construction", "Mai Zenin");
      this.technique(ModTechniques.REMOTE_PUPPETRY, "Puppet Manipulation (Remote)", "Kokichi Muta (Mechamaru)");
      this.technique(ModTechniques.BROOM_FLIGHT, "Broom Flight", "Momo Nishimiya");
      this.technique(ModTechniques.SOLO_FORBIDDEN_AREA, "Solo Forbidden Area", "Utahime Iori");
      this.technique(ModTechniques.GUITAR_TECHNIQUE, "Guitar Technique", "Yoshinobu Gakuganji");
      this.technique(ModTechniques.NEW_SHADOW_STYLE, "New Shadow Style", "Kasumi Miwa");
      this.technique(ModTechniques.RATIO, "Ratio Technique", "Kento Nanami");
      this.technique(ModTechniques.PROJECTION_SORCERY, "Projection Sorcery", "Naobito Zenin, Naoya Zenin");
      this.technique(ModTechniques.BLACK_BIRD_MANIPULATION, "Black Bird Manipulation", "Mei Mei");
      this.technique(ModTechniques.TELEPORTATION, "Teleportation", "Ui Ui");
      this.technique(ModTechniques.AUSPICIOUS_BEASTS, "Auspicious Beasts Summon", "Takuma Ino");
      this.technique(ModTechniques.STAR_RAGE, "Star Rage", "Yuki Tsukumo");
      this.technique(ModTechniques.FLAME_SWORD, "Flame Sword", "Ogi Zenin");
      this.technique(ModTechniques.GIANT_FIST, "Giant Fist", "Jinichi Zenin");
      this.technique(ModTechniques.EVIL_EYE, "Evil Eye", "Ranta");
      this.technique(ModTechniques.HEAVENLY_RESTRICTION_FULL, "Heavenly Restriction (Full)", "Toji Fushiguro");
      this.technique(ModTechniques.ICE_FORMATION, "Ice Formation", "Uraume");
      this.technique(ModTechniques.IDLE_DEATH_GAMBLE, "Idle Death Gamble", "Kinji Hakari");
      this.technique(ModTechniques.DEADLY_SENTENCING, "Deadly Sentencing", "Hiromi Higuruma");
      this.technique(ModTechniques.MYTHICAL_BEAST_AMBER, "Mythical Beast Amber", "Hajime Kashimo");
      this.technique(ModTechniques.SKY_MANIPULATION, "Sky Manipulation", "Takako Uro");
      this.technique(ModTechniques.GRANITE_BLAST, "Granite Blast", "Ryu Ishigori");
      this.technique(ModTechniques.LOVE_RENDEZVOUS, "Love Rendezvous", "Kirara Hoshi");
      this.technique(ModTechniques.COMEDIAN, "Comedian", "Fumihiko Takaba");
      this.technique(ModTechniques.JACOBS_LADDER, "Jacob's Ladder", "Hana Kurusu (Angel)");
      this.technique(ModTechniques.CONTRACT_REPOSSESSION, "Contract Repossession", "Reggie Star");
      this.technique(ModTechniques.SHIKIGAMI_TERRITORY, "Shikigami Territory", "Dhruv Lakdawalla");
      this.technique(ModTechniques.EXPLOSIVE_FRAGMENTS, "Explosive Fragments", "Iori Hazenoki");
      this.technique(ModTechniques.SUMO, "Sumo", "Rokujushi Miyo");
      this.technique(ModTechniques.G_WARSTAFF, "G-Warstaff", "Charles Bernard");
      this.technique(ModTechniques.HEART_CATCH, "Heart Catch", "Larue");
      this.technique(ModTechniques.BLACK_ROPE_DANCE, "Black Rope Dance", "Miguel");
      this.technique(ModTechniques.MIRACLES, "Miracles", "Haruta Shigemo");
      this.technique(ModTechniques.SEANCE, "Seance", "Granny Ogami");
      this.technique(ModTechniques.TRUE_SPHERE_CONSTRUCTION, "Construction (True Sphere)", "Yorozu");
      this.technique(ModTechniques.SHRINE, "Shrine", "Ryomen Sukuna");
      this.technique(ModTechniques.IDLE_TRANSFIGURATION, "Idle Transfiguration", "Mahito");
      this.technique(ModTechniques.DISASTER_FLAMES, "Disaster Flames", "Jogo");
      this.technique(ModTechniques.DISASTER_PLANTS, "Disaster Plants", "Hanami");
      this.technique(ModTechniques.DISASTER_TIDES, "Disaster Tides", "Dagon");
      this.technique(ModTechniques.CURSED_SPIRIT_MANIPULATION, "Cursed Spirit Manipulation", "Suguru Geto, Kenjaku");
      this.technique(ModTechniques.BODY_SWAP, "Body Swap", "Kenjaku");
      this.technique(ModTechniques.ANTI_GRAVITY_SYSTEM, "Anti-Gravity System", "Kaori Itadori");
      this.technique(ModTechniques.ROT_TECHNIQUE, "Rot Technique: Decay", "Eso, Kechizu");
      this.technique(ModTechniques.MOON_DREGS, "Moon Dregs", "Junpei Yoshino");
      this.technique(ModTechniques.COCKROACH_SWARM, "Cockroach Swarm", "Kurourushi");
      this.technique(ModTechniques.SMALLPOX_DEITY, "Smallpox Burial", "Smallpox Deity");

      for (DeferredHolder<Technique, ? extends Technique> entry : ModTechniques.TECHNIQUES.getEntries()) {
         if (!this.techniquesNamed.contains(entry.getId().getPath())) {
            throw new IllegalStateException("Technique without an English name: " + entry.getId());
         }
      }
   }

   private void technique(DeferredHolder<Technique, ? extends Technique> holder, String name, String user) {
      String key = Util.makeDescriptionId("technique", holder.getId());
      this.add(key, name);
      this.add(key + ".user", user);
      this.techniquesNamed.add(holder.getId().getPath());
   }

   private void enums() {
      this.status(SorcererStatus.NON_SORCERER, "Ordinary Person");
      this.status(SorcererStatus.AWAKENED, "Awakened");
      this.status(SorcererStatus.APPLICANT, "Applicant");
      this.status(SorcererStatus.ENTRANCE_MISSION, "Entrance Mission");
      this.status(SorcererStatus.STUDENT, "Jujutsu High Student");
      this.status(SorcererStatus.CURSE_USER, "Curse User");
      this.add(Grade.UNGRADED.translationKey(), "Ungraded");
      this.add(Grade.GRADE_4.translationKey(), "Grade 4");
      this.add(Grade.GRADE_3.translationKey(), "Grade 3");
      this.add(Grade.GRADE_2.translationKey(), "Grade 2");
      this.add(Grade.SEMI_GRADE_1.translationKey(), "Semi-Grade 1");
      this.add(Grade.GRADE_1.translationKey(), "Grade 1");
      this.add(Grade.SPECIAL_GRADE.translationKey(), "Special Grade");
      this.add(InnateTrait.SIX_EYES.translationKey(), "Six Eyes");
      this.add(InnateTrait.HEAVENLY_RESTRICTION_PHYSICAL.translationKey(), "Heavenly Restriction (Physical)");
      this.add(InnateTrait.HEAVENLY_RESTRICTION_ENERGY.translationKey(), "Heavenly Restriction (Cursed Energy)");
      this.add(InnateTrait.VESSEL.translationKey(), "Vessel");
      this.add(InnateTrait.DEATH_PAINTING.translationKey(), "Death Painting Incarnation");
      this.add(InnateTrait.INCARNATED_SORCERER.translationKey(), "Incarnated Sorcerer");
      this.add(CursedEnergyNature.STANDARD.translationKey(), "Standard");
      this.add(CursedEnergyNature.ELECTRIC.translationKey(), "Electric");
      this.add(CursedEnergyNature.ROUGH.translationKey(), "Rough");
      this.add(TechniqueRarity.COMMON.translationKey(), "Common");
      this.add(TechniqueRarity.UNCOMMON.translationKey(), "Uncommon");
      this.add(TechniqueRarity.RARE.translationKey(), "Rare");
      this.add(TechniqueRarity.EPIC.translationKey(), "Epic");
      this.add(TechniqueRarity.LEGENDARY.translationKey(), "Legendary");
      this.add(TechniqueRarity.MYTHIC.translationKey(), "Mythic");

      for (TechniqueTier tier : TechniqueTier.values()) {
         this.add(tier.translationKey(), "Tier " + tier.name());
      }

      this.add(TechniqueCategory.TOKYO_HIGH.translationKey(), "Tokyo Jujutsu High");
      this.add(TechniqueCategory.KYOTO_HIGH.translationKey(), "Kyoto Jujutsu High");
      this.add(TechniqueCategory.CLANS_AND_ADULTS.translationKey(), "Clans & Adult Sorcerers");
      this.add(TechniqueCategory.CULLING_GAME.translationKey(), "Culling Game Players");
      this.add(TechniqueCategory.CURSES_AND_CURSE_USERS.translationKey(), "Curses & Curse Users");
   }

   private void status(SorcererStatus status, String name) {
      this.add(status.translationKey(), name);
   }

   private void hud() {
      this.add("hud.cursed_domain.energy", "Cursed Energy");
      this.add("hud.cursed_domain.meditating", "Meditating");
   }

   private void commands() {
      String c = "commands.cursed_domain.";
      this.add(c + "error.unknown_technique", "Unknown technique: %s");
      this.add(c + "error.not_enrolled", "%s is not enrolled. Set their status to student or curse_user first.");
      this.add(c + "error.invalid_value", "Invalid value: %s");
      this.add(c + "set.status", "Set %s's status to %s");
      this.add(c + "set.grade", "Set %s's grade to %s");
      this.add(c + "set.technique", "Set %s's technique to %s");
      this.add(c + "set.energy", "Set %s's cursed energy to %s / %s");
      this.add(c + "reveal", "Revealed %s's technique to them");
      this.add(c + "hide", "Hid %s's technique from them again");
      this.add(c + "trait.add", "Gave %s to %s");
      this.add(c + "trait.remove", "Removed %s from %s");
      this.add(c + "none", "None");
      this.add(c + "yes", "yes");
      this.add(c + "no", "no");
      this.add(c + "info.header", "Sorcerer record: %s");
      this.add(c + "info.status", "Status: %s | Grade: %s (%s XP)");
      this.add(c + "info.technique", "Technique: %s %s");
      this.add(c + "info.revealed", "(known to the player)");
      this.add(c + "info.hidden", "(secret, not revealed yet)");
      this.add(c + "info.traits", "Traits: %s");
      this.add(c + "info.energy", "Cursed energy: %s / %s | regen %s/s | output %s | control %s%% | nature %s");
      this.add(c + "info.state", "Meditating: %s | In combat: %s | Cost multiplier: x%s");
      this.add(c + "techniques.header", "Technique catalogue (%s). Hover a name for details.");
      this.add(c + "techniques.hover", "%s, %s\nUser: %s\n%s");
      this.add(c + "techniques.npc_only", "NPC / boss only (unless allowNpcOnlyTechniques is on)");
      this.add(c + "techniques.not_rolled", "Never handed out by the random roll");
      this.add(c + "techniques.mod_original", "Mod-original name (unnamed in canon)");
      this.add(c + "techniques.placeholder", "Catalogue entry: its abilities arrive in its tier's phase");
   }

   private void config() {
      String prefix = "cursed_domain.configuration.";
      this.add(prefix + "title", "Cursed Domain Configuration");

      for (String type : new String[]{"client", "common", "server"}) {
         String section = prefix + "section." + "cursed_domain".replace('_', '.') + "." + type + ".toml";
         String label = type.substring(0, 1).toUpperCase(Locale.ROOT) + type.substring(1) + " Settings";
         this.add(section, label);
         this.add(section + ".title", "Cursed Domain: " + label);
      }

      String culling = prefix + "section.cursed.domain.culling.server.toml";
      this.add(culling, "Shibuya & Culling Game Settings");
      this.add(culling + ".title", "Cursed Domain: Shibuya & Culling Game Settings");
      this.configSpec(ServerConfig.SPEC);
      this.configSpec(CullingConfig.SPEC);
      this.configSpec(ClientConfig.SPEC);
      this.configSpec(CommonConfig.SPEC);
   }

   private void configSpec(ModConfigSpec spec) {
      this.walk(spec, spec.getSpec(), new ArrayList<>());
   }

   private void walk(ModConfigSpec spec, UnmodifiableConfig level, List<String> path) {
      level.valueMap().forEach((name, value) -> {
         String key = "cursed_domain.configuration." + name;
         List<String> childPath = new ArrayList<>(path);
         childPath.add(name);
         if (value instanceof UnmodifiableConfig child) {
            this.addOnce(key, humanize(name));
            String comment = spec.getLevelComment(childPath);
            if (comment != null && !comment.isBlank()) {
               this.addOnce(key + ".tooltip", comment);
            }

            this.walk(spec, child, childPath);
         } else if (value instanceof ValueSpec valueSpec) {
            this.addOnce(key, humanize(name));
            if (valueSpec.getComment() != null && !valueSpec.getComment().isBlank()) {
               this.addOnce(key + ".tooltip", withoutDefault(valueSpec.getComment()));
            }
         }
      });
   }

   private static String withoutDefault(String comment) {
      return comment.lines().map(String::strip).filter(line -> !line.startsWith("Default:")).collect(Collectors.joining("\n"));
   }

   static String humanize(String name) {
      String spaced = name.replace('_', ' ').replaceAll("([a-z])([A-Z0-9])", "$1 $2");
      StringBuilder out = new StringBuilder(spaced.length());
      boolean upper = true;

      for (char ch : spaced.toCharArray()) {
         out.append(upper ? Character.toUpperCase(ch) : ch);
         upper = ch == ' ';
      }

      return out.toString();
   }

   private void addOnce(String key, String value) {
      if (this.added.add(key)) {
         this.add(key, value);
      }
   }
}
