package com.curseddomain.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;

public final class ModRenderTypes {
   public static final RenderType GLOW = RenderType.create(
      "cursed_domain_glow",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.QUADS,
      4096,
      false,
      true,
      CompositeState.builder()
         .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
         .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
         .setWriteMaskState(RenderStateShard.COLOR_WRITE)
         .setCullState(RenderStateShard.NO_CULL)
         .createCompositeState(false)
   );
   public static final RenderType SHADE = RenderType.create(
      "cursed_domain_shade",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.QUADS,
      4096,
      false,
      true,
      CompositeState.builder()
         .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .setWriteMaskState(RenderStateShard.COLOR_WRITE)
         .setCullState(RenderStateShard.NO_CULL)
         .createCompositeState(false)
   );

   private ModRenderTypes() {
   }
}
