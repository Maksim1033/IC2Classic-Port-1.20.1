package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import net.minecraft.world.item.ItemStack;

public class ExtraEnergyModuleItem extends BaseModuleItem {
   int extra;

   public ExtraEnergyModuleItem(String itemName, String textureFolder, String textureName, int extra) {
      super(itemName, null, textureFolder, textureName, IArmorModule.ModuleType.CHARGER);
      this.extra = extra;
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.addAddModifier(armor, IArmorModule.ArmorMod.ENERGY_STORAGE, this.extra);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.ENERGY_STORAGE, this.extra);
   }
}
