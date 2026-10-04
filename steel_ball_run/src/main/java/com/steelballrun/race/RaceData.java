package com.steelballrun.race;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/** Everything the race committee remembers, saved with the overworld. */
public final class RaceData extends SavedData {
   private static final String NAME = "steel_ball_run_race";
   private static final SavedData.Factory<RaceData> FACTORY = new SavedData.Factory<>(RaceData::new, RaceData::load, null);

   public enum Phase {
      REGISTRATION,
      COUNTDOWN,
      RUNNING,
      FINISHED;

      public static final Phase[] ALL = values();
   }

   public Phase phase = Phase.REGISTRATION;
   public boolean laidOut;
   public double originX;
   public double originZ;
   /** Route scale captured when the course was laid out, so changing the config can't move the gates mid-race. */
   public double scale = 1.0;
   public int countdown;
   public long startTick;
   public long endTick;
   /** Game time at which every registered player had finished or retired; -1 while someone is still riding. */
   public long playersDoneTick = -1L;
   public int builtGates;
   @Nullable
   public UUID steel;
   public int nextNumber = 1;
   public boolean rivalsSeeded;
   /** Racers through each gate so far, to hand out places. */
   public final int[] arrivals = new int[Stage.GATES];
   public final Map<UUID, Entrant> entrants = new LinkedHashMap<>();
   @Nullable
   private Route route;

   public static RaceData get(MinecraftServer server) {
      return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
   }

   public Route route() {
      if (this.route == null) {
         this.route = Route.create(this.originX, this.originZ, this.scale);
      }
      return this.route;
   }

   public void layOut(double x, double z, double scale) {
      this.laidOut = true;
      this.originX = x;
      this.originZ = z;
      this.scale = scale;
      this.route = null;
      this.builtGates = 0;
      this.setDirty();
   }

   @Nullable
   public Entrant entrant(UUID id) {
      return this.entrants.get(id);
   }

   /** Back to registration with the same course; keeps nothing else. */
   public void reset() {
      this.phase = Phase.REGISTRATION;
      this.countdown = 0;
      this.startTick = 0L;
      this.endTick = 0L;
      this.playersDoneTick = -1L;
      this.nextNumber = 1;
      this.rivalsSeeded = false;
      java.util.Arrays.fill(this.arrivals, 0);
      this.entrants.clear();
      this.setDirty();
   }

   @Override
   public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
      tag.putInt("phase", this.phase.ordinal());
      tag.putBoolean("laidOut", this.laidOut);
      tag.putDouble("originX", this.originX);
      tag.putDouble("originZ", this.originZ);
      tag.putDouble("scale", this.scale);
      tag.putInt("countdown", this.countdown);
      tag.putLong("startTick", this.startTick);
      tag.putLong("endTick", this.endTick);
      tag.putLong("playersDoneTick", this.playersDoneTick);
      tag.putInt("builtGates", this.builtGates);
      if (this.steel != null) {
         tag.putString("steel", this.steel.toString());
      }
      tag.putInt("nextNumber", this.nextNumber);
      tag.putBoolean("rivalsSeeded", this.rivalsSeeded);
      tag.putIntArray("arrivals", this.arrivals);
      ListTag list = new ListTag();
      for (Entrant e : this.entrants.values()) {
         CompoundTag t = new CompoundTag();
         t.putString("id", e.id.toString());
         t.putString("name", e.name);
         t.putInt("number", e.number);
         t.putInt("rival", e.rival);
         t.putString("background", e.background == null ? "" : e.background.id());
         t.putInt("nextGate", e.nextGate);
         t.putInt("points", e.points);
         t.putInt("penalties", e.penalties);
         t.putDouble("progress", e.progress);
         t.putDouble("pace", e.pace);
         t.putIntArray("places", e.places);
         t.putLong("finishTick", e.finishTick);
         t.putBoolean("retired", e.retired);
         list.add(t);
      }
      tag.put("entrants", list);
      return tag;
   }

   private static RaceData load(CompoundTag tag, HolderLookup.Provider provider) {
      RaceData d = new RaceData();
      int phase = tag.getInt("phase");
      d.phase = phase >= 0 && phase < Phase.ALL.length ? Phase.ALL[phase] : Phase.REGISTRATION;
      d.laidOut = tag.getBoolean("laidOut");
      d.originX = tag.getDouble("originX");
      d.originZ = tag.getDouble("originZ");
      d.scale = tag.contains("scale") ? tag.getDouble("scale") : 1.0;
      d.countdown = tag.getInt("countdown");
      d.startTick = tag.getLong("startTick");
      d.endTick = tag.getLong("endTick");
      d.playersDoneTick = tag.contains("playersDoneTick") ? tag.getLong("playersDoneTick") : -1L;
      d.builtGates = tag.getInt("builtGates");
      d.steel = parse(tag.getString("steel"));
      d.nextNumber = Math.max(1, tag.getInt("nextNumber"));
      d.rivalsSeeded = tag.getBoolean("rivalsSeeded");
      int[] arrivals = tag.getIntArray("arrivals");
      System.arraycopy(arrivals, 0, d.arrivals, 0, Math.min(arrivals.length, d.arrivals.length));
      ListTag list = tag.getList("entrants", 10);
      for (int i = 0; i < list.size(); i++) {
         CompoundTag t = list.getCompound(i);
         UUID id = parse(t.getString("id"));
         if (id != null) {
            Entrant e = new Entrant(id, t.getString("name"), t.getInt("number"), t.getInt("rival"), Background.byId(t.getString("background")));
            e.nextGate = t.getInt("nextGate");
            e.points = t.getInt("points");
            e.penalties = t.getInt("penalties");
            e.progress = t.getDouble("progress");
            e.pace = t.contains("pace") ? t.getDouble("pace") : 1.0;
            int[] places = t.getIntArray("places");
            System.arraycopy(places, 0, e.places, 0, Math.min(places.length, e.places.length));
            e.finishTick = t.contains("finishTick") ? t.getLong("finishTick") : -1L;
            e.retired = t.getBoolean("retired");
            d.entrants.put(id, e);
         }
      }
      return d;
   }

   @Nullable
   private static UUID parse(String s) {
      try {
         return s == null || s.isEmpty() ? null : UUID.fromString(s);
      } catch (IllegalArgumentException ex) {
         return null;
      }
   }
}
