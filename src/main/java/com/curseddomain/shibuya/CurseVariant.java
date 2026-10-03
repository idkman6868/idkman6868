package com.curseddomain.shibuya;

import com.curseddomain.sorcerer.Grade;
import java.util.Locale;
import net.minecraft.network.chat.Component;

/** The kinds of curses Kenjaku lets loose, from ordinary rabble up to the disaster curses. */
public enum CurseVariant {
   GRADE_3(Grade.GRADE_3, 30.0, 4.0, 0.28, 1.15F, false),
   GRADE_2(Grade.GRADE_2, 55.0, 6.0, 0.29, 1.35F, false),
   GRADE_1(Grade.GRADE_1, 100.0, 9.0, 0.3, 1.6F, false),
   JOGO(Grade.SPECIAL_GRADE, 220.0, 10.0, 0.3, 1.5F, true),
   HANAMI(Grade.SPECIAL_GRADE, 260.0, 9.0, 0.27, 1.9F, true),
   DAGON(Grade.SPECIAL_GRADE, 220.0, 9.0, 0.27, 1.8F, true),
   MAHITO(Grade.SPECIAL_GRADE, 240.0, 10.0, 0.32, 1.4F, true),
   KUROURUSHI(Grade.SPECIAL_GRADE, 180.0, 8.0, 0.33, 1.3F, true),
   NAOYA(Grade.SPECIAL_GRADE, 200.0, 10.0, 0.38, 1.4F, true);

   private final Grade grade;
   private final double health;
   private final double damage;
   private final double speed;
   private final float scale;
   private final boolean boss;

   CurseVariant(Grade grade, double health, double damage, double speed, float scale, boolean boss) {
      this.grade = grade;
      this.health = health;
      this.damage = damage;
      this.speed = speed;
      this.scale = scale;
      this.boss = boss;
   }

   public Grade grade() {
      return this.grade;
   }

   public double health() {
      return this.health;
   }

   public double damage() {
      return this.damage;
   }

   public double speed() {
      return this.speed;
   }

   public float scale() {
      return this.scale;
   }

   /** Named special grades get a boss bar and a name tag. */
   public boolean boss() {
      return this.boss;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public Component displayName() {
      return Component.translatable("entity.cursed_domain.rampant_curse." + this.id());
   }

   public String texture() {
      return "textures/entity/curse/" + this.id() + ".png";
   }

   public static CurseVariant byIndex(int index) {
      CurseVariant[] values = values();
      return index >= 0 && index < values.length ? values[index] : GRADE_3;
   }

   public static CurseVariant byId(String id) {
      for (CurseVariant v : values()) {
         if (v.id().equals(id)) {
            return v;
         }
      }

      return null;
   }
}
