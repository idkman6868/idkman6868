package com.curseddomain.network.payload;

import com.curseddomain.ModMain;
import com.curseddomain.network.ClientBridge;
import com.curseddomain.network.ModCodecs;
import com.curseddomain.sorcerer.AwakeningCause;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class StoryPayloads {
   private StoryPayloads() {
   }

   public static void toClient(CustomPacketPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientBridge.dispatch(payload));
   }

   public record Awakening(AwakeningCause cause) implements CustomPacketPayload {
      public static final Type<StoryPayloads.Awakening> TYPE = new Type(ModMain.id("awakening"));
      public static final StreamCodec<ByteBuf, StoryPayloads.Awakening> STREAM_CODEC = ModCodecs.enumCodec(AwakeningCause.class)
         .map(StoryPayloads.Awakening::new, StoryPayloads.Awakening::cause);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record HintTrail(float yaw, float distance) implements CustomPacketPayload {
      public static final Type<StoryPayloads.HintTrail> TYPE = new Type(ModMain.id("hint_trail"));
      public static final StreamCodec<ByteBuf, StoryPayloads.HintTrail> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT, StoryPayloads.HintTrail::yaw, ByteBufCodecs.FLOAT, StoryPayloads.HintTrail::distance, StoryPayloads.HintTrail::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }

   public record OpenLetter(boolean firstRead) implements CustomPacketPayload {
      public static final Type<StoryPayloads.OpenLetter> TYPE = new Type(ModMain.id("open_letter"));
      public static final StreamCodec<ByteBuf, StoryPayloads.OpenLetter> STREAM_CODEC = ByteBufCodecs.BOOL
         .map(StoryPayloads.OpenLetter::new, StoryPayloads.OpenLetter::firstRead);

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }
   }
}
