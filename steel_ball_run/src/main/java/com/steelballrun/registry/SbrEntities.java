package com.steelballrun.registry;

import com.steelballrun.SteelBallRun;
import com.steelballrun.npc.RivalRiderEntity;
import com.steelballrun.npc.StephenSteelEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SbrEntities {
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, SteelBallRun.MODID);
   public static final DeferredHolder<EntityType<?>, EntityType<StephenSteelEntity>> STEPHEN_STEEL = ENTITY_TYPES.register(
      "stephen_steel",
      () -> EntityType.Builder.<StephenSteelEntity>of(StephenSteelEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(10).build("stephen_steel")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<RivalRiderEntity>> RIVAL_RIDER = ENTITY_TYPES.register(
      "rival_rider",
      () -> EntityType.Builder.<RivalRiderEntity>of(RivalRiderEntity::new, MobCategory.MISC).sized(0.6F, 1.8F).clientTrackingRange(12).build("rival_rider")
   );

   private SbrEntities() {
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put(STEPHEN_STEEL.get(), StephenSteelEntity.createAttributes().build());
      event.put(RIVAL_RIDER.get(), RivalRiderEntity.createAttributes().build());
   }
}
