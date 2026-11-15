package ic2.core.block.transport.fluid.graph;

import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
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
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class FluidGrid {
   Level world;
   FluidPaths paths = new FluidPaths();
   Long2ObjectMap<IFluidPipe> pipes = new Long2ObjectOpenHashMap();
   Map<IFluidPipe, FluidGrid.Connection> connections = CollectionUtils.<IFluidPipe, FluidGrid.Connection>createMap();
   Map<IFluidPipe, List<IFluidPipe.FluidOutput>> outputs = CollectionUtils.<IFluidPipe, List<IFluidPipe.FluidOutput>>createMap();
   Int2ObjectMap<FluidGraph> graphs = new Int2ObjectOpenHashMap();
   Long2IntMap pipesToGraph = new Long2IntOpenHashMap();
   IntSet dirtyGrids = new IntLinkedOpenHashSet();

   FluidGrid(Level world) {
      this.world = world;
   }

   public void onTick() {
      if (!this.dirtyGrids.isEmpty()) {
         int[] grids = this.dirtyGrids.toIntArray();
         this.dirtyGrids.clear();

         for (int grid : grids) {
            FluidGraph graph = (FluidGraph)this.graphs.get(grid);
            if (graph != null && !graph.pipes.isEmpty()) {
               graph.processChanges();
            }
         }
      }
   }

   public List<FluidGrid.FluidPath> getPaths(IFluidPipe pipe, Direction dir) {
      return new ObjectArrayList(this.paths.getPaths(new FluidGrid.DirSource(pipe, dir)));
   }

   public int pump(IFluidPipe pipe, Direction dir, FluidStack fluid, int pressure, FluidAction action) {
      return !fluid.isEmpty() && pressure > 0 ? this.sendFluids(this.paths.getPaths(new FluidGrid.DirSource(pipe, dir)), fluid, pressure, action) : 0;
   }

   public void updatePipe(IFluidPipe pipe) {
      if (this.pipes.containsKey(pipe.getPosition().asLong())) {
         this.removePipe(pipe);
         this.addPipe(pipe);
      }
   }

   public void addPipe(IFluidPipe pipe) {
      long pos = pipe.getPosition().asLong();
      if (!this.pipes.containsKey(pos)) {
         this.pipes.put(pos, pipe);
         this.update(pipe.getPosition());
         List<IFluidPipe.FluidOutput> outs = pipe.getOutputs();
         if (!outs.isEmpty()) {
            this.outputs.put(pipe, outs);
         }

         FluidGrid.Connection connect = new FluidGrid.Connection(pipe, this);
         this.connections.put(pipe, connect);
         IntSortedSet combine = new IntLinkedOpenHashSet();

         for (FluidGrid.FluidLink link : connect.getLinks()) {
            combine.add(this.pipesToGraph.get(link.targetPos.asLong()));
         }

         if (combine.isEmpty()) {
            FluidGraph graph = new FluidGraph(this);
            graph.addPipe(pipe);
            this.graphs.put(graph.getId(), graph);
         } else if (combine.size() == 1) {
            ((FluidGraph)this.graphs.get(combine.firstInt())).addPipe(pipe);
         } else {
            FluidGraph graph = new FluidGraph(this);
            this.graphs.put(graph.getId(), graph);
            IntBidirectionalIterator var13 = combine.iterator();

            while (var13.hasNext()) {
               int id = (Integer)var13.next();
               FluidGraph other = (FluidGraph)this.graphs.remove(id);
               graph.addGraph(other);
               other.destroy();
            }

            graph.addPipe(pipe);
            graph.finishMerge();
         }
      }
   }

   public void removePipe(IFluidPipe pipe) {
      long pos = pipe.getPosition().asLong();
      if (this.pipes.containsKey(pos)) {
         pipe = (IFluidPipe)this.pipes.remove(pos);
         this.update(pipe.getPosition());
         FluidGrid.Connection connect = this.connections.remove(pipe);
         if (connect != null) {
            connect.destroy(this);
            FluidGraph graph = (FluidGraph)this.graphs.get(this.pipesToGraph.remove(pos));
            switch (connect.getConnections()) {
               case 0:
                  graph.removePipe(pipe);
                  if (graph.pipes.isEmpty()) {
                     this.graphs.remove(graph.getId());
                  }
                  break;
               case 1:
                  graph.removePipe(pipe);
                  break;
               default:
                  graph.removePipe(pipe);
                  graph.markSplit();
            }

            List<IFluidPipe.FluidOutput> outputList = this.outputs.remove(pipe);
            if (outputList != null) {
               for (IFluidPipe.FluidOutput out : outputList) {
                  this.paths.removeSink(out);
               }
            }
         }
      }
   }

   public IFluidPipe getPipe(BlockPos pos) {
      return (IFluidPipe)this.pipes.get(pos.asLong());
   }

   void splitGrid(FluidGraph grid) {
      this.graphs.remove(grid.getId());
      List<FluidGraph> gridsToAdd = CollectionUtils.<FluidGraph>createList();
      Set<IFluidPipe> left = CollectionUtils.<IFluidPipe>createLinkedSet();
      Set<IFluidPipe> processed = CollectionUtils.<IFluidPipe>createLinkedSet();
      left.addAll(grid.pipes.keySet());

      while (!left.isEmpty()) {
         FluidGraph graph = new FluidGraph(this);
         graph.setFlag(2);
         PriorityQueue<IFluidPipe> toProcess = CollectionUtils.createInsertionQueue();
         toProcess.enqueue(left.iterator().next());

         while (!toProcess.isEmpty()) {
            IFluidPipe pipe = (IFluidPipe)toProcess.dequeue();
            if (this.pipes.containsKey(pipe.getPosition().asLong())) {
               left.remove(pipe);
               graph.addPipe(pipe);
               FluidGrid.Connection targets = this.connections.get(pipe);

               for (FluidGrid.FluidLink link : targets.getLinks()) {
                  IFluidPipe tile = link.target;
                  if (processed.add(tile) && left.remove(tile)) {
                     toProcess.enqueue(tile);
                  }
               }
            }
         }

         gridsToAdd.add(graph);
         this.graphs.put(graph.getId(), graph);
         graph.finishSplit();
      }

      grid.destroy();

      for (FluidGraph graph : gridsToAdd) {
         graph.paths.addSubSet();
      }
   }

   public Set<FluidGrid.FluidPath> generatePaths(FluidGrid.DirSource source, Set<IFluidPipe.FluidOutput> targetOutputs) {
      Map<IFluidPipe, FluidGrid.FluidBlock> reachedLinks = CollectionUtils.<IFluidPipe, FluidGrid.FluidBlock>createLinkedMap();
      LinkedList<IFluidPipe> toCheck = new LinkedList<>();
      toCheck.add(source.pos());
      Set<IFluidPipe.FluidOutput> outputsReached = new ObjectLinkedOpenHashSet(targetOutputs);

      while (!toCheck.isEmpty()) {
         IFluidPipe pipe = toCheck.pollFirst();

         for (IFluidPipe.FluidOutput output : this.outputs.getOrDefault(pipe, Collections.emptyList())) {
            if (pipe != source.pos() || output.dir != source.dir()) {
               outputsReached.remove(output);
            }
         }

         FluidGrid.FluidBlock block = reachedLinks.get(pipe);
         int pressure = block == null ? 0 : block.pressureRequired;

         for (FluidGrid.FluidLink link : this.connections.getOrDefault(pipe, FluidGrid.Connection.DEFAULT).getLinks(true)) {
            IFluidPipe target = link.target;
            if (target != source.pos()) {
               int extra = link.direction.getAxis().isVertical() ? (link.direction.getAxisDirection() == AxisDirection.POSITIVE ? 2 : 0) : 1;
               block = reachedLinks.get(target);
               if (block == null || block.pressureRequired > pressure + extra) {
                  reachedLinks.put(target, new FluidGrid.FluidBlock(link, pressure + extra));
                  toCheck.remove(target);
                  toCheck.add(target);
               }
            }
         }
      }

      targetOutputs.removeAll(outputsReached);
      if (targetOutputs.isEmpty()) {
         return Collections.emptySet();
      } else {
         Set<FluidGrid.FluidPath> outputs = CollectionUtils.<FluidGrid.FluidPath>createLinkedSet();

         for (IFluidPipe.FluidOutput outputx : targetOutputs) {
            IFluidPipe pipe = outputx.source;
            FluidGrid.FluidBlock block = reachedLinks.get(pipe);
            if (block != null || pipe == source.pos()) {
               FluidGrid.FluidPath path = new FluidGrid.FluidPath();
               path.resistance = Math.max(1, block == null ? 0 : block.pressureRequired);
               path.source = source;
               path.output = outputx;
               path.paths.add(pipe);
               if (block != null) {
                  while (true) {
                     IFluidPipe tile = block.source;
                     if (tile == source.pos()) {
                        path.paths.add(tile);
                        break;
                     }

                     path.paths.add(tile);
                     block = reachedLinks.get(tile);
                     if (block == null) {
                        IC2.PLATFORM
                           .displayError(
                              "An Fluid network pathfinding entry is corrupted.\nThis could happen due to incorrect Minecraft behavior or a bug.\n\n(Technical information: Fluid Block, tile entities below)\nE: "
                                 + source
                                 + " ("
                                 + source.pos().getPosition()
                                 + ")\nC: "
                                 + tile
                                 + " ("
                                 + tile.getPosition()
                                 + ")\nR: "
                                 + path.output
                                 + " ("
                                 + path.output.source.getPosition()
                                 + ")"
                           );
                        break;
                     }
                  }
               }

               outputs.add(path);
            }
         }

         return outputs;
      }
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

   ObjectList<FluidGrid.FluidPath> create(int ignored) {
      return CollectionUtils.createList();
   }

   int sendFluids(Set<FluidGrid.FluidPath> targetPumps, FluidStack stack, int pressure, FluidAction action) {
      if (targetPumps.isEmpty()) {
         return 0;
      } else {
         Fluid fluid = stack.getFluid();
         Int2ObjectMap<ObjectList<FluidGrid.FluidPath>> paths = new Int2ObjectAVLTreeMap();

         for (FluidGrid.FluidPath path : targetPumps) {
            if (path.resistance < pressure && path.output.isFluidAllowed(fluid)) {
               ((ObjectList)paths.computeIfAbsent(path.output.getPriority(), this::create)).add(path);
            }
         }

         if (paths.isEmpty()) {
            return 0;
         } else {
            boolean adding = action.execute();
            int send = 0;
            ObjectIterator var9 = paths.values().iterator();

            while (var9.hasNext()) {
               ObjectList<FluidGrid.FluidPath> activePaths = (ObjectList<FluidGrid.FluidPath>)var9.next();

               while (!activePaths.isEmpty()) {
                  int left = stack.getAmount() - send;
                  if (left <= 0) {
                     return send;
                  }

                  Collections.shuffle(activePaths);

                  while (left < activePaths.size()) {
                     activePaths.remove(activePaths.size() - 1);
                  }

                  int perTarget = left / activePaths.size();
                  int i = 0;

                  for (int m = activePaths.size(); i < m; i++) {
                     FluidGrid.FluidPath pathx = (FluidGrid.FluidPath)activePaths.get(i);
                     int added = pathx.output.handler.fill(new FluidStack(stack, perTarget), action);
                     send += added;
                     if (adding) {
                        pathx.addFluid(fluid, (long)added);
                     }

                     if (send >= stack.getAmount()) {
                        return send;
                     }

                     if (added < perTarget) {
                        activePaths.remove(i--);
                        m--;
                     }
                  }
               }
            }

            return send;
         }
      }
   }

   public FluidNet.TransportStats getStats(IFluidPipe tile) {
      Object2LongLinkedOpenHashMap<Fluid> fluidStats = new Object2LongLinkedOpenHashMap();
      long totalSend = 0L;
      FluidGraph graph = (FluidGraph)this.graphs.get(this.pipesToGraph.get(tile.getPosition().asLong()));
      if (graph != null) {
         for (FluidGrid.FluidPath path : graph.paths.getPathsForPipe(tile)) {
            path.sendFluid.forEach(fluidStats::addTo);
            totalSend += path.totalSend;
         }
      }

      return new FluidNet.TransportStats(fluidStats, totalSend);
   }

   static class Connection {
      private static FluidGrid.Connection DEFAULT = new FluidGrid.Connection();
      IFluidPipe pipe;
      Set<FluidGrid.FluidLink> receive = CollectionUtils.<FluidGrid.FluidLink>createLinkedSet();
      Set<FluidGrid.FluidLink> send = CollectionUtils.<FluidGrid.FluidLink>createLinkedSet();
      Set<FluidGrid.FluidLink> links = CollectionUtils.<FluidGrid.FluidLink>createLinkedSet();

      private Connection() {
      }

      public Connection(IFluidPipe pipe, FluidGrid grid) {
         this.pipe = pipe;
         long pos = pipe.getPosition().asLong();

         for (Direction dir : DirectionList.ALL) {
            FluidGrid.Connection connect = grid.connections.get(grid.pipes.get(BlockPos.offset(pos, dir)));
            if (connect != null) {
               if (pipe.canPushFluid(dir) && connect.pipe.canReceiveFluid(dir.getOpposite())) {
                  FluidGrid.FluidLink link = new FluidGrid.FluidLink(pipe, connect.pipe, dir);
                  this.add(link, true);
                  connect.add(link.invert(), false);
               }

               if (pipe.canReceiveFluid(dir) && connect.pipe.canPushFluid(dir.getOpposite())) {
                  FluidGrid.FluidLink link = new FluidGrid.FluidLink(pipe, connect.pipe, dir);
                  this.add(link, false);
                  connect.add(link.invert(), true);
               }
            }
         }
      }

      void add(FluidGrid.FluidLink link, boolean push) {
         this.links.add(link);
         (push ? this.send : this.receive).add(link);
      }

      public Set<FluidGrid.FluidLink> getLinks() {
         return this.links;
      }

      public Set<FluidGrid.FluidLink> getLinks(boolean push) {
         return push ? this.send : this.receive;
      }

      public int getConnections() {
         return this.links.size();
      }

      public void destroy(FluidGrid grid) {
         for (FluidGrid.FluidLink link : this.links) {
            FluidGrid.Connection other = grid.connections.get(link.target);
            if (other != null) {
               FluidGrid.FluidLink inv = link.invert();
               other.links.remove(inv);
               other.send.remove(inv);
               other.receive.remove(inv);
            }
         }
      }
   }

   public static record DirSource(IFluidPipe pos, Direction dir) {
      public static List<FluidGrid.DirSource> createSources(IFluidPipe pipe) {
         List<FluidGrid.DirSource> list = new ObjectArrayList(7);

         for (Direction dir : DirectionList.ALL) {
            list.add(new FluidGrid.DirSource(pipe, dir));
         }

         list.add(new FluidGrid.DirSource(pipe, null));
         return list;
      }
   }

   static class FluidBlock {
      IFluidPipe source;
      BlockPos sourcePos;
      IFluidPipe target;
      BlockPos targetPos;
      Direction direction;
      int pressureRequired;

      public FluidBlock(FluidGrid.FluidLink link, int pressureRequired) {
         this.source = link.source;
         this.sourcePos = link.sourcePos;
         this.target = link.target;
         this.targetPos = link.targetPos;
         this.direction = link.direction;
         this.pressureRequired = pressureRequired;
      }
   }

   static class FluidLink {
      public IFluidPipe source;
      public BlockPos sourcePos;
      public IFluidPipe target;
      public BlockPos targetPos;
      public Direction direction;
      FluidGrid.FluidLink invert;

      private FluidLink() {
      }

      public FluidLink(IFluidPipe source, IFluidPipe target, Direction direction) {
         this.source = source;
         this.sourcePos = source.getPosition();
         this.target = target;
         this.targetPos = target.getPosition();
         this.direction = direction;
         this.invert = new FluidGrid.FluidLink();
         this.invert.source = target;
         this.invert.sourcePos = this.targetPos;
         this.invert.target = source;
         this.invert.targetPos = this.sourcePos;
         this.invert.direction = direction.getOpposite();
         this.invert.invert = this;
      }

      public FluidGrid.FluidLink invert() {
         return this.invert;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.targetPos, this.direction);
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof FluidGrid.FluidLink other) ? false : other.targetPos.equals(this.targetPos) && other.direction == this.direction;
      }

      @Override
      public String toString() {
         return "Link: Source[" + this.source + "(" + this.sourcePos + ")], Target[" + this.target + "(" + this.targetPos + ")]";
      }
   }

   public static class FluidPath {
      FluidGrid.DirSource source;
      IFluidPipe.FluidOutput output;
      int resistance;
      Set<IFluidPipe> paths = CollectionUtils.<IFluidPipe>createSet();
      Object2LongLinkedOpenHashMap<Fluid> sendFluid = new Object2LongLinkedOpenHashMap();
      long totalSend = 0L;

      private void addFluid(Fluid fluid, long amount) {
         this.sendFluid.addTo(fluid, amount);
         this.totalSend += amount;
      }

      @Override
      public int hashCode() {
         return this.source.hashCode() + Long.hashCode(this.output.position) * 31;
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof FluidGrid.FluidPath path) ? false : path.source == this.source && path.output.position == this.output.position;
      }

      public BlockPos getTarget() {
         return BlockPos.of(this.output.position);
      }
   }
}
