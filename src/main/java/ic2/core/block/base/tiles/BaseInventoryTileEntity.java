package ic2.core.block.base.tiles;

import ic2.api.network.buffer.NetworkInfo;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.DirectionalInventoryComparator;
import ic2.core.block.base.misc.comparator.types.base.InventoryComparator;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.handler.IHasInventoryHandler;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.filter.ISlotFilter;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

public abstract class BaseInventoryTileEntity extends BaseTileEntity implements IHasInventory, IHasInventoryHandler, IDropProvider {
   public NonNullList<ItemStack> inventory;
   public int inventorySize;
   @NetworkInfo
   InventoryHandler handler = new InventoryHandler(this);

   public BaseInventoryTileEntity(BlockPos pos, BlockState state, int size) {
      super(pos, state);
      this.inventorySize = size;
      this.addGuiFields(new String[]{"handler"});
      this.inventory = NonNullList.withSize(size, ItemStack.EMPTY);
      this.addSlotInfo(this.handler);
      this.handler.validateSlots();
      if (this.inventorySize > 0) {
         this.addComparator(new InventoryComparator("slots", ComparatorNames.FILLED_SLOTS, this, true));
         this.addComparator(new InventoryComparator("items", ComparatorNames.FILLED_INVENTORY, this, false));
         this.addComparator(new DirectionalInventoryComparator("dir_slots", ComparatorNames.FILLED_DIR_SLOTS, this, true));
         this.addComparator(new DirectionalInventoryComparator("dir_items", ComparatorNames.FILLED_DIR_INVENTORY, this, false));
      }
   }

   protected void addSlotInfo(InventoryHandler handler) {
   }

   @Override
   public InventoryHandler getInventoryHandler() {
      return this.handler;
   }

   @Override
   public boolean allowsUI() {
      return true;
   }

   @Override
   public int getSlotCount() {
      return this.inventorySize;
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      this.inventory.set(slot, stack);
      if (stack.getCount() > this.getMaxStackSize(slot)) {
         stack.setCount(this.getMaxStackSize(slot));
      }

      this.handleNonTickComparators();
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 64;
   }

   @Override
   public boolean canInsert(int slot, ItemStack stack) {
      InventoryHandler handler = this.getInventoryHandler();
      if (handler == null) {
         return true;
      } else {
         ISlotFilter filter = handler.getInputFilter(slot);
         return filter == null || filter.matches(slot, stack);
      }
   }

   @Override
   public boolean canExtract(int slot, ItemStack stack) {
      return true;
   }

   protected void setOrGrow(int slot, ItemStack stack, boolean compare) {
      ItemStack inv = (ItemStack)this.inventory.get(slot);
      if (inv.isEmpty()) {
         this.inventory.set(slot, stack);
      } else if (!compare || StackUtil.isStackEqual(inv, stack)) {
         inv.setCount(Mth.clamp(stack.getCount() + inv.getCount(), 0, Math.min(stack.getMaxStackSize(), this.getMaxStackSize(slot))));
      }
   }

   protected boolean canPlace(int slot, ItemStack stack) {
      ItemStack inv = (ItemStack)this.inventory.get(slot);
      return inv.isEmpty() || StackUtil.isStackEqual(inv, stack) && StackUtil.getStackSizeLeft(inv) >= stack.getCount();
   }

   protected boolean transferStack(int from, int to) {
      if (((ItemStack)this.inventory.get(from)).isEmpty()) {
         return false;
      } else if (((ItemStack)this.inventory.get(to)).isEmpty()) {
         this.inventory.set(to, (ItemStack)this.inventory.get(from));
         this.inventory.set(from, ItemStack.EMPTY);
         this.handleNonTickComparators();
         return true;
      } else {
         if (StackUtil.isStackEqual((ItemStack)this.inventory.get(to), (ItemStack)this.inventory.get(from))) {
            int left = StackUtil.getStackSizeLeft((ItemStack)this.inventory.get(to));
            if (left > 0) {
               if (left >= ((ItemStack)this.inventory.get(from)).getCount()) {
                  ((ItemStack)this.inventory.get(to)).grow(((ItemStack)this.inventory.get(from)).getCount());
                  this.inventory.set(from, ItemStack.EMPTY);
               } else {
                  ((ItemStack)this.inventory.get(to)).grow(left);
                  ((ItemStack)this.inventory.get(from)).shrink(left);
               }

               this.handleNonTickComparators();
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      NBTUtils.loadItems(compound, this.inventory);
      this.handler.load(compound.getCompound("handler"));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.saveItems(compound, this.inventory);
      NBTUtils.put(compound, "handler", this.handler.save(new CompoundTag()));
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      for (int i = 0; i < this.inventorySize; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         if (!stack.isEmpty()) {
            drops.add(stack);
         }
      }
   }

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == ForgeCapabilities.ITEM_HANDLER && this.inventorySize > 0 ? this.handler.getHandler(side).cast() : super.getCapability(cap, side);
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.handler.remove();
      super.onUnloaded(chunk);
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key >= 32767 && key <= 32767 + this.inventorySize + 3) {
         if (key == 32767 + this.inventorySize + 3) {
            if (value == -1) {
               this.handler.resetMachineData();
            } else {
               this.handler.resetSlotData(value);
            }
         } else if (key == 32767 + this.inventorySize + 2) {
            this.handler.setSingleStack(value == 1);
         } else if (key == 32767 + this.inventorySize + 1) {
            this.handler.setSingleItem(value == 1);
         } else if (key == 32767) {
            if ((value & 1) == 1) {
               this.handler.toggleMachineSide(Direction.from3DDataValue(value >>> 1));
            } else {
               this.handler.toggleMachineAccess(Direction.from3DDataValue(value >>> 1));
            }
         } else {
            int slot = key - 32767 - 1;
            if ((value & 1) == 1) {
               this.handler.toggleSlotSide(slot, Direction.from3DDataValue(value >>> 1));
            } else {
               this.handler.toggleSlotAccess(slot, Direction.from3DDataValue(value >>> 1));
            }
         }

         this.updateGuiField("handler");
      }
   }
}
