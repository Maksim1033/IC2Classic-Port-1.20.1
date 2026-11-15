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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class JumpBoostModuleItem extends BaseModuleItem {
   public JumpBoostModuleItem(String textureFolder, String textureName) {
      super("jump_boost_module", null, textureFolder, textureName, IArmorModule.ModuleType.MOVEMENT);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.FEET;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      PlayerHandler handler = PlayerHandler.getHandler(player);
      if (handler.jumpBoost < 1.0F && player.onGround() && this.canUseEnergy(armor, 1000)) {
         handler.jumpBoost = 1.0F;
         this.useEnergy(armor, 1000, player);
      }

      if (player.getDeltaMovement().y() >= 0.0 && handler.jumpBoost > 0.0F && !player.isInWater()) {
         if (handler.jumpKeyDown && handler.boostKeyDown) {
            boolean original = IC2.CONFIG.originalQuantumBehavior.get();
            double mod = handler.jumpBoost == 1.0F ? (original ? 3.5 : 1.75) : 1.0;
            player.setDeltaMovement(player.getDeltaMovement().multiply(mod, 1.0, mod).add(0.0, (double)handler.jumpBoost * (original ? 0.3 : 0.15), 0.0));
            handler.jumpBoost *= 0.75F;
         } else if (handler.jumpBoost < 1.0F) {
            handler.jumpBoost = 0.0F;
         }
      }
   }
}
