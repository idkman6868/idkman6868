package com.curseddomain.domain;

import com.curseddomain.ModMain;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.ability.AbilityStatsLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public abstract class DomainExpansion {
   private final String name;
   private final DomainStyle style;
   private final DomainType nativeType;
   private final AbilityStats defaults;
   private Technique technique;
   private ResourceLocation statsId;

   protected DomainExpansion(String name, DomainStyle style, DomainType nativeType, AbilityStats defaults) {
      this.name = name;
      this.style = style;
      this.nativeType = nativeType;
      this.defaults = defaults;
   }

   public void bind(Technique owner) {
      this.technique = owner;
      this.statsId = ResourceLocation.fromNamespaceAndPath(owner.id().getNamespace(), owner.id().getPath() + "/domain");
   }

   public void onOpen(DomainInstance domain) {
   }

   public void onTick(DomainInstance domain) {
   }

   public abstract void sureHit(DomainInstance var1, LivingEntity var2);

   public void onClose(DomainInstance domain) {
   }

   public float refinement() {
      return 1.0F;
   }

   public String name() {
      return this.name;
   }

   public DomainStyle style() {
      return this.style;
   }

   public DomainType nativeType() {
      return this.nativeType;
   }

   public Technique technique() {
      return this.technique;
   }

   public AbilityStats stats() {
      return AbilityStatsLoader.statsFor(this.statsId, this.defaults);
   }

   public String translationKey() {
      return ModMain.key("domain", this.name);
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }
}
