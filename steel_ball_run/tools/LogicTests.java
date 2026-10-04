import com.steelballrun.horse.Affinity;
import com.steelballrun.horse.Stamina;
import com.steelballrun.npc.Rivals;
import com.steelballrun.race.Background;
import com.steelballrun.race.Entrant;
import com.steelballrun.race.Points;
import com.steelballrun.race.Route;
import com.steelballrun.race.Stage;
import com.steelballrun.race.Standings;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tests for the parts of the mod that are plain Java: the course, points, standings and stamina. Run by
 * tools/sandbox-build.sh; exits non-zero on the first failure.
 */
public class LogicTests {
   private static int passed;

   public static void main(String[] args) {
      route();
      points();
      standings();
      stamina();
      pacing();
      data();
      System.out.println("LogicTests: " + passed + " checks passed");
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         throw new AssertionError("FAILED: " + what);
      }
      passed++;
   }

   static boolean near(double a, double b, double eps) {
      return Math.abs(a - b) <= eps;
   }

   static void route() {
      Route r = Route.create(100.0, -50.0, 1.0);
      check(r.gates() == Stage.GATES && Stage.GATES == 10, "ten gates: start + nine stages");
      check(r.gateX(0) == 100.0 && r.gateZ(0) == -50.0, "gate 0 is the start line");
      check(near(r.distanceTo(0), 0.0, 1e-9), "start is at distance 0");
      for (int g = 1; g < r.gates(); g++) {
         check(r.gateX(g) > r.gateX(g - 1), "course always heads east (gate " + g + ")");
         check(r.distanceTo(g) > r.distanceTo(g - 1), "distances increase (gate " + g + ")");
         double seg = r.distanceTo(g) - r.distanceTo(g - 1);
         check(near(seg, Stage.ALL[g - 1].length(), Stage.ALL[g - 1].length() * 0.05), "stage " + g + " is about its configured length");
      }
      check(near(r.length(), 12000.0, 400.0), "default course is about 12,000 blocks, was " + r.length());
      for (int g = 0; g < r.gates(); g++) {
         check(near(r.progressOf(r.gateX(g), r.gateZ(g)), r.distanceTo(g), 1e-6), "progress at gate " + g + " equals its distance");
      }
      for (double p = 0.0; p < r.length(); p += 137.0) {
         double[] pt = r.pointAt(p);
         check(near(r.progressOf(pt[0], pt[1]), p, 1e-6), "pointAt/progressOf round trip at " + p);
      }
      check(r.progressOf(r.gateX(0) - 10.0, 0.0) < 0.0, "behind the start line is negative progress");
      check(near(r.progressOf(r.gateX(9) + 25.0, r.gateZ(9)), r.length() + 25.0, 1e-6), "past the finish extends the last segment");
      double[] dir = r.directionAt(500.0);
      check(near(dir[0] * dir[0] + dir[1] * dir[1], 1.0, 1e-9) && dir[0] > 0.0, "direction is a unit vector heading east");
      Route small = Route.create(0.0, 0.0, 0.05);
      check(small.length() > 9 * 31.0 && small.length() < 9 * 120.0, "tiny scale keeps a minimum stage length");
      Route same = Route.create(100.0, -50.0, 1.0);
      check(same.gateZ(5) == r.gateZ(5), "the same origin always gives the same course");
   }

   static void points() {
      Points p = Points.parse(Points.DEFAULT_TABLE);
      check(p.forPlace(1) == 100 && p.forPlace(2) == 70 && p.forPlace(16) == 1, "default table");
      check(p.forPlace(17) == 0 && p.forPlace(0) == 0 && p.forPlace(-3) == 0, "places off the table score nothing");
      Points custom = Points.parse(" 10, x, 5 ,-2, 1");
      check(custom.forPlace(1) == 10 && custom.forPlace(2) == 5 && custom.forPlace(3) == 1 && custom.forPlace(4) == 0, "bad entries are skipped");
      check(Points.parse("").forPlace(1) == 100 && Points.parse(null).forPlace(1) == 100, "empty table falls back to default");
   }

   static Entrant entrant(String name, int points, int nextGate, double progress) {
      Entrant e = new Entrant(UUID.randomUUID(), name, 1, -1, Background.JOCKEY);
      e.points = points;
      e.nextGate = nextGate;
      e.progress = progress;
      return e;
   }

   static void standings() {
      Entrant a = entrant("a", 170, 3, 3400.0);
      Entrant b = entrant("b", 170, 3, 3600.0);
      Entrant c = entrant("c", 200, 2, 2000.0);
      Entrant d = entrant("d", 999, 4, 5000.0);
      d.retired = true;
      Entrant f = entrant("f", 50, Stage.GATES, 99999.0);
      f.finishTick = 500L;
      Entrant g = entrant("g", 40, Stage.GATES, 99999.0);
      g.finishTick = 400L;
      List<Entrant> all = new ArrayList<>(List.of(a, b, c, d, f, g));
      List<Entrant> overall = Standings.sorted(all, Standings.OVERALL);
      check(overall.get(0) == c, "most points leads overall");
      check(overall.indexOf(b) < overall.indexOf(a), "points tie goes to whoever is further along");
      check(overall.get(overall.size() - 1) == d, "retired racers go last overall, whatever their points");
      List<Entrant> road = Standings.sorted(all, Standings.ON_ROAD);
      check(road.get(0) == g && road.get(1) == f, "finishers lead on the road, earliest first");
      check(road.indexOf(b) < road.indexOf(a) && road.indexOf(a) < road.indexOf(c), "then by gate and distance");
      check(Standings.placeOf(all, c, Standings.OVERALL) == 1, "placeOf leader");
      check(Standings.placeOf(all, d, Standings.OVERALL) == 6, "placeOf retired");
      check(Standings.placeOf(all, entrant("x", 0, 0, 0), Standings.OVERALL) == 0, "placeOf someone not entered is 0");
      check(f.finished() && !f.racing() && !d.racing() && a.racing(), "finished/racing flags");
   }

   static void stamina() {
      Stamina.Tuning t = Stamina.Tuning.DEFAULT;
      Stamina.State s = Stamina.step(100.0, 100.0, false, 1.0, 10.0, 1.0, 1.0, t);
      check(near(s.stamina(), 84.0, 1e-9) && !s.exhausted(), "galloping drains 1.6/s");
      s = Stamina.step(50.0, 100.0, false, 0.5, 10.0, 1.0, 1.0, t);
      check(near(s.stamina(), 60.0, 1e-9), "moving slowly recovers 1/s");
      s = Stamina.step(50.0, 100.0, false, 0.0, 10.0, 1.0, 1.0, t);
      check(near(s.stamina(), 80.0, 1e-9), "standing recovers 3/s");
      s = Stamina.step(2.0, 100.0, false, 1.0, 5.0, 1.0, 1.0, t);
      check(s.stamina() == 0.0 && s.exhausted(), "running dry exhausts the horse");
      s = Stamina.step(10.0, 100.0, true, 1.0, 10.0, 1.0, 1.0, t);
      check(near(s.stamina(), 15.0, 1e-9) && s.exhausted(), "exhausted horses recover slowly even when pushed");
      s = Stamina.step(48.0, 100.0, true, 0.0, 2.0, 1.0, 1.0, t);
      check(!s.exhausted(), "exhaustion ends at the recover fraction");
      s = Stamina.step(100.0, 100.0, false, 0.0, 60.0, 1.0, 1.0, t);
      check(s.stamina() == 100.0, "never above max");
      s = Stamina.step(100.0, 100.0, false, 1.0, 10.0, 0.75, 1.0, t);
      check(near(s.stamina(), 88.0, 1e-9), "drain multiplier (terrain affinity) applies");
      check(near(Stamina.maxFor(100.0, 100), 150.0, 1e-9) && near(Stamina.maxFor(100.0, 0), 100.0, 1e-9), "bond raises max by up to 50%");
      check(near(Stamina.bondDrain(100), 0.75, 1e-9) && near(Stamina.bondDrain(-5), 1.0, 1e-9), "bond cuts drain by up to 25%");
      check(near(Stamina.restWhileUnridden(10.0, 100.0, 20.0, 1.0, t), 70.0, 1e-9), "rest while unridden");
      check(Affinity.of(2.0F, 70) == Affinity.DESERT && Affinity.of(0.0F, 70) == Affinity.SNOW && Affinity.of(0.8F, 140) == Affinity.MOUNTAIN
         && Affinity.of(0.8F, 70) == Affinity.PLAINS, "terrain from temperature and height");
   }

   /**
    * Rough balance check: a player on an average horse who rides flat out and lets it rest while exhausted should
    * average about the same pace as the field of rivals, so the race is close.
    */
   static void pacing() {
      Stamina.Tuning t = Stamina.Tuning.DEFAULT;
      double top = 0.225 * 43.17;
      double max = 100.0;
      double stamina = max;
      boolean exhausted = false;
      double dist = 0.0;
      double dt = 0.5;
      for (double time = 0.0; time < 3600.0; time += dt) {
         double speed = exhausted ? top * 0.4 : top;
         Stamina.State s = Stamina.step(stamina, max, exhausted, speed / top, dt, 1.0, 1.0, t);
         stamina = s.stamina();
         exhausted = s.exhausted();
         dist += speed * dt;
      }
      double playerPace = dist / 3600.0;
      System.out.printf("  pacing: average horse ridden flat out averages %.2f blocks/s%n", playerPace);
      double rivals = 0.0;
      for (Rivals.Profile p : Rivals.ALL) {
         rivals += p.speed();
      }
      rivals /= Rivals.ALL.length;
      check(playerPace > rivals * 0.9 && playerPace < rivals * 1.6, "player pace " + playerPace + " is close to the rival average " + rivals);
      double best = 0.0;
      for (Rivals.Profile p : Rivals.ALL) {
         best = Math.max(best, p.speed());
      }
      check(best < top, "no rival outruns a galloping average horse");
   }

   static void data() {
      check(Rivals.ALL.length == 24, "24 rivals");
      check(Rivals.ALL[3].onFoot() && "Sandman".equals(Rivals.ALL[3].name()), "Sandman runs");
      for (Rivals.Profile p : Rivals.ALL) {
         check(Rivals.SKINS[Rivals.skinIndex(p)].equals(p.skin()), "skin listed for " + p.id());
      }
      for (Background b : Background.ALL) {
         check(Background.byId(b.id()) == b, "background id round trip " + b.id());
      }
      check(Background.byId("nope") == null, "unknown background");
      check(Stage.forNextGate(0) == Stage.SAN_DIEGO_BEACH && Stage.forNextGate(9) == Stage.NEW_YORK && Stage.forNextGate(10) == Stage.NEW_YORK,
         "stage for next gate");
   }
}
