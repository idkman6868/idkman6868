package com.steelballrun.registry;

import com.steelballrun.SteelBallRun;
import com.steelballrun.horse.HorseData;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SbrAttachments {
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SteelBallRun.MODID);
   /** Racehorse stats, stamina and bond. Attached to any horse, donkey or mule a player rides. */
   public static final Supplier<AttachmentType<HorseData>> HORSE = ATTACHMENT_TYPES.register(
      "racehorse", () -> AttachmentType.serializable(() -> new HorseData()).build()
   );

   private SbrAttachments() {
   }
}
