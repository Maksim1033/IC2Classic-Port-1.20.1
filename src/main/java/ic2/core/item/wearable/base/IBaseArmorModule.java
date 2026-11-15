package ic2.core.item.wearable.base;

import ic2.api.items.armor.IArmorModule;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IBaseArmorModule extends IArmorModule {
   @Override
   default boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return true;
   }

   @Override
   default void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
   }

   @Override
   default void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
   }

   @Override
   default void transferToArmor(ItemStack stack, ItemStack oldArmor, ItemStack newArmor) {
   }

   @Override
   default void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
   }

   @Override
   default void onEquipped(ItemStack stack, ItemStack armor, Player entity) {
   }

   @Override
   default void onUnequipped(ItemStack stack, ItemStack armor, Player entity) {
   }

   @Override
   default void provideCapabilities(ItemStack stack, ItemStack armor) {
   }
}
