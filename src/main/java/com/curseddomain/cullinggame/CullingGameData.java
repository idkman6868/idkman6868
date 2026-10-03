package com.curseddomain.cullinggame;

import com.curseddomain.util.EnumNames;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import org.jetbrains.annotations.Nullable;

/** Everything the game master remembers about the Culling Game, saved with the overworld. */
public final class CullingGameData extends SavedData {
   private static final String NAME = "cursed_domain_culling_game";
   static final Factory<CullingGameData> FACTORY = new Factory<CullingGameData>(
      CullingGameData::new, (tag, provider) -> load((CompoundTag)tag, (Provider)provider), null
   );
   public CullingGameData.Phase phase = CullingGameData.Phase.INACTIVE;
   public long startTick;
   @Nullable
   public BlockPos anchor;
   public double angle;
   public final List<CullingGameData.AddedRule> rules = new ArrayList<>();
   public final Map<UUID, CullingGameData.PlayerRecord> players = new LinkedHashMap<>();
   public final Map<String, CullingGameData.NpcState> npcs = new LinkedHashMap<>();
   /** 0 = not started, 1 = Kenjaku culls at Lake Gosho, 2 = Kenjaku's failsafe woke Sukuna, 3 = over. */
   public int endgameStage;
   public boolean gojoFreed;
   public String outcome = "";
   @Nullable
   private List<Colony> colonies;

   public static CullingGameData get(MinecraftServer server) {
      return (CullingGameData)server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
   }

   public static int ruleCost() {
      return CullingConfig.i(CullingConfig.RULE_COST);
   }

   public boolean active() {
      return this.phase == CullingGameData.Phase.ACTIVE;
   }

   public boolean has(CullingRule rule) {
      if (rule.original()) {
         return true;
      } else {
         for (CullingGameData.AddedRule added : this.rules) {
            if (added.rule() == rule) {
               return true;
            }
         }

         return false;
      }
   }

   /** The number this rule was given when it was added (rules 1-8 are the originals). */
   public int number(CullingRule rule) {
      if (rule.original()) {
         return rule.ordinal() + 1;
      } else {
         for (int i = 0; i < this.rules.size(); i++) {
            if (this.rules.get(i).rule() == rule) {
               return 9 + i;
            }
         }

         return -1;
      }
   }

   public List<Colony> colonies() {
      if (this.colonies == null) {
         this.colonies = this.anchor == null
            ? List.of()
            : Colonies.layout(this.anchor, this.angle, CullingConfig.i(CullingConfig.COLONY_SPACING), CullingConfig.i(CullingConfig.COLONY_RADIUS));
      }

      return this.colonies;
   }

   public void relayout() {
      this.colonies = null;
   }

   @Nullable
   public Colony colonyAt(double x, double z) {
      for (Colony colony : this.colonies()) {
         if (colony.contains(x, z)) {
            return colony;
         }
      }

      return null;
   }

   @Nullable
   public Colony colony(int index) {
      List<Colony> list = this.colonies();
      return index >= 0 && index < list.size() ? list.get(index) : null;
   }

   @Nullable
   public Colony colony(String key) {
      for (Colony colony : this.colonies()) {
         if (colony.key().equals(key)) {
            return colony;
         }
      }

      return null;
   }

   public CullingGameData.PlayerRecord record(UUID id, String name) {
      CullingGameData.PlayerRecord record = this.players.computeIfAbsent(id, CullingGameData.PlayerRecord::new);
      record.name = name;
      return record;
   }

   @Nullable
   public CullingGameData.PlayerRecord recordOf(UUID id) {
      return this.players.get(id);
   }

   public CullingGameData.NpcState npc(String profile) {
      return this.npcs.computeIfAbsent(profile, k -> new CullingGameData.NpcState());
   }

   public void reset() {
      this.phase = CullingGameData.Phase.INACTIVE;
      this.startTick = 0L;
      this.rules.clear();
      this.players.clear();
      this.npcs.clear();
      this.endgameStage = 0;
      this.gojoFreed = false;
      this.outcome = "";
      this.setDirty();
   }

   public List<CullingGameData.PlayerRecord> participants() {
      List<CullingGameData.PlayerRecord> list = new ArrayList<>();

      for (CullingGameData.PlayerRecord record : this.players.values()) {
         if (record.participant) {
            list.add(record);
         }
      }

      return Collections.unmodifiableList(list);
   }

   static CullingGameData load(CompoundTag tag, Provider provider) {
      CullingGameData data = new CullingGameData();
      data.phase = EnumNames.byName(CullingGameData.Phase.values(), tag.getString("phase"), CullingGameData.Phase.INACTIVE);
      data.startTick = tag.getLong("start");
      if (tag.contains("anchor_x")) {
         data.anchor = new BlockPos(tag.getInt("anchor_x"), 0, tag.getInt("anchor_z"));
      }

      data.angle = tag.getDouble("angle");
      data.endgameStage = tag.getInt("endgame");
      data.gojoFreed = tag.getBoolean("gojo_freed");
      data.outcome = tag.getString("outcome");
      ListTag rules = tag.getList("rules", 10);

      for (int i = 0; i < rules.size(); i++) {
         CompoundTag r = rules.getCompound(i);
         CullingRule rule = CullingRule.byId(r.getString("rule"));
         if (rule != null) {
            data.rules.add(new CullingGameData.AddedRule(rule, r.getString("author"), r.getLong("at")));
         }
      }

      ListTag players = tag.getList("players", 10);

      for (int i = 0; i < players.size(); i++) {
         CompoundTag p = players.getCompound(i);

         try {
            CullingGameData.PlayerRecord record = new CullingGameData.PlayerRecord(UUID.fromString(p.getString("id")));
            record.read(p);
            data.players.put(record.id, record);
         } catch (IllegalArgumentException ignored) {
         }
      }

      CompoundTag npcs = tag.getCompound("npcs");

      for (String key : npcs.getAllKeys()) {
         CullingGameData.NpcState state = new CullingGameData.NpcState();
         CompoundTag n = npcs.getCompound(key);
         state.spawned = n.getBoolean("spawned");
         state.defeated = n.getBoolean("defeated");
         state.points = n.getInt("points");
         data.npcs.put(key, state);
      }

      return data;
   }

   public CompoundTag save(CompoundTag tag, Provider provider) {
      tag.putString("phase", this.phase.getSerializedName());
      tag.putLong("start", this.startTick);
      if (this.anchor != null) {
         tag.putInt("anchor_x", this.anchor.getX());
         tag.putInt("anchor_z", this.anchor.getZ());
      }

      tag.putDouble("angle", this.angle);
      tag.putInt("endgame", this.endgameStage);
      tag.putBoolean("gojo_freed", this.gojoFreed);
      tag.putString("outcome", this.outcome);
      ListTag rules = new ListTag();

      for (CullingGameData.AddedRule added : this.rules) {
         CompoundTag r = new CompoundTag();
         r.putString("rule", added.rule().id());
         r.putString("author", added.author());
         r.putLong("at", added.tick());
         rules.add(r);
      }

      tag.put("rules", rules);
      ListTag players = new ListTag();

      for (CullingGameData.PlayerRecord record : this.players.values()) {
         CompoundTag p = new CompoundTag();
         p.putString("id", record.id.toString());
         record.write(p);
         players.add(p);
      }

      tag.put("players", players);
      CompoundTag npcs = new CompoundTag();

      for (Map.Entry<String, CullingGameData.NpcState> entry : this.npcs.entrySet()) {
         CompoundTag n = new CompoundTag();
         n.putBoolean("spawned", entry.getValue().spawned);
         n.putBoolean("defeated", entry.getValue().defeated);
         n.putInt("points", entry.getValue().points);
         npcs.put(entry.getKey(), n);
      }

      tag.put("npcs", npcs);
      return tag;
   }

   public static enum Phase implements net.minecraft.util.StringRepresentable {
      INACTIVE,
      ACTIVE,
      ENDED;

      public String getSerializedName() {
         return EnumNames.lower(this);
      }
   }

   public record AddedRule(CullingRule rule, String author, long tick) {
   }

   public static final class NpcState {
      public boolean spawned;
      public boolean defeated;
      public int points;
   }

   public static final class PlayerRecord {
      public final UUID id;
      public String name = "";
      public int points;
      /** Marked by Kenjaku: must declare participation within the rule 1 deadline. */
      public boolean marked;
      public long markedAt;
      /** Currently a player of the Culling Game. */
      public boolean participant;
      public long lastScoreChange;
      public int colony = -1;
      public int rulesAdded;
      /** Highest deadline warning already given (days left), so each warning is only sent once. */
      public int warned = Integer.MAX_VALUE;

      public PlayerRecord(UUID id) {
         this.id = id;
      }

      void read(CompoundTag tag) {
         this.name = tag.getString("name");
         this.points = tag.getInt("points");
         this.marked = tag.getBoolean("marked");
         this.markedAt = tag.getLong("marked_at");
         this.participant = tag.getBoolean("participant");
         this.lastScoreChange = tag.getLong("last_score");
         this.colony = tag.contains("colony") ? tag.getInt("colony") : -1;
         this.rulesAdded = tag.getInt("rules_added");
         this.warned = tag.contains("warned") ? tag.getInt("warned") : Integer.MAX_VALUE;
      }

      void write(CompoundTag tag) {
         tag.putString("name", this.name);
         tag.putInt("points", this.points);
         tag.putBoolean("marked", this.marked);
         tag.putLong("marked_at", this.markedAt);
         tag.putBoolean("participant", this.participant);
         tag.putLong("last_score", this.lastScoreChange);
         tag.putInt("colony", this.colony);
         tag.putInt("rules_added", this.rulesAdded);
         tag.putInt("warned", this.warned);
      }
   }
}
