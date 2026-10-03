package com.curseddomain.client.dev;

import com.curseddomain.ModMain;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

@EventBusSubscriber(
   modid = "cursed_domain",
   value = {Dist.CLIENT}
)
public final class AutoTest {
   static final String SCENARIO = System.getProperty("curseddomain.autotest");
   private static final String WORLD = "cursed-domain-autotest";
   private static final Deque<AutoTest.Step> QUEUE = new ArrayDeque<>();
   private static boolean worldRequested;
   private static boolean scripted;
   private static int wait;
   private static int titleTicks;
   private static File outDir;

   private AutoTest() {
   }

   @SubscribeEvent
   public static void onTick(Post event) {
      if (SCENARIO != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            titleTicks++;
            if (mc.screen instanceof AccessibilityOnboardingScreen) {
               mc.options.onboardAccessibility = false;
               mc.setScreen(new TitleScreen());
            }

            if (!worldRequested && mc.screen instanceof TitleScreen && titleTicks > 30) {
               worldRequested = true;
               createWorld(mc);
            }
         } else if (mc.player != null) {
            if (!scripted) {
               if (mc.player.tickCount < 60) {
                  return;
               }

               scripted = true;
               outDir = new File(new File(mc.gameDirectory, "cursed-domain-autotest"), SCENARIO);
               outDir.mkdirs();
               AutoScenarios.run(SCENARIO);
               finish();
            }

            if (wait > 0) {
               wait--;
            } else {
               AutoTest.Step step = QUEUE.poll();
               if (step != null) {
                  wait = step.delay();

                  try {
                     step.action().run();
                  } catch (RuntimeException var4) {
                     ModMain.LOGGER.error("[autotest] step failed", var4);
                  }
               }
            }
         }
      }
   }

   private static void createWorld(Minecraft mc) {
      ModMain.LOGGER.info("[autotest] creating test world for scenario {}", SCENARIO);
      mc.options.pauseOnLostFocus = false;
      mc.options.tutorialStep = TutorialSteps.NONE;
      deleteOldWorld(mc);
      WorldOpenFlows flows = mc.createWorldOpenFlows();
      LevelSettings settings = new LevelSettings(
         "cursed-domain-autotest", GameType.SURVIVAL, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT
      );
      flows.createFreshLevel(
         "cursed-domain-autotest",
         settings,
         new WorldOptions(12345L, false, false),
         reg -> ((WorldPreset)reg.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()).createWorldDimensions(),
         mc.screen
      );
   }

   private static void deleteOldWorld(Minecraft mc) {
      Path dir = mc.gameDirectory.toPath().resolve("saves").resolve("cursed-domain-autotest");
      if (Files.isDirectory(dir)) {
         try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
         } catch (IOException var7) {
            ModMain.LOGGER.warn("[autotest] could not clear the old test world", var7);
         }
      }
   }

   static void cmd(String command) {
      add(3, () -> Minecraft.getInstance().player.connection.sendCommand(command));
   }

   static void add(int delay, Runnable action) {
      QUEUE.add(new AutoTest.Step(delay, action));
   }

   static void pause(int ticks) {
      add(ticks, () -> {});
   }

   static void shot(String name) {
      add(1, () -> {
         Minecraft mc = Minecraft.getInstance();

         try {
            NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget());

            try {
               image.writeToFile(new File(outDir, name + ".png"));
               ModMain.LOGGER.info("[autotest] screenshot {}", name);
            } catch (Throwable var6) {
               if (image != null) {
                  try {
                     image.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (image != null) {
               image.close();
            }
         } catch (Exception var7) {
            ModMain.LOGGER.error("[autotest] screenshot failed", var7);
         }
      });
   }

   static void log(String message) {
      add(0, () -> ModMain.LOGGER.info("[autotest] {}", message));
   }

   private static void finish() {
      add(5, () -> {
         ModMain.LOGGER.info("[autotest] DONE - screenshots in {}", outDir);
         Minecraft.getInstance().stop();
      });
   }

   private record Step(int delay, Runnable action) {
   }
}
