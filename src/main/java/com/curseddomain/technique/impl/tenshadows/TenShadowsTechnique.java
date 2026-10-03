package com.curseddomain.technique.impl.tenshadows;

import com.curseddomain.combat.Moves;
import com.curseddomain.domain.DomainExpansion;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.shikigami.DivineDogEntity;
import com.curseddomain.shikigami.NueEntity;
import com.curseddomain.shikigami.ShadowStorage;
import com.curseddomain.shikigami.ShikigamiEntity;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.ability.Ability;
import com.curseddomain.technique.ability.AbilityContext;
import com.curseddomain.technique.ability.AbilityStats;
import com.curseddomain.technique.impl.ImplementedTechnique;
import com.curseddomain.vfx.Vfx;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.phys.Vec3;

public class TenShadowsTechnique extends ImplementedTechnique {
   public static final int SHADOW = -11904352;

   public TenShadowsTechnique(Technique.Properties properties) {
      super(properties);
   }

   @Override
   protected List<Ability> createAbilities() {
      return List.of(
         Ability.instant("divine_dogs", AbilityStats.builder().cost(25.0F).cooldownSeconds(5.0F).build(), TenShadowsTechnique::dogs),
         Ability.instant("nue", AbilityStats.builder().cost(30.0F).cooldownSeconds(5.0F).build(), TenShadowsTechnique::nue),
         Ability.instant("shadow_step", AbilityStats.builder().cost(10.0F).cooldownSeconds(4.0F).range(20.0F).build(), TenShadowsTechnique::shadowStep),
         Ability.instant("shadow_storage", AbilityStats.builder().cost(2.0F).cooldownSeconds(1.0F).build(), TenShadowsTechnique::storage)
      );
   }

   @Override
   protected DomainExpansion createDomain() {
      return new ChimeraShadowGarden();
   }

   static <T extends ShikigamiEntity> List<T> owned(ServerPlayer player, Class<T> type) {
      return player.serverLevel().getEntitiesOfClass(type, player.getBoundingBox().inflate(96.0), s -> player.getUUID().equals(s.summonerUUID()));
   }

   static <T extends ShikigamiEntity> T summon(ServerPlayer player, EntityType<T> type, Vec3 at) {
      ServerLevel level = player.serverLevel();
      T s = (T)type.create(level);
      if (s == null) {
         return null;
      } else {
         s.moveTo(at.x, at.y, at.z, player.getYRot(), 0.0F);
         s.setOwner(player);
         s.finalizeSpawn(level, level.getCurrentDifficultyAt(s.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
         level.addFreshEntity(s);
         Vfx.ring(level, at, -15724520, 1.6F, 14);
         Vfx.pillar(level, at, -11904352, 2.2F, 12);
         return s;
      }
   }

   private static Vec3 beside(ServerPlayer player, double side) {
      Vec3 look = player.getLookAngle();
      Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
      return player.position().add(look.x * 1.5, 0.0, look.z * 1.5).add(right.scale(side));
   }

   private static boolean dogs(AbilityContext ctx) {
      List<DivineDogEntity> existing = owned(ctx.player, DivineDogEntity.class);
      if (!existing.isEmpty()) {
         existing.forEach(ShikigamiEntity::dismiss);
         return false;
      } else {
         SorcererData data = SorcererManager.get(ctx.player);
         boolean white = !data.isUnlocked("lost:divine_dog_white");
         boolean black = !data.isUnlocked("lost:divine_dog_black");
         boolean totality = data.isUnlocked("shikigami:totality") && !data.isUnlocked("lost:divine_dog_totality");
         ctx.sound(SoundEvents.WOLF_HOWL, 0.8F, 0.9F);
         if (!totality || white && black) {
            if (!white && !black) {
               ctx.player.displayClientMessage(Component.translatable("shikigami.cursed_domain.none_left").withStyle(ChatFormatting.GRAY), true);
               return false;
            } else {
               if (white) {
                  DivineDogEntity dog = summon(ctx.player, (EntityType<DivineDogEntity>)ModEntities.DIVINE_DOG.get(), beside(ctx.player, -1.2));
                  if (dog != null) {
                     dog.setVariant(0);
                  }
               }

               if (black) {
                  DivineDogEntity dog = summon(ctx.player, (EntityType<DivineDogEntity>)ModEntities.DIVINE_DOG.get(), beside(ctx.player, 1.2));
                  if (dog != null) {
                     dog.setVariant(1);
                  }
               }

               return true;
            }
         } else {
            DivineDogEntity dog = summon(ctx.player, (EntityType<DivineDogEntity>)ModEntities.DIVINE_DOG.get(), beside(ctx.player, 0.0));
            if (dog != null) {
               dog.setVariant(2);
            }

            return dog != null;
         }
      }
   }

   private static boolean nue(AbilityContext ctx) {
      List<NueEntity> existing = owned(ctx.player, NueEntity.class);
      if (!existing.isEmpty()) {
         existing.forEach(ShikigamiEntity::dismiss);
         return false;
      } else {
         boolean tamed = SorcererManager.get(ctx.player).isUnlocked("shikigami:nue");
         NueEntity nue = summon(ctx.player, (EntityType<NueEntity>)ModEntities.NUE.get(), ctx.player.position().add(0.0, 3.0, 0.0));
         if (nue == null) {
            return false;
         } else {
            if (!tamed) {
               nue.setRitual(true);
               ctx.player
                  .displayClientMessage(
                     Component.translatable("shikigami.cursed_domain.ritual", new Object[]{nue.getDisplayName()}).withStyle(ChatFormatting.GOLD), false
                  );
            }

            ctx.sound(SoundEvents.PHANTOM_FLAP, 1.0F, 0.6F);
            return true;
         }
      }
   }

   private static boolean shadowStep(AbilityContext ctx) {
      Vec3 from = ctx.player.position();
      Vec3 to = Moves.dashTarget(ctx.level, ctx.player, from, ctx.look(), ctx.stats.range());
      if (to.distanceTo(from) < 2.0) {
         return false;
      } else {
         Vfx.ring(ctx.level, from, -15724520, 1.4F, 12);
         Vfx.pillar(ctx.level, from, -11904352, 2.0F, 8);
         Moves.teleport(ctx.player, to);
         Vfx.ring(ctx.level, to, -15724520, 1.4F, 12);
         Vfx.pillar(ctx.level, to, -11904352, 2.0F, 8);
         ctx.sound(SoundEvents.ENDERMAN_TELEPORT, 0.6F, 0.6F);
         return true;
      }
   }

   private static boolean storage(AbilityContext ctx) {
      ShadowStorage storage = (ShadowStorage)ctx.player.getData(ModAttachments.SHADOW_STORAGE);
      ctx.player
         .openMenu(
            new SimpleMenuProvider(
               (id, inventory, p) -> ChestMenu.threeRows(id, inventory, storage.container), Component.translatable("container.cursed_domain.shadow_storage")
            )
         );
      ctx.sound(SoundEvents.ENDER_CHEST_OPEN, 0.7F, 0.6F);
      return true;
   }
}
