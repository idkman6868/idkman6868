package com.curseddomain.network.payload;

import com.curseddomain.ModMain;
import com.curseddomain.technique.ability.AbilityManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AbilityInputPayloads {
   private AbilityInputPayloads() {
   }

   public record AssignSlot(int slot, int index) implements CustomPacketPayload {
      public static final Type<AbilityInputPayloads.AssignSlot> TYPE = new Type(ModMain.id("ability_assign"));
      public static final StreamCodec<ByteBuf, AbilityInputPayloads.AssignSlot> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT,
         AbilityInputPayloads.AssignSlot::slot,
         ByteBufCodecs.VAR_INT,
         AbilityInputPayloads.AssignSlot::index,
         AbilityInputPayloads.AssignSlot::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }

      public static void handle(AbilityInputPayloads.AssignSlot payload, IPayloadContext context) {
         if (context.player() instanceof ServerPlayer player) {
            AbilityManager.assignSlot(player, payload.slot(), payload.index());
         }
      }
   }

   public record Input(int index, boolean pressed) implements CustomPacketPayload {
      public static final Type<AbilityInputPayloads.Input> TYPE = new Type(ModMain.id("ability_input"));
      public static final StreamCodec<ByteBuf, AbilityInputPayloads.Input> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT, AbilityInputPayloads.Input::index, ByteBufCodecs.BOOL, AbilityInputPayloads.Input::pressed, AbilityInputPayloads.Input::new
      );

      public Type<? extends CustomPacketPayload> type() {
         return TYPE;
      }

      public static void handle(AbilityInputPayloads.Input payload, IPayloadContext context) {
         if (context.player() instanceof ServerPlayer player) {
            AbilityManager.input(player, payload.index(), payload.pressed());
         }
      }
   }
}
