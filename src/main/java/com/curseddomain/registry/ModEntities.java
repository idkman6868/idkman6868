package com.curseddomain.registry;

import com.curseddomain.domain.DomainBarrierEntity;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import com.curseddomain.entity.projectile.TechniqueProjectile;
import com.curseddomain.shikigami.DivineDogEntity;
import com.curseddomain.shikigami.NueEntity;
import com.curseddomain.shikigami.TransfiguredHumanEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class ModEntities {
   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, "cursed_domain");
   public static final DeferredHolder<EntityType<?>, EntityType<Grade4Curse>> GRADE_4_CURSE = ENTITY_TYPES.register(
      "grade_4_curse", () -> Builder.of(Grade4Curse::new, MobCategory.MONSTER).sized(0.8F, 1.2F).clientTrackingRange(8).build("grade_4_curse")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<TechniqueProjectile>> TECHNIQUE_PROJECTILE = ENTITY_TYPES.register(
      "technique_projectile",
      () -> Builder.of(TechniqueProjectile::new, MobCategory.MISC)
         .sized(0.5F, 0.5F)
         .clientTrackingRange(10)
         .updateInterval(1)
         .noSave()
         .build("technique_projectile")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<DomainBarrierEntity>> DOMAIN_BARRIER = ENTITY_TYPES.register(
      "domain_barrier",
      () -> Builder.of(DomainBarrierEntity::new, MobCategory.MISC)
         .sized(1.0F, 1.0F)
         .clientTrackingRange(16)
         .updateInterval(20)
         .noSave()
         .fireImmune()
         .build("domain_barrier")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<DivineDogEntity>> DIVINE_DOG = ENTITY_TYPES.register(
      "divine_dog", () -> Builder.of(DivineDogEntity::new, MobCategory.MISC).sized(0.8F, 1.0F).clientTrackingRange(10).noSave().build("divine_dog")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<NueEntity>> NUE = ENTITY_TYPES.register(
      "nue", () -> Builder.of(NueEntity::new, MobCategory.MISC).sized(1.2F, 1.0F).clientTrackingRange(10).noSave().build("nue")
   );
   public static final DeferredHolder<EntityType<?>, EntityType<TransfiguredHumanEntity>> TRANSFIGURED_HUMAN = ENTITY_TYPES.register(
      "transfigured_human",
      () -> Builder.of(TransfiguredHumanEntity::new, MobCategory.MISC).sized(0.7F, 2.1F).clientTrackingRange(10).noSave().build("transfigured_human")
   );

   private ModEntities() {
   }

   @SubscribeEvent
   public static void attributes(EntityAttributeCreationEvent event) {
      event.put((EntityType)GRADE_4_CURSE.get(), Grade4Curse.createAttributes().build());
      event.put((EntityType)DIVINE_DOG.get(), DivineDogEntity.createAttributes().build());
      event.put((EntityType)NUE.get(), NueEntity.createAttributes().build());
      event.put((EntityType)TRANSFIGURED_HUMAN.get(), TransfiguredHumanEntity.createAttributes().build());
   }

   @SubscribeEvent
   public static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
      event.register(
         (EntityType)GRADE_4_CURSE.get(), SpawnPlacementTypes.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, Operation.REPLACE
      );
   }
}
