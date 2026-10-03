package com.curseddomain.datagen.lang;

import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModItems;
import com.curseddomain.sorcerer.AwakeningCause;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class StoryLang {
   private StoryLang() {
   }

   public static void addTo(LanguageProvider lang) {
      lang.add("itemGroup.cursed_domain", "Jujutsu Kaisen: Cursed Domain");
      lang.add((EntityType)ModEntities.GRADE_4_CURSE.get(), "Grade 4 Curse");
      lang.add((Item)ModItems.RECRUITMENT_LETTER.get(), "Jujutsu High Recruitment Letter");
      lang.add((Item)ModItems.CURSED_OBJECT.get(), "Cursed Object");
      lang.add((Item)ModItems.GRADE_4_CURSE_SPAWN_EGG.get(), "Grade 4 Curse Spawn Egg");
      String letter = "item.cursed_domain.recruitment_letter.";
      lang.add(letter + "desc", "A letter sealed in red wax, addressed to no one in particular.");
      lang.add(letter + "usage", "Use: feel which way it leans. Sneak + use: read it.");
      lang.add(letter + "blank", "The paper is blank. Isn't it?");
      lang.add(letter + "answered", "You have already answered this letter.");
      lang.add(letter + "still", "The letter lies still. Whatever it points to is not in this world.");
      lang.add(letter + "hint", "The letter leans toward the %s. %s");
      lang.add(letter + "distance.very_close", "It trembles in your hand: you are nearly there.");
      lang.add(letter + "distance.near", "Not far now.");
      lang.add(letter + "distance.far", "A long walk yet.");
      lang.add(letter + "distance.very_far", "It is very far.");
      lang.add("item.cursed_domain.cursed_object.desc", "Cold to the touch. Something inside it is watching.");
      lang.add("letter.cursed_domain.title", "Tokyo Prefectural Jujutsu High School");
      lang.add(
         "letter.cursed_domain.body",
         String.join(
            "\n",
            "To the one who can now read this,",
            "If these words have appeared to you, then you have started to see what most people never will. What hurt you was no accident. It was a curse: a thing born from the fear and spite people leave behind.",
            "You now carry cursed energy of your own. Untrained, it will draw them to you. Trained, it can drive them back.",
            "There is a school hidden in the mountains where sorcerers are made. Hold this letter out and it will lean the way you need to go. The road is not marked, and it is not meant to be.",
            "Come prepared to answer one question: why do you want to be here?"
         )
      );
      lang.add("letter.cursed_domain.signature", "- The Principal's Office");
      lang.add(
         "letter.cursed_domain.applicant",
         "(You are now an applicant to Jujutsu High. Use the letter to feel which way it leans; sneak and use it to read it again.)"
      );
      lang.add("letter.cursed_domain.close", "Fold the letter");
      lang.add("story.cursed_domain.scout.name", "[Jujutsu High Scout] ");
      lang.add("story.cursed_domain.scout.1", "Ah, um, excuse me. You can see them now, can't you? The things in the corners.");
      lang.add("story.cursed_domain.scout.2", "Please don't panic. I left a letter with you. It's from the school; it will show you the way.");
      lang.add("story.cursed_domain.scout.3", "I'm sorry, I can't stay. Please be careful, and don't fight anything bigger than you.");
      lang.add("story.cursed_domain.rumor.1", "Strange students in dark uniforms were seen in the mountains to the %s.");
      lang.add("story.cursed_domain.rumor.2", "My cousin swears there's a temple to the %s that you can't find unless it lets you.");
      lang.add("story.cursed_domain.rumor.3", "People who wander %s come back talking about a school. Nobody finds it twice.");
      lang.add("story.cursed_domain.seal_break", "Somewhere close, a seal snaps. The air turns heavy and cold.");
      lang.add("awakening.cursed_domain.title", "Your eyes are open");
      lang.add(AwakeningCause.CURSE_ATTACKS.subtitleKey(), "The thing that kept hurting you has a shape.");
      lang.add(AwakeningCause.NEAR_DEATH.subtitleKey(), "At the edge of death, you finally see it.");
      lang.add(AwakeningCause.CURSED_OBJECT.subtitleKey(), "The relic stares back at you.");
      lang.add(AwakeningCause.SEAL_BREAK.subtitleKey(), "A seal broke, and the world broke open with it.");
      lang.add(AwakeningCause.COMMAND.subtitleKey(), "Something has changed.");
      String[][] directions = new String[][]{
         {"north", "north"},
         {"northeast", "northeast"},
         {"east", "east"},
         {"southeast", "southeast"},
         {"south", "south"},
         {"southwest", "southwest"},
         {"west", "west"},
         {"northwest", "northwest"}
      };

      for (String[] d : directions) {
         lang.add("direction.cursed_domain." + d[0], d[1]);
      }

      String c = "commands.cursed_domain.";
      lang.add(c + "roll", "Rolled %s: %s, traits: %s");
      lang.add(c + "awaken", "%s has awakened");
      lang.add(c + "awaken.already", "%s can already see curses");
      lang.add(c + "locate.school", "Tokyo Jujutsu High is at %s (%s blocks away)");
      lang.add(c + "locate.none", "The school has no location yet (no world loaded?)");
      lang.add(c + "event.seal_break", "A seal broke at %s");
   }
}
