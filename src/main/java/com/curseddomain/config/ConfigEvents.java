package com.curseddomain.config;

import com.curseddomain.energy.EnergyManager;
import com.curseddomain.sorcerer.SorcererManager;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class ConfigEvents {
   private ConfigEvents() {
   }

   @SubscribeEvent
   public static void onReload(Reloading event) {
      if (event.getConfig().getSpec() == ServerConfig.SPEC) {
         MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
         if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(player -> {
               EnergyManager.recalculate(player);
               SorcererManager.syncAll(player);
            }));
         }
      }
   }
}
