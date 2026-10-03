package com.curseddomain.domain;

import com.curseddomain.ModMain;
import com.curseddomain.network.ClientBridge;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DomainPayloads {
   private DomainPayloads() {
   }

   static void sendClash(ServerPlayer player, float share, int remaining) {
      PacketDistributor.sendToPlayer(player, new DomainPayloads.Clash(share, remaining), new CustomPacketPayload[0]);
   }

   public record Clash(float share, int remainingTicks) implements CustomPacketPayload {
      public static final Type<DomainPayloads.Clash> TYPE = new Type(ModMain.id("domain_clash"));
      public static final StreamCodec<ByteBuf, DomainPayloads.Clash> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.FLOAT, DomainPayloads.Clash::share, ByteBufCodecs.VAR_INT, DomainPayloads.Clash::remainingTicks, DomainPayloads.Clash::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }

      public static void handle(DomainPayloads.Clash payload, IPayloadContext context) {
         context.enqueueWork(() -> ClientBridge.dispatch(payload));
      }
   }

   public record Input() implements CustomPacketPayload {
      public static final Type<DomainPayloads.Input> TYPE = new Type(ModMain.id("domain_input"));
      public static final StreamCodec<ByteBuf, DomainPayloads.Input> STREAM_CODEC = StreamCodec.unit(new DomainPayloads.Input());

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }

      public static void handle(DomainPayloads.Input payload, IPayloadContext context) {
         if (context.player() instanceof ServerPlayer player) {
            DomainManager.input(player);
         }
      }
   }
}
