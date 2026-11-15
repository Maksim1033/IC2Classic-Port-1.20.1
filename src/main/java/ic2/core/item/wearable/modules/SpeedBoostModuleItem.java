package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.IC2;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpeedBoostModuleItem extends BaseModuleItem {
   public SpeedBoostModuleItem(String textureFolder, String textureName) {
      super("speed_boost_module", null, textureFolder, textureName, IArmorModule.ModuleType.MOVEMENT);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.LEGS;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      if (this.canUseEnergy(armor, 1000)) {
         PlayerHandler handler = PlayerHandler.getHandler(player);
         if (handler.doSpringBoost && player.isSprinting() || handler.boostKeyDown && handler.forwardKeyDown) {
            if (++handler.speedTicker % 10 == 0) {
               this.useEnergy(armor, 1000, player);
            }

            boolean original = IC2.CONFIG.originalQuantumBehavior.get();
            player.moveRelative(
               player.isInWater() ? 0.1F : (!player.onGround() && !player.isFallFlying() ? (original ? 0.25F : 0.025F) : 0.22F), new Vec3(0.0, 0.0, 1.0)
            );
         }
      }
   }
}
