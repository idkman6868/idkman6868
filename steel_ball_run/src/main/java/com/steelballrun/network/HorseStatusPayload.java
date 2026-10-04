package com.steelballrun.network;

import com.steelballrun.SteelBallRun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Stamina of the horse you are riding. {@code horse == -1} clears the bar when you dismount. */
public record HorseStatusPayload(int horse, float stamina, float max, int bond, boolean exhausted, boolean galloping, int breed, int affinity)
   implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<HorseStatusPayload> TYPE = new CustomPacketPayload.Type<>(SteelBallRun.id("horse_status"));
   public static final StreamCodec<ByteBuf, HorseStatusPayload> STREAM_CODEC = StreamCodec.of(HorseStatusPayload::write, HorseStatusPayload::read);
   public static final HorseStatusPayload NONE = new HorseStatusPayload(-1, 0.0F, 0.0F, 0, false, false, 0, 0);

   private static void write(ByteBuf buf, HorseStatusPayload p) {
      buf.writeInt(p.horse);
      buf.writeFloat(p.stamina);
      buf.writeFloat(p.max);
      buf.writeByte(p.bond);
      buf.writeBoolean(p.exhausted);
      buf.writeBoolean(p.galloping);
      buf.writeByte(p.breed);
      buf.writeByte(p.affinity);
   }

   private static HorseStatusPayload read(ByteBuf buf) {
      return new HorseStatusPayload(buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readByte(), buf.readBoolean(), buf.readBoolean(), buf.readByte(), buf.readByte());
   }

   @Override
   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(HorseStatusPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientBridge.dispatch(payload));
   }
}
