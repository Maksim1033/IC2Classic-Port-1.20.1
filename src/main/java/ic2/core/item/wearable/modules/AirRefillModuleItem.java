package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.platform.registries.IC2Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AirRefillModuleItem extends BaseModuleItem {
   public AirRefillModuleItem(String textureFolder, String textureName) {
      super("air_refill_module", null, textureFolder, textureName, IArmorModule.ModuleType.GENERIC);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      int air = player.getAirSupply();
      if (air < 100 && this.canUseEnergy(armor, 1000)) {
         player.setAirSupply(air + 200);
         this.useEnergy(armor, 2000, player);
      } else if (air <= 0) {
         player.awardStat(IC2Stats.DROWNED_WITH_Q_HELMET);
      }
   }
}
