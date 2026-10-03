package com.curseddomain.datagen.lang;

import com.curseddomain.registry.ModBlocks;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModItems;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class TechniqueLang {
   private TechniqueLang() {
   }

   private static void ability(LanguageProvider lang, String technique, String ability, String name, String description) {
      String key = "ability.cursed_domain." + technique + "." + ability;
      lang.add(key, name);
      lang.add(key + ".desc", description);
   }

   public static void addTo(LanguageProvider lang) {
      abilities(lang);
      universal(lang);
      domains(lang);
      feedback(lang);
      world(lang);
   }

   private static void abilities(LanguageProvider lang) {
      ability(
         lang, "limitless", "infinity", "Infinity", "Toggle. Everything that approaches slows to a stop before it reaches you. Each blocked hit costs energy."
      );
      ability(lang, "limitless", "lapse_blue", "Lapse: Blue", "Open a point of attraction where you look; everything nearby is dragged in and crushed.");
      ability(
         lang,
         "limitless",
         "reversal_red",
         "Reversal: Red",
         "Charge and release a point of repulsion that blasts everything away. Needs Reverse Cursed Technique."
      );
      ability(lang, "limitless", "blue_step", "Blue-Step", "Pull yourself through space to where you are looking.");
      ability(lang, "limitless", "maximum_blue", "Maximum Output: Blue", "A huge, lasting vortex that tears the ground into itself.");
      ability(
         lang,
         "limitless",
         "hollow_purple",
         "Hollow Purple",
         "Collide Blue and Red. Hold to merge them, release to fire a mass that erases everything in its path."
      );
      ability(
         lang,
         "ten_shadows",
         "divine_dogs",
         "Divine Dogs",
         "Summon the white and black Divine Dogs (use again to recall). If one is destroyed, Totality inherits its power."
      );
      ability(lang, "ten_shadows", "nue", "Nue", "Summon Nue, a bird of lightning that dives at your enemies. The first summoning is a taming ritual.");
      ability(lang, "ten_shadows", "shadow_step", "Shadow Travel", "Sink into your shadow and rise out of another one ahead.");
      ability(lang, "ten_shadows", "shadow_storage", "Shadow Storage", "Keep items in your shadow. They stay with you through death.");
      ability(lang, "straw_doll", "hammer_nails", "Hammer & Nails", "Drive three nails with cursed energy. Nails that strike stay embedded.");
      ability(lang, "straw_doll", "hairpin", "Hairpin", "Detonate every nail you have embedded, in flesh or in stone.");
      ability(
         lang, "straw_doll", "resonance", "Resonance", "Strike the straw doll: everything carrying one of your nails is hurt in the soul, at any distance."
      );
      ability(lang, "straw_doll", "resonance_burst", "Resonance: Self-Inflicted", "Hurt yourself to hit every nailed target far harder.");
      ability(lang, "cursed_speech", "stop", "\"Stop.\"", "Everything in front of you freezes.");
      ability(lang, "cursed_speech", "dont_move", "\"Don't move.\"", "Everything in front of you is rooted where it stands.");
      ability(lang, "cursed_speech", "blast_away", "\"Blast away.\"", "Everything in front of you is hurled back.");
      ability(lang, "cursed_speech", "crush", "\"Crush.\"", "Everything in front of you is crushed. Strains the throat badly.");
      ability(lang, "cursed_speech", "sleep", "\"Sleep.\"", "Everything in front of you falls into a helpless sleep.");
      ability(lang, "cursed_speech", "run", "\"Run.\"", "Everything in front of you turns and flees.");
      ability(lang, "cursed_speech", "explode", "\"Explode.\"", "The strongest word. Devastating, and it nearly ruins your throat.");
      ability(lang, "boogie_woogie", "clap_swap", "Clap: Swap", "Clap to trade places with the target (or with your marked stone).");
      ability(lang, "boogie_woogie", "swap_others", "Clap: Swap Others", "Clap to make the target trade places with whatever is closest to it.");
      ability(lang, "boogie_woogie", "feint", "Clap: Feint", "A clap that swaps nothing. Everyone braced for it is thrown off.");
      ability(lang, "boogie_woogie", "mark_stone", "Marked Stone", "Throw a stone charged with cursed energy; your next empty clap swaps you to it.");
      ability(lang, "boogie_woogie", "swap_chain", "Clap: Chain", "Six claps in a heartbeat, trading places with every enemy around.");
      ability(
         lang,
         "blood_manipulation",
         "piercing_blood",
         "Piercing Blood",
         "Compress blood while held, then fire it as a beam that pierces everything in line. Costs blood."
      );
      ability(
         lang, "blood_manipulation", "slicing_exorcism", "Slicing Exorcism", "Two spinning blood discs that seek enemies and cut through them. Costs blood."
      );
      ability(
         lang, "blood_manipulation", "flowing_red_scale", "Flowing Red Scale", "Toggle. Blood surges through you: faster and stronger, but you keep bleeding."
      );
      ability(lang, "blood_manipulation", "crimson_binding", "Crimson Binding", "Bind the target in hardened blood. Costs blood.");
      ability(lang, "blood_manipulation", "supernova", "Supernova", "Orbs of compressed blood around you burst one after another. Costs a lot of blood.");
      ability(lang, "construction", "construct_blade", "Construct: Blade", "Make a blade out of cursed energy. It dissolves after a minute.");
      ability(lang, "construction", "construct_wall", "Construct: Wall", "Raise a wall of construct where you look. It fades in time.");
      ability(lang, "construction", "super_bullet", "Construct: Bullet", "The one bullet you can make a day. It goes through everything.");
      ability(
         lang,
         "construction",
         "final_construction",
         "Final Construction",
         "Create one permanent cursed tool. Binding vow: your cursed energy pool shrinks for good."
      );
      ability(
         lang,
         "ratio",
         "ratio_strike",
         "Ratio: 7:3",
         "Your next blow lands exactly on the 7:3 point: a guaranteed critical hit. Every hit also marks the point."
      );
      ability(lang, "ratio", "collapse", "Collapse", "Strike the weak point of the ground itself. It bursts into shrapnel.");
      ability(lang, "ratio", "overtime", "Overtime", "Toggle. Binding vow: weaker during the day, much stronger after dark.");

      for (String technique : new String[]{"heavenly_restriction", "heavenly_restriction_full"}) {
         ability(lang, technique, "heavenly_dash", "Heavenly Dash", "Cross the ground faster than the eye, cutting everything in your path.");
         ability(lang, technique, "crushing_leap", "Crushing Leap", "Leap high and land like a falling boulder.");
         ability(lang, technique, "keen_senses", "Keen Senses", "Feel every living thing around you without a drop of cursed energy.");
      }

      ability(lang, "heavenly_restriction_full", "inventory_curse", "Inventory Curse", "Your tools are kept inside a pet curse you carry.");
      ability(lang, "shrine", "dismantle", "Dismantle", "An invisible cutting slash that flies through everything in line.");
      ability(lang, "shrine", "cleave", "Cleave", "A slash that adjusts itself to the target's toughness. Tougher prey takes more.");
      ability(lang, "shrine", "divine_flame", "Divine Flame", "\"Open.\" Draw a bow of fire and loose it. The impact is an inferno.");
      ability(lang, "shrine", "world_cutting_slash", "World-Cutting Slash", "A slash through space itself. Nothing, not even Infinity, stands in its way.");
      ability(
         lang, "idle_transfiguration", "soul_touch", "Soul Touch", "Touch the soul and reshape it. Ignores armour; the wound lowers max health for a while."
      );
      ability(lang, "idle_transfiguration", "body_repel", "Body Repel", "Spikes burst out of your body in every direction.");
      ability(lang, "idle_transfiguration", "transfigured_humans", "Transfigured Humans", "Release reshaped people who fight for you for a while.");
      ability(lang, "idle_transfiguration", "blade_morph", "Body Morph: Blades", "Toggle. Reshape your arms into blades: more damage and reach.");
      ability(lang, "idle_transfiguration", "soul_isomer", "Polymorphic Soul Isomer", "Fuse several souls into one mass and throw it. It bursts on impact.");
      ability(lang, "disaster_flames", "ember_insects", "Ember Insects", "Release insects of flame that seek enemies and burst.");
      ability(lang, "disaster_flames", "flame_blast", "Flame Blast", "Hold to pour fire from your hands.");
      ability(lang, "disaster_flames", "eruption", "Eruption", "The ground rumbles where you look, then a volcano bursts out of it.");
      ability(lang, "disaster_flames", "maximum_meteor", "Maximum: Meteor", "Hold, then call down a molten mountain onto the aimed point.");
      ability(
         lang,
         "new_shadow_style",
         "simple_domain",
         "Simple Domain",
         "Toggle. A small field that cancels sure-hit effects. Anything that enters is cut by Batto."
      );
      ability(lang, "new_shadow_style", "quick_draw", "Quick Draw", "A dashing draw-cut through everything in front of you.");
      ability(lang, "new_shadow_style", "iai_counter", "Iai Counter", "Ready the sword: the next blow against you is caught and answered.");
   }

   private static void universal(LanguageProvider lang) {
      ability(
         lang,
         "universal",
         "black_flash",
         "Black Flash Focus",
         "Release cursed energy into your next blow. Land it within 0.1 seconds and it becomes a Black Flash."
      );
      ability(lang, "universal", "reinforcement", "Cursed Energy Reinforcement", "Toggle. Cursed energy flows through your body: harder hits, tougher skin.");
      ability(
         lang,
         "universal",
         "reverse_cursed_technique",
         "Reverse Cursed Technique",
         "Hold to turn cursed energy into positive energy and heal. With RCT Output, heal the ally you look at."
      );
      ability(lang, "universal", "simple_domain", "Simple Domain", "Toggle. A small field that cancels sure-hit effects; you move slowly inside it.");
      ability(
         lang, "universal", "hollow_wicker_basket", "Hollow Wicker Basket", "Hold still to weave an anti-domain around yourself. Sure-hit cannot find you."
      );
      ability(
         lang,
         "universal",
         "falling_blossom_emotion",
         "Falling Blossom Emotion",
         "Toggle. Every sure-hit that reaches you is met by an automatic counter-burst (costs energy)."
      );
      ability(
         lang,
         "universal",
         "domain_amplification",
         "Domain Amplification",
         "Toggle. Wrap your body in a domain that neutralises techniques on contact. You cannot use your own."
      );
      lang.add("combat.cursed_domain.black_flash", "BLACK FLASH");
      lang.add("combat.cursed_domain.first_black_flash", "BLACK FLASH. Cursed energy and body moved as one. You are in the Zone.");
      lang.add(
         "combat.cursed_domain.rct_awakened", "At the edge of death, something turns over: negative times negative. You have learned Reverse Cursed Technique."
      );
      lang.add("ability.cursed_domain.fail.amplified", "%s cannot be used under Domain Amplification.");
      lang.add("domain.cursed_domain.fail.amplified", "You cannot expand a domain under Domain Amplification.");
   }

   private static void domains(LanguageProvider lang) {
      lang.add("domain.cursed_domain.unlimited_void", "Unlimited Void");
      lang.add("domain.cursed_domain.chimera_shadow_garden", "Chimera Shadow Garden");
      lang.add("domain.cursed_domain.malevolent_shrine", "Malevolent Shrine");
      lang.add("domain.cursed_domain.self_embodiment_of_perfection", "Self-Embodiment of Perfection");
      lang.add("domain.cursed_domain.coffin_of_the_iron_mountain", "Coffin of the Iron Mountain");
      lang.add("domain.cursed_domain.expansion", "%s: Domain Expansion, %s");
      lang.add("domain.cursed_domain.casting", "Hand signs... %s");
      lang.add("domain.cursed_domain.interrupted", "Your hand signs were broken.");
      lang.add("domain.cursed_domain.clash", "DOMAIN CLASH!");
      lang.add("domain.cursed_domain.clash_won", "Your domain overwhelmed theirs.");
      String fail = "domain.cursed_domain.fail.";
      lang.add(fail + "locked", "Your innate technique is still locked.");
      lang.add(fail + "no_domain", "Your technique has no domain.");
      lang.add(fail + "burnout", "Your technique is burnt out.");
      lang.add(fail + "exhausted", "You are in no state to expand a domain.");
      lang.add(fail + "not_learned", "You have not learned to expand your domain yet.");
      lang.add(fail + "energy", "Not enough cursed energy to expand your domain.");
      String ended = "domain.cursed_domain.ended.";
      lang.add(ended + "dismissed", "You released your domain.");
      lang.add(ended + "expired", "Your domain closed.");
      lang.add(ended + "energy", "Your cursed energy ran dry and the domain collapsed.");
      lang.add(ended + "shattered", "Your barrier was shattered from outside!");
      lang.add(ended + "clash_lost", "Your domain was overwhelmed and collapsed.");
      lang.add(ended + "owner_gone", "The domain collapsed.");
      lang.add("hud.cursed_domain.clash", "DOMAIN CLASH - spam %s!");
   }

   private static void feedback(LanguageProvider lang) {
      String fail = "ability.cursed_domain.fail.";
      lang.add(fail + "locked", "Your innate technique is still locked.");
      lang.add(fail + "stunned", "You cannot move!");
      lang.add(fail + "exhausted", "You are exhausted.");
      lang.add(fail + "burnout", "Your technique is burnt out.");
      lang.add(fail + "grade", "%s needs a higher grade.");
      lang.add(fail + "cooldown", "%s is not ready.");
      lang.add(fail + "output", "%s needs more output than you can release at once.");
      lang.add(fail + "energy", "Not enough cursed energy for %s.");
      lang.add(fail + "unlock.rct", "%s needs Reverse Cursed Technique.");
      lang.add(fail + "unlock.world_slash", "%s has not been learned yet.");
      lang.add("ability.cursed_domain.exhausted", "You ran out of cursed energy!");
      lang.add("ability.cursed_domain.blood.too_weak", "You have no blood to spare.");
      lang.add("ability.cursed_domain.cursed_speech.throat", "Your throat is too damaged to speak.");

      for (String[] w : new String[][]{
         {"stop", "Stop."},
         {"dont_move", "Don't move."},
         {"blast_away", "Blast away."},
         {"crush", "Crush."},
         {"sleep", "Sleep."},
         {"run", "Run."},
         {"explode", "Explode."}
      }) {
         lang.add("ability.cursed_domain.cursed_speech.say." + w[0], "「" + w[1] + "」");
      }

      lang.add("ability.cursed_domain.shrine.open", "Open.");
      lang.add("ability.cursed_domain.construction.final", "The tool is finished. Something in you will never come back.");
      lang.add("shikigami.cursed_domain.lost", "%s is gone. It will never answer your shadow again.");
      lang.add("shikigami.cursed_domain.totality", "Its power did not vanish: it flows into the one that remains. Divine Dog: Totality.");
      lang.add("shikigami.cursed_domain.tamed", "You have tamed %s.");
      lang.add("shikigami.cursed_domain.ritual", "Taming ritual: defeat %s yourself to make it yours.");
      lang.add("shikigami.cursed_domain.none_left", "None of your dogs remain.");
      lang.add("container.cursed_domain.shadow_storage", "Shadow");
      lang.add("container.cursed_domain.inventory_curse", "Inventory Curse");
      lang.add("key.categories.cursed_domain", "Jujutsu Kaisen: Cursed Domain");
      lang.add("key.cursed_domain.ability_1", "Ability slot 1");
      lang.add("key.cursed_domain.ability_2", "Ability slot 2");
      lang.add("key.cursed_domain.ability_3", "Ability slot 3");
      lang.add("key.cursed_domain.ability_4", "Ability slot 4");
      lang.add("key.cursed_domain.ability_5", "Ability slot 5");
      lang.add("key.cursed_domain.ability_wheel", "Ability wheel (hold)");
      lang.add("key.cursed_domain.domain", "Domain expansion");
      lang.add("gui.cursed_domain.ability_wheel", "Abilities");
      lang.add("gui.cursed_domain.ability_wheel.hint", "Point at an ability, let go of the key to use it");
      lang.add("gui.cursed_domain.ability_wheel.assign", "While pointing: 1-5 puts it in a quick slot");
      lang.add("gui.cursed_domain.ability_cost", "Cost %s, cooldown %ss");
      lang.add("gui.cursed_domain.maximum", "Maximum");
      String c = "commands.cursed_domain.";
      lang.add(c + "unlock", "Unlocked %s for %s");
      lang.add(c + "lock", "Locked %s for %s");
      lang.add(c + "cooldowns", "Reset %s's cooldowns");
      lang.add(c + "cleanse", "Cleansed %s");
   }

   private static void world(LanguageProvider lang) {
      lang.add((MobEffect)ModEffects.STUNNED.get(), "Stunned");
      lang.add((MobEffect)ModEffects.EXHAUSTED.get(), "Exhausted");
      lang.add((MobEffect)ModEffects.BURNOUT.get(), "Technique Burnout");
      lang.add((MobEffect)ModEffects.HOARSE.get(), "Hoarse");
      lang.add((MobEffect)ModEffects.RATIO_MARK.get(), "7:3 Point");
      lang.add((MobEffect)ModEffects.FLOWING_RED_SCALE.get(), "Flowing Red Scale");
      lang.add((MobEffect)ModEffects.SOUL_DAMAGE.get(), "Wounded Soul");
      lang.add((MobEffect)ModEffects.SIMPLE_DOMAIN.get(), "Simple Domain");
      lang.add((MobEffect)ModEffects.HOLLOW_WICKER_BASKET.get(), "Hollow Wicker Basket");
      lang.add((MobEffect)ModEffects.FALLING_BLOSSOM_EMOTION.get(), "Falling Blossom Emotion");
      lang.add((MobEffect)ModEffects.DOMAIN_AMPLIFICATION.get(), "Domain Amplification");
      lang.add((MobEffect)ModEffects.ZONE.get(), "The Zone");
      lang.add((EntityType)ModEntities.TECHNIQUE_PROJECTILE.get(), "Cursed Technique");
      lang.add((EntityType)ModEntities.DOMAIN_BARRIER.get(), "Domain");
      lang.add((EntityType)ModEntities.DIVINE_DOG.get(), "Divine Dog");
      lang.add((EntityType)ModEntities.NUE.get(), "Nue");
      lang.add((EntityType)ModEntities.TRANSFIGURED_HUMAN.get(), "Transfigured Human");
      lang.add((Item)ModItems.THROAT_MEDICINE.get(), "Throat Medicine");
      lang.add("item.cursed_domain.throat_medicine.desc", "Soothes a throat torn by Cursed Speech.");
      lang.add((Item)ModItems.CONSTRUCTED_BLADE.get(), "Constructed Blade");
      lang.add((Item)ModItems.CONSTRUCTED_CURSED_TOOL.get(), "Constructed Cursed Tool");
      lang.add("item.cursed_domain.constructed.temporary", "Made of cursed energy. It will not last.");
      lang.add("item.cursed_domain.constructed.permanent", "Made at the cost of part of its maker's soul. It will never fade.");
      lang.add((Block)ModBlocks.CONSTRUCT.get(), "Construct");
      String death = "death.attack.cursed_domain.";
      lang.add(death + "technique", "%1$s was overwhelmed by a cursed technique");
      lang.add(death + "technique.player", "%1$s was struck down by %2$s's cursed technique");
      lang.add(death + "sure_hit", "%1$s could not escape a domain's sure-hit");
      lang.add(death + "sure_hit.player", "%1$s could not escape %2$s's domain");
      lang.add(death + "soul", "%1$s had their soul torn apart");
      lang.add(death + "soul.player", "%1$s had their soul torn apart by %2$s");
      lang.add(death + "black_flash", "%1$s met a Black Flash");
      lang.add(death + "black_flash.player", "%1$s met %2$s's Black Flash");
      lang.add(death + "domain", "%1$s was consumed by a domain");
      lang.add(death + "domain.player", "%1$s was consumed by %2$s's domain");
   }
}
