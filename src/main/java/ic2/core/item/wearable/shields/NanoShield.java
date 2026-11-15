package ic2.core.item.wearable.shields;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.core.IC2;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.wearable.base.IC2ShieldBase;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class NanoShield extends IC2ShieldBase implements IDamagelessElectricItem, ic2.core.item.base.IItemCategoryFiller {
   private ResourceLocation texture = new ResourceLocation(IC2.MOD_ID, "textures/models/armor/shield_nano.png");

   public NanoShield() {
      super("nano_shield", 0);
   }

   @Override
   public boolean canBlock(ItemStack item, LivingEntity entity) {
      return ElectricItem.MANAGER.canUse(item, 50);
   }

   @Override
   public void damageShield(ItemStack stack, int amount, LivingEntity entity) {
      ElectricItem.MANAGER.use(stack, amount * 50, entity);
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, 50 * amount), 2147483647, true, false, false);
      return 0;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack) {
      return this.texture;
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return 100000;
   }

   @Override
   public int getTier(ItemStack stack) {
      return 2;
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return 300;
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

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, items);
      }
   }
}
