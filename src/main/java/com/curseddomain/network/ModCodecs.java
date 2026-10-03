package com.curseddomain.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ModCodecs {
   private ModCodecs() {
   }

   public static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(Class<E> type) {
      E[] values = (E[])type.getEnumConstants();
      return ByteBufCodecs.VAR_INT.map(i -> i >= 0 && i < values.length ? values[i] : values[0], Enum::ordinal);
   }
}
