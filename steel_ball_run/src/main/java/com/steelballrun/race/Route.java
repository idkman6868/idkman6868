package com.steelballrun.race;

/**
 * The race course: a polyline of checkpoint gates running east (+X) from the start line. Pure maths, no Minecraft
 * types, so it can be tested outside the game.
 */
public final class Route {
   private final double[] gx;
   private final double[] gz;
   /** Distance along the course from the start line to each gate. */
   private final double[] cum;

   private Route(double[] gx, double[] gz, double[] cum) {
      this.gx = gx;
      this.gz = gz;
      this.cum = cum;
   }

   /**
    * Lays the course out from the start line at ({@code ox}, {@code oz}). Gates wander north and south a little so the
    * ride isn't a ruler-straight line; the offsets are fixed, so a world always gets the same course.
    */
   public static Route create(double ox, double oz, double scale) {
      int n = Stage.GATES;
      double[] gx = new double[n];
      double[] gz = new double[n];
      double[] cum = new double[n];
      gx[0] = ox;
      gz[0] = oz;
      for (int i = 1; i < n; i++) {
         double len = Math.max(32.0, Stage.ALL[i - 1].length() * scale);
         double lateral = Math.min(160.0, len * 0.09) * Math.sin(i * 1.9);
         double dz = (oz + lateral) - gz[i - 1];
         double dx = Math.sqrt(Math.max(len * len - dz * dz, len * len * 0.5));
         gx[i] = gx[i - 1] + dx;
         gz[i] = gz[i - 1] + dz;
         cum[i] = cum[i - 1] + Math.sqrt(dx * dx + dz * dz);
      }
      return new Route(gx, gz, cum);
   }

   public int gates() {
      return this.gx.length;
   }

   public double gateX(int gate) {
      return this.gx[gate];
   }

   public double gateZ(int gate) {
      return this.gz[gate];
   }

   /** Distance along the course from the start line to a gate. */
   public double distanceTo(int gate) {
      return this.cum[gate];
   }

   public double length() {
      return this.cum[this.cum.length - 1];
   }

   /**
    * How far along the course a point is. The course always heads east, so the segment is found by X. Points before
    * the start or past the finish extend the first or last segment.
    */
   public double progressOf(double x, double z) {
      int last = this.gx.length - 1;
      if (x <= this.gx[0]) {
         return x - this.gx[0];
      } else if (x >= this.gx[last]) {
         return this.cum[last] + (x - this.gx[last]);
      } else {
         int i = 0;
         while (i < last - 1 && x > this.gx[i + 1]) {
            i++;
         }
         double t = (x - this.gx[i]) / (this.gx[i + 1] - this.gx[i]);
         return this.cum[i] + t * (this.cum[i + 1] - this.cum[i]);
      }
   }

   /** The point on the course at a given distance from the start line: {x, z}. */
   public double[] pointAt(double progress) {
      int last = this.gx.length - 1;
      if (progress <= 0.0) {
         return new double[]{this.gx[0] + progress, this.gz[0]};
      } else if (progress >= this.cum[last]) {
         return new double[]{this.gx[last] + (progress - this.cum[last]), this.gz[last]};
      } else {
         int i = 0;
         while (i < last - 1 && progress > this.cum[i + 1]) {
            i++;
         }
         double t = (progress - this.cum[i]) / (this.cum[i + 1] - this.cum[i]);
         return new double[]{this.gx[i] + t * (this.gx[i + 1] - this.gx[i]), this.gz[i] + t * (this.gz[i + 1] - this.gz[i])};
      }
   }

   /** Unit vector pointing along the course at a given distance: {dx, dz}. */
   public double[] directionAt(double progress) {
      double[] a = this.pointAt(progress - 4.0);
      double[] b = this.pointAt(progress + 4.0);
      double dx = b[0] - a[0];
      double dz = b[1] - a[1];
      double len = Math.sqrt(dx * dx + dz * dz);
      return len < 1.0E-6 ? new double[]{1.0, 0.0} : new double[]{dx / len, dz / len};
   }
}
