package com.curseddomain.technique;

import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.energy.CursedEnergyNature;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.technique.ability.Ability;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class Technique {
   private final Technique.Properties properties;
   @Nullable
   private String descriptionId;
   @Nullable
   private List<Ability> abilities;
   @Nullable
   private DomainExpansion domain;
   private boolean domainBuilt;

   protected Technique(Technique.Properties properties) {
      this.properties = properties;
   }

   public abstract boolean isImplemented();

   public final List<Ability> abilities() {
      if (this.abilities == null) {
         List<Ability> built = List.copyOf(this.createAbilities());
         built.forEach(a -> a.bind(this));
         this.abilities = built;
      }

      return this.abilities;
   }

   protected List<Ability> createAbilities() {
      return List.of();
   }

   public void passiveTick(ServerPlayer player) {
   }

   public void onRemoved(ServerPlayer player) {
   }

   public final Optional<DomainExpansion> domain() {
      if (!this.domainBuilt) {
         this.domain = this.createDomain();
         if (this.domain != null) {
            this.domain.bind(this);
         }

         this.domainBuilt = true;
      }

      return Optional.ofNullable(this.domain);
   }

   @Nullable
   protected DomainExpansion createDomain() {
      return null;
   }

   public ResourceLocation id() {
      return Objects.requireNonNull(TechniqueRegistry.REGISTRY.getKey(this), "unregistered technique");
   }

   public TechniqueCategory category() {
      return this.properties.category;
   }

   public TechniqueRarity rarity() {
      return this.properties.rarity;
   }

   public TechniqueTier tier() {
      return this.properties.tier;
   }

   public boolean npcOnly() {
      return this.properties.npcOnly;
   }

   public boolean inRandomRoll() {
      return this.properties.inRandomRoll;
   }

   public Optional<InnateTrait> requiredTrait() {
      return Optional.ofNullable(this.properties.requiredTrait);
   }

   public Optional<InnateTrait> grantedTrait() {
      return Optional.ofNullable(this.properties.grantedTrait);
   }

   public CursedEnergyNature nature() {
      return this.properties.nature;
   }

   public boolean modOriginal() {
      return this.properties.modOriginal;
   }

   public String descriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("technique", this.id());
      }

      return this.descriptionId;
   }

   public Component displayName() {
      return Component.translatable(this.descriptionId()).withStyle(this.rarity().color());
   }

   public Component userName() {
      return Component.translatable(this.descriptionId() + ".user");
   }

   public static final class Properties {
      private final TechniqueCategory category;
      private final TechniqueRarity rarity;
      private final TechniqueTier tier;
      private boolean npcOnly;
      private boolean inRandomRoll = true;
      @Nullable
      private InnateTrait requiredTrait;
      @Nullable
      private InnateTrait grantedTrait;
      private CursedEnergyNature nature = CursedEnergyNature.STANDARD;
      private boolean modOriginal;

      private Properties(TechniqueCategory category, TechniqueRarity rarity, TechniqueTier tier) {
         this.category = category;
         this.rarity = rarity;
         this.tier = tier;
      }

      public static Technique.Properties of(TechniqueCategory category, TechniqueRarity rarity, TechniqueTier tier) {
         return new Technique.Properties(category, rarity, tier);
      }

      public Technique.Properties npcOnly() {
         this.npcOnly = true;
         return this;
      }

      public Technique.Properties notInRandomRoll() {
         this.inRandomRoll = false;
         return this;
      }

      public Technique.Properties requiresTrait(InnateTrait trait) {
         this.requiredTrait = trait;
         return this;
      }

      public Technique.Properties grantsTrait(InnateTrait trait) {
         this.grantedTrait = trait;
         return this;
      }

      public Technique.Properties nature(CursedEnergyNature nature) {
         this.nature = nature;
         return this;
      }

      public Technique.Properties modOriginal() {
         this.modOriginal = true;
         return this;
      }
   }
}
