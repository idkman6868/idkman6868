package com.curseddomain.client.culling;

import com.curseddomain.cullinggame.CullingRegistries;
import com.curseddomain.incarnation.IncarnationPayload;
import com.curseddomain.network.ClientBridge;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Client entry point for the Shibuya / Culling Game / Jujutsu High content. */
@Mod(value = "cursed_domain", dist = Dist.CLIENT)
public final class CullingClient {
   public static final KeyMapping TECHNIQUE_MENU = new KeyMapping(
      "key.cursed_domain.technique_menu", KeyConflictContext.IN_GAME, Type.KEYSYM, 75, "key.categories.cursed_domain"
   );

   public CullingClient(IEventBus modBus) {
      modBus.addListener(CullingClient::registerRenderers);
      modBus.addListener(CullingClient::registerKeys);
      ClientBridge.register(IncarnationPayload.class, IncarnationRenderer::update);
   }

   private static void registerRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)CullingRegistries.SORCERER_NPC.get(), SorcererNpcRenderer::new);
      event.registerEntityRenderer((EntityType)CullingRegistries.RAMPANT_CURSE.get(), RampantCurseRenderer::new);
   }

   private static void registerKeys(RegisterKeyMappingsEvent event) {
      event.register(TECHNIQUE_MENU);
   }
}
