package com.curseddomain.client.story;

import com.curseddomain.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

@EventBusSubscriber(
   modid = "cursed_domain",
   value = {Dist.CLIENT}
)
public final class HintTrail {
   private static final int DURATION = 100;
   private static int remaining;
   private static float yaw;
   private static boolean circling;

   private HintTrail() {
   }

   public static float lastYaw() {
      return yaw;
   }

   public static void start(float yawDegrees, float distance) {
      yaw = yawDegrees;
      circling = distance < 96.0F;
      remaining = 100;
   }

   @SubscribeEvent
   public static void onTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (remaining > 0 && player != null && mc.level != null && !mc.isPaused()) {
         remaining--;
         int age = 100 - remaining;
         if (circling) {
            for (int i = 0; i < 2; i++) {
               float a = (age * 9 + i * 180) * (float) (Math.PI / 180.0);
               Vec3 p = player.position().add(Mth.cos(a) * 1.6, 1.0 + Mth.sin(age * 0.2F) * 0.3, Mth.sin(a) * 1.6);
               mc.level.addParticle((ParticleOptions)ModParticles.TALISMAN.get(), p.x, p.y, p.z, -Mth.sin(a) * 0.05, 0.01, Mth.cos(a) * 0.05);
            }
         } else {
            Vec3 dir = new Vec3(-Mth.sin(yaw * (float) (Math.PI / 180.0)), 0.0, Mth.cos(yaw * (float) (Math.PI / 180.0)));
            Vec3 eye = player.getEyePosition().add(0.0, -0.4, 0.0);

            for (int i = 0; i < 2; i++) {
               double along = 1.5 + (age * 0.45 + i * 6) % 12.0;
               double sway = Math.sin((age + i * 11) * 0.3) * 0.35;
               Vec3 side = new Vec3(-dir.z, 0.0, dir.x).scale(sway);
               Vec3 p = eye.add(dir.scale(along)).add(side).add(0.0, Math.sin(along * 0.7) * 0.25, 0.0);
               mc.level.addParticle((ParticleOptions)ModParticles.TALISMAN.get(), p.x, p.y, p.z, dir.x * 0.09, 0.01, dir.z * 0.09);
            }
         }
      }
   }
}
