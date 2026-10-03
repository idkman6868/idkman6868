package com.curseddomain.world;

import com.curseddomain.ModMain;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModParticles;
import com.curseddomain.sorcerer.AwakeningCause;
import com.curseddomain.sorcerer.AwakeningManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class SealBreak {
   private SealBreak() {
   }

   public static BlockPos nearPlayer(ServerPlayer player) {
      RandomSource random = player.getRandom();
      double angle = random.nextDouble() * Math.PI * 2.0;
      double distance = 8.0 + random.nextDouble() * 8.0;
      int x = (int)(player.getX() + Math.cos(angle) * distance);
      int z = (int)(player.getZ() + Math.sin(angle) * distance);
      ServerLevel level = player.serverLevel();
      BlockPos pos = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
      trigger(level, pos);
      return pos;
   }

   public static void trigger(ServerLevel level, BlockPos pos) {
      double x = pos.getX() + 0.5;
      double y = pos.getY() + 0.5;
      double z = pos.getZ() + 0.5;
      level.playSound(null, pos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.5F, 0.6F);
      level.playSound(null, pos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.0F, 0.5F);
      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 80, 0.4, 2.5, 0.4, 0.05);
      level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 30, 1.2, 0.8, 1.2, 0.03);
      level.sendParticles(ParticleTypes.SMOKE, x, y, z, 30, 0.8, 0.4, 0.8, 0.01);
      level.sendParticles((SimpleParticleType)ModParticles.CURSED_WISP.get(), x, y + 1.0, z, 120, 3.0, 2.0, 3.0, 0.05);
      int radius = (Integer)ServerConfig.SEAL_BREAK_RADIUS.get();

      for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(x, y, z) <= 4096.0)) {
         player.sendSystemMessage(
            Component.translatable("story.cursed_domain.seal_break").withStyle(new ChatFormatting[]{ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC})
         );
         if (player.distanceToSqr(x, y, z) <= (double)radius * radius) {
            AwakeningManager.awaken(player, AwakeningCause.SEAL_BREAK);
         }
      }

      int curses = 2 + level.random.nextInt(2);

      for (int i = 0; i < curses; i++) {
         Grade4Curse curse = (Grade4Curse)((EntityType)ModEntities.GRADE_4_CURSE.get()).create(level);
         if (curse != null) {
            curse.moveTo(x + level.random.nextInt(5) - 2.0, y + 0.5, z + level.random.nextInt(5) - 2.0, level.random.nextFloat() * 360.0F, 0.0F);
            curse.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(curse);
         }
      }

      ModMain.LOGGER.info("[story] seal broke at {}", pos);
   }
}
