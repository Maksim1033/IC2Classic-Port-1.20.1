package ic2.core.block.transport.fluid.helper;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.util.DirectionList;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.IFluidPipe;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidCache<T extends BaseTileEntity & IFluidPipe> implements INetworkDataBuffer {
   final FluidCacheEntry[] entries = new FluidCacheEntry[6];
   final String variableName;
   final T tile;
   final ICache<IFluidHandler> cache;
   final BiPredicate<Direction, IFluidPipe> canConnectCheck;
   boolean addedToFluidNet;
   boolean updatingFluidNet;
   int tanks;

   public FluidCache(String variableName, T tile, ICache<IFluidHandler> cache, BiPredicate<Direction, IFluidPipe> canConnectCheck) {
      this.variableName = variableName;
      this.tile = tile;
      this.cache = cache;
      this.canConnectCheck = canConnectCheck;

      for (Direction dir : DirectionList.ALL) {
         this.entries[dir.get3DDataValue()] = new FluidCacheEntry(tile, dir, cache);
      }
   }

   public void remove() {
      if (!this.tile.isRendering() && this.addedToFluidNet) {
         this.addedToFluidNet = false;
         FluidNet.INSTANCE.removePipe(this.tile);
      }
   }

   public void add() {
      if (!this.tile.isRendering() && !this.addedToFluidNet) {
         this.addedToFluidNet = true;
         this.updatingFluidNet = true;
         FluidNet.INSTANCE.addPipe(this.tile);
         this.updatingFluidNet = false;
      }
   }

   public void update() {
      this.updatingFluidNet = true;
      FluidNet.INSTANCE.updatePipe(this.tile);
      this.updatingFluidNet = false;
   }

   public void protect(Runnable run) {
      this.updatingFluidNet = true;
      run.run();
      this.updatingFluidNet = false;
   }

   public boolean isAdded() {
      return this.addedToFluidNet;
   }

   public void setAdded(boolean value) {
      this.addedToFluidNet = value;
   }

   public int updateConnectivity() {
      DirectionList dirs = DirectionList.EMPTY;

      for (Direction dir : DirectionList.ALL) {
         IFluidPipe pipe = FluidNet.INSTANCE.getPipe(this.tile.getLevel(), this.tile.getBlockPos().relative(dir));
         if (pipe != null && this.canConnectCheck.test(dir, pipe)) {
            dirs = dirs.add(dir);
         }
      }

      int newTanks = this.cache.getPresentSides().getCode();
      if (this.addedToFluidNet && !this.updatingFluidNet && this.tanks != newTanks) {
         this.update();
         this.tanks = newTanks;
         if (this.variableName != null) {
            this.tile.updateTileField(this.variableName);
         }
      }

      return dirs.getCode();
   }

   public List<IFluidPipe.FluidOutput> getOutputs() {
      for (Direction dir : DirectionList.ofNumber(this.tanks).remove(this.cache.getPresentSides())) {
         this.entries[dir.get3DDataValue()].updateOutput();
      }

      if (this.cache.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<IFluidPipe.FluidOutput> outs = CollectionUtils.<IFluidPipe.FluidOutput>createList();
         DirectionList newConnections = DirectionList.EMPTY;

         for (Direction dir : this.cache.getPresentSides()) {
            IFluidPipe pipe = FluidNet.INSTANCE.getPipe(this.tile.getLevel(), this.tile.getBlockPos().relative(dir));
            FluidCacheEntry entry = this.entries[dir.get3DDataValue()];
            if ((pipe == null || pipe.isFluidAcceptor()) && entry.updateOutput()) {
               newConnections = newConnections.add(dir);
               if (entry.hasOutput()) {
                  outs.add(entry.getOutput());
               }
            }
         }

         if (newConnections.getCode() != this.tanks) {
            this.tanks = newConnections.getCode();
            if (this.variableName != null) {
               this.tile.updateTileField(this.variableName);
            }
         }

         return outs;
      }
   }

   public FluidCacheEntry getDir(Direction dir) {
      return this.entries[dir.get3DDataValue()];
   }

   public FluidCacheEntry get(int index) {
      return this.entries[index];
   }

   public IFluidPipe.FluidOutput get(Direction dir) {
      return this.entries[dir.get3DDataValue()].getOutput();
   }

   public int getTanks() {
      return this.tanks;
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeByte((byte)this.tanks);
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.tanks = buffer.readByte();
   }

   public CompoundTag save(CompoundTag nbt, boolean hasFluid) {
      NBTUtils.putByte(nbt, "tanks", this.tanks, 0);
      ListTag list = new ListTag();

      for (int i = 0; i < 6; i++) {
         CompoundTag data = this.entries[i].save(new CompoundTag());
         if (!data.isEmpty()) {
            data.putByte("index", (byte)i);
            list.add(data);
         }
      }

      NBTUtils.putIf(nbt, "settings", list, hasFluid);
      return nbt;
   }

   public void load(CompoundTag nbt) {
      this.tanks = nbt.getInt("tanks");

      for (int i = 0; i < 6; i++) {
         this.entries[i].clear();
      }

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("settings", 10), CompoundTag.class)) {
         int index = data.getInt("index");
         if (index >= 0 && index < 6) {
            this.entries[index].load(data);
         }
      }
   }
}
