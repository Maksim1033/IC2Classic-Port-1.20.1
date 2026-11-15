package ic2.core.energy;

import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnergyNetPaths {
   Map<IEnergySource, Set<EnergyNetLocal.EnergyPath>> sourcePaths = CollectionUtils.<IEnergySource, Set<EnergyNetLocal.EnergyPath>>createMap();
   Map<IEnergySink, Set<EnergyNetLocal.EnergyPath>> sinkPaths = CollectionUtils.<IEnergySink, Set<EnergyNetLocal.EnergyPath>>createMap();
   Set<EnergyNetLocal.EnergyPath> allPaths = CollectionUtils.<EnergyNetLocal.EnergyPath>createLinkedSet();

   public void addSourcePaths(IEnergySource source, List<EnergyNetLocal.EnergyPath> paths) {
      Set<EnergyNetLocal.EnergyPath> set = this.sourcePaths.computeIfAbsent(source, k -> CollectionUtils.createLinkedSet());
      set.addAll(paths);

      for (EnergyNetLocal.EnergyPath path : paths) {
         Set<EnergyNetLocal.EnergyPath> sinkSet = this.sinkPaths.computeIfAbsent(path.target, k -> CollectionUtils.createLinkedSet());
         sinkSet.add(path);
         this.allPaths.add(path);
      }
   }

   public void addSinkPaths(IEnergySink sink, List<EnergyNetLocal.EnergyPath> paths) {
      Set<EnergyNetLocal.EnergyPath> set = this.sinkPaths.computeIfAbsent(sink, k -> CollectionUtils.createLinkedSet());
      set.addAll(paths);

      for (EnergyNetLocal.EnergyPath path : paths) {
         Set<EnergyNetLocal.EnergyPath> sinkSet = this.sourcePaths.computeIfAbsent(path.source, k -> CollectionUtils.createLinkedSet());
         sinkSet.add(path);
         this.allPaths.add(path);
      }
   }

   void addPaths(Collection<EnergyNetLocal.EnergyPath> paths) {
      for (EnergyNetLocal.EnergyPath path : paths) {
         Set<EnergyNetLocal.EnergyPath> set = this.sourcePaths.computeIfAbsent(path.source, k -> CollectionUtils.createLinkedSet());
         set.add(path);
         set = this.sinkPaths.computeIfAbsent(path.target, k -> CollectionUtils.createLinkedSet());
         set.add(path);
         this.allPaths.add(path);
      }
   }

   public void removeSource(IEnergySource source) {
      Set<EnergyNetLocal.EnergyPath> paths = this.sourcePaths.remove(source);
      if (paths != null) {
         for (EnergyNetLocal.EnergyPath path : paths) {
            this.sinkPaths.get(path.target).remove(path);
            this.allPaths.remove(path);
         }
      }
   }

   public void removeSink(IEnergySink sink) {
      Set<EnergyNetLocal.EnergyPath> paths = this.sinkPaths.remove(sink);
      if (paths != null) {
         for (EnergyNetLocal.EnergyPath path : paths) {
            this.sourcePaths.get(path.source).remove(path);
            this.allPaths.remove(path);
         }
      }
   }

   public void removePaths(Collection<EnergyNetLocal.EnergyPath> paths) {
      for (EnergyNetLocal.EnergyPath path : paths) {
         Set<EnergyNetLocal.EnergyPath> source = this.sourcePaths.get(path.source);
         if (source != null) {
            source.remove(path);
         }

         Set<EnergyNetLocal.EnergyPath> sink = this.sinkPaths.get(path.target);
         if (sink != null) {
            sink.remove(path);
         }

         this.allPaths.remove(path);
      }
   }

   public Set<EnergyNetLocal.EnergyPath> get(IEnergySource source) {
      return this.sourcePaths.getOrDefault(source, ObjectSets.emptySet());
   }

   public Set<EnergyNetLocal.EnergyPath> getReverse(IEnergySink sink) {
      return this.sinkPaths.getOrDefault(sink, ObjectSets.emptySet());
   }

   public Set<EnergyNetLocal.EnergyPath> getAllPaths() {
      return this.allPaths;
   }

   public void clear() {
      this.sinkPaths.clear();
      this.sourcePaths.clear();
      this.allPaths.clear();
   }

   public EnergyNetPaths.EnergyNetSubPaths createSubMap() {
      return new EnergyNetPaths.EnergyNetSubPaths(this);
   }

   public static class EnergyNetSubPaths {
      EnergyNetPaths paths;
      Map<IEnergySource, Set<EnergyNetLocal.EnergyPath>> subSourcePaths = CollectionUtils.<IEnergySource, Set<EnergyNetLocal.EnergyPath>>createMap();
      Map<IEnergySink, Set<EnergyNetLocal.EnergyPath>> subSinkPaths = CollectionUtils.<IEnergySink, Set<EnergyNetLocal.EnergyPath>>createMap();
      Set<EnergyNetLocal.EnergyPath> allSubPaths = CollectionUtils.<EnergyNetLocal.EnergyPath>createLinkedSet();

      EnergyNetSubPaths(EnergyNetPaths paths) {
         this.paths = paths;
      }

      public void addSourcePaths(IEnergySource source, List<EnergyNetLocal.EnergyPath> paths) {
         Set<EnergyNetLocal.EnergyPath> set = this.subSourcePaths.computeIfAbsent(source, k -> CollectionUtils.createLinkedSet());
         set.addAll(paths);

         for (EnergyNetLocal.EnergyPath path : paths) {
            Set<EnergyNetLocal.EnergyPath> sinkSet = this.subSinkPaths.computeIfAbsent(path.target, k -> CollectionUtils.createLinkedSet());
            sinkSet.add(path);
            this.allSubPaths.add(path);
         }

         this.paths.addSourcePaths(source, paths);
      }

      public void addSinkPaths(IEnergySink sink, List<EnergyNetLocal.EnergyPath> paths) {
         Set<EnergyNetLocal.EnergyPath> set = this.subSinkPaths.computeIfAbsent(sink, k -> CollectionUtils.createLinkedSet());
         set.addAll(paths);

         for (EnergyNetLocal.EnergyPath path : paths) {
            Set<EnergyNetLocal.EnergyPath> sinkSet = this.subSourcePaths.computeIfAbsent(path.source, k -> CollectionUtils.createLinkedSet());
            sinkSet.add(path);
            this.allSubPaths.add(path);
         }

         this.paths.addSinkPaths(sink, paths);
      }

      public void removeSource(IEnergySource source) {
         Set<EnergyNetLocal.EnergyPath> paths = this.subSourcePaths.remove(source);
         if (paths != null) {
            for (EnergyNetLocal.EnergyPath path : paths) {
               this.subSinkPaths.get(path.target).remove(path);
               this.allSubPaths.remove(path);
            }

            this.paths.removePaths(paths);
         }
      }

      public void removeSink(IEnergySink sink) {
         Set<EnergyNetLocal.EnergyPath> paths = this.subSinkPaths.remove(sink);
         if (paths != null) {
            for (EnergyNetLocal.EnergyPath path : paths) {
               this.subSourcePaths.get(path.source).remove(path);
               this.allSubPaths.remove(path);
            }

            this.paths.removePaths(paths);
         }
      }

      public boolean removeConductor(IEnergyConductor conductor) {
         List<EnergyNetLocal.EnergyPath> paths = this.getPathsForConductor(conductor);
         this.removePaths(paths);
         return !paths.isEmpty();
      }

      public void removePaths(Collection<EnergyNetLocal.EnergyPath> paths) {
         for (EnergyNetLocal.EnergyPath path : paths) {
            Set<EnergyNetLocal.EnergyPath> source = this.subSourcePaths.get(path.source);
            if (source != null) {
               source.remove(path);
            }

            Set<EnergyNetLocal.EnergyPath> sink = this.subSinkPaths.get(path.target);
            if (sink != null) {
               sink.remove(path);
            }

            this.allSubPaths.remove(path);
         }

         this.paths.removePaths(paths);
      }

      public Set<EnergyNetLocal.EnergyPath> get(IEnergySource source) {
         return this.subSourcePaths.getOrDefault(source, ObjectSets.emptySet());
      }

      public Set<EnergyNetLocal.EnergyPath> getReverse(IEnergySink sink) {
         return this.subSinkPaths.getOrDefault(sink, ObjectSets.emptySet());
      }

      public Set<EnergyNetLocal.EnergyPath> getAllPaths() {
         return this.allSubPaths;
      }

      public Set<EnergyNetLocal.EnergyPath> getSubPaths(Set<IEnergySource> sources, Set<IEnergyConductor> validCables, Set<IEnergySink> sinks) {
         Set<EnergyNetLocal.EnergyPath> paths = CollectionUtils.<EnergyNetLocal.EnergyPath>createLinkedSet();

         for (EnergyNetLocal.EnergyPath path : this.allSubPaths) {
            if (sources.contains(path.source) && sinks.contains(path.target) && validCables.containsAll(path.cables)) {
               paths.add(path);
            }
         }

         return paths;
      }

      public void transferPaths(Collection<EnergyNetLocal.EnergyPath> paths) {
         for (EnergyNetLocal.EnergyPath path : paths) {
            Set<EnergyNetLocal.EnergyPath> set = this.subSourcePaths.computeIfAbsent(path.source, k -> CollectionUtils.createLinkedSet());
            set.add(path);
            set = this.subSinkPaths.computeIfAbsent(path.target, k -> CollectionUtils.createLinkedSet());
            set.add(path);
            this.allSubPaths.add(path);
         }
      }

      public void addSubSet() {
         this.paths.addPaths(this.allSubPaths);
      }

      public List<EnergyNetLocal.EnergyPath> getPathsForConductor(IEnergyConductor entry) {
         List<EnergyNetLocal.EnergyPath> paths = new ArrayList<>();

         for (EnergyNetLocal.EnergyPath path : this.allSubPaths) {
            if (path.cableCheck.contains(entry)) {
               paths.add(path);
            }
         }

         return paths;
      }

      public int sourcePaths() {
         return this.subSourcePaths.values().stream().mapToInt(Set::size).sum();
      }

      public int sinkPaths() {
         return this.subSinkPaths.values().stream().mapToInt(Set::size).sum();
      }

      public int totalPathCount() {
         return this.allSubPaths.size();
      }

      public void reset() {
         this.paths.removePaths(this.allSubPaths);
         this.subSinkPaths.clear();
         this.subSourcePaths.clear();
         this.allSubPaths.clear();
      }

      public void clear() {
         this.subSinkPaths.clear();
         this.subSourcePaths.clear();
         this.allSubPaths.clear();
      }
   }
}
