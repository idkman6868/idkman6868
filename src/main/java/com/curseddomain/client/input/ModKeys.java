package com.curseddomain.client.input;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class ModKeys {
   public static final String CATEGORY = "key.categories.cursed_domain";
   public static final KeyMapping[] SLOTS = new KeyMapping[]{key("ability_1", 90), key("ability_2", 86), key("ability_3", 66), key("ability_4", 78), key("ability_5", 77)};
   public static final KeyMapping WHEEL = key("ability_wheel", 82);
   public static final KeyMapping DOMAIN = key("domain", 71);

   private ModKeys() {
   }

   private static KeyMapping key(String name, int code) {
      return new KeyMapping("key.cursed_domain." + name, KeyConflictContext.IN_GAME, Type.KEYSYM, code, "key.categories.cursed_domain");
   }

   public static void register(RegisterKeyMappingsEvent event) {
      for (KeyMapping slot : SLOTS) {
         event.register(slot);
      }

      event.register(WHEEL);
      event.register(DOMAIN);
   }
}
