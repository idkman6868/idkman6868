package com.curseddomain.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class TalismanParticle extends TextureSheetParticle {
   private final float spin;

   protected TalismanParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
      super(level, x, y, z, dx, dy, dz);
      this.xd = dx;
      this.yd = dy;
      this.zd = dz;
      this.gravity = 0.0F;
      this.friction = 0.97F;
      this.lifetime = 40 + this.random.nextInt(20);
      this.quadSize = 0.12F + this.random.nextFloat() * 0.05F;
      this.spin = (this.random.nextFloat() - 0.5F) * 0.25F;
      this.roll = this.random.nextFloat() * (float) (Math.PI * 2);
      this.oRoll = this.roll;
      this.pickSprite(sprites);
   }

   public void tick() {
      super.tick();
      this.oRoll = this.roll;
      this.roll = this.roll + this.spin;
      this.yd = this.yd + Mth.sin(this.age * 0.35F) * 0.004;
      this.alpha = this.age > this.lifetime - 12 ? (this.lifetime - this.age) / 12.0F : 1.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new TalismanParticle(level, x, y, z, dx, dy, dz, this.sprites);
      }
   }
}
