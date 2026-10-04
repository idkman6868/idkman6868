package com.steelballrun.horse;

import com.steelballrun.config.SbrConfig;
import com.steelballrun.race.Background;
import com.steelballrun.registry.SbrAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Makes horses: starter horses for registered riders. */
public final class HorseFactory {
   private HorseFactory() {
   }

   /** A tamed horse for a new rider, standing next to them. Its quality depends on their background. */
   @Nullable
   public static Horse spawnStarter(ServerLevel level, ServerPlayer owner, Background background) {
      Horse horse = (Horse)EntityType.HORSE.create(level);
      if (horse == null) {
         return null;
      }
      RandomSource r = level.getRandom();
      Vec3 look = owner.getLookAngle();
      horse.moveTo(owner.getX() + look.x * 2.0, owner.getY(), owner.getZ() + look.z * 2.0, owner.getYRot(), 0.0F);
      horse.finalizeSpawn(level, level.getCurrentDifficultyAt(horse.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
      double speed = switch (background) {
         case JOCKEY -> 0.235 + r.nextDouble() * 0.03;
         case WANDERER -> 0.17 + r.nextDouble() * 0.14;
         default -> 0.21 + r.nextDouble() * 0.03;
      };
      horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
      horse.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(0.6 + r.nextDouble() * 0.25);
      horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(22.0 + r.nextInt(9));
      horse.setHealth(horse.getMaxHealth());
      horse.tameWithName(owner);
      HorseData data = horse.getData(SbrAttachments.HORSE);
      data.roll(r);
      if (background == Background.ZEPPELI_APPRENTICE) {
         data.addBond(40);
      } else if (background == Background.JOCKEY) {
         data.addBond(15);
      }
      data.stamina = data.max(SbrConfig.HORSE_STAMINA.get());
      data.lastUpdate = level.getGameTime();
      level.addFreshEntity(horse);
      return horse;
   }
}
