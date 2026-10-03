package com.curseddomain.client.input;

import com.curseddomain.client.gui.AbilityWheelScreen;
import com.curseddomain.domain.DomainPayloads;
import com.curseddomain.network.payload.AbilityInputPayloads;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.technique.ability.AbilityState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "cursed_domain",
   value = {Dist.CLIENT}
)
public final class AbilityInput {
   private static final boolean[] HELD = new boolean[5];
   private static final int[] HELD_INDEX = new int[]{-1, -1, -1, -1, -1};

   private AbilityInput() {
   }

   @SubscribeEvent
   public static void onTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         boolean inGame = mc.screen == null;
         AbilityState state = (AbilityState)mc.player.getData(ModAttachments.ABILITY_STATE);

         for (int i = 0; i < 5; i++) {
            boolean down = inGame && ModKeys.SLOTS[i].isDown();
            if (down && !HELD[i]) {
               HELD_INDEX[i] = state.slot(i);
               send(HELD_INDEX[i], true);
            } else if (!down && HELD[i] && HELD_INDEX[i] >= 0) {
               send(HELD_INDEX[i], false);
               HELD_INDEX[i] = -1;
            }

            HELD[i] = down;
         }

         while (ModKeys.DOMAIN.consumeClick()) {
            if (inGame) {
               PacketDistributor.sendToServer(new DomainPayloads.Input(), new CustomPacketPayload[0]);
            }
         }

         while (ModKeys.WHEEL.consumeClick()) {
            if (inGame && state.clientView() != null && !state.clientView().entries().isEmpty()) {
               mc.setScreen(new AbilityWheelScreen());
            }
         }
      }
   }

   public static void send(int index, boolean pressed) {
      PacketDistributor.sendToServer(new AbilityInputPayloads.Input(index, pressed), new CustomPacketPayload[0]);
   }
}
