package com.curseddomain.domain;

import java.util.Locale;

public enum DomainStyle {
   UNLIMITED_VOID(-16118742, -4663041, -9794305),
   MALEVOLENT_SHRINE(-14023162, -50646, -7730678),
   COFFIN_OF_THE_IRON_MOUNTAIN(-14021630, -30182, -5228022),
   CHIMERA_SHADOW_GARDEN(-16514038, -11904352, -15066566),
   SELF_EMBODIMENT_OF_PERFECTION(-15069152, -3628824, -10864016),
   HORIZON_OF_THE_CAPTIVATING_SKANDHA(-16111046, -7673601, -13989200),
   IDLE_DEATH_GAMBLE(-15070678, -10166, -1553744),
   DEADLY_SENTENCING(-15068152, -1521542, -10859990),
   AUTHENTIC_MUTUAL_LOVE(-16116728, -1511169, -9794982),
   TIME_CELL_MOON_PALACE(-16250348, -2037505, -12957078),
   WOMB_PROFUSION(-15070710, -1531744, -9819606),
   GENERIC(-15724528, -4144960, -12566464);

   private final int fog;
   private final int glow;
   private final int shell;

   private DomainStyle(int fog, int glow, int shell) {
      this.fog = fog;
      this.glow = glow;
      this.shell = shell;
   }

   public int fog() {
      return this.fog;
   }

   public int glow() {
      return this.glow;
   }

   public int shell() {
      return this.shell;
   }

   public String textureName() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
