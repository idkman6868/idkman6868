package com.curseddomain.cullinggame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.entity.LivingEntity;

/** A health bar shown to players near a boss. Bars of bosses that stop ticking (unloaded) are swept away. */
public final class EntityBossBar {
   private static final List<EntityBossBar> LIVE = new ArrayList<>();
   private static final double RANGE_SQR = 2304.0;
   private final ServerBossEvent event;
   private final Map<UUID, ServerPlayer> viewers = new HashMap<>();
   private long lastSeen;
   private boolean closed;

   public EntityBossBar(Component name, BossBarColor color) {
      this.event = new ServerBossEvent(name, color, BossBarOverlay.PROGRESS);
      LIVE.add(this);
   }

   public void tick(LivingEntity owner) {
      if (!this.closed && owner.level() instanceof ServerLevel level) {
         this.lastSeen = level.getGameTime();
         this.event.setProgress(Math.max(0.0F, Math.min(1.0F, owner.getHealth() / owner.getMaxHealth())));
         if (owner.tickCount % 10 == 0) {
            List<ServerPlayer> players = level.players();

            for (ServerPlayer player : players) {
               boolean near = player.distanceToSqr(owner.position()) < RANGE_SQR && player.isAlive();
               if (near && !this.viewers.containsKey(player.getUUID())) {
                  this.viewers.put(player.getUUID(), player);
                  this.event.addPlayer(player);
               } else if (!near && this.viewers.containsKey(player.getUUID())) {
                  this.viewers.remove(player.getUUID());
                  this.event.removePlayer(player);
               }
            }

            Iterator<Map.Entry<UUID, ServerPlayer>> it = this.viewers.entrySet().iterator();

            while (it.hasNext()) {
               ServerPlayer viewer = it.next().getValue();
               if (viewer.isRemoved() || viewer.level() != level) {
                  this.event.removePlayer(viewer);
                  it.remove();
               }
            }
         }
      }
   }

   public void setName(Component name) {
      this.event.setName(name);
   }

   public void close() {
      this.closed = true;
      this.event.removeAllPlayers();
      this.viewers.clear();
   }

   /** Called every second: hides bars whose boss is no longer ticking. */
   public static void sweep(long now) {
      Iterator<EntityBossBar> it = LIVE.iterator();

      while (it.hasNext()) {
         EntityBossBar bar = it.next();
         if (bar.closed || now - bar.lastSeen > 40L || now < bar.lastSeen) {
            bar.close();
            it.remove();
         }
      }
   }

   public static void clearAll() {
      for (EntityBossBar bar : LIVE) {
         bar.close();
      }

      LIVE.clear();
   }
}
