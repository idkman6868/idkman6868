package com.steelballrun.network;

import com.steelballrun.SteelBallRun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** What the race panel shows. Sent once a second to every player while the race exists. */
public record RaceStatusPayload(
   int phase, boolean registered, int nextGate, int gateX, int gateZ, int place, int field, int overall, int points, int seconds, int startDay,
   int finishPlace
) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<RaceStatusPayload> TYPE = new CustomPacketPayload.Type<>(SteelBallRun.id("race_status"));
   public static final StreamCodec<ByteBuf, RaceStatusPayload> STREAM_CODEC = StreamCodec.of(RaceStatusPayload::write, RaceStatusPayload::read);

   private static void write(ByteBuf buf, RaceStatusPayload p) {
      buf.writeByte(p.phase);
      buf.writeBoolean(p.registered);
      buf.writeInt(p.nextGate);
      buf.writeInt(p.gateX);
      buf.writeInt(p.gateZ);
      buf.writeInt(p.place);
      buf.writeInt(p.field);
      buf.writeInt(p.overall);
      buf.writeInt(p.points);
      buf.writeInt(p.seconds);
      buf.writeInt(p.startDay);
      buf.writeInt(p.finishPlace);
   }

   private static RaceStatusPayload read(ByteBuf buf) {
      return new RaceStatusPayload(
         buf.readByte(), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
         buf.readInt(), buf.readInt(), buf.readInt()
      );
   }

   @Override
   public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(RaceStatusPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientBridge.dispatch(payload));
   }
}
