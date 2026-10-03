package com.curseddomain.gametest;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

final class TestSupport {
   static final String TEMPLATE = "empty";

   private TestSupport() {
   }

   static ServerPlayer player(GameTestHelper helper) {
      TestSupport.TestPlayer player = new TestSupport.TestPlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "jjk-test"));
      Vec3 pos = helper.absoluteVec(new Vec3(2.5, 1.0, 2.5));
      player.setPos(pos.x, pos.y, pos.z);
      return player;
   }

   static boolean near(float actual, float expected) {
      return Math.abs(actual - expected) < 0.001F;
   }

   static final class TestPlayer extends FakePlayer {
      TestPlayer(ServerLevel level, GameProfile profile) {
         super(level, profile);

         try {
            Field f = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
            f.setAccessible(true);
            f.setInt(this, 0);
         } catch (ReflectiveOperationException var4) {
            throw new IllegalStateException(var4);
         }
      }

      public boolean isInvulnerableTo(DamageSource source) {
         return false;
      }
   }
}
