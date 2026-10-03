package com.curseddomain.combat;

import com.curseddomain.entity.cursedspirit.CursedSpirit;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.SorcererManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class Grades {
   private Grades() {
   }

   public static Grade of(LivingEntity entity) {
      if (entity instanceof Player player) {
         Grade g = SorcererManager.get(player).grade();
         return g == Grade.UNGRADED ? Grade.GRADE_4 : g;
      } else if (entity instanceof CursedSpirit curse) {
         return curse.curseGrade();
      } else {
         float hp = entity.getMaxHealth();
         if (hp < 24.0F) {
            return Grade.GRADE_4;
         } else if (hp < 45.0F) {
            return Grade.GRADE_3;
         } else if (hp < 90.0F) {
            return Grade.GRADE_2;
         } else {
            return hp < 180.0F ? Grade.GRADE_1 : Grade.SPECIAL_GRADE;
         }
      }
   }

   public static int gap(LivingEntity caster, LivingEntity target) {
      return of(target).ordinal() - of(caster).ordinal();
   }
}
