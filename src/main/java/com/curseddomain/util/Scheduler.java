package com.curseddomain.util;

import com.curseddomain.ModMain;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class Scheduler {
   private static final List<Scheduler.Task> TASKS = new ArrayList<>();

   private Scheduler() {
   }

   public static void later(ServerLevel level, int delayTicks, Runnable action) {
      TASKS.add(new Scheduler.Task(level, level.getGameTime() + Math.max(1, delayTicks), action));
   }

   public static void repeat(ServerLevel level, int delay, int interval, int count, IntConsumer action) {
      for (int i = 0; i < count; i++) {
         int index = i;
         later(level, delay + i * interval, () -> action.accept(index));
      }
   }

   @SubscribeEvent
   public static void onTick(Post event) {
      if (!TASKS.isEmpty()) {
         List<Scheduler.Task> due = new ArrayList<>();
         TASKS.removeIf(taskx -> {
            if (taskx.level.getGameTime() >= taskx.runAt) {
               due.add(taskx);
               return true;
            } else {
               return false;
            }
         });

         for (Scheduler.Task task : due) {
            try {
               task.action.run();
            } catch (RuntimeException var5) {
               ModMain.LOGGER.error("Scheduled task failed", var5);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onStopping(ServerStoppingEvent event) {
      TASKS.clear();
   }

   private record Task(ServerLevel level, long runAt, Runnable action) {
   }
}
