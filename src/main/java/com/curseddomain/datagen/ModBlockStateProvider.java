package com.curseddomain.datagen;

import com.curseddomain.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModBlockStateProvider extends BlockStateProvider {
   public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
      super(output, "cursed_domain", helper);
   }

   protected void registerStatesAndModels() {
      this.simpleBlock((Block)ModBlocks.CONSTRUCT.get());
   }
}
