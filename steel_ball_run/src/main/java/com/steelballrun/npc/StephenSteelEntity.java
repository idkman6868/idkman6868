package com.steelballrun.npc;

import com.steelballrun.race.RaceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** The race promoter. Stands outside the Race Office at San Diego Beach and takes registrations. */
public class StephenSteelEntity extends PathfinderMob {
   public StephenSteelEntity(EntityType<? extends StephenSteelEntity> type, Level level) {
      super(type, level);
      this.setInvulnerable(true);
      this.setPersistenceRequired();
      this.setCustomName(Component.translatable("entity.steel_ball_run.stephen_steel"));
      this.setCustomNameVisible(true);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
   }

   @Override
   protected InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer sp) {
         RaceManager.talkToSteel(sp);
      }
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }
}
