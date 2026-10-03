package com.curseddomain.registry;

import com.curseddomain.block.ConstructBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("cursed_domain");
   public static final DeferredBlock<ConstructBlock> CONSTRUCT = BLOCKS.register(
      "construct",
      () -> new ConstructBlock(Properties.of().mapColor(MapColor.ICE).strength(1.5F, 6.0F).sound(SoundType.AMETHYST).lightLevel(s -> 6).noLootTable())
   );

   private ModBlocks() {
   }
}
