package com.curseddomain.client.culling;

import com.curseddomain.incarnation.IncarnationPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Players incarnated by Kenjaku look like the villager whose body they took. */
public final class IncarnationRenderer {
   private static final Map<Integer, String> PROFESSIONS = new HashMap<>();
   private static final Map<Integer, Villager> BODIES = new HashMap<>();

   private IncarnationRenderer() {
   }

   public static void update(IncarnationPayload payload) {
      PROFESSIONS.put(payload.entityId(), payload.profession());
      BODIES.remove(payload.entityId());
   }

   public static boolean incarnated(Player player) {
      return PROFESSIONS.containsKey(player.getId());
   }

   @Nullable
   private static Villager body(Player player) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      if (level == null) {
         return null;
      } else {
         Villager body = BODIES.get(player.getId());
         if (body == null || body.level() != level) {
            body = new Villager(EntityType.VILLAGER, level);
            ResourceLocation id = ResourceLocation.tryParse(PROFESSIONS.get(player.getId()));
            if (id != null) {
               VillagerProfession profession = (VillagerProfession)BuiltInRegistries.VILLAGER_PROFESSION.get(id);
               body.setVillagerData(body.getVillagerData().setProfession(profession));
            }

            BODIES.put(player.getId(), body);
         }

         return body;
      }
   }

   /** Draws the villager body in place of the player. Returns false if the player should render normally. */
   public static boolean render(Player player, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
      if (!incarnated(player)) {
         return false;
      } else {
         Villager body = body(player);
         if (body == null) {
            return false;
         } else {
            body.yBodyRot = player.yBodyRot;
            body.yBodyRotO = player.yBodyRotO;
            body.yHeadRot = player.yHeadRot;
            body.yHeadRotO = player.yHeadRotO;
            body.setXRot(player.getXRot());
            body.xRotO = player.xRotO;
            body.setYRot(player.getYRot());
            body.yRotO = player.yRotO;
            body.tickCount = player.tickCount;
            body.setShiftKeyDown(player.isShiftKeyDown());
            Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body).render(body, player.getYRot(), partialTick, poseStack, buffers, light);
            return true;
         }
      }
   }

   /** Every client tick: move the bodies' legs in step with their players. */
   public static void tick() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         PROFESSIONS.clear();
         BODIES.clear();
      } else {
         Iterator<Map.Entry<Integer, Villager>> it = BODIES.entrySet().iterator();

         while (it.hasNext()) {
            Map.Entry<Integer, Villager> entry = it.next();
            Entity entity = mc.level.getEntity(entry.getKey());
            if (entity instanceof Player player) {
               double dx = player.getX() - player.xo;
               double dz = player.getZ() - player.zo;
               float speed = (float)Math.min(1.0, Math.sqrt(dx * dx + dz * dz) * 4.0);
               entry.getValue().walkAnimation.update(speed, 0.4F);
            }
         }
      }
   }
}
