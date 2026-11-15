package ic2.core.block.transport.fluid.helper;

import ic2.core.block.base.cache.ICache;
import ic2.core.inventory.base.IFluidInventory;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.item.misc.FluidDisplay;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class InventoryData implements IFluidInventory, IHasInventory, ContainerData {
   NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
   int direction;
   int allDirs;
   boolean inputs = true;
   ICache<IFluidHandler> fluidCache;
   FluidCache<?> cache;

   public InventoryData(ICache<IFluidHandler> fluidCache, FluidCache<?> cache) {
      this.fluidCache = fluidCache;
      this.cache = cache;
      this.allDirs = fluidCache.getPresentSides().getCode();
      this.direction = this.getIndex();
      this.inventory.clear();

      for (int i = 0; i < 9; i++) {
         Fluid fluid = this.getFluidInSlot(i);
         this.inventory.set(i, fluid == Fluids.EMPTY ? ItemStack.EMPTY : FluidDisplay.createDisplay(fluid));
      }
   }

   protected int getIndex() {
      return this.fluidCache.getPresentSides().getRandomFacing(Direction.DOWN).get3DDataValue();
   }

   public void setFacing(AbstractContainerMenu container, int direction) {
      this.direction = direction;
      this.updateContainer(container);
   }

   public void setInputs(AbstractContainerMenu container, boolean inputs) {
      this.inputs = inputs;
      this.updateContainer(container);
   }

   private void updateContainer(AbstractContainerMenu container) {
      container.broadcastChanges();
      this.inventory.clear();

      for (int i = 0; i < 9; i++) {
         Fluid fluid = this.getFluidInSlot(i);
         this.inventory.set(i, fluid == Fluids.EMPTY ? ItemStack.EMPTY : FluidDisplay.createDisplay(fluid));
      }
   }

   public void setPriority(int value) {
      this.cache.get(this.direction).priority += value;
   }

   public void setWhiteList(boolean value) {
      this.cache.get(this.direction).whiteList = value;
   }

   public boolean isInput() {
      return this.inputs;
   }

   public int getDir() {
      return this.direction;
   }

   public int getDirs() {
      return this.allDirs;
   }

   public int getPriority() {
      return this.cache.get(this.direction).priority;
   }

   public boolean isWhiteList() {
      return this.cache.get(this.direction).whiteList;
   }

   public int get(int index) {
      switch (index) {
         case 1:
            return this.cache.get(this.direction).priority;
         case 2:
            return this.cache.get(this.direction).whiteList ? 1 : 0;
         case 3:
            return this.allDirs;
         case 4:
            return this.inputs ? 1 : 0;
         default:
            return this.direction;
      }
   }

   public void set(int index, int value) {
      switch (index) {
         case 0:
            this.direction = value;
            break;
         case 1:
            this.cache.get(this.direction).priority = value;
            break;
         case 2:
            this.cache.get(this.direction).whiteList = value == 1;
            break;
         case 3:
            this.allDirs = value;
            break;
         case 4:
            this.inputs = value == 1;
      }
   }

   public int getCount() {
      return 5;
   }

   @Override
   public int getFluidSlots() {
      return 9;
   }

   @Override
   public void setFluidInSlot(int slot, Fluid fluid) {
      this.cache.get(this.direction).setFluid(slot, fluid, this.inputs);
      this.inventory.set(slot, fluid == Fluids.EMPTY ? ItemStack.EMPTY : FluidDisplay.createDisplay(fluid));
   }

   @Override
   public boolean canInsert(int slot, Fluid fluid) {
      return fluid != Fluids.EMPTY;
   }

   @Override
   public Fluid getFluidInSlot(int slot) {
      return this.cache.get(this.direction).getFluid(slot, this.inputs);
   }

   @Override
   public int getSlotCount() {
      return 9;
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      this.inventory.set(slot, stack);
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 64;
   }

   @Override
   public boolean canInsert(int slot, ItemStack stack) {
      return false;
   }

   @Override
   public boolean canExtract(int slot, ItemStack stack) {
      return false;
   }
}
