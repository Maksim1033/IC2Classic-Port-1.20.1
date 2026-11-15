package ic2.core.item.inv.inventory;

import ic2.core.inventory.base.IFluidInventory;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.FluidScannerContainer;
import ic2.core.item.misc.FluidDisplay;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidScannerInventory extends PortableInventory implements IFluidInventory {
   NonNullList<Fluid> fluids;

   public FluidScannerInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FluidScannerContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 9;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.fluids = NonNullList.withSize(9, Fluids.EMPTY);

      for (CompoundTag data : NBTListWrapper.wrap(StackUtil.getNbtData(this.stack).getList("fluids", 10), CompoundTag.class)) {
         Fluid fluid = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(data.getString("fluid")));
         int slot = data.getInt("slot");
         if (slot >= 0 && slot < 9 && this.canInsert(slot, fluid)) {
            this.setFluidInSlot(slot, fluid);
         }
      }
   }

   @Override
   public void save(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (int i = 0; i < 9; i++) {
         Fluid fluid = (Fluid)this.fluids.get(i);
         if (fluid != Fluids.EMPTY) {
            CompoundTag data = new CompoundTag();
            data.putString("fluid", ForgeRegistryWrapper.getFluidKey(fluid).toString());
            data.putByte("slot", (byte)i);
            list.add(data);
         }
      }

      nbt.put("fluids", list);
   }

   @Override
   public int getFluidSlots() {
      return this.getSlotCount();
   }

   @Override
   public void setFluidInSlot(int slot, Fluid fluid) {
      this.fluids.set(slot, fluid);
      this.inventory.set(slot, fluid == Fluids.EMPTY ? ItemStack.EMPTY : FluidDisplay.createDisplay(fluid));
      this.markDirty();
   }

   @Override
   public boolean canInsert(int slot, Fluid fluid) {
      return fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource();
   }

   @Override
   public Fluid getFluidInSlot(int slot) {
      return (Fluid)this.fluids.get(slot);
   }
}
