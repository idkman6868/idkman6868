package com.curseddomain.incarnation;

import com.curseddomain.ModMain;
import com.curseddomain.cullinggame.CullingGame;
import com.curseddomain.cullinggame.Kogane;
import com.curseddomain.cullinggame.npc.SorcererNpcEntity;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.registry.ModParticles;
import com.curseddomain.registry.ModTechniques;
import com.curseddomain.sorcerer.Grade;
import com.curseddomain.sorcerer.InnateTrait;
import com.curseddomain.sorcerer.SorcererData;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.technique.Technique;
import com.curseddomain.vfx.ScreenFxPayload;
import com.curseddomain.vfx.Vfx;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * A Shrine user can ask Kenjaku to make them into twenty fingers. A few seconds later they wake up incarnated in a
 * random villager's body, as the King of Curses.
 */
@EventBusSubscriber(modid = ModMain.MODID)
public final class Incarnation {
   private static final String KENJAKU = "entity.cursed_domain.sorcerer_npc.kenjaku";
   private static final String UNLOCK = "incarnation:";
   private static final Map<UUID, UUID> OFFERS = new HashMap<>();

   private Incarnation() {
   }

   private static void say(ServerPlayer player, String key) {
      Kogane.speech(player, KENJAKU, Component.translatable("incarnation.cursed_domain." + key), ChatFormatting.DARK_RED);
   }

   public static boolean hasShrine(ServerPlayer player) {
      SorcererData data = SorcererManager.get(player);
      Object shrine = ModTechniques.SHRINE.get();
      Optional<ResourceLocation> technique = data.techniqueId();
      return data.status().awakened() && shrine instanceof Technique t && technique.isPresent() && technique.get().equals(t.id());
   }

   /** The villager profession this player is incarnated as, or null if they are not. */
   @Nullable
   public static String profession(ServerPlayer player) {
      for (String flag : SorcererManager.get(player).unlocks()) {
         if (flag.startsWith(UNLOCK)) {
            return flag.substring(UNLOCK.length());
         }
      }

      return null;
   }

   // ------------------------------------------------------------------------------------------------ Kenjaku's offer

   public static void kenjakuInteract(ServerPlayer player, SorcererNpcEntity kenjaku) {
      if (profession(player) != null) {
         say(player, "already");
      } else if (IncarnationData.get(player.server).pending.containsKey(player.getUUID())) {
         say(player, "wait");
      } else if (!hasShrine(player)) {
         say(player, "idle." + (1 + player.getRandom().nextInt(3)));
      } else {
         OFFERS.put(player.getUUID(), kenjaku.getUUID());
         say(player, "offer.1");
         say(player, "offer.2");
         MutableComponent line = Component.literal("  ");
         line.append(button("accept", ChatFormatting.DARK_RED));
         line.append(Component.literal("   "));
         line.append(button("refuse", ChatFormatting.GRAY));
         player.sendSystemMessage(line);
      }
   }

   private static Component button(String what, ChatFormatting color) {
      return Component.translatable("incarnation.cursed_domain.button." + what)
         .withStyle(
            style -> style.withColor(color)
               .withBold(true)
               .withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kenjaku " + what))
               .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("incarnation.cursed_domain.button." + what + ".hover")))
         );
   }

   @Nullable
   private static SorcererNpcEntity offeringKenjaku(ServerPlayer player) {
      UUID id = OFFERS.get(player.getUUID());
      Entity entity = id == null ? null : player.serverLevel().getEntity(id);
      return entity instanceof SorcererNpcEntity npc && npc.isAlive() && npc.distanceTo(player) < 12.0F ? npc : null;
   }

   private static int accept(ServerPlayer player) {
      SorcererNpcEntity kenjaku = offeringKenjaku(player);
      if (kenjaku == null || !hasShrine(player) || profession(player) != null) {
         player.sendSystemMessage(Component.translatable("incarnation.cursed_domain.no_offer").withStyle(ChatFormatting.GRAY));
         return 0;
      } else {
         OFFERS.remove(player.getUUID());
         ServerLevel level = player.serverLevel();
         say(player, "begin");
         Vec3 at = player.position().add(0.0, 1.0, 0.0);
         Vfx.darkBurst(level, at, -14023162, 3.0F, 30);
         Vfx.implode(level, at, -50646, 2.5F, 20);
         level.sendParticles((SimpleParticleType)ModParticles.CURSED_WISP.get(), at.x, at.y, at.z, 160, 1.2, 1.2, 1.2, 0.15);
         level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8F, 1.4F);
         Kogane.title(
            player,
            Component.translatable("incarnation.cursed_domain.fingers.title").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}),
            Component.translatable("incarnation.cursed_domain.fingers.subtitle"),
            120
         );
         Vfx.screen(player, ScreenFxPayload.Kind.TINT, -14023162, 0.7F, 120);
         long wake = level.getGameTime() + 100L + player.getRandom().nextInt(101);
         IncarnationData data = IncarnationData.get(player.server);
         data.pending.put(player.getUUID(), new IncarnationData.Pending(wake, player.gameMode.getGameModeForPlayer().getName()));
         data.setDirty();
         player.setGameMode(GameType.SPECTATOR);
         ModMain.LOGGER.info("[incarnation] {} became twenty fingers", player.getScoreboardName());
         return 1;
      }
   }

   private static int refuse(ServerPlayer player) {
      if (OFFERS.remove(player.getUUID()) != null) {
         say(player, "refused");
      }

      return 1;
   }

   // ------------------------------------------------------------------------------------------------ waking up

   private static void wake(ServerPlayer player, IncarnationData.Pending pending) {
      MinecraftServer server = player.server;
      ServerLevel overworld = server.overworld();
      Villager host = pickVillager(overworld, player);
      String profession = "minecraft:none";
      Vec3 at;
      float yaw = player.getYRot();
      if (host != null) {
         ResourceLocation key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(host.getVillagerData().getProfession());
         profession = key.toString();
         at = host.position();
         yaw = host.getYRot();
         Vfx.burst(overworld, at.add(0.0, 1.0, 0.0), -50646, 1.5F, 16);
         host.discard();
      } else {
         at = CullingGame.surface(overworld, player.getX(), player.getZ());
      }

      player.teleportTo(overworld, at.x, at.y, at.z, yaw, 0.0F);
      GameType mode = GameType.byName(pending.previousMode());
      player.setGameMode(mode == GameType.SPECTATOR ? GameType.SURVIVAL : mode);
      SorcererManager.unlock(player, UNLOCK + profession);
      SorcererManager.addTrait(player, InnateTrait.INCARNATED_SORCERER);
      SorcererData data = SorcererManager.get(player);
      if (!data.status().enrolled()) {
         SorcererManager.setStatus(player, SorcererStatus.CURSE_USER);
      }

      SorcererManager.setGrade(player, Grade.SPECIAL_GRADE);
      SorcererManager.unlock(player, "domain");
      EnergyManager.setCurrent(player, EnergyManager.get(player).max());
      player.setHealth(player.getMaxHealth());
      player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
      Vfx.pillar(overworld, at, -50646, 6.0F, 30);
      overworld.playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 0.6F);
      Kogane.title(
         player,
         Component.translatable("incarnation.cursed_domain.woke.title").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}),
         Component.translatable("incarnation.cursed_domain.woke.subtitle"),
         100
      );
      player.sendSystemMessage(Component.translatable("incarnation.cursed_domain.woke").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
      broadcast(player);
      ModMain.LOGGER.info("[incarnation] {} incarnated as a villager ({})", player.getScoreboardName(), profession);
   }

   /** A random villager anywhere in the loaded overworld; if there is none, one far away is created for the purpose. */
   @Nullable
   private static Villager pickVillager(ServerLevel level, ServerPlayer player) {
      List<Villager> villagers = new ArrayList<>();

      for (Entity entity : level.getAllEntities()) {
         if (entity instanceof Villager villager && villager.isAlive() && !villager.isBaby()) {
            villagers.add(villager);
         }
      }

      if (!villagers.isEmpty()) {
         return villagers.get(player.getRandom().nextInt(villagers.size()));
      } else {
         double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
         double distance = 300.0 + player.getRandom().nextDouble() * 900.0;
         Vec3 at = CullingGame.surface(level, player.getX() + Math.cos(angle) * distance, player.getZ() + Math.sin(angle) * distance);
         Villager villager = (Villager)EntityType.VILLAGER.create(level);
         if (villager != null) {
            villager.moveTo(at.x, at.y, at.z, player.getRandom().nextFloat() * 360.0F, 0.0F);
            villager.finalizeSpawn(level, level.getCurrentDifficultyAt(villager.blockPosition()), MobSpawnType.EVENT, null);
            level.addFreshEntity(villager);
         }

         return villager;
      }
   }

   private static void broadcast(ServerPlayer player) {
      String profession = profession(player);
      if (profession != null) {
         PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new IncarnationPayload(player.getId(), profession), new CustomPacketPayload[0]);
      }
   }

   // ------------------------------------------------------------------------------------------------ events

   @SubscribeEvent
   public static void onServerTick(Post event) {
      MinecraftServer server = event.getServer();
      if (server.getTickCount() % 10 == 0) {
         IncarnationData data = IncarnationData.get(server);
         if (!data.pending.isEmpty()) {
            long now = server.overworld().getGameTime();
            Iterator<Map.Entry<UUID, IncarnationData.Pending>> it = data.pending.entrySet().iterator();
            List<Runnable> wakes = new ArrayList<>();

            while (it.hasNext()) {
               Map.Entry<UUID, IncarnationData.Pending> entry = it.next();
               ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
               if (player != null && now >= entry.getValue().wakeAt()) {
                  IncarnationData.Pending pending = entry.getValue();
                  it.remove();
                  data.setDirty();
                  wakes.add(() -> wake(player, pending));
               }
            }

            wakes.forEach(Runnable::run);
         }
      }
   }

   @SubscribeEvent
   public static void onStartTracking(StartTracking event) {
      if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer watcher) {
         String profession = profession(target);
         if (profession != null) {
            PacketDistributor.sendToPlayer(watcher, new IncarnationPayload(target.getId(), profession), new CustomPacketPayload[0]);
         }
      }
   }

   @SubscribeEvent
   public static void onLogin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         broadcast(player);
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         broadcast(player);
      }
   }

   @SubscribeEvent
   public static void onDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         broadcast(player);
      }
   }

   @SubscribeEvent
   public static void onStopped(ServerStoppedEvent event) {
      OFFERS.clear();
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("kenjaku");
      root.then(Commands.literal("accept").executes(ctx -> accept(player(ctx))));
      root.then(Commands.literal("refuse").executes(ctx -> refuse(player(ctx))));
      event.getDispatcher().register(root);
   }

   private static ServerPlayer player(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
      return ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
   }
}
