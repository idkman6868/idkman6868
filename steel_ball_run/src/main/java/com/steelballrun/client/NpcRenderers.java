package com.steelballrun.client;

import com.steelballrun.SteelBallRun;
import com.steelballrun.npc.RivalRiderEntity;
import com.steelballrun.npc.Rivals;
import com.steelballrun.npc.StephenSteelEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Both NPCs use the player model with their own skins. */
public final class NpcRenderers {
   private static final ResourceLocation STEEL = SteelBallRun.id("textures/entity/stephen_steel.png");
   private static final ResourceLocation[] RIVALS = new ResourceLocation[Rivals.SKINS.length];

   static {
      for (int i = 0; i < RIVALS.length; i++) {
         RIVALS[i] = SteelBallRun.id("textures/entity/rival/" + Rivals.SKINS[i] + ".png");
      }
   }

   private NpcRenderers() {
   }

   public static final class Steel extends HumanoidMobRenderer<StephenSteelEntity, HumanoidModel<StephenSteelEntity>> {
      public Steel(EntityRendererProvider.Context context) {
         super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
      }

      @Override
      public ResourceLocation getTextureLocation(StephenSteelEntity entity) {
         return STEEL;
      }
   }

   public static final class Rival extends HumanoidMobRenderer<RivalRiderEntity, HumanoidModel<RivalRiderEntity>> {
      public Rival(EntityRendererProvider.Context context) {
         super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
      }

      @Override
      public ResourceLocation getTextureLocation(RivalRiderEntity entity) {
         int skin = entity.skin();
         return RIVALS[skin >= 0 && skin < RIVALS.length ? skin : 0];
      }
   }
}
