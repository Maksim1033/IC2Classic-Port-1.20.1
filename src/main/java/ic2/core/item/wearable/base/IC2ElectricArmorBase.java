package ic2.core.item.wearable.base;

import ic2.api.items.armor.ICustomArmor;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.items.electric.IElectricItem;
import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Materials;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public abstract class IC2ElectricArmorBase extends IC2ArmorBase implements IDamagelessElectricItem, ICustomArmor, IElectricEnchantable {
   public int capacity;
   public int transferLimit;
   public int tier;

   public IC2ElectricArmorBase(String itemName, EquipmentSlot slot, @Nullable PropertiesBuilder props) {
      super(itemName, IC2Materials.ELECTRIC_ARMOR, slot, (props == null ? new PropertiesBuilder() : props).setNoRepair().maxDamage(0).maxStackSize(1));
   }

   public IC2ElectricArmorBase(String itemName, EquipmentSlot slot, int capacity, int transferLimit, int tier, @Nullable PropertiesBuilder props) {
      super(itemName, IC2Materials.ELECTRIC_ARMOR, slot, (props == null ? new PropertiesBuilder() : props).setNoRepair().maxDamage(0).maxStackSize(1));
      this.capacity = capacity;
      this.transferLimit = transferLimit;
      this.tier = tier;
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return this.capacity;
   }

   public int getTier() {
      return this.tier;
   }

   public int getTransferLimit() {
      return this.transferLimit;
   }

   public boolean isRepairable(ItemStack stack) {
      return false;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
      return false;
   }

   @Override
   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS;
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      if (stack.getItem() instanceof IC2ArmorBase item) {
         switch (item.getEquipmentSlot()) {
            case CHEST:
               return EnchantmentCategory.ARMOR_CHEST;
            case FEET:
               return EnchantmentCategory.ARMOR_FEET;
            case HEAD:
               return EnchantmentCategory.ARMOR_HEAD;
            case LEGS:
               return EnchantmentCategory.ARMOR_LEGS;
            default:
               return EnchantmentCategory.ARMOR;
         }
      } else {
         return null;
      }
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> subItems) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, subItems);
      }
   }

   public void damageAbsorbed(Player player, int damage) {
   }

   public void useEnergy(ItemStack stack, int amount, LivingEntity player) {
      ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, amount), 2147483647, true, false, false);
   }

   public boolean canUseEnergy(ItemStack stack, int amount) {
      return ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, amount), 2147483647, true, false, true) >= amount;
   }

   public boolean isBlockingEverything(ItemStack stack, DamageSource source, LivingEntity living) {
      return false;
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER
         .discharge(stack, ElectricItem.applyEnchantmentEffect(stack, this.getEnergyPerDamage(stack) * amount), 2147483647, true, false, false);
      return 0;
   }

   public abstract double getDamageAbsorptionRatio(ItemStack var1);

   public abstract int getEnergyPerDamage(ItemStack var1);

   public double getBaseAbsorptionRatio(ItemStack stack) {
      switch (this.getEquipmentSlot()) {
         case CHEST:
            return 0.4;
         case FEET:
         case LEGS:
            return 0.3;
         case HEAD:
            return 0.15;
         default:
            return 0.0;
      }
   }

   public static boolean hasElectricBoots(LivingEntity base) {
      ItemStack stack = base.getItemBySlot(EquipmentSlot.FEET);
      return !stack.isEmpty() && stack.getItem() instanceof IElectricItem ? !base.level().isEmptyBlock(base.blockPosition().below()) : false;
   }

   public int getBarWidth(ItemStack stack) {
      return IC2ElectricItem.getElectricWidth(stack);
   }

   public int getBarColor(ItemStack stack) {
      return IC2ElectricItem.getRGBDurability(stack);
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   @Override
   public ICustomArmor.AbsorptionProperties getProperties(LivingEntity entity, ItemStack armor, DamageSource source, double damage, EquipmentSlot slot) {
      if (source.is(DamageTypeTags.BYPASSES_ARMOR) && !this.isBlockingEverything(armor, source, entity)) {
         return new ICustomArmor.AbsorptionProperties(0, 0.0, 0);
      } else if (source.getMsgId().equals(IC2DamageSource.ELECTRICITY.getMsgId()) && IC2.CONFIG.enableSpecialElectricArmor.get()) {
         return new ICustomArmor.AbsorptionProperties(0, 1.0, 2147483647);
      } else {
         double absorptionRatio = this.getBaseAbsorptionRatio(armor) * this.getDamageAbsorptionRatio(armor);
         double energyPerDamage = (double)this.getEnergyPerDamage(armor);
         energyPerDamage *= IC2.CONFIG.electricSuitEnergyCostModifier.get();
         absorptionRatio *= IC2.CONFIG.electricSuitAbsorptionScale.get();
         int damageLimit = (int)(
            energyPerDamage > 0.0 ? (double)ElectricItem.MANAGER.discharge(armor, 2147483647, 2147483647, true, false, true) / energyPerDamage : 0.0
         );
         return new ICustomArmor.AbsorptionProperties(0, absorptionRatio, damageLimit);
      }
   }

   @Override
   public void damageArmor(LivingEntity player, ItemStack stack, DamageSource source, int damage, EquipmentSlot slot, ICustomArmor.DamageType type) {
      if (source.getMsgId().equals(IC2DamageSource.ELECTRICITY.getMsgId()) && IC2.CONFIG.enableSpecialElectricArmor.get()) {
         int max = this.getCapacity(stack) - ElectricItem.MANAGER.getCharge(stack);
         if (max > 0) {
            ElectricItem.MANAGER.charge(stack, damage, 2147483647, true, false);
            return;
         }

         if (hasElectricBoots(player)) {
            return;
         }
      }

      this.useEnergy(stack, (int)((double)damage * (double)this.getEnergyPerDamage(stack) * IC2.CONFIG.electricSuitEnergyCostModifier.get()), player);
      if (player instanceof Player) {
         this.damageAbsorbed((Player)player, damage);
      }
   }

   @Override
   public boolean canBlockDamageSource(LivingEntity player, ItemStack stack, DamageSource source, EquipmentSlot slot) {
      return source.getMsgId().equals(IC2DamageSource.ELECTRICITY.getMsgId()) && IC2.CONFIG.enableSpecialElectricArmor.get()
         || !source.is(DamageTypeTags.BYPASSES_ARMOR)
         || this.isBlockingEverything(stack, source, player);
   }
}
