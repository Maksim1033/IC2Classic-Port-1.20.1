package ic2.core.block.base.misc.comparator;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.util.DirectionList;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ComparatorManager implements INetworkDataBuffer, INBTSavable {
   Map<String, BaseComparator> mappedComparator = CollectionUtils.<String, BaseComparator>createMap();
   List<BaseComparator> comparators = CollectionUtils.<BaseComparator>createList();
   List<BaseComparator> updating = CollectionUtils.<BaseComparator>createList();
   ComparingList[] values = new ComparingList[]{
      new ComparingList(this, Direction.DOWN),
      new ComparingList(this, Direction.UP),
      new ComparingList(this, Direction.NORTH),
      new ComparingList(this, Direction.SOUTH),
      new ComparingList(this, Direction.WEST),
      new ComparingList(this, Direction.EAST),
      new ComparingList(this, null)
   };
   boolean forceAll = false;
   boolean ignore_update = true;
   BlockEntity tile;

   public ComparatorManager(BlockEntity tile) {
      this.tile = tile;
   }

   public void addComparator(BaseComparator comparator) {
      if (this.mappedComparator.putIfAbsent(comparator.getId(), comparator) == null) {
         this.comparators.add(comparator);
         comparator.setManager(this);
      }
   }

   public boolean update(boolean force) {
      if (!force && this.invClock(10)) {
         return false;
      } else {
         List<BaseComparator> list = this.forceAll ? this.comparators : this.updating;
         if (list.isEmpty()) {
            return false;
         } else {
            long time = this.tile.getLevel().getGameTime();
            boolean updated = false;
            int i = 0;

            for (int m = list.size(); i < m; i++) {
               if (list.get(i).updateValue(time, this.forceAll)) {
                  updated = true;
               }
            }

            if (!updated && !this.ignore_update) {
               return false;
            } else {
               updated = false;

               for (int ix = 0; ix < 7; ix++) {
                  if (this.values[ix].updateValue()) {
                     updated = true;
                  }
               }

               if (updated && !this.ignore_update) {
                  BaseTileEntity.notifyChanges(
                     true, this.tile.getLevel(), this.tile.getBlockPos(), this.tile.getBlockState(), this.tile.getBlockPos(), DirectionList.ALL
                  );
               }

               return true;
            }
         }
      }
   }

   public void add(int index, int side) {
      if (index >= 0 && index < this.comparators.size()) {
         this.values[side].addListener(new ComparatorListener(this.comparators.get(index)));
         this.update(true);
         this.tile.getLevel().updateNeighbourForOutputSignal(this.tile.getBlockPos(), this.tile.getBlockState().getBlock());
      }
   }

   public void remove(int index, int side) {
      this.values[side].removeListeners(index);
      this.update(true);
      this.tile.getLevel().updateNeighbourForOutputSignal(this.tile.getBlockPos(), this.tile.getBlockState().getBlock());
   }

   public void setFlag(int index, int side, int flag) {
      ComparingList list = this.values[side];
      if (list.size() > 0) {
         ComparatorListener listener = list.get(index);
         listener.flags ^= 1 << flag;
         listener.onComparatorChanged();
         list.updateValue();
         this.update(true);
         this.tile.getLevel().updateNeighbourForOutputSignal(this.tile.getBlockPos(), this.tile.getBlockState().getBlock());
      }
   }

   public void flipMax(int value) {
      this.values[value].flipMax();
      this.update(true);
      this.tile.getLevel().updateNeighbourForOutputSignal(this.tile.getBlockPos(), this.tile.getBlockState().getBlock());
   }

   @OnlyIn(Dist.CLIENT)
   public ComparingList getList(int index) {
      return this.values[index];
   }

   public int getValue(Direction dir) {
      return this.values[dir == null ? 6 : dir.get3DDataValue()].getValue();
   }

   final boolean invClock(int value) {
      return (this.tile.getLevel().getGameTime() ^ this.tile.getBlockPos().asLong()) % (long)value != 0L;
   }

   @Override
   public void write(IOutputBuffer buffer) {
      int i = 0;

      for (int m = this.comparators.size(); i < m; i++) {
         this.comparators.get(i).write(buffer);
      }

      for (int ix = 0; ix < 7; ix++) {
         this.values[ix].write(buffer);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      int i = 0;

      for (int m = this.comparators.size(); i < m; i++) {
         this.comparators.get(i).read(buffer);
      }

      for (int ix = 0; ix < 7; ix++) {
         this.values[ix].read(buffer);
      }
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (int i = 0; i < 7; i++) {
         CompoundTag data = this.values[i].save(new CompoundTag());
         if (!data.isEmpty()) {
            data.putByte("slot", (byte)i);
            list.add(data);
         }
      }

      NBTUtils.put(nbt, "comparator_data", list);
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.ignore_update = true;

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("comparator_data", 10), CompoundTag.class)) {
         this.values[data.getInt("slot")].load(data);
      }
   }

   public boolean hasComparators() {
      return !this.comparators.isEmpty();
   }

   public int size() {
      return this.comparators.size();
   }

   public List<BaseComparator> getComparators() {
      List<BaseComparator> comps = CollectionUtils.<BaseComparator>createList();

      for (int i = 0; i < this.comparators.size(); i++) {
         BaseComparator comp = this.comparators.get(i);
         if (comp.visible()) {
            comps.add(comp);
         }
      }

      return comps;
   }

   public int getIndex(BaseComparator base) {
      return this.comparators.indexOf(base);
   }

   public BaseComparator getComparator(int index) {
      return index >= 0 && index < this.comparators.size() ? this.comparators.get(index) : null;
   }

   public boolean shouldSync() {
      return this.forceAll;
   }

   public void setSync(boolean value) {
      this.forceAll = value;
   }

   public void disableIgnore() {
      this.ignore_update = false;
   }

   public void onLoaded() {
      this.forceAll = true;
      this.update(true);
      this.forceAll = false;
      this.ignore_update = false;
   }
}
