package com.curseddomain.vfx;

import com.curseddomain.registry.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EnergyParticleOptions(int color, float size, float gravity) implements ParticleOptions {
   public static final MapCodec<EnergyParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.INT.fieldOf("color").forGetter(EnergyParticleOptions::color),
            Codec.FLOAT.fieldOf("size").forGetter(EnergyParticleOptions::size),
            Codec.FLOAT.optionalFieldOf("gravity", 0.0F).forGetter(EnergyParticleOptions::gravity)
         )
         .apply(i, EnergyParticleOptions::new)
   );
   public static final StreamCodec<ByteBuf, EnergyParticleOptions> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      EnergyParticleOptions::color,
      ByteBufCodecs.FLOAT,
      EnergyParticleOptions::size,
      ByteBufCodecs.FLOAT,
      EnergyParticleOptions::gravity,
      EnergyParticleOptions::new
   );

   public ParticleType<?> getType() {
      return (ParticleType<?>)ModParticles.ENERGY.get();
   }
}
