package com.steelballrun.race;

import java.util.UUID;

/** One racer in the standings: a player or a rival rider. */
public final class Entrant {
   public final UUID id;
   public String name;
   public final int number;
   /** -1 for players, otherwise the index into {@link com.steelballrun.npc.Rivals#ALL}. */
   public final int rival;
   public Background background;
   /** The next gate this racer must ride through; {@link Stage#GATES} once they have finished. */
   public int nextGate;
   public int points;
   /** Points taken away for breaking the rules (already subtracted from {@link #points}). */
   public int penalties;
   /** Distance along the course. Simulated for rivals who aren't loaded, last known position for players. */
   public double progress;
   /** Rival pace for the current stage, rerolled at every gate. */
   public double pace = 1.0;
   /** Place reached at each gate, 0 if not reached. */
   public final int[] places = new int[Stage.GATES];
   public long finishTick = -1L;
   public boolean retired;

   public Entrant(UUID id, String name, int number, int rival, Background background) {
      this.id = id;
      this.name = name;
      this.number = number;
      this.rival = rival;
      this.background = background;
   }

   public boolean isRival() {
      return this.rival >= 0;
   }

   public boolean finished() {
      return this.nextGate >= Stage.GATES;
   }

   public boolean racing() {
      return !this.finished() && !this.retired;
   }
}
