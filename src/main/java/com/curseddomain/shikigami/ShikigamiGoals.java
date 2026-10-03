package com.curseddomain.shikigami;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public final class ShikigamiGoals {
   private ShikigamiGoals() {
   }

   public static final class AssistOwner extends TargetGoal {
      private final ShikigamiEntity shikigami;
      private LivingEntity victim;
      private int timestamp;

      public AssistOwner(ShikigamiEntity mob) {
         super(mob, false);
         this.shikigami = mob;
         this.setFlags(EnumSet.of(Flag.TARGET));
      }

      public boolean canUse() {
         LivingEntity owner = this.shikigami.isRitual() ? null : this.shikigami.summoner();
         if (owner == null) {
            return false;
         } else {
            this.victim = owner.getLastHurtMob();
            return this.victim != null
               && owner.getLastHurtMobTimestamp() != this.timestamp
               && !this.shikigami.isAlliedTo(this.victim)
               && this.canAttack(this.victim, TargetingConditions.DEFAULT);
         }
      }

      public void start() {
         this.mob.setTarget(this.victim);
         LivingEntity owner = this.shikigami.summoner();
         if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
         }

         super.start();
      }
   }

   public static final class DefendOwner extends TargetGoal {
      private final ShikigamiEntity shikigami;
      private LivingEntity attacker;
      private int timestamp;

      public DefendOwner(ShikigamiEntity mob) {
         super(mob, false);
         this.shikigami = mob;
         this.setFlags(EnumSet.of(Flag.TARGET));
      }

      public boolean canUse() {
         LivingEntity owner = this.shikigami.isRitual() ? null : this.shikigami.summoner();
         if (owner == null) {
            return false;
         } else {
            this.attacker = owner.getLastHurtByMob();
            return this.attacker != null
               && owner.getLastHurtByMobTimestamp() != this.timestamp
               && !this.shikigami.isAlliedTo(this.attacker)
               && this.canAttack(this.attacker, TargetingConditions.DEFAULT);
         }
      }

      public void start() {
         this.mob.setTarget(this.attacker);
         LivingEntity owner = this.shikigami.summoner();
         if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
         }

         super.start();
      }
   }

   public static final class FollowOwner extends Goal {
      private final ShikigamiEntity mob;
      private final double speed;
      private final float startDistance;
      private final float stopDistance;

      public FollowOwner(ShikigamiEntity mob, double speed, float startDistance, float stopDistance) {
         this.mob = mob;
         this.speed = speed;
         this.startDistance = startDistance;
         this.stopDistance = stopDistance;
         this.setFlags(EnumSet.of(Flag.MOVE));
      }

      public boolean canUse() {
         LivingEntity owner = this.mob.isRitual() ? null : this.mob.summoner();
         return owner != null && this.mob.getTarget() == null && this.mob.distanceToSqr(owner) > this.startDistance * this.startDistance;
      }

      public boolean canContinueToUse() {
         LivingEntity owner = this.mob.summoner();
         return owner != null
            && this.mob.getTarget() == null
            && this.mob.distanceToSqr(owner) > this.stopDistance * this.stopDistance
            && !this.mob.getNavigation().isDone();
      }

      public void tick() {
         LivingEntity owner = this.mob.summoner();
         if (owner != null) {
            this.mob.getLookControl().setLookAt(owner, 10.0F, this.mob.getMaxHeadXRot());
            if (this.mob.tickCount % 10 == 0) {
               this.mob.getNavigation().moveTo(owner, this.speed);
            }
         }
      }

      public void stop() {
         this.mob.getNavigation().stop();
      }
   }

   public static final class RitualTarget extends TargetGoal {
      private final ShikigamiEntity shikigami;

      public RitualTarget(ShikigamiEntity mob) {
         super(mob, false);
         this.shikigami = mob;
      }

      public boolean canUse() {
         LivingEntity summoner = this.shikigami.summoner();
         return this.shikigami.isRitual() && summoner != null && summoner.isAlive();
      }

      public void start() {
         this.mob.setTarget(this.shikigami.summoner());
         super.start();
      }
   }
}
