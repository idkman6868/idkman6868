package com.curseddomain.client.render.entity;

import com.curseddomain.ModMain;
import com.curseddomain.shikigami.DivineDogEntity;
import com.curseddomain.shikigami.NueEntity;
import com.curseddomain.shikigami.ShikigamiEntity;
import com.curseddomain.shikigami.TransfiguredHumanEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public final class ShikigamiRenderers {
   private ShikigamiRenderers() {
   }

   private static <T extends ShikigamiEntity, M extends EntityModel<T>> void eyes(MobRenderer<T, M> renderer, String texture) {
      final RenderType type = RenderType.eyes(ModMain.id("textures/entity/" + texture + ".png"));
      renderer.addLayer(new EyesLayer<T, M>(renderer) {
         public RenderType renderType() {
            return type;
         }
      });
   }

   public static class Dog extends MobRenderer<DivineDogEntity, ShikigamiModels.DogModel> {
      private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
         ModMain.id("textures/entity/divine_dog_white.png"),
         ModMain.id("textures/entity/divine_dog_black.png"),
         ModMain.id("textures/entity/divine_dog_totality.png")
      };

      public Dog(Context context) {
         super(context, new ShikigamiModels.DogModel(context.bakeLayer(ShikigamiModels.DIVINE_DOG)), 0.5F);
         ShikigamiRenderers.eyes(this, "divine_dog_eyes");
      }

      public ResourceLocation getTextureLocation(DivineDogEntity entity) {
         return TEXTURES[Math.max(0, Math.min(2, entity.variant()))];
      }
   }

   public static class Human extends MobRenderer<TransfiguredHumanEntity, ShikigamiModels.HumanModel> {
      private static final ResourceLocation TEXTURE = ModMain.id("textures/entity/transfigured_human.png");

      public Human(Context context) {
         super(context, new ShikigamiModels.HumanModel(context.bakeLayer(ShikigamiModels.TRANSFIGURED_HUMAN)), 0.45F);
         ShikigamiRenderers.eyes(this, "transfigured_human_eyes");
      }

      public ResourceLocation getTextureLocation(TransfiguredHumanEntity entity) {
         return TEXTURE;
      }
   }

   public static class Nue extends MobRenderer<NueEntity, ShikigamiModels.NueModel> {
      private static final ResourceLocation TEXTURE = ModMain.id("textures/entity/nue.png");

      public Nue(Context context) {
         super(context, new ShikigamiModels.NueModel(context.bakeLayer(ShikigamiModels.NUE)), 0.5F);
         ShikigamiRenderers.eyes(this, "nue_eyes");
      }

      public ResourceLocation getTextureLocation(NueEntity entity) {
         return TEXTURE;
      }
   }
}
