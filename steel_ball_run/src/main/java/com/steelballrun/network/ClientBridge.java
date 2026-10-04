package com.steelballrun.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Lets common code hand payloads to client-only code without loading client classes on a dedicated server. */
public final class ClientBridge {
   private static final Map<Class<?>, Consumer<Object>> HANDLERS = new ConcurrentHashMap<>();

   private ClientBridge() {
   }

   @SuppressWarnings("unchecked")
   public static <P extends CustomPacketPayload> void register(Class<P> type, Consumer<P> handler) {
      HANDLERS.put(type, payload -> handler.accept((P)payload));
   }

   public static void dispatch(CustomPacketPayload payload) {
      Consumer<Object> handler = HANDLERS.get(payload.getClass());
      if (handler != null) {
         handler.accept(payload);
      }
   }
}
