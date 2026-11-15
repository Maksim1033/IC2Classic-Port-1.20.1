package ic2.core.item.wearable.jetpacks;

import ic2.api.items.IFuelableItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.wearable.base.IC2JetpackBase;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class FuelJetpack extends IC2JetpackBase implements IFuelableItem {
   public FuelJetpack() {
      super("fuel_jetpack", EquipmentSlot.CHEST, new PropertiesBuilder().maxDamage(18002));
   }

   @Override
   public boolean canDisable(ItemStack stack) {
      return false;
   }

   @Override
   public boolean canDoRocketMode(ItemStack stack) {
      return false;
   }

   @Override
   public boolean canDoAdvHoverMode(ItemStack stack) {
      return false;
   }

   @Override
   public boolean isElectricJetpack(ItemStack stack) {
      return false;
   }

   @Override
   public float getPower(ItemStack stack) {
      return 1.0F;
   }

   @Override
   public float getThruster(ItemStack stack, IC2JetpackBase.HoverMode mode) {
      return mode == IC2JetpackBase.HoverMode.BASIC ? 0.5F : 0.15F;
   }

   @Override
   public float getDropPercentage(ItemStack stack) {
      return 0.5F;
   }

   @Override
   public int getMaxHeight(ItemStack stack, int worldHeight) {
      return worldHeight;
   }

   @Override
   public int getFuel(ItemStack stack) {
      return Math.max(stack.getMaxDamage() - stack.getDamageValue() - 1, 0);
   }

   @Override
   public int getMaxFuel(ItemStack stack) {
      return stack.getMaxDamage();
   }

   @Override
   public int getMaxRocketCharge(ItemStack stack) {
      return 0;
   }

   @Override
   public int getFuelCost(ItemStack stack, IC2JetpackBase.HoverMode mode) {
      return mode == IC2JetpackBase.HoverMode.BASIC ? 6 : 9;
   }

   @Override
   public void useEnergy(Player player, ItemStack stack, int amount) {
      stack.setDamageValue(Math.min(stack.getDamageValue() + amount, stack.getMaxDamage() - 1));
   }

   @Override
   public String getTextureFolder() {
      return "armor/jetpack";
   }

   @Override
   public String getTextureName() {
      return "base";
   }

   @Override
   public String getArmorTexture() {
      return "ic2:textures/models/armor/jetpack_fuel";
   }

   @Override
   public ItemStack fill(ItemStack stack, int amount) {
      stack.setDamageValue(Math.max(1, stack.getDamageValue() - amount));
      return stack;
   }

   @Override
   public boolean canFuel(ItemStack stack) {
      return stack.getDamageValue() > 1;
   }

   @Override
   public boolean hasFuel(ItemStack stack) {
      return false;
   }

   @Override
   public int getFuel(ItemStack stack, int requested, boolean doDrain) {
      return 0;
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         ItemStack full = new ItemStack(this);
         ItemStack empty = new ItemStack(this);
         full.setDamageValue(1);
         empty.setDamageValue(18001);
         items.add(full);
         items.add(empty);
      }
   }
}
