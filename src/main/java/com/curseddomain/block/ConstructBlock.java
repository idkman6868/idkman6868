package com.curseddomain.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ConstructBlock extends Block {
   public static final int LIFETIME = 600;

   public ConstructBlock(Properties properties) {
      super(properties);
   }

   protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
      super.onPlace(state, level, pos, oldState, movedByPiston);
      if (!level.isClientSide) {
         level.scheduleTick(pos, this, 600 + level.random.nextInt(40));
      }
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      level.removeBlock(pos, false);
   }
}
