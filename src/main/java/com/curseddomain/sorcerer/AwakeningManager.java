package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.config.CombatConfig;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.network.payload.StoryPayloads;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.registry.ModItems;
import com.curseddomain.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AwakeningManager {
   public static final String REINFORCEMENT = "universal:reinforcement";
   private static final int SCOUT_DELAY_TICKS = 80;

   private AwakeningManager() {
   }

   public static StoryData story(Player player) {
      return (StoryData)player.getData(ModAttachments.STORY);
   }

   public static boolean awaken(ServerPlayer player, AwakeningCause cause) {
      if (SorcererManager.get(player).status() != SorcererStatus.NON_SORCERER) {
         return false;
      } else {
         SorcererManager.setStatus(player, SorcererStatus.AWAKENED);
         SorcererManager.unlock(player, "universal:reinforcement");
         if (!(Boolean)CombatConfig.TECHNIQUES_NEED_ENROLLMENT.get()) {
            SorcererManager.setTechniqueRevealed(player, true);
         }

         EnergyManager.setCurrent(player, EnergyManager.get(player).max());
         StoryData story = story(player);
         story.curseHits = 0;
         story.scoutMessageAt = player.serverLevel().getGameTime() + 80L;
         giveLetter(player);
         player.serverLevel()
            .sendParticles((SimpleParticleType)ModParticles.CURSED_WISP.get(), player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.6, 0.9, 0.6, 0.02);
         PacketDistributor.sendToPlayer(player, new StoryPayloads.Awakening(cause), new CustomPacketPayload[0]);
         ModMain.LOGGER.info("[story] {} awakened ({})", player.getScoreboardName(), cause.getSerializedName());
         return true;
      }
   }

   public static void giveLetter(ServerPlayer player) {
      boolean hasLetter = player.getInventory().items.stream().anyMatch(s -> s.is((Item)ModItems.RECRUITMENT_LETTER.get()));
      if (!hasLetter) {
         ItemStack letter = new ItemStack((ItemLike)ModItems.RECRUITMENT_LETTER.get());
         if (!player.getInventory().add(letter)) {
            player.drop(letter, false);
         }
      }
   }

   static void sendScoutMessage(ServerPlayer player) {
      for (int i = 1; i <= 3; i++) {
         player.sendSystemMessage(
            Component.translatable("story.cursed_domain.scout.name")
               .withStyle(ChatFormatting.DARK_AQUA)
               .append(Component.translatable("story.cursed_domain.scout." + i).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}))
         );
      }
   }
}
