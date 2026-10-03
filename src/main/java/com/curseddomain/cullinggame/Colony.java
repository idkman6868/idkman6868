package com.curseddomain.cullinggame;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** One barrier colony. Colonies are cylinders that run from the bottom of the world to the top. */
public record Colony(int index, String key, int x, int z, int radius, boolean named) {
   public boolean contains(double px, double pz) {
      double dx = px - this.x;
      double dz = pz - this.z;
      return dx * dx + dz * dz < (double)this.radius * this.radius;
   }

   public double distance(double px, double pz) {
      double dx = px - this.x;
      double dz = pz - this.z;
      return Math.sqrt(dx * dx + dz * dz);
   }

   public BlockPos center() {
      return new BlockPos(this.x, 0, this.z);
   }

   public Component displayName() {
      return Component.translatable("colony.cursed_domain." + this.key);
   }
}
