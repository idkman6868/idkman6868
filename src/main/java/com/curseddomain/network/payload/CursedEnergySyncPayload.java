package com.curseddomain.network.payload;

import com.curseddomain.ModMain;
import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.energy.CursedEnergyNature;
import com.curseddomain.network.ModCodecs;
import com.curseddomain.registry.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CursedEnergySyncPayload(float current, float max, float output, float control, CursedEnergyNature nature, byte flags)
   implements CustomPacketPayload {
   public static final Type<CursedEnergySyncPayload> TYPE = new Type(ModMain.id("cursed_energy_sync"));
   private static final int MEDITATING = 1;
   private static final int IN_COMBAT = 2;
   public static final StreamCodec<ByteBuf, CursedEnergySyncPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      CursedEnergySyncPayload::current,
      ByteBufCodecs.FLOAT,
      CursedEnergySyncPayload::max,
      ByteBufCodecs.FLOAT,
      CursedEnergySyncPayload::output,
      ByteBufCodecs.FLOAT,
      CursedEnergySyncPayload::control,
      ModCodecs.enumCodec(CursedEnergyNature.class),
      CursedEnergySyncPayload::nature,
      ByteBufCodecs.BYTE,
      CursedEnergySyncPayload::flags,
      CursedEnergySyncPayload::new
   );

   public static CursedEnergySyncPayload of(
      float current, float max, float output, float control, CursedEnergyNature nature, boolean meditating, boolean inCombat
   ) {
      int flags = (meditating ? 1 : 0) | (inCombat ? 2 : 0);
      return new CursedEnergySyncPayload(current, max, output, control, nature, (byte)flags);
   }

   public boolean meditating() {
      return (this.flags & 1) != 0;
   }

   public boolean inCombat() {
      return (this.flags & 2) != 0;
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static void handle(CursedEnergySyncPayload payload, IPayloadContext context) {
      context.enqueueWork(() -> ((CursedEnergyData)context.player().getData(ModAttachments.CURSED_ENERGY)).applySync(payload));
   }
}
