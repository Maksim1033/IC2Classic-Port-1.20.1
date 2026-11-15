package ic2.core.block.cables.mointor;

import ic2.api.util.DirectionList;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public class MonitorGrid {
   static Map<ResourceKey<Level>, Long2ObjectMap<MonitorGrid.SubGrid>> GRIDS = CollectionUtils.<ResourceKey<Level>, Long2ObjectMap<MonitorGrid.SubGrid>>createLinkedMap();

   public static void register(MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.computeIfAbsent(tile.getLevel().dimension(), k -> new Long2ObjectOpenHashMap());
      SortedSet<MonitorGrid.SubGrid> grids = CollectionUtils.<MonitorGrid.SubGrid>createLinkedSet();
      Direction face = tile.getFacing();
      long pos = tile.getBlockPos().asLong();

      for (Direction dir : DirectionList.ofAxis(face.getAxis()).invert()) {
         MonitorGrid.SubGrid subGrid = (MonitorGrid.SubGrid)grid.get(BlockPos.offset(pos, dir));
         if (subGrid != null && subGrid.facing == face && subGrid.gridId == tile.gridId) {
            grids.add(subGrid);
         }
      }

      if (grids.isEmpty()) {
         grid.put(pos, new MonitorGrid.SubGrid(tile, true));
      } else if (grids.size() == 1) {
         grid.put(tile.getBlockPos().asLong(), grids.first());
         grids.first().add(tile, true);
      } else {
         MonitorGrid.SubGrid newGrid = new MonitorGrid.SubGrid(tile, false);

         for (MonitorGrid.SubGrid sub : grids) {
            newGrid.addAll(sub.tiles.values());
         }

         newGrid.apply(grid);
      }
   }

   public static void update(MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.get(tile.getLevel().dimension());
      if (grid != null) {
         if (grid.containsKey(tile.getBlockPos().asLong())) {
            unregister(tile);
            register(tile);
         }
      }
   }

   public static void validateMaster(MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.get(tile.getLevel().dimension());
      if (grid != null) {
         MonitorGrid.SubGrid removed = (MonitorGrid.SubGrid)grid.get(tile.getBlockPos().asLong());
         if (removed != null) {
            removed.currentMaster = 0L;
            removed.updateGrid();
         }
      }
   }

   public static void setGridId(int gridId, MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.get(tile.getLevel().dimension());
      if (grid != null) {
         MonitorGrid.SubGrid removed = (MonitorGrid.SubGrid)grid.get(tile.getBlockPos().asLong());
         if (removed != null) {
            removed.setGridId(gridId);
         }
      }
   }

   public static void setGridColor(DyeColor color, MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.get(tile.getLevel().dimension());
      if (grid != null) {
         MonitorGrid.SubGrid removed = (MonitorGrid.SubGrid)grid.get(tile.getBlockPos().asLong());
         if (removed != null) {
            removed.setGridColor(color);
         }
      }
   }

   public static void setActive(boolean active, MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.get(tile.getLevel().dimension());
      if (grid != null) {
         MonitorGrid.SubGrid subGrid = (MonitorGrid.SubGrid)grid.get(tile.getBlockPos().asLong());
         if (subGrid != null) {
            subGrid.setActive(active);
         }
      }
   }

   public static void unregister(MonitorTileEntity tile) {
      Long2ObjectMap<MonitorGrid.SubGrid> grid = GRIDS.get(tile.getLevel().dimension());
      if (grid != null) {
         MonitorGrid.SubGrid removed = (MonitorGrid.SubGrid)grid.remove(tile.getBlockPos().asLong());
         if (removed != null) {
            if (removed.remove(tile.getBlockPos())) {
               removed.split(tile.getBlockPos(), grid);
            } else {
               removed.updateGrid();
            }
         }
      }
   }

   public static void onWorldUnloaded(Level world) {
      GRIDS.remove(world.dimension());
   }

   public static void onServerStopped() {
      GRIDS.clear();
   }

   public static class SubGrid {
      Long2ObjectSortedMap<MonitorTileEntity> tiles = new Long2ObjectLinkedOpenHashMap();
      long currentMaster;
      int gridId;
      Box box;
      Direction facing;
      Boolean lastState = null;

      private SubGrid(Direction face, int gridId) {
         this.facing = face;
         this.gridId = gridId;
      }

      private SubGrid(MonitorTileEntity master, boolean update) {
         this.currentMaster = master.getBlockPos().asLong();
         this.tiles.put(this.currentMaster, master);
         this.facing = master.getFacing();
         this.gridId = master.gridId;
         if (update) {
            this.updateGrid();
         }
      }

      private void setGridColor(DyeColor color) {
         if (this.currentMaster != 0L && !((MonitorTileEntity)this.tiles.get(this.currentMaster)).isMaster()) {
            this.currentMaster = 0L;
         }

         if (this.currentMaster == 0L) {
            this.findMaster();
         }

         if (this.currentMaster != 0L) {
            MutableBlockPos pos = new MutableBlockPos();
            ObjectIterator var3 = Long2ObjectMaps.fastIterable(this.tiles).iterator();

            while (var3.hasNext()) {
               Entry<MonitorTileEntity> entry = (Entry<MonitorTileEntity>)var3.next();
               if (this.box.intersectsWith(pos.set(entry.getLongKey()))) {
                  ((MonitorTileEntity)entry.getValue()).setScreenColor(color);
               }
            }
         }
      }

      private void setGridId(int id) {
         if (this.currentMaster != 0L && !((MonitorTileEntity)this.tiles.get(this.currentMaster)).isMaster()) {
            this.currentMaster = 0L;
         }

         if (this.currentMaster == 0L) {
            this.findMaster();
         }

         if (this.currentMaster != 0L) {
            MutableBlockPos pos = new MutableBlockPos();
            List<MonitorTileEntity> toUpdate = CollectionUtils.<MonitorTileEntity>createList();
            ObjectIterator grid = Long2ObjectMaps.fastIterable(this.tiles).iterator();

            while (grid.hasNext()) {
               Entry<MonitorTileEntity> entry = (Entry<MonitorTileEntity>)grid.next();
               if (this.box.intersectsWith(pos.set(entry.getLongKey()))) {
                  ((MonitorTileEntity)entry.getValue()).setGridId(id);
                  toUpdate.add((MonitorTileEntity)entry.getValue());
               }
            }

            for (MonitorTileEntity entity : toUpdate) {
               entity.updateGridState();
            }

            Long2ObjectMap<MonitorGrid.SubGrid> gridx = MonitorGrid.GRIDS.get(((MonitorTileEntity)this.tiles.get(this.currentMaster)).getLevel().dimension());

            for (MonitorTileEntity entity : toUpdate) {
               gridx.remove(entity.getBlockPos().asLong());
            }

            for (MonitorTileEntity value : toUpdate) {
               MonitorGrid.register(value);
            }
         }
      }

      private void setActive(boolean active) {
         if (this.lastState == null || this.lastState != active) {
            this.lastState = active;
            MutableBlockPos pos = new MutableBlockPos();
            ObjectIterator var3 = Long2ObjectMaps.fastIterable(this.tiles).iterator();

            while (var3.hasNext()) {
               Entry<MonitorTileEntity> entry = (Entry<MonitorTileEntity>)var3.next();
               ((MonitorTileEntity)entry.getValue()).setActive(this.box.intersectsWith(pos.set(entry.getLongKey())) && active);
            }
         }
      }

      private void add(MonitorTileEntity tile, boolean update) {
         this.tiles.put(tile.getBlockPos().asLong(), tile);
         if (update) {
            if (tile.isMaster()) {
               this.currentMaster = 0L;
            }

            this.updateGrid();
         } else {
            this.lastState = null;
            this.currentMaster = 0L;
         }
      }

      private void addAll(Collection<MonitorTileEntity> monitors) {
         this.currentMaster = 0L;
         this.lastState = null;

         for (MonitorTileEntity tile : monitors) {
            this.tiles.put(tile.getBlockPos().asLong(), tile);
         }
      }

      private void split(BlockPos startPos, Long2ObjectMap<MonitorGrid.SubGrid> grids) {
         long pos = startPos.asLong();
         DirectionList sidesToCheck = DirectionList.ofAxis(this.facing.getAxis()).invert();
         LongSortedSet left = new LongLinkedOpenHashSet();

         for (Direction dir : sidesToCheck) {
            if (this.tiles.containsKey(BlockPos.offset(pos, dir))) {
               left.add(BlockPos.offset(pos, dir));
            }
         }

         if (left.size() > 1) {
            LongPriorityQueue queue = new LongArrayFIFOQueue();
            queue.enqueue(left.firstLong());
            LongSet positionsFound = new LongOpenHashSet();

            while (!queue.isEmpty() && !left.isEmpty()) {
               long key = queue.dequeueLong();
               positionsFound.add(key);
               left.remove(key);

               for (Direction dirx : sidesToCheck) {
                  long other = BlockPos.offset(key, dirx);
                  if (!positionsFound.contains(other)) {
                     queue.enqueue(other);
                     left.remove(other);
                  }
               }
            }

            if (!left.isEmpty() || positionsFound.size() != this.tiles.size()) {
               left.addAll(this.tiles.keySet());

               while (!left.isEmpty()) {
                  queue.clear();
                  queue.enqueue(left.firstLong());
                  MonitorGrid.SubGrid otherGrid = new MonitorGrid.SubGrid(this.facing, this.gridId);

                  while (!queue.isEmpty()) {
                     long toProcess = queue.dequeueLong();
                     left.remove(toProcess);
                     otherGrid.add((MonitorTileEntity)this.tiles.get(toProcess), false);

                     for (Direction dirxx : sidesToCheck) {
                        long other = BlockPos.offset(toProcess, dirxx);
                        if (left.contains(other)) {
                           queue.enqueue(other);
                        }
                     }
                  }

                  otherGrid.apply(grids);
               }

               this.tiles.clear();
            }
         }
      }

      private void apply(Long2ObjectMap<MonitorGrid.SubGrid> entries) {
         LongIterator iter = this.tiles.keySet().iterator();

         while (iter.hasNext()) {
            entries.put(iter.nextLong(), this);
         }

         this.currentMaster = 0L;
         this.updateGrid();
      }

      private boolean remove(BlockPos pos) {
         long longPos = pos.asLong();
         this.tiles.remove(longPos);
         if (this.currentMaster == longPos) {
            this.currentMaster = 0L;
         }

         int count = 0;

         for (Direction dir : DirectionList.ofAxis(this.facing.getAxis()).invert()) {
            count += this.tiles.containsKey(BlockPos.offset(longPos, dir)) ? 1 : 0;
         }

         this.lastState = null;
         return count > 1;
      }

      private void updateGrid() {
         if (this.currentMaster != 0L && !((MonitorTileEntity)this.tiles.get(this.currentMaster)).isMaster()) {
            this.currentMaster = 0L;
         }

         if (this.currentMaster == 0L) {
            this.findMaster();
         }

         if (this.currentMaster != 0L) {
            Direction mainDir = MonitorBlock.getSearchDir(this.facing, false);
            Direction subDir = MonitorBlock.getSearchDir(this.facing, true);
            this.box = this.findBounds(mainDir, subDir);
            ((MonitorTileEntity)this.tiles.get(this.currentMaster)).setBounds(this.box.get(mainDir.getAxis()) + 1, this.box.get(subDir.getAxis()) + 1);
            this.lastState = null;
         }
      }

      private Box findBounds(Direction mainDir, Direction subDir) {
         int width = 0;
         int height = 0;

         for (long startPos = this.currentMaster; this.tiles.containsKey(startPos); width++) {
            startPos = BlockPos.offset(startPos, mainDir);
         }

         for (long var7 = this.currentMaster; this.tiles.containsKey(var7) && this.isValid(this.currentMaster, width - 1, height, mainDir, subDir); height++) {
            var7 = BlockPos.offset(var7, subDir);
         }

         return Box.fromPos(BlockPos.of(this.currentMaster), true).expand(mainDir, width - 1).expand(subDir, height - 1);
      }

      private boolean isValid(long start, int width, int height, Direction mainDir, Direction subDir) {
         for (BlockPos pos : Box.fromPos(BlockPos.of(start), true).expand(mainDir, width).expand(subDir, height)) {
            if (!this.tiles.containsKey(pos.asLong())) {
               return false;
            }
         }

         return true;
      }

      private void findMaster() {
         if (this.currentMaster == 0L) {
            ObjectIterator var1 = Long2ObjectMaps.fastIterable(this.tiles).iterator();

            while (var1.hasNext()) {
               Entry<MonitorTileEntity> entry = (Entry<MonitorTileEntity>)var1.next();
               if (((MonitorTileEntity)entry.getValue()).isMaster()) {
                  this.currentMaster = entry.getLongKey();
                  break;
               }
            }
         }
      }
   }
}
