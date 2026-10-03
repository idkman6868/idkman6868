package com.curseddomain.incarnation;

import com.curseddomain.ModMain;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

/** Tells clients that a player has incarnated into a villager's body, so they render that player as a villager. */
public record IncarnationPayload(int entityId, String profession) implements CustomPacketPayload {
   public static final Type<IncarnationPayload> TYPE = new Type<IncarnationPayload>(ModMain.id("incarnation"));
   public static final StreamCodec<ByteBuf, IncarnationPayload> STREAM_CODEC = StreamCodec.of(
      (buf, payload) -> write((ByteBuf)buf, (IncarnationPayload)payload), buf -> read((ByteBuf)buf)
   );

   private static void write(ByteBuf buf, IncarnationPayload payload) {
      byte[] text = payload.profession.getBytes(StandardCharsets.UTF_8);
      buf.writeInt(payload.entityId);
      buf.writeInt(text.length);
      buf.writeBytes(text);
   }

   private static IncarnationPayload read(ByteBuf buf) {
      int id = buf.readInt();
      int length = Math.max(0, Math.min(256, buf.readInt()));
      byte[] text = new byte[length];
      buf.readBytes(text);
      return new IncarnationPayload(id, new String(text, StandardCharsets.UTF_8));
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
