package com.curseddomain.network.payload;

import com.curseddomain.ModMain;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.technique.ability.AbilityState;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilitySyncPayload(List<AbilitySyncPayload.Entry> entries, List<Integer> slots, int chargingIndex, int chargeElapsed, int chargeMax)
   implements CustomPacketPayload {
   public static final Type<AbilitySyncPayload> TYPE = new Type(ModMain.id("ability_sync"));
   public static final StreamCodec<ByteBuf, AbilitySyncPayload> STREAM_CODEC = StreamCodec.composite(
      AbilitySyncPayload.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
      AbilitySyncPayload::entries,
      ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
      AbilitySyncPayload::slots,
      ByteBufCodecs.VAR_INT,
      AbilitySyncPayload::chargingIndex,
      ByteBufCodecs.VAR_INT,
      AbilitySyncPayload::chargeElapsed,
      ByteBufCodecs.VAR_INT,
      AbilitySyncPayload::chargeMax,
      AbilitySyncPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(AbilitySyncPayload payload, IPayloadContext context) {
      context.enqueueWork(
         () -> ((AbilityState)context.player().getData(ModAttachments.ABILITY_STATE)).applySync(payload, context.player().level().getGameTime())
      );
   }

   public record Entry(int cooldownRemaining, int cooldownTotal, boolean active, float cost) {
      public static final StreamCodec<ByteBuf, AbilitySyncPayload.Entry> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT,
         AbilitySyncPayload.Entry::cooldownRemaining,
         ByteBufCodecs.VAR_INT,
         AbilitySyncPayload.Entry::cooldownTotal,
         ByteBufCodecs.BOOL,
         AbilitySyncPayload.Entry::active,
         ByteBufCodecs.FLOAT,
         AbilitySyncPayload.Entry::cost,
         AbilitySyncPayload.Entry::new
      );
   }
}
