package com.curseddomain.datagen;

import com.curseddomain.cullinggame.CullingRegistries;
import com.curseddomain.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ModItemModelProvider extends ItemModelProvider {
   public ModItemModelProvider(PackOutput output, ExistingFileHelper helper) {
      super(output, "cursed_domain", helper);
   }

   protected void registerModels() {
      ModItems.ITEMS.getEntries().forEach(holder -> {
         if (holder.get() instanceof SwordItem) {
            this.handheldItem((Item)holder.get());
         } else if (holder.get() instanceof SpawnEggItem) {
            this.withExistingParent(holder.getId().getPath(), this.mcLoc("item/template_spawn_egg"));
         } else if (!ModItems.CUSTOM_MODELS.contains(holder.getId().getPath())) {
            this.basicItem((Item)holder.get());
         }
      });
      CullingRegistries.ITEMS.getEntries().forEach(holder -> this.basicItem((Item)holder.get()));
   }
}
