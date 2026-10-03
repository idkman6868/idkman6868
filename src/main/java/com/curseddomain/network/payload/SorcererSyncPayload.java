package com.curseddomain.network.payload;

import com.curseddomain.ModMain;
import com.curseddomain.network.ModCodecs;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererStatus;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SorcererSyncPayload(
   SorcererStatus status, Grade grade, int gradeXp, Optional<ResourceLocation> technique, List<InnateTrait> traits, List<String> unlocks
) implements CustomPacketPayload {
   public static final Type<SorcererSyncPayload> TYPE = new Type(ModMain.id("sorcerer_sync"));
   public static final StreamCodec<ByteBuf, SorcererSyncPayload> STREAM_CODEC = StreamCodec.composite(
      ModCodecs.enumCodec(SorcererStatus.class),
      SorcererSyncPayload::status,
      ModCodecs.enumCodec(Grade.class),
      SorcererSyncPayload::grade,
      ByteBufCodecs.VAR_INT,
      SorcererSyncPayload::gradeXp,
      ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
      SorcererSyncPayload::technique,
      ModCodecs.enumCodec(InnateTrait.class).apply(ByteBufCodecs.list()),
      SorcererSyncPayload::traits,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      SorcererSyncPayload::unlocks,
      SorcererSyncPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(SorcererSyncPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ((SorcererData)context.player().getData(ModAttachments.SORCERER)).applyClientView(payload));
   }
}
