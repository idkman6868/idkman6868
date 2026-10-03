package com.curseddomain.cullinggame;

import com.curseddomain.ModMain;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Rules of the Culling Game. The first eight are always in force; the rest are added during the game. */
public enum CullingRule {
   DECLARE(true, false),
   TECHNIQUE_REMOVAL(true, false),
   NON_PLAYERS_ENTERING(true, false),
   SCORING(true, false),
   POINT_VALUES(true, false),
   ADDING_RULES(true, false),
   GAME_MASTER(true, false),
   STAGNATION(true, false),
   PLAYER_INFO(false, true),
   POINT_TRANSFER(false, true),
   LEAVE_BY_SUBSTITUTE(false, true),
   FREE_BORDERS(false, true),
   NO_NEW_PLAYERS(false, false),
   END_CONDITION(false, false),
   MERGER_AUTHORITY(false, false);

   private final boolean original;
   private final boolean purchasable;

   CullingRule(boolean original, boolean purchasable) {
      this.original = original;
      this.purchasable = purchasable;
   }

   public boolean original() {
      return this.original;
   }

   /** Whether a player may add this rule by spending points (rule 6). */
   public boolean purchasable() {
      return this.purchasable;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public Component text() {
      return Component.translatable("cullinggame.cursed_domain.rule." + this.id());
   }

   public static List<CullingRule> originals() {
      return Arrays.stream(values()).filter(CullingRule::original).toList();
   }

   public static CullingRule byId(String id) {
      for (CullingRule rule : values()) {
         if (rule.id().equals(id)) {
            return rule;
         }
      }

      return null;
   }

   public static String key(String suffix) {
      return ModMain.key("cullinggame", suffix);
   }
}
