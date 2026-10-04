package com.steelballrun.client;

import com.steelballrun.SteelBallRun;
import com.steelballrun.network.ClientBridge;
import com.steelballrun.network.HorseStatusPayload;
import com.steelballrun.network.RaceStatusPayload;
import com.steelballrun.registry.SbrEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SteelBallRun.MODID, dist = Dist.CLIENT)
public final class SbrClient {
   public SbrClient(IEventBus modBus, ModContainer container) {
      container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
      modBus.addListener(SbrClient::registerGuiLayers);
      modBus.addListener(SbrClient::registerRenderers);
      ClientBridge.register(RaceStatusPayload.class, ClientRace::update);
      ClientBridge.register(HorseStatusPayload.class, ClientRace::updateHorse);
   }

   private static void registerGuiLayers(RegisterGuiLayersEvent event) {
      event.registerAboveAll(SteelBallRun.id("race_panel"), new RaceHud());
      event.registerAboveAll(SteelBallRun.id("horse_stamina"), new HorseHud());
   }

   private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(SbrEntities.STEPHEN_STEEL.get(), NpcRenderers.Steel::new);
      event.registerEntityRenderer(SbrEntities.RIVAL_RIDER.get(), NpcRenderers.Rival::new);
   }
}
