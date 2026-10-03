package com.curseddomain.client.dev;

import com.curseddomain.ModMain;
import com.curseddomain.client.gui.AbilityWheelScreen;
import com.curseddomain.client.input.AbilityInput;
import com.curseddomain.client.story.HintTrail;
import com.curseddomain.config.ClientConfig;
import com.curseddomain.config.HudAnchor;
import com.curseddomain.domain.DomainPayloads;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueRegistry;
import com.curseddomain.technique.ability.Ability;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;

final class AutoScenarios {
   static final String[] TIER_A = new String[]{
      "limitless",
      "ten_shadows",
      "straw_doll",
      "cursed_speech",
      "boogie_woogie",
      "blood_manipulation",
      "construction",
      "ratio",
      "heavenly_restriction_full",
      "shrine",
      "idle_transfiguration",
      "disaster_flames",
      "new_shadow_style"
   };

   private AutoScenarios() {
   }

   static void run(String scenario) {
      switch (scenario) {
         case "foundation":
         case "1":
            foundation();
            break;
         case "story":
            story();
            break;
         case "limitless":
            limitless();
            break;
         case "tierA":
            for (String t : TIER_A) {
               technique(t);
            }
            break;
         default:
            if (scenario.startsWith("tech:")) {
               technique(scenario.substring(5));
               return;
            }

            foundation();
            story();
      }
   }

   private static void setup() {
      AutoTest.cmd("time set noon");
      AutoTest.cmd("gamerule doDaylightCycle false");
      AutoTest.cmd("gamerule doMobSpawning false");
      AutoTest.cmd("weather clear");
   }

   static void foundation() {
      setup();
      AutoTest.cmd("jjk set status @s non_sorcerer");
      AutoTest.pause(20);
      AutoTest.shot("01_ordinary_no_bar");
      AutoTest.cmd("jjk set status @s awakened");
      AutoTest.cmd("jjk set energy @s full");
      AutoTest.pause(30);
      AutoTest.shot("02_awakened_weak_bar");
      AutoTest.cmd("jjk set status @s student");
      AutoTest.cmd("jjk set grade @s grade_1");
      AutoTest.cmd("jjk set energy @s 80");
      AutoTest.pause(30);
      AutoTest.shot("03_grade1_low_energy");
      AutoTest.cmd("jjk set technique @s mythical_beast_amber");
      AutoTest.cmd("jjk set energy @s full");
      AutoTest.pause(30);
      AutoTest.shot("04_electric_nature");
      AutoTest.cmd("jjk set technique @s limitless");
      AutoTest.cmd("jjk trait add @s six_eyes");
      AutoTest.cmd("jjk info");
      AutoTest.pause(30);
      AutoTest.shot("05_info");
      AutoTest.cmd("jjk set energy @s 100");
      AutoTest.add(2, () -> Minecraft.getInstance().options.keyShift.setDown(true));
      AutoTest.pause(90);
      AutoTest.shot("06_meditating");
      AutoTest.add(2, () -> Minecraft.getInstance().options.keyShift.setDown(false));
      AutoTest.add(2, () -> ClientConfig.ENERGY_BAR_ANCHOR.set(HudAnchor.TOP_LEFT));
      AutoTest.pause(10);
      AutoTest.shot("07_corner_layout");
      AutoTest.add(2, () -> ClientConfig.ENERGY_BAR_ANCHOR.set(HudAnchor.HOTBAR));
   }

   static void sorcerer(String technique) {
      setup();
      AutoTest.cmd("gamemode creative");
      AutoTest.cmd("jjk set status @s student");
      AutoTest.cmd("jjk set grade @s special_grade");
      AutoTest.cmd("jjk set technique @s " + technique);

      for (String flag : new String[]{"rct", "domain", "world_slash", "shikigami:nue"}) {
         AutoTest.cmd("jjk unlock @s " + flag);
      }

      AutoTest.cmd("jjk cleanse @s");
      AutoTest.cmd("jjk set energy @s full");
      AutoTest.cmd("kill @e[type=!player]");
      AutoTest.cmd("tp @s 0 -60 0 0 0");
   }

   static void dummies(int count, int distance) {
      for (int i = 0; i < count; i++) {
         int x = (i - count / 2) * 2;
         AutoTest.cmd(
            "execute at @s run summon husk ~"
               + x
               + " -60 ~"
               + distance
               + " {PersistenceRequired:1b,attributes:[{id:\"minecraft:generic.movement_speed\",base:0.0}]}"
         );
      }
   }

   static void closeScreens() {
      AutoTest.add(1, () -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.screen != null && mc.player != null) {
            mc.player.closeContainer();
            mc.setScreen(null);
         }
      });
   }

   static void press(int index) {
      AutoTest.add(1, () -> AbilityInput.send(index, true));
   }

   static void release(int index) {
      AutoTest.add(1, () -> AbilityInput.send(index, false));
   }

   static void energy() {
      AutoTest.cmd("jjk set energy @s full");
      AutoTest.cmd("jjk cooldowns @s");
   }

   static void technique(String id) {
      Technique technique = TechniqueRegistry.get(ModMain.id(id)).orElse(null);
      if (technique == null) {
         AutoTest.log("unknown technique " + id);
      } else {
         closeScreens();
         sorcerer(id);
         int area = Arrays.asList(TIER_A).indexOf(id) + 1;
         AutoTest.cmd("tp @s " + area * 300 + " -60 0 0 0");
         AutoTest.cmd("time set 6000");
         dummies(5, 8);
         AutoTest.pause(30);
         List<Ability> list = technique.abilities();

         for (int i = 0; i < list.size(); i++) {
            Ability ability = list.get(i);
            String tag = id + "_" + String.format("%02d", i) + "_" + ability.name();
            AutoTest.cmd("jjk cleanse @s");
            energy();
            switch (ability.kind()) {
               case CHARGE:
                  press(i);
                  AutoTest.pause(Math.max(5, ability.defaults().chargeTicks() - 6));
                  AutoTest.shot(tag + "_charging");
                  AutoTest.pause(8);
                  release(i);
                  AutoTest.pause(5);
                  AutoTest.shot(tag);
                  AutoTest.pause(12);
                  AutoTest.shot(tag + "_late");
                  break;
               case CHANNEL:
                  press(i);
                  AutoTest.pause(25);
                  AutoTest.shot(tag);
                  release(i);
                  break;
               case TOGGLE:
                  press(i);
                  AutoTest.pause(15);
                  AutoTest.shot(tag);
                  press(i);
                  break;
               default:
                  press(i);
                  AutoTest.pause(5);
                  AutoTest.shot(tag);
                  AutoTest.pause(14);
                  AutoTest.shot(tag + "_late");
            }

            closeScreens();
            AutoTest.pause(20);
            AutoTest.cmd("kill @e[type=minecraft:husk]");
            dummies(5, 8);
            AutoTest.pause(5);
         }

         if (technique.domain().isPresent()) {
            AutoTest.cmd("jjk cleanse @s");
            energy();
            AutoTest.add(2, () -> PacketDistributor.sendToServer(new DomainPayloads.Input(), new CustomPacketPayload[0]));
            AutoTest.pause(45);
            AutoTest.shot(id + "_domain_forming");
            AutoTest.pause(40);
            AutoTest.shot(id + "_domain_inside");
            AutoTest.pause(40);
            AutoTest.shot(id + "_domain_inside_late");
            AutoTest.cmd("jjk domain end @s");
            AutoTest.pause(40);
         }

         AutoTest.cmd("kill @e[type=!player]");
         AutoTest.log(id + " done");
      }
   }

   static void limitless() {
      sorcerer("limitless");
      dummies(5, 9);
      AutoTest.pause(30);
      press(0);
      AutoTest.pause(10);
      AutoTest.shot("l01_infinity_hud");
      press(1);
      AutoTest.pause(6);
      AutoTest.shot("l02_blue_pull");
      AutoTest.pause(20);
      AutoTest.shot("l03_blue_late");
      energy();
      press(2);
      AutoTest.pause(30);
      release(2);
      AutoTest.pause(5);
      AutoTest.shot("l04_red_flight");
      AutoTest.pause(6);
      AutoTest.shot("l05_red_blast");
      energy();
      dummies(5, 12);
      press(5);
      AutoTest.pause(30);
      AutoTest.shot("l06_purple_windup");
      AutoTest.pause(25);
      release(5);
      AutoTest.pause(4);
      AutoTest.shot("l07_purple_launch");
      AutoTest.pause(10);
      AutoTest.shot("l08_purple_carving");
      energy();
      AutoTest.add(5, () -> {
         AbilityWheelScreen.debugHold = true;
         Minecraft.getInstance().setScreen(new AbilityWheelScreen());
      });
      AutoTest.pause(5);
      AutoTest.shot("l09_wheel");
      AutoTest.add(2, () -> {
         AbilityWheelScreen.debugHold = false;
         Minecraft.getInstance().setScreen(null);
      });
      AutoTest.cmd("jjk cleanse @s");
      energy();
      dummies(4, 6);
      AutoTest.add(2, () -> PacketDistributor.sendToServer(new DomainPayloads.Input(), new CustomPacketPayload[0]));
      AutoTest.pause(20);
      AutoTest.shot("l10_domain_handsign");
      AutoTest.pause(25);
      AutoTest.shot("l11_domain_forming");
      AutoTest.pause(30);
      AutoTest.shot("l12_unlimited_void_inside");
      AutoTest.cmd("jjk domain end @s");
      AutoTest.pause(40);
      AutoTest.cmd("jjk cleanse @s");
      energy();
      AutoTest.add(2, () -> PacketDistributor.sendToServer(new DomainPayloads.Input(), new CustomPacketPayload[0]));
      AutoTest.pause(10);
      AutoTest.cmd("tp @s 0 -55 -40 0 5");
      AutoTest.pause(60);
      AutoTest.shot("l13_unlimited_void_outside");
      AutoTest.log("limitless scenario done");
   }

   static void story() {
      setup();
      AutoTest.cmd("jjk set status @s non_sorcerer");
      AutoTest.cmd("clear @s");
      AutoTest.cmd("kill @e[type=cursed_domain:grade_4_curse]");
      AutoTest.cmd("tp @s 0 -60 0 0 10");
      AutoTest.cmd("summon cursed_domain:grade_4_curse 0 -60 3 {NoAI:1b,Rotation:[180f,0f]}");
      AutoTest.pause(20);
      AutoTest.shot("s01_curse_invisible_to_ordinary_person");
      AutoTest.cmd("jjk awaken @s");
      AutoTest.pause(4);
      AutoTest.shot("s02_awakening_flash");
      AutoTest.pause(25);
      AutoTest.shot("s03_awakening_vignette_title");
      AutoTest.pause(110);
      AutoTest.shot("s04_curse_now_visible");
      AutoTest.cmd("time set midnight");
      AutoTest.pause(20);
      AutoTest.shot("s05_curse_eyes_at_night");
      AutoTest.cmd("time set noon");
      AutoTest.add(5, () -> Minecraft.getInstance().player.getInventory().selected = 0);
      AutoTest.add(10, () -> Minecraft.getInstance().gameMode.useItem(Minecraft.getInstance().player, InteractionHand.MAIN_HAND));
      AutoTest.pause(15);
      AutoTest.shot("s06_letter_screen");
      AutoTest.add(5, () -> Minecraft.getInstance().setScreen(null));
      AutoTest.cmd("jjk info");
      AutoTest.pause(10);
      AutoTest.add(10, () -> Minecraft.getInstance().gameMode.useItem(Minecraft.getInstance().player, InteractionHand.MAIN_HAND));
      AutoTest.add(2, () -> Minecraft.getInstance().player.setYRot(HintTrail.lastYaw()));
      AutoTest.pause(30);
      AutoTest.shot("s07_hint_trail");
      AutoTest.cmd("jjk locate school");
      AutoTest.cmd("give @s cursed_domain:cursed_object");
      AutoTest.pause(10);
      AutoTest.shot("s08_hint_message_and_items");
      AutoTest.cmd("tp @s 0 -60 0 90 20");
      AutoTest.cmd("jjk event seal_break -6 -60 0");
      AutoTest.pause(8);
      AutoTest.shot("s09_seal_break");
      AutoTest.log("story scenario done");
   }
}
