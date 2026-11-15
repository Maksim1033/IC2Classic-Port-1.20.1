package ic2.core.item.upgrades.io.fluid;

import ic2.api.items.IUpgradeItem;
import ic2.api.tiles.IFluidMachine;
import ic2.api.tiles.IMachine;
import ic2.core.item.upgrades.base.BaseDirectionalUpgrade;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class SimpleFluidExportUpgrade extends BaseDirectionalUpgrade {
   public SimpleFluidExportUpgrade() {
      super("basic_fluid_export");
      this.functions.remove(IUpgradeItem.Functions.RECIPE);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addSimpleToolTip("tooltip.item.ic2.upgrade.stacking");
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.TRANSPORT_MOD;
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine instanceof IFluidMachine) {
         Direction dir = this.getFacing(stack);
         if (dir != null) {
            IFluidHandler target = ((IFluidMachine)machine).getConnectedTank(dir);
            if (target != null) {
               IFluidHandler source = this.getCapability(machine, dir.getOpposite(), ForgeCapabilities.FLUID_HANDLER);
               if (source != null) {
                  source.drain(target.fill(source.drain(Math.min(90, stack.getCount() * 10), FluidAction.SIMULATE), FluidAction.EXECUTE), FluidAction.EXECUTE);
               }
            }
         }
      }
   }
}
