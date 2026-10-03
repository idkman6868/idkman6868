package com.curseddomain.school;

import com.curseddomain.ModMain;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.cullinggame.Kogane;
import com.curseddomain.cullinggame.npc.NpcProfile;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.shibuya.CurseVariant;
import com.curseddomain.shibuya.RampantCurse;
import com.curseddomain.sorcerer.AwakeningManager;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.sorcerer.StoryData;
import com.curseddomain.technique.Technique;
import com.curseddomain.vfx.Vfx;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import org.jetbrains.annotations.Nullable;

/**
 * Enrolment at Tokyo or Kyoto Jujutsu High: the principal's question, the entrance exam, and the final question.
 * Passing makes you a student, which reveals and unlocks your innate technique.
 */
@EventBusSubscriber(modid = ModMain.MODID)
public final class JujutsuHigh {
   private static final Map<UUID, JujutsuHigh.Exam> EXAMS = new HashMap<>();
   private static final Map<UUID, Integer> STRIKES = new HashMap<>();
   private static final long EXAM_TICKS = 3600L;
   private static final long RETRY_TICKS = 3600L;

   private JujutsuHigh() {
   }

   enum Stage {
      ASKED,
      TESTING,
      FINAL;
   }

   static final class Exam {
      final boolean kyoto;
      final UUID principal;
      JujutsuHigh.Stage stage = JujutsuHigh.Stage.ASKED;
      final List<UUID> foes = new ArrayList<>();
      long deadline;

      Exam(boolean kyoto, UUID principal) {
         this.kyoto = kyoto;
         this.principal = principal;
      }
   }

   private static String speaker(boolean kyoto) {
      return "entity.cursed_domain.sorcerer_npc." + (kyoto ? "yoshinobu_gakuganji" : "masamichi_yaga");
   }

   private static void say(ServerPlayer player, boolean kyoto, String key, Object... args) {
      Kogane.speech(player, speaker(kyoto), Component.translatable("school.cursed_domain." + (kyoto ? "kyoto." : "tokyo.") + key, args), ChatFormatting.DARK_AQUA);
   }

   private static void choices(ServerPlayer player, boolean kyoto, String question, int count) {
      MutableComponent line = Component.literal("  ");

      for (int i = 1; i <= count; i++) {
         String command = "/interview " + i;
         String key = "school.cursed_domain." + (kyoto ? "kyoto." : "tokyo.") + question + "." + i;
         line.append(
            Component.literal("[" + i + "] ")
               .append(Component.translatable(key))
               .withStyle(
                  style -> style.withColor(ChatFormatting.AQUA)
                     .withClickEvent(new ClickEvent(Action.RUN_COMMAND, command))
                     .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("school.cursed_domain.click")))
               )
         );
         line.append(Component.literal("  "));
      }

      player.sendSystemMessage(line);
   }

   // ------------------------------------------------------------------------------------------------ talking to a principal

   public static void interact(ServerPlayer player, SorcererNpcEntity principal) {
      boolean kyoto = principal.profile() == NpcProfile.YOSHINOBU_GAKUGANJI;
      SorcererData data = SorcererManager.get(player);
      StoryData story = AwakeningManager.story(player);
      long now = player.serverLevel().getGameTime();
      JujutsuHigh.Exam exam = EXAMS.get(player.getUUID());
      if (data.status() == SorcererStatus.NON_SORCERER) {
         say(player, kyoto, "cant_see");
      } else if (data.status() == SorcererStatus.CURSE_USER) {
         say(player, kyoto, "curse_user");
      } else if (data.status().enrolled()) {
         boolean ours = data.isUnlocked(kyoto ? "school:kyoto" : "school:tokyo");
         say(player, kyoto, ours ? "student." + (1 + player.getRandom().nextInt(3)) : "visitor");
      } else if (exam != null) {
         if (exam.stage == JujutsuHigh.Stage.TESTING) {
            say(player, kyoto, "focus");
         } else {
            ask(player, exam);
         }
      } else if (story.interviewCooldownUntil > now) {
         say(player, kyoto, "come_back", (story.interviewCooldownUntil - now) / 20L + 1L);
      } else {
         exam = new JujutsuHigh.Exam(kyoto, principal.getUUID());
         EXAMS.put(player.getUUID(), exam);
         say(player, kyoto, "greet");
         ask(player, exam);
      }
   }

   private static void ask(ServerPlayer player, JujutsuHigh.Exam exam) {
      if (exam.stage == JujutsuHigh.Stage.ASKED) {
         say(player, exam.kyoto, "question");
         choices(player, exam.kyoto, "answer", 4);
      } else if (exam.stage == JujutsuHigh.Stage.FINAL) {
         say(player, exam.kyoto, "final");
         choices(player, exam.kyoto, "final_answer", 2);
      }
   }

   /** /interview <n> */
   private static int answer(ServerPlayer player, int choice) {
      JujutsuHigh.Exam exam = EXAMS.get(player.getUUID());
      SorcererNpcEntity principal = exam == null ? null : principal(player, exam);
      if (exam == null || principal == null || principal.distanceTo(player) > 16.0F) {
         player.sendSystemMessage(Component.translatable("school.cursed_domain.no_question").withStyle(ChatFormatting.GRAY));
         return 0;
      } else if (exam.stage == JujutsuHigh.Stage.ASKED && choice >= 1 && choice <= 4) {
         say(player, exam.kyoto, "response." + choice);
         startTest(player, exam, principal, choice);
         return 1;
      } else if (exam.stage == JujutsuHigh.Stage.FINAL && choice >= 1 && choice <= 2) {
         say(player, exam.kyoto, "final_response." + choice);
         enroll(player, exam.kyoto);
         EXAMS.remove(player.getUUID());
         return 1;
      } else {
         say(player, exam.kyoto, "focus");
         return 0;
      }
   }

   @Nullable
   private static SorcererNpcEntity principal(ServerPlayer player, JujutsuHigh.Exam exam) {
      Entity entity = player.serverLevel().getEntity(exam.principal);
      return entity instanceof SorcererNpcEntity npc && npc.isAlive() ? npc : null;
   }

   /** Yaga sends his cursed corpses; Gakuganji lets loose two curses the school keeps for the purpose. */
   private static void startTest(ServerPlayer player, JujutsuHigh.Exam exam, SorcererNpcEntity principal, int choice) {
      ServerLevel level = player.serverLevel();
      SorcererManager.setStatus(player, SorcererStatus.ENTRANCE_MISSION);
      exam.stage = JujutsuHigh.Stage.TESTING;
      exam.deadline = level.getGameTime() + EXAM_TICKS;
      int count = choice == 3 ? 3 : 2;

      for (int i = 0; i < count; i++) {
         double angle = Math.PI * 2.0 * i / count;
         Vec3 at = player.position().add(Math.cos(angle) * 4.0, 0.2, Math.sin(angle) * 4.0);
         LivingEntity foe;
         if (exam.kyoto) {
            foe = RampantCurse.spawn(level, CurseVariant.GRADE_3, at, false);
         } else {
            foe = SorcererNpcEntity.spawn(level, NpcProfile.CURSED_CORPSE, at, 0, false);
         }

         if (foe instanceof RampantCurse curse) {
            curse.setTarget(player);
            exam.foes.add(curse.getUUID());
         } else if (foe instanceof SorcererNpcEntity corpse) {
            corpse.setTarget(player);
            exam.foes.add(corpse.getUUID());
         }
      }

      Vfx.ring(level, principal.position().add(0.0, 0.2, 0.0), -9794982, 5.0F, 20);
      level.playSound(null, principal.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.NEUTRAL, 1.0F, 0.8F);
      Kogane.title(
         player,
         Component.translatable("school.cursed_domain.exam.title").withStyle(ChatFormatting.DARK_AQUA),
         Component.translatable("school.cursed_domain." + (exam.kyoto ? "kyoto" : "tokyo") + ".exam.subtitle"),
         60
      );
   }

   private static void enroll(ServerPlayer player, boolean kyoto) {
      SorcererManager.setStatus(player, SorcererStatus.STUDENT);
      SorcererManager.unlock(player, kyoto ? "school:kyoto" : "school:tokyo");
      AwakeningManager.story(player).interviewCooldownUntil = 0L;
      SorcererData data = SorcererManager.get(player);
      Component technique = data.technique().map(Technique::displayName).orElse(Component.translatable("school.cursed_domain.no_technique"));
      Kogane.title(
         player,
         Component.translatable("school.cursed_domain." + (kyoto ? "kyoto" : "tokyo") + ".welcome").withStyle(new ChatFormatting[]{ChatFormatting.AQUA, ChatFormatting.BOLD}),
         Component.translatable("school.cursed_domain.technique", technique),
         100
      );
      player.sendSystemMessage(Component.translatable("school.cursed_domain.enrolled", technique).withStyle(ChatFormatting.AQUA));
      player.sendSystemMessage(Component.translatable("school.cursed_domain.menu_hint").withStyle(ChatFormatting.GRAY));
      Vfx.pillar(player.serverLevel(), player.position(), -4663041, 4.0F, 30);
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
      ModMain.LOGGER.info("[school] {} enrolled at {}", player.getScoreboardName(), kyoto ? "Kyoto" : "Tokyo");
   }

   private static void fail(ServerPlayer player, JujutsuHigh.Exam exam, String why) {
      discardFoes(player.serverLevel(), exam);
      if (SorcererManager.get(player).status() == SorcererStatus.ENTRANCE_MISSION) {
         SorcererManager.setStatus(player, SorcererStatus.APPLICANT);
      }

      AwakeningManager.story(player).interviewCooldownUntil = player.serverLevel().getGameTime() + RETRY_TICKS;
      say(player, exam.kyoto, "fail." + why);
   }

   private static void discardFoes(ServerLevel level, JujutsuHigh.Exam exam) {
      for (UUID id : exam.foes) {
         Entity foe = level.getEntity(id);
         if (foe != null && foe.isAlive()) {
            foe.discard();
         }
      }

      exam.foes.clear();
   }

   // ------------------------------------------------------------------------------------------------ the curse user path

   /** Attacking a principal. With the curse user path enabled, attacking Yaga twice makes you a curse user. */
   public static void principalHit(ServerPlayer player, SorcererNpcEntity principal) {
      boolean kyoto = principal.profile() == NpcProfile.YOSHINOBU_GAKUGANJI;
      SorcererData data = SorcererManager.get(player);
      if (kyoto || data.status().enrolled() || !data.status().awakened() || !(Boolean)ServerConfig.ALLOW_CURSE_USER_PATH.get()) {
         say(player, kyoto, "hit");
      } else {
         int strikes = STRIKES.merge(player.getUUID(), 1, Integer::sum);
         if (strikes < 2) {
            say(player, false, "hit_warning");
         } else {
            STRIKES.remove(player.getUUID());
            JujutsuHigh.Exam exam = EXAMS.remove(player.getUUID());
            if (exam != null) {
               discardFoes(player.serverLevel(), exam);
            }

            SorcererManager.setStatus(player, SorcererStatus.CURSE_USER);
            say(player, false, "curse_user_path");
            Kogane.title(
               player,
               Component.translatable("school.cursed_domain.curse_user.title").withStyle(ChatFormatting.DARK_RED),
               Component.translatable("school.cursed_domain.curse_user.subtitle"),
               80
            );
            ServerLevel level = player.serverLevel();

            for (int i = 0; i < 2; i++) {
               SorcererNpcEntity corpse = SorcererNpcEntity.spawn(level, NpcProfile.CURSED_CORPSE, principal.position().add(i * 2 - 1.0, 0.2, 1.5), 0, false);
               if (corpse != null) {
                  corpse.setTarget(player);
               }
            }
         }
      }
   }

   // ------------------------------------------------------------------------------------------------ ticking & commands

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = event.getServer();
      if (server.getTickCount() % 20 == 0 && !EXAMS.isEmpty()) {
         Iterator<Map.Entry<UUID, JujutsuHigh.Exam>> it = EXAMS.entrySet().iterator();

         while (it.hasNext()) {
            Map.Entry<UUID, JujutsuHigh.Exam> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            JujutsuHigh.Exam exam = entry.getValue();
            if (player == null) {
               it.remove();
            } else if (exam.stage == JujutsuHigh.Stage.TESTING) {
               SorcererNpcEntity principal = principal(player, exam);
               ServerLevel level = player.serverLevel();
               if (!player.isAlive()) {
                  fail(player, exam, "died");
                  it.remove();
               } else if (principal == null || principal.distanceTo(player) > 48.0F) {
                  fail(player, exam, "fled");
                  it.remove();
               } else if (level.getGameTime() > exam.deadline) {
                  fail(player, exam, "time");
                  it.remove();
               } else {
                  boolean cleared = true;

                  for (UUID id : exam.foes) {
                     Entity foe = level.getEntity(id);
                     if (foe != null && foe.isAlive()) {
                        cleared = false;
                     }
                  }

                  if (cleared) {
                     exam.foes.clear();
                     exam.stage = JujutsuHigh.Stage.FINAL;
                     say(player, exam.kyoto, "passed");
                     ask(player, exam);
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         JujutsuHigh.Exam exam = EXAMS.remove(player.getUUID());
         if (exam != null && exam.stage == JujutsuHigh.Stage.TESTING) {
            fail(player, exam, "fled");
         }

         STRIKES.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onStopped(ServerStoppedEvent event) {
      EXAMS.clear();
      STRIKES.clear();
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("interview");
      RequiredArgumentBuilder<CommandSourceStack, Integer> choice = Commands.argument("choice", IntegerArgumentType.integer(1, 4));
      choice.executes(ctx -> answer(player(ctx), IntegerArgumentType.getInteger(ctx, "choice")));
      root.then(choice);
      event.getDispatcher().register(root);
   }

   private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      return ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
   }
}
