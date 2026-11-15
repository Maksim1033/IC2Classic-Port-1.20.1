package ic2.core.inventory.slot;

import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.filter.special.FuelFilter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FilterSlot extends SlotBase implements IGhostSlot {
   IFilter filter;

   public FilterSlot(IHasInventory inv, int index, int xPosition, int yPosition, IFilter filter) {
      super(inv, index, xPosition, yPosition);
      this.filter = filter;
   }

   public boolean mayPlace(ItemStack stack) {
      return this.filter == null || this.filter.matches(stack);
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
   }

   public static Slot createClassSlot(IHasInventory inv, int index, int xPosition, int yPosition, Class<?> clz) {
      return new FilterSlot(inv, index, xPosition, yPosition, new ClassFilter(clz));
   }

   public static Slot createChargeSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, ElectricItemFilter.CHARGE_FILTER)
         .setBackground(new ResourceLocation("ic2", "misc/gui/charge_slot"));
   }

   public static Slot createChargeSlot(IHasInventory inv, int tier, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, new ElectricItemFilter(true, true, tier))
         .setBackground(new ResourceLocation("ic2", "misc/gui/charge_slot"));
   }

   public static Slot createDischargeSlot(IHasInventory inv, int tier, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, new ElectricItemFilter(false, true, tier))
         .setBackground(new ResourceLocation("ic2", "misc/gui/discharge"));
   }

   public static Slot createDischargeSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER)
         .setBackground(new ResourceLocation("ic2", "misc/gui/discharge"));
   }

   public static Slot createFluidFillSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, FluidFilter.EMPTY_CONTAINER).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain"));
   }

   public static Slot createFluidFillSlot(IHasInventory inv, int index, int xPosition, int yPosition, Fluid fluid) {
      return new FilterSlot(inv, index, xPosition, yPosition, FluidFilter.createFillFilter(fluid))
         .setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain"));
   }

   public static Slot createFluidDrainSlot(IHasInventory inv, int index, int xPosition, int yPosition, Fluid... fluids) {
      return new FilterSlot(inv, index, xPosition, yPosition, new FluidFilter(fluids)).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain"));
   }

   public static Slot createFluidFillSlot(IHasInventory inv, int index, int xPosition, int yPosition, IFluidHandler fluid) {
      return new FilterSlot(inv, index, xPosition, yPosition, new FluidFilter.FluidTankFillFilter(fluid))
         .setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain"));
   }

   public static Slot createFluidDrainSlot(IHasInventory inv, int index, int xPosition, int yPosition, IFluidHandler fluid) {
      return new FilterSlot(inv, index, xPosition, yPosition, new FluidFilter.FluidTankFilter(fluid))
         .setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain"));
   }

   public static Slot createOutputSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, SpecialFilters.ALWAYS_FALSE);
   }

   public static Slot createFluidOutputSlot(IHasInventory inv, int index, int xPosition, int yPosition) {
      return new FilterSlot(inv, index, xPosition, yPosition, SpecialFilters.ALWAYS_FALSE).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain"));
   }

   public static Slot createFuelSlot(IHasInventory inv, int index, int xPosition, int yPosition, boolean lava) {
      return new FilterSlot(inv, index, xPosition, yPosition, lava ? FuelFilter.WITH_LAVA : FuelFilter.WITHOUT_LAVA)
         .setBackground(new ResourceLocation("ic2", "misc/gui/fuel"));
   }
}
