package ic2.core.item.wearable.base;

import ic2.api.items.armor.IArmorModule;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class IC2ElectricJetpackBase extends IC2JetpackBase implements IDamagelessElectricItem, IElectricEnchantable, IBaseArmorModule {
   public static final String MODULE_DATA = "jetpack_data";

   public IC2ElectricJetpackBase(String itemName, EquipmentSlot slot, @Nullable PropertiesBuilder props) {
      super(itemName, slot, (props == null ? new PropertiesBuilder() : props).maxDamage(0));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   @Override
   public int getMaxFuel(ItemStack stack) {
      return this.getCapacity(stack);
   }

   @Override
   public int getFuel(ItemStack stack) {
      return ElectricItem.MANAGER.getCharge(stack);
   }

   @Override
   public void useEnergy(Player player, ItemStack stack, int amount) {
      if (stack.getItem() != this) {
         ElectricItem.MANAGER.discharge(stack, amount, 2147483647, true, false, false);
      } else if (!ElectricItem.MANAGER.use(stack, amount, player)) {
         ElectricItem.MANAGER.discharge(stack, amount, 2147483647, true, false, false);
      }
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, items);
      }
   }

   @Override
   public int getBarWidth(ItemStack stack) {
      return IC2ElectricItem.getElectricWidth(stack);
   }

   @Override
   public int getBarColor(ItemStack stack) {
      return IC2ElectricItem.getRGBDurability(stack);
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER
         .discharge(
            stack, ElectricItem.applyEnchantmentEffect(stack, this.getFuelCost(stack, IC2JetpackBase.HoverMode.NONE) * amount), 2147483647, true, false, false
         );
      return 0;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS;
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      if (stack.getItem() instanceof IC2ElectricJetpackBase item) {
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

   @Override
   public CompoundTag getNBTData(ItemStack stack, boolean create) {
      CompoundTag data = super.getNBTData(stack, create);
      if (stack.getItem() != this) {
         CompoundTag subData = data.getCompound("jetpack_data");
         if (create) {
            data.put("jetpack_data", subData);
         }

         return subData;
      } else {
         return data;
      }
   }

   @Override
   public IArmorModule.ModuleType getType(ItemStack stack) {
      return IArmorModule.ModuleType.BACK_SLOT;
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.CHEST;
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      CompoundTag nbt = StackUtil.getNbtData(stack).copy();
      nbt.remove("charge");
      armor.getOrCreateTagElement("jetpack_data").merge(nbt);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      CompoundTag original = StackUtil.getNbtData(armor).getCompound("jetpack_data").copy();
      if (!original.isEmpty() || !StackUtil.getNbtData(stack).isEmpty()) {
         Tag charge = stack.getOrCreateTag().get("charge");
         stack.setTag(original.isEmpty() && charge == null ? null : original);
         if (charge != null) {
            stack.addTagElement("charge", charge);
         }
      }

      armor.removeTagKey("jetpack_data");
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      this.onArmorTick(armor, world, player);
   }

   @Override
   public void transferToArmor(ItemStack stack, ItemStack oldArmor, ItemStack newArmor) {
      newArmor.getOrCreateTagElement("jetpack_data").merge(StackUtil.getNbtData(newArmor).getCompound("jetpack_data"));
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      return false;
   }
}
