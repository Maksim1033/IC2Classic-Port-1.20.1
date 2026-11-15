package ic2.core.utils.helpers;

import com.google.common.annotations.Beta;
import com.mojang.datafixers.util.Either;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.platform.corehacks.mixins.server.ChunkHolderMixin;
import ic2.core.platform.corehacks.mixins.server.ChunkProviderMixin;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ChunkHolder.ChunkLoadingFailure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

public class AABBUtil {
   public static final int CHUNK_LOAD = 1;
   public static final int IGNORE_INVALID = 2;
   public static final int RANDOM_START = 4;
   public static final int HOLLOW_ITERATOR = 8;
   public static final int INSTANT_LOADER = 16;
   public static final int MAPPED_POSITIONS = 32;

   public static int countBlocks(Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions) {
      return getValidBlocks(world, start, Box.withRange(start, radius), filter, flags, directions, 2147483647).size();
   }

   public static int countBlocks(Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit) {
      return getValidBlocks(world, start, Box.withRange(start, radius), filter, flags, directions, limit).size();
   }

   public static int countBlocks(Level world, BlockPos start, Box box, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit) {
      return getValidBlocks(world, start, box, filter, flags, directions, limit).size();
   }

   public static AABBUtil.SearchResult getValidBlocks(
      Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions
   ) {
      return getValidBlocks(world, start, Box.withRange(start, radius), filter, flags, directions, 2147483647);
   }

   public static AABBUtil.SearchResult getValidBlocks(
      Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit
   ) {
      return getValidBlocks(world, start, Box.withRange(start, radius), filter, flags, directions, limit);
   }

   public static AABBUtil.QueuedSearcher createSubTask(
      Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions
   ) {
      return createSubTask(world, start, Box.withRange(start, radius), filter, flags, directions, 2147483647);
   }

   public static AABBUtil.QueuedSearcher createSubTask(
      Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit
   ) {
      return createSubTask(world, start, Box.withRange(start, radius), filter, flags, directions, limit);
   }

   public static AABBUtil.QueuedSearcher createSubTask(
      Level world, BlockPos start, Box box, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit
   ) {
      return new AABBUtil.QueuedSearcher(world, start, box, filter, flags, directions, limit);
   }

   @Beta
   public static void createOffThreadTask(
      Level world, BlockPos start, int radius, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, Consumer<AABBUtil.SearchResult> listener
   ) {
      createOffThreadTask(world, start, Box.withRange(start, radius), filter, flags, directions, 2147483647, listener);
   }

   @Beta
   public static void createOffThreadTask(
      Level world,
      BlockPos start,
      int radius,
      AABBUtil.IBlockFilter filter,
      int flags,
      DirectionList directions,
      int limit,
      Consumer<AABBUtil.SearchResult> listener
   ) {
      createOffThreadTask(world, start, Box.withRange(start, radius), filter, flags, directions, limit, listener);
   }

   @Beta
   public static void createOffThreadTask(
      Level world,
      BlockPos start,
      Box box,
      AABBUtil.IBlockFilter filter,
      int flags,
      DirectionList directions,
      int limit,
      Consumer<AABBUtil.SearchResult> listener
   ) {
      MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
      if (server == null) {
         throw new IllegalStateException("Only Usable on a Server!");
      } else {
         IC2.OFF_THREAD_WORKER
            .execute(
               new AABBUtil.ThreadedSearcher(
                  new WorldRegion(world, box, true, true), start, box, filter, directions, flags, limit, T -> server.execute(() -> listener.accept(T))
               )
            );
      }
   }

   public static void getBlockEntities(Level world, Consumer<BlockEntity> result) {
      if (world instanceof ServerLevel server) {
         getBlockEntities(server, result);
      }
   }

   public static void getBlockEntities(ServerLevel world, Consumer<BlockEntity> result) {
      ServerChunkCache cache = world.getChunkSource();

      for (ChunkHolder holder : ((ChunkProviderMixin)cache.chunkMap).getVisibleChunks()) {
         ChunkHolderMixin mixin = (ChunkHolderMixin)holder;
         LevelChunk chunk = mixin.getLoadingChunk();
         if (chunk == null) {
            Either<ChunkAccess, ChunkLoadingFailure> either = (Either<ChunkAccess, ChunkLoadingFailure>)holder.getFutureIfPresent(ChunkStatus.FULL)
               .getNow(null);
            if (either == null) {
               continue;
            }

            ChunkAccess testChunk = (ChunkAccess)either.left().orElse(null);
            if (!(testChunk instanceof LevelChunk)) {
               continue;
            }

            chunk = (LevelChunk)testChunk;
         }

         chunk.getBlockEntities().values().forEach(result);
      }
   }

   public static AABBUtil.SearchResult getValidBlocks(
      Level world, BlockPos start, Box box, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit
   ) {
      ObjectList<BlockPos> resultList = CollectionUtils.createList();
      Object2ObjectMap<Block, List<BlockPos>> mapped_result = (Object2ObjectMap<Block, List<BlockPos>>)((flags & 32) != 0
         ? CollectionUtils.<Block, List<BlockPos>>createLinkedMap()
         : Object2ObjectMaps.emptyMap());
      boolean map_results = (flags & 32) != 0;
      boolean chunkLoad = (flags & 1) != 0;
      WorldRegion reader = new WorldRegion(world, box, (flags & 1) != 0);
      if ((flags & 2) != 0) {
         for (BlockPos pos : (flags & 8) != 0 ? box.getHollowIterator() : box) {
            if ((chunkLoad || reader.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) && filter.isValid(reader, pos)) {
               BlockPos immutable = pos.immutable();
               resultList.add(immutable);
               mapResult(filter, reader, pos, map_results, mapped_result);
               if (resultList.size() >= limit) {
                  return new AABBUtil.SearchResult(resultList, mapped_result);
               }
            }
         }

         return new AABBUtil.SearchResult(resultList, mapped_result);
      } else {
         PriorityQueue<BlockPos> queue = new ObjectArrayFIFOQueue();
         Set<BlockPos> found = CollectionUtils.<BlockPos>createSet();

         for (Direction dir : (flags & 4) != 0 ? directions.getRandomIterator() : directions) {
            queue.enqueue(start.relative(dir));
         }

         while (!queue.isEmpty()) {
            BlockPos toProcess = (BlockPos)queue.dequeue();
            if (box.intersectsWith(toProcess)
               && found.add(toProcess)
               && (chunkLoad || reader.hasChunk(toProcess.getX() >> 4, toProcess.getZ() >> 4))
               && filter.isValid(reader, toProcess)) {
               resultList.add(toProcess);
               mapResult(filter, reader, toProcess, map_results, mapped_result);
               if (resultList.size() >= limit) {
                  return new AABBUtil.SearchResult(resultList, mapped_result);
               }

               for (Direction dir : directions) {
                  BlockPos posx = toProcess.relative(dir);
                  if (box.intersectsWith(posx) && !found.contains(posx)) {
                     queue.enqueue(posx);
                  }
               }
            }
         }

         return new AABBUtil.SearchResult(resultList, mapped_result);
      }
   }

   private static void mapResult(AABBUtil.IBlockFilter filter, LevelReader reader, BlockPos pos, boolean doMap, Map<Block, List<BlockPos>> mapper) {
      if (doMap) {
         if (filter instanceof AABBUtil.IBlockMapper) {
            ((AABBUtil.IBlockMapper)filter).mapBlocks(reader, pos, mapper);
            return;
         }

         mapResult(reader.getBlockState(pos).getBlock(), pos, mapper);
      }
   }

   public static void mapResult(Block block, BlockPos pos, Map<Block, List<BlockPos>> mapper) {
      List<BlockPos> positions = mapper.computeIfAbsent(block, k -> CollectionUtils.createList());
      positions.add(pos);
   }

   public static <T extends Entity> T getClosestEntity(List<T> entities, Vec3 center) {
      double closestDist = 1.7976931348623157E308;
      T closest = null;
      int i = 0;

      for (int m = entities.size(); i < m; i++) {
         T entity = (T)entities.get(i);
         double dist = entity.distanceToSqr(center);
         if (closestDist > dist) {
            closestDist = dist;
            closest = entity;
         }
      }

      return closest;
   }

   @FunctionalInterface
   public interface IBlockFilter {
      default boolean isValid(LevelReader world, BlockPos pos) {
         return this.isValid(world.getBlockState(pos));
      }

      boolean isValid(BlockState var1);
   }

   public interface IBlockMapper extends AABBUtil.IBlockFilter {
      void mapBlocks(LevelReader var1, BlockPos var2, Map<Block, List<BlockPos>> var3);
   }

   public static class QueuedSearcher {
      Level world;
      WorldRegion reader;
      BlockPos start;
      Box box;
      AABBUtil.IBlockFilter filter;
      DirectionList directions;
      PriorityQueue<BlockPos> queue = new ObjectArrayPriorityQueue();
      Set<BlockPos> found = CollectionUtils.<BlockPos>createSet();
      List<BlockPos> results = CollectionUtils.<BlockPos>createList();
      int limit;
      int flags;
      int speed = 100;

      QueuedSearcher(Level world, BlockPos start, Box box, AABBUtil.IBlockFilter filter, int flags, DirectionList directions, int limit) {
         this.limit = limit;
         this.world = world;
         this.start = start;
         this.box = box;
         this.filter = filter;
         this.directions = directions;
         this.flags = flags;
      }

      public AABBUtil.QueuedSearcher setSpeed(int blocksPerProcess) {
         this.speed = blocksPerProcess;
         return this;
      }

      public int size() {
         return this.queue.isEmpty() ? this.results.size() : -1;
      }

      public List<BlockPos> getPos() {
         return this.queue.isEmpty() ? this.results : null;
      }

      public boolean process(boolean result) {
         if (this.reader == null) {
            this.reader = new WorldRegion(this.world, this.box, (this.flags & 1) != 0);
            if ((this.flags & 2) != 0) {
               for (BlockPos pos : (this.flags & 8) != 0 ? this.box.getHollowIterator() : this.box) {
                  this.queue.enqueue(pos.immutable());
               }
            } else {
               for (Direction dir : (this.flags & 4) != 0 ? this.directions.getRandomIterator() : this.directions) {
                  this.queue.enqueue(this.start.relative(dir));
               }
            }

            if ((this.flags & 16) == 0) {
               return !result;
            }
         }

         boolean loading = (this.flags & 1) != 0;
         boolean invalid = (this.flags & 2) != 0;

         for (int i = 0; i < this.speed && !this.queue.isEmpty(); i++) {
            BlockPos pos = (BlockPos)this.queue.dequeue();
            if ((invalid || this.box.intersectsWith(pos) && this.found.add(pos))
               && (loading || this.reader.hasChunk(pos.getX() >> 4, pos.getZ() >> 4))
               && this.filter.isValid(this.reader, pos)) {
               this.results.add(pos);
               if (this.results.size() >= this.limit) {
                  this.queue.clear();
                  return result;
               }

               if (!invalid) {
                  for (Direction dir : this.directions) {
                     BlockPos next = pos.relative(dir);
                     if (this.box.intersectsWith(next) && !this.found.contains(next)) {
                        this.queue.enqueue(next);
                     }
                  }
               }
            }
         }

         return this.queue.isEmpty() == result;
      }
   }

   public static class SearchResult {
      ObjectList<BlockPos> positions;
      Object2ObjectMap<Block, List<BlockPos>> mappedPositions;

      public SearchResult(ObjectList<BlockPos> positions, Object2ObjectMap<Block, List<BlockPos>> mappedPositions) {
         this.positions = positions;
         this.mappedPositions = mappedPositions;
      }

      public int size() {
         return this.positions.size();
      }

      public ObjectList<BlockPos> getPositions() {
         return this.positions;
      }

      public Object2ObjectMap<Block, List<BlockPos>> getMappedPositions() {
         return this.mappedPositions;
      }
   }

   static class ThreadedSearcher implements Runnable {
      WorldRegion reader;
      BlockPos start;
      Box box;
      AABBUtil.IBlockFilter filter;
      DirectionList directions;
      int flags;
      int limit;
      Consumer<AABBUtil.SearchResult> result;

      public ThreadedSearcher(
         WorldRegion reader,
         BlockPos start,
         Box box,
         AABBUtil.IBlockFilter filter,
         DirectionList directions,
         int flags,
         int limit,
         Consumer<AABBUtil.SearchResult> result
      ) {
         this.reader = reader;
         this.start = start;
         this.box = box;
         this.filter = filter;
         this.directions = directions;
         this.flags = flags;
         this.limit = limit;
         this.result = result;
      }

      @Override
      public void run() {
         ObjectList<BlockPos> resultList = CollectionUtils.createList();
         Object2ObjectMap<Block, List<BlockPos>> mapped_result = (Object2ObjectMap<Block, List<BlockPos>>)((this.flags & 32) != 0
            ? CollectionUtils.<Block, List<BlockPos>>createLinkedMap()
            : Object2ObjectMaps.emptyMap());
         boolean map_results = (this.flags & 32) != 0;
         if ((this.flags & 2) != 0) {
            for (BlockPos pos : (this.flags & 8) != 0 ? this.box.getHollowIterator() : this.box) {
               if (this.reader.hasChunk(pos.getX() >> 4, pos.getZ() >> 4) && this.filter.isValid(this.reader, pos)) {
                  BlockPos immutable = pos.immutable();
                  resultList.add(immutable);
                  AABBUtil.mapResult(this.filter, this.reader, pos, map_results, mapped_result);
                  if (resultList.size() >= this.limit) {
                     this.result.accept(new AABBUtil.SearchResult(resultList, mapped_result));
                     return;
                  }
               }
            }

            this.result.accept(new AABBUtil.SearchResult(resultList, mapped_result));
         } else {
            PriorityQueue<BlockPos> queue = new ObjectArrayFIFOQueue();
            Set<BlockPos> found = CollectionUtils.<BlockPos>createSet();

            for (Direction dir : (this.flags & 4) != 0 ? this.directions.getRandomIterator() : this.directions) {
               queue.enqueue(this.start.relative(dir));
            }

            while (!queue.isEmpty()) {
               BlockPos toProcess = (BlockPos)queue.dequeue();
               if (this.box.intersectsWith(toProcess) && !found.contains(toProcess)) {
                  found.add(toProcess);
                  if (this.reader.hasChunk(toProcess.getX() >> 4, toProcess.getZ() >> 4) && this.filter.isValid(this.reader, toProcess)) {
                     resultList.add(toProcess);
                     AABBUtil.mapResult(this.filter, this.reader, toProcess, map_results, mapped_result);
                     if (resultList.size() >= this.limit) {
                        this.result.accept(new AABBUtil.SearchResult(resultList, mapped_result));
                        return;
                     }

                     for (Direction dir : this.directions) {
                        BlockPos posx = toProcess.relative(dir);
                        if (this.box.intersectsWith(posx) && !found.contains(posx)) {
                           queue.enqueue(posx);
                        }
                     }
                  }
               }
            }

            this.result.accept(new AABBUtil.SearchResult(resultList, mapped_result));
         }
      }
   }
}
