package ic2.core.block.machines.logic.planner;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class PlannerInventory extends SimpleInventory implements INetworkDataBuffer {
   ReactorPlannerTileEntity planner;
   ItemStack[] currentSet;
   byte currentIndex = -1;
   int offset = 0;

   public PlannerInventory(ReactorPlannerTileEntity planner) {
      super(18);
      this.planner = planner;
   }

   public void init() {
      this.currentSet = PlannerRegistry.getByType(
         this.currentIndex == -1 ? null : IReactorPlannerComponent.ComponentType.byID(this.currentIndex), this.planner.isSteamReactor()
      );
      this.offset = 0;
      this.update();
   }

   public void update() {
      int start = this.offset * 3;

      for (int i = 0; i < 18; i++) {
         int slot = start + i;
         if (slot >= this.currentSet.length) {
            this.setStackInSlot(i, ItemStack.EMPTY);
         } else {
            ItemStack stack = this.currentSet[slot];
            if (stack == null) {
               this.setStackInSlot(i, ItemStack.EMPTY);
            } else {
               this.setStackInSlot(i, ((IReactorPlannerComponent)stack.getItem()).applyStackSize(stack.copy(), this.planner.stackSize));
            }
         }
      }
   }

   public void setFilter(IReactorPlannerComponent.ComponentType type) {
      this.currentIndex = (byte)(type == null ? -1 : type.getIndex());
      this.init();
   }

   public void setPrevFilter() {
      this.currentIndex = (byte)(this.currentIndex == -1 ? IReactorPlannerComponent.ComponentType.size() - 1 : this.currentIndex - 1);
      this.init();
   }

   public void setNextFilter() {
      this.currentIndex = (byte)(this.currentIndex + 1 >= IReactorPlannerComponent.ComponentType.size() ? -1 : this.currentIndex + 1);
      this.init();
   }

   public int findItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return -1;
      } else {
         this.setFilter(null);
         int index = -1;
         int i = 0;

         for (int m = this.currentSet.length; i < m; i++) {
            if (StackUtil.isStackEqual(stack, this.currentSet[i])) {
               index = i;
               break;
            }
         }

         if (index != -1) {
            this.setScroll(index / 3);
         }

         return index;
      }
   }

   public void scroll(int scrollOffset) {
      this.offset = Mth.clamp(this.offset + scrollOffset, 0, Math.max(0, (this.currentSet.length - 15) / 3));
      this.update();
   }

   public void setScroll(int scroll) {
      this.offset = Mth.clamp(scroll, 0, Math.max(0, (this.currentSet.length - 15) / 3));
      this.update();
   }

   public int getOffset() {
      return this.offset;
   }

   public byte getCurrentIndex() {
      return this.currentIndex;
   }

   public int getTotalItems() {
      return this.currentSet.length;
   }

   public ItemStack getCurrentItem(int index) {
      return PlannerRegistry.setStackSize(this.currentSet[index].copy(), this.planner.stackSize);
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.currentIndex = nbt.getByte("index");
      this.offset = nbt.getInt("offset");
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      super.save(nbt);
      nbt.putByte("index", this.currentIndex);
      nbt.putInt("offset", this.offset);
      return nbt;
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeByte(this.currentIndex);
      buffer.writeVarInt(this.offset);
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.currentIndex = buffer.readByte();
      this.offset = buffer.readVarInt();
      this.currentSet = PlannerRegistry.getByType(
         this.currentIndex == -1 ? null : IReactorPlannerComponent.ComponentType.byID(this.currentIndex), this.planner.isSteamReactor()
      );
   }
}
