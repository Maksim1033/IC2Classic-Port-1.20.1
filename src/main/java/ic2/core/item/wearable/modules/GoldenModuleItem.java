package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import net.minecraft.world.item.ItemStack;

public class GoldenModuleItem extends BaseModuleItem {
   public GoldenModuleItem(String textureFolder, String textureName) {
      super("golden_module", null, textureFolder, textureName, IArmorModule.ModuleType.GENERIC);
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.addAddModifier(armor, IArmorModule.ArmorMod.GOLDEN, 1);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.GOLDEN, 1);
   }
}
