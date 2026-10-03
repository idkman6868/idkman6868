package com.curseddomain.cullinggame;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;

/**
 * Second entry point of the mod: the Shibuya Incident and the Culling Game. Kept separate from {@code ModMain} so the
 * arc can be read (and switched off) as one unit.
 */
@Mod("cursed_domain")
public final class CullingGameMod {
   public CullingGameMod(IEventBus modBus, ModContainer container) {
      CullingRegistries.ENTITY_TYPES.register(modBus);
      CullingRegistries.ITEMS.register(modBus);
      modBus.addListener(CullingRegistries::attributes);
      modBus.addListener(CullingRegistries::creativeTab);
      container.registerConfig(Type.SERVER, CullingConfig.SPEC, "cursed_domain-culling-server.toml");
   }
}
