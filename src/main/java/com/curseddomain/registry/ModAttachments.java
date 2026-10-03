package com.curseddomain.registry;

import com.curseddomain.combat.CombatRecord;
import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.shikigami.ShadowStorage;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.StoryData;
import com.curseddomain.technique.ability.AbilityState;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "cursed_domain");
   public static final Supplier<AttachmentType<SorcererData>> SORCERER = ATTACHMENT_TYPES.register(
      "sorcerer", () -> AttachmentType.serializable(() -> new SorcererData()).copyOnDeath().build()
   );
   public static final Supplier<AttachmentType<CursedEnergyData>> CURSED_ENERGY = ATTACHMENT_TYPES.register(
      "cursed_energy", () -> AttachmentType.serializable(() -> new CursedEnergyData()).copyOnDeath().build()
   );
   public static final Supplier<AttachmentType<StoryData>> STORY = ATTACHMENT_TYPES.register(
      "story", () -> AttachmentType.serializable(() -> new StoryData()).copyOnDeath().build()
   );
   public static final Supplier<AttachmentType<AbilityState>> ABILITY_STATE = ATTACHMENT_TYPES.register(
      "ability_state", () -> AttachmentType.serializable(() -> new AbilityState()).copyOnDeath().build()
   );
   public static final Supplier<AttachmentType<ShadowStorage>> SHADOW_STORAGE = ATTACHMENT_TYPES.register(
      "shadow_storage", () -> AttachmentType.serializable(() -> new ShadowStorage()).copyOnDeath().build()
   );
   public static final Supplier<AttachmentType<CombatRecord>> COMBAT_RECORD = ATTACHMENT_TYPES.register(
      "combat_record", () -> AttachmentType.serializable(() -> new CombatRecord()).copyOnDeath().build()
   );

   private ModAttachments() {
   }
}
