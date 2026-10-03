package com.curseddomain.network;

import com.curseddomain.domain.DomainPayloads;
import com.curseddomain.network.payload.AbilityInputPayloads;
import com.curseddomain.network.payload.AbilitySyncPayload;
import com.curseddomain.network.payload.CursedEnergySyncPayload;
import com.curseddomain.network.payload.SorcererSyncPayload;
import com.curseddomain.network.payload.StoryPayloads;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.VfxPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
   public static final String PROTOCOL = "1";

   private ModNetwork() {
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1");
      registrar.playToClient(SorcererSyncPayload.TYPE, SorcererSyncPayload.STREAM_CODEC, SorcererSyncPayload::handle);
      registrar.playToClient(CursedEnergySyncPayload.TYPE, CursedEnergySyncPayload.STREAM_CODEC, CursedEnergySyncPayload::handle);
      registrar.playToClient(StoryPayloads.Awakening.TYPE, StoryPayloads.Awakening.STREAM_CODEC, StoryPayloads::toClient);
      registrar.playToClient(StoryPayloads.HintTrail.TYPE, StoryPayloads.HintTrail.STREAM_CODEC, StoryPayloads::toClient);
      registrar.playToClient(AbilitySyncPayload.TYPE, AbilitySyncPayload.STREAM_CODEC, AbilitySyncPayload::handle);
      registrar.playToServer(AbilityInputPayloads.Input.TYPE, AbilityInputPayloads.Input.STREAM_CODEC, AbilityInputPayloads.Input::handle);
      registrar.playToServer(AbilityInputPayloads.AssignSlot.TYPE, AbilityInputPayloads.AssignSlot.STREAM_CODEC, AbilityInputPayloads.AssignSlot::handle);
      registrar.playToClient(VfxPayload.TYPE, VfxPayload.STREAM_CODEC, VfxPayload::handle);
      registrar.playToClient(ScreenFxPayload.TYPE, ScreenFxPayload.STREAM_CODEC, ScreenFxPayload::handle);
      registrar.playToServer(DomainPayloads.Input.TYPE, DomainPayloads.Input.STREAM_CODEC, DomainPayloads.Input::handle);
      registrar.playToClient(DomainPayloads.Clash.TYPE, DomainPayloads.Clash.STREAM_CODEC, DomainPayloads.Clash::handle);
      registrar.playToClient(StoryPayloads.OpenLetter.TYPE, StoryPayloads.OpenLetter.STREAM_CODEC, StoryPayloads::toClient);
   }
}
