package com.steelballrun.client;

import com.steelballrun.network.HorseStatusPayload;
import com.steelballrun.network.RaceStatusPayload;
import org.jetbrains.annotations.Nullable;

/** What the server last told this client about the race and the horse it is riding. */
public final class ClientRace {
   @Nullable
   static RaceStatusPayload status;
   @Nullable
   static HorseStatusPayload horse;

   private ClientRace() {
   }

   static void update(RaceStatusPayload payload) {
      status = payload;
   }

   static void updateHorse(HorseStatusPayload payload) {
      horse = payload.horse() < 0 ? null : payload;
   }
}
