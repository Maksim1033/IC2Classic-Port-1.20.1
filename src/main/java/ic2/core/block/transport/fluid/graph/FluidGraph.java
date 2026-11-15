package ic2.core.block.transport.fluid.graph;

import ic2.api.util.DirectionList;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Direction;

public class FluidGraph {
   static final int DIRTY = 1;
   static final int SPLIT = 2;
   static final int RESET = 4;
   static final int POTENTIAL_SPLIT = 8;
   static final BitSet USED_GRIDS = new BitSet();
   FluidGrid grid;
   FluidPaths.FluidSubPaths paths;
   int gridId;
   int flags;
   Map<IFluidPipe, FluidGraph.FluidConnection> pipes = CollectionUtils.<IFluidPipe, FluidGraph.FluidConnection>createLinkedMap();
   Set<IFluidPipe.FluidOutput> outputs = CollectionUtils.<IFluidPipe.FluidOutput>createLinkedSet();
   Set<FluidGrid.DirSource> inputs = CollectionUtils.<FluidGrid.DirSource>createLinkedSet();
   Set<IFluidPipe.FluidOutput> newOutputs = CollectionUtils.<IFluidPipe.FluidOutput>createLinkedSet();
   Set<FluidGrid.DirSource> newInputs = CollectionUtils.<FluidGrid.DirSource>createLinkedSet();
   Set<FluidGraph.FluidConnection> looseEnds = CollectionUtils.<FluidGraph.FluidConnection>createLinkedSet();

   public FluidGraph(FluidGrid grid) {
      this.grid = grid;
      this.paths = grid.paths.createPaths();
      this.gridId = USED_GRIDS.nextClearBit(1);
      USED_GRIDS.set(this.gridId);
   }

   public int getId() {
      return this.gridId;
   }

   public void destroy() {
      this.paths.reset();
      this.paths = null;
      USED_GRIDS.clear(this.gridId);
   }

   public void addSource(IFluidPipe pipe, DirectionList list) {
      if (list == null) {
         this.addSource(new FluidGrid.DirSource(pipe, null));
      } else {
         for (Direction dir : list) {
            this.addSource(new FluidGrid.DirSource(pipe, dir));
         }
      }
   }

   private void addSource(FluidGrid.DirSource source) {
      if (this.inputs.add(source)) {
         this.newInputs.add(source);
         this.markDirty(false);
      }
   }

   public void addPipe(IFluidPipe pipe) {
      if (!this.pipes.containsKey(pipe)) {
         this.grid.pipesToGraph.put(pipe.getPosition().asLong(), this.gridId);
         FluidGraph.FluidConnection connection = new FluidGraph.FluidConnection(pipe, this);
         this.pipes.put(pipe, connection);
         List<IFluidPipe.FluidOutput> list = this.grid.outputs.getOrDefault(pipe, Collections.emptyList());
         if (!list.isEmpty()) {
            this.outputs.addAll(list);
            this.newOutputs.addAll(list);
            this.markDirty(false);
         }

         for (Direction dir : pipe.getEmitterSources()) {
            this.addSource(new FluidGrid.DirSource(pipe, dir));
         }

         if (connection.shouldReset() && this.flags == 0) {
            this.markDirty(false);
            this.setFlag(4);
            this.newOutputs.addAll(this.outputs);
            this.newInputs.addAll(this.inputs);
         }
      }
   }

   public void addGraph(FluidGraph graph) {
      for (IFluidPipe pipe : graph.pipes.keySet()) {
         this.pipes.put(pipe, new FluidGraph.FluidConnection(pipe, this));
      }

      this.outputs.addAll(graph.outputs);
      this.inputs.addAll(graph.inputs);
   }

   public void finishMerge() {
      for (IFluidPipe pipe : this.pipes.keySet()) {
         this.grid.pipesToGraph.put(pipe.getPosition().asLong(), this.gridId);
      }

      this.setFlag(4);
      this.markDirty(true);
   }

   public void removePipe(IFluidPipe pipe) {
      FluidGraph.FluidConnection connection = this.pipes.remove(pipe);
      if (connection != null) {
         this.looseEnds.remove(connection);
         this.looseEnds.addAll(connection.links);
         connection.destroy();
         this.grid.outputs.getOrDefault(pipe, Collections.emptyList()).forEach(this.outputs::remove);
         FluidGrid.DirSource.createSources(pipe).forEach(this.inputs::remove);
         this.paths.removePaths(this.paths.getPathsForPipe(pipe));
      }
   }

   public void clearGrid() {
      this.paths.clear();
      this.paths = null;
   }

   public void markSplit() {
      this.markDirty(false);
      this.setFlag(8);
   }

   public void markDirty(boolean force) {
      if (!this.hasFlag(1) && !this.hasFlag(2) || force) {
         this.setFlag(1);
         this.grid.dirtyGrids.add(this.gridId);
      }
   }

   public void processChanges() {
      if (this.hasFlag(1)) {
         if (this.hasFlag(4)) {
            this.newOutputs.addAll(this.outputs);
            this.newInputs.addAll(this.inputs);
         }

         if (this.hasFlag(8)) {
            if (this.isGridSplit()) {
               this.grid.splitGrid(this);
               return;
            }

            this.newOutputs.addAll(this.outputs);
            this.newInputs.addAll(this.inputs);
         }

         if (!this.newOutputs.isEmpty() && !this.inputs.isEmpty() || !this.newInputs.isEmpty() && !this.outputs.isEmpty()) {
            this.updatePaths();
         }

         this.flags = 0;
      }
   }

   boolean isGridSplit() {
      if (this.looseEnds.isEmpty()) {
         return false;
      } else {
         PriorityQueue<FluidGraph.FluidConnection> todoList = new ObjectArrayFIFOQueue();
         todoList.enqueue(this.looseEnds.iterator().next());
         Set<FluidGraph.FluidConnection> foundConnections = CollectionUtils.<FluidGraph.FluidConnection>createSet();

         while (!todoList.isEmpty() && !this.looseEnds.isEmpty()) {
            foundConnections.add((FluidGraph.FluidConnection)todoList.first());
            this.looseEnds.remove(todoList.first());

            for (FluidGraph.FluidConnection connection : ((FluidGraph.FluidConnection)todoList.dequeue()).links) {
               if (foundConnections.add(connection)) {
                  todoList.enqueue(connection);
                  this.looseEnds.remove(connection);
               }
            }
         }

         return !this.looseEnds.isEmpty() && foundConnections.size() != this.pipes.size();
      }
   }

   public void finishSplit() {
      this.clearFlag(2);
      this.markDirty(true);
      this.setFlag(4);
   }

   public void updatePaths() {
      Object2ObjectMap<FluidGrid.DirSource, Set<IFluidPipe.FluidOutput>> targetOutputs = CollectionUtils.<FluidGrid.DirSource, Set<IFluidPipe.FluidOutput>>createLinkedMap();
      if (!this.newOutputs.isEmpty()) {
         for (FluidGrid.DirSource pump : this.inputs) {
            targetOutputs.put(pump, new ObjectOpenHashSet(this.outputs));
         }

         this.newOutputs.clear();
         this.newInputs.clear();
      } else {
         for (FluidGrid.DirSource pump : this.newInputs) {
            targetOutputs.put(pump, new ObjectOpenHashSet(this.outputs));
         }

         this.newInputs.clear();
      }

      ObjectIterator var6 = Object2ObjectMaps.fastIterable(targetOutputs).iterator();

      while (var6.hasNext()) {
         Entry<FluidGrid.DirSource, Set<IFluidPipe.FluidOutput>> entry = (Entry<FluidGrid.DirSource, Set<IFluidPipe.FluidOutput>>)var6.next();
         Set<FluidGrid.FluidPath> result = this.grid.generatePaths(entry.getKey(), entry.getValue());
         this.paths.removePaths(result);
         this.paths.addPaths(entry.getKey(), result);
      }
   }

   void setFlag(int flag) {
      this.flags |= flag;
   }

   void clearFlag(int flag) {
      this.flags &= ~flag;
   }

   boolean hasFlag(int flag) {
      return (this.flags & flag) != 0;
   }

   static class FluidConnection {
      Set<FluidGraph.FluidConnection> links = CollectionUtils.<FluidGraph.FluidConnection>createLinkedSet();

      public FluidConnection(IFluidPipe pipe, FluidGraph graph) {
         for (FluidGrid.FluidLink link : graph.grid.connections.get(pipe).getLinks()) {
            FluidGraph.FluidConnection other = graph.pipes.get(link.target);
            if (other != null) {
               other.links.add(this);
               this.links.add(other);
            }
         }
      }

      public boolean shouldReset() {
         return this.links.size() > 1;
      }

      public void destroy() {
         for (FluidGraph.FluidConnection connection : this.links) {
            connection.links.remove(this);
         }

         this.links.clear();
      }
   }
}
