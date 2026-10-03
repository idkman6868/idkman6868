package com.curseddomain.item;

import com.curseddomain.config.ServerConfig;
import com.curseddomain.network.payload.StoryPayloads;
import com.curseddomain.sorcerer.AwakeningManager;
import com.curseddomain.sorcerer.SorcererManager;
import com.curseddomain.sorcerer.SorcererStatus;
import com.curseddomain.sorcerer.StoryData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class RecruitmentLetterItem extends Item {
   public RecruitmentLetterItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
         SorcererStatus status = SorcererManager.get(serverPlayer).status();
         if (!status.awakened()) {
            serverPlayer.displayClientMessage(
               Component.translatable("item.cursed_domain.recruitment_letter.blank")
                  .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
               true
            );
            return InteractionResultHolder.success(stack);
         } else if (status.enrolled()) {
            serverPlayer.displayClientMessage(
               Component.translatable("item.cursed_domain.recruitment_letter.answered")
                  .withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
               true
            );
            return InteractionResultHolder.success(stack);
         } else {
            StoryData story = AwakeningManager.story(serverPlayer);
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (story.letterRead && !player.isShiftKeyDown()) {
               LetterHints.show(serverPlayer);
               player.getCooldowns().addCooldown(this, (int)Math.round((Double)ServerConfig.LETTER_COOLDOWN_SECONDS.get() * 20.0));
               return InteractionResultHolder.success(stack);
            } else {
               boolean first = !story.letterRead;
               story.letterRead = true;
               if (status == SorcererStatus.AWAKENED) {
                  SorcererManager.setStatus(serverPlayer, SorcererStatus.APPLICANT);
               }

               PacketDistributor.sendToPlayer(serverPlayer, new StoryPayloads.OpenLetter(first), new CustomPacketPayload[0]);
               return InteractionResultHolder.success(stack);
            }
         }
      } else {
         return InteractionResultHolder.success(stack);
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(
         Component.translatable("item.cursed_domain.recruitment_letter.desc").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
      );
      tooltip.add(Component.translatable("item.cursed_domain.recruitment_letter.usage").withStyle(ChatFormatting.DARK_GRAY));
   }
}
