package ic2.core.item.upgrades.io.fluid;

import ic2.api.tiles.IFluidMachine;
import ic2.api.tiles.IMachine;
import ic2.core.item.upgrades.base.BaseFluidTransportUpgrade;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidImportUpgrade extends BaseFluidTransportUpgrade {
   public FluidImportUpgrade() {
      super("fluid_import");
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine instanceof IFluidMachine) {
         Direction dir = this.getFacing(stack);
         if (dir != null) {
            IFluidHandler source = ((IFluidMachine)machine).getConnectedTank(dir);
            if (source != null) {
               IFluidHandler target = this.getCapability(machine, dir, ForgeCapabilities.FLUID_HANDLER);
               if (target != null) {
                  this.transferFluid(
                     machine,
                     source,
                     target,
                     Math.min(this.transferRate(stack), machine.getAvailableEnergy() * 100),
                     this.getFluids(stack),
                     this.isInverted(stack)
                  );
               }
            }
         }
      }
   }
}
