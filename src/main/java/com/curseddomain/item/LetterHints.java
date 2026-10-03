package com.curseddomain.item;

import com.curseddomain.ModMain;
import com.curseddomain.config.ServerConfig;
import com.curseddomain.network.payload.StoryPayloads;
import com.curseddomain.registry.ModItems;
import com.curseddomain.sorcerer.AwakeningManager;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.sorcerer.StoryData;
import com.curseddomain.world.SchoolLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "cursed_domain"
)
public final class LetterHints {
   private static final String[] COMPASS = new String[]{"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};

   private LetterHints() {
   }

   public static float yawTowards(double fromX, double fromZ, double toX, double toZ) {
      return (float)Math.toDegrees(Math.atan2(-(toX - fromX), toZ - fromZ));
   }

   public static Component compass(float yaw) {
      int index = Math.floorMod(Math.round(yaw / 45.0F), 8);
      return Component.translatable(ModMain.key("direction", COMPASS[index]));
   }

   static void show(ServerPlayer player) {
      BlockPos school = SchoolLocation.get().orElse(null);
      if (school != null && player.level().dimension() == Level.OVERWORLD) {
         StoryData story = AwakeningManager.story(player);
         long now = player.serverLevel().getGameTime();
         long refresh = Math.round((Double)ServerConfig.LETTER_HINT_REFRESH_MINUTES.get() * 1200.0);
         if (now - story.hintRolledAt >= refresh) {
            float jitter = ((Double)ServerConfig.LETTER_HINT_JITTER_DEGREES.get()).floatValue();
            story.hintOffsetDegrees = (player.getRandom().nextFloat() * 2.0F - 1.0F) * jitter;
            story.hintRolledAt = now;
         }

         double distance = Math.sqrt(player.distanceToSqr(school.getX(), player.getY(), school.getZ()));
         float yaw = yawTowards(player.getX(), player.getZ(), school.getX(), school.getZ()) + story.hintOffsetDegrees;
         PacketDistributor.sendToPlayer(player, new StoryPayloads.HintTrail(yaw, (float)distance), new CustomPacketPayload[0]);
         String distanceKey = distance < 96.0 ? "very_close" : (distance < 400.0 ? "near" : (distance < 1200.0 ? "far" : "very_far"));
         player.displayClientMessage(
            Component.translatable(
                  "item.cursed_domain.recruitment_letter.hint",
                  new Object[]{compass(yaw), Component.translatable("item.cursed_domain.recruitment_letter.distance." + distanceKey)}
               )
               .withStyle(ChatFormatting.LIGHT_PURPLE),
            true
         );
      } else {
         player.displayClientMessage(Component.translatable("item.cursed_domain.recruitment_letter.still").withStyle(ChatFormatting.GRAY), true);
      }
   }

   @SubscribeEvent
   public static void onInteractVillager(EntityInteract event) {
      Entity target = event.getTarget();
      if (target instanceof AbstractVillager && event.getEntity().isShiftKeyDown() && event.getItemStack().is((Item)ModItems.RECRUITMENT_LETTER.get())) {
         SorcererStatus status = SorcererManager.get(event.getEntity()).status();
         if (status.awakened() && !status.enrolled()) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (event.getEntity() instanceof ServerPlayer player) {
               BlockPos school = SchoolLocation.get().orElse(null);
               if (school == null) {
                  return;
               }

               float yaw = yawTowards(target.getX(), target.getZ(), school.getX(), school.getZ());
               int variant = 1 + player.getRandom().nextInt(3);
               player.sendSystemMessage(
                  target.getDisplayName()
                     .copy()
                     .withStyle(ChatFormatting.GREEN)
                     .append(Component.literal(": ").withStyle(ChatFormatting.GREEN))
                     .append(Component.translatable("story.cursed_domain.rumor." + variant, new Object[]{compass(yaw)}).withStyle(ChatFormatting.WHITE))
               );
               target.playSound(SoundEvents.VILLAGER_AMBIENT, 1.0F, 1.0F);
            }
         }
      }
   }
}
