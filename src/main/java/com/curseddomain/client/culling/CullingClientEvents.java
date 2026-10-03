package com.curseddomain.client.culling;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre;

@EventBusSubscriber(modid = "cursed_domain", value = Dist.CLIENT)
public final class CullingClientEvents {
   private CullingClientEvents() {
   }

   @SubscribeEvent
   public static void onClientTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      while (CullingClient.TECHNIQUE_MENU.consumeClick()) {
         if (mc.player != null && mc.screen == null) {
            mc.setScreen(new TechniqueMenuScreen());
         }
      }

      IncarnationRenderer.tick();
   }

   @SubscribeEvent
   public static void onRenderPlayer(Pre event) {
      if (IncarnationRenderer.render(event.getEntity(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight())) {
         event.setCanceled(true);
      }
   }
}
