package com.curseddomain.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class GameTestRegistration {
   private GameTestRegistration() {
   }

   @SubscribeEvent
   public static void register(RegisterGameTestsEvent event) {
      event.register(FoundationGameTests.class);
      event.register(StoryGameTests.class);
      event.register(TechniqueGameTests.class);
   }
}
