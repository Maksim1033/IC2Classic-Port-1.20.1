package ic2.core.energy;

import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class EnergyNetGrid {
   static final int DIRTY = 1;
   static final int SPLIT = 2;
   static final int RESET = 4;
   static final int POTENTIAL_SPLIT = 8;
   static int GRID_ID = 1;
   final EnergyNetLocal local;
   final int gridID;
   EnergyNetPaths.EnergyNetSubPaths subPaths;
   Map<IEnergyConductor, EnergyNetGrid.WireConnection> cables = CollectionUtils.<IEnergyConductor, EnergyNetGrid.WireConnection>createLinkedMap();
   Set<IEnergySource> sources = CollectionUtils.<IEnergySource>createLinkedSet();
   Set<IEnergySink> sinks = CollectionUtils.<IEnergySink>createLinkedSet();
   Set<IEnergySource> newSources = CollectionUtils.<IEnergySource>createLinkedSet();
   Set<IEnergySink> newSinks = CollectionUtils.<IEnergySink>createLinkedSet();
   ObjectSet<EnergyNetGrid.WireConnection> loseEnds = CollectionUtils.createSet();
   int flags = 0;

   EnergyNetGrid(int gridID, EnergyNetLocal local) {
      this.gridID = gridID;
      this.local = local;
      this.subPaths = local.energySourcePaths.createSubMap();
   }

   public boolean isGridToLarge() {
      return this.toBig(this.local.limitations[0], this.cables.size())
         || this.toBig(this.local.limitations[1], this.sources.size())
         || this.toBig(this.local.limitations[2], this.sinks.size())
         || this.toBig(this.local.limitations[3], this.sinks.size() * this.sources.size());
   }

   public int getGridToBigReason() {
      if (this.toBig(this.local.limitations[0], this.cables.size())) {
         return 0;
      } else if (this.toBig(this.local.limitations[1], this.sources.size())) {
         return 1;
      } else if (this.toBig(this.local.limitations[2], this.sinks.size())) {
         return 2;
      } else {
         return this.toBig(this.local.limitations[3], this.sinks.size() * this.sources.size()) ? 3 : -1;
      }
   }

   private boolean toBig(int limit, int value) {
      return limit != -1 && value >= limit;
   }

   public String generateDebugString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Grid: ").append(this.gridID).append("\n");
      builder.append("Wires: ").append(this.cables.size()).append("\n");
      builder.append("Sources: ").append(this.sources.size()).append("\n");
      builder.append("Sinks: ").append(this.sinks.size()).append("\n");
      builder.append("All Paths: ").append(this.subPaths.totalPathCount()).append("\n");
      builder.append("Source Paths: ").append(this.subPaths.sourcePaths()).append("\n");
      builder.append("Sink Paths: ").append(this.subPaths.sinkPaths()).append("\n");
      return builder.toString();
   }

   public int getGridID() {
      return this.gridID;
   }

   public void addConductor(IEnergyConductor conductor) {
      if (!this.cables.containsKey(conductor)) {
         EnergyNetGrid.WireConnection connection = new EnergyNetGrid.WireConnection(conductor, this);
         this.cables.put(conductor, connection);
         this.local.addConductorToGrid(conductor, this.gridID);
         if (connection.getTotalConnections() > 1 && this.flags == 0) {
            this.markDirty(false);
            this.setFlag(4);
            this.newSinks.addAll(this.sinks);
            this.newSources.addAll(this.sources);
         }
      }
   }

   public void addSink(IEnergySink sink) {
      this.sinks.add(sink);
      this.newSinks.add(sink);
      this.markDirty(false);
   }

   public void addSinks(Collection<IEnergySink> toAdd) {
      this.sinks.addAll(toAdd);
      this.newSinks.addAll(toAdd);
      this.markDirty(false);
   }

   public void addSource(IEnergySource source) {
      this.sources.add(source);
      this.newSources.add(source);
      this.markDirty(false);
   }

   public void addSource(Collection<IEnergySource> toAdd) {
      this.sources.addAll(toAdd);
      this.newSources.addAll(toAdd);
      this.markDirty(false);
   }

   public void removeConductor(IEnergyConductor conductor) {
      EnergyNetGrid.WireConnection connection = this.cables.remove(conductor);
      if (connection != null) {
         this.loseEnds.remove(connection);
         ObjectIterators.pour(connection.iterator(), this.loseEnds);
         connection.destroy();
      }
   }

   public void removeSink(IEnergySink sink) {
      this.sinks.remove(sink);
      this.newSinks.remove(sink);
      this.subPaths.removeSink(sink);
      this.markDirty(false);
      this.local.recalculateSources = true;
   }

   public void removeSource(IEnergySource source) {
      this.sources.remove(source);
      this.newSources.remove(source);
      this.subPaths.removeSource(source);
      this.markDirty(false);
      this.local.recalculateSources = true;
   }

   public void addGrid(EnergyNetGrid grid) {
      this.sinks.addAll(grid.sinks);
      this.sources.addAll(grid.sources);

      for (IEnergyConductor conductor : grid.cables.keySet()) {
         this.cables.put(conductor, new EnergyNetGrid.WireConnection(conductor, this));
      }
   }

   public void finishCombining() {
      for (IEnergyConductor conductor : this.cables.keySet()) {
         this.local.addConductorToGrid(conductor, this.gridID);
      }

      this.setFlag(4);
      this.markDirty(true);
   }

   public void destroyGrid() {
      this.subPaths.reset();
      this.subPaths = null;
   }

   public void clearGrid() {
      this.subPaths.clear();
      this.subPaths = null;
   }

   public void markSplit() {
      this.markDirty(false);
      this.setFlag(8);
   }

   public void markDirty(boolean force) {
      if (!this.hasFlag(1) && !this.hasFlag(2) || force) {
         this.setFlag(1);
         this.local.markGridDirty(this.gridID);
      }
   }

   public void processChanges() {
      if (this.hasFlag(1)) {
         if (this.hasFlag(4)) {
            this.subPaths.reset();
            this.newSinks.addAll(this.sinks);
            this.newSources.addAll(this.sources);
         }

         if (this.hasFlag(8)) {
            if (this.isGridSplit()) {
               this.local.splitGrid(this);
               return;
            }

            this.subPaths.reset();
            this.newSinks.addAll(this.sinks);
            this.newSources.addAll(this.sources);
         }

         if (this.isGridToLarge()) {
            this.subPaths.reset();
            this.newSinks.clear();
            this.newSources.clear();
         }

         if (!this.newSinks.isEmpty() && !this.sources.isEmpty() || !this.newSources.isEmpty() && !this.sinks.isEmpty()) {
            this.updatePaths();
         }

         this.flags = 0;
      }
   }

   boolean isDoingFullRecalculation() {
      return this.newSinks.size() == this.sinks.size() || this.newSources.size() == this.sources.size();
   }

   boolean shouldDoSinkBasedPathFinding() {
      return this.sinks.size() * 2 < this.sources.size();
   }

   void updatePaths() {
      if (this.isGridToLarge()) {
         this.newSinks.clear();
         this.newSources.clear();
      } else {
         this.local.recalculateSources = true;
         if (this.isDoingFullRecalculation()) {
            this.newSinks.clear();
            this.newSources.clear();
            if (this.shouldDoSinkBasedPathFinding()) {
               for (IEnergySink sink : this.sinks) {
                  List<EnergyNetLocal.EnergyPath> paths = this.local.discoverPaths(sink, this.sources, true, 2147483647);
                  this.subPaths.removePaths(paths);
                  this.subPaths.addSinkPaths(sink, paths);
               }
            } else {
               for (IEnergySource source : this.sources) {
                  List<EnergyNetLocal.EnergyPath> paths = this.local.discoverPaths(source, this.sinks, false, source.getMaxEnergyOutput());
                  this.subPaths.removePaths(paths);
                  this.subPaths.addSourcePaths(source, paths);
               }
            }
         } else {
            Set<IEnergyTile> sourceTargets = new ObjectOpenHashSet(this.sinks);
            Map<IEnergySink, Set<IEnergyTile>> sinkList = CollectionUtils.<IEnergySink, Set<IEnergyTile>>createMap();

            for (IEnergySink sink : this.newSinks) {
               sinkList.put(sink, new ObjectOpenHashSet(this.sources));
            }

            this.newSinks.clear();

            for (IEnergySource source : this.newSources) {
               List<EnergyNetLocal.EnergyPath> paths = this.local.discoverPaths(source, sourceTargets, false, source.getMaxEnergyOutput());
               this.subPaths.removePaths(paths);
               this.subPaths.addSourcePaths(source, paths);

               for (EnergyNetLocal.EnergyPath path : paths) {
                  Set<IEnergyTile> sinkTodo = sinkList.get(path.target);
                  if (sinkTodo != null) {
                     sinkTodo.remove(source);
                     if (sinkTodo.isEmpty()) {
                        sinkList.remove(path.target);
                     }
                  }
               }
            }

            this.newSources.clear();

            for (Entry<IEnergySink, Set<IEnergyTile>> entry : sinkList.entrySet()) {
               List<EnergyNetLocal.EnergyPath> paths = this.local.discoverPaths(entry.getKey(), entry.getValue(), true, 2147483647);
               this.subPaths.removePaths(paths);
               this.subPaths.addSinkPaths(entry.getKey(), paths);
            }
         }
      }
   }

   boolean isGridSplit() {
      if (this.loseEnds.isEmpty()) {
         return false;
      } else {
         PriorityQueue<EnergyNetGrid.WireConnection> todoList = new ObjectArrayFIFOQueue();
         todoList.enqueue((EnergyNetGrid.WireConnection)this.loseEnds.iterator().next());
         Set<EnergyNetGrid.WireConnection> foundConnections = CollectionUtils.<EnergyNetGrid.WireConnection>createSet();

         while (!todoList.isEmpty() && !this.loseEnds.isEmpty()) {
            foundConnections.add((EnergyNetGrid.WireConnection)todoList.first());
            this.loseEnds.remove(todoList.first());

            for (EnergyNetGrid.WireConnection connection : (EnergyNetGrid.WireConnection)todoList.dequeue()) {
               if (foundConnections.add(connection)) {
                  todoList.enqueue(connection);
                  this.loseEnds.remove(connection);
               }
            }
         }

         return !this.loseEnds.isEmpty() && foundConnections.size() != this.cables.size();
      }
   }

   void finishSplit() {
      if (this.isGridToLarge()) {
         this.newSinks.clear();
         this.newSources.clear();
      } else {
         Object2ObjectMap<IEnergySource, Set<IEnergySink>> sourceTodo = CollectionUtils.<IEnergySource, Set<IEnergySink>>createLinkedMap();

         for (IEnergySource source : this.sources) {
            Set<IEnergySink> stored = new ObjectLinkedOpenHashSet(this.sinks);

            for (EnergyNetLocal.EnergyPath path : this.subPaths.get(source)) {
               stored.remove(path.target);
            }

            if (!stored.isEmpty()) {
               Iterator<IEnergySink> iter = stored.iterator();

               while (iter.hasNext()) {
                  if (this.local.connectivity.get(iter.next()).getEmitters().isEmpty()) {
                     iter.remove();
                  }
               }

               if (!stored.isEmpty()) {
                  sourceTodo.put(source, stored);
               }
            }
         }

         for (IEnergySink sink : this.sinks) {
            Set<IEnergySource> stored = new ObjectLinkedOpenHashSet(this.sources);

            for (EnergyNetLocal.EnergyPath path : this.subPaths.getReverse(sink)) {
               stored.remove(path.source);
            }

            if (!stored.isEmpty()) {
               for (IEnergySource source : stored) {
                  if (!this.local.connectivity.get(source).getReceivers().isEmpty()) {
                     Set<IEnergySink> list = (Set<IEnergySink>)sourceTodo.get(source);
                     if (list == null) {
                        list = CollectionUtils.<IEnergySink>createLinkedSet();
                        sourceTodo.put(source, list);
                     }

                     list.add(sink);
                  }
               }
            }
         }

         this.newSinks.clear();
         this.newSources.clear();
         if (!sourceTodo.isEmpty()) {
            ObjectIterator var9 = Object2ObjectMaps.fastIterable(sourceTodo).iterator();

            while (var9.hasNext()) {
               Entry<IEnergySource, Set<IEnergySink>> entry = (Entry<IEnergySource, Set<IEnergySink>>)var9.next();
               IEnergySource sourcex = entry.getKey();
               this.subPaths.addSourcePaths(sourcex, this.local.discoverPaths(sourcex, entry.getValue(), false, sourcex.getMaxEnergyOutput()));
            }
         }

         this.clearFlag(2);
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

   @Override
   public int hashCode() {
      return this.gridID;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof EnergyNetGrid ? ((EnergyNetGrid)obj).gridID == this.gridID : false;
   }

   static EnergyNetGrid createNewGrid(EnergyNetLocal local) {
      return new EnergyNetGrid(GRID_ID++, local);
   }

   static class WireConnection implements Iterable<EnergyNetGrid.WireConnection> {
      Set<EnergyNetGrid.WireConnection> links = CollectionUtils.<EnergyNetGrid.WireConnection>createLinkedSet();

      public WireConnection(IEnergyConductor owner, EnergyNetGrid local) {
         for (EnergyNetLocal.EnergyLink link : local.local.connectivity.get(owner).getAll()) {
            if (link.target instanceof IEnergyConductor) {
               EnergyNetGrid.WireConnection other = local.cables.get(link.target);
               if (other != null && other.add(this)) {
                  this.add(other);
               }
            }
         }
      }

      public void destroy() {
         for (EnergyNetGrid.WireConnection entry : this.links) {
            entry.remove(this);
         }

         this.links.clear();
      }

      public int getTotalConnections() {
         return this.links.size();
      }

      public boolean add(EnergyNetGrid.WireConnection other) {
         return this.links.add(other);
      }

      public void remove(EnergyNetGrid.WireConnection other) {
         this.links.remove(other);
      }

      @Override
      public Iterator<EnergyNetGrid.WireConnection> iterator() {
         return this.links.iterator();
      }
   }
}
