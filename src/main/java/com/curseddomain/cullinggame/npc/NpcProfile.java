package com.curseddomain.cullinggame.npc;

import com.curseddomain.cullinggame.Colonies;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Who an NPC sorcerer is: their colony, points, stats and technique. */
public enum NpcProfile {
   HIROMI_HIGURUMA(Colonies.TOKYO_1, 100, 90.0, 6.0, 0.3, 4.0, true, true, NpcAbility.DEADLY_SENTENCING, NpcProfile.Defeat.HIGURUMA, true, NpcProfile.Held.NONE),
   REGGIE_STAR(Colonies.TOKYO_1, 46, 60.0, 5.0, 0.3, 2.0, true, true, NpcAbility.CONTRACT_RECREATION, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.NONE),
   IORI_HAZENOKI(Colonies.TOKYO_1, 30, 50.0, 5.0, 0.3, 2.0, true, true, NpcAbility.EXPLOSIONS, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.NONE),
   CHIZURU_HARI(Colonies.TOKYO_1, 10, 34.0, 4.0, 0.32, 0.0, true, true, NpcAbility.NEEDLES, NpcProfile.Defeat.DIES, false, NpcProfile.Held.NONE),
   REMI(Colonies.TOKYO_1, 5, 26.0, 3.0, 0.33, 0.0, true, true, NpcAbility.NEEDLES, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.NONE),
   HANYU(Colonies.TOKYO_1, 5, 26.0, 3.0, 0.3, 0.0, true, true, NpcAbility.RANDOM, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.NONE),
   HABA(Colonies.TOKYO_1, 5, 30.0, 4.0, 0.3, 2.0, true, true, NpcAbility.NONE, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.STICK),
   FUMIHIKO_TAKABA(Colonies.TOKYO_1, 5, 60.0, 0.0, 0.3, 0.0, false, true, NpcAbility.COMEDIAN, NpcProfile.Defeat.INVULNERABLE, false, NpcProfile.Held.NONE),
   HANA_KURUSU(Colonies.TOKYO_1, 0, 70.0, 4.0, 0.3, 2.0, false, true, NpcAbility.JACOBS_LADDER, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.NONE),
   HAJIME_KASHIMO(Colonies.TOKYO_2, 100, 140.0, 8.0, 0.34, 6.0, true, true, NpcAbility.LIGHTNING, NpcProfile.Defeat.YIELDS, true, NpcProfile.Held.STICK),
   CHARLES_BERNARD(Colonies.TOKYO_2, 30, 60.0, 6.0, 0.32, 4.0, true, true, NpcAbility.FORESIGHT, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.GOLDEN_SWORD),
   RYU_ISHIGORI(Colonies.SENDAI, 100, 130.0, 8.0, 0.3, 6.0, true, true, NpcAbility.GRANITE_BLAST, NpcProfile.Defeat.YIELDS, true, NpcProfile.Held.NONE),
   TAKAKO_URO(Colonies.SENDAI, 70, 100.0, 6.0, 0.32, 4.0, true, true, NpcAbility.SKY_MANIPULATION, NpcProfile.Defeat.YIELDS, true, NpcProfile.Held.NONE),
   DHRUV_LAKDAWALLA(Colonies.SENDAI, 40, 90.0, 6.0, 0.3, 4.0, true, true, NpcAbility.SHIKIGAMI_SWARM, NpcProfile.Defeat.DIES, false, NpcProfile.Held.NONE),
   HAGANE_DAIDO(Colonies.SAKURAJIMA, 30, 80.0, 9.0, 0.33, 4.0, true, false, NpcAbility.SWORDSMAN, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.IRON_SWORD),
   ROKUJUSHI_MIYO(Colonies.SAKURAJIMA, 15, 80.0, 0.0, 0.3, 6.0, false, true, NpcAbility.SUMO, NpcProfile.Defeat.INVULNERABLE, false, NpcProfile.Held.NONE),
   KENJAKU(Colonies.LAKE_GOSHO, 0, 320.0, 9.0, 0.3, 10.0, true, true, NpcAbility.CURSED_SPIRIT_MANIPULATION, NpcProfile.Defeat.BOSS, true, NpcProfile.Held.NONE),
   SUKUNA(Colonies.LAKE_GOSHO, 0, 420.0, 12.0, 0.34, 12.0, true, true, NpcAbility.SHRINE, NpcProfile.Defeat.BOSS, true, NpcProfile.Held.NONE),
   AWAKENED_PLAYER(null, 0, 30.0, 4.0, 0.3, 0.0, true, true, NpcAbility.RANDOM, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.NONE),
   INCARNATED_SORCERER(null, 0, 46.0, 6.0, 0.31, 2.0, true, true, NpcAbility.RANDOM, NpcProfile.Defeat.YIELDS, false, NpcProfile.Held.IRON_SWORD);

   @Nullable
   private final String colony;
   private final int points;
   private final double health;
   private final double damage;
   private final double speed;
   private final double armor;
   private final boolean hostile;
   private final boolean sorcerer;
   private final NpcAbility ability;
   private final NpcProfile.Defeat defeat;
   private final boolean boss;
   private final NpcProfile.Held held;

   NpcProfile(
      @Nullable String colony,
      int points,
      double health,
      double damage,
      double speed,
      double armor,
      boolean hostile,
      boolean sorcerer,
      NpcAbility ability,
      NpcProfile.Defeat defeat,
      boolean boss,
      NpcProfile.Held held
   ) {
      this.colony = colony;
      this.points = points;
      this.health = health;
      this.damage = damage;
      this.speed = speed;
      this.armor = armor;
      this.hostile = hostile;
      this.sorcerer = sorcerer;
      this.ability = ability;
      this.defeat = defeat;
      this.boss = boss;
      this.held = held;
   }

   public String id() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   @Nullable
   public String colony() {
      return this.colony;
   }

   public int points() {
      return this.points;
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

   public double armor() {
      return this.armor;
   }

   public boolean hostile() {
      return this.hostile;
   }

   /** Rule 5: sorcerers are worth five points, non-sorcerers one. */
   public boolean sorcerer() {
      return this.sorcerer;
   }

   public NpcAbility ability() {
      return this.ability;
   }

   public NpcProfile.Defeat defeat() {
      return this.defeat;
   }

   public boolean boss() {
      return this.boss;
   }

   public NpcProfile.Held held() {
      return this.held;
   }

   /** Named players appear once per game; generic ones are spawned as needed. */
   public boolean unique() {
      return this.colony != null;
   }

   /** Spawned by the endgame script rather than by players walking into the colony. */
   public boolean scripted() {
      return this == KENJAKU || this == SUKUNA;
   }

   public Component displayName() {
      return Component.translatable("entity.cursed_domain.sorcerer_npc." + this.id());
   }

   public String texture() {
      return "textures/entity/npc/" + this.id() + ".png";
   }

   public static NpcProfile byIndex(int index) {
      NpcProfile[] values = values();
      return index >= 0 && index < values.length ? values[index] : AWAKENED_PLAYER;
   }

   @Nullable
   public static NpcProfile byId(String id) {
      for (NpcProfile profile : values()) {
         if (profile.id().equals(id)) {
            return profile;
         }
      }

      return null;
   }

   public static enum Defeat {
      /** Fights to the death. */
      DIES,
      /** Once rule 10 exists, surrenders their points to whoever beat them instead of dying. */
      YIELDS,
      /** Throws the fight and adds rule 10 himself, as in Tokyo No. 1. */
      HIGURUMA,
      /** Cannot be hurt by players. */
      INVULNERABLE,
      /** Scripted boss. */
      BOSS;
   }

   public static enum Held {
      NONE,
      IRON_SWORD,
      GOLDEN_SWORD,
      NETHERITE_SWORD,
      STICK;
   }
}
