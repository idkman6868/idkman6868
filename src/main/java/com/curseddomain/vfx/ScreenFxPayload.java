package com.curseddomain.vfx;

import com.curseddomain.ModMain;
import com.curseddomain.network.ClientBridge;
import com.curseddomain.network.ModCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScreenFxPayload(ScreenFxPayload.Kind kind, int color, float intensity, int duration) implements CustomPacketPayload {
   public static final Type<ScreenFxPayload> TYPE = new Type(ModMain.id("screen_fx"));
   public static final StreamCodec<ByteBuf, ScreenFxPayload> STREAM_CODEC = StreamCodec.composite(
      ModCodecs.enumCodec(ScreenFxPayload.Kind.class),
      ScreenFxPayload::kind,
      ByteBufCodecs.INT,
      ScreenFxPayload::color,
      ByteBufCodecs.FLOAT,
      ScreenFxPayload::intensity,
      ByteBufCodecs.VAR_INT,
      ScreenFxPayload::duration,
      ScreenFxPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(ScreenFxPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientBridge.dispatch(payload));
   }

   public static enum Kind {
      FLASH,
      SHAKE,
      TINT,
      WHITEOUT;
   }
}
