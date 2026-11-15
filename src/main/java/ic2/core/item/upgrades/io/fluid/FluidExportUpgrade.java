package ic2.core.item.upgrades.io.fluid;

import ic2.api.tiles.IFluidMachine;
import ic2.api.tiles.IMachine;
import ic2.core.item.upgrades.base.BaseFluidTransportUpgrade;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidExportUpgrade extends BaseFluidTransportUpgrade {
   public FluidExportUpgrade() {
      super("fluid_export");
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine instanceof IFluidMachine) {
         Direction dir = this.getFacing(stack);
         if (dir != null) {
            IFluidHandler target = ((IFluidMachine)machine).getConnectedTank(dir);
            if (target != null) {
               IFluidHandler source = this.getCapability(machine, dir, ForgeCapabilities.FLUID_HANDLER);
               if (source != null) {
                  int rate = Math.min(this.transferRate(stack), machine.getAvailableEnergy() * 100);
                  if (rate > 0) {
                     this.transferFluid(machine, source, target, rate, this.getFluids(stack), this.isInverted(stack));
                  }
               }
            }
         }
      }
   }
}
