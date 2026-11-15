package ic2.core.block.transport.fluid.graph;

import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidNet {
   public static final FluidNet INSTANCE = new FluidNet();
   static final Map<Level, FluidGrid> WORLDS = CollectionUtils.<Level, FluidGrid>createMap();

   public void addPipe(IFluidPipe pipe) {
      FluidGrid grid = this.getOrCreate(pipe.getWorldObj());
      if (grid != null) {
         grid.addPipe(pipe);
      }
   }

   public void removePipe(IFluidPipe pipe) {
      FluidGrid grid = this.getOrCreate(pipe.getWorldObj());
      if (grid != null) {
         grid.removePipe(pipe);
      }
   }

   public void updatePipe(IFluidPipe pipe) {
      FluidGrid grid = this.getOrCreate(pipe.getWorldObj());
      if (grid != null) {
         grid.updatePipe(pipe);
      }
   }

   public List<FluidGrid.FluidPath> getPaths(IFluidPipe pump, Direction dir) {
      FluidGrid grid = this.getOrCreate(pump.getWorldObj());
      return (List<FluidGrid.FluidPath>)(grid == null ? ObjectLists.emptyList() : grid.getPaths(pump, dir));
   }

   public int sendFluid(IFluidPipe pipe, Direction dir, FluidStack fluid, int pressure) {
      return this.sendFluid(pipe, dir, fluid, pressure, FluidAction.EXECUTE);
   }

   public int sendFluid(IFluidPipe pipe, Direction dir, FluidStack fluid, int pressure, FluidAction action) {
      if (fluid.isEmpty()) {
         return 0;
      } else {
         if (IC2.CONFIG.gassesTravelInfinite.get() && fluid.getFluid().is(Fluids.GASEOUS)) {
            pressure = 2147483647;
         }

         if (pressure <= 0) {
            return 0;
         } else {
            FluidGrid grid = WORLDS.get(pipe.getWorldObj());
            return grid != null ? grid.pump(pipe, dir, fluid, pressure, action) : 0;
         }
      }
   }

   public IFluidPipe getPipe(Level world, BlockPos pos) {
      FluidGrid grid = this.getOrCreate(world);
      return grid != null ? grid.getPipe(pos) : null;
   }

   public FluidNet.TransportStats getStats(IFluidPipe pipe) {
      FluidGrid grid = this.getOrCreate(pipe.getWorldObj());
      return grid != null ? grid.getStats(pipe) : new FluidNet.TransportStats();
   }

   FluidGrid getOrCreate(Level world) {
      if (world == null) {
         return null;
      } else {
         FluidGrid local = WORLDS.get(world);
         if (local == null) {
            local = new FluidGrid(world);
            WORLDS.put(world, local);
         }

         return local;
      }
   }

   public static void onTick(Level world) {
      FluidGrid grid = WORLDS.get(world);
      if (grid != null) {
         grid.onTick();
      }
   }

   public static void onWorldUnload(Level world) {
      WORLDS.remove(world);
   }

   public static void onServerStopped() {
      WORLDS.clear();
   }

   public static class TransportStats {
      Object2LongMap<Fluid> transferred = new Object2LongOpenHashMap();
      long totalTransferred = 0L;

      public TransportStats() {
      }

      public TransportStats(Object2LongMap<Fluid> transferred, long totalTransferred) {
         this.transferred = transferred;
         this.totalTransferred = totalTransferred;
      }

      public Object2LongMap<Fluid> getTransferred() {
         return this.transferred;
      }

      public long getTotalTransferred() {
         return this.totalTransferred;
      }
   }
}
