package com.curseddomain.sorcerer;

import com.curseddomain.ModMain;
import com.curseddomain.config.CommonConfig;
import com.curseddomain.energy.EnergyManager;
import com.curseddomain.entity.cursedspirit.CurseVisibility;
import com.curseddomain.registry.ModAttachments;
import com.curseddomain.technique.Technique;
import com.curseddomain.technique.TechniqueRegistry;
import com.curseddomain.technique.ability.AbilityManager;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class SorcererManager {
   private SorcererManager() {
   }

   public static SorcererData get(Player player) {
      return (SorcererData)player.getData(ModAttachments.SORCERER);
   }

   public static void setStatus(ServerPlayer player, SorcererStatus status) {
      SorcererData data = get(player);
      data.setStatus(status);
      if (status.enrolled()) {
         if (data.grade() == Grade.UNGRADED) {
            data.setGrade(Grade.GRADE_4);
         }

         data.setTechniqueRevealed(true);
      } else {
         data.setGrade(Grade.UNGRADED);
         data.setGradeXp(0);
         data.setTechniqueRevealed(false);
      }

      changed(player, "status -> " + status.getSerializedName());
   }

   public static boolean setGrade(ServerPlayer player, Grade grade) {
      SorcererData data = get(player);
      if (data.status().enrolled() && grade != Grade.UNGRADED) {
         data.setGrade(grade);
         if (grade.atLeast(Grade.GRADE_1)) {
            data.unlock("domain_incomplete");
         }

         changed(player, "grade -> " + grade.getSerializedName());
         return true;
      } else {
         return false;
      }
   }

   public static void setTechnique(ServerPlayer player, @Nullable ResourceLocation id) {
      SorcererData data = get(player);
      data.technique().ifPresent(old -> old.onRemoved(player));
      AbilityManager.stopAll(player);
      data.technique().flatMap(Technique::grantedTrait).ifPresent(data::removeTrait);
      data.setTechnique(id);
      Optional<Technique> technique = id == null ? Optional.empty() : TechniqueRegistry.get(id);
      technique.flatMap(Technique::grantedTrait).ifPresent(data::addTrait);
      changed(player, "technique -> " + id);
   }

   public static void setTechniqueRevealed(ServerPlayer player, boolean revealed) {
      get(player).setTechniqueRevealed(revealed);
      changed(player, "revealed -> " + revealed);
   }

   public static void applyRoll(ServerPlayer player, ResourceLocation technique, Set<InnateTrait> traits) {
      SorcererData data = get(player);
      data.technique().ifPresent(old -> old.onRemoved(player));
      AbilityManager.stopAll(player);
      boolean incarnated = data.hasTrait(InnateTrait.INCARNATED_SORCERER);

      for (InnateTrait trait : InnateTrait.values()) {
         data.removeTrait(trait);
      }

      traits.forEach(data::addTrait);
      if (incarnated) {
         data.addTrait(InnateTrait.INCARNATED_SORCERER);
      }

      data.setTechnique(technique);
      data.setRolled(true);
      changed(player, "rolled " + technique + " " + traits);
   }

   public static boolean addTrait(ServerPlayer player, InnateTrait trait) {
      boolean added = get(player).addTrait(trait);
      if (added) {
         changed(player, "trait + " + trait.getSerializedName());
      }

      return added;
   }

   public static boolean removeTrait(ServerPlayer player, InnateTrait trait) {
      boolean removed = get(player).removeTrait(trait);
      if (removed) {
         changed(player, "trait - " + trait.getSerializedName());
      }

      return removed;
   }

   public static boolean unlock(ServerPlayer player, String flag) {
      boolean added = get(player).unlock(flag);
      if (added) {
         changed(player, "unlock " + flag);
      }

      return added;
   }

   public static boolean lock(ServerPlayer player, String flag) {
      boolean removed = get(player).lock(flag);
      if (removed) {
         changed(player, "lock " + flag);
      }

      return removed;
   }

   public static void addGradeXp(ServerPlayer player, int amount) {
      SorcererData data = get(player);
      data.setGradeXp(data.gradeXp() + amount);
      sync(player);
   }

   public static void syncAll(ServerPlayer player) {
      sync(player);
      EnergyManager.sync(player);
   }

   public static void sync(ServerPlayer player) {
      PacketDistributor.sendToPlayer(player, get(player).clientView(), new CustomPacketPayload[0]);
   }

   private static void changed(ServerPlayer player, String what) {
      if ((Boolean)CommonConfig.DEBUG_LOGGING.get()) {
         ModMain.LOGGER.info("[sorcerer] {}: {}", player.getScoreboardName(), what);
      }

      EnergyManager.recalculate(player);
      TraitEffects.apply(player);
      AbilityManager.markDirty(player);
      if (get(player).sightChanged()) {
         CurseVisibility.refresh(player);
      }

      syncAll(player);
   }
}
