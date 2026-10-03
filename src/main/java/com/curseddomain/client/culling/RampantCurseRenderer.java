package com.curseddomain.client.culling;

import com.curseddomain.ModMain;
import com.curseddomain.client.render.entity.Grade4CurseRenderer;
import com.curseddomain.entity.cursedspirit.Grade4Curse;
import com.curseddomain.shibuya.CurseVariant;
import com.curseddomain.shibuya.RampantCurse;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

/** Same body as the Grade 4 curse, recoloured per variant; size comes from the scale attribute. */
public class RampantCurseRenderer extends Grade4CurseRenderer {
   private static final ResourceLocation[] TEXTURES = new ResourceLocation[CurseVariant.values().length];

   public RampantCurseRenderer(Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(Grade4Curse entity) {
      if (entity instanceof RampantCurse curse) {
         CurseVariant variant = curse.variant();
         ResourceLocation texture = TEXTURES[variant.ordinal()];
         if (texture == null) {
            texture = ModMain.id(variant.texture());
            TEXTURES[variant.ordinal()] = texture;
         }

         return texture;
      } else {
         return super.getTextureLocation(entity);
      }
   }
}
