package com.curseddomain.client;

import com.curseddomain.ModMain;
import com.curseddomain.client.hud.AbilityBarHud;
import com.curseddomain.client.hud.ClashHud;
import com.curseddomain.client.hud.CursedEnergyHud;
import com.curseddomain.client.input.ModKeys;
import com.curseddomain.client.particle.CursedWispParticle;
import com.curseddomain.client.particle.EnergyParticle;
import com.curseddomain.client.particle.TalismanParticle;
import com.curseddomain.client.render.DomainEnvironment;
import com.curseddomain.client.render.entity.DomainBarrierRenderer;
import com.curseddomain.client.render.entity.Grade4CurseModel;
import com.curseddomain.client.render.entity.Grade4CurseRenderer;
import com.curseddomain.client.render.entity.ShikigamiModels;
import com.curseddomain.client.render.entity.ShikigamiRenderers;
import com.curseddomain.client.render.entity.TechniqueProjectileRenderer;
import com.curseddomain.client.story.AwakeningOverlay;
import com.curseddomain.client.story.HintTrail;
import com.curseddomain.client.story.LetterScreen;
import com.curseddomain.client.vfx.ScreenEffects;
import com.curseddomain.client.vfx.VfxManager;
import com.curseddomain.domain.DomainPayloads;
import com.curseddomain.network.ClientBridge;
import com.curseddomain.network.payload.StoryPayloads;
import com.curseddomain.registry.ModEntities;
import com.curseddomain.registry.ModParticles;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.VfxPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@Mod(
   value = "cursed_domain",
   dist = {Dist.CLIENT}
)
public final class ModClient {
   public ModClient(IEventBus modBus, ModContainer container) {
      container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
      modBus.addListener(ModClient::registerGuiLayers);
      modBus.addListener(ModClient::registerRenderers);
      modBus.addListener(ModClient::registerLayers);
      modBus.addListener(ModClient::registerParticles);
      modBus.addListener(ModKeys::register);
      registerPayloadReactions();
   }

   private static void registerPayloadReactions() {
      ClientBridge.register(StoryPayloads.Awakening.class, p -> AwakeningOverlay.start(p.cause()));
      ClientBridge.register(StoryPayloads.HintTrail.class, p -> HintTrail.start(p.yaw(), p.distance()));
      ClientBridge.register(StoryPayloads.OpenLetter.class, p -> Minecraft.getInstance().setScreen(new LetterScreen(p.firstRead())));
      ClientBridge.register(VfxPayload.class, VfxManager::add);
      ClientBridge.register(ScreenFxPayload.class, ScreenEffects::add);
      ClientBridge.register(DomainPayloads.Clash.class, p -> ClashHud.update(p.share(), p.remainingTicks()));
   }

   private static void registerGuiLayers(RegisterGuiLayersEvent event) {
      event.registerAbove(VanillaGuiLayers.AIR_LEVEL, ModMain.id("cursed_energy"), new CursedEnergyHud());
      event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, ModMain.id("ability_bar"), new AbilityBarHud());
      event.registerBelow(VanillaGuiLayers.CROSSHAIR, ModMain.id("domain_environment"), DomainEnvironment.overlay());
      event.registerAboveAll(ModMain.id("screen_effects"), new ScreenEffects());
      event.registerAboveAll(ModMain.id("domain_clash"), new ClashHud());
      event.registerAboveAll(ModMain.id("awakening"), new AwakeningOverlay());
   }

   private static void registerRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)ModEntities.GRADE_4_CURSE.get(), Grade4CurseRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.TECHNIQUE_PROJECTILE.get(), TechniqueProjectileRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.DOMAIN_BARRIER.get(), DomainBarrierRenderer::new);
      event.registerEntityRenderer((EntityType)ModEntities.DIVINE_DOG.get(), ShikigamiRenderers.Dog::new);
      event.registerEntityRenderer((EntityType)ModEntities.NUE.get(), ShikigamiRenderers.Nue::new);
      event.registerEntityRenderer((EntityType)ModEntities.TRANSFIGURED_HUMAN.get(), ShikigamiRenderers.Human::new);
   }

   private static void registerLayers(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(Grade4CurseModel.LAYER, Grade4CurseModel::createLayer);
      event.registerLayerDefinition(ShikigamiModels.DIVINE_DOG, ShikigamiModels::dogLayer);
      event.registerLayerDefinition(ShikigamiModels.NUE, ShikigamiModels::nueLayer);
      event.registerLayerDefinition(ShikigamiModels.TRANSFIGURED_HUMAN, ShikigamiModels::humanLayer);
   }

   private static void registerParticles(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)ModParticles.TALISMAN.get(), TalismanParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ModParticles.CURSED_WISP.get(), CursedWispParticle.Provider::new);
      event.registerSpriteSet((ParticleType)ModParticles.ENERGY.get(), EnergyParticle.Provider::new);
   }
}
