package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LavaProtectModuleItem extends BaseModuleItem {
   public static final String LAVA = "lava_enabled";

   public LavaProtectModuleItem(String textureFolder, String textureName) {
      super("lava_module", null, textureFolder, textureName, IArmorModule.ModuleType.ANY);
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      armor.getOrCreateTag().putBoolean("lava_enabled", true);
      holder.addAddModifier(armor, IArmorModule.ArmorMod.NO_LAVA_DAMAGE, 1);
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      if (world.getGameTime() % 20L == 0L) {
         boolean result = this.canUseEnergy(stack, 1000);
         if (player.isInLava()) {
            this.useEnergy(armor, 1000, player);
         }

         CompoundTag data = stack.getOrCreateTag();
         if (data.getBoolean("lava_enabled") != result) {
            data.putBoolean("lava_enabled", result);
            if (result) {
               ((IArmorModule.IArmorModuleHolder)armor.getItem()).addAddModifier(armor, IArmorModule.ArmorMod.NO_LAVA_DAMAGE, 1);
            } else {
               ((IArmorModule.IArmorModuleHolder)armor.getItem()).removeAddModifier(armor, IArmorModule.ArmorMod.NO_LAVA_DAMAGE, 1);
            }
         }
      }
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      CompoundTag data = StackUtil.getNbtData(armor);
      if (data.getBoolean("lava_enabled")) {
         holder.removeAddModifier(armor, IArmorModule.ArmorMod.NO_LAVA_DAMAGE, 1);
      }

      data.remove("lava_enabled");
   }
}
