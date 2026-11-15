package ic2.core.block.transport.item.logistic;

import ic2.api.tiles.tubes.ILimiterTube;
import ic2.api.tiles.tubes.IProviderTube;
import ic2.api.tiles.tubes.IRequestTube;
import ic2.api.tiles.tubes.ITube;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.LongAverager;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TubeGrid {
   Long2ObjectMap<ITube> tubes = new Long2ObjectOpenHashMap();
   Object2ObjectMap<ITube, TubeGrid.Connection> connections = new Object2ObjectLinkedOpenHashMap();
   Long2IntMap tubeToGrid = new Long2IntOpenHashMap();
   IntSet dirtyGrids = new IntLinkedOpenHashSet();
   Int2ObjectMap<RequestGrid> grids = new Int2ObjectOpenHashMap();
   LongAverager totalLag = new LongAverager(200);
   Level world;

   public TubeGrid(Level world) {
      this.world = world;
   }

   public void onTick() {
      if (!this.dirtyGrids.isEmpty()) {
         int[] gridIds = this.dirtyGrids.toIntArray();
         this.dirtyGrids.clear();

         for (int gridId : gridIds) {
            RequestGrid grid = (RequestGrid)this.grids.get(gridId);
            if (grid != null && !grid.tubes.isEmpty()) {
               grid.processChanges();
            }
         }
      }

      long start = System.nanoTime();
      ObjectIterator var8 = this.grids.values().iterator();

      while (var8.hasNext()) {
         RequestGrid grid = (RequestGrid)var8.next();
         grid.tick();
      }

      this.totalLag.addEntry(System.nanoTime() - start);
   }

   public void printLag(ITube tube, Player player) {
      long pos = tube.getPosition().asLong();
      if (this.tubes.containsKey(pos)) {
         RequestGrid grid = (RequestGrid)this.grids.get(this.tubeToGrid.get(pos));
         if (grid != null) {
            StringBuilder builder = new StringBuilder();
            long lag = this.totalLag.getCachedAverage();
            builder.append("Global Lag: ms=").append(lag / 1000000L).append(", \u00b5s=").append(lag / 1000L).append(", ns=").append(lag).append("\n");
            lag = grid.totalLag.getCachedAverage();
            builder.append("Grid Lag: ms=").append(lag / 1000000L).append(", \u00b5s=").append(lag / 1000L).append(", ns=").append(lag).append("\n");
            player.sendSystemMessage(Component.literal(builder.toString()));
         }
      }
   }

   public void addTube(ITube tube) {
      long pos = tube.getPosition().asLong();
      if (!this.tubes.containsKey(pos)) {
         this.tubes.put(pos, tube);
         this.update(tube.getPosition());
         TubeGrid.Connection connect = new TubeGrid.Connection(tube);
         this.connections.put(tube, connect);
         IntSortedSet combine = new IntLinkedOpenHashSet();

         for (TubeGrid.TubeLink link : connect.getLinks()) {
            combine.add(this.tubeToGrid.get(link.targetPos.asLong()));
         }

         if (combine.isEmpty()) {
            RequestGrid graph = new RequestGrid(this);
            graph.addTube(tube);
            this.grids.put(graph.getId(), graph);
         } else if (combine.size() == 1) {
            ((RequestGrid)this.grids.get(combine.firstInt())).addTube(tube);
         } else {
            RequestGrid graph = new RequestGrid(this);
            this.grids.put(graph.getId(), graph);
            IntBidirectionalIterator var12 = combine.iterator();

            while (var12.hasNext()) {
               int id = (Integer)var12.next();
               RequestGrid other = (RequestGrid)this.grids.remove(id);
               graph.addGrid(other);
               other.destroy();
            }

            graph.addTube(tube);
            graph.finishMerge();
         }
      }
   }

   public void updateTube(ITube tube) {
      long pos = tube.getPosition().asLong();
      if (this.tubes.containsKey(pos)) {
         this.removeTube(tube);
         this.addTube(tube);
      }
   }

   public void removeTube(ITube tube) {
      long pos = tube.getPosition().asLong();
      if (this.tubes.containsKey(pos)) {
         tube = (ITube)this.tubes.remove(pos);
         this.update(tube.getPosition());
         TubeGrid.Connection connect = (TubeGrid.Connection)this.connections.remove(tube);
         connect.destroy();
         RequestGrid graph = (RequestGrid)this.grids.get(this.tubeToGrid.remove(pos));
         switch (connect.getLinks().size()) {
            case 0:
               graph.removeTube(tube);
               if (graph.tubes.isEmpty()) {
                  this.grids.remove(graph.getId());
               }
               break;
            case 1:
               graph.removeTube(tube);
               break;
            default:
               graph.removeTube(tube);
               graph.markSplit();
         }
      }
   }

   public void clearRequests(IRequestTube tube) {
      long pos = tube.getPosition().asLong();
      if (this.tubes.containsKey(pos)) {
         RequestGrid grid = (RequestGrid)this.grids.get(this.tubeToGrid.get(pos));
         if (grid != null) {
            grid.clearRequests(tube);
         }
      }
   }

   public Object2IntMap<Item> getPotentialItems(IRequestTube tube) {
      long pos = tube.getPosition().asLong();
      if (this.tubes.containsKey(pos)) {
         RequestGrid grid = (RequestGrid)this.grids.get(this.tubeToGrid.get(pos));
         return grid.getPotentialItems();
      } else {
         return Object2IntMaps.emptyMap();
      }
   }

   public void onRequestLost(ITube tube, ItemStack stack, UUID requestId) {
      long pos = tube.getPosition().asLong();
      if (this.tubes.containsKey(pos)) {
         RequestGrid grid = (RequestGrid)this.grids.get(this.tubeToGrid.get(pos));
         if (grid != null) {
            grid.onRequestLost(requestId, stack);
         }
      }
   }

   void splitGrid(RequestGrid grid) {
      this.grids.remove(grid.getId());
      List<RequestGrid> gridsToAdd = CollectionUtils.<RequestGrid>createList();
      ObjectSortedSet<ITube> left = CollectionUtils.createLinkedSet();
      Set<ITube> processed = CollectionUtils.<ITube>createLinkedSet();
      left.addAll(grid.tubes.keySet());

      while (!left.isEmpty()) {
         RequestGrid graph = new RequestGrid(this);
         graph.setFlag(2);
         PriorityQueue<ITube> toProcess = CollectionUtils.createInsertionQueue();
         toProcess.enqueue((ITube)left.first());

         while (!toProcess.isEmpty()) {
            ITube tube = (ITube)toProcess.dequeue();
            if (this.tubes.containsKey(tube.getPosition().asLong())) {
               left.remove(tube);
               graph.addTube(tube);
               TubeGrid.Connection targets = (TubeGrid.Connection)this.connections.get(tube);

               for (TubeGrid.TubeLink link : targets.getLinks()) {
                  ITube tile = link.target;
                  if (processed.add(tile) && left.remove(tile)) {
                     toProcess.enqueue(tile);
                  }
               }
            }
         }

         gridsToAdd.add(graph);
         this.grids.put(graph.getId(), graph);
         graph.finishSplit();
      }

      grid.destroy();
   }

   void update(BlockPos pos) {
      for (Direction facing : DirectionList.ALL) {
         BlockPos sidedPos = pos.relative(facing);
         if (this.world.isLoaded(sidedPos)) {
            this.world.neighborChanged(sidedPos, Blocks.AIR, pos);
         }
      }

      if (this.world.isLoaded(pos)) {
         this.world.neighborChanged(pos, Blocks.AIR, pos);
      }
   }

   public List<TubeGrid.TubePath> discoverPaths(IRequestTube request, Set<IProviderTube> targets) {
      Map<ITube, TubeGrid.TubeBlock> reachedLinks = CollectionUtils.<ITube, TubeGrid.TubeBlock>createLinkedMap();
      LinkedList<ITube> toCheck = new LinkedList<>();
      toCheck.add(request);
      Set<IProviderTube> targetsReached = new ObjectLinkedOpenHashSet(targets);

      while (!toCheck.isEmpty()) {
         ITube tube = toCheck.pollFirst();
         TubeGrid.TubeBlock block = reachedLinks.get(tube);
         int distanceWalked = block == null ? 0 : block.distanceWalked;

         for (TubeGrid.TubeLink link : ((TubeGrid.Connection)this.connections.get(tube)).getLinks()) {
            ITube target = link.target;
            if (target != request) {
               int extra = target instanceof ILimiterTube ? 10000 : 1;
               block = reachedLinks.get(target);
               if (block == null || block.distanceWalked > distanceWalked + extra) {
                  if (target instanceof IProviderTube provider) {
                     targetsReached.remove(provider);
                  }

                  reachedLinks.put(target, new TubeGrid.TubeBlock(link, distanceWalked + extra));
                  toCheck.remove(target);
                  toCheck.add(target);
               }
            }
         }
      }

      targets.removeAll(targetsReached);
      if (targets.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<TubeGrid.TubePath> results = CollectionUtils.<TubeGrid.TubePath>createList();

         for (IProviderTube target : targets) {
            TubeGrid.TubeBlock block = reachedLinks.get(target);
            if (block != null) {
               EnumSet<DyeColor> validColors = EnumSet.allOf(DyeColor.class);
               boolean hasLimits = false;

               while (true) {
                  ITube tube = block.source;
                  if (tube instanceof ILimiterTube limiter) {
                     validColors.retainAll(limiter.getValidColors());
                     hasLimits = true;
                  }

                  if (tube == request) {
                     break;
                  }

                  block = reachedLinks.get(tube);
                  if (block == null) {
                     IC2.PLATFORM
                        .displayError(
                           "An Tube network pathfinding entry is corrupted.\nThis could happen due to incorrect Minecraft behavior or a bug.\n\n(Technical information: Tube Block, tile entities below)\nE: "
                              + request
                              + " ("
                              + request.getPosition()
                              + ")\nC: "
                              + tube
                              + " ("
                              + tube.getPosition()
                              + ")\nR: "
                              + target
                              + " ("
                              + target.getPosition()
                              + ")"
                        );
                     break;
                  }
               }

               TubeGrid.TubePath path = new TubeGrid.TubePath();
               path.provider = target;
               path.validColors = hasLimits ? this.getLimitedColors(validColors) : -1;
               results.add(path);
            }
         }

         return results;
      }
   }

   private int getLimitedColors(EnumSet<DyeColor> valid) {
      int color = 0;

      for (DyeColor dye : valid) {
         color |= 1 << dye.getId();
      }

      return color;
   }

   class Connection {
      ITube tube;
      Set<TubeGrid.TubeLink> links = new ObjectLinkedOpenHashSet();

      public Connection(ITube tube) {
         this.tube = tube;
         long pos = tube.getPosition().asLong();

         for (Direction dir : DirectionList.ALL) {
            TubeGrid.Connection connect = (TubeGrid.Connection)TubeGrid.this.connections.get(TubeGrid.this.tubes.get(BlockPos.offset(pos, dir)));
            if (connect != null && tube.canConnect(connect.tube, dir) && connect.tube.canConnect(tube, dir.getOpposite())) {
               TubeGrid.TubeLink link = new TubeGrid.TubeLink(tube, connect.tube, dir);
               this.add(link);
               connect.add(link.invert());
            }
         }
      }

      void add(TubeGrid.TubeLink link) {
         this.links.add(link);
      }

      void remove(TubeGrid.TubeLink link) {
         this.links.remove(link);
      }

      public Set<TubeGrid.TubeLink> getLinks() {
         return this.links;
      }

      public void destroy() {
         for (TubeGrid.TubeLink link : this.links) {
            TubeGrid.Connection other = (TubeGrid.Connection)TubeGrid.this.connections.get(link.target);
            if (other != null) {
               other.remove(link.invert());
            }
         }
      }
   }

   static class TubeBlock {
      ITube source;
      BlockPos sourcePos;
      ITube target;
      BlockPos targetPos;
      Direction direction;
      int distanceWalked;

      public TubeBlock(TubeGrid.TubeLink link, int distanceWalked) {
         this.source = link.source;
         this.sourcePos = link.sourcePos;
         this.target = link.target;
         this.targetPos = link.targetPos;
         this.direction = link.direction;
         this.distanceWalked = distanceWalked;
      }
   }

   static class TubeLink {
      public ITube source;
      public BlockPos sourcePos;
      public ITube target;
      public BlockPos targetPos;
      public Direction direction;
      TubeGrid.TubeLink invert;

      private TubeLink() {
      }

      public TubeLink(ITube source, ITube target, Direction direction) {
         this.source = source;
         this.sourcePos = source.getPosition();
         this.target = target;
         this.targetPos = target.getPosition();
         this.direction = direction;
         this.invert = new TubeGrid.TubeLink();
         this.invert.source = target;
         this.invert.sourcePos = this.targetPos;
         this.invert.target = source;
         this.invert.targetPos = this.sourcePos;
         this.invert.direction = direction.getOpposite();
         this.invert.invert = this;
      }

      public TubeGrid.TubeLink invert() {
         return this.invert;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.targetPos, this.direction);
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof TubeGrid.TubeLink other) ? false : other.targetPos.equals(this.targetPos) && other.direction == this.direction;
      }

      @Override
      public String toString() {
         return "Link: Source[" + this.source + "(" + this.sourcePos + ")], Target[" + this.target + "(" + this.targetPos + ")]";
      }
   }

   public static class TubePath {
      int validColors;
      IProviderTube provider;
   }
}
