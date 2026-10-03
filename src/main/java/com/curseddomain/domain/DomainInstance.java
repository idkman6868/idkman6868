package com.curseddomain.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class DomainInstance {
   public static final int FORM_TICKS = 24;
   public static final int COLLAPSE_TICKS = 24;
   public final LivingEntity owner;
   public final DomainExpansion domain;
   public final DomainType type;
   public final ServerLevel level;
   public final Vec3 center;
   public final float radius;
   public final Set<UUID> trapped = new HashSet<>();
   public DomainBarrierEntity barrier;
   public DomainInstance.Phase phase = DomainInstance.Phase.CASTING;
   public int phaseTicks;
   public int activeTicks;
   public float barrierHealth;
   public float castDamageTaken;
   @Nullable
   public DomainClash clash;
   public int clashPresses;
   @Nullable
   public String endReason;

   public DomainInstance(LivingEntity owner, DomainExpansion domain, DomainType type, Vec3 center, float radius) {
      this.owner = owner;
      this.domain = domain;
      this.type = type;
      this.level = (ServerLevel)owner.level();
      this.center = center;
      this.radius = radius;
   }

   public boolean contains(Vec3 point) {
      return point.distanceToSqr(this.center) < this.radius * this.radius;
   }

   public boolean contains(Entity entity) {
      return this.contains(entity.position().add(0.0, entity.getBbHeight() / 2.0F, 0.0));
   }

   public boolean hasBarrier() {
      return this.type == DomainType.CLOSED;
   }

   public boolean live() {
      return this.phase == DomainInstance.Phase.ACTIVE;
   }

   void setPhase(DomainInstance.Phase next) {
      this.phase = next;
      this.phaseTicks = 0;
      if (this.barrier != null) {
         this.barrier.setPhase(next);
      }
   }

   public static enum Phase {
      CASTING,
      FORMING,
      ACTIVE,
      COLLAPSING;
   }
}
