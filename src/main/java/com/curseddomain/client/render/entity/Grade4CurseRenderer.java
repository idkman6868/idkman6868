package com.curseddomain.client.render.entity;

import com.curseddomain.ModMain;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class Grade4CurseRenderer extends MobRenderer<Grade4Curse, Grade4CurseModel> {
   private static final ResourceLocation TEXTURE = ModMain.id("textures/entity/grade_4_curse.png");
   private static final RenderType EYES = RenderType.eyes(ModMain.id("textures/entity/grade_4_curse_eyes.png"));

   public Grade4CurseRenderer(Context context) {
      super(context, new Grade4CurseModel(context.bakeLayer(Grade4CurseModel.LAYER)), 0.45F);
      this.addLayer(new EyesLayer<Grade4Curse, Grade4CurseModel>(this) {
         public RenderType renderType() {
            return Grade4CurseRenderer.EYES;
         }
      });
   }

   public ResourceLocation getTextureLocation(Grade4Curse entity) {
      return TEXTURE;
   }
}
