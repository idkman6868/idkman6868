package com.curseddomain.registry;

import com.curseddomain.vfx.EnergyParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
   public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, "cursed_domain");
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TALISMAN = PARTICLE_TYPES.register("talisman", () -> new SimpleParticleType(false));
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CURSED_WISP = PARTICLE_TYPES.register(
      "cursed_wisp", () -> new SimpleParticleType(false)
   );
   public static final DeferredHolder<ParticleType<?>, ParticleType<EnergyParticleOptions>> ENERGY = PARTICLE_TYPES.register(
      "energy", () -> new ParticleType<EnergyParticleOptions>(false) {
         public MapCodec<EnergyParticleOptions> codec() {
            return EnergyParticleOptions.CODEC;
         }

         public StreamCodec<? super RegistryFriendlyByteBuf, EnergyParticleOptions> streamCodec() {
            return EnergyParticleOptions.STREAM_CODEC;
         }
      }
   );

   private ModParticles() {
   }
}
