package ic2.core.item.wearable.base;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.items.armor.ICustomArmor;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Materials;
import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public abstract class IC2AdvancedArmorBase extends IC2ArmorBase implements ICustomArmor {
   public IC2AdvancedArmorBase(String itemName, ArmorMaterial material, EquipmentSlot slot, @Nullable PropertiesBuilder props) {
      super(itemName, material, slot, (props == null ? new PropertiesBuilder() : props).setNoRepair());
   }

   public IC2AdvancedArmorBase(String itemName, EquipmentSlot slot, @Nullable PropertiesBuilder props) {
      this(itemName, IC2Materials.ADVANCED_ARMOR, slot, props);
   }

   @Override
   public ICustomArmor.AbsorptionProperties getProperties(LivingEntity entity, ItemStack armor, DamageSource source, double damage, EquipmentSlot slot) {
      return new ICustomArmor.AbsorptionProperties(0, 0.0, 0);
   }

   @Override
   public void damageArmor(LivingEntity player, ItemStack stack, DamageSource source, int damage, EquipmentSlot slot, ICustomArmor.DamageType type) {
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
      return HashMultimap.create();
   }

   public abstract Ingredient getRepairMaterial();

   public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
      return this.getRepairMaterial().test(repair);
   }
}
