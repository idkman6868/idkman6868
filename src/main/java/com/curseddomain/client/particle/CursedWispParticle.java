package com.curseddomain.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class CursedWispParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected CursedWispParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
      super(level, x, y, z, dx, dy, dz);
      this.sprites = sprites;
      this.xd = dx + (this.random.nextDouble() - 0.5) * 0.01;
      this.yd = dy;
      this.zd = dz + (this.random.nextDouble() - 0.5) * 0.01;
      this.gravity = -0.01F;
      this.friction = 0.92F;
      this.lifetime = 18 + this.random.nextInt(14);
      this.quadSize = 0.1F + this.random.nextFloat() * 0.12F;
      this.setSpriteFromAge(sprites);
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
      this.alpha = 0.9F * (1.0F - (float)this.age / this.lifetime);
      this.quadSize *= 0.985F;
   }

   protected int getLightColor(float partialTick) {
      return 15728880;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new CursedWispParticle(level, x, y, z, dx, dy, dz, this.sprites);
      }
   }
}
