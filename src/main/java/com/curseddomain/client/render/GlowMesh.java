package com.curseddomain.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class GlowMesh {
   private GlowMesh() {
   }

   public static int withAlpha(int argb, float alpha) {
      int a = Mth.clamp((int)((argb >>> 24 & 0xFF) * alpha), 0, 255);
      return a << 24 | argb & 16777215;
   }

   private static void v(VertexConsumer vc, Pose pose, double x, double y, double z, int argb) {
      vc.addVertex(pose, (float)x, (float)y, (float)z).setColor(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, argb >>> 24 & 0xFF);
   }

   public static void sphere(Pose pose, VertexConsumer vc, double cx, double cy, double cz, float r, int argb, int segments) {
      int rings = Math.max(4, segments / 2);

      for (int i = 0; i < rings; i++) {
         float t0 = (float) Math.PI * i / rings - (float) (Math.PI / 2);
         float t1 = (float) Math.PI * (i + 1) / rings - (float) (Math.PI / 2);

         for (int j = 0; j < segments; j++) {
            float p0 = (float) (Math.PI * 2) * j / segments;
            float p1 = (float) (Math.PI * 2) * (j + 1) / segments;
            v(vc, pose, cx + r * Mth.cos(t0) * Mth.cos(p0), cy + r * Mth.sin(t0), cz + r * Mth.cos(t0) * Mth.sin(p0), argb);
            v(vc, pose, cx + r * Mth.cos(t0) * Mth.cos(p1), cy + r * Mth.sin(t0), cz + r * Mth.cos(t0) * Mth.sin(p1), argb);
            v(vc, pose, cx + r * Mth.cos(t1) * Mth.cos(p1), cy + r * Mth.sin(t1), cz + r * Mth.cos(t1) * Mth.sin(p1), argb);
            v(vc, pose, cx + r * Mth.cos(t1) * Mth.cos(p0), cy + r * Mth.sin(t1), cz + r * Mth.cos(t1) * Mth.sin(p0), argb);
         }
      }
   }

   public static void orb(Pose pose, VertexConsumer vc, double cx, double cy, double cz, float r, int argb, float alpha) {
      sphere(pose, vc, cx, cy, cz, r * 0.45F, withAlpha(-1, alpha * 0.9F), 12);
      sphere(pose, vc, cx, cy, cz, r * 0.75F, withAlpha(argb, alpha * 0.8F), 14);
      sphere(pose, vc, cx, cy, cz, r, withAlpha(argb, alpha * 0.35F), 16);
      sphere(pose, vc, cx, cy, cz, r * 1.5F, withAlpha(argb, alpha * 0.12F), 16);
   }

   public static void body(Pose pose, VertexConsumer shade, double cx, double cy, double cz, float r, int argb, float alpha) {
      sphere(pose, shade, cx, cy, cz, r, withAlpha(argb, alpha), 14);
   }

   public static void ring(Pose pose, VertexConsumer vc, double cx, double cy, double cz, float r, float width, int argb) {
      int seg = Math.max(24, (int)(r * 8.0F));
      int clear = withAlpha(argb, 0.0F);
      float inner = Math.max(0.0F, r - width);

      for (int j = 0; j < seg; j++) {
         float a0 = (float) (Math.PI * 2) * j / seg;
         float a1 = (float) (Math.PI * 2) * (j + 1) / seg;
         v(vc, pose, cx + inner * Mth.cos(a0), cy, cz + inner * Mth.sin(a0), clear);
         v(vc, pose, cx + inner * Mth.cos(a1), cy, cz + inner * Mth.sin(a1), clear);
         v(vc, pose, cx + r * Mth.cos(a1), cy, cz + r * Mth.sin(a1), argb);
         v(vc, pose, cx + r * Mth.cos(a0), cy, cz + r * Mth.sin(a0), argb);
      }
   }

   public static void ring(Pose pose, VertexConsumer vc, Vec3 c, Vec3 u, Vec3 w, float r, float width, int argb) {
      int seg = Math.max(24, (int)(r * 8.0F));
      int clear = withAlpha(argb, 0.0F);
      float inner = Math.max(0.0F, r - width);

      for (int j = 0; j < seg; j++) {
         float a0 = (float) (Math.PI * 2) * j / seg;
         float a1 = (float) (Math.PI * 2) * (j + 1) / seg;
         Vec3 d0 = u.scale(Mth.cos(a0)).add(w.scale(Mth.sin(a0)));
         Vec3 d1 = u.scale(Mth.cos(a1)).add(w.scale(Mth.sin(a1)));
         put(vc, pose, c.add(d0.scale(inner)), clear);
         put(vc, pose, c.add(d1.scale(inner)), clear);
         put(vc, pose, c.add(d1.scale(r)), argb);
         put(vc, pose, c.add(d0.scale(r)), argb);
      }
   }

   private static void put(VertexConsumer vc, Pose pose, Vec3 p, int argb) {
      v(vc, pose, p.x, p.y, p.z, argb);
   }

   public static void beam(Pose pose, VertexConsumer vc, Vec3 a, Vec3 b, float radius, int argb, int sides) {
      Vec3 axis = b.subtract(a);
      if (!(axis.lengthSqr() < 1.0E-6)) {
         Vec3 dir = axis.normalize();
         Vec3 u = perpendicular(dir);
         Vec3 w = dir.cross(u).normalize();

         for (int i = 0; i < sides; i++) {
            float a0 = (float) (Math.PI * 2) * i / sides;
            float a1 = (float) (Math.PI * 2) * (i + 1) / sides;
            Vec3 o0 = u.scale(Mth.cos(a0) * radius).add(w.scale(Mth.sin(a0) * radius));
            Vec3 o1 = u.scale(Mth.cos(a1) * radius).add(w.scale(Mth.sin(a1) * radius));
            put(vc, pose, a.add(o0), argb);
            put(vc, pose, a.add(o1), argb);
            put(vc, pose, b.add(o1), argb);
            put(vc, pose, b.add(o0), argb);
         }
      }
   }

   public static void energyBeam(Pose pose, VertexConsumer vc, Vec3 a, Vec3 b, float radius, int argb, float alpha) {
      beam(pose, vc, a, b, radius * 0.35F, withAlpha(-1, alpha), 8);
      beam(pose, vc, a, b, radius * 0.7F, withAlpha(argb, alpha * 0.7F), 10);
      beam(pose, vc, a, b, radius, withAlpha(argb, alpha * 0.25F), 12);
   }

   public static void arc(Pose pose, VertexConsumer vc, Vec3 c, Vec3 u, Vec3 w, float radius, float start, float sweep, float width, int argb, float reveal) {
      int seg = 24;
      int shown = Math.max(1, (int)(seg * Mth.clamp(reveal, 0.0F, 1.0F)));

      for (int i = 0; i < shown; i++) {
         float f0 = (float)i / seg;
         float f1 = (float)(i + 1) / seg;
         float th0 = Mth.sin(f0 * (float) Math.PI) * width;
         float th1 = Mth.sin(f1 * (float) Math.PI) * width;
         float a0 = start + sweep * f0;
         float a1 = start + sweep * f1;
         Vec3 d0 = u.scale(Mth.cos(a0)).add(w.scale(Mth.sin(a0)));
         Vec3 d1 = u.scale(Mth.cos(a1)).add(w.scale(Mth.sin(a1)));
         int c0 = withAlpha(argb, 0.3F + 0.7F * Mth.sin(f0 * (float) Math.PI));
         int c1 = withAlpha(argb, 0.3F + 0.7F * Mth.sin(f1 * (float) Math.PI));
         put(vc, pose, c.add(d0.scale(radius - th0)), c0);
         put(vc, pose, c.add(d1.scale(radius - th1)), c1);
         put(vc, pose, c.add(d1.scale(radius + th1 * 0.3F)), c1);
         put(vc, pose, c.add(d0.scale(radius + th0 * 0.3F)), c0);
      }
   }

   public static void bolt(Pose pose, VertexConsumer vc, Vec3 a, Vec3 b, float width, int argb, long seed) {
      Random random = new Random(seed);
      Vec3 axis = b.subtract(a);
      double length = axis.length();
      if (!(length < 1.0E-4)) {
         Vec3 dir = axis.scale(1.0 / length);
         Vec3 u = perpendicular(dir);
         Vec3 w = dir.cross(u).normalize();
         int segments = Math.max(3, (int)(length * 1.5));
         Vec3 prev = a;

         for (int i = 1; i <= segments; i++) {
            Vec3 next = i == segments
               ? b
               : a.add(dir.scale(length * i / segments))
                  .add(u.scale((random.nextDouble() - 0.5) * length * 0.12))
                  .add(w.scale((random.nextDouble() - 0.5) * length * 0.12));
            ribbon(pose, vc, prev, next, u, width, argb);
            ribbon(pose, vc, prev, next, w, width, argb);
            prev = next;
         }
      }
   }

   private static void ribbon(Pose pose, VertexConsumer vc, Vec3 a, Vec3 b, Vec3 side, float width, int argb) {
      Vec3 o = side.scale(width / 2.0F);
      put(vc, pose, a.subtract(o), argb);
      put(vc, pose, a.add(o), argb);
      put(vc, pose, b.add(o), argb);
      put(vc, pose, b.subtract(o), argb);
   }

   public static Vec3 perpendicular(Vec3 dir) {
      Vec3 ref = Math.abs(dir.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
      return dir.cross(ref).normalize();
   }
}
