package com.steelballrun;

import com.mojang.logging.LogUtils;
import com.steelballrun.config.SbrClientConfig;
import com.steelballrun.config.SbrConfig;
import com.steelballrun.datagen.SbrLanguageProvider;
import com.steelballrun.network.SbrNetwork;
import com.steelballrun.registry.SbrAttachments;
import com.steelballrun.registry.SbrEntities;
import com.steelballrun.registry.SbrItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import org.slf4j.Logger;

@Mod(SteelBallRun.MODID)
public final class SteelBallRun {
   public static final String MODID = "steel_ball_run";
   public static final Logger LOGGER = LogUtils.getLogger();

   public SteelBallRun(IEventBus modBus, ModContainer container) {
      SbrAttachments.ATTACHMENT_TYPES.register(modBus);
      SbrItems.ITEMS.register(modBus);
      SbrItems.TABS.register(modBus);
      SbrEntities.ENTITY_TYPES.register(modBus);
      modBus.addListener(SbrEntities::registerAttributes);
      modBus.addListener(SbrNetwork::register);
      modBus.addListener(SbrLanguageProvider::gather);
      container.registerConfig(Type.SERVER, SbrConfig.SPEC);
      container.registerConfig(Type.CLIENT, SbrClientConfig.SPEC);
   }

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath(MODID, path);
   }
}
