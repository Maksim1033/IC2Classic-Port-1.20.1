package ic2.core.item.base;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.core.IC2;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.ColorUtils;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public abstract class IC2ElectricItem extends IC2Item implements IDamagelessElectricItem {
   public static final String HIDDEN_CHARGE_BAR = "hide_charge_bar";
   public static final int DAMAGE_COLOR = 8600319;
   public static final int NORMAL_COLOR = 717055;
   public static final double DAMAGED_R = 131.0;
   public static final double DAMAGED_G = 58.0;
   public static final double DAMAGED_B = 255.0;
   public static final double NORMAL_R = 10.0;
   public static final double NORMAL_G = 240.0;
   public static final double NORMAL_B = 255.0;
   public int capacity;
   public int tier;
   public int transferLimit;
   public boolean provider;

   public IC2ElectricItem(String itemName, @Nullable PropertiesBuilder properties) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).setNoRepair().maxStackSize(1).group(IC2.IC2_MAIN_GROUP));
   }

   public IC2ElectricItem(String itemName) {
      this(itemName, null);
   }

   protected abstract int getEnergyCost(ItemStack var1);

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return this.provider;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return this.capacity;
   }

   @Override
   public int getTier(ItemStack stack) {
      return this.tier;
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return this.transferLimit;
   }

   public boolean isBarVisible(ItemStack stack) {
      return !StackUtil.getNbtData(stack).getBoolean("hide_charge_bar");
   }

   public int getBarWidth(ItemStack stack) {
      return getElectricWidth(stack);
   }

   public int getBarColor(ItemStack stack) {
      return getRGBDurability(stack);
   }

   public static int getElectricWidth(ItemStack stack) {
      return (int)Math.round((double)ElectricItem.MANAGER.getCharge(stack) / (double)ElectricItem.MANAGER.getCapacity(stack) * 13.0);
   }

   public static int getRGBDurability(ItemStack stack) {
      return ColorUtils.mixColors(717055, 8600319, 1.0F - (float)stack.getItem().getBarWidth(stack) / 13.0F) | 0xFF000000;
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, this.getEnergyCost(stack) * amount), 2147483647, true, false, false);
      return 0;
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         addEmptyAndFullToGroup(this, items);
      }
   }

   public static void addEmptyAndFullToGroup(ItemLike item, NonNullList<ItemStack> items) {
      ItemStack empty = new ItemStack(item, 1);
      ItemStack full = new ItemStack(item, 1);
      ElectricItem.MANAGER.discharge(empty, 2147483647, 2147483647, true, false, false);
      ElectricItem.MANAGER.charge(full, 2147483647, 2147483647, true, false);
      items.add(empty);
      items.add(full);
   }

   public static void addEmptyAndFullToGroup(ItemStack stack, NonNullList<ItemStack> items) {
      ItemStack empty = stack.copy();
      ItemStack full = stack.copy();
      ElectricItem.MANAGER.discharge(empty, 2147483647, 2147483647, true, false, false);
      ElectricItem.MANAGER.charge(full, 2147483647, 2147483647, true, false);
      items.add(empty);
      items.add(full);
   }
}
