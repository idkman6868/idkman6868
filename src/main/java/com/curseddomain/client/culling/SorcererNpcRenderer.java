package com.curseddomain.client.culling;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.npc.NpcProfile;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Culling Game players use the player model with their own skins. */
public class SorcererNpcRenderer extends HumanoidMobRenderer<SorcererNpcEntity, HumanoidModel<SorcererNpcEntity>> {
   private static final ResourceLocation[] TEXTURES = new ResourceLocation[NpcProfile.values().length];

   public SorcererNpcRenderer(Context context) {
      super(context, new HumanoidModel<SorcererNpcEntity>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
   }

   public ResourceLocation getTextureLocation(SorcererNpcEntity entity) {
      NpcProfile profile = entity.profile();
      ResourceLocation texture = TEXTURES[profile.ordinal()];
      if (texture == null) {
         texture = ModMain.id(profile.texture());
         TEXTURES[profile.ordinal()] = texture;
      }

      return texture;
   }
}
