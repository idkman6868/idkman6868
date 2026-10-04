package com.steelballrun.race;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Orders racers. Pure Java so it can be tested outside the game. */
public final class Standings {
   /**
    * Overall standings: most points first. Ties go to whoever is further along (finishers count as fully along,
    * earlier finishers first); retired racers go last.
    */
   public static final Comparator<Entrant> OVERALL = Comparator.<Entrant>comparingInt(e -> e.retired ? 1 : 0)
      .thenComparing(Comparator.<Entrant>comparingInt(e -> e.points).reversed())
      .thenComparing(Standings::byProgress);

   /** Race order on the road right now: finishers first (by finishing time), then by distance covered. */
   public static final Comparator<Entrant> ON_ROAD = Comparator.<Entrant>comparingInt(e -> e.retired ? 1 : 0).thenComparing(Standings::byProgress);

   private Standings() {
   }

   private static int byProgress(Entrant a, Entrant b) {
      if (a.finished() && b.finished()) {
         return Long.compare(a.finishTick, b.finishTick);
      } else if (a.finished() != b.finished()) {
         return a.finished() ? -1 : 1;
      } else if (a.nextGate != b.nextGate) {
         return Integer.compare(b.nextGate, a.nextGate);
      } else {
         return Double.compare(b.progress, a.progress);
      }
   }

   public static List<Entrant> sorted(Collection<Entrant> entrants, Comparator<Entrant> order) {
      List<Entrant> list = new ArrayList<>(entrants);
      list.sort(order);
      return list;
   }

   /** 1-based place of {@code who} in the given order, or 0 if they aren't entered. */
   public static int placeOf(Collection<Entrant> entrants, Entrant who, Comparator<Entrant> order) {
      int place = 1;
      for (Entrant e : entrants) {
         if (e != who && order.compare(e, who) < 0) {
            place++;
         }
      }
      return entrants.contains(who) ? place : 0;
   }
}
