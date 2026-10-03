package com.curseddomain.technique.ability;

import com.curseddomain.ModMain;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.technique.Technique;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class Ability {
   private final String name;
   private final AbilityKind kind;
   private final AbilityStats defaults;
   private Grade minGrade = Grade.UNGRADED;
   @Nullable
   private String requiredUnlock;
   private boolean maximum;
   private Technique technique;
   private ResourceLocation id;
   @Nullable
   private BiConsumer<AbilityContext, Integer> chargeVisual;

   protected Ability(String name, AbilityKind kind, AbilityStats defaults) {
      this.name = name;
      this.kind = kind;
      this.defaults = defaults;
   }

   public static Ability instant(String name, AbilityStats stats, final Predicate<AbilityContext> action) {
      return new Ability(name, AbilityKind.INSTANT, stats) {
         @Override
         public boolean activate(AbilityContext ctx) {
            return action.test(ctx);
         }
      };
   }

   public static Ability charge(String name, AbilityStats stats, final BiConsumer<AbilityContext, Float> action) {
      return new Ability(name, AbilityKind.CHARGE, stats) {
         @Override
         public void release(AbilityContext ctx, float charge) {
            action.accept(ctx, charge);
         }
      };
   }

   public Ability minGrade(Grade grade) {
      this.minGrade = grade;
      return this;
   }

   public Ability requires(String unlockFlag) {
      this.requiredUnlock = unlockFlag;
      return this;
   }

   public Ability maximum() {
      this.maximum = true;
      if (!this.minGrade.atLeast(Grade.GRADE_2)) {
         this.minGrade = Grade.GRADE_2;
      }

      return this;
   }

   public void bind(Technique owner) {
      this.technique = owner;
      this.id = ResourceLocation.fromNamespaceAndPath(owner.id().getNamespace(), owner.id().getPath() + "/" + this.name);
   }

   public boolean activate(AbilityContext ctx) {
      return true;
   }

   public void release(AbilityContext ctx, float charge) {
   }

   public void chargeTick(AbilityContext ctx, int ticks) {
      if (this.chargeVisual != null) {
         this.chargeVisual.accept(ctx, ticks);
      }
   }

   public Ability onCharge(BiConsumer<AbilityContext, Integer> visual) {
      this.chargeVisual = visual;
      return this;
   }

   public boolean activeTick(AbilityContext ctx, int ticks) {
      return true;
   }

   public void deactivate(AbilityContext ctx) {
   }

   public String name() {
      return this.name;
   }

   public AbilityKind kind() {
      return this.kind;
   }

   public AbilityStats defaults() {
      return this.defaults;
   }

   public Grade minGrade() {
      return this.minGrade;
   }

   @Nullable
   public String requiredUnlock() {
      return this.requiredUnlock;
   }

   public boolean isMaximum() {
      return this.maximum;
   }

   public Technique technique() {
      return this.technique;
   }

   public ResourceLocation id() {
      return this.id;
   }

   public AbilityStats stats() {
      return AbilityStatsLoader.statsFor(this);
   }

   public String translationKey() {
      return ModMain.key("ability", this.technique.id().getPath() + "." + this.name);
   }

   public Component displayName() {
      return Component.translatable(this.translationKey());
   }

   public Component description() {
      return Component.translatable(this.translationKey() + ".desc");
   }

   public ResourceLocation icon() {
      return ResourceLocation.fromNamespaceAndPath(
         this.technique.id().getNamespace(), "textures/gui/ability/" + this.technique.id().getPath() + "/" + this.name + ".png"
      );
   }
}
