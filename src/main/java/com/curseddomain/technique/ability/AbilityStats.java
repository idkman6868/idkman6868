package com.curseddomain.technique.ability;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public record AbilityStats(float cost, float upkeep, int cooldown, float damage, float range, float radius, int duration, int chargeTicks) {
   public static AbilityStats.Builder builder() {
      return new AbilityStats.Builder();
   }

   public AbilityStats override(JsonObject json) {
      return new AbilityStats(
         GsonHelper.getAsFloat(json, "cost", this.cost),
         GsonHelper.getAsFloat(json, "upkeep", this.upkeep),
         GsonHelper.getAsInt(json, "cooldown", this.cooldown),
         GsonHelper.getAsFloat(json, "damage", this.damage),
         GsonHelper.getAsFloat(json, "range", this.range),
         GsonHelper.getAsFloat(json, "radius", this.radius),
         GsonHelper.getAsInt(json, "duration", this.duration),
         GsonHelper.getAsInt(json, "charge_ticks", this.chargeTicks)
      );
   }

   public AbilityStats scaled(float costMultiplier, float cooldownMultiplier) {
      return new AbilityStats(
         this.cost * costMultiplier,
         this.upkeep * costMultiplier,
         Math.round(this.cooldown * cooldownMultiplier),
         this.damage,
         this.range,
         this.radius,
         this.duration,
         this.chargeTicks
      );
   }

   public static final class Builder {
      private float cost;
      private float upkeep;
      private int cooldown;
      private float damage;
      private float range;
      private float radius;
      private int duration;
      private int chargeTicks = 20;

      public AbilityStats.Builder cost(float v) {
         this.cost = v;
         return this;
      }

      public AbilityStats.Builder upkeep(float v) {
         this.upkeep = v;
         return this;
      }

      public AbilityStats.Builder cooldown(int ticks) {
         this.cooldown = ticks;
         return this;
      }

      public AbilityStats.Builder cooldownSeconds(float s) {
         this.cooldown = Math.round(s * 20.0F);
         return this;
      }

      public AbilityStats.Builder damage(float v) {
         this.damage = v;
         return this;
      }

      public AbilityStats.Builder range(float v) {
         this.range = v;
         return this;
      }

      public AbilityStats.Builder radius(float v) {
         this.radius = v;
         return this;
      }

      public AbilityStats.Builder duration(int ticks) {
         this.duration = ticks;
         return this;
      }

      public AbilityStats.Builder durationSeconds(float s) {
         this.duration = Math.round(s * 20.0F);
         return this;
      }

      public AbilityStats.Builder charge(int ticks) {
         this.chargeTicks = ticks;
         return this;
      }

      public AbilityStats build() {
         return new AbilityStats(this.cost, this.upkeep, this.cooldown, this.damage, this.range, this.radius, this.duration, this.chargeTicks);
      }
   }
}
