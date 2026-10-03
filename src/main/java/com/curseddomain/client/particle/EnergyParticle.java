package com.curseddomain.client.particle;

import com.curseddomain.vfx.EnergyParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class EnergyParticle extends TextureSheetParticle {
   private final float startSize;

   protected EnergyParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, EnergyParticleOptions options, SpriteSet sprites) {
      super(level, x, y, z, dx, dy, dz);
      this.xd = dx;
      this.yd = dy;
      this.zd = dz;
      this.gravity = options.gravity();
      this.friction = 0.9F;
      this.hasPhysics = options.gravity() > 0.0F;
      this.lifetime = 10 + this.random.nextInt(12);
      this.startSize = 0.08F * options.size() * (0.8F + this.random.nextFloat() * 0.4F);
      this.quadSize = this.startSize;
      int c = options.color();
      this.rCol = (c >> 16 & 0xFF) / 255.0F;
      this.gCol = (c >> 8 & 0xFF) / 255.0F;
      this.bCol = (c & 0xFF) / 255.0F;
      this.pickSprite(sprites);
   }

   public void tick() {
      super.tick();
      float life = (float)this.age / this.lifetime;
      this.quadSize = this.startSize * (1.0F - life * 0.7F);
      this.alpha = 1.0F - life * life;
   }

   protected int getLightColor(float partialTick) {
      return 15728880;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public record Provider(SpriteSet sprites) implements ParticleProvider<EnergyParticleOptions> {
      public Particle createParticle(EnergyParticleOptions type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new EnergyParticle(level, x, y, z, dx, dy, dz, type, this.sprites);
      }
   }
}
