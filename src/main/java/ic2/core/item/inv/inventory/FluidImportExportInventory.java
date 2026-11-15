package ic2.core.item.inv.inventory;

import ic2.core.inventory.base.IFluidInventory;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.FluidImportExportContainer;
import ic2.core.item.misc.FluidDisplay;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidImportExportInventory extends PortableInventory implements ContainerData, IFluidInventory {
   NonNullList<Fluid> fluids;
   public int direction;
   public int transferRate;
   public boolean whitelist;

   public FluidImportExportInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FluidImportExportContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 9;
   }

   public void onDataReceived(int key, int value) {
      switch (key) {
         case 0:
            this.direction = value;
            break;
         case 1:
            this.transferRate = value;
            break;
         case 2:
            this.whitelist = value == 1;
      }
   }

   public int get(int index) {
      switch (index) {
         case 0:
            return this.direction;
         case 1:
            return this.transferRate;
         case 2:
            return this.whitelist ? 1 : 0;
         default:
            return 0;
      }
   }

   public void set(int index, int value) {
      switch (index) {
         case 0:
            this.direction = value;
            break;
         case 1:
            this.transferRate = value;
            break;
         case 2:
            this.whitelist = value == 1;
      }
   }

   public int getCount() {
      return 3;
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

   @Override
   public void load(CompoundTag nbt) {
      this.fluids = NonNullList.withSize(9, Fluids.EMPTY);

      for (CompoundTag entry : NBTListWrapper.wrap(nbt.getList("filters", 10), CompoundTag.class)) {
         int index = entry.getInt("slot");
         if (index >= 0 && index < 9) {
            Fluid fluid = ForgeRegistryWrapper.getFluidValue(new ResourceLocation(entry.getString("id")));
            this.fluids.set(index, fluid);
            this.inventory.set(index, fluid == Fluids.EMPTY ? ItemStack.EMPTY : FluidDisplay.createDisplay(fluid));
         }
      }

      this.direction = NBTUtils.getInt(nbt, "facing", -1);
      this.transferRate = NBTUtils.getInt(nbt, "transfer", 1000);
      this.whitelist = NBTUtils.getBoolean(nbt, "inverted", true);
   }

   @Override
   public void save(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (int i = 0; i < 9; i++) {
         Fluid fluid = (Fluid)this.fluids.get(i);
         if (fluid != Fluids.EMPTY) {
            CompoundTag data = new CompoundTag();
            data.putByte("slot", (byte)i);
            data.putString("id", ForgeRegistryWrapper.getFluidKey(fluid).toString());
            list.add(data);
         }
      }

      NBTUtils.put(nbt, "filters", list);
      NBTUtils.putInt(nbt, "transfer", this.transferRate, 1000);
      NBTUtils.putBoolean(nbt, "inverted", this.whitelist, true);
      NBTUtils.putInt(nbt, "facing", this.direction, -1);
   }
}
