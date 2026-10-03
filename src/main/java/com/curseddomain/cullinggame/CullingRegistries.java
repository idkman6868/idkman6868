package com.curseddomain.cullinggame;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.shibuya.RampantCurse;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import com.curseddomain.registry.ModCreativeTabs;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

/** Entities and items added by the Shibuya Incident / Culling Game update. */
public final class CullingRegistries {
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ModMain.MODID);
   public static final Items ITEMS = DeferredRegister.createItems(ModMain.MODID);
   public static final DeferredHolder<EntityType<?>, EntityType<SorcererNpcEntity>> SORCERER_NPC = ENTITY_TYPES.register(
      "sorcerer_npc", () -> Builder.of(SorcererNpcEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(10).build("sorcerer_npc")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<RampantCurse>> RAMPANT_CURSE = ENTITY_TYPES.register(
      "rampant_curse", () -> Builder.of(RampantCurse::new, MobCategory.MONSTER).sized(0.8F, 1.2F).clientTrackingRange(10).build("rampant_curse")
   );
   public static final DeferredItem<PrisonRealmItem> PRISON_REALM = ITEMS.register(
      "prison_realm", () -> new PrisonRealmItem(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant())
   );

   private CullingRegistries() {
   }

   static void creativeTab(BuildCreativeModeTabContentsEvent event) {
      if (ModCreativeTabs.MAIN.getKey().equals(event.getTabKey())) {
         event.accept((ItemLike)PRISON_REALM.get());
      }
   }

   static void attributes(EntityAttributeCreationEvent event) {
      event.put((EntityType)SORCERER_NPC.get(), SorcererNpcEntity.createAttributes().build());
      event.put((EntityType)RAMPANT_CURSE.get(), Grade4Curse.createAttributes().build());
   }
}
