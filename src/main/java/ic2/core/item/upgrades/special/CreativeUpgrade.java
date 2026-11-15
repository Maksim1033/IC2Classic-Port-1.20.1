package ic2.core.item.upgrades.special;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.items.IUpgradeItem;
import ic2.api.tiles.IMachine;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.item.upgrades.base.BaseUpgradeItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class CreativeUpgrade extends BaseUpgradeItem.SimpleUpgradeItem {
   public CreativeUpgrade() {
      super("creative");
      this.functions.add(IUpgradeItem.Functions.TICK);
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.CUSTOM_MOD;
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine instanceof IEnergySink sink) {
         double needed = (double)sink.getRequestedEnergy();
         if (needed > 1.0) {
            sink.acceptEnergy(Direction.UP, EnergyNet.INSTANCE.getPowerFromTier(sink.getSinkTier()) - 1, 0);
         }
      }

      if (machine instanceof BaseTileEntity) {
         IFluidHandler handler = ((BaseTileEntity)machine).getInternalCapability(ForgeCapabilities.FLUID_HANDLER);
         if (handler != null) {
            handler.drain(1000, FluidAction.EXECUTE);
         }
      }
   }

   @Override
   public int getExtraProcessingSpeed(ItemStack stack, IMachine machine) {
      return 32767;
   }

   @Override
   public double getProcessingTimeMultiplier(ItemStack stack, IMachine machine) {
      return 0.0;
   }

   @Override
   public double getEnergyDemandMultiplier(ItemStack stack, IMachine machine) {
      return 0.0;
   }

   @Override
   public int getExtraTier(ItemStack stack, IMachine machine) {
      return 13;
   }
}
