package com.curseddomain.vfx;

import com.curseddomain.ModMain;
import com.curseddomain.network.ClientBridge;
import com.curseddomain.network.ModCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VfxPayload(VfxKind kind, Vec3 a, Vec3 b, int color, float size, int duration, int seed) implements CustomPacketPayload {
   public static final Type<VfxPayload> TYPE = new Type(ModMain.id("vfx"));
   private static final StreamCodec<ByteBuf, VfxKind> KIND = ModCodecs.enumCodec(VfxKind.class);
   public static final StreamCodec<ByteBuf, VfxPayload> STREAM_CODEC = StreamCodec.of(VfxPayload::write, VfxPayload::read);

   private static void write(ByteBuf buf, VfxPayload p) {
      KIND.encode(buf, p.kind);
      writeVec(buf, p.a);
      writeVec(buf, p.b);
      buf.writeInt(p.color);
      buf.writeFloat(p.size);
      buf.writeShort(p.duration);
      buf.writeInt(p.seed);
   }

   private static VfxPayload read(ByteBuf buf) {
      return new VfxPayload((VfxKind)KIND.decode(buf), readVec(buf), readVec(buf), buf.readInt(), buf.readFloat(), buf.readShort(), buf.readInt());
   }

   private static void writeVec(ByteBuf buf, Vec3 v) {
      buf.writeFloat((float)v.x);
      buf.writeFloat((float)v.y);
      buf.writeFloat((float)v.z);
   }

   private static Vec3 readVec(ByteBuf buf) {
      return new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(VfxPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ClientBridge.dispatch(payload));
   }
}
