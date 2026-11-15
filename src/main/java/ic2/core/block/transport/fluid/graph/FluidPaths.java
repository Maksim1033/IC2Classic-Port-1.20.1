package ic2.core.block.transport.fluid.graph;

import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FluidPaths {
   Map<FluidGrid.DirSource, Set<FluidGrid.FluidPath>> pumpPaths = CollectionUtils.<FluidGrid.DirSource, Set<FluidGrid.FluidPath>>createMap();
   Map<IFluidPipe.FluidOutput, Set<FluidGrid.FluidPath>> receivePaths = CollectionUtils.<IFluidPipe.FluidOutput, Set<FluidGrid.FluidPath>>createMap();
   Set<FluidGrid.FluidPath> allPaths = CollectionUtils.<FluidGrid.FluidPath>createLinkedSet();

   public void addPaths(FluidGrid.DirSource source, Set<FluidGrid.FluidPath> paths) {
      this.allPaths.addAll(paths);
      this.pumpPaths.put(source, new ObjectLinkedOpenHashSet(paths));

      for (FluidGrid.FluidPath path : paths) {
         Set<FluidGrid.FluidPath> targets = this.receivePaths.computeIfAbsent(path.output, k -> CollectionUtils.createLinkedSet());
         targets.add(path);
      }
   }

   public void addPaths(Set<FluidGrid.FluidPath> paths) {
      for (FluidGrid.FluidPath path : paths) {
         Set<FluidGrid.FluidPath> targets = this.receivePaths.computeIfAbsent(path.output, k -> CollectionUtils.createLinkedSet());
         targets.add(path);
      }
   }

   public void removeSource(FluidGrid.DirSource source) {
      Set<FluidGrid.FluidPath> paths = this.pumpPaths.remove(source);
      if (paths != null) {
         for (FluidGrid.FluidPath path : paths) {
            this.receivePaths.get(path.output).remove(path);
            this.allPaths.remove(path);
         }
      }
   }

   public void removeSink(IFluidPipe.FluidOutput sink) {
      Set<FluidGrid.FluidPath> paths = this.receivePaths.remove(sink);
      if (paths != null) {
         for (FluidGrid.FluidPath path : paths) {
            this.pumpPaths.get(path.source).remove(path);
            this.allPaths.remove(path);
         }
      }
   }

   public void removePaths(Collection<FluidGrid.FluidPath> paths) {
      for (FluidGrid.FluidPath path : paths) {
         Set<FluidGrid.FluidPath> source = this.pumpPaths.get(path.source);
         if (source != null) {
            source.remove(path);
         }

         Set<FluidGrid.FluidPath> sink = this.receivePaths.get(path.output);
         if (sink != null) {
            sink.remove(path);
         }

         this.allPaths.remove(path);
      }
   }

   public Set<FluidGrid.FluidPath> getPaths(FluidGrid.DirSource source) {
      return this.pumpPaths.getOrDefault(source, ObjectSets.emptySet());
   }

   public void clear() {
      this.allPaths.clear();
      this.receivePaths.clear();
      this.pumpPaths.clear();
   }

   public FluidPaths.FluidSubPaths createPaths() {
      return new FluidPaths.FluidSubPaths(this);
   }

   public static class FluidSubPaths {
      FluidPaths owner;
      Map<FluidGrid.DirSource, Set<FluidGrid.FluidPath>> pumpPaths = CollectionUtils.<FluidGrid.DirSource, Set<FluidGrid.FluidPath>>createMap();
      Map<IFluidPipe.FluidOutput, Set<FluidGrid.FluidPath>> receivePaths = CollectionUtils.<IFluidPipe.FluidOutput, Set<FluidGrid.FluidPath>>createMap();
      Set<FluidGrid.FluidPath> allPaths = CollectionUtils.<FluidGrid.FluidPath>createLinkedSet();

      public FluidSubPaths(FluidPaths owner) {
         this.owner = owner;
      }

      public void addPaths(FluidGrid.DirSource source, Set<FluidGrid.FluidPath> paths) {
         this.allPaths.addAll(paths);
         this.pumpPaths.put(source, new ObjectLinkedOpenHashSet(paths));

         for (FluidGrid.FluidPath path : paths) {
            Set<FluidGrid.FluidPath> targets = this.receivePaths.computeIfAbsent(path.output, k -> CollectionUtils.createLinkedSet());
            targets.add(path);
         }

         this.owner.addPaths(source, paths);
      }

      public void removeSource(FluidGrid.DirSource source) {
         Set<FluidGrid.FluidPath> paths = this.pumpPaths.remove(source);
         if (paths != null) {
            for (FluidGrid.FluidPath path : paths) {
               this.receivePaths.get(path.output).remove(path);
               this.allPaths.remove(path);
            }

            this.owner.removePaths(paths);
         }
      }

      public void removeSink(IFluidPipe.FluidOutput sink) {
         Set<FluidGrid.FluidPath> paths = this.receivePaths.remove(sink);
         if (paths != null) {
            for (FluidGrid.FluidPath path : paths) {
               this.pumpPaths.get(path.source).remove(path);
               this.allPaths.remove(path);
            }

            this.owner.removePaths(paths);
         }
      }

      public void removePaths(Collection<FluidGrid.FluidPath> paths) {
         for (FluidGrid.FluidPath path : paths) {
            Set<FluidGrid.FluidPath> source = this.pumpPaths.get(path.source);
            if (source != null) {
               source.remove(path);
            }

            Set<FluidGrid.FluidPath> sink = this.receivePaths.get(path.output);
            if (sink != null) {
               sink.remove(path);
            }

            this.allPaths.remove(path);
         }

         this.owner.removePaths(paths);
      }

      public void addSubSet() {
         this.owner.addPaths(this.allPaths);
      }

      public Set<FluidGrid.FluidPath> getSubPaths(Set<FluidGrid.DirSource> inputs, Set<IFluidPipe.FluidOutput> outputs) {
         Set<FluidGrid.FluidPath> paths = CollectionUtils.<FluidGrid.FluidPath>createLinkedSet();

         for (FluidGrid.FluidPath path : this.allPaths) {
            if (inputs.contains(path.source) && outputs.contains(path.output)) {
               paths.add(path);
            }
         }

         return paths;
      }

      public void transferPaths(Collection<FluidGrid.FluidPath> paths) {
         for (FluidGrid.FluidPath path : paths) {
            Set<FluidGrid.FluidPath> set = this.pumpPaths.computeIfAbsent(path.source, k -> CollectionUtils.createLinkedSet());
            set.add(path);
            set = this.receivePaths.computeIfAbsent(path.output, k -> CollectionUtils.createLinkedSet());
            set.add(path);
            this.allPaths.add(path);
         }
      }

      public Set<FluidGrid.FluidPath> getPaths(FluidGrid.DirSource source) {
         return this.pumpPaths.getOrDefault(source, Collections.emptySet());
      }

      public Set<FluidGrid.FluidPath> getPaths(IFluidPipe.FluidOutput output) {
         return this.receivePaths.getOrDefault(output, Collections.emptySet());
      }

      public List<FluidGrid.FluidPath> getPathsForPipe(IFluidPipe entry) {
         List<FluidGrid.FluidPath> paths = CollectionUtils.<FluidGrid.FluidPath>createList();

         for (FluidGrid.FluidPath path : this.allPaths) {
            if (path.paths.contains(entry)) {
               paths.add(path);
            }
         }

         return paths;
      }

      public Set<FluidGrid.FluidPath> getPaths() {
         return this.allPaths;
      }

      public void reset() {
         this.owner.removePaths(this.allPaths);
         this.pumpPaths.clear();
         this.receivePaths.clear();
         this.allPaths.clear();
      }

      public void clear() {
         this.pumpPaths.clear();
         this.receivePaths.clear();
         this.allPaths.clear();
      }
   }
}
