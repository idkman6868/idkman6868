package com.curseddomain;

import com.curseddomain.config.ClientConfig;
import com.curseddomain.config.CommonConfig;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.network.ModNetwork;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.registry.ModBlocks;
import com.curseddomain.registry.ModCreativeTabs;
import com.curseddomain.registry.ModEffects;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModItems;
import com.curseddomain.registry.ModParticles;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.technique.TechniqueRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import org.slf4j.Logger;

@Mod("cursed_domain")
public final class ModMain {
   public static final String MODID = "cursed_domain";
   public static final Logger LOGGER = LogUtils.getLogger();

   public ModMain(IEventBus modBus, ModContainer container) {
      modBus.addListener(TechniqueRegistry::onNewRegistry);
      ModTechniques.TECHNIQUES.register(modBus);
      ModAttachments.ATTACHMENT_TYPES.register(modBus);
      ModBlocks.BLOCKS.register(modBus);
      ModItems.ITEMS.register(modBus);
      ModEntities.ENTITY_TYPES.register(modBus);
      ModParticles.PARTICLE_TYPES.register(modBus);
      ModCreativeTabs.TABS.register(modBus);
      ModEffects.EFFECTS.register(modBus);
      modBus.addListener(ModNetwork::register);
      container.registerConfig(Type.COMMON, CommonConfig.SPEC);
      container.registerConfig(Type.SERVER, ServerConfig.SPEC);
      container.registerConfig(Type.CLIENT, ClientConfig.SPEC);
   }

   public static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("cursed_domain", path);
   }

   public static String key(String prefix, String name) {
      return prefix + ".cursed_domain." + name;
   }
}
