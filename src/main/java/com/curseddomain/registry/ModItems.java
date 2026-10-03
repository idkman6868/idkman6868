package com.curseddomain.registry;

import com.curseddomain.item.ConstructedBladeItem;
import com.curseddomain.item.RecruitmentLetterItem;
import com.curseddomain.item.ThroatMedicineItem;
import com.curseddomain.item.cursedobject.CursedObjectItem;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class ModItems {
   public static final Items ITEMS = DeferredRegister.createItems("cursed_domain");
   public static final Set<String> CUSTOM_MODELS = new HashSet<>();
   public static final DeferredItem<RecruitmentLetterItem> RECRUITMENT_LETTER = ITEMS.register(
      "recruitment_letter", () -> new RecruitmentLetterItem(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<CursedObjectItem> CURSED_OBJECT = ITEMS.register(
      "cursed_object", () -> new CursedObjectItem(new Properties().stacksTo(16).rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<ThroatMedicineItem> THROAT_MEDICINE = ITEMS.register(
      "throat_medicine", () -> new ThroatMedicineItem(new Properties().stacksTo(16))
   );
   public static final DeferredItem<ConstructedBladeItem> CONSTRUCTED_BLADE = ITEMS.register(
      "constructed_blade", () -> new ConstructedBladeItem(Tiers.IRON, new Properties().attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)))
   );
   public static final DeferredItem<ConstructedBladeItem> CONSTRUCTED_CURSED_TOOL = ITEMS.register(
      "constructed_cursed_tool",
      () -> new ConstructedBladeItem(
         Tiers.NETHERITE, new Properties().rarity(Rarity.EPIC).fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 5, -2.2F))
      )
   );
   public static final DeferredItem<DeferredSpawnEggItem> GRADE_4_CURSE_SPAWN_EGG = ITEMS.register(
      "grade_4_curse_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.GRADE_4_CURSE, 2758712, 10170320, new Properties())
   );

   private ModItems() {
   }
}
