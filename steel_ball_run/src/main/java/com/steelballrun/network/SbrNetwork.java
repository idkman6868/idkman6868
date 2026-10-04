package com.steelballrun.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SbrNetwork {
   private SbrNetwork() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playToClient(RaceStatusPayload.TYPE, RaceStatusPayload.STREAM_CODEC, RaceStatusPayload::handle);
      registrar.playToClient(HorseStatusPayload.TYPE, HorseStatusPayload.STREAM_CODEC, HorseStatusPayload::handle);
   }
}
