package ic2.core.inventory.slot;

import ic2.core.inventory.base.IFluidInventory;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidSlot extends SlotBase implements IGhostSlot, IFluidSlot {
   IFluidInventory fluidInv;

   public <T extends IHasInventory & IFluidInventory> FluidSlot(T inv, int index, int xPosition, int yPosition) {
      super(inv, index, xPosition, yPosition);
      this.fluidInv = inv;
   }

   public boolean mayPlace(ItemStack stack) {
      return this.fluidInv.canInsert(this.slot, FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).getFluid());
   }

   public boolean mayPickup(Player playerIn) {
      return false;
   }

   @Override
   public int getSlotID() {
      return this.index;
   }

   @Override
   public int getXPos() {
      return this.x;
   }

   @Override
   public int getYPos() {
      return this.y;
   }

   @Override
   public boolean isStackValid(ItemStack stack) {
      return this.mayPlace(stack);
   }

   @Override
   public void setFilter(ItemStack stack) {
      Fluid fluid = FluidHelper.getDisplayFluid(stack);
      if (this.fluidInv.canInsert(this.slot, fluid)) {
         this.fluidInv.setFluidInSlot(this.slot, fluid);
      }
   }

   @Override
   public IGhostSlot.GhostType getType() {
      return IGhostSlot.GhostType.FILTER;
   }
}
