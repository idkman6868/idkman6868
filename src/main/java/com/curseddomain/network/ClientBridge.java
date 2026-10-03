package com.curseddomain.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ClientBridge {
   private static final Map<Class<?>, Consumer<Object>> HANDLERS = new ConcurrentHashMap<>();

   private ClientBridge() {
   }

   public static <P extends CustomPacketPayload> void register(Class<P> type, Consumer<P> handler) {
      HANDLERS.put(type, payload -> handler.accept((CustomPacketPayload)payload));
   }

   public static void dispatch(CustomPacketPayload payload) {
      Consumer<Object> handler = HANDLERS.get(payload.getClass());
      if (handler != null) {
         handler.accept(payload);
      }
   }
}
