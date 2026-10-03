package com.curseddomain.shikigami;

import com.curseddomain.sorcerer.SorcererManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public final class TenShadowsShikigami {
   public static final String LOST_WHITE = "lost:divine_dog_white";
   public static final String LOST_BLACK = "lost:divine_dog_black";
   public static final String TOTALITY = "shikigami:totality";
   public static final String LOST_TOTALITY = "lost:divine_dog_totality";

   private TenShadowsShikigami() {
   }

   static void onDogDied(DivineDogEntity dog) {
      if (dog.summoner() instanceof ServerPlayer owner) {
         String flag = switch (dog.variant()) {
            case 0 -> "lost:divine_dog_white";
            case 1 -> "lost:divine_dog_black";
            default -> "lost:divine_dog_totality";
         };
         SorcererManager.unlock(owner, flag);
         owner.sendSystemMessage(Component.translatable("shikigami.cursed_domain.lost", new Object[]{dog.getDisplayName()}).withStyle(ChatFormatting.DARK_GRAY));
         if (dog.variant() != 2 && !SorcererManager.get(owner).isUnlocked("shikigami:totality")) {
            SorcererManager.unlock(owner, "shikigami:totality");
            owner.sendSystemMessage(
               Component.translatable("shikigami.cursed_domain.totality").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC})
            );
         }
      }
   }

   static void onRitualWon(ShikigamiEntity shikigami, DamageSource source, String flag) {
      if (shikigami.summoner() instanceof ServerPlayer player && source.getEntity() == player) {
         SorcererManager.unlock(player, flag);
         player.sendSystemMessage(
            Component.translatable("shikigami.cursed_domain.tamed", new Object[]{shikigami.getDisplayName()}).withStyle(ChatFormatting.GOLD)
         );
      }
   }
}
