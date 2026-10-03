package com.curseddomain.client.culling;

import com.curseddomain.cullinggame.CullingRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

/** Client entry point for the Shibuya Incident / Culling Game content. */
@Mod(value = "cursed_domain", dist = Dist.CLIENT)
public final class CullingClient {
   public CullingClient(IEventBus modBus) {
      modBus.addListener(CullingClient::registerRenderers);
   }

   private static void registerRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)CullingRegistries.SORCERER_NPC.get(), SorcererNpcRenderer::new);
      event.registerEntityRenderer((EntityType)CullingRegistries.RAMPANT_CURSE.get(), RampantCurseRenderer::new);
   }
}
