package ic2.core.platform.registries;

import ic2.core.item.wearable.material.IC2ArmorMaterial;
import ic2.core.item.wearable.material.IC2SpecialArmorMaterial;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class IC2Materials {
   public static ArmorMaterial BRONZE_ARMOR = new IC2ArmorMaterial(
      "IC2_BRONZE", 15, new int[]{3, 6, 8, 3}, 9, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(new ItemLike[]{IC2Items.INGOT_BRONZE}), 0.0F
   );
   public static ArmorMaterial COMPOSITE_ARMOR = new IC2ArmorMaterial(
      "IC2_ALLOY", 50, new int[]{4, 7, 9, 4}, 12, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(new ItemLike[]{IC2Items.INGOT_ADVANCED_ALLOY}), 4.0F
   );
   public static ArmorMaterial HAZMAT_ARMOR = new IC2SpecialArmorMaterial(ArmorMaterials.DIAMOND)
      .setName("IC2_HAZMAT")
      .setDurability(64)
      .setRepairMaterial(() -> Ingredient.of(new ItemLike[]{IC2Items.RUBBER}));
   public static ArmorMaterial ELECTRIC_ARMOR = new IC2SpecialArmorMaterial(ArmorMaterials.DIAMOND)
      .setName("IC2_ELECTRIC")
      .setDurability(0)
      .setRepairMaterial(() -> Ingredient.EMPTY);
   public static ArmorMaterial ADVANCED_ARMOR = new IC2SpecialArmorMaterial(ArmorMaterials.DIAMOND).setName("IC2_ADVANCED").setDurability(0);
   public static final List<WoodType> ALL_TYPES = ObjectLists.synchronize(CollectionUtils.createList());
   public static final WoodType SIGN_RUBBER_WOOD = create("ic2:rubberwood_sign");
   public static final WoodType SIGN_COPPER = create("ic2:copper_sign");
   public static final WoodType SIGN_ALUMINIUM = create("ic2:aluminium_sign");
   public static final WoodType SIGN_TIN = create("ic2:tin_sign");
   public static final WoodType SIGN_SILVER = create("ic2:silver_sign");
   public static final WoodType SIGN_BRONZE = create("ic2:bronze_sign");
   public static final WoodType SIGN_REFINED_IRON = create("ic2:refined_iron_sign");

   public static WoodType create(String name) {
      BlockSetType setType = BlockSetType.register(new BlockSetType(name));
      WoodType type = WoodType.register(new WoodType(name, setType));
      ALL_TYPES.add(type);
      return type;
   }
}
