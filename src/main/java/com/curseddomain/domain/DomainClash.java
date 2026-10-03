package com.curseddomain.domain;

import com.curseddomain.energy.CursedEnergyData;
import com.curseddomain.energy.EnergyManager;
import net.minecraft.server.level.ServerPlayer;

public final class DomainClash {
   public final DomainInstance a;
   public final DomainInstance b;
   public final int duration;
   public int ticks;

   public DomainClash(DomainInstance a, DomainInstance b, int duration) {
      this.a = a;
      this.b = b;
      this.duration = duration;
   }

   public DomainInstance other(DomainInstance self) {
      return self == this.a ? this.b : this.a;
   }

   public static float strength(DomainInstance d) {
      float base;
      if (d.owner instanceof ServerPlayer player) {
         CursedEnergyData energy = EnergyManager.get(player);
         base = energy.output() * (0.5F + energy.control());
      } else {
         base = 120.0F;
      }

      return base * d.domain.refinement() * (d.type == DomainType.INCOMPLETE ? 0.4F : 1.0F) * (1.0F + d.clashPresses * 0.08F);
   }

   public float share(DomainInstance self) {
      float mine = strength(self);
      float theirs = strength(this.other(self));
      return mine + theirs <= 0.0F ? 0.5F : mine / (mine + theirs);
   }

   public DomainInstance loser() {
      return strength(this.a) >= strength(this.b) ? this.b : this.a;
   }
}
