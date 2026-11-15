package ic2.core.block.machines.logic.miner;

import ic2.api.items.electric.IMiningDrill;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MiningQueue {
   BlockPos masterPos;
   LongSet workers = new LongLinkedOpenHashSet();
   Long2ObjectMap<MinerTileEntity> activeWorkers = new Long2ObjectLinkedOpenHashMap();
   Long2ObjectMap<BooleanSupplier> pumpCache = new Long2ObjectLinkedOpenHashMap();
   LinkedList<IMiningTarget> queue = new LinkedList<>();
   int targetY;

   public MiningQueue(BlockPos masterPos, CompoundTag data) {
      this(masterPos);
      this.workers.addAll(LongArrayList.wrap(data.getLongArray("known_workers")));
      this.targetY = data.getInt("yLevel");

      for (CompoundTag nbt : NBTListWrapper.wrap(data.getList("queue", 10), CompoundTag.class)) {
         this.queue.add((IMiningTarget)(nbt.getByte("type") == 1 ? new FluidMiningTarget(nbt) : new BlockMiningTarget(nbt)));
      }
   }

   public MiningQueue(BlockPos masterPos) {
      this.masterPos = masterPos;
      this.targetY = masterPos.getY() - 1;
   }

   public void loadIfPossible(Level level) {
      MutableBlockPos pos = new MutableBlockPos();
      LongIterator iter = this.workers.iterator();

      while (iter.hasNext()) {
         if (level.isLoaded(pos.set(iter.nextLong()))) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MinerTileEntity) {
               MinerTileEntity miner = (MinerTileEntity)entity;
               if (!(entity instanceof RocketMinerTileEntity)) {
                  miner.setQueue(this);
                  this.pumpCache.put(pos.asLong(), miner::hasPumps);
               }
            }
         }
      }
   }

   public void addWorker(MinerTileEntity tile) {
      if (tile.setMaster(this.masterPos, this)) {
         this.workers.add(tile.getPosition().asLong());
         this.pumpCache.put(tile.getBlockPos().asLong(), tile::hasPumps);
      }
   }

   public void removeWorker(MinerTileEntity tile) {
      this.workers.remove(tile.getPosition().asLong());
      this.pumpCache.remove(tile.getPosition().asLong());
   }

   public void claimTarget(BlockPos pos, MinerTileEntity tile) {
      this.activeWorkers.putIfAbsent(pos.asLong(), tile);
   }

   public void releaseTarget(BlockPos pos, MinerTileEntity tile) {
      this.activeWorkers.remove(pos.asLong(), tile);
   }

   public boolean isAssisting(BlockPos pos, MinerTileEntity tile) {
      MinerTileEntity mine = (MinerTileEntity)this.activeWorkers.get(pos.asLong());
      return mine != null && mine != tile;
   }

   public void assist(MinerTileEntity tile, BlockPos pos, int extraProgress) {
      MinerTileEntity mine = (MinerTileEntity)this.activeWorkers.get(pos.asLong());
      if (mine != null) {
         mine.assistMiner(tile, extraProgress);
      }
   }

   public boolean hasPumps() {
      ObjectIterator var1 = this.pumpCache.values().iterator();

      while (var1.hasNext()) {
         BooleanSupplier prov = (BooleanSupplier)var1.next();
         if (prov.getAsBoolean()) {
            return true;
         }
      }

      return false;
   }

   public boolean assistRandomly(MinerTileEntity tile, ItemStack stack, IMiningDrill drill) {
      if (this.activeWorkers.isEmpty()) {
         return false;
      } else {
         ObjectIterator var4 = this.activeWorkers.values().iterator();

         while (var4.hasNext()) {
            MinerTileEntity other = (MinerTileEntity)var4.next();
            if (other.canAssist(tile)) {
               other.assistMiner(tile, stack, drill);
               return true;
            }
         }

         return false;
      }
   }

   public void onUnloaded(BlockPos pos) {
      Iterator<MinerTileEntity> iter = this.activeWorkers.values().iterator();

      while (iter.hasNext()) {
         if (iter.next().getBlockPos().equals(pos)) {
            iter.remove();
         }
      }
   }

   public int size() {
      return this.workers.size();
   }

   public void remove(Level level) {
      this.activeWorkers.clear();
      MutableBlockPos pos = new MutableBlockPos();
      LongIterator iter = this.workers.iterator();

      while (iter.hasNext()) {
         if (level.getBlockEntity(pos.set(iter.nextLong())) instanceof MinerTileEntity miner) {
            miner.clear(this.masterPos);
         }
      }

      this.workers.clear();
      this.pumpCache.clear();
   }

   public LinkedList<IMiningTarget> getTaskQueue() {
      return this.queue;
   }

   public BlockPos getMasterPos() {
      return this.masterPos;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      NBTUtils.putLongArray(tag, "known_workers", this.workers.toLongArray());
      tag.putInt("yLevel", this.targetY);
      ListTag list = new ListTag();

      for (IMiningTarget target : this.queue) {
         CompoundTag nbt = target.save();
         nbt.putByte("type", target.getID());
         list.add(nbt);
      }

      if (!list.isEmpty()) {
         tag.put("queue", list);
      }

      return tag;
   }

   public int getYLevel() {
      return this.targetY;
   }

   public void setYLevel(int yLevel) {
      this.targetY = yLevel;
   }
}
